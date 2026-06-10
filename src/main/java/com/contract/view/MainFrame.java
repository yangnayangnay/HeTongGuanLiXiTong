package com.contract.view;

import com.contract.entity.User;
import com.contract.service.UserService;
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

    /**
     * 构造方法：初始化主窗口
     *
     * @param user 当前登录的用户对象
     */
    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("合同管理系统 - 当前用户: " + user.getName());
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    /**
     * 初始化主界面布局
     */
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 左侧导航面板
        JPanel navPanel = createNavPanel();
        mainPanel.add(navPanel, BorderLayout.WEST);

        // 右侧内容面板（动态切换各功能面板）
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 默认显示欢迎页面
        showWelcomePanel();

        add(mainPanel);
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

        JLabel lblUser = new JLabel("  " + currentUser.getName());
        lblUser.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblUser.setForeground(Color.BLACK);
        userPanel.add(lblUser, BorderLayout.CENTER);

        // 显示用户角色（管理员/普通用户）
        boolean isAdmin = userService.isAdmin(currentUser.getName());
        JLabel lblRole = new JLabel("  " + (isAdmin ? "管理员" : "普通用户"));
        lblRole.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        lblRole.setForeground(new Color(189, 195, 199));
        userPanel.add(lblRole, BorderLayout.SOUTH);

        navPanel.add(userPanel, BorderLayout.NORTH);

        // ===== 功能导航菜单（中间）=====
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));  // 垂直排列
        btnPanel.setBackground(new Color(44, 62, 80));
        btnPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // 获取当前用户的权限功能列表
        Set<String> functions = userService.getUserFunctions(currentUser.getName());

        // 合同管理分组
        btnPanel.add(createSectionLabel("合同管理"));
        if (functions.contains("F01")) btnPanel.add(createNavButton("起草合同", "draft"));       // F01:起草权限
        if (functions.contains("F02")) btnPanel.add(createNavButton("会签合同", "countersign")); // F02:会签权限
        if (functions.contains("F03")) btnPanel.add(createNavButton("定稿合同", "finalize"));    // F03:定稿权限
        if (functions.contains("F04")) btnPanel.add(createNavButton("审批合同", "approve"));     // F04:审批权限
        if (functions.contains("F05")) btnPanel.add(createNavButton("签订合同", "sign"));        // F05:签订权限

        // 查询统计分组
        btnPanel.add(createSectionLabel("查询统计"));
        if (functions.contains("F07")) btnPanel.add(createNavButton("合同查询", "query"));         // F07:查询权限(含流程历史)

        // 基础数据管理分组
        btnPanel.add(createSectionLabel("基础数据管理"));
        if (functions.contains("F09")) btnPanel.add(createNavButton("客户管理", "customer"));      // F09:客户管理权限

        // 系统管理分组（仅管理员可见）
        if (isAdmin) {
            btnPanel.add(createSectionLabel("系统管理"));
            if (functions.contains("F06")) btnPanel.add(createNavButton("分配合同", "assign"));      // F06:分配权限
            if (functions.contains("F10")) btnPanel.add(createNavButton("用户管理", "userManage"));  // F10:用户管理权限
            if (functions.contains("F11")) btnPanel.add(createNavButton("角色管理", "roleManage"));  // F11:角色管理权限
            if (functions.contains("F12")) btnPanel.add(createNavButton("日志管理", "logManage"));   // F12:日志管理权限
        }

        // 将菜单放入滚动条，防止菜单过长时溢出
        JScrollPane scrollPane = new JScrollPane(btnPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navPanel.add(scrollPane, BorderLayout.CENTER);

        // ===== 退出按钮（底部）=====
        JButton btnLogout = new JButton("退出登录");
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
                    MainFrame.this.dispose();  // 关闭主窗口
                    new LoginFrame().setVisible(true);  // 返回登录窗口
                }
            }
        });
        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setBackground(new Color(44, 62, 80));
        logoutPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        logoutPanel.add(btnLogout, BorderLayout.CENTER);
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
     * 根据命令标识切换右侧内容面板
     * <p>清除旧面板，创建并添加新的功能面板</p>
     *
     * @param command 命令标识
     */
    private void switchPanel(String command) {
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
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblWelcome = new JLabel("欢迎使用合同管理系统");
        lblWelcome.setFont(new Font("微软雅黑", Font.BOLD, 28));
        lblWelcome.setForeground(new Color(51, 51, 102));
        welcomePanel.add(lblWelcome, gbc);
        gbc.gridy = 1;
        JLabel lblUser = new JLabel("当前用户: " + currentUser.getName());
        lblUser.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        lblUser.setForeground(Color.GRAY);
        welcomePanel.add(lblUser, gbc);
        contentPanel.add(welcomePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
