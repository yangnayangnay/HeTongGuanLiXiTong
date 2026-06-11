package com.contract.view;

import com.contract.service.UserService;

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

    /**
     * 构造方法：初始化注册窗口
     */
    public RegisterFrame() {
        setTitle("📝 合同管理系统 - 注册");
        setSize(420, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // 关闭时只销毁本窗口，不退出程序
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    /**
     * 初始化界面组件
     */
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        mainPanel.setBackground(new Color(245, 245, 250));

        // 标题区域面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        // 主标题
        JLabel lblTitle = new JLabel("👤 用户注册", SwingConstants.CENTER);
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 20));
        lblTitle.setForeground(new Color(44, 62, 80));  // #2C3E50 深蓝灰
        titlePanel.add(lblTitle, BorderLayout.CENTER);

        // 副标题
        JLabel lblSubtitle = new JLabel("Create Your Account", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSubtitle.setForeground(new Color(52, 152, 219));  // #3498DB 亮蓝
        titlePanel.add(lblSubtitle, BorderLayout.SOUTH);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("用户名:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        txtUsername = new JTextField(15);
        formPanel.add(txtUsername, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(createLabel("密  码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        txtPassword = new JPasswordField(15);
        formPanel.add(txtPassword, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(createLabel("确认密码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        txtConfirmPassword = new JPasswordField(15);
        formPanel.add(txtConfirmPassword, gbc);

        // 邮箱（必填，用于接收任务通知）
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(createLabel("邮  箱:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1;
        txtEmail = new JTextField(15);
        txtEmail.setToolTipText("请输入有效的邮箱地址，用于接收合同任务通知邮件");
        formPanel.add(txtEmail, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        JButton btnRegister = new JButton("📝 注 册");
        btnRegister.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnRegister.setPreferredSize(new Dimension(120, 38));
        btnRegister.setBackground(new Color(39, 174, 96));  // #27AE60 绿色
        btnRegister.setOpaque(true);
        btnRegister.setForeground(Color.WHITE);  // 白色文字
        btnRegister.setFocusPainted(false);

        JButton btnCancel = new JButton("取 消");
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.setBackground(new Color(149, 165, 166));  // 灰色
        btnCancel.setOpaque(true);
        btnCancel.setContentAreaFilled(true);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);

        btnPanel.add(btnRegister);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 注册按钮事件
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        // 取消按钮事件：关闭注册窗口
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * 创建表单标签
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        return label;
    }

    /**
     * 执行注册操作
     * <p>校验输入数据，调用Service完成注册</p>
     */
    private void register() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        String email = txtEmail.getText().trim();

        // 校验所有字段非空
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有字段不能为空！（邮箱为必填项）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 校验两次密码一致
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 校验邮箱格式（简单正则校验）
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            JOptionPane.showMessageDialog(this, "请输入有效的邮箱地址格式！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 调用Service进行注册（传入邮箱参数）
        boolean success = userService.register(username, password, email);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "注册成功！请等待管理员审核通过后即可登录。",
                "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();  // 注册成功后关闭窗口
        } else {
            JOptionPane.showMessageDialog(this, "注册失败，用户名可能已存在！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
