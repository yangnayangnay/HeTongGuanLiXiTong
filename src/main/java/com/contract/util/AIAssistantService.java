package com.contract.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AI辅助合同审查服务
 * <p>
 * 提供基于本地大语言模型(LLM)的智能合同审查能力。
 * 默认使用Ollama本地模型接口，确保数据不外泄。
 * </p>
 *
 * <h3>支持的AI方案：</h3>
 * <ul>
 *   <li>方案A（推荐）：Ollama本地模型 - 数据完全不出本机</li>
 *   <li>方案B：加密API调用 - 通过HTTPS加密通道访问云端AI</li>
 * </ul>
 *
 * <h3>Ollama使用说明：</h3>
 * <ol>
 *   <li>从 https://ollama.ai 下载并安装Ollama</li>
 *   <li>运行 ollama pull qwen2.5 或 ollama run qwen2.5</li>
 *   <li>系统会自动连接 localhost:11434</li>
 * </ol>
 */
public class AIAssistantService {

    /** Ollama API地址（默认本地） */
    private static String ollamaUrl = "http://localhost:11434";
    /** 使用的模型名称 */
    private static String modelName = "qwen2:7b";
    /** 是否启用AI功能 */
    private static boolean aiEnabled = false;

    /**
     * 配置AI服务连接参数
     * @param url Ollama服务地址（默认http://localhost:11434）
     * @param model 模型名称（如qwen2.5, llama3.2等）
     */
    public static void configure(String url, String model) {
        FileLogger.info("AIAssistantService", "configure", "配置AI服务, URL: " + url + ", 模型: " + model);
        ollamaUrl = url;
        modelName = model;
        aiEnabled = true;
        FileLogger.info("AIAssistantService", "configure", "AI服务配置成功");
    }

    /**
     * AI审查合同内容
     * <p>将合同文本发送给本地AI模型进行分析，返回审查意见</p>
     *
     * @param contractContent 合同正文内容
     * @return AI生成的审查建议；失败时返回错误提示
     */
    public static String reviewContract(String contractContent) {
        FileLogger.info("AIAssistantService", "reviewContract", "开始AI合同审查, 输入长度: " + (contractContent != null ? contractContent.length() : 0));
        if (!aiEnabled) {
            FileLogger.warn("AIAssistantService", "reviewContract", "AI功能未启用");
            return "[AI功能未启用]\n\n请先配置AI服务：\n1. 安装Ollama: https://ollama.ai\n2. 运行: ollama pull qwen2.5\n3. 在设置中启用AI功能";
        }

        try {
            // 构建提示词
            String prompt = "你是一个专业的合同审查助手。请审查以下合同内容，指出潜在的风险点和不规范之处。\n\n合同内容：\n" + contractContent + "\n\n请从以下方面给出审查意见：\n1. 条款完整性\n2. 法律风险\n3. 格式规范性\n4. 关键条款建议\n\n请用简洁明了的中文回答。";

            // 调用Ollama API
            URL url = new URL(ollamaUrl + "/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            // 设置超时时间
            conn.setConnectTimeout(10000);   // 连接超时10秒
            conn.setReadTimeout(120000);      // 读取超时120秒（AI生成可能较慢）

            // 发送请求体
            String jsonBody = "{\"model\":\"" + modelName + "\",\"prompt\":\"" + escapeJson(prompt) + "\",\"stream\":false}";
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes("UTF-8"));
            os.flush();

            // 读取响应
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // 解析JSON获取response字段
            String respStr = response.toString();
            // 简单提取"response":"..."字段的内容
            int start = respStr.indexOf("\"response\":\"") + 12;
            if (start > 11) {
                int end = findJsonStringEnd(respStr, start);
                if (end > start) {
                    String result = unescapeJson(respStr.substring(start, end));
                    FileLogger.info("AIAssistantService", "reviewContract", "AI审查完成, 结果长度: " + result.length());
                    return result;
                }
            }
            FileLogger.warn("AIAssistantService", "reviewContract", "AI返回结果解析失败");
            return "AI返回结果解析失败: " + respStr.substring(0, Math.min(200, respStr.length()));

        } catch (Exception e) {
            FileLogger.error("AIAssistantService", "reviewContract", "AI审查失败: " + e.getMessage(), e);
            return "[AI服务连接失败]\n\n错误信息: " + e.getMessage() + "\n\n请确认：\n1. Ollama是否已安装并运行?\n2. 是否已下载模型? (运行: ollama pull qwen2.5)\n3. 服务地址是否正确? (当前: " + ollamaUrl + ")";
        }
    }

    /**
     * 查找JSON字符串的结束位置（处理转义字符）
     * @param json JSON字符串
     * @param start 起始位置（在第一个引号之后）
     * @return 结束引号的位置
     */
    private static int findJsonStringEnd(String json, int start) {
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '"') {
                // 检查是否是转义引号
                int backslashCount = 0;
                int j = i - 1;
                while (j >= start - 1 && json.charAt(j) == '\\') {
                    backslashCount++;
                    j--;
                }
                if (backslashCount % 2 == 0) {
                    return i;  // 偶数个反斜杠，说明这个引号不是转义的
                }
            }
            i++;
        }
        return -1;
    }

    /** JSON字符串转义 */
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                 .replace("\n", "\\n").replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

    /** JSON字符串反转义 */
    private static String unescapeJson(String s) {
        return s.replace("\\n", "\n").replace("\r", "\r")
                 .replace("\t", "\\t").replace("\\\"", "\"")
                 .replace("\\\\", "\\");
    }

    /**
     * AI自动填入合同信息
     * <p>基于模板填充合同信息，而非完全生成</p>
     *
     * @param contractName 合同名称
     * @param customerName 客户/对方企业名
     * @param userName 当前用户名（作为甲方）
     * @return AI生成的合同草稿内容
     */
    public static String generateContractDraft(String contractName, String customerName, String userName) {
        FileLogger.info("AIAssistantService", "generateContractDraft", "开始AI生成合同草稿");
        if (!aiEnabled) {
            return null;
        }

        try {
            // 优化提示词：基于模板填充，明确区分合同名称和编号
            String prompt = "你是合同填写助手。请基于以下模板，将提供的信息填入对应位置。\n\n" +
                "【模板内容】\n" +
                "合同编号：{{合同编号}}\n" +
                "合同名称：{{合同名称}}\n" +
                "甲方：{{甲方}}\n" +
                "乙方：{{乙方}}\n" +
                "签订日期：{{签订日期}}\n" +
                "合同期限：自{{开始日期}}至{{结束日期}}\n\n" +
                "【需要填入的信息】\n" +
                "合同名称：" + (contractName != null && !contractName.isEmpty() ? contractName : "待定") + "\n" +
                "甲方：" + (userName != null && !userName.isEmpty() ? userName : "待定") + "\n" +
                "乙方：" + (customerName != null && !customerName.isEmpty() ? customerName : "待定") + "\n" +
                "签订日期：" + java.time.LocalDate.now() + "\n" +
                "开始日期：" + java.time.LocalDate.now() + "\n" +
                "结束日期：" + java.time.LocalDate.now().plusYears(1) + "\n\n" +
                "【重要说明】\n" +
                "1. 合同编号保持为{{合同编号}}，由系统自动生成\n" +
                "2. 合同名称使用提供的名称，不要编造编号\n" +
                "3. 只输出填充后的模板内容，不要添加额外条款或解释\n" +
                "4. 保持模板原有结构，只替换占位符";

            URL url = new URL(ollamaUrl + "/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);  // 连接超时5秒
            conn.setReadTimeout(60000);    // 读取超时60秒（优化后）

            String jsonBody = "{\"model\":\"" + modelName + "\",\"prompt\":\"" + escapeJson(prompt) + "\",\"stream\":false}";
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes("UTF-8"));
            os.flush();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String respStr = response.toString();
            int start = respStr.indexOf("\"response\":\"") + 12;
            if (start > 11) {
                int end = findJsonStringEnd(respStr, start);
                if (end > start) {
                    String result = unescapeJson(respStr.substring(start, end));
                    FileLogger.info("AIAssistantService", "generateContractDraft", "AI草稿生成完成, 长度: " + result.length());
                    return result;
                }
            }
            return null;
        } catch (Exception e) {
            FileLogger.error("AIAssistantService", "generateContractDraft", "AI草稿生成失败: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查AI服务是否可用
     * @return true表示AI服务可正常连接；false表示不可用
     */
    public static boolean isAvailable() {
        if (!aiEnabled) return false;
        try {
            URL url = new URL(ollamaUrl + "/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            boolean available = conn.getResponseCode() == 200;
            FileLogger.info("AIAssistantService", "isAvailable", "AI服务可用性检查: " + available);
            return available;
        } catch (Exception e) {
            FileLogger.warn("AIAssistantService", "isAvailable", "AI服务不可用: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前配置的Ollama URL
     * @return Ollama服务地址
     */
    public static String getOllamaUrl() {
        return ollamaUrl;
    }

    /**
     * 获取当前使用的模型名称
     * @return 模型名称
     */
    public static String getModelName() {
        return modelName;
    }

    /**
     * 检查AI功能是否已启用
     * @return true表示已启用；false表示未启用
     */
    public static boolean isAiEnabled() {
        return aiEnabled;
    }
}
