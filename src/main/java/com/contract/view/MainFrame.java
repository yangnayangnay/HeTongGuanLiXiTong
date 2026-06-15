package com.contract.view;

import com.contract.entity.User;
import com.contract.service.UserService;
import com.contract.util.NotificationService;
import com.contract.util.TaskReminderScheduler;
import com.contract.util.HotKeyManager;
import com.contract.util.ContractExpiryReminder;
import com.contract.util.ThemeManager;
import com.contract.util.FileLogger;
import com.contract.util.AppSettingsUtil;
import com.contract.util.SoundUtil;
import com.contract.util.I18NUtil;
import com.contract.view.panel.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * 主窗口（MainFrame）
 * <p>
 * 系统的主界面框架，采用左右布局：
 * 左侧为导航菜单，右侧为内容展示区域。
 * 通过导航菜单切换不同的功能面板。
 * </p>
 *
 * <h3>布局结构：</h3>
 * <pre>
 * +--------------------------------------------------+
 * | 导航栏 |           内容区域                  |
 * |        |  （各功能面板动态切换）              |
 * | 用户信息|                                    |
 * | 功能菜单|                                    |
 * | 退出按钮|                                    |
 * +--------+--------------------------------------+
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class MainFrame extends JFrame {
    /** 当前登录用户 */
    private User currentUser;
    /** 右侧内容面板容器 */
    private JPanel contentPanel;
    /** 用户服务对象 */
    private UserService userService = new UserService();
    /** 左侧导航面板（用于响应式布局控制） */
    private JPanel navPanel;
    /** 顶部导航栏（小屏幕时显示，替代左侧导航） */
    private JPanel topNavBar;

    /**
     * 构造方法：初始化主窗口
     *
     * @param user 当前登录的用户对象
     */
    public MainFrame(User user) {
        this.currentUser = user;
        FileLogger.info("MainFrame", "constructor", "主窗口初始化: user=" + user.getName());
        setTitle("📋 合同管理系统 v2.0 - 当前用户: " + user.getName());
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();

        // 加载保存的主题并注册主题变更监听器
        ThemeManager.loadSavedTheme();
        ThemeManager.addChangeListener(() -> {
            SwingUtilities.invokeLater(() -> {
                ThemeManager.applyTo(MainFrame.this);
                // 刷新左侧导航面板和内容区域的背景色
                navPanel.setBackground(ThemeManager.getCurrentTheme().bgSecondary);
                contentPanel.setBackground(ThemeManager.getCurrentTheme().bgPrimary);
                repaint();
            });
        });

        // 延迟500ms后检查待办任务并弹窗提示（等待主窗口完全显示后再弹窗）
        SwingUtilities.invokeLater(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            checkAndShowPendingTasks();
            // 启动定时任务提醒服务（每30分钟检查一次待办任务并发送邮件提醒）
            TaskReminderScheduler.start(currentUser.getName());
            // 启动合同到期自动提醒（每6小时扫描一次）
            ContractExpiryReminder.start();
        });

        // 窗口关闭时停止定时任务调度器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                TaskReminderScheduler.stop();  // 停止定时提醒服务
                ContractExpiryReminder.stop(); // 停止到期提醒服务
            }
        });
    }

    /**
     * 初始化主界面布局
     */
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 左侧导航面板
        navPanel = createNavPanel();
        mainPanel.add(navPanel, BorderLayout.WEST);

        // 右侧内容面板（动态切换各功能面板）
        // 加载并应用背景图片
        String bgImagePath = AppSettingsUtil.loadSetting("backgroundImage", "");
        if (!bgImagePath.isEmpty()) {
            try {
                java.io.File bgFile = new java.io.File(bgImagePath);
                if (bgFile.exists()) {
                    final javax.swing.ImageIcon bgIcon = new javax.swing.ImageIcon(bgImagePath);
                    // 设置内容面板的背景图片
                    contentPanel = new JPanel(new BorderLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                        }
                    };
                } else {
                    contentPanel = new JPanel(new BorderLayout());
                }
            } catch (Exception e) {
                FileLogger.warn("MainFrame", "initUI", "背景图片加载失败: " + e.getMessage());
                contentPanel = new JPanel(new BorderLayout());
            }
        } else {
            contentPanel = new JPanel(new BorderLayout());
        }
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 顶部导航栏（小屏幕时显示，替代左侧导航）
        topNavBar = com.contract.util.ResponsiveLayoutUtil.createTopNavBar(e -> switchPanel(e.getActionCommand()));
        topNavBar.setVisible(false);  // 默认隐藏，小屏幕时自动显示
        mainPanel.add(topNavBar, BorderLayout.NORTH);

        // 默认显示欢迎页面
        showWelcomePanel();

        add(mainPanel);

        // 初始化全局快捷键管理器
        HotKeyManager.init(getRootPane());
        // 设置面板切换回调，将快捷键命令转发给switchPanel方法
        HotKeyManager.setPanelSwitcher((cmd) -> switchPanel(cmd));

        // 启用响应式布局（自动适应不同屏幕尺寸）
        com.contract.util.ResponsiveLayoutUtil.applyResponsive(this, navPanel, contentPanel, topNavBar);
    }

    /**
     * 创建左侧导航面板
     * <p>包含用户信息、功能菜单、退出按钮</p>
     *
     * @return 导航面板
     */
    private JPanel createNavPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setPreferredSize(new Dimension(200, 0));  // 固定宽度200px
        navPanel.setBackground(new Color(44, 62, 80));   // 深蓝灰色背景
        navPanel.setLayout(new BorderLayout());

        // ===== 用户信息区域（顶部）=====
        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(52, 73, 94));  // 稍浅的蓝灰色
        userPanel.setLayout(new BorderLayout());
        userPanel.setBorder(new EmptyBorder(15, 10, 15, 10));

        // 用户头像区域（圆形效果）
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarPanel.setOpaque(false);
        String initial = currentUser.getName().substring(0, 1).toUpperCase();
        JLabel lblAvatar = new JLabel(initial, SwingConstants.CENTER);
        lblAvatar.setFont(new Font("微软雅黑", Font.BOLD, 22));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(new Color(52, 152, 219));  // 亮蓝色圆形背景
        lblAvatar.setPreferredSize(new Dimension(45, 45));
        // 圆形边框效果
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2, true));
        avatarPanel.add(lblAvatar);

        // 用户名和角色信息
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setOpaque(false);
        JLabel lblUser = new JLabel("  " + currentUser.getName());
        lblUser.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblUser.setForeground(Color.BLACK);
        userPanel.add(lblUser, BorderLayout.CENTER);

        // 待办任务数量徽章（红色圆圈+数字）
        int pendingCount = NotificationService.getPendingTaskCount(currentUser.getName());
        if (pendingCount > 0) {
            JLabel lblBadge = createPendingBadge(pendingCount);
            userPanel.add(lblBadge, BorderLayout.EAST);
        }

        // 显示用户角色（管理员/普通用户）
        boolean isAdmin = userService.isAdmin(currentUser.getName());
        JLabel lblRole = new JLabel("  " + (isAdmin ? "👑 管理员" : "👤 普通用户"));
        lblRole.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        lblRole.setForeground(new Color(189, 195, 199));
        userInfoPanel.add(lblRole, BorderLayout.SOUTH);

        userPanel.add(avatarPanel, BorderLayout.WEST);
        userPanel.add(userInfoPanel, BorderLayout.CENTER);

        navPanel.add(userPanel, BorderLayout.NORTH);

        // ===== 功能导航菜单（中间）=====
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));  // 垂直排列
        btnPanel.setBackground(new Color(44, 62, 80));
        btnPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // 获取当前用户的权限功能列表
        Set<String> functions = userService.getUserFunctions(currentUser.getName());

        // 合同管理分组
        btnPanel.add(createSectionLabel("📄 " + I18NUtil.getString("contract.name")));
        if (functions.contains("F01")) btnPanel.add(createNavButton("📝 " + I18NUtil.getString("nav.draft"), "draft"));       // F01:起草权限
        if (functions.contains("F02")) btnPanel.add(createNavButton("✍️ " + I18NUtil.getString("nav.countersign"), "countersign")); // F02:会签权限
        if (functions.contains("F03")) btnPanel.add(createNavButton("📋 " + I18NUtil.getString("nav.finalize"), "finalize"));    // F03:定稿权限
        if (functions.contains("F04")) btnPanel.add(createNavButton("✅ " + I18NUtil.getString("nav.approve"), "approve"));     // F04:审批权限
        if (functions.contains("F05")) btnPanel.add(createNavButton("📝 " + I18NUtil.getString("nav.sign"), "sign"));        // F05:签订权限

        // 查询统计分组
        btnPanel.add(createSectionLabel("📊 查询统计"));
        if (functions.contains("F07")) btnPanel.add(createNavButton("🔍 " + I18NUtil.getString("nav.query"), "query"));         // F07:查询权限(含流程历史)
        if (functions.contains("F07")) btnPanel.add(createNavButton("📋 " + I18NUtil.getString("nav.kanban"), "kanban"));         // F07:流程看板权限
        if (functions.contains("F07")) btnPanel.add(createNavButton("📊 " + I18NUtil.getString("nav.statistics"), "statistics"));      // F07:数据统计权限
        btnPanel.add(createNavButton("🔔 " + I18NUtil.getString("nav.pendingTasks"), "pendingTasks"));                             // 待办任务（所有人可见）

        // 基础数据管理分组
        btnPanel.add(createSectionLabel("📁 基础数据管理"));
        if (functions.contains("F09")) btnPanel.add(createNavButton("👥 " + I18NUtil.getString("nav.customer"), "customer"));      // F09:客户管理权限

        // 系统管理分组（仅管理员可见）
        if (isAdmin) {
            btnPanel.add(createSectionLabel("⚙️ 系统管理"));
            if (functions.contains("F06")) btnPanel.add(createNavButton("👥 " + I18NUtil.getString("nav.assign"), "assign"));      // F06:分配权限
            if (functions.contains("F10")) btnPanel.add(createNavButton("👤 " + I18NUtil.getString("nav.userManage"), "userManage"));  // F10:用户管理权限
            if (functions.contains("F11")) btnPanel.add(createNavButton("🔐 " + I18NUtil.getString("nav.roleManage"), "roleManage"));  // F11:角色管理权限
            if (functions.contains("F12")) btnPanel.add(createNavButton("📋 " + I18NUtil.getString("nav.logManage"), "logManage"));   // F12:日志管理权限
        }

        // 将菜单放入滚动条，防止菜单过长时溢出
        JScrollPane scrollPane = new JScrollPane(btnPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navPanel.add(scrollPane, BorderLayout.CENTER);

        // ===== 系统设置按钮（所有人可见）=====
        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBackground(new Color(44, 62, 80));
        settingsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JButton btnSettings = createNavButton("⚙️ " + I18NUtil.getString("nav.settings"), "settings");
        settingsPanel.add(btnSettings, BorderLayout.CENTER);
        navPanel.add(settingsPanel, BorderLayout.SOUTH);

        // ===== 退出按钮（底部）=====
        // 关于系统按钮（所有人可见，不需要权限）
        JButton btnAbout = new JButton("ℹ️ " + I18NUtil.getString("nav.about"));
        btnAbout.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnAbout.setBackground(new Color(52, 73, 94));   // 灰色背景
        btnAbout.setOpaque(true);
        btnAbout.setContentAreaFilled(true);
        btnAbout.setForeground(new Color(189, 195, 199));
        btnAbout.setFocusPainted(false);
        btnAbout.setBorderPainted(false);
        btnAbout.addActionListener(e -> switchPanel("about"));

        JButton btnLogout = new JButton("🚪 " + I18NUtil.getString("nav.logout"));
        btnLogout.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnLogout.setBackground(new Color(192, 57, 43));  // 红色退出按钮
        btnLogout.setOpaque(true);
        btnLogout.setContentAreaFilled(true);
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 二次确认后退出
                int confirm = JOptionPane.showConfirmDialog(MainFrame.this, "确定要退出登录吗？", "确认", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    FileLogger.info("MainFrame", "logout", "用户退出登录: user=" + currentUser.getName());
                    MainFrame.this.dispose();  // 关闭主窗口
                    new LoginFrame().setVisible(true);  // 返回登录窗口
                }
            }
        });
        JPanel logoutPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        logoutPanel.setBackground(new Color(44, 62, 80));
        logoutPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        logoutPanel.add(btnAbout);
        logoutPanel.add(btnLogout);
        navPanel.add(logoutPanel, BorderLayout.SOUTH);

        return navPanel;
    }

    /**
     * 创建分组标题标签
     *
     * @param text 分组名称
     * @return 格式化的标签
     */
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 12));
        label.setForeground(new Color(149, 165, 166));  // 灰色文字
        label.setBorder(new EmptyBorder(10, 0, 5, 0));
        return label;
    }

    /**
     * 创建导航按钮
     * <p>带鼠标悬停效果的功能按钮</p>
     *
     * @param text    按钮显示文字
     * @param command 命令标识（用于面板切换）
     * @return 格式化的按钮
     */
    private JButton createNavButton(String text, String command) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btn.setBackground(new Color(44, 62, 80));  // 默认深色背景
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setForeground(Color.WHITE);  // 白色文字
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 25, 8, 10));
        btn.setActionCommand(command);  // 设置命令标识
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(e.getActionCommand());  // 点击时切换面板
            }
        });
        // 鼠标悬停效果：高亮显示
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 152, 219));  // 悬停时变亮蓝色
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(44, 62, 80));  // 离开恢复原色
            }
        });
        return btn;
    }

    /**
     * 创建待办任务数量徽章
     * <p>红色圆圈背景+白色数字，用于在用户信息区域显示待办数量</p>
     *
     * @param count 待办任务数量
     * @return 格式化的徽章标签
     */
    private JLabel createPendingBadge(int count) {
        JLabel badge = new JLabel(String.valueOf(count), SwingConstants.CENTER);
        badge.setFont(new Font("微软雅黑", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);                    // 白色文字
        badge.setOpaque(true);
        badge.setBackground(new Color(231, 76, 60));        // 红色背景 #E74C3C
        badge.setPreferredSize(new Dimension(22, 22));       // 固定尺寸圆形
        // 圆形边框效果
        badge.setBorder(BorderFactory.createLineBorder(new Color(192, 57, 43), 1, true));
        return badge;
    }

    /**
     * 根据命令标识切换右侧内容面板
     * <p>清除旧面板，创建并添加新的功能面板</p>
     *
     * @param command 命令标识
     */
    private void switchPanel(String command) {
        FileLogger.info("MainFrame", "switchPanel", "切换到面板: " + command);
        contentPanel.removeAll();  // 清除现有内容
        switch (command) {
            case "draft":
                contentPanel.add(new ContractDraftPanel(currentUser), BorderLayout.CENTER);
                break;
            case "countersign":
                contentPanel.add(new ContractCountersignPanel(currentUser), BorderLayout.CENTER);
                break;
            case "finalize":
                contentPanel.add(new ContractFinalizePanel(currentUser), BorderLayout.CENTER);
                break;
            case "approve":
                contentPanel.add(new ContractApprovePanel(currentUser), BorderLayout.CENTER);
                break;
            case "sign":
                contentPanel.add(new ContractSignPanel(currentUser), BorderLayout.CENTER);
                break;
            case "assign":
                contentPanel.add(new ContractAssignPanel(), BorderLayout.CENTER);
                break;
            case "query":
                contentPanel.add(new ContractQueryPanel(currentUser), BorderLayout.CENTER);
                break;
            case "statistics":
                contentPanel.add(new StatisticsPanel(currentUser), BorderLayout.CENTER);
                break;
            case "kanban":
                contentPanel.add(new KanbanBoardPanel(), BorderLayout.CENTER);
                break;
            case "customer":
                contentPanel.add(new CustomerManagePanel(), BorderLayout.CENTER);
                break;
            case "userManage":
                contentPanel.add(new UserManagePanel(currentUser), BorderLayout.CENTER);
                break;
            case "roleManage":
                contentPanel.add(new RoleManagePanel(), BorderLayout.CENTER);
                break;
            case "logManage":
                contentPanel.add(new LogPanel(), BorderLayout.CENTER);
                break;
            case "settings":
                contentPanel.add(new SettingsPanel(), BorderLayout.CENTER);
                break;
            case "pendingTasks":
                contentPanel.add(new PendingTaskPanel(currentUser), BorderLayout.CENTER);
                break;
            case "about":
                contentPanel.add(new AboutPanel(), BorderLayout.CENTER);
                break;
            default:
                showWelcomePanel();
                return;
        }
        contentPanel.revalidate();  // 重新布局
        contentPanel.repaint();      // 重绘
    }

    /**
     * 显示欢迎面板
     * <p>初始加载或无匹配命令时显示</p>
     */
    private void showWelcomePanel() {
        contentPanel.removeAll();

        // 自定义渐变背景面板
        JPanel welcomePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                // 渐变背景：从左上到右下
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 248, 255),   // 浅蓝白
                    getWidth(), getHeight(), new Color(230, 240, 250)  // 灰蓝
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        welcomePanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);

        // 主标题
        gbc.gridy = 0;
        JLabel lblWelcome = new JLabel("🏢 欢迎使用合同管理系统 v2.0");
        lblWelcome.setFont(new Font("微软雅黑", Font.BOLD, 32));
        lblWelcome.setForeground(new Color(44, 62, 80));  // #2C3E50 深蓝灰
        welcomePanel.add(lblWelcome, gbc);

        // 副标题
        gbc.gridy = 1;
        JLabel lblSubtitle = new JLabel("Enterprise Contract Management System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblSubtitle.setForeground(new Color(52, 152, 219));  // #3498DB 亮蓝
        welcomePanel.add(lblSubtitle, gbc);

        // 分隔线
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 100, 15, 100);
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(189, 195, 199));
        separator.setPreferredSize(new Dimension(400, 1));
        welcomePanel.add(separator, gbc);

        // 当前用户信息区
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridy = 3;
        JLabel lblUser = new JLabel("👤 当前用户: " + currentUser.getName());
        lblUser.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        lblUser.setForeground(new Color(52, 73, 94));
        welcomePanel.add(lblUser, gbc);

        // 当前时间
        gbc.gridy = 4;
        String currentTime = java.text.SimpleDateFormat.getDateInstance(java.text.SimpleDateFormat.FULL, java.util.Locale.CHINA)
            .format(new java.util.Date()) + " " +
            new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        JLabel lblTime = new JLabel("🕐 当前时间: " + currentTime);
        lblTime.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblTime.setForeground(new Color(127, 140, 141));
        welcomePanel.add(lblTime, gbc);

        // 用户角色
        gbc.gridy = 5;
        boolean isAdmin = userService.isAdmin(currentUser.getName());
        JLabel lblRoleInfo = new JLabel("🔐 用户角色: " + (isAdmin ? "系统管理员（拥有全部功能权限）" : "普通用户（受限功能权限）"));
        lblRoleInfo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblRoleInfo.setForeground(isAdmin ? new Color(39, 174, 96) : new Color(243, 156, 18));
        welcomePanel.add(lblRoleInfo, gbc);

        // 功能提示
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 0, 8, 0);
        JLabel lblTip = new JLabel("💡 请从左侧导航菜单选择功能模块开始操作");
        lblTip.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        lblTip.setForeground(new Color(149, 165, 166));
        welcomePanel.add(lblTip, gbc);

        // 版本信息
        gbc.gridy = 7;
        JLabel lblVersion = new JLabel("📌 版本: v2.0 | 架构: C/S (Java Swing + Oracle) | 含16项扩展创新功能");
        lblVersion.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        lblVersion.setForeground(new Color(189, 195, 199));
        welcomePanel.add(lblVersion, gbc);

        contentPanel.add(welcomePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * 检查并显示待办任务通知
     * <p>
     * 在用户登录成功后、主窗口完全加载后调用此方法。
     * 通过NotificationService查询当前用户的待办任务数量，
     * 如果有待办任务则弹出提示对话框告知用户。
     * </p>
     *
     * <p>异常处理：通知检查失败不影响系统的正常使用，
     * 仅在控制台输出错误日志。</p>
     */
    private void checkAndShowPendingTasks() {
        try {
            // 查询当前登录用户的待办任务数量
            int pendingCount = NotificationService.getPendingTaskCount(currentUser.getName());
            if (pendingCount > 0) {
                // 播放提示音
                SoundUtil.playNotificationSound();
                // 有待办任务时弹窗提示用户
                JOptionPane.showMessageDialog(this,
                    "您有 " + pendingCount + " 个待处理的合同任务！\n\n" +
                    "请在左侧菜单中选择对应的功能模块查看和处理。",
                    "待办任务提醒",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            // 通知检查失败不影响正常使用，仅记录错误日志
            FileLogger.error("MainFrame", "checkAndShowPendingTasks", "待办任务检查异常: " + e.getMessage(), e);
            System.err.println("[通知] 待办任务检查异常: " + e.getMessage());
        }
    }
}
