package com.contract.view.panel;

import com.contract.util.AIAssistantService;
import com.contract.util.AppSettingsUtil;
import com.contract.util.EmailService;
import com.contract.util.I18NUtil;
import com.contract.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * 系统设置面板
 * <p>提供个性化配置选项：</p>
 * <ul>
 *   <li>外观设置（主题颜色、背景图片、提示音）</li>
 *   <li>通知设置（邮件SMTP配置）</li>
 *   <li>AI助手设置（Ollama服务配置）</li>
 *   <li>定时提醒设置（检查间隔）</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 */
public class SettingsPanel extends JPanel {

    // ===== 外观设置组件 =====
    private JComboBox<ThemeManager.ThemeType> cmbThemeColor;  // 主题颜色选择（使用ThemeManager枚举）
    private JTextField txtBackgroundImage;         // 背景图片路径
    private JButton btnBrowseImg;                 // 浏览背景图按钮
    private JButton btnClearImg;                  // 清除背景图按钮
    private JComboBox<String> cmbSoundEffect;      // 提示音效选择

    // ===== 通知设置组件 =====
    private JTextField txtSmtpHost;               // SMTP服务器
    private JTextField txtSmtpPort;               // SMTP端口
    private JTextField txtSenderEmail;             // 发件邮箱
    private JPasswordField txtSenderPassword;       // 授权码
    private JButton btnTestEmail;                 // 测试邮件按钮
    private JButton btnSaveEmailSettings;         // 保存邮件设置按钮

    // ===== AI设置组件 =====
    private JTextField txtAiUrl;                  // AI服务地址
    private JTextField txtAiModel;                // AI模型名
    private JButton btnTestAI;                    // 测试AI连接按钮
    private JButton btnSaveAiSettings;            // 保存AI设置按钮

    // ===== 提醒设置组件 =====
    private JTextField txtReminderInterval;        // 提醒间隔（分钟）
    private JTextField txtExpiryInterval;           // 到期检查间隔（小时）

    /**
     * 构造方法：初始化系统设置面板
     */
    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadSettings();
    }

    /**
     * 初始化界面组件
     */
    private void initUI() {
        // 标题区域
        JLabel lblTitle = new JLabel("⚙️ 系统设置");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 设置表单面板（GridBagLayout）
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // === 外观设置分组 ===
        formPanel.add(createSectionLabel("--- 外观设置 ---"), getGbc(row++, 0, 2));

        // 主题颜色（使用ThemeManager的主题枚举）
        formPanel.add(new JLabel("主题颜色:"), getGbc(row, 0, 1));
        cmbThemeColor = new JComboBox<>(new ThemeManager.ThemeType[]{
            ThemeManager.ThemeType.LIGHT, ThemeManager.ThemeType.DARK,
            ThemeManager.ThemeType.BLUE, ThemeManager.ThemeType.GREEN
        });
        // 切换主题时立即应用
        cmbThemeColor.addActionListener(e -> {
            ThemeManager.ThemeType selected = (ThemeManager.ThemeType) cmbThemeColor.getSelectedItem();
            if (selected != null) ThemeManager.setTheme(selected);
        });
        formPanel.add(cmbThemeColor, getGbc(row++, 1, 1));

        // 背景图片
        formPanel.add(new JLabel("背景图片:"), getGbc(row, 0, 1));
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        txtBackgroundImage = new JTextField(20);
        btnBrowseImg = new JButton("浏览...");
        btnBrowseImg.addActionListener(e -> browseBackgroundImage());
        btnClearImg = new JButton("清除");
        btnClearImg.addActionListener(e -> { txtBackgroundImage.setText(""); });
        imgPanel.add(txtBackgroundImage);
        imgPanel.add(btnBrowseImg);
        imgPanel.add(btnClearImg);
        formPanel.add(imgPanel, getGbc(row++, 1, 1));

        // 提示音
        formPanel.add(new JLabel("提示音效:"), getGbc(row, 0, 1));
        cmbSoundEffect = new JComboBox<>(new String[]{"系统默认", "提示音1", "提示音2", "静音"});
        formPanel.add(cmbSoundEffect, getGbc(row++, 1, 1));

        // 界面语言（国际化设置）
        formPanel.add(new JLabel("界面语言:"), getGbc(row, 0, 1));
        JComboBox<String> cmbLanguage = new JComboBox<>(new String[]{"简体中文", "English"});
        // 根据当前locale设置选中项
        if (I18NUtil.getCurrentLocale() == Locale.US) {
            cmbLanguage.setSelectedIndex(1);
        } else {
            cmbLanguage.setSelectedIndex(0);
        }
        formPanel.add(cmbLanguage, getGbc(row++, 1, 1));
        cmbLanguage.addActionListener(e -> {
            int selectedIndex = cmbLanguage.getSelectedIndex();
            if (selectedIndex == 1) {
                I18NUtil.setLocale(java.util.Locale.US);
            } else {
                I18NUtil.setLocale(java.util.Locale.CHINESE);
            }
            JOptionPane.showMessageDialog(SettingsPanel.this,
                "语言设置已保存！\n\n部分界面文本将在下次启动后完全生效。",
                "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        // === 通知设置分组 ===
        formPanel.add(createSectionLabel("--- 通知设置 ---"), getGbc(row++, 0, 2));

        // SMTP配置
        formPanel.add(new JLabel("SMTP服务器:"), getGbc(row, 0, 1));
        txtSmtpHost = new JTextField(20);
        txtSmtpHost.setText("smtp.qq.com");
        formPanel.add(txtSmtpHost, getGbc(row++, 1, 1));

        formPanel.add(new JLabel("SMTP端口:"), getGbc(row, 0, 1));
        txtSmtpPort = new JTextField(20);
        txtSmtpPort.setText("465");
        formPanel.add(txtSmtpPort, getGbc(row++, 1, 1));

        formPanel.add(new JLabel("发件人邮箱:"), getGbc(row, 0, 1));
        txtSenderEmail = new JTextField(20);
        formPanel.add(txtSenderEmail, getGbc(row++, 1, 1));

        formPanel.add(new JLabel("授权码:"), getGbc(row, 0, 1));
        txtSenderPassword = new JPasswordField(20);
        formPanel.add(txtSenderPassword, getGbc(row++, 1, 1));

        // 邮件操作按钮
        JPanel emailTestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnTestEmail = new JButton("测试发送");
        btnTestEmail.setFocusPainted(false);
        btnTestEmail.setBackground(new Color(52, 152, 219));
        btnTestEmail.setOpaque(true);
        btnTestEmail.setForeground(Color.WHITE);
        btnTestEmail.addActionListener(e -> testEmailSettings());
        btnSaveEmailSettings = new JButton("保存邮件设置");
        btnSaveEmailSettings.setFocusPainted(false);
        btnSaveEmailSettings.setBackground(new Color(46, 204, 113));
        btnSaveEmailSettings.setOpaque(true);
        btnSaveEmailSettings.setForeground(Color.BLACK);
        btnSaveEmailSettings.addActionListener(e -> saveEmailSettings());
        emailTestPanel.add(btnTestEmail);
        emailTestPanel.add(btnSaveEmailSettings);
        formPanel.add(emailTestPanel, getGbc(row++, 1, 1));

        // === AI助手设置分组 ===
        formPanel.add(createSectionLabel("--- AI助手设置 ---"), getGbc(row++, 0, 2));

        formPanel.add(new JLabel("AI服务地址:"), getGbc(row, 0, 1));
        txtAiUrl = new JTextField(20);
        txtAiUrl.setText("http://localhost:11434");
        formPanel.add(txtAiUrl, getGbc(row++, 1, 1));

        formPanel.add(new JLabel("AI模型:"), getGbc(row, 0, 1));
        txtAiModel = new JTextField(20);
        txtAiModel.setText("qwen2.5");
        formPanel.add(txtAiModel, getGbc(row++, 1, 1));

        // AI操作按钮
        JPanel aiTestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnTestAI = new JButton("测试连接");
        btnTestAI.setFocusPainted(false);
        btnTestAI.setBackground(new Color(52, 152, 219));
        btnTestAI.setOpaque(true);
        btnTestAI.setForeground(Color.WHITE);
        btnTestAI.addActionListener(e -> testAIConnection());
        btnSaveAiSettings = new JButton("保存AI设置");
        btnSaveAiSettings.setFocusPainted(false);
        btnSaveAiSettings.setBackground(new Color(46, 204, 113));
        btnSaveAiSettings.setOpaque(true);
        btnSaveAiSettings.setForeground(Color.BLACK);
        btnSaveAiSettings.addActionListener(e -> saveAISettings());
        aiTestPanel.add(btnTestAI);
        aiTestPanel.add(btnSaveAiSettings);
        formPanel.add(aiTestPanel, getGbc(row++, 1, 1));

        // === 定时提醒设置 ===
        formPanel.add(createSectionLabel("--- 定时提醒设置 ---"), getGbc(row++, 0, 2));

        formPanel.add(new JLabel("检查间隔(分钟):"), getGbc(row, 0, 1));
        txtReminderInterval = new JTextField(20);
        txtReminderInterval.setText("30");
        formPanel.add(txtReminderInterval, getGbc(row++, 1, 1));

        // 到期合同检查间隔
        formPanel.add(new JLabel("到期检查间隔(小时):"), getGbc(row, 0, 1));
        txtExpiryInterval = new JTextField(20);
        txtExpiryInterval.setText("6");  // 默认6小时
        formPanel.add(txtExpiryInterval, getGbc(row++, 1, 1));

        // 保存全部设置按钮
        JPanel saveAllPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSaveAll = new JButton("💾 保存所有设置");
        btnSaveAll.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSaveAll.setBackground(new Color(46, 204, 113));
        btnSaveAll.setOpaque(true);
        btnSaveAll.setContentAreaFilled(true);
        btnSaveAll.setForeground(Color.BLACK);
        btnSaveAll.setFocusPainted(false);
        btnSaveAll.addActionListener(e -> saveAllSettings());
        saveAllPanel.add(btnSaveAll);
        formPanel.add(saveAllPanel, getGbc(row++, 0, 2));

        add(formPanel, BorderLayout.CENTER);
    }

    /**
     * 创建GridBagConstraints辅助方法
     *
     * @param row  行号
     * @param col  列号
     * @param span 列跨度
     * @return 配置好的GridBagConstraints对象
     */
    private GridBagConstraints getGbc(int row, int col, int span) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        if (span > 1) gbc.gridwidth = span;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    /**
     * 创建分组标题标签
     *
     * @param text 分组名称
     * @return 格式化的分组标签
     */
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 13));
        label.setForeground(new Color(44, 62, 80));
        return label;
    }

    /**
     * 浏览选择背景图片
     * <p>使用文件选择器让用户选择图片文件作为背景</p>
     */
    private void browseBackgroundImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择背景图片");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "图片文件 (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            txtBackgroundImage.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * 测试邮件发送功能
     * <p>使用当前填写的SMTP配置发送一封测试邮件到发件人自身</p>
     */
    private void testEmailSettings() {
        String host = txtSmtpHost.getText().trim();
        String portStr = txtSmtpPort.getText().trim();
        String email = txtSenderEmail.getText().trim();
        String password = new String(txtSenderPassword.getPassword());

        if (host.isEmpty() || portStr.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先填写完整的SMTP配置信息！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            // 先配置邮件服务
            EmailService.configure(host, port, email, password);
            // 发送测试邮件（发给发件人自己）
            boolean success = EmailService.sendTaskNotification(email, email, "TEST-001", "邮件测试合同", "测试");
            if (success) {
                JOptionPane.showMessageDialog(this, "测试邮件发送成功！请检查收件箱。", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "测试邮件发送失败，请检查配置信息。", "失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "SMTP端口必须是数字！", "格式错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 保存邮件设置到持久化存储和内存
     */
    private void saveEmailSettings() {
        AppSettingsUtil.saveSetting("smtp.host", txtSmtpHost.getText().trim());
        AppSettingsUtil.saveSetting("smtp.port", txtSmtpPort.getText().trim());
        AppSettingsUtil.saveSetting("smtp.email", txtSenderEmail.getText().trim());
        AppSettingsUtil.saveSetting("smtp.password", new String(txtSenderPassword.getPassword()));
        // 同时配置到EmailService
        try {
            EmailService.configure(
                txtSmtpHost.getText().trim(),
                Integer.parseInt(txtSmtpPort.getText().trim()),
                txtSenderEmail.getText().trim(),
                new String(txtSenderPassword.getPassword())
            );
        } catch (NumberFormatException ignored) { }
        JOptionPane.showMessageDialog(this, "邮件设置已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 测试AI服务连接
     * <p>调用AIAssistantService.isAvailable()检测Ollama是否可用</p>
     */
    private void testAIConnection() {
        // 先临时配置再测试
        AIAssistantService.configure(txtAiUrl.getText().trim(), txtAiModel.getText().trim());

        // 异步测试避免阻塞UI
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "正在测试AI连接...", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        new Thread(() -> {
            final boolean available = AIAssistantService.isAvailable();
            SwingUtilities.invokeLater(() -> {
                if (available) {
                    JOptionPane.showMessageDialog(SettingsPanel.this,
                        "✅ AI服务连接成功！\n\n服务地址: " + txtAiUrl.getText().trim()
                        + "\n模型: " + txtAiModel.getText().trim(),
                        "连接成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(SettingsPanel.this,
                        "❌ AI服务连接失败！\n\n请确认：\n"
                        + "1. Ollama是否已安装并运行?\n"
                        + "2. 是否已下载模型? (运行: ollama pull " + txtAiModel.getText().trim() + ")\n"
                        + "3. 服务地址是否正确? (当前: " + txtAiUrl.getText().trim() + ")",
                        "连接失败", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    /**
     * 保存AI设置到持久化存储和内存
     */
    private void saveAISettings() {
        AppSettingsUtil.saveSetting("ai.url", txtAiUrl.getText().trim());
        AppSettingsUtil.saveSetting("ai.model", txtAiModel.getText().trim());
        // 同时配置到AIAssistantService
        AIAssistantService.configure(txtAiUrl.getText().trim(), txtAiModel.getText().trim());
        JOptionPane.showMessageDialog(this, "AI设置已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 保存所有设置到持久化存储
     */
    private void saveAllSettings() {
        // 外观设置
        ThemeManager.ThemeType selectedTheme = (ThemeManager.ThemeType) cmbThemeColor.getSelectedItem();
        if (selectedTheme != null) AppSettingsUtil.saveSetting("theme", selectedTheme.name());
        AppSettingsUtil.saveSetting("theme.backgroundImage", txtBackgroundImage.getText().trim());
        AppSettingsUtil.saveSetting("sound.effect", (String) cmbSoundEffect.getSelectedItem());

        // 邮件设置
        AppSettingsUtil.saveSetting("smtp.host", txtSmtpHost.getText().trim());
        AppSettingsUtil.saveSetting("smtp.port", txtSmtpPort.getText().trim());
        AppSettingsUtil.saveSetting("smtp.email", txtSenderEmail.getText().trim());
        AppSettingsUtil.saveSetting("smtp.password", new String(txtSenderPassword.getPassword()));

        // AI设置
        AppSettingsUtil.saveSetting("ai.url", txtAiUrl.getText().trim());
        AppSettingsUtil.saveSetting("ai.model", txtAiModel.getText().trim());

        // 提醒设置
        AppSettingsUtil.saveSetting("reminder.interval", txtReminderInterval.getText().trim());
        AppSettingsUtil.saveSetting("expiry.interval", txtExpiryInterval.getText().trim());

        // 同时应用到各服务
        try {
            EmailService.configure(
                txtSmtpHost.getText().trim(),
                Integer.parseInt(txtSmtpPort.getText().trim()),
                txtSenderEmail.getText().trim(),
                new String(txtSenderPassword.getPassword())
            );
        } catch (NumberFormatException ignored) { }
        AIAssistantService.configure(txtAiUrl.getText().trim(), txtAiModel.getText().trim());

        JOptionPane.showMessageDialog(this, "✅ 所有设置已保存！\n\n设置文件位置: "
            + AppSettingsUtil.getSettingsFilePath(), "保存成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 从持久化存储加载设置到界面
     */
    private void loadSettings() {
        // 加载外观设置（使用ThemeManager枚举）
        String savedTheme = AppSettingsUtil.loadSetting("theme", "LIGHT");
        try {
            ThemeManager.ThemeType theme = ThemeManager.ThemeType.valueOf(savedTheme);
            cmbThemeColor.setSelectedItem(theme);
        } catch (Exception ignored) {
            cmbThemeColor.setSelectedItem(ThemeManager.ThemeType.LIGHT);
        }
        txtBackgroundImage.setText(AppSettingsUtil.loadSetting("theme.backgroundImage", ""));
        cmbSoundEffect.setSelectedItem(AppSettingsUtil.loadSetting("sound.effect", "系统默认"));

        // 加载邮件设置
        txtSmtpHost.setText(AppSettingsUtil.loadSetting("smtp.host", "smtp.qq.com"));
        txtSmtpPort.setText(AppSettingsUtil.loadSetting("smtp.port", "465"));
        txtSenderEmail.setText(AppSettingsUtil.loadSetting("smtp.email", ""));
        txtSenderPassword.setText(AppSettingsUtil.loadSetting("smtp.password", ""));

        // 加载AI设置
        txtAiUrl.setText(AppSettingsUtil.loadSetting("ai.url", "http://localhost:11434"));
        txtAiModel.setText(AppSettingsUtil.loadSetting("ai.model", "qwen2.5"));

        // 加载提醒设置
        txtReminderInterval.setText(AppSettingsUtil.loadSetting("reminder.interval", "30"));
        txtExpiryInterval.setText(AppSettingsUtil.loadSetting("expiry.interval", "6"));
    }
}
