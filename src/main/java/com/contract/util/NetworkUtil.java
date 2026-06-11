package com.contract.util;

/**
 * 网络工具类
 * <p>获取本机网络相关信息，用于操作日志审计记录</p>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class NetworkUtil {

    /**
     * 获取本机IP地址
     * <p>使用InetAddress.getLocalHost()获取本地主机地址</p>
     *
     * @return IP地址字符串；获取失败返回"unknown"
     */
    public static String getLocalIPAddress() {
        try {
            java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取本机主机名
     *
     * @return 主机名字符串；获取失败返回"unknown"
     */
    public static String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
