package com.contract.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库连接工具类（DBUtil）
 * <p>
 * 封装数据库连接的获取和释放操作，提供统一的数据库访问入口。
 * 采用MySQL数据库，使用JDBC原生方式连接。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>数据库连接管理：获取、释放数据库连接</li>
 *   <li>资源清理：统一关闭Connection、Statement、ResultSet</li>
 *   <li>ID生成：通过MySQL AUTO_INCREMENT机制生成唯一主键</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class DBUtil {
    /** MySQL JDBC驱动类名 */
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    /** 数据库连接URL */
    private static final String URL = "jdbc:mysql://localhost:3306/contract_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
    /** 数据库用户名 */
    private static final String USER = "root";
    /** 数据库密码 */
    private static final String PASSWORD = "root";

    /**
     * 静态初始化块：加载JDBC驱动
     */
    static {
        try {
            Class.forName(DRIVER);
            FileLogger.info("DBUtil", "static", "MySQL JDBC驱动加载成功: " + DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            FileLogger.error("DBUtil", "static", "MySQL驱动加载失败: " + e.getMessage());
            throw new RuntimeException("MySQL驱动加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     *
     * @return Connection对象
     * @throws SQLException 数据库连接失败时抛出异常
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        FileLogger.debug("DBUtil", "getConnection", "获取数据库连接成功");
        return conn;
    }

    /**
     * 关闭所有数据库资源（完整版）
     *
     * @param conn 数据库连接对象（可为null）
     * @param stmt SQL语句对象（可为null）
     * @param rs   结果集对象（可为null）
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); FileLogger.warn("DBUtil", "close", "关闭ResultSet异常: " + e.getMessage()); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); FileLogger.warn("DBUtil", "close", "关闭Statement异常: " + e.getMessage()); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); FileLogger.warn("DBUtil", "close", "关闭Connection异常: " + e.getMessage()); }
    }

    /**
     * 关闭数据库资源（无结果集版本）
     *
     * @param conn 数据库连接对象
     * @param stmt SQL语句对象
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    /**
     * 获取指定表的下一个自增ID
     * <p>MySQL使用AUTO_INCREMENT，此方法通过查询当前最大ID+1来获取下一个ID值。
     * 注意：实际插入时建议让MySQL自动生成ID，此方法主要用于兼容原有代码逻辑。</p>
     *
     * @param tableName 表名（如："t_user"、"t_contract"等，对应原序列名seq_xxx的xxx部分）
     * @return 下一个ID值；失败返回-1
     */
    public static int getNextId(String tableName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // 将seq_xxx转换为t_xxx表名
            String actualTable = tableName;
            if (tableName.startsWith("seq_")) {
                actualTable = "t_" + tableName.substring(4);
            }
            pstmt = conn.prepareStatement("SELECT IFNULL(MAX(id), 0) + 1 FROM " + actualTable);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int nextId = rs.getInt(1);
                FileLogger.debug("DBUtil", "getNextId", "获取下一个ID成功: " + tableName + " = " + nextId);
                return nextId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            FileLogger.error("DBUtil", "getNextId", "获取下一个ID失败: " + tableName + ", 错误: " + e.getMessage());
        } finally {
            close(conn, pstmt, rs);
        }
        return -1;
    }
}
