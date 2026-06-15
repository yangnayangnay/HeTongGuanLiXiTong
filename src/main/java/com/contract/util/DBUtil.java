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
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>数据库连接管理：获取、释放数据库连接</li>
 *   <li>资源清理：统一关闭Connection、Statement、ResultSet</li>
 *   <li>序列值获取：通过Oracle序列生成唯一主键</li>
 * </ul>
 *
 * <h3>使用说明：</h3>
 * <ul>
 *   <li>所有DAO层通过此类获取数据库连接</li>
 *   <li>使用完毕后必须调用close方法释放资源</li>
 *   <li>序列用于生成各表的自增主键ID</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class DBUtil {
    /** Oracle JDBC驱动类名 */
    private static final String DRIVER = "oracle.jdbc.OracleDriver";
    /** 数据库连接URL（格式：jdbc:oracle:thin:@//主机:端口/服务名） */
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/freepdb1";
    /** 数据库用户名 */
    private static final String USER = "scott";
    /** 数据库密码 */
    private static final String PASSWORD = "tiger";

    /**
     * 静态初始化块：加载JDBC驱动
     * <p>在类首次被加载时执行，确保驱动程序只加载一次</p>
     */
    static {
        try {
            Class.forName(DRIVER);  // 加载并注册Oracle JDBC驱动
            FileLogger.info("DBUtil", "static", "Oracle JDBC驱动加载成功: " + DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            FileLogger.error("DBUtil", "static", "Oracle驱动加载失败: " + e.getMessage());
            throw new RuntimeException("Oracle驱动加载失败", e);  // 驱动加载失败则系统无法运行
        }
    }

    /**
     * 获取数据库连接
     * <p>每次调用创建一个新的数据库连接</p>
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
     * <p>按照ResultSet → Statement → Connection的顺序关闭</p>
     * <p>每个资源的关闭操作独立try-catch，确保一个资源关闭失败不影响其他资源</p>
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
     * <p>用于INSERT/UPDATE/DELETE等不需要结果集的操作</p>
     *
     * @param conn 数据库连接对象
     * @param stmt SQL语句对象
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);  // 委托给三参数版本，rs传null
    }

    /**
     * 获取Oracle序列的下一个值
     * <p>用于生成表的主键ID，保证全局唯一性</p>
     * <p>Oracle序列是线程安全的，支持高并发场景</p>
     *
     * @param sequenceName 序列名称（如："seq_user"、"seq_contract"等）
     * @return 序列的下一个值；失败返回-1
     */
    public static int getNextId(String sequenceName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // Oracle语法：从dual虚拟表查询序列的下一个值
            pstmt = conn.prepareStatement("SELECT " + sequenceName + ".NEXTVAL FROM dual");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int nextId = rs.getInt(1);
                FileLogger.debug("DBUtil", "getNextId", "获取序列值成功: " + sequenceName + " = " + nextId);
                return nextId;  // 返回第一列（序列值）
            }
        } catch (SQLException e) {
            e.printStackTrace();
            FileLogger.error("DBUtil", "getNextId", "获取序列值失败: " + sequenceName + ", 错误: " + e.getMessage());
        } finally {
            close(conn, pstmt, rs);  // 确保资源释放
        }
        return -1;  // 异常情况返回-1表示失败
    }
}
