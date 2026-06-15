package com.contract.dao;

import com.contract.entity.Function;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能数据访问对象（Function DAO）
 * <p>
 * 提供对功能表（t_function）的数据访问操作。功能是系统的最小权限单元，
 * 对应具体的菜单项或操作按钮。功能的增删通常由系统初始化完成，
 * 运行期间较少变动。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>功能列表管理：查询系统中定义的所有功能点</li>
 *   <li>功能维护：新增、删除功能（通常由管理员操作）</li>
 *   <li>为角色分配提供可选的功能列表</li>
 * </ul>
 *
 * <h3>数据库表结构：</h3>
 * <pre>
 * 表名：t_function
 * 字段：id(主键), num(编号), name(名称), url(地址), description(描述)
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class FunctionDao {

    /**
     * 查找所有功能
     * <p>获取系统中定义的所有功能点列表，按ID升序排列</p>
     * <p>用于构建系统菜单树和角色权限配置界面</p>
     *
     * @return 功能列表（可能为空列表，但不会为null）
     */
    public List<Function> findAll() {
        long startTime = System.currentTimeMillis();
        FileLogger.info("FunctionDao", "findAll", "开始查询所有功能");
        List<Function> list = new ArrayList<>();  // 用于存储查询结果的功能列表
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 查询所有功能，按id排序
            pstmt = conn.prepareStatement("SELECT * FROM t_function ORDER BY id");
            FileLogger.debug("FunctionDao", "findAll", "SQL=SELECT * FROM t_function ORDER BY id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                // 将结果集映射为Function对象
                list.add(new Function(rs.getInt("id"), rs.getString("num"),
                        rs.getString("name"), rs.getString("url"), rs.getString("description")));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("FunctionDao", "findAll", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("FunctionDao", "findAll", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 新增功能
     * <p>向数据库插入一条新的功能记录</p>
     * <p>功能ID由序列自动生成</p>
     *
     * @param func 要新增的功能对象（包含编号、名称、URL、描述等）
     * @return true-插入成功；false-插入失败
     */
    public boolean insert(Function func) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("FunctionDao", "insert", "开始新增功能, 功能编号: " + func.getNum() + ", 功能名称: " + func.getName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 获取下一个自增ID
            int id = DBUtil.getNextId("seq_function");
            // 插入新功能记录
            pstmt = conn.prepareStatement("INSERT INTO t_function(id, num, name, url, description) VALUES(?, ?, ?, ?, ?)");
            FileLogger.debug("FunctionDao", "insert", "SQL=INSERT INTO t_function(id, num, name, url, description) VALUES(...)");
            pstmt.setInt(1, id);               // 参数1：功能ID
            pstmt.setString(2, func.getNum());  // 参数2：功能编号
            pstmt.setString(3, func.getName()); // 参数3：功能名称
            pstmt.setString(4, func.getUrl());  // 参数4：功能URL
            pstmt.setString(5, func.getDescription()); // 参数5：功能描述
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("FunctionDao", "insert", "新增功能" + (result ? "成功" : "失败") + ", 功能编号: " + func.getNum() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("FunctionDao", "insert", "新增功能失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 删除功能
     * <p>根据功能ID删除功能记录</p>
     * <p>注意：删除功能前需确认没有角色正在使用此功能</p>
     *
     * @param id 要删除的功能ID
     * @return true-删除成功；false-删除失败
     */
    public boolean delete(int id) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("FunctionDao", "delete", "开始删除功能, 功能ID: " + id);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 根据主键ID删除功能
            pstmt = conn.prepareStatement("DELETE FROM t_function WHERE id=?");
            FileLogger.debug("FunctionDao", "delete", "SQL=DELETE FROM t_function WHERE id=?");
            pstmt.setInt(1, id);  // 设置要删除的功能ID
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("FunctionDao", "delete", "删除功能" + (result ? "成功" : "失败") + ", 功能ID: " + id + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("FunctionDao", "delete", "删除功能失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }
}
