package com.contract.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 国际化工具类
 * <p>管理系统多语言资源文件的加载和切换，支持中英文双语界面</p>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class I18NUtil {

    /** 资源包对象（缓存当前语言的翻译文本） */
    private static ResourceBundle bundle;
    /** 当前语言环境设置 */
    private static Locale currentLocale = Locale.CHINESE;
    /** 资源包基础名称（对应i18n目录下的messages_*.properties文件） */
    private static final String BASE_NAME = "i18n.messages";

    // 静态初始化块：加载默认语言资源
    static { reloadBundle(); }

    /**
     * 切换系统显示语言
     *
     * @param locale 目标语言环境（Locale.CHINESE 或 Locale.US）
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        reloadBundle();
        // 保存用户语言偏好到本地设置文件
        AppSettingsUtil.saveSetting("locale", locale.toString());
    }

    /**
     * 获取当前使用的语言环境
     *
     * @return 当前Locale对象
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * 加载/重新加载资源包
     * <p>根据currentLocale加载对应语言的properties文件</p>
     */
    private static void reloadBundle() {
        try {
            bundle = ResourceBundle.getBundle(BASE_NAME, currentLocale);
        } catch (Exception e) {
            // 加载失败时回退到中文
            System.err.println("[国际化] 加载资源包失败: " + e.getMessage() + "，回退到中文");
            bundle = ResourceBundle.getBundle(BASE_NAME, Locale.CHINESE);
        }
    }

    /**
     * 根据key获取翻译后的文本
     *
     * @param key 资源文件中的键名（如"app.title"、"btn.confirm"等）
     * @return 翻译后的文本；如果key不存在则返回[key]格式提示
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // key不存在时返回[key]格式，便于排查缺失的翻译
            return "[" + key + "]";
        }
    }

    /**
     * 从本地设置中加载保存的语言偏好
     * <p>在应用启动时调用，恢复用户上次选择的语言</p>
     */
    public static void loadSavedLocale() {
        String saved = AppSettingsUtil.loadSetting("locale", "zh_CN");
        if ("en_US".equals(saved)) {
            setLocale(Locale.US);
        } else {
            setLocale(Locale.CHINESE);
        }
    }
}
