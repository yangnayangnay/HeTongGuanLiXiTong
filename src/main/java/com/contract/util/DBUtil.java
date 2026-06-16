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
 * 采用Oracle数据库，使用JDBC原生方式连接。
 * </p>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class DBUtil {
    /** Oracle JDBC驱动类名 */
    private static final String DRIVER = "oracle.jdbc.OracleDriver";
    /** 数据库连接URL */
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/freepdb1";
    /** 数据库用户名 */
    private static final String USER = "scott";
    /** 数据库密码 */
    private static final String PASSWORD = "tiger";

    /**
     * 静态初始化块：加载JDBC驱动
     */
    static {
        try {
            Class.forName(DRIVER);
            FileLogger.info("DBUtil", "static", "Oracle JDBC驱动加载成功: " + DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            FileLogger.error("DBUtil", "static", "Oracle驱动加载失败: " + e.getMessage());
            throw new RuntimeException("Oracle驱动加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        FileLogger.debug("DBUtil", "getConnection", "获取数据库连接成功");
        return conn;
    }

    /**
     * 关闭所有数据库资源
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); FileLogger.warn("DBUtil", "close", "关闭ResultSet异常: " + e.getMessage()); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); FileLogger.warn("DBUtil", "close", "关闭Statement异常: " + e.getMessage()); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); FileLogger.warn("DBUtil", "close", "关闭Connection异常: " + e.getMessage()); }
    }

    /**
     * 关闭数据库资源（无结果集版本）
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    /**
     * 获取指定序列的下一个值
     * <p>Oracle使用序列（Sequence）生成唯一主键</p>
     *
     * @param sequenceName 序列名（如："seq_user"、"seq_contract"等）
     * @return 下一个ID值；失败返回-1
     */
    public static int getNextId(String sequenceName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement("SELECT " + sequenceName + ".NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int nextId = rs.getInt(1);
                FileLogger.debug("DBUtil", "getNextId", "获取下一个ID成功: " + sequenceName + " = " + nextId);
                return nextId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            FileLogger.error("DBUtil", "getNextId", "获取下一个ID失败: " + sequenceName + ", 错误: " + e.getMessage());
        } finally {
            close(conn, pstmt, rs);
        }
        return -1;
    }
}
