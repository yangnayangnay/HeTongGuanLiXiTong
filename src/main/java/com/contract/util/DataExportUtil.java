package com.contract.util;

import com.contract.entity.Contract;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 数据导出工具类
 * <p>
 * 提供合同数据的导出功能，支持CSV和HTML两种格式：
 * - CSV格式：逗号分隔值文件，可用Excel直接打开
 * - HTML格式：带样式的网页表格，可用浏览器打开后打印为PDF
 * </p>
 *
 * <h3>使用说明：</h3>
 * <ul>
 *   <li>CSV导出：无需额外依赖，UTF-8 BOM编码确保Excel正确显示中文</li>
 *   <li>HTML导出：生成专业美观的报表样式，支持浏览器打印为PDF</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 */
public class DataExportUtil {

    /** 日期格式化器 */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    /** 金额格式化器 */
    private static final java.text.DecimalFormat AMOUNT_FORMAT = new java.text.DecimalFormat("#,##0.00");

    /**
     * 导出合同列表为CSV文件
     * <p>
     * 将合同数据以CSV（逗号分隔值）格式导出到指定路径。
     * 使用UTF-8 BOM编码确保Excel能正确识别中文。
     * </p>
     *
     * @param contracts 合同列表
     * @param filePath  保存路径（建议使用.csv扩展名）
     * @throws IOException 文件写入异常
     */
    public static void exportToCSV(List<Contract> contracts, String filePath) throws IOException {
        // UTF-8 BOM编码，确保Excel正确显示中文
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
            // 写入BOM头（EF BB BF），让Excel识别为UTF-8编码
            writer.write('\ufeff');

            // 写入表头
            writer.write("合同编号,合同名称,客户,开始时间,结束时间,起草人,合同金额");
            writer.newLine();

            // 写入数据行
            for (Contract c : contracts) {
                StringBuilder line = new StringBuilder();
                // 处理每个字段，包含逗号或引号的字段需要用引号包裹
                line.append(escapeCSVField(c.getNum())).append(",");
                line.append(escapeCSVField(c.getName())).append(",");
                line.append(escapeCSVField(c.getCustomer())).append(",");
                line.append(escapeCSVField(c.getBeginTime() != null ? DATE_FORMAT.format(c.getBeginTime()) : "")).append(",");
                line.append(escapeCSVField(c.getEndTime() != null ? DATE_FORMAT.format(c.getEndTime()) : "")).append(",");
                line.append(escapeCSVField(c.getUserName())).append(",");
                line.append(AMOUNT_FORMAT.format(c.getAmount()));
                writer.write(line.toString());
                writer.newLine();
            }

            writer.flush();
        }

        System.out.println("[导出] CSV文件已保存: " + filePath + " (共" + contracts.size() + "条记录)");
    }

    /**
     * 导出为HTML格式文件（可用浏览器打开后打印为PDF）
     * <p>
     * 生成带有专业CSS样式的HTML表格报表，
     * 包含合同编号、名称、客户、日期、金额等信息。
     * 生成的HTML可以直接用浏览器打开并Ctrl+P打印为PDF。
     * </p>
     *
     * @param contracts 合同列表
     * @param filePath  保存路径（建议使用.html扩展名）
     * @throws IOException 文件写入异常
     */
    public static void exportToHTML(List<Contract> contracts, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath, false))) {

            // 计算总金额用于汇总行
            double totalAmount = 0;
            for (Contract c : contracts) {
                totalAmount += c.getAmount();
            }

            // ===== HTML文档开始 =====
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"zh-CN\">\n<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>合同统计报表</title>\n");

            // 内联CSS样式（确保离线也能正常显示）
            writer.write("<style>\n");
            writer.write("body { font-family: 'Microsoft YaHei', 'Segoe UI', Arial, sans-serif; margin: 20px; background-color: #f5f7fa; }\n");
            writer.write(".container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }\n");
            writer.write("h1 { color: #2c3e50; text-align: center; font-size: 24px; margin-bottom: 5px; }\n");
            writer.write(".subtitle { text-align: center; color: #7f8c8d; font-size: 14px; margin-bottom: 25px; }\n");
            writer.write("table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n");
            writer.write("th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 10px; text-align: left; font-size: 13px; }\n");
            writer.write("td { padding: 10px; border-bottom: 1px solid #ecf0f1; font-size: 13px; }\n");
            writer.write("tr:nth-child(even) { background-color: #f9f9f9; }\n");
            writer.write("tr:hover { background-color: #e8f4fd; }\n");
            writer.write(".amount { text-align: right; font-weight: bold; color: #27ae60; }\n");
            writer.write(".summary { margin-top: 20px; padding: 15px; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }\n");
            writer.write(".summary-label { font-size: 16px; }\n");
            writer.write(".summary-value { font-size: 22px; font-weight: bold; }\n");
            writer.write(".footer { text-align: center; margin-top: 20px; color: #95a5a6; font-size: 12px; }\n");
            writer.write("@media print { body { background: white; } .container { box-shadow: none; } .no-print { display: none; } }\n");
            writer.write("</style>\n");
            writer.write("</head>\n<body>\n");

            // 容器开始
            writer.write("<div class=\"container\">\n");

            // 标题
            writer.write("<h1>📋 合同管理系统 - 统计报表</h1>\n");
            writer.write("<div class=\"subtitle\">导出时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) +
                         " | 共 " + contracts.size() + " 条记录</div>\n");

            // 表格开始
            writer.write("<table>\n");
            writer.write("<thead><tr>\n");
            writer.write("<th>序号</th>");
            writer.write("<th>合同编号</th>");
            writer.write("<th>合同名称</th>");
            writer.write("<th>客户</th>");
            writer.write("<th>开始时间</th>");
            writer.write("<th>结束时间</th>");
            writer.write("<th>起草人</th>");
            writer.write("<th>合同金额(元)</th>");
            writer.write("</tr></thead>\n<tbody>\n");

            // 表格数据行
            int index = 1;
            for (Contract c : contracts) {
                writer.write("<tr>");
                writer.write("<td>" + index++ + "</td>");
                writer.write("<td>" + escapeHtml(c.getNum()) + "</td>");
                writer.write("<td>" + escapeHtml(c.getName()) + "</td>");
                writer.write("<td>" + escapeHtml(c.getCustomer()) + "</td>");
                writer.write("<td>" + (c.getBeginTime() != null ? DATE_FORMAT.format(c.getBeginTime()) : "-") + "</td>");
                writer.write("<td>" + (c.getEndTime() != null ? DATE_FORMAT.format(c.getEndTime()) : "-") + "</td>");
                writer.write("<td>" + escapeHtml(c.getUserName()) + "</td>");
                writer.write("<td class=\"amount\">" + AMOUNT_FORMAT.format(c.getAmount()) + "</td>");
                writer.write("</tr>\n");
            }

            // 表格结束
            writer.write("</tbody></table>\n");

            // 汇总信息栏
            writer.write("<div class=\"summary\">\n");
            writer.write("<span class=\"summary-label\">💰 合计金额:</span>\n");
            writer.write("<span class=\"summary-value\">" + AMOUNT_FORMAT.format(totalAmount) + " 元</span>\n");
            writer.write("</div>\n");

            // 页脚
            writer.write("<div class=\"footer\">\n");
            writer.write("<p>本报表由合同管理系统自动生成 | 提示：按 Ctrl+P 可将此页面打印/另存为 PDF</p>\n");
            writer.write("</div>\n");

            // 容器结束
            writer.write("</div>\n");

            // HTML文档结束
            writer.write("</body>\n</html>");

            writer.flush();
        }

        System.out.println("[导出] HTML报表已保存: " + filePath + " (共" + contracts.size() + "条记录)");
    }

    /**
     * CSV字段转义处理
     * <p>
     * 如果字段内容包含逗号、双引号或换行符，需要用双引号包裹，
     * 并且字段内的双引号需要转义为两个双引号。
     * </p>
     *
     * @param field 原始字段值
     * @return 转义后的安全字符串
     */
    private static String escapeCSVField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * HTML特殊字符转义
     * <p>防止XSS攻击和HTML解析错误</p>
     *
     * @param text 原始文本
     * @return 转义后的安全HTML字符串
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
