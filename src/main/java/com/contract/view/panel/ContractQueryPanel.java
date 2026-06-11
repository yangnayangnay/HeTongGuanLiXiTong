package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.entity.ContractState;
import com.contract.entity.ContractVersion;
import com.contract.service.ContractService;
import com.contract.service.ContractVersionService;
import com.contract.service.UserService;
import com.contract.util.CalendarPickerUtil;
import com.contract.util.DataExportUtil;

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
    // 状态筛选下拉框
    private JComboBox<String> cmbStatus;
    // 开始时间筛选（文本框+日历按钮）
    private JTextField txtDateFrom;
    // 结束时间筛选（文本框+日历按钮）
    private JTextField txtDateTo;
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
    // 附件信息标签（显示文件名）
    private JLabel lblAttachmentInfo;
    // 下载附件按钮
    private JButton btnDownloadAttachment;
    // 当前选中合同的附件文件名（用于下载功能）
    private String currentAttachmentFileName;
    // 合同业务服务类
    private ContractService contractService = new ContractService();
    // 合同版本控制服务类
    private ContractVersionService versionService = new ContractVersionService();
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

        // 状态筛选下拉框
        cmbStatus = new JComboBox<>(new String[]{"全部状态", "起草", "会签完成", "定稿完成", "审批完成", "签订完成"});
        cmbStatus.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        row1.add(cmbStatus);

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
        btnShowAll.addActionListener(e -> { cmbStatus.setSelectedIndex(0); chkUseFrom.setSelected(false); chkUseTo.setSelected(false); loadAllData(); });
        row1.add(btnShowAll);

        // 导出CSV按钮
        JButton btnExportCSV = new JButton("📥 导出CSV");
        btnExportCSV.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnExportCSV.setBackground(new Color(66, 133, 244));
        btnExportCSV.setOpaque(true);
        btnExportCSV.setContentAreaFilled(true);
        btnExportCSV.setForeground(Color.WHITE);
        btnExportCSV.setFocusPainted(false);
        btnExportCSV.addActionListener(e -> exportToCSV());
        row1.add(btnExportCSV);

        // 导出HTML报表按钮
        JButton btnExportHTML = new JButton("📄 导出报表");
        btnExportHTML.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnExportHTML.setBackground(new Color(46, 204, 113));
        btnExportHTML.setOpaque(true);
        btnExportHTML.setContentAreaFilled(true);
        btnExportHTML.setForeground(Color.BLACK);
        btnExportHTML.setFocusPainted(false);
        btnExportHTML.addActionListener(e -> exportToHTML());
        row1.add(btnExportHTML);

        // 版本历史按钮（查看选中合同的版本变更记录）
        JButton btnVersionHistory = new JButton("版本历史");
        btnVersionHistory.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnVersionHistory.setBackground(new Color(155, 89, 182));  // 紫色背景
        btnVersionHistory.setOpaque(true);
        btnVersionHistory.setContentAreaFilled(true);
        btnVersionHistory.setForeground(Color.WHITE);
        btnVersionHistory.setFocusPainted(false);
        btnVersionHistory.addActionListener(e -> showVersionHistoryDialog());
        row1.add(btnVersionHistory);

        // 第二行：时间范围筛选（文本框 + 日历按钮 + 启用开关）
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));

        // 开始时间区域
        chkUseFrom = new JCheckBox("开始时间:");
        chkUseFrom.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chkUseFrom.setFocusPainted(false);
        row2.add(chkUseFrom);

        txtDateFrom = new JTextField(12);
        txtDateFrom.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtDateFrom.setEnabled(false);
        row2.add(txtDateFrom);

        JButton btnCalFrom = new JButton("\uD83D\uDCC5");
        btnCalFrom.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        btnCalFrom.setToolTipText("从日历选择日期");
        btnCalFrom.setEnabled(false);
        btnCalFrom.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Date selected = CalendarPickerUtil.showDatePicker(ContractQueryPanel.this, "选择开始时间", null);
                if (selected != null) {
                    txtDateFrom.setText(new SimpleDateFormat("yyyy-MM-dd").format(selected));
                }
            }
        });
        row2.add(btnCalFrom);

        chkUseFrom.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean enabled = chkUseFrom.isSelected();
                txtDateFrom.setEnabled(enabled);
                btnCalFrom.setEnabled(enabled);
            }
        });

        row2.add(new JLabel("~"));

        // 结束时间区域
        chkUseTo = new JCheckBox("结束时间:");
        chkUseTo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chkUseTo.setFocusPainted(false);
        row2.add(chkUseTo);

        txtDateTo = new JTextField(12);
        txtDateTo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtDateTo.setEnabled(false);
        row2.add(txtDateTo);

        JButton btnCalTo = new JButton("\uD83D\uDCC5");
        btnCalTo.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        btnCalTo.setToolTipText("从日历选择日期");
        btnCalTo.setEnabled(false);
        btnCalTo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Date selected = CalendarPickerUtil.showDatePicker(ContractQueryPanel.this, "选择结束时间", null);
                if (selected != null) {
                    txtDateTo.setText(new SimpleDateFormat("yyyy-MM-dd").format(selected));
                }
            }
        });
        row2.add(btnCalTo);

        chkUseTo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean enabled = chkUseTo.isSelected();
                txtDateTo.setEnabled(enabled);
                btnCalTo.setEnabled(enabled);
            }
        });

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

        // 左侧底部：附件操作按钮区
        JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        lblAttachmentInfo = new JLabel("");  // 显示附件文件名
        lblAttachmentInfo.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        lblAttachmentInfo.setForeground(new Color(0, 120, 215));  // 蓝色链接色
        attachmentPanel.add(lblAttachmentInfo);

        // 下载附件按钮（默认隐藏，有附件时显示）
        btnDownloadAttachment = new JButton("下载附件");
        btnDownloadAttachment.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        btnDownloadAttachment.setBackground(new Color(66, 133, 244));
        btnDownloadAttachment.setOpaque(true);
        btnDownloadAttachment.setContentAreaFilled(true);
        btnDownloadAttachment.setForeground(Color.BLACK);
        btnDownloadAttachment.setFocusPainted(false);
        btnDownloadAttachment.setVisible(false);  // 默认隐藏
        btnDownloadAttachment.addActionListener(e -> downloadAttachment());
        attachmentPanel.add(btnDownloadAttachment);
        leftPanel.add(attachmentPanel, BorderLayout.SOUTH);

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
            // 重置附件信息
            lblAttachmentInfo.setText("");
            btnDownloadAttachment.setVisible(false);
            currentAttachmentFileName = null;
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

        // 处理附件信息显示
        currentAttachmentFileName = null;
        if (contract != null && contract.getFileName() != null && !contract.getFileName().isEmpty()) {
            currentAttachmentFileName = contract.getFileName();
            lblAttachmentInfo.setText("📎 附件: " + contract.getFileName());
            btnDownloadAttachment.setVisible(true);
        } else {
            lblAttachmentInfo.setText("");
            btnDownloadAttachment.setVisible(false);
        }

        if (canViewFullContent(drafter)) {
            lblDetailTitle.setText(conName + " - 完整内容");
            if (contract != null && contract.getContent() != null) {
                sb.append("--- 合同正文 ---\n").append(contract.getContent());
            } else {
                sb.append("(无合同内容)");
            }
            // 显示附件信息（如果有）
            if (contract != null && contract.getFileName() != null && !contract.getFileName().isEmpty()) {
                sb.append("\n\n--- 附件信息 ---\n");
                sb.append("附件: ").append(contract.getFileName()).append("\n");
            }
            txtDetailContent.setText(sb.toString());
            txtDetailContent.setForeground(Color.BLACK);
        } else {
            lblDetailTitle.setText(conName + " - 仅基本信息");
            sb.append("--- 合同正文 ---\n");
            sb.append("[您没有权限查看该合同的详细内容]\n(只有管理员或合同起草人可查看完整内容)");
            // 即使无权限也显示附件名称
            if (contract != null && contract.getFileName() != null && !contract.getFileName().isEmpty()) {
                sb.append("\n\n--- 附件信息 ---\n");
                sb.append("附件: ").append(contract.getFileName()).append("\n");
            }
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
        // 只有勾选了复选框才启用对应的日期筛选（从文本框解析日期）
        Date dateFrom = null;
        if (chkUseFrom.isSelected()) {
            try {
                dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse(txtDateFrom.getText().trim());
            } catch (Exception ignored) {}
        }
        Date dateTo = null;
        if (chkUseTo.isSelected()) {
            try {
                dateTo = new SimpleDateFormat("yyyy-MM-dd").parse(txtDateTo.getText().trim());
            } catch (Exception ignored) {}
        }

        // 获取状态筛选条件
        String selectedStatus = (String) cmbStatus.getSelectedItem();
        boolean filterByStatus = selectedStatus != null && !"全部状态".equals(selectedStatus);

        int matched = 0;
        for (Contract c : contracts) {
            // 状态筛选：如果选择了非"全部状态"的选项，则按状态名过滤
            if (filterByStatus) {
                String stateName = contractService.getContractStateName(c.getNum());
                if (!selectedStatus.equals(stateName)) continue;
            }

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

    /**
     * 下载当前选中合同的附件
     * <p>
     * 该方法为文件下载功能的入口，后续可配合实际的文件存储服务实现真正的下载逻辑。
     * 当前版本提供功能框架和用户提示。
     * </p>
     */
    private void downloadAttachment() {
        if (currentAttachmentFileName == null || currentAttachmentFileName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前合同没有附件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // TODO: 后续配合文件存储服务实现实际下载功能
        // 此处预留下载接口，可通过 contractService 获取文件流并保存到本地
        JOptionPane.showMessageDialog(this,
            "附件下载功能正在开发中...\n\n附件文件名: " + currentAttachmentFileName,
            "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出当前查询结果为CSV文件
     */
    private void exportToCSV() {
        try {
            List<Contract> contracts = contractService.findAll();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出CSV文件");
            fileChooser.setSelectedFile(new java.io.File("合同数据.csv"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                DataExportUtil.exportToCSV(contracts, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "CSV导出成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "导出失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 导出当前查询结果为HTML报表
     */
    private void exportToHTML() {
        try {
            List<Contract> contracts = contractService.findAll();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出HTML报表");
            fileChooser.setSelectedFile(new java.io.File("合同报表.html"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                DataExportUtil.exportToHTML(contracts, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "HTML报表导出成功！\n可用浏览器打开后Ctrl+P打印为PDF。", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "导出失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 显示版本历史对话框
     * <p>
     * 选中一行后点击"版本历史"按钮弹出版本历史对话框：
     * - 左侧是版本列表（版本号+修改人+时间+摘要）
     * - 右侧是该版本的内容预览
     * - 底部有"对比"按钮可选择两个版本进行diff
     * </p>
     */
    private void showVersionHistoryDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一行数据！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String conNum = (String) tableModel.getValueAt(row, 0);       // 合同编号
        String conName = (String) tableModel.getValueAt(row, 1);      // 合同名称

        // 查询该合同的所有版本
        List<ContractVersion> versions = versionService.getVersions(conNum);
        if (versions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该合同暂无版本记录！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 创建版本历史对话框
        JDialog dialog = new JDialog((javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "📋 版本历史 - " + conName + " (" + conNum + ")", false);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setSize(950, 600);
        dialog.setLocationRelativeTo(this);

        // ===== 左侧：版本列表 =====
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("版本列表"));
        String[] verColumns = {"版本号", "修改人", "修改时间", "变更摘要"};
        DefaultTableModel verModel = new DefaultTableModel(verColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable verTable = new JTable(verModel);
        verTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        verTable.setRowHeight(24);
        verTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 11));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (ContractVersion v : versions) {
            verModel.addRow(new Object[]{
                "v" + v.getVersionNo(),
                v.getModifier(),
                v.getModifyTime() != null ? sdf.format(v.getModifyTime()) : "",
                v.getChangeSummary()
            });
        }
        leftPanel.add(new JScrollPane(verTable), BorderLayout.CENTER);

        // 默认选中第一行
        if (verModel.getRowCount() > 0) {
            verTable.setRowSelectionInterval(0, 0);
        }

        // ===== 右侧：内容预览区 =====
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("版本内容预览"));
        JTextArea txtPreview = new JTextArea();
        txtPreview.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtPreview.setLineWrap(true);
        txtPreview.setEditable(false);
        rightPanel.add(new JScrollPane(txtPreview), BorderLayout.CENTER);

        // 点击左侧表格行时显示对应版本内容
        verTable.getSelectionModel().addListSelectionListener(e -> {
            int selRow = verTable.getSelectedRow();
            if (selRow >= 0 && selRow < versions.size()) {
                ContractVersion selectedVer = versions.get(selRow);
                txtPreview.setText(selectedVer.getContent() != null ? selectedVer.getContent() : "(无内容)");
                txtPreview.setCaretPosition(0);  // 滚动到顶部
            }
        });

        // 初始化时加载第一个版本的内容
        if (!versions.isEmpty()) {
            txtPreview.setText(versions.get(0).getContent() != null ? versions.get(0).getContent() : "(无内容)");
        }

        // 用JSplitPane组合左右面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        dialog.add(splitPane, BorderLayout.CENTER);

        // ===== 底部：操作按钮区 =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        // 版本对比按钮
        JButton btnCompare = new JButton("🔍 版本对比");
        btnCompare.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnCompare.setBackground(new Color(52, 152, 219));
        btnCompare.setOpaque(true);
        btnCompare.setForeground(Color.WHITE);
        btnCompare.setFocusPainted(false);
        btnCompare.addActionListener(e -> {
            // 弹出版本选择对话框让用户选择两个要对比的版本
            JComboBox<String> cmbV1 = new JComboBox<>();
            JComboBox<String> cmbV2 = new JComboBox<>();
            for (ContractVersion v : versions) {
                cmbV1.addItem("v" + v.getVersionNo());
                cmbV2.addItem("v" + v.getVersionNo());
            }
            if (cmbV2.getItemCount() > 1) cmbV2.setSelectedIndex(1);  // 默认选第二个

            JPanel selectPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            selectPanel.add(new JLabel("选择版本1:"));
            selectPanel.add(cmbV1);
            selectPanel.add(new JLabel("选择版本2:"));
            selectPanel.add(cmbV2);

            int option = JOptionPane.showConfirmDialog(dialog, selectPanel,
                "选择要对比的两个版本", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                int v1Num = Integer.parseInt(((String) cmbV1.getSelectedItem()).substring(1));
                int v2Num = Integer.parseInt(((String) cmbV2.getSelectedItem()).substring(1));
                String diffResult = versionService.compareVersions(conNum, v1Num, v2Num);

                // 显示对比结果对话框
                JDialog diffDialog = new JDialog(dialog, "版本对比结果: v" + v1Num + " vs v" + v2Num, false);
                diffDialog.setLayout(new BorderLayout(5, 5));
                diffDialog.setSize(700, 500);
                diffDialog.setLocationRelativeTo(dialog);

                JTextArea txtDiff = new JTextArea();
                txtDiff.setFont(new Font("Consolas", Font.PLAIN, 12));
                txtDiff.setEditable(false);
                txtDiff.setText(diffResult);
                diffDialog.add(new JScrollPane(txtDiff), BorderLayout.CENTER);

                JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JButton btnCloseDiff = new JButton("关闭");
                btnCloseDiff.addActionListener(ev -> diffDialog.dispose());
                closePanel.add(btnCloseDiff);
                diffDialog.add(closePanel, BorderLayout.SOUTH);

                diffDialog.setVisible(true);
            }
        });
        bottomPanel.add(btnCompare);

        // 关闭按钮
        JButton btnClose = new JButton("关闭");
        btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());
        bottomPanel.add(btnClose);

        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
