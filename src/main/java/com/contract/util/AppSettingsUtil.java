package com.contract.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * 应用设置持久化工具类
 * <p>
 * 使用Properties文件存储系统设置到用户目录下。
 * 所有设置保存在用户主目录的.contract子目录中的settings.properties文件中。
 * 提供设置的保存、加载和查询功能。
 * </p>
 *
 * <h3>存储路径：</h3>
 * <pre>%USERPROFILE%\.contract\settings.properties</pre>
 *
 * <h3>使用示例：</h3>
 * <pre>
 *   AppSettingsUtil.saveSetting("smtp.host", "smtp.qq.com");
 *   String host = AppSettingsUtil.loadSetting("smtp.host", "smtp.qq.com");
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 */
public class AppSettingsUtil {

    /** 设置文件所在目录 */
    private static final String SETTINGS_DIR = System.getProperty("user.home") + File.separator + ".contract";
    /** 设置文件名 */
    private static final String SETTINGS_FILE = SETTINGS_DIR + File.separator + "settings.properties";
    /** Properties实例（内存缓存） */
    private static Properties properties = new Properties();

    static {
        // 类加载时自动初始化：确保目录存在并加载已有设置
        initSettingsDir();
        loadFromFile();
    }

    /**
     * 初始化设置存储目录
     * 如果目录不存在则自动创建
     */
    private static void initSettingsDir() {
        File dir = new File(SETTINGS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();  // 创建多级目录
        }
    }

    /**
     * 从文件加载所有设置到内存
     * 如果文件不存在或读取失败，使用空属性集
     */
    private static void loadFromFile() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (Exception e) {
                System.err.println("[设置] 加载设置文件失败: " + e.getMessage());
            }
        }
    }

    /**
     * 保存单个设置项到文件
     * <p>同时更新内存缓存和持久化文件</p>
     *
     * @param key   设置键名（如"smtp.host"、"ai.url"等）
     * @param value 设置值
     */
    public static void saveSetting(String key, String value) {
        properties.setProperty(key, value);
        saveToFile();
    }

    /**
     * 加载单个设置项
     *
     * @param key          设置键名
     * @param defaultValue 当键不存在时的默认值
     * @return 设置值；如果键不存在则返回默认值
     */
    public static String loadSetting(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 加载整型设置项
     *
     * @param key          设置键名
     * @param defaultValue 当键不存在或格式错误时的默认值
     * @return 整数值
     */
    public static int loadIntSetting(String key, int defaultValue) {
        try {
            String val = properties.getProperty(key);
            return val != null ? Integer.parseInt(val) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 加载布尔型设置项
     *
     * @param key          设置键名
     * @param defaultValue 当键不存在时的默认值
     * @return 布尔值
     */
    public static boolean loadBooleanSetting(String key, boolean defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        return "true".equalsIgnoreCase(val.trim());
    }

    /**
     * 获取所有设置项（只读副本）
     * @return 包含所有设置的Properties对象
     */
    public static Properties getAll() {
        return new Properties(properties);  // 返回副本防止外部修改
    }

    /**
     * 将内存中的所有设置写入文件
     */
    private static void saveToFile() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "合同管理系统设置文件");
        } catch (Exception e) {
            System.err.println("[设置] 保存设置文件失败: " + e.getMessage());
        }
    }

    /**
     * 删除某个设置项
     *
     * @param key 要删除的设置键名
     */
    public static void removeSetting(String key) {
        properties.remove(key);
        saveToFile();
    }

    /**
     * 清除所有设置
     */
    public static void clearAll() {
        properties.clear();
        saveToFile();
    }

    /**
     * 获取设置文件的完整路径
     * @return 设置文件路径
     */
    public static String getSettingsFilePath() {
        return SETTINGS_FILE;
    }
}
