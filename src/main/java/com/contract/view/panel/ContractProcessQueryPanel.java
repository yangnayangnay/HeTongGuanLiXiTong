package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.entity.ContractState;
import com.contract.service.ContractService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 合同流程查询面板
 * <p>
 * 该面板用于查询和查看合同的完整流转历史记录。
 * 用户可以按合同状态筛选合同列表，并查看每个合同的
 * 状态变更时间线和详细的操作流程记录。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示所有合同的简要列表（编号、名称、客户、当前状态）</li>
 *   <li>支持按合同状态进行筛选（全部/起草/会签完成/定稿完成/审批完成/签订完成）</li>
 *   <li>点击表格行可查看该合同的完整流程详情：
 *     · 状态变更记录（何时进入各阶段）
 *     · 操作流程记录（谁在何时做了什么操作，结果如何）</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>提供合同全生命周期的审计追踪功能</li>
 *   <li>帮助管理人员了解合同的处理进度和历史操作</li>
 *   <li>支持问题追溯和责任定位</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractProcessQueryPanel extends JPanel {
    // 合同状态下拉选择框
    private JComboBox<String> cmbState;
    // 合同列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 流程详情显示区域（只读）
    private JTextArea txtProcessDetail;
    // 合同业务服务类
    private ContractService contractService = new ContractService();

    /**
     * 构造方法：初始化合同流程查询面板
     */
    public ContractProcessQueryPanel() {
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载所有合同数据
        loadAllData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构采用三层设计：
     * - 北部(NORTH)：搜索栏（状态下拉框 + 查询按钮）
     * - 南部(SOUTH)：主内容区
     *   · 上方：合同列表表格
     *   · 下方：流程详情展示区（固定高度150px）
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("合同流程查询");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 搜索面板 =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("合同状态:"));
        // 状态下拉选项：全部 + 5种具体状态（对应type值1-5）
        cmbState = new JComboBox<>(new String[]{"全部", "起草", "会签完成", "定稿完成", "审批完成", "签订完成"});
        cmbState.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchPanel.add(cmbState);

        // 查询按钮
        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnSearch.setBackground(new Color(66, 133, 244));  // 蓝色背景
        btnSearch.setOpaque(true);
        btnSearch.setContentAreaFilled(true);
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> doSearch());  // 点击后执行按状态筛选
        searchPanel.add(btnSearch);
        add(searchPanel, BorderLayout.CENTER);

        // ===== 主内容区（包含表格和详情）=====
        JPanel centerPanel = new JPanel(new BorderLayout());

        // ---- 合同列表表格 ----
        // 定义表格列名：合同编号、合同名称、客户、当前状态
        String[] columns = {"合同编号", "合同名称", "客户", "当前状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        // 监听行选择事件，选中行时自动显示该合同的流程详情
        table.getSelectionModel().addListSelectionListener(e -> showProcessDetail());
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- 流程详情展示区 ----
        JPanel detailPanel = new JPanel(new BorderLayout(5, 5));
        detailPanel.setPreferredSize(new Dimension(0, 150));  // 固定高度150像素
        detailPanel.add(new JLabel("流程详情:"), BorderLayout.NORTH);
        txtProcessDetail = new JTextArea(5, 30);  // 5行30列
        txtProcessDetail.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtProcessDetail.setLineWrap(true);  // 启用自动换行
        txtProcessDetail.setEditable(false);  // 设为只读
        detailPanel.add(new JScrollPane(txtProcessDetail), BorderLayout.CENTER);
        centerPanel.add(detailPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载所有合同数据到表格中
     * <p>
     * 查询数据库中的所有合同记录，不分状态，
     * 全部显示在列表中供用户浏览。
     * </p>
     */
    private void loadAllData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有合同
        List<Contract> contracts = contractService.findAll();
        for (Contract c : contracts) {
            // 获取合同的当前状态描述
            String stateName = contractService.getContractStateName(c.getNum());
            // 将合同基本信息添加到表格行
            tableModel.addRow(new Object[]{c.getNum(), c.getName(), c.getCustomer(), stateName});
        }
    }

    /**
     * 执行按状态筛选查询操作
     * <p>
     * 根据用户选择的合同状态进行筛选查询。
     * 下拉框选项与状态类型的映射关系：
     * <ul>
     *   <li>"全部"(索引0)：显示所有合同</li>
     *   <li>"起草"(索引1)：stateType=1，刚创建的合同</li>
     *   <li>"会签完成"(索引2)：stateType=2，会签阶段已完成</li>
     *   <li>"定稿完成"(索引3)：stateType=3，定稿阶段已完成</li>
     *   <li>"审批完成"(索引4)：stateType=4，审批阶段已完成</li>
     *   <li>"签订完成"(索引5)：stateType=5，整个流程结束</li>
     * </ul>
     * </p>
     */
    private void doSearch() {
        // 获取下拉框选中项的索引
        int selectedIndex = cmbState.getSelectedIndex();
        // 清空表格现有数据
        tableModel.setRowCount(0);
        if (selectedIndex == 0) {
            // 选择"全部"时加载所有合同
            loadAllData();
            return;
        }
        // selectedIndex即为状态类型值（1-5）
        int stateType = selectedIndex;
        // 根据状态类型查询处于该状态的合同记录
        List<ContractState> states = contractService.getContractsByState(stateType);
        for (ContractState cs : states) {
            // 根据合同编号查询完整的合同信息
            Contract contract = contractService.findByNum(cs.getConNum());
            if (contract != null) {
                // 将合同信息添加到表格行
                tableModel.addRow(new Object[]{contract.getNum(), contract.getName(), contract.getCustomer(), cs.getTypeName()});
            }
        }
    }

    /**
     * 显示选中合同的流程详情
     * <p>
     * 当用户点击表格中的某一行时，自动加载并显示该合同的完整流转历史，
     * 包括两部分信息：
     * <ol>
     *   <li><b>状态变更记录</b>：合同在各阶段的时间节点
     *       （如"会签完成 - 2024-01-05 10:30:00"）</li>
     *   <li><b>操作流程记录</b>：每一步操作的详细信息
     *       （包括操作类型、操作人、操作结果、意见内容、操作时间）</li>
     * </ol>
     * </p>
     *
     * <p>输出格式示例：</p>
     * <pre>
     * === 合同状态变更记录 ===
     * 起草 - 2024-01-01 09:00:00
     * 会签完成 - 2024-01-03 14:20:00
     *
     * === 操作流程记录 ===
     * 会签 | 操作人: 张三 | 状态: 已完成 | 意见: 同意 | 时间: 2024-01-02 10:15:00
     * 审批 | 操作人: 李四 | 状态: 已完成 | 意见: 审核通过 | 时间: 2024-01-04 16:45:00
     * </pre>
     */
    private void showProcessDetail() {
        int row = table.getSelectedRow();
        if (row < 0) return;  // 未选中任何行时不处理

        // 获取选中行的合同编号（第一列）
        String conNum = (String) tableModel.getValueAt(row, 0);

        StringBuilder sb = new StringBuilder();

        // === 第一部分：状态变更记录 ===
        sb.append("=== 合同状态变更记录 ===\n");
        // 查询该合同的所有状态变更记录（按时间顺序）
        List<ContractState> states = contractService.getContractStates(conNum);
        for (ContractState cs : states) {
            // 格式："状态名称 - 变更时间"
            sb.append(cs.getTypeName()).append(" - ")
              .append(cs.getTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cs.getTime()) : "")
              .append("\n");
        }

        sb.append("\n");  // 分隔线

        // === 第二部分：操作流程记录 ===
        sb.append("=== 操作流程记录 ===\n");
        // 查询该合同的所有流程操作记录
        List<ContractProcess> processes = contractService.getContractProcesses(conNum);
        for (ContractProcess cp : processes) {
            // 格式："操作类型 | 操作人: xxx | 状态: xxx | 意见: xxx | 时间: xxx"
            sb.append(cp.getTypeName()).append(" | 操作人: ").append(cp.getUserName())
              .append(" | 状态: ").append(cp.getStateName())
              .append(" | 意见: ").append(cp.getContent() != null ? cp.getContent() : "")
              .append(" | 时间: ").append(cp.getTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cp.getTime()) : "")
              .append("\n");
        }
        // 将构建好的详情文本显示到文本区域
        txtProcessDetail.setText(sb.toString());
    }
}
