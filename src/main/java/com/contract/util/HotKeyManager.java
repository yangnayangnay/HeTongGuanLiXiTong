package com.contract.util;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局快捷键管理器
 * <p>统一管理系统中所有的快捷键绑定，提供键盘操作快捷方式</p>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class HotKeyManager {

    /** 快捷键绑定映射表 */
    private static Map<KeyStroke, Runnable> keyBindings = new HashMap<>();
    /** 根窗格引用（用于注册全局键盘事件） */
    private static JRootPane rootPane;

    /**
     * 初始化快捷键管理器，绑定到指定根窗格
     *
     * @param pane 根窗格（通常是主窗口的getRootPane()）
     */
    public static void init(JRootPane pane) {
        HotKeyManager.rootPane = pane;
        registerDefaultKeys();
    }

    /**
     * 注册默认快捷键
     * <p>定义系统内置的所有快捷键绑定：</p>
     * <ul>
     *   <li>Ctrl+N: 新建合同起草</li>
     *   <li>Ctrl+F: 搜索/查询合同</li>
     *   <li>Ctrl+S: 保存（当前操作）</li>
     *   <li>F5: 刷新当前列表</li>
     *   <li>Ctrl+1~9: 快速切换到对应的功能面板</li>
     *   <li>F1: 显示快捷键帮助</li>
     *   <li>Ctrl+Q: 退出系统</li>
     * </ul>
     */
    private static void registerDefaultKeys() {
        // Ctrl+N: 新建合同起草
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK), "draft",
            () -> switchToPanel("draft"));

        // Ctrl+F: 搜索/查询
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "search",
            () -> switchToPanel("query"));

        // Ctrl+S: 保存（当前操作）
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "save",
            () -> {});  // 需要根据当前面板类型决定

        // F5: 刷新当前列表
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh",
            () -> {});  // 需要通知当前面板刷新

        // Ctrl+1~9: 快速切换到对应的面板
        String[] panels = {"draft", "countersign", "finalize", "approve", "sign",
                          "query", "customer", "assign", "userManage"};
        for (int i = 0; i < panels.length && i < 9; i++) {
            final String panelCmd = panels[i];
            bind(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, InputEvent.CTRL_MASK),
                 "switch_" + (i + 1), () -> switchToPanel(panelCmd));
        }

        // F1: 显示快捷键帮助
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help",
            () -> showHelpDialog());

        // Ctrl+Q: 退出
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK), "exit",
            () -> System.exit(0));

        // ESC: 关闭当前焦点弹窗（默认行为，不需要额外处理）
    }

    /**
     * 绑定单个快捷键到指定动作
     *
     * @param stroke  键盘按键组合
     * @param name    动作名称（唯一标识）
     * @param action  执行的动作
     */
    public static void bind(KeyStroke stroke, String name, Runnable action) {
        keyBindings.put(stroke, action);
        if (rootPane != null) {
            InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = rootPane.getActionMap();
            im.put(stroke, name);
            am.put(name, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    Runnable r = keyBindings.get(stroke);
                    if (r != null) r.run();
                }
            });
        }
    }

    /** 面板切换回调（需要在MainFrame中设置） */
    private static java.util.function.Consumer<String> panelSwitcher;

    /**
     * 设置面板切换回调函数
     * <p>由MainFrame在初始化时设置，用于将快捷键命令转发给switchPanel方法</p>
     *
     * @param switcher 面板切换回调
     */
    public static void setPanelSwitcher(java.util.function.Consumer<String> switcher) {
        panelSwitcher = switcher;
    }

    /**
     * 执行面板切换
     *
     * @param cmd 面板命令标识
     */
    private static void switchToPanel(String cmd) {
        if (panelSwitcher != null) panelSwitcher.accept(cmd);
    }

    /**
     * 显示快捷键帮助对话框
     */
    private static void showHelpDialog() {
        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 13));
        helpText.setText(
            "快捷键帮助\n" +
            "==========\n\n" +
            "Ctrl+N    新建合同起草\n" +
            "Ctrl+F    合同查询\n" +
            "Ctrl+S    保存当前操作\n" +
            "F5        刷新当前列表\n" +
            "Ctrl+1~9  快速切换功能面板\n" +
            "           1=起草 2=会签 3=定稿 4=审批 5=签订\n" +
            "           6=查询 7=客户 8=分配 9=用户\n" +
            "F1        显示此帮助\n" +
            "Ctrl+Q    退出系统\n" +
            "ESC       关闭弹窗"
        );
        JOptionPane.showMessageDialog(rootPane == null ? null : rootPane.getParent(),
            new JScrollPane(helpText), "快捷键帮助", JOptionPane.INFORMATION_MESSAGE);
    }
}
