package com.contract.dao;

import com.contract.entity.Right;
import com.contract.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限数据访问对象（Right DAO）
 * <p>
 * 提供对权限表（t_right）的数据访问操作。权限表实现了用户与角色的多对多关系，
 * 是RBAC权限模型的核心关联表。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>用户权限查询：查询某个用户拥有的所有角色</li>
 *   <li>权限列表管理：查看所有的用户-角色分配关系</li>
 *   <li>权限分配：为用户分配新的角色</li>
 *   <li>权限回收：移除用户的所有角色（通常在删除用户前调用）</li>
 * </ul>
 *
 * <h3>数据库表结构：</h3>
 * <pre>
 * 表名：t_right
 * 字段：id(主键), userName(用户名), roleName(角色名), description(备注)
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class RightDao {

    /**
     * 根据用户名查询用户拥有的所有角色
     * <p>用于登录时加载用户权限，或在用户详情页展示其角色列表</p>
     *
     * @param userName 用户名称
     * @return 该用户拥有的权限（角色）列表
     */
    public List<Right> findByUserName(String userName) {
        List<Right> list = new ArrayList<>();  // 用于存储查询结果的权限列表
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 按用户名查询其所有角色分配记录
            pstmt = conn.prepareStatement("SELECT * FROM t_right WHERE userName = ?");
            pstmt.setString(1, userName);  // 设置用户名参数
            rs = pstmt.executeQuery();
            while (rs.next()) {
                // 将结果集映射为Right对象
                list.add(new Right(rs.getInt("id"), rs.getString("userName"),
                        rs.getString("roleName"), rs.getString("description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 查找所有权限记录
     * <p>获取系统中所有的用户-角色分配关系，按ID升序排列</p>
     * <p>用于权限管理界面的全局视图展示</p>
     *
     * @return 所有权限记录的列表
     */
    public List<Right> findAll() {
        List<Right> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 查询所有权限记录
            pstmt = conn.prepareStatement("SELECT * FROM t_right ORDER BY id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Right(rs.getInt("id"), rs.getString("userName"),
                        rs.getString("roleName"), rs.getString("description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 新增权限分配记录
     * <p>为指定用户分配一个角色，建立用户-角色关联关系</p>
     * <p>一个用户可以有多个权限记录（多个角色）</p>
     *
     * @param right 权限对象（包含用户名、角色名、备注等信息）
     * @return true-插入成功；false-插入失败
     */
    public boolean insert(Right right) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 获取下一个自增ID
            int id = DBUtil.getNextId("seq_right");
            // 插入新的权限分配记录
            pstmt = conn.prepareStatement("INSERT INTO t_right(id, userName, roleName, description) VALUES(?, ?, ?, ?)");
            pstmt.setInt(1, id);                   // 参数1：权限记录ID
            pstmt.setString(2, right.getUserName());   // 参数2：用户名
            pstmt.setString(3, right.getRoleName());   // 参数3：角色名
            pstmt.setString(4, right.getDescription());// 参数4：备注说明
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 删除指定用户的所有权限记录
     * <p>移除某用户的所有角色分配，通常在以下场景调用：</p>
 * <ul>
     *   <li>删除用户之前，先清理其权限关联</li>
     *   <li>重新分配角色时，先清除原有角色</li>
     * </ul>
     *
     * @param userName 要清除权限的用户名
     * @return true-删除成功（包括无记录可删的情况）；false-删除失败
     */
    public boolean deleteByUserName(String userName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 删除指定用户的所有权限记录
            pstmt = conn.prepareStatement("DELETE FROM t_right WHERE userName=?");
            pstmt.setString(1, userName);  // 设置用户名参数
            // 使用>=0判断，因为无记录删除时返回0也算成功
            return pstmt.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }
}
