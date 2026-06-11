package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.entity.ContractState;
import com.contract.service.ContractService;
import com.contract.service.ContractVersionService;
import com.contract.util.AIAssistantService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 定稿合同面板
 * <p>
 * 该面板用于处理合同的定稿流程。定稿是合同生命周期中的第三步，
 * 在所有会签人员完成会签后，由合同起草人对合同进行最终确认和修改。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示当前用户起草的、已完成会签待定稿的合同列表</li>
 *   <li>查看会签人员的意见汇总</li>
 *   <li>编辑和修正合同内容</li>
 *   <li>提交定稿后的最终版本</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>只有合同的起草人才能执行定稿操作</li>
 *   <li>定稿前必须先完成所有会签流程（状态type=2表示会签完成）</li>
 *   <li>定稿后合同将进入审批阶段</li>
 *   <li>起草人可以根据会签意见对合同内容进行修订</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractFinalizePanel extends JPanel {
    // 待定稿合同列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 合同内容编辑区域（用于定稿时修改内容）
    private JTextArea txtContent;
    // 会签意见显示区域（只读，展示各会签人的意见）
    private JTextArea txtOpinionArea;
    // 合同业务服务类
    private ContractService contractService = new ContractService();
    // 合同版本控制服务类
    private ContractVersionService versionService = new ContractVersionService();
    // 当前登录用户信息（必须是合同起草人才能看到待定稿的合同）
    private com.contract.entity.User currentUser;

    /**
     * 构造方法：初始化定稿合同面板
     *
     * @param user 当前登录的用户对象，用于过滤显示该用户起草的待定稿合同
     */
    public ContractFinalizePanel(com.contract.entity.User user) {
        this.currentUser = user;
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载待定稿的合同数据
        loadData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构采用上下分区设计：
     * - 中部(CENTER)：上半部分 - 待定稿合同列表表格（高度固定200px）
     * - 南部(SOUTH)：下半部分 - 定稿操作区
     *   · 上方：会签意见显示区（只读）
     *   · 中间：合同内容编辑区（可编辑）
     *   · 下方：确认定稿按钮
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("定稿合同");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 上半部分：待定稿合同列表 =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(0, 200));  // 固定高度200像素

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
        // 监听表格行选择事件，选中行时自动加载该合同的详细内容和会签意见
        table.getSelectionModel().addListSelectionListener(e -> onTableSelect());
        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 刷新按钮
        JButton btnRefresh = new JButton("刷新列表");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> loadData());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshPanel.add(btnRefresh);

        // 查看合同内容按钮（灰色次要按钮）
        JButton btnViewContract = new JButton("查看合同");
        btnViewContract.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnViewContract.setFocusPainted(false);
        btnViewContract.addActionListener(e -> viewContractContent());
        refreshPanel.add(btnViewContract);

        topPanel.add(refreshPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);

        // ===== 下半部分：定稿操作区域 =====
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // ---- 会签意见显示区（只读）----
        JPanel opinionPanel = new JPanel(new BorderLayout(5, 5));
        opinionPanel.setPreferredSize(new Dimension(0, 100));  // 固定高度100像素
        opinionPanel.add(new JLabel("会签意见:"), BorderLayout.NORTH);
        txtOpinionArea = new JTextArea(3, 30);  // 3行30列
        txtOpinionArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtOpinionArea.setLineWrap(true);
        txtOpinionArea.setEditable(false);  // 设为只读，只能查看不能编辑
        opinionPanel.add(new JScrollPane(txtOpinionArea), BorderLayout.CENTER);
        bottomPanel.add(opinionPanel, BorderLayout.NORTH);

        // ---- 合同内容编辑区（可编辑）----
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.add(new JLabel("合同内容(定稿):"), BorderLayout.NORTH);
        txtContent = new JTextArea(5, 30);  // 5行30列
        txtContent.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtContent.setLineWrap(true);  // 启用自动换行
        contentPanel.add(new JScrollPane(txtContent), BorderLayout.CENTER);
        bottomPanel.add(contentPanel, BorderLayout.CENTER);

        // 确认定稿按钮（主操作按钮）
        JButton btnFinalize = new JButton("确认定稿");
        btnFinalize.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnFinalize.setBackground(new Color(66, 133, 244));  // 蓝色背景
        btnFinalize.setOpaque(true);
        btnFinalize.setContentAreaFilled(true);
        btnFinalize.setForeground(Color.BLACK);
        btnFinalize.setFocusPainted(false);
        btnFinalize.addActionListener(e -> doFinalize());  // 点击后执行定稿逻辑

        // AI审查按钮（调用AI服务对合同内容进行智能审查）
        JButton btnAIReview = new JButton("🤖 AI审查");
        btnAIReview.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnAIReview.setBackground(new Color(155, 89, 182));  // 紫色背景
        btnAIReview.setOpaque(true);
        btnAIReview.setContentAreaFilled(true);
        btnAIReview.setForeground(Color.WHITE);
        btnAIReview.setFocusPainted(false);
        btnAIReview.addActionListener(e -> showAIReviewDialog());

        JPanel finalizeBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        finalizeBtnPanel.add(btnFinalize);
        finalizeBtnPanel.add(btnAIReview);
        bottomPanel.add(finalizeBtnPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载当前用户起草的、已完成会签待定稿的合同列表
     * <p>
     * 筛选条件：
     * <ul>
     *   <li>合同起草人必须是当前登录用户</li>
     *   <li>合同状态必须为"会签完成"（stateType=2），即所有会签人都已签署完毕</li>
     * </ul>
     * 加载完成后自动选中第一行并加载其详细内容。
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询当前用户起草的所有合同
        List<Contract> contracts = contractService.findByUserName(currentUser.getName());
        for (Contract c : contracts) {
            // 获取合同当前的状态类型
            int stateType = contractService.getContractStateType(c.getNum());
            if (stateType == 2) {  // stateType=2 表示会签完成，可以进入定稿阶段
                tableModel.addRow(new Object[]{c.getNum(), c.getName(), c.getCustomer(), contractService.getContractStateName(c.getNum())});
            }
        }
        // 如果有待定稿的合同，自动选中第一行以加载详情
        if (tableModel.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);  // 选中第一行
            onTableSelect();  // 触发行选择事件，加载合同详情
        }
    }

    /**
     * 处理表格行选择事件
     * <p>
     * 当用户在表格中选中某个合同时，自动加载该合同的详细信息：
     * <ul>
     *   <li>在内容编辑区显示合同正文（可编辑）</li>
     *   <li>在意见显示区显示所有会签人员的意见汇总（只读）</li>
     * </ul>
     * </p>
     */
    private void onTableSelect() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            // 获取选中行的合同编号（第一列）
            String conNum = (String) tableModel.getValueAt(row, 0);
            // 根据合同编号查询完整的合同实体
            Contract contract = contractService.findByNum(conNum);
            if (contract != null) {
                // 加载合同内容到编辑区（允许起草人进行修改）
                txtContent.setText(contract.getContent() != null ? contract.getContent() : "");
                // 查询并显示所有会签(type=1)流程记录的意见
                List<ContractProcess> processes = contractService.getContractProcessesByType(conNum, 1);
                StringBuilder sb = new StringBuilder();
                for (ContractProcess cp : processes) {
                    // 格式："会签人姓名: 会签意见内容"
                    sb.append(cp.getUserName()).append(": ").append(cp.getContent() != null ? cp.getContent() : "").append("\n");
                }
                txtOpinionArea.setText(sb.toString());  // 显示会签意见汇总
            }
        }
    }

    /**
     * 执行定稿操作
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查是否选中了要定稿的合同</li>
     *   <li>验证合同内容不能为空</li>
     *   <li>调用服务层执行定稿操作，保存修改后的合同内容</li>
     *   <li>更新合同状态为"定稿完成"</li>
     *   <li>显示操作结果并刷新列表</li>
     * </ol>
     * </p>
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>必须先选择一个待定稿的合同</li>
     *   <li>合同内容为必填项</li>
     *   <li>定稿成功后合同进入审批阶段</li>
     * </ul>
     */
    private void doFinalize() {
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要定稿的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取选中行的合同编号（第一列）
        String conNum = (String) tableModel.getValueAt(row, 0);
        // 获取用户编辑后的合同内容
        String content = txtContent.getText().trim();
        // 校验合同内容不能为空
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "合同内容不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 调用服务层执行定稿操作（传入合同编号、修改后的内容、当前用户名）
        if (contractService.finalizeContract(conNum, content, currentUser.getName())) {
            // 定稿成功提示
            JOptionPane.showMessageDialog(this, "定稿成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            // 自动保存新版本（定稿版本）
            Contract contract = contractService.findByNum(conNum);
            versionService.saveVersion(conNum, content,
                contract != null ? contract.getFileData() : null,
                contract != null ? contract.getFileName() : null,
                currentUser.getName(), "定稿合同（会签意见汇总后形成终稿）");
            // 清空编辑区和意见区
            txtContent.setText("");
            txtOpinionArea.setText("");
            // 刷新列表，已定稿的合同不再显示
            loadData();
        } else {
            // 定稿失败提示（可能是合同状态不正确等原因）
            JOptionPane.showMessageDialog(this, "定稿失败！合同状态可能不正确。", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 查看选中合同的完整内容
     * <p>
     * 弹出一个对话框，显示当前选中合同的完整正文内容。
     * 用户可以在定稿前查看合同详细内容以便做出准确的定稿判断。
     * </p>
     */
    private void viewContractContent() {
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要查看的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取选中行的合同编号和名称
        String conNum = (String) tableModel.getValueAt(row, 0);
        String conName = (String) tableModel.getValueAt(row, 1);
        // 根据合同编号查询完整的合同实体
        Contract contract = contractService.findByNum(conNum);
        if (contract == null) {
            JOptionPane.showMessageDialog(this, "未找到合同信息！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 创建对话框显示合同内容
        JDialog dialog = new JDialog((javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "合同内容 - " + conName, false);
        dialog.setLayout(new BorderLayout(5, 5));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);  // 居中显示

        // 合同内容文本区域（只读）
        JTextArea txtContent = new JTextArea();
        txtContent.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setText(contract.getContent() != null ? contract.getContent() : "(无合同内容)");
        dialog.add(new JScrollPane(txtContent), BorderLayout.CENTER);

        // 关闭按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnClose = new JButton("关闭");
        btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 显示AI审查对话框
     * <p>
     * 弹出对话框调用AI服务对选中合同的定稿内容进行智能审查，
     * 以异步方式执行避免阻塞UI线程。
     * </p>
     */
    private void showAIReviewDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要审查的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = txtContent.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可供审查的合同内容！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 创建AI审查结果对话框
        JDialog dialog = new JDialog((javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "🤖 AI智能审查 - 定稿", false);
        dialog.setLayout(new BorderLayout(5, 5));
        dialog.setSize(700, 550);
        dialog.setLocationRelativeTo(this);

        JTextArea txtResult = new JTextArea();
        txtResult.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        txtResult.setEditable(false);
        txtResult.setText("⏳ 正在调用AI审查，请稍候...");
        dialog.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnClose = new JButton("关闭");
        btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);

        new Thread(() -> {
            String result = AIAssistantService.reviewContract(content);
            SwingUtilities.invokeLater(() -> txtResult.setText(result));
        }).start();
    }
}
