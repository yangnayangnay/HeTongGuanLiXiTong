package com.contract.view;

import com.contract.entity.User;
import com.contract.service.UserService;
import com.contract.util.FileLogger;
import com.contract.util.I18NUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 登录窗口（LoginFrame）
 * <p>
 * 系统的入口界面，提供用户登录功能。
 * 验证用户身份后进入主界面。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>用户名密码输入</li>
 *   <li>登录验证（含状态检查）</li>
 *   <li>跳转到注册页面</li>
 *   <li>支持回车键快捷登录</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class LoginFrame extends JFrame {
    /** 用户名输入框 */
    private JTextField txtUsername;
    /** 密码输入框（使用JPasswordField隐藏显示） */
    private JPasswordField txtPassword;
    /** 用户服务对象，用于调用业务逻辑 */
    private UserService userService = new UserService();

    // ===== 颜色常量 =====
    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color TITLE_COLOR = new Color(44, 62, 80);
    private static final Color SUBTITLE_COLOR = new Color(100, 120, 140);
    private static final Color ACCENT_COLOR = new Color(41, 128, 185);   // 主色调：深蓝
    private static final Color BTN_LOGIN_BG = new Color(41, 128, 185);   // 登录按钮：深蓝
    private static final Color BTN_LOGIN_HOVER = new Color(31, 97, 141);
    private static final Color LABEL_COLOR = new Color(80, 90, 100);
    private static final Color INPUT_BORDER = new Color(200, 210, 220);
    private static final Color INPUT_FOCUS = new Color(41, 128, 185);

    /**
     * 构造方法：初始化登录窗口
     */
    public LoginFrame() {
        setTitle(I18NUtil.getString("login.title"));
        setSize(460, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel(I18NUtil.getString("app.title"), SwingConstants.CENTER);
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 26));
        lblTitle.setForeground(TITLE_COLOR);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Contract Management System", SwingConstants.CENTER);
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
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(separator);
        mainPanel.add(Box.createVerticalStrut(25));

        // ===== 表单区域（对称居中） =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 统一标签宽度，保证左右对称
        Dimension labelSize = new Dimension(70, 30);
        Dimension inputSize = new Dimension(220, 36);

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblUser = new JLabel(I18NUtil.getString("login.username"), SwingConstants.RIGHT);
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
        JLabel lblPwd = new JLabel(I18NUtil.getString("login.password"), SwingConstants.RIGHT);
        lblPwd.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblPwd.setForeground(LABEL_COLOR);
        lblPwd.setPreferredSize(labelSize);
        formPanel.add(lblPwd, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        txtPassword = createStyledPasswordField();
        txtPassword.setPreferredSize(inputSize);
        formPanel.add(txtPassword, gbc);

        // 表单居中
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        formWrapper.setOpaque(false);
        formWrapper.add(formPanel);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(formWrapper);
        mainPanel.add(Box.createVerticalStrut(25));

        // ===== 按钮区域 =====
        JButton btnLogin = new JButton(I18NUtil.getString("login.btnLogin"));
        btnLogin.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btnLogin.setPreferredSize(new Dimension(300, 42));
        btnLogin.setBackground(BTN_LOGIN_BG);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setContentAreaFilled(true);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        // 悬停效果
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(BTN_LOGIN_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(BTN_LOGIN_BG);
            }
        });

        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(18));

        // 注册链接
        JLabel lblRegister = new JLabel(I18NUtil.getString("login.registerTip"));
        lblRegister.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        lblRegister.setForeground(ACCENT_COLOR);
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new RegisterFrame().setVisible(true);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblRegister.setForeground(BTN_LOGIN_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblRegister.setForeground(ACCENT_COLOR);
            }
        });

        mainPanel.add(lblRegister);

        add(mainPanel);

        // 登录按钮事件
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        // 密码框回车事件
        txtPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
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
     * 执行登录操作
     */
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            FileLogger.warn("LoginFrame", "login", "登录校验失败: 用户名或密码为空");
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FileLogger.info("LoginFrame", "login", "用户登录尝试: username=" + username);
        User user = userService.login(username, password);
        if (user != null) {
            FileLogger.info("LoginFrame", "login", "用户登录成功: username=" + username);
            this.dispose();
            new MainFrame(user).setVisible(true);
        } else {
            FileLogger.warn("LoginFrame", "login", "用户登录失败: username=" + username);
            User checkUser = userService.getUserForStatusCheck(username);
            if (checkUser == null) {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
            } else if (checkUser.getStatus() == UserService.STATUS_PENDING) {
                JOptionPane.showMessageDialog(this,
                    "您的账号正在等待管理员审核，请耐心等待！",
                    "提示", JOptionPane.WARNING_MESSAGE);
            } else if (checkUser.getStatus() == UserService.STATUS_REJECTED) {
                JOptionPane.showMessageDialog(this,
                    "您的账号已被管理员拒绝，请联系管理员！",
                    "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
