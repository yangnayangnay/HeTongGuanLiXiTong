package com.contract.dao;

import com.contract.entity.Log;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志数据访问对象（Log DAO）
 * <p>
 * 提供对日志表（t_log）的数据访问操作。系统日志记录用户的操作行为，
 * 用于安全审计、问题排查和合规检查。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class LogDao {

    /**
     * 新增日志记录
     * <p>发生重要操作时调用，记录操作人和操作内容</p>
     *
     * @param log 日志对象（包含操作人、内容描述）
     * @return true-成功；false-失败
     */
    public boolean insert(Log log) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("LogDao", "insert", "开始新增日志记录, 操作人: " + log.getUserName() + ", 内容: " + log.getContent());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_log");
            pstmt = conn.prepareStatement("INSERT INTO t_log(id, userName, content, time, ip_address, old_value, new_value) VALUES(?, ?, ?, ?, ?, ?, ?)");
            FileLogger.debug("LogDao", "insert", "SQL=INSERT INTO t_log(id, userName, content, time, ip_address, old_value, new_value) VALUES(...)");
            pstmt.setInt(1, id);
            pstmt.setString(2, log.getUserName());  // 操作人
            pstmt.setString(3, log.getContent());   // 操作描述
            // 日志时间使用当前系统时间，确保准确性
            pstmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            // 审计增强字段：IP地址、变更前值、变更后值
            pstmt.setString(5, log.getIpAddress());
            pstmt.setString(6, log.getOldValue());
            pstmt.setString(7, log.getNewValue());
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("LogDao", "insert", "新增日志记录" + (result ? "成功" : "失败") + ", 操作人: " + log.getUserName() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("LogDao", "insert", "新增日志记录失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 查找所有日志记录
     * <p>按时间倒序排列，最新的日志在前</p>
     * <p>用于日志管理界面展示</p>
     *
     * @return 日志记录列表（按时间降序）
     */
    public List<Log> findAll() {
        long startTime = System.currentTimeMillis();
        FileLogger.info("LogDao", "findAll", "开始查询所有日志记录");
        List<Log> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 按ID倒序使最新日志排在前面
            pstmt = conn.prepareStatement("SELECT * FROM t_log ORDER BY id DESC");
            FileLogger.debug("LogDao", "findAll", "SQL=SELECT * FROM t_log ORDER BY id DESC");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Log log = new Log();
                log.setId(rs.getInt("id"));
                log.setUserName(rs.getString("userName"));
                log.setContent(rs.getString("content"));
                log.setTime(rs.getTimestamp("time"));  // 使用Timestamp保留完整时间精度
                // 读取审计增强字段
                log.setIpAddress(rs.getString("ip_address"));
                log.setOldValue(rs.getString("old_value"));
                log.setNewValue(rs.getString("new_value"));
                list.add(log);
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("LogDao", "findAll", "查询完成，共" + list.size() + "条日志记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("LogDao", "findAll", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
}
