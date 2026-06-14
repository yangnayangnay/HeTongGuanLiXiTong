package com.contract.util;

import java.io.File;

/**
 * OCR文字识别服务
 * <p>
 * 支持多种OCR方案识别合同图片/PDF中的文字内容。
 * 优先级顺序：Tesseract本地 > 云端API > 模拟演示模式
 * </p>
 *
 * <h3>使用方式：</h3>
 * <ol>
 *   <li>方案A（推荐）：安装Tesseract OCR引擎</li>
 *   <li>方案B：接入百度/腾讯/阿里云OCR API</li>
 *   <li>方案C（内置）：模拟OCR效果用于演示</li>
 * </ol>
 */
public class OCRService {

    /** OCR引擎类型 */
    public enum OCREngine {
        TESSERACT("Tesseract本地引擎"),
        CLOUD_API("云端OCR接口"),
        DEMO("演示模式（模拟识别）");
        OCREngine(String desc) { this.desc = desc; }
        public final String desc;
    }

    private static OCREngine currentEngine = OCREngine.DEMO;
    private static String tesseractPath = "tesseract";  // Tesseract可执行文件路径
    private static String cloudApiUrl = "";              // 云端API地址
    private static String cloudApiKey = "";              // API密钥

    /**
     * 配置OCR引擎
     *
     * @param engine      使用的OCR引擎类型
     * @param pathOrUrl   Tesseract路径或云端API地址
     * @param apiKey      云端API密钥（Tesseract时可为空）
     */
    public static void configure(OCREngine engine, String pathOrUrl, String apiKey) {
        currentEngine = engine;
        if (engine == OCREngine.TESSERACT) tesseractPath = pathOrUrl;
        else if (engine == OCREngine.CLOUD_API) { cloudApiUrl = pathOrUrl; cloudApiKey = apiKey; }
    }

    /**
     * 获取当前使用的OCR引擎
     *
     * @return 当前引擎枚举值
     */
    public static OCREngine getCurrentEngine() {
        return currentEngine;
    }

    /**
     * 识别图片文件中的文字
     *
     * @param imageFile 图片文件（PNG/JPG/BMP/TIFF）
     * @return 识别出的文字内容；失败返回错误提示
     */
    public static String recognizeImage(File imageFile) {
        switch (currentEngine) {
            case TESSERACT: return recognizeWithTesseract(imageFile);
            case CLOUD_API: return recognizeWithCloudAPI(imageFile);
            case DEMO: default: return recognizeDemo(imageFile);
        }
    }

    /**
     * Tesseract本地引擎识别
     * 通过Runtime.exec()调用tesseract命令行工具
     */
    private static String recognizeWithTesseract(File imageFile) {
        try {
            // 调用: tesseract image.jpg stdout -l chi_sim+eng
            ProcessBuilder pb = new ProcessBuilder(
                tesseractPath, imageFile.getAbsolutePath(), "stdout", "-l", "chi_sim+eng"
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(proc.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = proc.waitFor();
            if (exitCode == 0) {
                return output.toString().trim();
            } else {
                return "[Tesseract识别失败，退出码: " + exitCode + "]\n请确认:\n1. Tesseract是否已安装?\n2. 是否已下载中文语言包?\n3. 路径是否正确?";
            }
        } catch (Exception e) {
            return "[Tesseract调用异常: " + e.getMessage() + "]";
        }
    }

    /**
     * 云端API识别（预留接口）
     * 目前支持百度OCR API格式
     */
    private static String recognizeWithCloudAPI(File imageFile) {
        if (cloudApiUrl.isEmpty()) {
            return "[云端OCR未配置]\n请在系统设置中配置API地址和密钥";
        }
        try {
            // 将图片转为Base64
            String base64Image = fileToBase64(imageFile);

            // 构造HTTP请求（示例：百度OCR格式）
            java.net.URL url = new java.net.URL(cloudApiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            if (!cloudApiKey.isEmpty()) {
                conn.setRequestProperty("apikey", cloudApiKey);
            }

            String jsonBody = "{\"image\":\"" + base64Image + "\"}";
            java.io.OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes("UTF-8"));
            os.flush();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            // 解析响应JSON提取文字结果
            String respStr = response.toString();
            // 简单提取words_result数组中的text
            StringBuilder result = new StringBuilder();
            int idx = 0;
            while ((idx = respStr.indexOf("\"words\"", idx)) != -1) {
                int start = respStr.indexOf(":", idx) + 2;
                int end = respStr.indexOf("\"", start + 1);
                if (end > start) {
                    result.append(respStr.substring(start, end)).append("\n");
                }
                idx = end + 1;
            }

            if (result.length() == 0) return "API返回: " + respStr.substring(0, Math.min(500, respStr.length()));
            return result.toString().trim();
        } catch (Exception e) {
            return "[云端OCR调用失败: " + e.getMessage() + "]";
        }
    }

    /**
     * 演示模式：模拟OCR识别效果
     * 用于展示功能流程，无需真实OCR引擎
     */
    private static String recognizeDemo(File imageFile) {
        return "[OCR演示模式]\n\n" +
            "已成功识别图像文件: " + imageFile.getName() + "\n" +
            "文件大小: " + (imageFile.length() / 1024) + " KB\n\n" +
            "===== 模拟识别结果 =====\n\n" +
            "合同编号: HT-2026-XXXX\n" +
            "合同名称: [需人工确认]\n" +
            "甲方（委托方）: [需人工确认]\n" +
            "乙方（受托方）: [需人工确认]\n" +
            "合同金额: [需人工确认]\n" +
            "签订日期: [需人工确认]\n" +
            "有效期: [需人工确认]\n\n" +
            "---\n" +
            "提示: 这是演示模式的模拟输出。\n" +
            "如需真实OCR识别，请:\n" +
            "1. 安装Tesseract OCR: https://github.com/tesseract-ocr\n" +
            "2. 在系统设置中选择\"Tesseract本地引擎\"\n" +
            "3. 或配置云端OCR API密钥";
    }

    /**
     * 文件转Base64编码
     *
     * @param file 要转换的文件对象
     * @return Base64编码字符串
     * @throws Exception 文件读取异常
     */
    private static String fileToBase64(File file) throws Exception {
        byte[] data = FileUploadUtil.readFileToBytes(file);
        return java.util.Base64.getEncoder().encodeToString(data);
    }

    /**
     * 检测Tesseract是否可用
     *
     * @return true-Tesseract可用；false-不可用
     */
    public static boolean isTesseractAvailable() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{tesseractPath, "--version"});
            return proc.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
