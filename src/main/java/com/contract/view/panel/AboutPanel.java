package com.contract.view.panel;

import com.contract.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 关于系统/项目说明面板
 * <p>
 * 显示合同管理系统的项目基本信息，包括项目名称、版本号、
 * 技术栈说明、功能模块列表、开发日期和版权信息等。
 * 使用HTML格式化的JEditorPane展示内容，居中对齐排版美观。
 * </p>
 *
 * @author 合同管理系统
 * @version 2.0
 * @since 2024-01-01
 */
public class AboutPanel extends JPanel {

    /**
     * 构造方法：初始化关于系统面板
     */
    public AboutPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        FileLogger.info("AboutPanel", "AboutPanel", "关于面板初始化");
        initUI();
    }

    /**
     * 初始化界面组件
     * <p>使用JEditorPane以HTML格式展示项目信息，居中对齐</p>
     */
    private void initUI() {
        // 使用HTML格式构建项目说明内容
        String htmlContent =
            "<html><body style='font-family:微软雅黑,sans-serif; text-align:center; padding:20px;'>" +
            "<h1 style='color:#2C3E50;'>📋 合同管理系统</h1>" +
            "<hr style='border:none; border-top:2px solid #3498DB; width:60%; margin:15px auto;'>" +
            "<table style='margin:0 auto; border-collapse:collapse; width:70%;'>" +
            "  <tr><td style='padding:8px; color:#7F8C8D; text-align:right; font-weight:bold;'>版本号：</td>" +
            "      <td style='padding:8px; color:#2C3E50; text-align:left;'><b>v2.0</b></td></tr>" +
            "  <tr><td style='padding:8px; color:#7F8C8D; text-align:right; font-weight:bold;'>技术栈：</td>" +
            "      <td style='padding:8px; color:#2C3E50; text-align:left;'>Java Swing / Oracle Database / JDBC / Maven / RBAC权限模型</td></tr>" +
            "  <tr><td style='padding:8px; color:#7F8C8D; text-align:right; font-weight:bold;'>开发周期：</td>" +
            "      <td style='padding:8px; color:#2C3E50; text-align:left;'>2024 - 2026</td></tr>" +
            "</table>" +

            "<h3 style='color:#2980B9; margin-top:25px;'>📦 功能模块（共12个）</h3>" +
            "<table style='margin:10px auto; border-collapse:collapse; width:65%; background:#FAFAFA; border-radius:8px;'>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>📝 F01 - 起草合同</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>✍️ F02 - 会签合同</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>📄 F03 - 定稿合同</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>✅ F04 - 审批合同</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>🖋️ F05 - 签订合同</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>📤 F06 - 分配合同</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>🔍 F07 - 合同查询</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>👥 F09 - 客户管理</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>👤 F10 - 用户管理</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>🛡️ F11 - 角色管理</td></tr>" +
            "  <tr><td style='padding:6px 15px; border-bottom:1px solid #EEE;'>📋 F12 - 日志管理</td></tr>" +
            "  <tr><td style='padding:6px 15px;'>⏰ 定时提醒 / 邮件通知</td></tr>" +
            "</table>" +

            "<hr style='border:none; border-top:1px solid #ECF0F1; width:40%; margin:20px auto;'>" +
            "<p style='color:#95A5A6; font-size:12px;'>© 2024-2026 合同管理系统 All Rights Reserved.</p>" +
            "<p style='color:#BDC3C7; font-size:11px;'>基于 Java Swing + Oracle + JDBC + RBAC 权限模型构建</p>" +
            "</body></html>";

        // 创建JEditorPane展示HTML内容
        JEditorPane editorPane = new JEditorPane("text/html", htmlContent);
        editorPane.setEditable(false);                           // 设置为只读模式
        editorPane.setBackground(Color.WHITE);                  // 白色背景
        editorPane.setMargin(new Insets(10, 10, 10, 10));       // 内边距

        // 放入滚动面板支持长内容浏览
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(null);                              // 去掉边框更美观
        add(scrollPane, BorderLayout.CENTER);
    }
}
