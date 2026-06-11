package com.contract.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * 响应式布局工具类
 * <p>根据窗口大小动态调整UI布局，优化不同屏幕尺寸下的显示效果</p>
 *
 * <h3>功能特点：</h3>
 * <ul>
 *   <li>自动检测屏幕尺寸级别（小屏/中屏/大屏）</li>
 *   <li>小屏幕时隐藏左侧导航，显示顶部快捷导航栏</li>
 *   <li>中等屏幕缩小导航宽度</li>
 *   <li>大屏幕保持正常布局</li>
 *   <li>触摸设备优化字体大小</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 2.0
 * @since 2026-01-01
 */
public class ResponsiveLayoutUtil {

    /** 小屏幕宽度阈值（移动端/平板） */
    public static final int SMALL_SCREEN_WIDTH = 800;
    /** 中等屏幕宽度阈值（笔记本） */
    public static final int MEDIUM_SCREEN_WIDTH = 1200;

    /**
     * 屏幕尺寸级别枚举
     */
    public enum ScreenSizeLevel {
        /** 小屏幕：移动端或平板设备 */
        SMALL("小屏/移动端"),
        /** 中等屏幕：笔记本电脑等 */
        MEDIUM("中等屏幕"),
        /** 大屏幕：桌面显示器 */
        LARGE("大屏/桌面");

        ScreenSizeLevel(String desc) { this.desc = desc; }
        final String desc;  // 级别描述
    }

    /**
     * 获取当前容器的屏幕尺寸级别
     *
     * @param container 要检测的容器组件
     * @return 屏幕尺寸级别枚举值
     */
    public static ScreenSizeLevel getScreenSizeLevel(Container container) {
        Dimension size = container.getSize();
        if (size.width <= SMALL_SCREEN_WIDTH) return ScreenSizeLevel.SMALL;
        if (size.width <= MEDIUM_SCREEN_WIDTH) return ScreenSizeLevel.MEDIUM;
        return ScreenSizeLevel.LARGE;
    }

    /**
     * 应用响应式布局到主框架
     * <p>
     * 监听窗口大小变化事件，根据当前窗口宽度自动调整布局：
     * <ul>
     *   <li>小屏幕(≤800px)：隐藏左侧导航，显示顶部Tab导航栏</li>
     *   <li>中等屏幕(≤1200px)：显示左侧导航但缩小宽度至150px</li>
     *   <li>大屏幕(&gt;1200px)：正常布局，左侧导航宽度200px</li>
     * </ul>
     * </p>
     *
     * @param mainFrame   主窗口对象(JFrame)
     * @param leftPanel   左侧导航面板
     * @param contentPanel 右侧内容面板
     * @param topNavBar   顶部导航栏（小屏幕时显示）
     */
    public static void applyResponsive(JFrame mainFrame, JPanel leftPanel, JPanel contentPanel, JPanel topNavBar) {
        // 添加窗口大小变化监听器
        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 获取当前屏幕尺寸级别
                ScreenSizeLevel level = getScreenSizeLevel(mainFrame);

                switch (level) {
                    case SMALL:
                        // 小屏幕模式：隐藏左侧导航，改为顶部Tab栏
                        leftPanel.setVisible(false);
                        if (topNavBar != null) topNavBar.setVisible(true);
                        break;
                    case MEDIUM:
                        // 中等屏幕模式：缩小左侧导航宽度
                        leftPanel.setVisible(true);
                        leftPanel.setPreferredSize(new Dimension(150, 0));
                        if (topNavBar != null) topNavBar.setVisible(false);
                        break;
                    case LARGE:
                        // 大屏幕模式：恢复正常布局
                        leftPanel.setVisible(true);
                        leftPanel.setPreferredSize(new Dimension(200, 0));
                        if (topNavBar != null) topNavBar.setVisible(false);
                        break;
                }

                // 重新验证布局并重绘
                mainFrame.revalidate();
                mainFrame.repaint();
            }
        });
    }

    /**
     * 为触摸设备优化的字体大小
     * <p>
     * 根据屏幕尺寸调整基础字号：
     * - 小屏幕：增大15%（便于触摸操作）
     * - 中等屏幕：增大5%
     * - 大屏幕：保持原样
     * </p>
     *
     * @param baseSize 基础字号（像素）
     * @param level    当前屏幕尺寸级别
     * @return 调整后的优化字号
     */
    public static float getOptimizedFontSize(float baseSize, ScreenSizeLevel level) {
        switch (level) {
            case SMALL: return baseSize * 1.15f;  // 触摸设备字号稍大
            case MEDIUM: return baseSize * 1.05f;
            default: return baseSize;
        }
    }

    /**
     * 创建顶部快速导航栏（用于小屏幕替代左侧导航）
     * <p>
     * 当窗口宽度小于800px时，左侧导航会被隐藏，
     * 此时在顶部显示一个包含核心功能的快捷按钮栏。
     * </p>
     *
     * @param actionHandler 按钮点击事件处理器（接收command参数）
     * @return 配置好的顶部导航栏JPanel
     */
    public static JPanel createTopNavBar(java.awt.event.ActionListener actionHandler) {
        JPanel topNavBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topNavBar.setBackground(new Color(52, 73, 94));  // 深蓝灰色背景
        topNavBar.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // 定义核心功能的快速访问按钮
        String[][] quickActions = {
            {"📝 起草", "contractDraft"},
            {"✍️ 会签", "countersign"},
            {"✅ 审批", "approve"},
            {"📝 签订", "sign"},
            {"🔍 查询", "contractQuery"}
        };

        // 创建每个快捷按钮
        for (String[] action : quickActions) {
            JButton btn = new JButton(action[0]);
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(44, 62, 80));  // 默认深色背景
            btn.setOpaque(true);
            btn.setForeground(Color.WHITE);  // 白色文字
            btn.setBorderPainted(false);
            btn.setActionCommand(action[1]);  // 设置命令标识
            btn.addActionListener(actionHandler);  // 绑定事件处理器

            // 鼠标悬停效果
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(52, 152, 219));  // 悬停变蓝
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(44, 62, 80));  // 离开恢复
                }
            });

            topNavBar.add(btn);
        }

        return topNavBar;
    }
}
