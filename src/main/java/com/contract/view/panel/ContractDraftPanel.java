package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.Customer;
import com.contract.service.ContractService;
import com.contract.service.CustomerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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
    // 客户业务服务类，用于加载客户列表
    private CustomerService customerService = new CustomerService();
    // 当前登录用户信息
    private com.contract.entity.User currentUser;

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
        txtBeginTime = new JTextField(15);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        // 默认显示当前日期作为开始时间
        txtBeginTime.setText(sdf.format(new Date()));
        // 输入开始时间时触发焦点丢失事件，自动校验结束时间
        txtBeginTime.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) { validateAndFixEndTime(); }
        });
        formPanel.add(txtBeginTime, gbc);

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

        // 增加时间的快捷按钮（放在右边）
        JButton btnPlusHalf = createSmallBtn("+半年");
        btnPlusHalf.addActionListener(e -> adjustEndTime(6));   // 增加6个月
        JButton btnPlusYear = createSmallBtn("+一年");
        btnPlusYear.addActionListener(e -> adjustEndTime(12));  // 增加12个月
        endTimePanel.add(btnPlusHalf);
        endTimePanel.add(btnPlusYear);

        formPanel.add(endTimePanel, gbc);

        // ---- 第4行：合同内容 ----
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;  // 标签左上对齐
        formPanel.add(createLabel("合同内容:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;      // 文本区居中
        txtContent = new JTextArea(10, 30);  // 10行30列的多行文本区
        txtContent.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        txtContent.setLineWrap(true);  // 启用自动换行
        JScrollPane scrollContent = new JScrollPane(txtContent);  // 添加滚动条
        formPanel.add(scrollContent, gbc);

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

            // 调用服务层保存合同
            if (contractService.draftContract(contract)) {
                // 保存成功：显示生成的合同编号并重置表单
                JOptionPane.showMessageDialog(this, "起草成功！合同编号: " + contract.getNum(), "成功", JOptionPane.INFORMATION_MESSAGE);
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
    }
}
