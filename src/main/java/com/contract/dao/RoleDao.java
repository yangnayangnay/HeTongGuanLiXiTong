package com.contract.dao;

import com.contract.entity.Role;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色数据访问对象（Role DAO）
 * <p>
 * 提供对角色表（t_role）的数据访问操作，包括角色的增删改查等功能。
 * 角色是权限管理的核心，每个角色关联一组系统功能权限。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>角色列表管理：查询系统中定义的所有角色</li>
 *   <li>角色查询：根据角色名称查找特定角色</li>
 *   <li>角色维护：新增、修改、删除角色</li>
 * </ul>
 *
 * <h3>数据库表结构：</h3>
 * <pre>
 * 表名：t_role
 * 字段：id(主键), name(角色名), description(描述), functions(功能ID列表)
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class RoleDao {

    /**
     * 查找所有角色
     * <p>获取系统中定义的所有角色列表，按ID升序排列</p>
     * <p>用于角色管理界面展示和角色分配下拉框填充</p>
     *
     * @return 角色列表（可能为空列表，但不会为null）
     */
    public List<Role> findAll() {
        long startTime = System.currentTimeMillis();
        FileLogger.info("RoleDao", "findAll", "开始查询所有角色");
        List<Role> list = new ArrayList<>();  // 用于存储查询结果的角色列表
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 查询所有角色，按id排序
            pstmt = conn.prepareStatement("SELECT * FROM t_role ORDER BY id");
            FileLogger.debug("RoleDao", "findAll", "SQL=SELECT * FROM t_role ORDER BY id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                // 将结果集映射为Role对象
                list.add(new Role(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getString("functions")));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("RoleDao", "findAll", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("RoleDao", "findAll", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据角色名称查找角色
     * <p>用于检查角色是否已存在，或获取特定角色的详细信息</p>
     *
     * @param name 角色名称
     * @return 找到的角色对象；如果未找到则返回null
     */
    public Role findByName(String name) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("RoleDao", "findByName", "开始根据名称查询角色, 角色名: " + name);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 按角色名精确查询
            pstmt = conn.prepareStatement("SELECT * FROM t_role WHERE name = ?");
            FileLogger.debug("RoleDao", "findByName", "SQL=SELECT * FROM t_role WHERE name = ?");
            pstmt.setString(1, name);  // 设置角色名参数
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Role role = new Role(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getString("functions"));
                long cost = System.currentTimeMillis() - startTime;
                FileLogger.info("RoleDao", "findByName", "查询成功, 找到角色: " + name + ", 耗时" + cost + "ms");
                return role;
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("RoleDao", "findByName", "查询完成, 未找到角色: " + name + ", 耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("RoleDao", "findByName", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;  // 未找到返回null
    }

    /**
     * 新增角色
     * <p>向数据库插入一条新的角色记录</p>
     * <p>角色ID由序列自动生成，functions字段存储该角色拥有的功能ID列表</p>
     *
     * @param role 要新增的角色对象（包含角色名、描述、功能列表等）
     * @return true-插入成功；false-插入失败
     */
    public boolean insert(Role role) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("RoleDao", "insert", "开始新增角色, 角色名: " + role.getName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 获取下一个自增ID
            int id = DBUtil.getNextId("seq_role");
            // 插入新角色记录
            pstmt = conn.prepareStatement("INSERT INTO t_role(id, name, description, functions) VALUES(?, ?, ?, ?)");
            FileLogger.debug("RoleDao", "insert", "SQL=INSERT INTO t_role(id, name, description, functions) VALUES(...)");
            pstmt.setInt(1, id);                       // 参数1：角色ID
            pstmt.setString(2, role.getName());         // 参数2：角色名称
            pstmt.setString(3, role.getDescription());  // 参数3：角色描述
            pstmt.setString(4, role.getFunctions());    // 参数4：功能ID列表
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("RoleDao", "insert", "新增角色" + (result ? "成功" : "失败") + ", 角色名: " + role.getName() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("RoleDao", "insert", "新增角色失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 更新角色信息
     * <p>根据角色ID更新角色的基本信息</p>
     * <p>通常在修改角色权限分配时调用</p>
     *
     * @param role 包含更新后信息的角色对象（必须包含有效的ID）
     * @return true-更新成功；false-更新失败
     */
    public boolean update(Role role) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("RoleDao", "update", "开始更新角色, 角色ID: " + role.getId() + ", 角色名: " + role.getName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 根据ID更新角色信息
            pstmt = conn.prepareStatement("UPDATE t_role SET name=?, description=?, functions=? WHERE id=?");
            FileLogger.debug("RoleDao", "update", "SQL=UPDATE t_role SET name=?, description=?, functions=? WHERE id=?");
            pstmt.setString(1, role.getName());         // 参数1：新角色名
            pstmt.setString(2, role.getDescription());  // 参数2：新描述
            pstmt.setString(3, role.getFunctions());    // 参数3：新功能列表
            pstmt.setInt(4, role.getId());              // 参数4：条件-角色ID
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("RoleDao", "update", "更新角色" + (result ? "成功" : "失败") + ", 角色ID: " + role.getId() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("RoleDao", "update", "更新角色失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 删除角色
     * <p>根据角色ID删除角色记录</p>
     * <p>注意：删除前应先清理该角色与用户的关联关系（t_right表）</p>
     *
     * @param id 要删除的角色ID
     * @return true-删除成功；false-删除失败
     */
    public boolean delete(int id) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("RoleDao", "delete", "开始删除角色, 角色ID: " + id);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 根据主键ID删除角色
            pstmt = conn.prepareStatement("DELETE FROM t_role WHERE id=?");
            FileLogger.debug("RoleDao", "delete", "SQL=DELETE FROM t_role WHERE id=?");
            pstmt.setInt(1, id);  // 设置要删除的角色ID
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("RoleDao", "delete", "删除角色" + (result ? "成功" : "失败") + ", 角色ID: " + id + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("RoleDao", "delete", "删除角色失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }
}
