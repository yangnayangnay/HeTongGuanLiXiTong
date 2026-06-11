package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.Customer;
import com.contract.service.ContractService;
import com.contract.service.ContractVersionService;
import com.contract.service.CustomerService;
import com.contract.util.AIAssistantService;
import com.contract.util.CalendarPickerUtil;
import com.contract.util.FileUploadUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 起草合同面板
 * <p>
 * 该面板用于创建新的合同，是合同生命周期的第一步。
 * 用户可以在此输入合同名称、选择客户、设置合同时间范围、填写合同内容。
 * 提供日期校验和自动修正功能，确保结束时间不早于开始时间。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>输入合同基本信息（名称、客户、时间范围）</li>
 *   <li>编辑合同正文内容</li>
 *   <li>自动日期校验和调整（支持半年/一年的快捷调整按钮）</li>
 *   <li>提交合同到系统，生成合同编号</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractDraftPanel extends JPanel {
    // 合同名称输入框
    private JTextField txtName;
    // 客户下拉选择框
    private JComboBox<String> cmbCustomer;
    // 合同开始时间输入框
    private JTextField txtBeginTime;
    // 合同结束时间输入框
    private JTextField txtEndTime;
    // 合同内容文本区域
    private JTextArea txtContent;
    // 合同业务服务类，用于提交起草的合同
    private ContractService contractService = new ContractService();
    // 合同版本控制服务类
    private ContractVersionService versionService = new ContractVersionService();
    // 客户业务服务类，用于加载客户列表
    private CustomerService customerService = new CustomerService();
    // 当前登录用户信息
    private com.contract.entity.User currentUser;
    // 合同附件上传按钮
    private JButton btnUploadFile;
    // 附件文件名显示标签（显示已选择的文件名）
    private JLabel lblFileName;
    // 合同附件下载按钮
    private JButton btnDownloadFile;
    // 当前选择的合同附件数据（保存在内存中，提交时写入数据库）
    private byte[] currentFileData;
    // 当前选择的附件文件名
    private String currentFileName;
    // 合同金额输入框
    private JTextField txtAmount;

    /**
     * 构造方法：初始化起草合同面板
     *
     * @param user 当前登录的用户对象，用于记录合同的起草人
     */
    public ContractDraftPanel(com.contract.entity.User user) {
        this.currentUser = user;
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题"起草合同"
     * - 中部(CENTER)：表单面板（使用GridBagLayout精细布局）
     *   · 合同名称输入框
     *   · 客户选择下拉框
     *   · 开始时间 + 结束时间（含快捷调整按钮）
     *   · 合同内容编辑区
     * - 南部(SOUTH)：操作按钮（提交/重置）
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("起草合同");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 表单面板（使用GridBagLayout实现灵活布局）=====
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // 设置组件间距为5像素
        gbc.insets = new Insets(5, 5, 5, 5);
        // 组件水平方向拉伸填充
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // ---- 第1行：合同名称 ----
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;  // 标签列不拉伸
        formPanel.add(createLabel("合同名称:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 3;  // 输入框跨3列
        txtName = new JTextField(30);
        formPanel.add(txtName, gbc);

        // ---- 第2行：客户选择 ----
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        formPanel.add(createLabel("客户:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 3;
        cmbCustomer = new JComboBox<>();
        // 加载所有可用客户到下拉列表
        loadCustomers();
        formPanel.add(cmbCustomer, gbc);

        // ---- 第3行：时间范围（开始时间 + 结束时间及调整按钮）----
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        formPanel.add(createLabel("开始时间:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 1;
        JPanel beginTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        txtBeginTime = new JTextField(15);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        // 默认显示当前日期作为开始时间
        txtBeginTime.setText(sdf.format(new Date()));
        // 输入开始时间时触发焦点丢失事件，自动校验结束时间
        txtBeginTime.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) { validateAndFixEndTime(); }
        });
        beginTimePanel.add(txtBeginTime);
        // 开始时间日历按钮（点击弹出日历选择器）
        JButton btnCalBegin = new JButton("📅");
        btnCalBegin.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnCalBegin.setMargin(new Insets(1, 4, 1, 4));
        btnCalBegin.setFocusPainted(false);
        btnCalBegin.setToolTipText("从日历选择开始时间");
        btnCalBegin.addActionListener(e -> showDatePickerDialog(txtBeginTime));
        beginTimePanel.add(btnCalBegin);
        formPanel.add(beginTimePanel, gbc);

        // 结束时间标签 + 调整按钮（减号在左，加号在右）
        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("结束时间:"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = 1;
        JPanel endTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        // 减少时间的快捷按钮（放在左边）
        JButton btnMinusHalf = createSmallBtn("-半年");
        btnMinusHalf.addActionListener(e -> adjustEndTime(-6));  // 减少6个月
        JButton btnMinusYear = createSmallBtn("-一年");
        btnMinusYear.addActionListener(e -> adjustEndTime(-12)); // 减少12个月
        endTimePanel.add(btnMinusHalf);
        endTimePanel.add(btnMinusYear);

        // 结束时间输入框
        txtEndTime = new JTextField(12);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, 1);  // 默认结束时间为一年后
        txtEndTime.setText(sdf.format(cal.getTime()));
        // 输入结束时自动校验日期合法性
        txtEndTime.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) { validateAndFixEndTime(); }
        });
        endTimePanel.add(txtEndTime);

        // 结束时间日历按钮（点击弹出日历选择器）
        JButton btnCalEnd = new JButton("📅");
        btnCalEnd.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnCalEnd.setMargin(new Insets(1, 4, 1, 4));
        btnCalEnd.setFocusPainted(false);
        btnCalEnd.setToolTipText("从日历选择结束时间");
        btnCalEnd.addActionListener(e -> showDatePickerDialog(txtEndTime));
        endTimePanel.add(btnCalEnd);

        // 增加时间的快捷按钮（放在右边）
        JButton btnPlusHalf = createSmallBtn("+半年");
        btnPlusHalf.addActionListener(e -> adjustEndTime(6));   // 增加6个月
        JButton btnPlusYear = createSmallBtn("+一年");
        btnPlusYear.addActionListener(e -> adjustEndTime(12));  // 增加12个月
        endTimePanel.add(btnPlusHalf);
        endTimePanel.add(btnPlusYear);

        formPanel.add(endTimePanel, gbc);

        // ---- 第3.5行：合同金额 ----
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        formPanel.add(createLabel("合同金额:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 3;
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        txtAmount = new JTextField(15);
        txtAmount.setText("0");  // 默认金额为0
        amountPanel.add(txtAmount);
        amountPanel.add(new JLabel("元"));
        formPanel.add(amountPanel, gbc);

        // ---- 第4行：合同内容（含加载模板按钮）----
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;  // 标签左上对齐
        formPanel.add(createLabel("合同内容:"), gbc);

        // 合同内容区域面板（包含模板按钮和文本区）
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        // 加载模板按钮：点击后将默认合同模板填充到文本区域
        JButton btnLoadTemplate = new JButton("📄 加载模板");
        btnLoadTemplate.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnLoadTemplate.setFocusPainted(false);
        btnLoadTemplate.setBackground(new Color(241, 196, 15));
        btnLoadTemplate.setOpaque(true);
        btnLoadTemplate.addActionListener(e -> loadContractTemplate());
        contentPanel.add(btnLoadTemplate, BorderLayout.NORTH);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;      // 文本区居中
        txtContent = new JTextArea(10, 30);  // 10行30列的多行文本区
        txtContent.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtContent.setLineWrap(true);  // 启用自动换行
        JScrollPane scrollContent = new JScrollPane(txtContent);  // 添加滚动条
        contentPanel.add(scrollContent, BorderLayout.CENTER);
        formPanel.add(contentPanel, gbc);

        // ---- 第5行：合同附件（上传/下载）----
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;  // 标签左对齐
        formPanel.add(createLabel("合同附件:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;   // 附件面板左对齐
        JPanel attachPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        // 上传附件按钮：点击后弹出文件选择器选择PDF/Word文档
        btnUploadFile = new JButton("📎 上传附件");
        btnUploadFile.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnUploadFile.setFocusPainted(false);
        btnUploadFile.setBackground(new Color(46, 204, 113));
        btnUploadFile.setOpaque(true);
        btnUploadFile.setForeground(Color.WHITE);
        btnUploadFile.addActionListener(e -> uploadAttachment());
        attachPanel.add(btnUploadFile);

        // OCR识别按钮：点击后选择图片文件进行OCR文字识别
        JButton btnOCR = new JButton("📷 OCR识别");
        btnOCR.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnOCR.setFocusPainted(false);
        btnOCR.setBackground(new Color(230, 126, 34));  // 橙色背景
        btnOCR.setOpaque(true);
        btnOCR.setForeground(Color.WHITE);
        btnOCR.setToolTipText("通过OCR识别图片中的合同文字内容");
        btnOCR.addActionListener(e -> performOCRRecognition());
        attachPanel.add(btnOCR);

        // 文件名显示标签：显示当前已选择的附件文件名
        lblFileName = new JLabel("未选择文件");
        lblFileName.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblFileName.setForeground(Color.GRAY);
        attachPanel.add(lblFileName);

        // 下载附件按钮：将已上传的附件保存到用户选择的本地路径
        btnDownloadFile = new JButton("⬇ 下载");
        btnDownloadFile.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnDownloadFile.setFocusPainted(false);
        btnDownloadFile.setEnabled(false);  // 默认禁用，有文件后才启用
        btnDownloadFile.addActionListener(e -> downloadAttachment());
        attachPanel.add(btnDownloadFile);

        formPanel.add(attachPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ===== 按钮面板 =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // 提交起草按钮（主操作按钮，蓝色高亮）
        JButton btnSubmit = new JButton("提交起草");
        btnSubmit.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSubmit.setBackground(new Color(66, 133, 244));
        btnSubmit.setOpaque(true);
        btnSubmit.setContentAreaFilled(true);
        btnSubmit.setForeground(Color.BLACK);
        btnSubmit.setFocusPainted(false);
        btnSubmit.addActionListener(e -> submitDraft());  // 点击后执行提交逻辑
        btnPanel.add(btnSubmit);

        // 重置按钮（清空表单恢复默认值）
        JButton btnReset = new JButton("重置");
        btnReset.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnReset.addActionListener(e -> resetForm());
        btnPanel.add(btnReset);

        // AI审查按钮（调用AI服务对合同内容进行智能审查）
        JButton btnAIReview = new JButton("🤖 AI审查");
        btnAIReview.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnAIReview.setBackground(new Color(155, 89, 182));  // 紫色背景
        btnAIReview.setOpaque(true);
        btnAIReview.setContentAreaFilled(true);
        btnAIReview.setForeground(Color.WHITE);
        btnAIReview.setFocusPainted(false);
        btnAIReview.addActionListener(e -> showAIReviewDialog());
        btnPanel.add(btnAIReview);

        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载客户列表到下拉选择框
     * <p>
     * 从数据库查询所有客户信息，将客户名称填充到下拉框中。
     * 在初始化界面时调用，确保用户可以选择已有客户。
     * </p>
     */
    private void loadCustomers() {
        // 先清空下拉框中的现有项
        cmbCustomer.removeAllItems();
        // 从数据库查询所有客户
        List<Customer> customers = customerService.findAll();
        // 将每个客户的名称添加到下拉框
        for (Customer c : customers) {
            cmbCustomer.addItem(c.getName());
        }
    }

    /**
     * 创建统一风格的标签组件
     *
     * @param text 标签显示的文字
     * @return 配置好字体样式的JLabel对象
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        return label;
    }

    /**
     * 创建小型快捷调整按钮
     * <p>
     * 用于结束时间的快速增减操作（如"+半年"、"-一年"等），
     * 按钮尺寸较小以节省空间。
     * </p>
     *
     * @param text 按钮上显示的文字（如"+半年"、"-一年"）
     * @return 配置好样式的小型按钮
     */
    private JButton createSmallBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 11));  // 使用较小字号
        btn.setMargin(new Insets(1, 6, 1, 6));  // 设置紧凑的内边距
        btn.setFocusPainted(false);  // 去除点击后的焦点框
        return btn;
    }

    /**
     * 校验并自动修正结束时间
     * <p>
     * 当结束时间早于或等于开始时间时，自动将结束时间调整为开始时间的后一天。
     * 此方法在以下时机被调用：
     * <ul>
     *   <li>开始时间输入框失去焦点时</li>
     *   <li>结束时间输入框失去焦点时</li>
     *   <li>提交合同时</li>
     * </ul>
     * </p>
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>结束时间必须严格晚于开始时间</li>
     *   <li>如果不符合规则，自动修正到最小合法值（开始时间+1天）</li>
     * </ul>
     */
    private void validateAndFixEndTime() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);  // 严格模式，不允许非法日期（如2月30日）

            // 解析用户输入的开始时间和结束时间
            Date beginDate = sdf.parse(txtBeginTime.getText().trim());
            Date endDate = sdf.parse(txtEndTime.getText().trim());

            // 如果结束时间早于或等于开始时间，需要自动修正
            if (endDate.before(beginDate) || endDate.equals(beginDate)) {
                java.util.Calendar fixCal = java.util.Calendar.getInstance();
                fixCal.setTime(beginDate);
                fixCal.add(java.util.Calendar.DAY_OF_MONTH, 1);  // 设置为开始时间后一天
                txtEndTime.setText(sdf.format(fixCal.getTime()));  // 自动更新结束时间字段
            }
        } catch (Exception ignored) {
            // 格式错误时不处理（如用户正在输入中），等待用户输入完整后再校验
        }
    }

    /**
     * 调整结束时间（按月增减）
     * <p>
     * 通过快捷按钮快速调整合同期限，支持按半年或一年为单位进行增减。
     * 调整后会进行合法性检查，确保调整后的结束时间不早于开始时间+1天。
     * </p>
     *
     * @param months 要调整的月数，正数表示延长时间，负数表示缩短时间
     *               例如：6表示增加半年，-12表示减少一年
     */
    private void adjustEndTime(int months) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);  // 严格日期解析模式

            // 解析当前输入的结束时间和开始时间
            Date endDate = sdf.parse(txtEndTime.getText().trim());
            Date beginDate = sdf.parse(txtBeginTime.getText().trim());

            // 计算最小允许的结束日期：开始时间 + 1天
            java.util.Calendar minCal = java.util.Calendar.getInstance();
            minCal.setTime(beginDate);
            minCal.add(java.util.Calendar.DAY_OF_MONTH, 1);

            // 对当前结束时间进行月份调整
            java.util.Calendar endCal = java.util.Calendar.getInstance();
            endCal.setTime(endDate);
            endCal.add(java.util.Calendar.MONTH, months);

            // 检查调整后的日期是否满足最小值要求
            if (!endCal.getTime().after(minCal.getTime())) {
                // 调整后不满足最小值要求，强制修正到最小允许日期
                txtEndTime.setText(sdf.format(minCal.getTime()));
                JOptionPane.showMessageDialog(this,
                    "结束时间不能早于或等于开始时间，已自动调整为 " + sdf.format(minCal.getTime()),
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 更新结束时间显示
            txtEndTime.setText(sdf.format(endCal.getTime()));
        } catch (Exception e) {
            // 日期格式错误时的友好提示
            JOptionPane.showMessageDialog(this,
                "日期格式错误，请先确认开始时间和结束时间的格式为 yyyy-MM-dd",
                "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 提交起草的合同
     * <p>
     * 执行合同提交流程：
     * <ol>
     *   <li>前端数据校验（必填项检查、日期格式验证、日期逻辑校验）</li>
     *   <li>构建合同实体对象</li>
     *   <li>调用服务层保存合同到数据库</li>
     *   <li>显示操作结果并重置表单</li>
     * </ol>
     * </p>
     *
     * <p>校验规则：</p>
     * <ul>
     *   <li>合同名称不能为空</li>
     *   <li>合同内容不能为空</li>
     *   <li>结束时间必须晚于开始时间</li>
     *   <li>日期格式必须符合 yyyy-MM-dd 格式</li>
     * </ul>
     */
    private void submitDraft() {
        // 获取表单中的各项输入值
        String name = txtName.getText().trim();
        String customer = (String) cmbCustomer.getSelectedItem();
        String beginTimeStr = txtBeginTime.getText().trim();
        String endTimeStr = txtEndTime.getText().trim();
        String content = txtContent.getText().trim();

        // === 前端必填项校验 ===
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "合同名称不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "合同内容不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);  // 严格日期模式

            // 提交前再次自动校验并修正结束时间
            validateAndFixEndTime();

            // 解析日期字符串为Date对象
            Date beginTime = beginTimeStr.isEmpty() ? null : sdf.parse(beginTimeStr);
            Date endTime = endTimeStr.isEmpty() ? null : sdf.parse(endTimeStr);

            // 二次校验：确保结束时间不早于开始时间
            if (beginTime != null && endTime != null && endTime.before(beginTime)) {
                JOptionPane.showMessageDialog(this,
                    "合同结束时间不能早于开始时间！",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 构建合同实体对象
            Contract contract = new Contract();
            contract.setName(name);                    // 设置合同名称
            contract.setCustomer(customer != null ? customer : "");  // 设置客户名称
            contract.setBeginTime(beginTime);          // 设置开始时间
            contract.setEndTime(endTime);              // 设置结束时间
            contract.setContent(content);              // 设置合同内容
            contract.setUserName(currentUser.getName()); // 记录当前用户为起草人
            // 设置附件信息（如果有选择文件）
            contract.setFileData(currentFileData);     // 附件二进制数据
            contract.setFileName(currentFileName);     // 附件文件名
            if (currentFileName != null && !currentFileName.isEmpty()) {
                // 根据文件名自动提取文件类型（扩展名）
                contract.setFileType(FileUploadUtil.getFileExtension(currentFileName));
            }
            // 设置合同金额
            try {
                double amount = Double.parseDouble(txtAmount.getText().trim());
                contract.setAmount(amount);
            } catch (NumberFormatException e) {
                contract.setAmount(0);  // 格式错误时默认为0
            }

            // 调用服务层保存合同
            if (contractService.draftContract(contract)) {
                // 保存成功：显示生成的合同编号并重置表单
                JOptionPane.showMessageDialog(this, "起草成功！合同编号: " + contract.getNum(), "成功", JOptionPane.INFORMATION_MESSAGE);
                // 自动保存版本v1（首次提交）
                versionService.saveVersion(contract.getNum(), content, currentFileData,
                    currentFileName, currentUser.getName(), "首次起草合同");
                resetForm();  // 重置表单以便继续起草下一个合同
            } else {
                // 保存失败：显示错误提示
                JOptionPane.showMessageDialog(this, "起草失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            // 日期格式异常时的友好提示
            JOptionPane.showMessageDialog(this, "日期格式不正确，请使用 yyyy-MM-dd 格式！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 上传合同附件
     * <p>
     * 点击"上传附件"按钮后触发此方法。弹出JFileChooser文件选择对话框，
     * 用户可以选择PDF或Word文档（.pdf/.docx/.doc）作为合同附件。
     * 选择文件后读取文件内容到内存，并更新界面显示已选择的文件名。
     * </p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>创建文件选择器并设置过滤器（仅允许PDF/DOCX/DOC）</li>
     *   <li>用户选择文件后进行类型校验</li>
     *   <li>将文件读入内存字节数组保存到currentFileData字段</li>
     *   <li>更新界面显示文件名并启用下载按钮</li>
     * </ol>
     */
    private void uploadAttachment() {
        // 创建文件选择器对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择合同附件");  // 设置对话框标题
        // 设置文件过滤器：只显示PDF和Word文档
        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
            "合同文档 (PDF, Word)", "pdf", "docx", "doc");
        fileChooser.setFileFilter(filter);  // 应用过滤器
        fileChooser.setAcceptAllFileFilterUsed(false);  // 禁用"所有文件"选项

        // 显示文件选择对话框
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;  // 用户取消了选择，直接返回
        }

        // 获取用户选择的文件
        File selectedFile = fileChooser.getSelectedFile();
        String selectedName = selectedFile.getName();

        // 校验文件类型是否在允许列表中（双重校验确保安全）
        if (!FileUploadUtil.isAllowedFileType(selectedName)) {
            JOptionPane.showMessageDialog(this,
                "不支持的文件类型！仅允许上传 PDF、DOCX、DOC 格式的文件。",
                "文件类型错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 使用工具类将文件读取为字节数组
        byte[] fileBytes = FileUploadUtil.readFileToBytes(selectedFile);
        if (fileBytes == null) {
            JOptionPane.showMessageDialog(this,
                "文件读取失败，请检查文件是否存在且可访问。",
                "读取失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 保存文件数据到内存字段（提交合同时一起存入数据库）
        currentFileData = fileBytes;
        currentFileName = selectedName;

        // 更新界面：显示已选择的文件名
        lblFileName.setText("📄 " + selectedName + " (" + formatFileSize(fileBytes.length) + ")");
        lblFileName.setForeground(new Color(39, 174, 96));  // 绿色表示成功
        // 启用下载按钮（已有文件可供下载）
        btnDownloadFile.setEnabled(true);

        System.out.println("[起草面板] 附件加载成功: " + selectedName + ", 大小: " + fileBytes.length + " 字节");
    }

    /**
     * 下载合同附件到本地
     * <p>
     * 点击"下载"按钮后触发此方法。如果当前已有选择的附件文件，
     * 弹出保存文件对话框让用户选择保存位置和文件名，
     * 然后将内存中的文件数据写入用户指定的本地路径。
     * </p>
     */
    private void downloadAttachment() {
        // 检查是否有可下载的文件数据
        if (currentFileData == null || currentFileName == null || currentFileName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "当前没有可下载的附件，请先上传附件。",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 创建文件保存对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存合同附件");  // 设置对话框标题
        // 设置默认文件名为当前附件名称
        fileChooser.setSelectedFile(new java.io.File(currentFileName));

        // 显示保存文件对话框
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;  // 用户取消了保存操作
        }

        // 获取用户选择的保存路径
        File saveFile = fileChooser.getSelectedFile();
        // 如果用户没有手动输入扩展名，自动追加原始扩展名
        if (!saveFile.getName().contains(".")) {
            saveFile = new java.io.File(saveFile.getAbsolutePath() + "." +
                FileUploadUtil.getFileExtension(currentFileName));
        }

        // 使用工具类将字节数据写入本地文件
        FileUploadUtil.saveBytesToFile(currentFileData, saveFile.getAbsolutePath());
        JOptionPane.showMessageDialog(this,
            "附件已保存到: " + saveFile.getAbsolutePath(),
            "下载成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 格式化文件大小为人类可读字符串
     * <p>
     * 将字节数转换为KB、MB、GB等可读格式，用于界面上显示文件大小。
     * </p>
     *
     * @param bytes 文件大小（字节）
     * @return 格式化后的字符串，如 "1.5 MB"、"320 KB"
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 加载默认合同模板
     * <p>
     * 将标准合同模板内容填充到"合同内容"文本区域中。
     * 模板包含完整的合同条款框架，用户可以在此基础上进行修改和填写。
     * 如果文本区域已有内容，会提示用户是否覆盖。
     * </p>
     *
     * <p>模板内容包括：</p>
     * <ul>
     *   <li>合同基本信息（编号、名称、甲乙方）</li>
     *   <li>合同标的、金额及支付方式</li>
     *   <li>履行期限、双方权利与义务</li>
     *   <li>违约责任、争议解决方式</li>
     *   <li>其他约定、附则及签章区域</li>
     * </ul>
     */
    private void loadContractTemplate() {
        // 如果文本区域已有内容，提示用户确认是否覆盖
        if (!txtContent.getText().trim().isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this,
                "当前合同内容不为空，是否用模板覆盖现有内容？",
                "确认覆盖", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                return;  // 用户选择不覆盖，直接返回
            }
        }

        // 定义标准合同模板内容
        String template = "合同编号：[自动生成]\n" +
            "合同名称：[请填写]\n" +
            "甲方（委托方）：____________________\n" +
            "乙方（受托方）：____________________\n" +
            "\n" +
            "一、合同标的\n" +
            "__________________________________________\n" +
            "\n" +
            "二、合同金额及支付方式\n" +
            "__________________________________________\n" +
            "\n" +
            "三、履行期限\n" +
            "自____年____月____日起至____年____月____日止。\n" +
            "\n" +
            "四、双方权利与义务\n" +
            "4.1 甲方权利与义务：\n" +
            "__________________________________________\n" +
            "4.2 乙方权利与义务：\n" +
            "__________________________________________\n" +
            "\n" +
            "五、违约责任\n" +
            "__________________________________________\n" +
            "\n" +
            "六、争议解决方式\n" +
            "本合同在履行过程中发生争议，由双方协商解决；协商不成的，提交____________仲裁委员会仲裁。\n" +
            "\n" +
            "七、其他约定\n" +
            "__________________________________________\n" +
            "\n" +
            "八、附则\n" +
            "8.1 本合同一式两份，甲乙双方各执一份。\n" +
            "8.2 本合同自双方签字盖章之日起生效。\n" +
            "\n" +
            "甲方（签章）：________________    日期：____年____月____日\n" +
            "乙方（签章）：________________    日期：____年____月____日";

        // 将模板内容设置到文本区域
        txtContent.setText(template);
    }

    /**
     * 弹出日历选择对话框，将选中的日期填入指定的文本框
     * <p>
     * 使用纯Swing实现的CalendarPickerUtil工具类弹出月历选择器，
     * 用户从日历选择日期后，自动将日期格式化为 yyyy-MM-dd 填入文本框。
     * </p>
     *
     * @param targetField 目标文本框，选中的日期将被填入此框
     */
    private void showDatePickerDialog(JTextField targetField) {
        // 如果文本框已有日期值，则作为初始选中日期传入
        Date initialDate = null;
        try {
            String currentText = targetField.getText().trim();
            if (!currentText.isEmpty()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                initialDate = sdf.parse(currentText);
            }
        } catch (Exception ignored) { }

        // 调用纯Swing日历选择器
        Date selected = CalendarPickerUtil.showDatePicker(ContractDraftPanel.this, "选择日期", initialDate);
        if (selected != null) {
            targetField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(selected));
            // 选择后触发校验
            validateAndFixEndTime();
        }
    }

    /**
     * 重置表单到初始状态
     * <p>
     * 清空所有输入字段，并将时间字段恢复为默认值：
     * <ul>
     *   <li>合同名称：清空</li>
     *   <li>开始时间：设置为当前日期</li>
     *   <li>结束时间：设置为当前日期+1年</li>
     *   <li>合同内容：清空</li>
     * </ul>
     * </p>
     */
    private void resetForm() {
        // 清空合同名称
        txtName.setText("");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        // 开始时间重置为今天
        txtBeginTime.setText(sdf.format(new Date()));
        // 结束时间重置为一年后
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, 1);
        txtEndTime.setText(sdf.format(cal.getTime()));
        // 清空合同内容
        txtContent.setText("");
        // 重置合同金额
        txtAmount.setText("0");
        // 重置附件相关状态
        currentFileData = null;           // 清空内存中的文件数据
        currentFileName = null;          // 清空文件名
        lblFileName.setText("未选择文件"); // 重置文件名显示
        lblFileName.setForeground(Color.GRAY);  // 恢复默认灰色
        btnDownloadFile.setEnabled(false);      // 禁用下载按钮（无文件可下载）
    }

    /**
     * 显示AI审查对话框
     * <p>
     * 弹出对话框调用AI服务对当前编辑的合同内容进行智能审查，
     * 以异步方式执行避免阻塞UI线程。
     * </p>
     */
    private void showAIReviewDialog() {
        String content = txtContent.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可供审查的合同内容，请先填写合同内容！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 创建AI审查结果对话框
        JDialog dialog = new JDialog((javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "🤖 AI智能审查", false);
        dialog.setLayout(new BorderLayout(5, 5));
        dialog.setSize(700, 550);
        dialog.setLocationRelativeTo(this);

        // AI审查结果显示区域
        JTextArea txtResult = new JTextArea();
        txtResult.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        txtResult.setEditable(false);
        txtResult.setText("⏳ 正在调用AI审查，请稍候...");
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
            SwingUtilities.invokeLater(() -> txtResult.setText(result));
        }).start();
    }

    /**
     * 执行OCR文字识别
     * <p>
     * 点击"OCR识别"按钮后触发此方法：
     * <ol>
     *   <li>弹出文件选择器让用户选择图片文件</li>
     *   <li>使用异步线程调用OCR服务识别图片中的文字</li>
     *   <li>将识别结果填入合同内容文本区域</li>
     * </ol>
     * 使用异步线程是因为OCR识别可能耗时较长，避免阻塞UI界面。
     * </p>
     */
    private void performOCRRecognition() {
        // 创建文件选择器对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要识别的合同图片");
        // 设置文件过滤器：只显示常见图片格式
        javax.swing.filechooser.FileFilter imageFilter = new javax.swing.filechooser.FileNameExtensionFilter(
            "图片文件 (PNG, JPG, BMP, TIFF)", "png", "jpg", "jpeg", "bmp", "tiff");
        fileChooser.setFileFilter(imageFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // 显示文件选择对话框
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;  // 用户取消了选择
        }

        // 获取用户选择的图片文件
        File selectedImage = fileChooser.getSelectedFile();

        // 创建进度提示对话框
        JDialog progressDlg = new JDialog((javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "📷 OCR文字识别", true);  // 模态对话框
        progressDlg.setLayout(new BorderLayout(10, 10));
        progressDlg.setSize(450, 200);
        progressDlg.setLocationRelativeTo(this);

        // 进度提示信息
        JPanel msgPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        msgPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblStatus = new JLabel("⏳ 正在识别图片文字，请稍候...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        msgPanel.add(lblStatus);

        JLabel lblEngine = new JLabel("使用引擎: " + com.contract.util.OCRService.getCurrentEngine().desc, SwingConstants.CENTER);
        lblEngine.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblEngine.setForeground(Color.GRAY);
        msgPanel.add(lblEngine);

        progressDlg.add(msgPanel, BorderLayout.CENTER);
        progressDlg.setVisible(true);

        // 异步执行OCR识别（避免阻塞UI线程）
        new Thread(() -> {
            try {
                // 调用OCR服务进行文字识别
                String ocrResult = com.contract.util.OCRService.recognizeImage(selectedImage);

                // 在UI线程中更新结果
                SwingUtilities.invokeLater(() -> {
                    progressDlg.dispose();  // 关闭进度对话框

                    // 将识别结果填入合同内容文本区
                    txtContent.setText(ocrResult);

                    // 提示用户识别完成
                    JOptionPane.showMessageDialog(ContractDraftPanel.this,
                        "OCR识别完成！\n\n已将识别结果填入"合同内容"文本区域。\n" +
                        "请检查并修改识别不准确的内容。",
                        "识别完成", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                // OCR识别异常处理
                SwingUtilities.invokeLater(() -> {
                    progressDlg.dispose();  // 关闭进度对话框
                    JOptionPane.showMessageDialog(ContractDraftPanel.this,
                        "OCR识别失败: " + e.getMessage(),
                        "识别错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}
