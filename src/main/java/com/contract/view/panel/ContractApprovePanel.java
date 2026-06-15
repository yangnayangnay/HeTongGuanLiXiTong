package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.service.ContractService;
import com.contract.service.ContractVersionService;
import com.contract.util.AIAssistantService;
import com.contract.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 审批合同面板
 * <p>
 * 该面板用于处理合同的审批流程。审批是合同生命周期中的第四步，
 * 在定稿完成后，由指定的审批人员对合同进行最终审核，决定是否通过或拒绝。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示当前用户待审批的合同列表</li>
 *   <li>查看合同基本信息（编号、名称、客户、状态等）</li>
 *   <li>输入审批意见</li>
 *   <li>执行"通过"或"拒绝"操作</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>只有被分配为审批人员的用户才能看到待审批的合同</li>
 *   <li>审批结果有两种：通过（绿色按钮）或拒绝（红色按钮）</li>
 *   <li>审批通过后合同进入签订阶段；审批拒绝则流程终止</li>
 *   <li>审批意见为必填项，用于记录审批依据和理由</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractApprovePanel extends JPanel {
    // 待审批合同列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 审批意见输入区域
    private JTextArea txtOpinion;
    // 合同业务服务类
    private ContractService contractService = new ContractService();
    // 合同版本控制服务类
    private ContractVersionService versionService = new ContractVersionService();
    // 当前登录用户信息（用于过滤显示该用户待审批的合同）
    private com.contract.entity.User currentUser;

    /**
     * 构造方法：初始化审批合同面板
     *
     * @param user 当前登录的用户对象，用于过滤显示该用户待审批的合同
     */
    public ContractApprovePanel(com.contract.entity.User user) {
        this.currentUser = user;
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载待审批合同数据
        loadData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题"审批合同"
     * - 中部(CENTER)：待审批合同列表表格
     * - 南部(SOUTH)：审批操作区
     *   · 上方：审批意见输入框
     *   · 下方：操作按钮组（审批通过/审批拒绝/刷新列表）
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("审批合同");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 待审批合同列表表格 =====
        // 定义表格列名：流程ID、合同编号、合同名称、客户、合同状态、操作类型、状态
        String[] columns = {"流程ID", "合同编号", "合同名称", "客户", "合同状态", "操作类型", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));  // 设置表格字体
        table.setRowHeight(28);  // 设置行高以提升可读性
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));  // 表头加粗
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== 审批操作面板 =====
        JPanel opPanel = new JPanel(new BorderLayout(5, 5));
        opPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // 审批意见输入区域
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.add(new JLabel("审批意见:"), BorderLayout.NORTH);
        txtOpinion = new JTextArea(3, 30);  // 3行30列的多行文本框
        txtOpinion.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtOpinion.setLineWrap(true);  // 启用自动换行
        formPanel.add(new JScrollPane(txtOpinion), BorderLayout.CENTER);
        opPanel.add(formPanel, BorderLayout.CENTER);

        // 操作按钮组（水平排列）
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        // 审批通过按钮（绿色背景，表示正向操作）
        JButton btnApprove = new JButton("审批通过");
        btnApprove.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnApprove.setBackground(new Color(52, 168, 83));  // 绿色背景
        btnApprove.setOpaque(true);
        btnApprove.setContentAreaFilled(true);
        btnApprove.setForeground(Color.BLACK);
        btnApprove.setFocusPainted(false);
        btnApprove.addActionListener(e -> doApprove(true));  // 传入true表示通过
        btnPanel.add(btnApprove);

        // 审批拒绝按钮（红色背景，表示负向操作）
        JButton btnReject = new JButton("审批拒绝");
        btnReject.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnReject.setBackground(new Color(234, 67, 53));  // 红色背景
        btnReject.setOpaque(true);
        btnReject.setContentAreaFilled(true);
        btnReject.setForeground(Color.BLACK);
        btnReject.setFocusPainted(false);
        btnReject.addActionListener(e -> doApprove(false));  // 传入false表示拒绝
        btnPanel.add(btnReject);

        // 刷新列表按钮
        JButton btnRefresh = new JButton("刷新列表");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> loadData());  // 点击后重新加载数据
        btnPanel.add(btnRefresh);

        // 查看合同内容按钮（灰色次要按钮）
        JButton btnViewContract = new JButton("查看合同");
        btnViewContract.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnViewContract.setFocusPainted(false);
        btnViewContract.addActionListener(e -> viewContractContent());
        btnPanel.add(btnViewContract);

        // AI审查按钮（调用AI服务审查合同内容）
        JButton btnAIReview = new JButton("🤖 AI审查");
        btnAIReview.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnAIReview.setBackground(new Color(155, 89, 182));  // 紫色背景
        btnAIReview.setOpaque(true);
        btnAIReview.setContentAreaFilled(true);
        btnAIReview.setForeground(Color.WHITE);
        btnAIReview.setFocusPainted(false);
        btnAIReview.addActionListener(e -> showAIReviewDialog());
        btnPanel.add(btnAIReview);

        // 上传附件按钮
        JButton btnUpload = new JButton("上传附件");
        btnUpload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnUpload.setFocusPainted(false);
        btnUpload.addActionListener(e -> uploadAttachment());
        btnPanel.add(btnUpload);

        // 下载附件按钮
        JButton btnDownload = new JButton("下载附件");
        btnDownload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnDownload.setFocusPainted(false);
        btnDownload.addActionListener(e -> downloadAttachment());
        btnPanel.add(btnDownload);

        opPanel.add(btnPanel, BorderLayout.SOUTH);
        add(opPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载当前用户待审批的合同列表
     * <p>
     * 从数据库查询分配给当前用户的、类型为"审批"(type=2)且状态为"未完成"(state=0)的流程节点。
     * 查询结果显示在表格中，包括：
     * <ul>
     *   <li>流程ID：用于标识具体的流程节点记录</li>
     *   <li>合同编号：合同的唯一标识符</li>
     *   <li>合同名称：便于识别合同用途</li>
     *   <li>客户：合同关联的客户信息</li>
     *   <li>合同状态：当前合同所处的生命周期阶段</li>
     *   <li>操作类型：显示"审批"</li>
     *   <li>状态：显示"未完成"/"已完成"/"已否决"</li>
     * </ul>
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询当前用户待处理的审批流程（type=2表示审批类型）
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 2);
        for (ContractProcess cp : processes) {
            // 根据合同编号查询完整的合同信息
            Contract contract = contractService.findByNum(cp.getConNum());
            String contractName = contract != null ? contract.getName() : "";
            String customer = contract != null ? contract.getCustomer() : "";
            // 获取合同的当前状态描述
            String stateName = contract != null ? contractService.getContractStateName(cp.getConNum()) : "";
            // 将一条流程记录添加到表格中
            tableModel.addRow(new Object[]{cp.getId(), cp.getConNum(), contractName, customer, stateName, cp.getTypeName(), cp.getStateName()});
        }
    }

    /**
     * 执行审批操作（通过或拒绝）
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查是否选中了要审批的合同</li>
     *   <li>验证审批意见不能为空</li>
     *   <li>调用服务层执行审批操作</li>
     *   <li>根据approved参数决定更新状态为"通过"或"否决"</li>
     *   <li>显示操作结果提示</li>
     *   <li>清空意见输入框并刷新列表</li>
     * </ol>
     * </p>
     *
     * @param approved 审批结果标志：
     *                 true 表示审批通过（合同进入签订阶段）；
     *                 false 表示审批拒绝（合同流程终止）
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>必须先选择一个待审批的合同</li>
     *   <li>审批意见为必填项</li>
     *   <li>审批成功后自动刷新列表，已处理的合同不再显示</li>
     * </ul>
     */
    private void doApprove(boolean approved) {
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要审批的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取选中行的流程ID（第一列）
        int processId = (int) tableModel.getValueAt(row, 0);
        // 获取用户输入的审批意见
        String opinion = txtOpinion.getText().trim();
        // 校验审批意见不能为空
        if (opinion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "审批意见不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 调用服务层执行审批操作（传入流程ID、是否通过、审批意见、操作人姓名）
        if (contractService.approveContract(processId, approved, opinion, currentUser.getName())) {
            // 显示对应的操作结果提示
            FileLogger.info("ContractApprovePanel", "doApprove", "审批操作: processId=" + processId + ", approved=" + approved);
            JOptionPane.showMessageDialog(this, approved ? "审批通过！" : "审批拒绝！", "成功", JOptionPane.INFORMATION_MESSAGE);
            // 审批通过时自动保存新版本
            if (approved) {
                String conNum = (String) tableModel.getValueAt(row, 1);
                Contract contract = contractService.findByNum(conNum);
                if (contract != null) {
                    versionService.saveVersion(conNum, contract.getContent(),
                        contract.getFileData(), contract.getFileName(),
                        currentUser.getName(), "审批通过合同（领导最终审核确认）");
                }
            }
            // 清空意见输入框
            txtOpinion.setText("");
            // 刷新列表，移除已处理的合同
            loadData();
        } else {
            // 审批失败提示
            JOptionPane.showMessageDialog(this, "审批操作失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 查看选中合同的完整内容
     * <p>
     * 弹出一个对话框，显示当前选中合同的完整正文内容。
     * 用户可以在审批前查看合同详细内容以便做出准确的审批判断。
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
        String conNum = (String) tableModel.getValueAt(row, 1);
        String conName = (String) tableModel.getValueAt(row, 2);
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
     * 弹出对话框调用AI服务对选中合同的内容进行智能审查，
     * 以异步方式执行避免阻塞UI线程。
     * </p>
     */
    private void showAIReviewDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要审查的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String conNum = (String) tableModel.getValueAt(row, 1);
        Contract contract = contractService.findByNum(conNum);
        if (contract == null || contract.getContent() == null || contract.getContent().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可供审查的合同内容！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = contract.getContent();

        JDialog dialog = new JDialog((javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "🤖 AI智能审查 - 审批", false);
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

    /**
     * 上传合同附件
     * <p>
     * 选择本地文件（PDF/DOCX/DOC），读取为字节数组后更新到合同记录中。
     * </p>
     */
    private void uploadAttachment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一行数据！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String conNum = (String) tableModel.getValueAt(row, 1);
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("合同文档 (PDF, Word)", "pdf", "docx", "doc"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = chooser.getSelectedFile();
                byte[] fileData = com.contract.util.FileUploadUtil.readFileToBytes(file);
                String fileName = file.getName();
                // 更新合同附件
                com.contract.dao.ContractDao contractDao = new com.contract.dao.ContractDao();
                com.contract.entity.Contract contract = contractDao.findByNum(conNum);
                if (contract != null) {
                    contract.setFileData(fileData);
                    contract.setFileName(fileName);
                    contractDao.update(contract);
                    JOptionPane.showMessageDialog(this, "附件上传成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    FileLogger.info("ContractApprovePanel", "uploadAttachment", "上传附件: " + fileName + ", 合同=" + conNum);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "上传失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                FileLogger.error("ContractApprovePanel", "uploadAttachment", "上传失败", ex);
            }
        }
    }

    /**
     * 下载合同附件
     * <p>
     * 从数据库读取合同附件数据，选择保存位置后写入本地文件。
     * </p>
     */
    private void downloadAttachment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一行数据！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String conNum = (String) tableModel.getValueAt(row, 1);
        try {
            com.contract.dao.ContractDao contractDao = new com.contract.dao.ContractDao();
            com.contract.entity.Contract contract = contractDao.findByNum(conNum);
            if (contract == null || contract.getFileData() == null || contract.getFileData().length == 0) {
                JOptionPane.showMessageDialog(this, "该合同暂无附件！", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File(contract.getFileName() != null ? contract.getFileName() : "contract_attachment"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                com.contract.util.FileUploadUtil.saveBytesToFile(contract.getFileData(), chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "附件下载成功！\n保存至: " + chooser.getSelectedFile().getAbsolutePath(), "成功", JOptionPane.INFORMATION_MESSAGE);
                FileLogger.info("ContractApprovePanel", "downloadAttachment", "下载附件: " + contract.getFileName() + ", 合同=" + conNum);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "下载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            FileLogger.error("ContractApprovePanel", "downloadAttachment", "下载失败", ex);
        }
    }
}
