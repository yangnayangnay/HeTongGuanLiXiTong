package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.service.ContractService;
import com.contract.service.ContractVersionService;
import com.contract.util.AIAssistantService;
import com.contract.util.SignaturePad;
import com.contract.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 签订合同面板
 * <p>
 * 该面板用于处理合同的签订流程。签订是合同生命周期中的最后一步，
 * 在审批通过后，由指定的签订人员完成最终的合同签署确认。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示当前用户待签订的合同列表</li>
 *   <li>查看合同基本信息（编号、名称、客户、状态等）</li>
 *   <li>输入签订信息（如签署备注等）</li>
 *   <li>提交签订结果，完成整个合同流程</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>只有被分配为签订人员的用户才能看到待签订的合同</li>
 *   <li>只有审批通过的合同才能进入签订阶段</li>
 *   <li>签订完成后，合同状态变为"签订完成"，整个合同生命周期结束</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractSignPanel extends JPanel {
    // 待签订合同列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 签订信息输入区域（用于填写签订时的备注信息）
    private JTextArea txtSignInfo;
    // 电子签名板组件（用于手写电子签名）
    private SignaturePad signaturePad;
    // 合同业务服务类
    private ContractService contractService = new ContractService();
    // 合同版本控制服务类
    private ContractVersionService versionService = new ContractVersionService();
    // 当前登录用户信息（用于过滤显示该用户待签订的合同）
    private com.contract.entity.User currentUser;

    /**
     * 构造方法：初始化签订合同面板
     *
     * @param user 当前登录的用户对象，用于过滤显示该用户待签订的合同
     */
    public ContractSignPanel(com.contract.entity.User user) {
        this.currentUser = user;
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载待签订合同数据
        loadData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题"签订合同"
     * - 中部(CENTER)：待签订合同列表表格
     * - 南部(SOUTH)：签订操作区
     *   · 上方：签订信息输入框
     *   · 下方：操作按钮组（确认签订/刷新列表）
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("签订合同");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 待签订合同列表表格 =====
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

        // ===== 签订操作面板 =====
        JPanel opPanel = new JPanel(new BorderLayout(5, 5));
        opPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // 电子签名区域（放在签订信息上方）
        JPanel signArea = new JPanel(new BorderLayout(5, 5));
        signArea.setBorder(BorderFactory.createTitledBorder("电子签名（请在下方签字）"));
        signaturePad = new SignaturePad();
        signArea.add(signaturePad, BorderLayout.CENTER);

        // 签名操作按钮（清除/确认）
        JPanel signBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnClearSign = new JButton("清除签名");
        btnClearSign.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnClearSign.setFocusPainted(false);
        btnClearSign.addActionListener(e -> signaturePad.resetCanvas());
        signBtnPanel.add(btnClearSign);
        signArea.add(signBtnPanel, BorderLayout.SOUTH);
        opPanel.add(signArea, BorderLayout.NORTH);

        // 签订信息输入区域
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.add(new JLabel("签订信息:"), BorderLayout.NORTH);
        txtSignInfo = new JTextArea(3, 30);  // 3行30列的多行文本框
        txtSignInfo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtSignInfo.setLineWrap(true);  // 启用自动换行
        formPanel.add(new JScrollPane(txtSignInfo), BorderLayout.CENTER);
        opPanel.add(formPanel, BorderLayout.CENTER);

        // 操作按钮组（水平排列）
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        // 确认签订按钮（主操作按钮）
        JButton btnSign = new JButton("确认签订");
        btnSign.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSign.setBackground(new Color(66, 133, 244));  // 蓝色背景
        btnSign.setOpaque(true);
        btnSign.setContentAreaFilled(true);
        btnSign.setForeground(Color.BLACK);
        btnSign.setFocusPainted(false);
        btnSign.addActionListener(e -> doSign());  // 点击后执行签订逻辑
        btnPanel.add(btnSign);

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
        JButton btnAIReview = new JButton("AI审查");
        btnAIReview.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnAIReview.setBackground(new Color(155, 89, 182));  // 紫色背景
        btnAIReview.setOpaque(true);
        btnAIReview.setContentAreaFilled(true);
        btnAIReview.setForeground(Color.WHITE);
        btnAIReview.setFocusPainted(false);
        btnAIReview.addActionListener(e -> showAIReviewDialog());
        btnPanel.add(btnAIReview);

        // 上传附件按钮
        JButton btnUpload = new JButton("上传合同文档");
        btnUpload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnUpload.setFocusPainted(false);
        btnUpload.addActionListener(e -> uploadAttachment());
        btnPanel.add(btnUpload);

        // 下载附件按钮
        JButton btnDownload = new JButton("下载合同文档");
        btnDownload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnDownload.setFocusPainted(false);
        btnDownload.addActionListener(e -> downloadAttachment());
        btnPanel.add(btnDownload);

        opPanel.add(btnPanel, BorderLayout.SOUTH);
        add(opPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载当前用户待签订的合同列表
     * <p>
     * 从数据库查询分配给当前用户的、类型为"签订"(type=3)且状态为"未完成"(state=0)的流程节点。
     * 只有审批通过的合同才会出现在此列表中。
     * 查询结果显示在表格中，包括：
     * <ul>
     *   <li>流程ID：用于标识具体的流程节点记录</li>
     *   <li>合同编号：合同的唯一标识符</li>
     *   <li>合同名称：便于识别合同用途</li>
     *   <li>客户：合同关联的客户信息</li>
     *   <li>合同状态：当前合同所处的生命周期阶段</li>
     *   <li>操作类型：显示"签订"</li>
     *   <li>状态：显示"未完成"/"已完成"/"已否决"</li>
     * </ul>
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询当前用户待处理的签订流程（type=3表示签订类型）
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 3);
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
     * 执行签订操作
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查是否选中了要签订的合同</li>
     *   <li>验证签订信息不能为空</li>
     *   <li>调用服务层执行签订操作，将流程节点状态更新为"已完成"</li>
     *   <li>更新合同整体状态为"签订完成"</li>
     *   <li>显示操作结果提示</li>
     *   <li>清空签订信息并刷新列表</li>
     * </ol>
     * </p>
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>必须先选择一个待签订的合同</li>
     *   <li>签订信息为必填项（用于记录签订备注）</li>
     *   <li>签订成功后自动刷新列表，已完成的合同不再显示</li>
     *   <li>签订完成标志着合同生命周期的终结</li>
     * </ul>
     */
    private void doSign() {
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要签订的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取选中行的流程ID（第一列）
        int processId = (int) tableModel.getValueAt(row, 0);
        // 获取用户输入的签订信息
        String signInfo = txtSignInfo.getText().trim();
        // 校验签订信息不能为空
        if (signInfo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "签订信息不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 校验电子签名（签订前必须完成手写签名）
        if (!signaturePad.hasSignature()) {
            JOptionPane.showMessageDialog(this, "请先进行电子签名！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取签名数据（可用于后续存入数据库）
        byte[] signatureBytes = signaturePad.getSignatureBytes();
        System.out.println("[签订面板] 电子签名已校验通过，签名数据大小: " +
            (signatureBytes != null ? signatureBytes.length : 0) + " 字节");
        // 调用服务层执行签订操作（传入流程ID、签订信息、操作人姓名）
        if (contractService.signContract(processId, signInfo, currentUser.getName())) {
            // 签订成功提示
            FileLogger.info("ContractSignPanel", "doSign", "签订成功: processId=" + processId);
            JOptionPane.showMessageDialog(this, "签订成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            // 自动保存最终版本（签订版本）
            String conNum = (String) tableModel.getValueAt(row, 1);
            Contract contract = contractService.findByNum(conNum);
            if (contract != null) {
                versionService.saveVersion(conNum, contract.getContent(),
                    contract.getFileData(), contract.getFileName(),
                    currentUser.getName(), "正式签订合同（合同生命周期完成）");
            }
            // 清空签订信息输入框
            txtSignInfo.setText("");
            // 重置电子签名板
            signaturePad.resetCanvas();
            // 刷新列表，移除已完成的合同
            loadData();
        } else {
            // 签订失败提示
            JOptionPane.showMessageDialog(this, "签订失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 查看选中合同的完整内容
     * <p>
     * 弹出一个对话框，显示当前选中合同的完整正文内容。
     * 用户可以在签订前查看合同详细内容以便做出准确的签订判断。
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
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要审查的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取选中行的合同编号
        String conNum = (String) tableModel.getValueAt(row, 1);
        Contract contract = contractService.findByNum(conNum);
        if (contract == null || contract.getContent() == null || contract.getContent().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可供审查的合同内容！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = contract.getContent();

        // 创建AI审查结果对话框
        JDialog dialog = new JDialog((javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "AI智能审查 - " + conNum, false);
        dialog.setLayout(new BorderLayout(5, 5));
        dialog.setSize(700, 550);
        dialog.setLocationRelativeTo(this);  // 居中显示

        // AI审查结果显示区域
        JTextArea txtResult = new JTextArea();
        txtResult.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        txtResult.setEditable(false);
        txtResult.setText("正在调用AI审查，请稍候...");
        dialog.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        // 关闭按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnClose = new JButton("关闭");
        btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);

        // 异步调用AI服务（避免阻塞UI）
        new Thread(() -> {
            String result = AIAssistantService.reviewContract(content);
            // 在EDT线程中更新UI
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
                    FileLogger.info("ContractSignPanel", "uploadAttachment", "上传附件: " + fileName + ", 合同=" + conNum);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "上传失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                FileLogger.error("ContractSignPanel", "uploadAttachment", "上传失败", ex);
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
                FileLogger.info("ContractSignPanel", "downloadAttachment", "下载附件: " + contract.getFileName() + ", 合同=" + conNum);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "下载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            FileLogger.error("ContractSignPanel", "downloadAttachment", "下载失败", ex);
        }
    }
}
