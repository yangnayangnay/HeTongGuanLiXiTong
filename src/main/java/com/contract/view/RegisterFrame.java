package com.contract.view;

import com.contract.service.UserService;
import com.contract.util.FileLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 注册窗口（RegisterFrame）
 * <p>
 * 提供新用户注册功能。注册的用户默认处于"待审核"状态，
 * 需要管理员审批通过后才能使用系统。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class RegisterFrame extends JFrame {
    /** 用户名输入框 */
    private JTextField txtUsername;
    /** 密码输入框 */
    private JPasswordField txtPassword;
    /** 确认密码输入框 */
    private JPasswordField txtConfirmPassword;
    /** 邮箱输入框（必填，用于接收任务通知邮件） */
    private JTextField txtEmail;
    /** 用户服务对象 */
    private UserService userService = new UserService();

    // ===== 颜色常量（与LoginFrame统一风格） =====
    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color TITLE_COLOR = new Color(44, 62, 80);
    private static final Color SUBTITLE_COLOR = new Color(100, 120, 140);
    private static final Color ACCENT_COLOR = new Color(41, 128, 185);
    private static final Color BTN_REGISTER_BG = new Color(39, 174, 96);   // 注册按钮：绿色
    private static final Color BTN_REGISTER_HOVER = new Color(30, 145, 78);
    private static final Color BTN_CANCEL_BG = new Color(149, 165, 166);
    private static final Color BTN_CANCEL_HOVER = new Color(127, 140, 141);
    private static final Color LABEL_COLOR = new Color(80, 90, 100);
    private static final Color INPUT_BORDER = new Color(200, 210, 220);

    /**
     * 构造方法：初始化注册窗口
     */
    public RegisterFrame() {
        setTitle("合同管理系统 - 注册");
        setSize(460, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    /**
     * 初始化界面组件
     */
    private void initUI() {
        // 主面板：垂直布局，整体居中
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(35, 60, 35, 60));

        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("用户注册", SwingConstants.CENTER);
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 24));
        lblTitle.setForeground(TITLE_COLOR);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Create Your Account", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(SUBTITLE_COLOR);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 分隔线
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(220, 225, 230));
        separator.setMaximumSize(new Dimension(300, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(lblSubtitle);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(separator);
        mainPanel.add(Box.createVerticalStrut(20));

        // ===== 表单区域（对称居中） =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 统一标签宽度，保证左右对称
        Dimension labelSize = new Dimension(75, 30);
        Dimension inputSize = new Dimension(220, 36);

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblUser = new JLabel("用户名:", SwingConstants.RIGHT);
        lblUser.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblUser.setForeground(LABEL_COLOR);
        lblUser.setPreferredSize(labelSize);
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        txtUsername = createStyledTextField();
        txtUsername.setPreferredSize(inputSize);
        formPanel.add(txtUsername, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblPwd = new JLabel("密  码:", SwingConstants.RIGHT);
        lblPwd.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblPwd.setForeground(LABEL_COLOR);
        lblPwd.setPreferredSize(labelSize);
        formPanel.add(lblPwd, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        txtPassword = createStyledPasswordField();
        txtPassword.setPreferredSize(inputSize);
        formPanel.add(txtPassword, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblConfirm = new JLabel("确认密码:", SwingConstants.RIGHT);
        lblConfirm.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblConfirm.setForeground(LABEL_COLOR);
        lblConfirm.setPreferredSize(labelSize);
        formPanel.add(lblConfirm, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        txtConfirmPassword = createStyledPasswordField();
        txtConfirmPassword.setPreferredSize(inputSize);
        formPanel.add(txtConfirmPassword, gbc);

        // 邮箱（必填）
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblEmail = new JLabel("邮  箱:*", SwingConstants.RIGHT);
        lblEmail.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblEmail.setForeground(LABEL_COLOR);
        lblEmail.setPreferredSize(labelSize);
        formPanel.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1;
        txtEmail = createStyledTextField();
        txtEmail.setPreferredSize(inputSize);
        txtEmail.setToolTipText("请输入有效的邮箱地址，用于接收合同任务通知邮件");
        formPanel.add(txtEmail, gbc);

        // 表单居中
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        formWrapper.setOpaque(false);
        formWrapper.add(formPanel);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(formWrapper);
        mainPanel.add(Box.createVerticalStrut(22));

        // ===== 按钮区域（居中并排） =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnRegister = new JButton("注 册");
        btnRegister.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnRegister.setPreferredSize(new Dimension(130, 40));
        btnRegister.setBackground(BTN_REGISTER_BG);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setOpaque(true);
        btnRegister.setContentAreaFilled(true);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnRegister.setBackground(BTN_REGISTER_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnRegister.setBackground(BTN_REGISTER_BG);
            }
        });

        JButton btnCancel = new JButton("取 消");
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnCancel.setPreferredSize(new Dimension(130, 40));
        btnCancel.setBackground(BTN_CANCEL_BG);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setOpaque(true);
        btnCancel.setContentAreaFilled(true);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCancel.setBackground(BTN_CANCEL_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCancel.setBackground(BTN_CANCEL_BG);
            }
        });

        btnPanel.add(btnRegister);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel);

        add(mainPanel);

        // 注册按钮事件
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        // 取消按钮事件
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * 创建统一样式的文本输入框
     */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return field;
    }

    /**
     * 创建统一样式的密码输入框
     */
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return field;
    }

    /**
     * 执行注册操作
     */
    private void register() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            FileLogger.warn("RegisterFrame", "register", "注册校验失败: 存在空字段");
            JOptionPane.showMessageDialog(this, "所有字段不能为空！（邮箱为必填项）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            FileLogger.warn("RegisterFrame", "register", "注册校验失败: 两次密码不一致, username=" + username);
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            FileLogger.warn("RegisterFrame", "register", "注册校验失败: 邮箱格式不正确, email=" + email);
            JOptionPane.showMessageDialog(this, "请输入有效的邮箱地址格式！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FileLogger.info("RegisterFrame", "register", "用户注册: username=" + username + ", email=" + email);
        boolean success = userService.register(username, password, email);
        if (success) {
            FileLogger.info("RegisterFrame", "register", "用户注册成功: username=" + username);
            JOptionPane.showMessageDialog(this,
                "注册成功！请等待管理员审核通过后即可登录。",
                "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            FileLogger.warn("RegisterFrame", "register", "用户注册失败: username=" + username + " (用户名可能已存在)");
            JOptionPane.showMessageDialog(this, "注册失败，用户名可能已存在！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
