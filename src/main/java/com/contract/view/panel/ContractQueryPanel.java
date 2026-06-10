package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.entity.ContractState;
import com.contract.service.ContractService;
import com.contract.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    // 合同详情内容显示区域（只读）
    private JTextArea txtDetailContent;
    // 详情区域标题标签（用于动态更新标题）
    private JLabel lblDetailTitle;
    // 流程详情内容显示区域（只读）
    private JTextArea txtProcessDetail;
    // 流程区域标题标签
    private JLabel lblProcessTitle;
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

        // ---- 搜索面板（两行布局）----
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 第一行：名称搜索
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        row1.add(new JLabel("合同名称:"));
        txtSearchName = new JTextField(18);
        txtSearchName.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        row1.add(txtSearchName);

        // 查询按钮（执行模糊搜索）
        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnSearch.setBackground(new Color(66, 133, 244));  // 蓝色背景
        btnSearch.setOpaque(true);
        btnSearch.setContentAreaFilled(true);
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> doSearch());
        row1.add(btnSearch);

        // 显示全部按钮（重置搜索条件）
        JButton btnShowAll = new JButton("显示全部");
        btnShowAll.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnShowAll.addActionListener(e -> { chkUseFrom.setSelected(false); chkUseTo.setSelected(false); loadAllData(); });
        row1.add(btnShowAll);

        // 第二行：时间范围筛选（下拉日期选择器 + 启用开关）
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

        // 勾选复选框时启用/禁用日期选择器
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
     * 创建详情展示面板（左右分栏：左边合同内容 + 右边流程历史）
     * <p>
     * 面板采用JSplitPane实现可调整的左右分栏：
     * - 左侧：合同基本信息 + 合同正文（按权限控制显示）
     * -右侧：流程状态变更记录 + 操作流程记录
     * </p>
     *
     * @return 配置好的详情面板组件
     */
    private JPanel createDetailPanel() {
        // 外层容器
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createTitledBorder("合同详情与流程"));
        outer.setPreferredSize(new Dimension(0, 250));  // 固定高度250像素

        // ===== 左侧：合同内容面板 =====
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("合同内容"));
        lblDetailTitle = new JLabel("点击上方表格中的合同查看详情");
        lblDetailTitle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(lblDetailTitle, BorderLayout.NORTH);
        txtDetailContent = new JTextArea(6, 35);
        txtDetailContent.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtDetailContent.setLineWrap(true);
        txtDetailContent.setEditable(false);
        leftPanel.add(new JScrollPane(txtDetailContent), BorderLayout.CENTER);

        // ===== 右侧：流程历史面板 =====
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("流程历史"));
        lblProcessTitle = new JLabel("点击上方表格查看流程记录");
        lblProcessTitle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rightPanel.add(lblProcessTitle, BorderLayout.NORTH);
        txtProcessDetail = new JTextArea(6, 30);
        txtProcessDetail.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtProcessDetail.setLineWrap(true);
        txtProcessDetail.setEditable(false);
        rightPanel.add(new JScrollPane(txtProcessDetail), BorderLayout.CENTER);

        // 用JSplitPane组合左右面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(0.55);  // 左侧占55%
        splitPane.setResizeWeight(0.55);      // 调整大小时左侧优先

        outer.add(splitPane, BorderLayout.CENTER);

        return outer;
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
     * 详情区采用左右分栏布局：
     * <ul>
     *   <li><b>左侧（合同内容）</b>：基本信息 + 合同正文（按权限控制）</li>
     *   <li><b>右侧（流程历史）</b>：状态变更时间线 + 操作流程记录</li>
     * </ul>
     * </p>
     */
    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            // 未选中任何行时，恢复默认提示
            lblDetailTitle.setText("点击上方表格中的合同查看详情");
            txtDetailContent.setText("");
            lblProcessTitle.setText("点击上方表格查看流程记录");
            txtProcessDetail.setText("");
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

        // 查询完整的合同实体对象
        Contract contract = contractService.findByNum(conNum);

        // ===== 左侧：构建合同内容 =====
        StringBuilder sb = new StringBuilder();
        sb.append("合同编号: ").append(conNum).append("\n");
        sb.append("合同名称: ").append(conName).append("\n");
        sb.append("客户: ").append(customer != null ? customer : "").append("\n");
        sb.append("开始时间: ").append(beginTime != null ? beginTime : "").append("\n");
        sb.append("结束时间: ").append(endTime != null ? endTime : "").append("\n");
        sb.append("起草人: ").append(drafter).append("\n");
        sb.append("当前状态: ").append(state).append("\n\n");

        if (canViewFullContent(drafter)) {
            lblDetailTitle.setText(conName + " - 完整内容");
            if (contract != null && contract.getContent() != null) {
                sb.append("--- 合同正文 ---\n").append(contract.getContent());
            } else {
                sb.append("(无合同内容)");
            }
            txtDetailContent.setText(sb.toString());
            txtDetailContent.setForeground(Color.BLACK);
        } else {
            lblDetailTitle.setText(conName + " - 仅基本信息");
            sb.append("--- 合同正文 ---\n");
            sb.append("[您没有权限查看该合同的详细内容]\n(只有管理员或合同起草人可查看完整内容)");
            txtDetailContent.setText(sb.toString());
            txtDetailContent.setForeground(Color.GRAY);
        }

        // ===== 右侧：构建流程历史 =====
        loadProcessDetail(conNum);
    }

    /**
     * 加载并显示指定合同的完整流程历史
     * <p>
     * 流程历史包含两部分：
     * <ol>
     *   <li><b>状态变更记录</b>：合同在各阶段的时间节点（起草→会签完成→定稿完成→审批完成→签订完成）</li>
     *   <li><b>操作流程记录</b>：每一步操作的详细信息（操作类型、操作人、状态、意见、时间）</li>
     * </ol>
     * </p>
     *
     * @param conNum 要查询的合同编号
     */
    private void loadProcessDetail(String conNum) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder psb = new StringBuilder();

        // === 第一部分：状态变更记录 ===
        psb.append("=== 状态变更记录 ===\n");
        List<ContractState> states = contractService.getContractStates(conNum);
        for (ContractState cs : states) {
            psb.append(cs.getTypeName()).append(" - ")
              .append(cs.getTime() != null ? sdf.format(cs.getTime()) : "")
              .append("\n");
        }
        if (states.isEmpty()) {
            psb.append("(暂无状态变更记录)\n");
        }

        psb.append("\n");

        // === 第二部分：操作流程记录 ===
        psb.append("=== 操作流程记录 ===\n");
        List<ContractProcess> processes = contractService.getContractProcesses(conNum);
        for (ContractProcess cp : processes) {
            psb.append(cp.getTypeName())
              .append(" | 操作人: ").append(cp.getUserName())
              .append(" | 状态: ").append(cp.getStateName())
              .append(" | 意见: ").append(cp.getContent() != null ? cp.getContent() : "")
              .append(" | 时间: ").append(cp.getTime() != null ? sdf.format(cp.getTime()) : "")
              .append("\n");
        }
        if (processes.isEmpty()) {
            psb.append("(暂无操作流程记录)\n");
        }

        // 更新右侧面板
        lblProcessTitle.setText(conNum + " 流程记录 (" + states.size() + "个状态 / " + processes.size() + "个操作)");
        txtProcessDetail.setText(psb.toString());
        txtProcessDetail.setForeground(Color.BLACK);
    }

    /**
     * 加载所有合同数据到表格中（支持时间范围筛选）
     * <p>
     * 查询数据库中的所有合同记录，并根据搜索栏中的名称和时间范围条件进行筛选。
     * 时间筛选规则：
     * <ul>
     *   <li>开始时间为空：不限制起始时间</li>
     *   <li>结束时间为空：不限制结束时间</li>
     *   <li>两者都填：只显示合同开始时间在该范围内的合同</li>
     * </ul>
     * </p>
     */
    private void loadAllData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有合同
        List<Contract> contracts = contractService.findAll();
        // 应用筛选条件后填充表格
        fillTable(contracts);
        // 清空详情区，恢复默认提示
        lblDetailTitle.setText("点击上方表格中的合同查看详情");
        txtDetailContent.setText("");
        lblProcessTitle.setText("点击上方表格查看流程记录");
        txtProcessDetail.setText("");
    }

    /**
     * 执行综合搜索操作（名称 + 时间范围）
     * <p>
     * 支持同时按合同名称模糊匹配和按时间范围筛选。
     * 筛选条件可以单独使用或组合使用。
     * </p>
     */
    private void doSearch() {
        tableModel.setRowCount(0);
        String name = txtSearchName.getText().trim();
        List<Contract> contracts;
        if (name.isEmpty()) {
            contracts = contractService.findAll();
        } else {
            contracts = contractService.findByName(name);
        }
        fillTable(contracts);
    }

    /**
     * 将合同列表按时间范围筛选后填充到表格
     * <p>
     * 根据复选框是否勾选来决定是否启用对应的时间筛选条件。
     * 勾选了"开始时间"则只显示合同开始时间 >= 选中日期的合同。
     * 勾选了"结束时间"则只显示合同开始时间 <= 选中日期的合同。
     * </p>
     *
     * @param contracts 待筛选的原始合同列表
     */
    private void fillTable(List<Contract> contracts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 只有勾选了复选框才启用对应的日期筛选
        Date dateFrom = chkUseFrom.isSelected() ? (Date) spinDateFrom.getValue() : null;
        Date dateTo = chkUseTo.isSelected() ? (Date) spinDateTo.getValue() : null;

        int matched = 0;
        for (Contract c : contracts) {
            Date conBeginTime = c.getBeginTime();
            // 时间范围筛选：合同的开始时间必须在 [dateFrom, dateTo] 范围内
            if (dateFrom != null && conBeginTime != null && conBeginTime.before(dateFrom)) continue;
            if (dateTo != null && conBeginTime != null && conBeginTime.after(dateTo)) continue;

            // 通过筛选，添加到表格
            matched++;
            String stateName = contractService.getContractStateName(c.getNum());
            tableModel.addRow(new Object[]{
                c.getNum(), c.getName(), c.getCustomer(),
                c.getBeginTime() != null ? sdf.format(c.getBeginTime()) : "",
                c.getEndTime() != null ? sdf.format(c.getEndTime()) : "",
                c.getUserName(), stateName
            });
        }

        // 如果有时间筛选但无结果，给出提示
        if ((chkUseFrom.isSelected() || chkUseTo.isSelected()) && matched == 0 && !contracts.isEmpty()) {
            lblDetailTitle.setText("在指定时间范围内未找到符合条件的合同");
            txtDetailContent.setText("(提示: 请尝试调整日期或取消勾选时间条件)");
            txtDetailContent.setForeground(Color.GRAY);
            lblProcessTitle.setText("无匹配结果");
            txtProcessDetail.setText("");
        }
    }
}
