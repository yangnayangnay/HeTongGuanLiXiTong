package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.service.ContractService;
import com.contract.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * 合同信息查询面板（按权限控制是否显示详细内容）
 * <p>
 * 该面板用于查询和浏览系统中所有合同的基本信息。
 * 支持按合同名称进行模糊搜索，并提供详细的合同信息查看功能。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示所有合同的列表（编号、名称、客户、时间、起草人、状态）</li>
 *   <li>支持按合同名称进行模糊查询</li>
 *   <li>点击表格行可查看合同详细信息</li>
 *   <li>根据用户权限控制是否显示合同完整内容：
 *     · 管理员和合同起草人可以查看完整内容
 *     · 其他用户只能看到基本信息和权限提示</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>实现了基于RBAC的数据访问控制</li>
 *   <li>保护敏感的合同内容不被未授权人员查看</li>
 *   <li>无权限时以灰色文字显示提示信息</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractQueryPanel extends JPanel {
    // 合同名称搜索输入框
    private JTextField txtSearchName;
    // 合同列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 合同详情内容显示区域（只读）
    private JTextArea txtDetailContent;
    // 详情区域标题标签（用于动态更新标题）
    private JLabel lblDetailTitle;
    // 合同业务服务类
    private ContractService contractService = new ContractService();
    // 用户业务服务类（用于权限判断）
    private UserService userService = new UserService();
    // 当前登录用户信息（用于权限控制）
    private com.contract.entity.User currentUser;

    /**
     * 构造方法：初始化合同查询面板
     *
     * @param user 当前登录的用户对象，用于判断是否有权查看合同详细内容
     */
    public ContractQueryPanel(com.contract.entity.User user) {
        this.currentUser = user;
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
     * 布局结构采用上下分区设计：
     * - 北部(NORTH)：标题"合同信息查询"
     * - 中部(CENTER)：主面板
     *   · 上方：搜索栏（输入框 + 查询/显示全部按钮）
     *   · 中间：合同列表表格
     * - 南部(SOUTH)：详情面板（带边框的文本区域）
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("合同信息查询");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 主面板（包含搜索栏和表格）=====
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ---- 搜索面板 ----
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("合同名称:"));
        txtSearchName = new JTextField(20);
        txtSearchName.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchPanel.add(txtSearchName);

        // 查询按钮（执行模糊搜索）
        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnSearch.setBackground(new Color(66, 133, 244));  // 蓝色背景
        btnSearch.setOpaque(true);
        btnSearch.setContentAreaFilled(true);
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> doSearch());
        searchPanel.add(btnSearch);

        // 显示全部按钮（重置搜索条件）
        JButton btnShowAll = new JButton("显示全部");
        btnShowAll.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnShowAll.addActionListener(e -> loadAllData());  // 点击后加载所有合同
        searchPanel.add(btnShowAll);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // ---- 合同列表表格 ----
        // 定义表格列名：合同编号、合同名称、客户、开始时间、结束时间、起草人、当前状态
        String[] columns = {"合同编号", "合同名称", "客户", "开始时间", "结束时间", "起草人", "当前状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        // 监听行选择事件，选中行时自动显示该合同的详情
        table.getSelectionModel().addListSelectionListener(e -> onRowSelected());
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // ===== 下半部分：合同详情区域（按权限显示）=====
        JPanel detailPanel = createDetailPanel();
        add(detailPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建详情展示面板
     * <p>
     * 构建一个带边框的面板，用于显示选中合同的详细信息。
     * 面板包含一个标题标签和一个只读的多行文本区域。
     * 默认高度为200像素，固定在底部区域。
     * </p>
     *
     * @return 配置好的详情面板组件
     */
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("合同详情"));  // 带标题的边框
        panel.setPreferredSize(new Dimension(0, 200));  // 固定高度200像素

        // 详情标题（默认提示文字）
        lblDetailTitle = new JLabel("点击上方表格中的合同查看详情");
        lblDetailTitle.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        panel.add(lblDetailTitle, BorderLayout.NORTH);

        // 详情内容显示区（只读）
        txtDetailContent = new JTextArea(6, 40);  // 6行40列
        txtDetailContent.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtDetailContent.setLineWrap(true);  // 启用自动换行
        txtDetailContent.setEditable(false);  // 设为只读
        panel.add(new JScrollPane(txtDetailContent), BorderLayout.CENTER);

        return panel;
    }

    /**
     * 判断当前用户是否有权限查看合同的完整内容
     * <p>
     * 权限规则：
     * <ul>
     *   <li>管理员用户可以查看所有合同的完整内容（拥有最高权限）</li>
     *   <li>合同的起草人可以查看自己起草的合同内容（数据所有者权限）</li>
     *   <li>其他用户只能看到基本信息，无法查看合同正文</li>
     * </ul>
     * </p>
     *
     * @param drafterName 合同的起草人姓名
     * @return true表示有权限查看完整内容；false表示只有基本信息的查看权限
     */
    private boolean canViewFullContent(String drafterName) {
        // 管理员可以看到所有内容（系统最高权限）
        if (userService.isAdmin(currentUser.getName())) return true;
        // 自己起草的合同可以看到内容（数据所有者权限）
        if (currentUser.getName().equals(drafterName)) return true;
        // 其他情况无权查看完整内容
        return false;
    }

    /**
     * 处理表格行选中事件
     * <p>
     * 当用户点击表格中的某一行时，自动加载该合同的详细信息并显示在下方详情区。
     * 显示内容包括：
     * <ul>
     *   <li>基本信息：编号、名称、客户、时间范围、起草人、当前状态</li>
     *   <li>合同正文：根据权限决定是否显示（有权限显示完整内容，无权限显示提示）</li>
     * </ul>
     * </p>
     *
     * <p>权限控制逻辑：</p>
     * <ul>
     *   <li>有权限时：标题显示"(完整内容)"，正文正常显示，黑色字体</li>
     *   <li>无权限时：标题显示"(仅基本信息)"，正文显示权限提示，灰色字体</li>
     * </ul>
     */
    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            // 未选中任何行时，恢复默认提示
            lblDetailTitle.setText("点击上方表格中的合同查看详情");
            txtDetailContent.setText("");
            return;
        }

        // 获取选中行的各列数据
        String conNum = (String) tableModel.getValueAt(row, 0);      // 合同编号
        String conName = (String) tableModel.getValueAt(row, 1);     // 合同名称
        String customer = (String) tableModel.getValueAt(row, 2);    // 客户
        String beginTime = (String) tableModel.getValueAt(row, 3);   // 开始时间
        String endTime = (String) tableModel.getValueAt(row, 4);     // 结束时间
        String drafter = (String) tableModel.getValueAt(row, 5);     // 起草人
        String state = (String) tableModel.getValueAt(row, 6);       // 当前状态

        // 查询完整的合同实体对象（可能包含合同正文内容）
        Contract contract = contractService.findByNum(conNum);

        // 构建详情文本（先添加基本信息部分）
        StringBuilder sb = new StringBuilder();
        sb.append("合同编号: ").append(conNum).append("\n");
        sb.append("合同名称: ").append(conName).append("\n");
        sb.append("客户: ").append(customer != null ? customer : "").append("\n");
        sb.append("开始时间: ").append(beginTime != null ? beginTime : "").append("\n");
        sb.append("结束时间: ").append(endTime != null ? endTime : "").append("\n");
        sb.append("起草人: ").append(drafter).append("\n");
        sb.append("当前状态: ").append(state).append("\n");

        sb.append("\n");  // 分隔线

        // 根据权限决定是否显示合同正文
        if (canViewFullContent(drafter)) {
            // 有权限：显示完整合同内容
            lblDetailTitle.setText("合同详情 - " + conName + "（完整内容）");
            if (contract != null && contract.getContent() != null) {
                sb.append("--- 合同内容 ---\n").append(contract.getContent());
            } else {
                sb.append("(无合同内容)");
            }
            txtDetailContent.setText(sb.toString());
            txtDetailContent.setForeground(Color.BLACK);  // 黑色字体表示正常内容
        } else {
            // 无权限：显示权限提示
            lblDetailTitle.setText("合同详情 - " + conName + "（仅基本信息）");
            sb.append("--- 合同内容 ---\n");
            sb.append("[您没有权限查看该合同的详细内容]\n");
            sb.append("(只有管理员或合同起草人可查看完整内容)");
            txtDetailContent.setText(sb.toString());
            txtDetailContent.setForeground(Color.GRAY);  // 灰色字体表示受限内容
        }
    }

    /**
     * 加载所有合同数据到表格中
     * <p>
     * 查询数据库中的所有合同记录，不分状态和类型，
     * 全部显示在列表中供用户浏览和筛选。
     * 同时清空下方的详情显示区。
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
            // 将合同信息添加到表格行（日期格式化为yyyy-MM-dd）
            tableModel.addRow(new Object[]{
                c.getNum(), c.getName(), c.getCustomer(),
                c.getBeginTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getBeginTime()) : "",
                c.getEndTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getEndTime()) : "",
                c.getUserName(), stateName
            });
        }
        // 清空详情区，恢复默认提示
        lblDetailTitle.setText("点击上方表格中的合同查看详情");
        txtDetailContent.setText("");
    }

    /**
     * 执行按名称搜索操作
     * <p>
     * 根据用户输入的合同名称进行模糊匹配查询。
     * 如果搜索框为空，则等同于显示全部合同。
     * </p>
     *
     * <p>搜索逻辑：</p>
     * <ul>
     *   <li>获取用户输入的搜索关键词</li>
     *   <li>如果关键词为空，调用findAll()查询全部</li>
     *   <li>如果有关键词，调用findByName()进行模糊匹配</li>
     *   <li>将查询结果填充到表格中</li>
     * </ul>
     */
    private void doSearch() {
        // 获取搜索关键词
        String name = txtSearchName.getText().trim();
        // 清空表格现有数据
        tableModel.setRowCount(0);
        List<Contract> contracts;
        // 根据是否输入关键词选择不同的查询方式
        if (name.isEmpty()) {
            // 关键词为空时查询全部合同
            contracts = contractService.findAll();
        } else {
            // 有关键词时执行模糊匹配查询
            contracts = contractService.findByName(name);
        }
        // 将查询结果填充到表格
        for (Contract c : contracts) {
            String stateName = contractService.getContractStateName(c.getNum());
            tableModel.addRow(new Object[]{
                c.getNum(), c.getName(), c.getCustomer(),
                c.getBeginTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getBeginTime()) : "",
                c.getEndTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getEndTime()) : "",
                c.getUserName(), stateName
            });
        }
    }
}
