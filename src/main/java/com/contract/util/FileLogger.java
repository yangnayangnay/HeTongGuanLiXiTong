package com.contract.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件日志工具类
 * <p>提供统一的日志记录功能，将日志写入本地txt文件，支持按日期分文件和自动清理。</p>
 *
 * <h3>功能特点：</h3>
 * <ul>
 *   <li>日志按日期分文件存储（如 2026-06-11.log）</li>
 *   <li>日志级别：DEBUG / INFO / WARN / ERROR</li>
 *   <li>自动清理：单文件超过2GB时自动截断保留末尾部分</li>
 *   <li>线程安全：使用synchronized保证多线程写入安全</li>
 *   <li>格式统一：[时间] [级别] [类名.方法] 消息内容</li>
 * </ul>
 *
 * <h3>日志文件位置：</h3>
 * <code>%USERPROFILE%/.contract/logs/yyyy-MM-dd.log</code>
 *
 * <h3>使用示例：</h3>
 * <pre>
 *   FileLogger.info("UserDao", "findByName", "查询用户: name=" + name);
 *   FileLogger.error("UserService", "login", "登录失败: " + e.getMessage(), e);
 *   FileLogger.debug("ContractDao", "findAll", "SQL=" + sql);
 * </pre>
 */
public class FileLogger {

    /** 日志目录路径（优先使用环境变量，其次项目目录，最后用户目录） */
    private static final String LOG_DIR = System.getProperty("contract.log.dir",
        System.getenv("CONTRACT_LOG_DIR") != null ? System.getenv("CONTRACT_LOG_DIR") :
        System.getProperty("user.home") + File.separator + ".contract" + File.separator + "logs");
    /** 单个日志文件最大大小：2GB */
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024 * 1024;  // 2GB
    /** 日期格式（用于文件名和日志时间戳） */
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    /** 当前日志文件大小缓存 */
    private static long currentFileSize = -1;
    /** 当前日志文件日期 */
    private static String currentDate = "";

    /**
     * 记录DEBUG级别日志
     * @param className  类名（简短标识）
     * @param method     方法名
     * @param message    日志消息
     */
    public static void debug(String className, String method, String message) {
        writeLog("DEBUG", className, method, message, null);
    }

    /**
     * 记录INFO级别日志
     * @param className  类名（简短标识）
     * @param method     方法名
     * @param message    日志消息
     */
    public static void info(String className, String method, String message) {
        writeLog("INFO ", className, method, message, null);
    }

    /**
     * 记录WARN级别日志
     * @param className  类名（简短标识）
     * @param method     方法名
     * @param message    日志消息
     */
    public static void warn(String className, String method, String message) {
        writeLog("WARN ", className, method, message, null);
    }

    /**
     * 记录ERROR级别日志（无异常对象）
     * @param className  类名（简短标识）
     * @param method     方法名
     * @param message    日志消息
     */
    public static void error(String className, String method, String message) {
        writeLog("ERROR", className, method, message, null);
    }

    /**
     * 记录ERROR级别日志（含异常堆栈）
     * @param className  类名（简短标识）
     * @param method     方法名
     * @param message    日志消息
     * @param e          异常对象
     */
    public static void error(String className, String method, String message, Throwable e) {
        writeLog("ERROR", className, method, message, e);
    }

    /**
     * 核心日志写入方法
     * @param level      日志级别
     * @param className  类名
     * @param method     方法名
     * @param message    消息内容
     * @param e          异常对象（可为null）
     */
    private static synchronized void writeLog(String level, String className, String method, String message, Throwable e) {
        try {
            // 确保日志目录存在
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 按日期生成日志文件名
            String today = DATE_FMT.format(new Date());
            File logFile = new File(dir, today + ".log");

            // 检查文件大小，超过2GB则清理
            checkAndCleanLogFile(logFile);

            // 构造日志行
            String timestamp = TIME_FMT.format(new Date());
            StringBuilder line = new StringBuilder();
            line.append("[").append(timestamp).append("] ");
            line.append("[").append(level).append("] ");
            line.append("[").append(className).append(".").append(method).append("] ");
            line.append(message);

            // 如果有异常，追加堆栈信息
            if (e != null) {
                line.append("\n  异常类型: ").append(e.getClass().getName());
                line.append("\n  异常消息: ").append(e.getMessage());
                // 追加前5行堆栈
                StackTraceElement[] stack = e.getStackTrace();
                int limit = Math.min(stack.length, 5);
                for (int i = 0; i < limit; i++) {
                    line.append("\n    at ").append(stack[i].toString());
                }
                if (stack.length > limit) {
                    line.append("\n    ... ").append(stack.length - limit).append(" more");
                }
            }

            // 写入文件（追加模式）
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(line.toString());
                bw.newLine();
                bw.flush();
            }

            // 更新文件大小缓存
            currentFileSize = logFile.length();
            currentDate = today;

            // 同时输出到控制台（ERROR和WARN始终输出，DEBUG和INFO可选）
            if ("ERROR".equals(level.trim()) || "WARN".equals(level.trim())) {
                System.err.println(line.toString());
            } else {
                System.out.println(line.toString());
            }

        } catch (Exception ex) {
            // 日志系统本身出错，只打印到控制台
            System.err.println("[FileLogger] 写入日志失败: " + ex.getMessage());
        }
    }

    /**
     * 检查日志文件大小，超过2GB时自动清理
     * <p>清理策略：保留文件末尾100MB数据，删除前半部分</p>
     *
     * @param logFile 日志文件
     */
    private static void checkAndCleanLogFile(File logFile) {
        try {
            if (!logFile.exists()) {
                currentFileSize = 0;
                return;
            }

            long fileSize = logFile.length();
            if (fileSize < MAX_FILE_SIZE) {
                currentFileSize = fileSize;
                return;
            }

            // 文件超过2GB，执行清理
            System.out.println("[FileLogger] 日志文件 " + logFile.getName() + " 超过2GB(" + (fileSize / 1024 / 1024) + "MB)，开始清理...");

            // 保留末尾100MB数据
            long keepSize = 100L * 1024 * 1024;  // 100MB
            long skipSize = fileSize - keepSize;

            File tempFile = new File(logFile.getParent(), logFile.getName() + ".tmp");
            try (RandomAccessFile raf = new RandomAccessFile(logFile, "r");
                 FileWriter fw = new FileWriter(tempFile, false);
                 BufferedWriter bw = new BufferedWriter(fw)) {

                raf.seek(skipSize);
                byte[] buffer = new byte[8192];
                int bytesRead;
                // 跳到第一行完整行的开始位置
                boolean firstLine = true;
                while ((bytesRead = raf.read(buffer)) != -1) {
                    String chunk = new String(buffer, 0, bytesRead, "UTF-8");
                    if (firstLine) {
                        // 跳过可能不完整的第一行
                        int newlineIdx = chunk.indexOf('\n');
                        if (newlineIdx >= 0) {
                            chunk = chunk.substring(newlineIdx + 1);
                        } else {
                            continue;
                        }
                        firstLine = false;
                    }
                    bw.write(chunk);
                }
                bw.flush();
            }

            // 替换原文件
            if (logFile.delete() && tempFile.renameTo(logFile)) {
                currentFileSize = logFile.length();
                System.out.println("[FileLogger] 日志清理完成，保留末尾" + (currentFileSize / 1024 / 1024) + "MB数据");
            } else {
                System.err.println("[FileLogger] 日志清理失败，无法替换文件");
                tempFile.delete();
            }

        } catch (Exception e) {
            System.err.println("[FileLogger] 日志清理异常: " + e.getMessage());
        }
    }

    /**
     * 获取日志目录路径
     * @return 日志目录的绝对路径
     */
    public static String getLogDir() {
        return LOG_DIR;
    }

    /**
     * 获取当前日志文件路径
     * @return 当前日期的日志文件绝对路径
     */
    public static String getCurrentLogFile() {
        String today = DATE_FMT.format(new Date());
        return LOG_DIR + File.separator + today + ".log";
    }
}
