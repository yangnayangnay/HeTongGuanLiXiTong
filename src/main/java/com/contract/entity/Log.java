package com.contract.entity;

import java.util.Date;

/**
 * 系统日志实体类
 * <p>
 * 用于记录合同管理系统中的操作日志和审计信息。系统日志对于安全审计、
 * 问题排查、责任追溯等方面具有重要作用，是企业级系统的必备功能。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>记录用户的登录、登出行为</li>
 *   <li>追踪关键业务操作（合同创建、修改、审批等）</li>
 *   <li>记录系统异常和错误信息</li>
 *   <li>支持操作历史的查询和统计</li>
 * </ul>
 *
 * <h3>日志级别分类：</h3>
 * <ul>
 *   <li>操作日志：记录正常的业务操作</li>
 *   <li>安全日志：记录登录、权限变更等安全相关事件</li>
 *   <li>异常日志：记录系统错误和异常情况</li>
 * </ul>
 *
 * <h3>合规要求：</h3>
 * <p>
 * 根据企业内控和审计要求，重要操作必须留痕，日志不可删除或篡改。
 * 日志应保留足够长的时间以满足合规审查需求。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class Log {
    /** 日志记录唯一标识符，自增主键 */
    private int id;
    /**
     * 操作人用户名
     * <p>对应User表的name字段，标识执行操作的用户</p>
     * <p>对于系统自动触发的操作，可能为"SYSTEM"</p>
     */
    private String userName;
    /**
     * 日志内容/操作描述
     * <p>详细记录执行的操作内容</p>
     * <p>例如："用户张三登录系统"、"审批合同HT2024001（通过）"</p>
     */
    private String content;
    /**
     * 操作发生时间
     * <p>精确到秒的时间戳，用于事件排序和时间线分析</p>
     */
    private Date time;
    /**
     * 操作者IP地址
     * <p>记录执行操作时的客户端IP地址，用于安全审计和追踪</p>
     */
    private String ipAddress;
    /**
     * 操作前的值（变更审计）
     * <p>用于记录操作前数据的状态，便于追踪数据变化</p>
     */
    private String oldValue;
    /**
     * 操作后的值（变更审计）
     * <p>用于记录操作后数据的状态，与oldValue配合可还原变更</p>
     */
    private String newValue;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public Log() {}

    /**
     * 完整构造方法
     * <p>创建日志对象并设置所有属性</p>
     *
     * @param id      日志记录唯一标识
     * @param userName 操作人用户名
     * @param content 日志内容描述
     * @param time    操作时间
     */
    public Log(int id, String userName, String content, Date time) {
        this.id = id;
        this.userName = userName;
        this.content = content;
        this.time = time;
    }

    /**
     * 获取日志记录ID
     * @return 日志记录唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置日志记录ID
     * @param id 日志记录唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取操作人用户名
     * @return 用户名字符串
     */
    public String getUserName() { return userName; }

    /**
     * 设置操作人用户名
     * @param userName 用户名字符串
     */
    public void setUserName(String userName) { this.userName = userName; }

    /**
     * 获取日志内容
     * @return 日志描述内容
     */
    public String getContent() { return content; }

    /**
     * 设置日志内容
     * @param content 日志描述内容
     */
    public void setContent(String content) { this.content = content; }

    /**
     * 获取操作时间
     * @return 时间戳
     */
    public Date getTime() { return time; }

    /**
     * 设置操作时间
     * @param time 时间戳
     */
    public void setTime(Date time) { this.time = time; }

    /**
     * 获取操作者IP地址
     * @return IP地址字符串
     */
    public String getIpAddress() { return ipAddress; }

    /**
     * 设置操作者IP地址
     * @param ipAddress IP地址字符串
     */
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    /**
     * 获取变更前的值
     * @return 变更前的值
     */
    public String getOldValue() { return oldValue; }

    /**
     * 设置变更前的值
     * @param oldValue 变更前的值
     */
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    /**
     * 获取变更后的值
     * @return 变更后的值
     */
    public String getNewValue() { return newValue; }

    /**
     * 设置变更后的值
     * @param newValue 变更后的值
     */
    public void setNewValue(String newValue) { this.newValue = newValue; }
}
