package com.contract.dao;

import com.contract.entity.User;
import com.contract.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象（User DAO）
 * <p>
 * 提供对用户表（t_user）的数据访问操作，包括用户的增删改查、
 * 状态更新等功能。采用JDBC原生方式实现数据库交互。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>用户登录验证：根据用户名查询用户信息</li>
 *   <li>用户列表管理：查询所有用户或待审核用户</li>
 *   <li>用户信息维护：新增、修改、删除用户</li>
 *   <li>状态管理：审核通过/拒绝用户注册申请</li>
 * </ul>
 *
 * <h3>数据库表结构：</h3>
 * <pre>
 * 表名：t_user
 * 字段：id(主键), name(用户名), password(密码), status(状态)
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class UserDao {

    /**
     * 根据用户名查找用户
     * <p>用于用户登录验证，根据用户名精确匹配查询用户信息</p>
     *
     * @param name 用户登录名称
     * @return 找到的用户对象；如果未找到则返回null
     */
    public User findByName(String name) {
        Connection conn = null;          // 数据库连接对象
        PreparedStatement pstmt = null;  // 预编译语句对象，防止SQL注入
        ResultSet rs = null;             // 结果集对象
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            // 准备SQL查询语句，使用参数化查询防止SQL注入攻击
            pstmt = conn.prepareStatement("SELECT * FROM t_user WHERE name = ?");
            pstmt.setString(1, name);  // 设置查询参数：用户名
            rs = pstmt.executeQuery();  // 执行查询
            if (rs.next()) {
                // 将结果集映射为User对象并返回
                User user = new User(rs.getInt("id"), rs.getString("name"),
                    rs.getString("password"), rs.getInt("status"));
                user.setEmail(rs.getString("email"));  // 设置邮箱字段
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();  // 打印异常堆栈信息，便于调试
        } finally {
            // 确保资源被正确释放，避免连接泄漏
            DBUtil.close(conn, pstmt, rs);
        }
        return null;  // 未找到用户返回null
    }

    /**
     * 查找所有用户
     * <p>获取系统中所有用户的列表，按ID升序排列</p>
     * <p>主要用于用户管理界面展示和用户选择下拉框填充</p>
     *
     * @return 用户列表（可能为空列表，但不会为null）
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();  // 用于存储查询结果的用户列表
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 查询所有用户，按id排序确保结果稳定
            pstmt = conn.prepareStatement("SELECT * FROM t_user ORDER BY id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                // 遍历结果集，将每条记录转换为User对象添加到列表
                User user = new User(rs.getInt("id"), rs.getString("name"),
                    rs.getString("password"), rs.getInt("status"));
                user.setEmail(rs.getString("email"));  // 设置邮箱字段
                list.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 查找待审核用户
     * <p>获取所有状态为"待审核"（status=0）的用户列表</p>
     * <p>用于管理员审核界面，显示等待审批的新注册用户</p>
     *
     * @return 待审核用户列表
     */
    public List<User> findPending() {
        List<User> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 只查询状态为0（待审核）的用户
            pstmt = conn.prepareStatement("SELECT * FROM t_user WHERE status = 0 ORDER BY id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("name"),
                    rs.getString("password"), rs.getInt("status"));
                user.setEmail(rs.getString("email"));  // 设置邮箱字段
                list.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 新增用户
     * <p>向数据库插入一条新的用户记录</p>
     * <p>用户ID由序列自动生成，确保唯一性</p>
     *
     * @param user 要新增的用户对象（包含用户名、密码、状态等信息）
     * @return true-插入成功；false-插入失败
     */
    public boolean insert(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 获取下一个自增ID，保证主键唯一
            int id = DBUtil.getNextId("seq_user");
            // 插入新用户记录，使用参数化防止SQL注入
            pstmt = conn.prepareStatement("INSERT INTO t_user(id, name, password, status, email) VALUES(?, ?, ?, ?, ?)");
            pstmt.setInt(1, id);                    // 参数1：用户ID
            pstmt.setString(2, user.getName());      // 参数2：用户名
            pstmt.setString(3, user.getPassword());  // 参数3：密码
            pstmt.setInt(4, user.getStatus());       // 参数4：状态
            pstmt.setString(5, user.getEmail());      // 参数5：邮箱
            // executeUpdate返回受影响的行数，大于0表示插入成功
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 更新用户信息
     * <p>根据用户ID更新用户的基本信息（用户名、密码、状态）</p>
     *
     * @param user 包含更新后信息的用户对象（必须包含有效的ID）
     * @return true-更新成功；false-更新失败
     */
    public boolean update(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 根据ID更新用户信息
            pstmt = conn.prepareStatement("UPDATE t_user SET name=?, password=?, status=?, email=? WHERE id=?");
            pstmt.setString(1, user.getName());      // 参数1：新用户名
            pstmt.setString(2, user.getPassword());  // 参数2：新密码
            pstmt.setInt(3, user.getStatus());       // 参数3：新状态
            pstmt.setString(4, user.getEmail());      // 参数4：新邮箱
            pstmt.setInt(5, user.getId());           // 参数5：条件-用户ID
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 更新用户审核状态
     * <p>单独更新用户的状态字段，用于管理员审批操作</p>
     * <p>相比update方法更轻量，只修改status字段</p>
     *
     * @param id     用户ID
     * @param status 新的状态值（0-待审核, 1-已通过, 2-已拒绝）
     * @return true-更新成功；false-更新失败
     */
    public boolean updateStatus(int id, int status) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 只更新status字段，保持其他信息不变
            pstmt = conn.prepareStatement("UPDATE t_user SET status=? WHERE id=?");
            pstmt.setInt(1, status);  // 参数1：新状态值
            pstmt.setInt(2, id);      // 参数2：条件-用户ID
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 删除用户
     * <p>根据用户ID删除用户记录</p>
     * <p>注意：删除前应检查是否有关联的合同或权限记录</p>
     *
     * @param id 要删除的用户ID
     * @return true-删除成功；false-删除失败
     */
    public boolean delete(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 根据主键ID删除用户
            pstmt = conn.prepareStatement("DELETE FROM t_user WHERE id=?");
            pstmt.setInt(1, id);  // 设置要删除的用户ID
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }
}
