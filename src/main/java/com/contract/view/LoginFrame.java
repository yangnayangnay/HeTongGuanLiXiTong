package com.contract.view;

import com.contract.entity.User;
import com.contract.service.UserService;

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

    /**
     * 构造方法：初始化登录窗口
     */
    public LoginFrame() {
        setTitle("合同管理系统 - 登录");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 关闭窗口时退出程序
        setLocationRelativeTo(null);  // 窗口居中显示
        setResizable(false);  // 禁止调整大小
        initUI();
    }

    /**
     * 初始化界面组件
     */
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(245, 245, 250));  // 浅灰背景

        // 标题标签
        JLabel lblTitle = new JLabel("合同管理系统", SwingConstants.CENTER);
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 24));
        lblTitle.setForeground(new Color(51, 51, 102));  // 深蓝色标题
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // 表单面板（用户名、密码输入区）
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);  // 透明背景
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);  // 组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名输入行
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("用户名:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(txtUsername, gbc);

        // 密码输入行
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(createLabel("密  码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        txtPassword = new JPasswordField(15);  // 密码框自动隐藏字符
        txtPassword.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板（登录、注册按钮）
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        JButton btnLogin = new JButton("登 录");
        btnLogin.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnLogin.setPreferredSize(new Dimension(100, 35));
        btnLogin.setBackground(new Color(66, 133, 244));  // 蓝色登录按钮
        btnLogin.setOpaque(true);
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFocusPainted(false);  // 去除焦点边框

        JButton btnRegister = new JButton("注 册");
        btnRegister.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnRegister.setPreferredSize(new Dimension(100, 35));
        btnRegister.setBackground(new Color(52, 168, 83));  // 绿色注册按钮
        btnRegister.setOpaque(true);
        btnRegister.setContentAreaFilled(true);
        btnRegister.setForeground(Color.BLACK);
        btnRegister.setFocusPainted(false);

        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 登录按钮事件：执行登录逻辑
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        // 注册按钮事件：打开注册窗口
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame().setVisible(true);
            }
        });

        // 密码框回车事件：支持回车键快捷登录
        txtPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
    }

    /**
     * 创建表单标签的辅助方法
     *
     * @param text 标签文字
     * @return 格式化后的JLabel
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        return label;
    }

    /**
     * 执行登录操作
     * <p>获取用户输入，调用Service层验证，根据结果显示不同提示</p>
     */
    private void login() {
        String username = txtUsername.getText().trim();  // 获取并去除首尾空格
        String password = new String(txtPassword.getPassword()).trim();

        // 输入校验
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 调用Service进行登录验证
        User user = userService.login(username, password);
        if (user != null) {
            // 登录成功：关闭登录窗，打开主窗口
            this.dispose();
            new MainFrame(user).setVisible(true);
        } else {
            // 登录失败：区分不同的失败原因给出具体提示
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
