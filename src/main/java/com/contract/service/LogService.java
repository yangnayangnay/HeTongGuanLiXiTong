package com.contract.service;

import com.contract.dao.LogDao;
import com.contract.entity.Log;

import java.util.List;

/**
 * 日志业务逻辑类（Log Service）
 * <p>
 * 提供日志记录和查询的业务接口。封装日志DAO的操作，
 * 为其他Service提供统一的日志记录方法。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class LogService {
    /** 日志数据访问对象 */
    private LogDao logDao = new LogDao();

    /**
     * 获取所有日志记录
     * <p>按时间倒序排列，最新日志在前</p>
     *
     * @return 日志记录列表
     *
     * [REST-API] GET /api/logs
     */
    public List<Log> findAll() {
        return logDao.findAll();
    }

    /**
     * 添加日志记录
     * <p>便捷方法，简化日志记录调用</p>
     *
     * @param userName 操作人用户名
     * @param content 日志内容描述
     * @return true-记录成功；false-记录失败
     */
    public boolean addLog(String userName, String content) {
        Log log = new Log();
        log.setUserName(userName);
        log.setContent(content);
        return logDao.insert(log);
    }
}
