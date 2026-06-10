package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.entity.ContractState;
import com.contract.service.ContractService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    // 合同名称搜索输入框
    private JTextField txtSearchName;
    // 合同状态下拉选择框
    private JComboBox<String> cmbState;
    // 开始时间筛选（下拉日期选择器）
    private JSpinner spinDateFrom;
    // 结束时间筛选（下拉日期选择器）
    private JSpinner spinDateTo;
    // 是否启用开始时间筛选
    private JCheckBox chkUseFrom;
    // 是否启用结束时间筛选
    private JCheckBox chkUseTo;
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
     * - 北部(NORTH)：标题"合同流程查询"
     * - 中部(CENTER)：搜索栏（两行布局）
     *   · 第一行：名称输入框 + 状态下拉 + 查询/显示全部按钮
     *   · 第二行：时间范围筛选（复选框 + JSpinner日期选择器）
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

        // ===== 搜索面板（两行布局）=====
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 第一行：名称搜索 + 状态筛选 + 按钮
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        row1.add(new JLabel("合同名称:"));
        txtSearchName = new JTextField(15);
        txtSearchName.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        row1.add(txtSearchName);

        row1.add(new JLabel("状态:"));
        // 状态下拉选项：全部 + 5种具体状态（对应type值1-5）
        cmbState = new JComboBox<>(new String[]{"全部", "起草", "会签完成", "定稿完成", "审批完成", "签订完成"});
        cmbState.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        row1.add(cmbState);

        // 查询按钮（蓝色背景）
        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnSearch.setBackground(new Color(66, 133, 244));
        btnSearch.setOpaque(true);
        btnSearch.setContentAreaFilled(true);
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> doSearch());
        row1.add(btnSearch);

        // 显示全部按钮（重置所有条件）
        JButton btnShowAll = new JButton("显示全部");
        btnShowAll.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnShowAll.addActionListener(e -> {
            txtSearchName.setText("");
            cmbState.setSelectedIndex(0);
            chkUseFrom.setSelected(false);
            chkUseTo.setSelected(false);
            loadAllData();
        });
        row1.add(btnShowAll);

        // 第二行：时间范围筛选（复选框 + 下拉日期选择器）
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));

        // 开始时间区域
        chkUseFrom = new JCheckBox("开始时间:");
        chkUseFrom.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chkUseFrom.setFocusPainted(false);
        row2.add(chkUseFrom);

        spinDateFrom = new JSpinner(new SpinnerDateModel());
        spinDateFrom.setEditor(new JSpinner.DateEditor(spinDateFrom, "yyyy-MM-dd"));
        spinDateFrom.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        spinDateFrom.setEnabled(false);  // 默认不启用
        ((JSpinner.DefaultEditor)spinDateFrom.getEditor()).getTextField().setColumns(12);
        row2.add(spinDateFrom);
        chkUseFrom.addActionListener(e -> spinDateFrom.setEnabled(chkUseFrom.isSelected()));

        row2.add(new JLabel("~"));

        // 结束时间区域
        chkUseTo = new JCheckBox("结束时间:");
        chkUseTo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chkUseTo.setFocusPainted(false);
        row2.add(chkUseTo);

        spinDateTo = new JSpinner(new SpinnerDateModel());
        spinDateTo.setEditor(new JSpinner.DateEditor(spinDateTo, "yyyy-MM-dd"));
        spinDateTo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        spinDateTo.setEnabled(false);  // 默认不启用
        ((JSpinner.DefaultEditor)spinDateTo.getEditor()).getTextField().setColumns(12);
        row2.add(spinDateTo);
        chkUseTo.addActionListener(e -> spinDateTo.setEnabled(chkUseTo.isSelected()));

        JLabel lblHint = new JLabel("(勾选后 ▼ 选择日期)");
        lblHint.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        lblHint.setForeground(Color.GRAY);
        row2.add(lblHint);

        searchPanel.add(row1);
        searchPanel.add(row2);

        add(searchPanel, BorderLayout.CENTER);

        // ===== 主内容区（包含表格和详情）=====
        JPanel centerPanel = new JPanel(new BorderLayout());

        // ---- 合同列表表格 ----
        String[] columns = {"合同编号", "合同名称", "客户", "当前状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.getSelectionModel().addListSelectionListener(e -> showProcessDetail());
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- 流程详情展示区 ----
        JPanel detailPanel = new JPanel(new BorderLayout(5, 5));
        detailPanel.setPreferredSize(new Dimension(0, 150));
        detailPanel.add(new JLabel("流程详情:"), BorderLayout.NORTH);
        txtProcessDetail = new JTextArea(5, 30);
        txtProcessDetail.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtProcessDetail.setLineWrap(true);
        txtProcessDetail.setEditable(false);
        detailPanel.add(new JScrollPane(txtProcessDetail), BorderLayout.CENTER);
        centerPanel.add(detailPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载所有合同数据到表格中（不经过筛选）
     * <p>
     * 查询数据库中的所有合同记录，不分状态、不分时间，
     * 全部显示在列表中供用户浏览。
     * </p>
     */
    private void loadAllData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有合同
        List<Contract> contracts = contractService.findAll();
        for (Contract c : contracts) {
            String stateName = contractService.getContractStateName(c.getNum());
            tableModel.addRow(new Object[]{c.getNum(), c.getName(), c.getCustomer(), stateName});
        }
    }

    /**
     * 执行组合条件筛选查询
     * <p>
     * 支持三个维度的组合筛选：
     * <ol>
     *   <li><b>合同名称</b>：模糊匹配（输入关键词即可）</li>
     *   <li><b>合同状态</b>：精确匹配（下拉框选择）</li>
     *   <li><b>时间范围</b>：按合同的开始时间筛选（需勾选复选框启用）</li>
     * </ol>
     * </p>
     */
    private void doSearch() {
        // 获取名称搜索关键词
        String keyword = txtSearchName.getText().trim();
        // 获取状态下拉框选中项的索引
        int stateIndex = cmbState.getSelectedIndex();

        // 清空表格
        tableModel.setRowCount(0);

        // 根据状态获取候选列表
        List<Contract> candidates;
        if (stateIndex == 0) {
            // "全部"状态 → 查询所有合同
            candidates = contractService.findAll();
        } else {
            // 具体状态 → 按状态类型查询
            List<ContractState> states = contractService.getContractsByState(stateIndex);
            candidates = new java.util.ArrayList<>();
            for (ContractState cs : states) {
                Contract c = contractService.findByNum(cs.getConNum());
                if (c != null) candidates.add(c);
            }
        }

        // 应用名称和时间筛选
        fillTable(keyword, stateIndex, candidates);
    }

    /**
     * 将合同列表按名称和时间范围筛选后填充到表格
     *
     * @param keyword    名称搜索关键词（空字符串=不过滤）
     * @param stateIndex 状态下拉框选中索引（0=全部）
     * @param contracts  候选合同列表
     */
    private void fillTable(String keyword, int stateIndex, List<Contract> contracts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 只有勾选了复选框才启用对应的日期筛选
        Date dateFrom = chkUseFrom.isSelected() ? (Date) spinDateFrom.getValue() : null;
        Date dateTo = chkUseTo.isSelected() ? (Date) spinDateTo.getValue() : null;

        int matched = 0;
        for (Contract c : contracts) {
            // 名称模糊筛选
            if (!keyword.isEmpty()
                && c.getName().toLowerCase().indexOf(keyword.toLowerCase()) == -1
                && c.getNum().toLowerCase().indexOf(keyword.toLowerCase()) == -1) continue;

            Date conBeginTime = c.getBeginTime();
            // 时间范围筛选
            if (dateFrom != null && conBeginTime != null && conBeginTime.before(dateFrom)) continue;
            if (dateTo != null && conBeginTime != null && conBeginTime.after(dateTo)) continue;

            matched++;
            String stateName = contractService.getContractStateName(c.getNum());
            tableModel.addRow(new Object[]{c.getNum(), c.getName(), c.getCustomer(), stateName});
        }

        // 有筛选条件但无结果时给出提示
        boolean hasFilter = !keyword.isEmpty() || chkUseFrom.isSelected() || chkUseTo.isSelected() || stateIndex > 0;
        if (hasFilter && matched == 0 && !contracts.isEmpty()) {
            txtProcessDetail.setText("(在当前筛选条件下未找到符合条件的合同)\n提示: 请尝试调整或清空部分筛选条件");
            txtProcessDetail.setForeground(Color.GRAY);
        } else {
            txtProcessDetail.setForeground(Color.BLACK);
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
