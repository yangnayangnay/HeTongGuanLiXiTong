package com.contract.util;

import javax.swing.*;
import java.awt.*;

/**
 * 主题管理器
 * <p>管理系统全局外观主题，支持深色/浅色模式切换</p>
 *
 * <h3>支持的主题：</h3>
 * <ul>
 *   <li>浅色主题：白色背景，深色文字</li>
 *   <li>深色主题：深灰背景，浅色文字</li>
 *   <li>蓝色主题：淡蓝背景，蓝色强调</li>
 *   <li>绿色主题：淡绿背景，绿色强调</li>
 * </ul>
 */
public class ThemeManager {

    /**
     * 当前主题类型
     */
    public enum ThemeType {
        /** 浅色主题（默认） */
        LIGHT("浅色主题", Color.WHITE, new Color(245, 245, 250),
              new Color(44, 62, 80), new Color(52, 152, 219)),
        /** 深色主题 */
        DARK("深色主题", new Color(30, 30, 35), new Color(45, 45, 50),
             new Color(236, 240, 241), new Color(41, 128, 185)),
        /** 蓝色主题 */
        BLUE("蓝色主题", new Color(232, 244, 253), new Color(219, 234, 254),
             new Color(30, 64, 175), new Color(37, 99, 235)),
        /** 绿色主题 */
        GREEN("绿色主题", new Color(240, 253, 244), new Color(220, 252, 231),
             new Color(21, 128, 61), new Color(34, 197, 94));

        public final String displayName;   // 显示名称
        public final Color bgPrimary;      // 主背景色
        public final Color bgSecondary;    // 次背景色（面板背景）
        public final Color textPrimary;    // 主文字颜色
        public final Color accentColor;    // 强调色（按钮等）

        ThemeType(String display, Color bg1, Color bg2, Color text, Color accent) {
            this.displayName = display;
            this.bgPrimary = bg1;
            this.bgSecondary = bg2;
            this.textPrimary = text;
            this.accentColor = accent;
        }

        @Override
        public String toString() { return displayName; }
    }

    /** 当前使用的主题 */
    private static ThemeType currentTheme = ThemeType.LIGHT;
    /** 主题变更监听器列表 */
    private static java.util.List<Runnable> changeListeners = new java.util.ArrayList<>();

    /**
     * 获取当前主题
     * @return 当前主题类型枚举
     */
    public static ThemeType getCurrentTheme() { return currentTheme; }

    /**
     * 切换主题
     * <p>设置新主题并通知所有注册的监听器刷新界面</p>
     *
     * @param theme 要切换的目标主题
     */
    public static void setTheme(ThemeType theme) {
        currentTheme = theme;
        AppSettingsUtil.saveSetting("theme", theme.name());
        // 通知所有注册的组件刷新
        for (Runnable r : changeListeners) { try { r.run(); } catch (Exception ignored) {} }
        System.out.println("[主题] 已切换至: " + theme.displayName);
    }

    /**
     * 注册主题变更监听器
     * <p>当主题变更时，所有注册的监听器会被调用以刷新界面</p>
     *
     * @param listener 监听器回调（无参数无返回值）
     */
    public static void addChangeListener(Runnable listener) { changeListeners.add(listener); }

    /**
     * 应用主题到指定组件及其子组件
     * <p>递归遍历组件树，根据当前主题设置各组件的颜色</p>
     *
     * @param comp 要应用主题的根组件
     */
    public static void applyTo(Component comp) {
        applyRecursive(comp, 0);
    }

    /**
     * 递归应用主题到组件树
     *
     * @param comp 当前组件
     * @param depth 当前深度（用于限制递归深度）
     */
    private static void applyRecursive(Component comp, int depth) {
        if (comp == null) return;
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyRecursive(child, depth + 1);
            }
        }
        // 只应用到一定深度，避免影响内部弹窗等
        if (depth > 10) return;

        if (comp instanceof JPanel && !(comp instanceof JSplitPane)) {
            comp.setBackground(currentTheme.bgSecondary);
        } else if (comp instanceof JFrame || comp instanceof JDialog) {
            comp.setBackground(currentTheme.bgPrimary);
        } else if (comp instanceof JLabel && !(comp instanceof JButton)) {
            comp.setForeground(currentTheme.textPrimary);
        }
    }

    /**
     * 从持久化存储加载保存的主题设置
     * <p>在应用启动时调用，恢复用户上次选择的主题</p>
     */
    public static void loadSavedTheme() {
        String saved = AppSettingsUtil.loadSetting("theme", "LIGHT");
        try { currentTheme = ThemeType.valueOf(saved); } catch (Exception ignored) {}
    }
}
