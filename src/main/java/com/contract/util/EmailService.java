package com.contract.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * 邮件发送服务类
 * <p>
 * 负责向用户发送任务分配通知邮件，采用SMTP协议通过SSL加密通道发送。
 * 支持配置SMTP服务器参数（地址、端口、发件人账号、授权码），
 * 默认使用QQ邮箱SMTP服务器作为参考配置。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>配置邮件服务参数（SMTP服务器、发件人账号等）</li>
 *   <li>发送任务分配通知邮件（会签/审批/签订）</li>
 *   <li>支持启用/禁用邮件功能开关</li>
 * </ul>
 *
 * <h3>使用说明：</h3>
 * <ul>
 *   <li>系统启动后需调用configure()方法配置邮件参数才能正常发送</li>
 *   <li>未配置或收件人为默认邮箱时自动跳过发送</li>
 *   <li>发送结果通过返回值和日志输出反馈</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class EmailService {

    /** SMTP服务器地址（可配置），默认使用QQ邮箱SMTP */
    private static String smtpHost = "smtp.qq.com";
    /** SMTP端口，默认465（SSL加密端口） */
    private static int smtpPort = 465;
    /** 发件人邮箱地址 */
    private static String senderEmail = "noreply@contract.com";
    /** 发件人密码或授权码（第三方登录需使用授权码而非登录密码） */
    private static String senderPassword = "";
    /** 是否启用邮件功能，默认关闭，需配置后手动开启 */
    private static boolean emailEnabled = false;

    /**
     * 配置邮件服务参数
     * <p>
     * 设置SMTP连接所需的所有参数，包括服务器地址、端口号、
     * 发件人邮箱和授权码。调用此方法后会自动启用邮件功能。
     * </p>
     *
     * @param host     SMTP服务器地址（如"smtp.qq.com"、"smtp.163.com"）
     * @param port     SMTP端口号（SSL通常为465，TLS通常为587）
     * @param email    发件人邮箱地址
     * @param password 发件人授权码（非登录密码，需在邮箱设置中获取）
     */
    public static void configure(String host, int port, String email, String password) {
        smtpHost = host;
        smtpPort = port;
        senderEmail = email;
        senderPassword = password;
        emailEnabled = true;  // 配置完成后自动启用邮件功能
    }

    /**
     * 启用或禁用邮件功能
     * <p>
     * 用于在运行时动态控制是否发送邮件通知。
     * 例如：测试环境可禁用邮件以避免误发。
     * </p>
     *
     * @param enabled true表示启用邮件发送；false表示禁用
     */
    public static void setEnabled(boolean enabled) {
        emailEnabled = enabled;
    }

    /**
     * 发送任务分配通知邮件
     * <p>
     * 向被分配任务的用户发送包含合同信息和任务类型的邮件通知。
     * 邮件内容包括合同编号、合同名称、任务类型等关键信息。
     * </p>
     *
     * <h3>前置检查：</h3>
     * <ul>
     *   <li>邮件功能必须已配置并启用</li>
     *   <li>收件人邮箱不能为空且不能是默认占位邮箱</li>
     * </ul>
     *
     * @param toEmail      收件人邮箱地址
     * @param userName     收件人用户名（用于邮件正文称呼）
     * @param contractNum  合同编号
     * @param contractName 合同名称
     * @param taskType     任务类型（如"会签"、"审批"、"签订"）
     * @return true表示发送成功；false表示未发送或发送失败
     */
    public static boolean sendTaskNotification(String toEmail, String userName,
            String contractNum, String contractName, String taskType) {
        // 前置校验：邮件未配置、收件人为空或为默认邮箱时跳过发送
        if (!emailEnabled || toEmail == null || toEmail.isEmpty()
                || "default@example.com".equals(toEmail)) {
            System.out.println("[邮件] 跳过发送（邮件未配置或收件人无有效邮箱）: " + userName);
            return false;
        }

        try {
            // 配置SMTP连接属性
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);           // SMTP服务器地址
            props.put("mail.smtp.port", smtpPort);           // SMTP端口
            props.put("mail.smtp.ssl.enable", "true");       // 启用SSL加密传输
            props.put("mail.smtp.auth", "true");             // 启用SMTP身份认证

            // 创建邮件会话，设置发件人认证信息
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            // 构建邮件消息对象
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));                          // 设置发件人
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));  // 设置收件人
            message.setSubject("【合同管理系统】您有新的" + taskType + "任务待处理");       // 设置邮件主题

            // 组装邮件正文内容
            String body = "尊敬的 " + userName + "：\n\n" +
                "您好！\n\n" +
                "合同管理系统中为您分配了新的【" + taskType + "】任务，请及时处理。\n\n" +
                "合同编号：" + contractNum + "\n" +
                "合同名称：" + contractName + "\n" +
                "任务类型：" + taskType + "\n\n" +
                "请登录合同管理系统查看详情。\n\n" +
                "此邮件由系统自动发送，请勿回复。\n" +
                "--\n" +
                "合同管理系统";

            message.setText(body);          // 设置邮件正文（纯文本格式）
            Transport.send(message);         // 发送邮件

            // 记录发送成功的日志
            System.out.println("[邮件] 发送成功 -> " + toEmail + " (" + taskType + ": " + contractName + ")");
            return true;
        } catch (Exception e) {
            // 记录发送失败的异常信息，不抛出以免影响主业务流程
            System.err.println("[邮件] 发送失败 -> " + toEmail + ": " + e.getMessage());
            return false;
        }
    }
}
