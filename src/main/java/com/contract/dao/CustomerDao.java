package com.contract.dao;

import com.contract.entity.Customer;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户数据访问对象（Customer DAO）
 * <p>
 * 提供对客户表（t_customer）的数据访问操作。客户是合同签约的另一方，
 * 客户信息的统一管理有助于提高合同签订效率和数据质量。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class CustomerDao {

    /**
     * 查找所有客户
     * <p>获取客户列表，用于客户选择下拉框和管理界面</p>
     *
     * @return 所有客户列表
     */
    public List<Customer> findAll() {
        long startTime = System.currentTimeMillis();
        FileLogger.info("CustomerDao", "findAll", "开始查询所有客户");
        List<Customer> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_customer ORDER BY id");
            FileLogger.debug("CustomerDao", "findAll", "SQL=SELECT * FROM t_customer ORDER BY id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("CustomerDao", "findAll", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("CustomerDao", "findAll", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据客户编号查找客户
     *
     * @param num 客户编号
     * @return 客户对象；未找到返回null
     */
    public Customer findByNum(String num) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("CustomerDao", "findByNum", "开始根据编号查询客户, 客户编号: " + num);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_customer WHERE num = ?");
            FileLogger.debug("CustomerDao", "findByNum", "SQL=SELECT * FROM t_customer WHERE num = ?");
            pstmt.setString(1, num);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Customer c = mapCustomer(rs);
                long cost = System.currentTimeMillis() - startTime;
                FileLogger.info("CustomerDao", "findByNum", "查询成功, 找到客户: " + num + ", 耗时" + cost + "ms");
                return c;
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("CustomerDao", "findByNum", "查询完成, 未找到客户: " + num + ", 耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("CustomerDao", "findByNum", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 根据客户名称模糊查询
     * <p>支持模糊搜索，用于客户搜索功能</p>
     *
     * @param name 搜索关键词
     * @return 匹配的客户列表
     */
    public List<Customer> findByName(String name) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("CustomerDao", "findByName", "开始根据名称模糊查询客户, 关键词: " + name);
        List<Customer> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_customer WHERE name LIKE ? ORDER BY id");
            FileLogger.debug("CustomerDao", "findByName", "SQL=SELECT * FROM t_customer WHERE name LIKE ? ORDER BY id");
            pstmt.setString(1, "%" + name + "%");  // LIKE模糊匹配
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("CustomerDao", "findByName", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("CustomerDao", "findByName", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 新增客户
     *
     * @param customer 客户对象
     * @return true-成功；false-失败
     */
    public boolean insert(Customer customer) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("CustomerDao", "insert", "开始新增客户, 客户编号: " + customer.getNum() + ", 客户名称: " + customer.getName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_customer");
            pstmt = conn.prepareStatement("INSERT INTO t_customer(id, num, name, address, tel, fax, code, bank, account) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            FileLogger.debug("CustomerDao", "insert", "SQL=INSERT INTO t_customer(id, num, name, address, tel, fax, code, bank, account) VALUES(...)");
            pstmt.setInt(1, id);
            pstmt.setString(2, customer.getNum());      // 客户编号
            pstmt.setString(3, customer.getName());     // 客户名称
            pstmt.setString(4, customer.getAddress());  // 联系地址
            pstmt.setString(5, customer.getTel());      // 电话
            pstmt.setString(6, customer.getFax());      // 传真
            pstmt.setString(7, customer.getCode());     // 邮编
            pstmt.setString(8, customer.getBank());     // 开户银行
            pstmt.setString(9, customer.getAccount());  // 银行账号
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("CustomerDao", "insert", "新增客户" + (result ? "成功" : "失败") + ", 客户编号: " + customer.getNum() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("CustomerDao", "insert", "新增客户失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 更新客户信息
     * <p>根据客户编号更新客户资料</p>
     *
     * @param customer 包含更新信息的客户对象
     * @return true-成功；false-失败
     */
    public boolean update(Customer customer) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("CustomerDao", "update", "开始更新客户, 客户编号: " + customer.getNum() + ", 客户名称: " + customer.getName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("UPDATE t_customer SET name=?, address=?, tel=?, fax=?, code=?, bank=?, account=? WHERE num=?");
            FileLogger.debug("CustomerDao", "update", "SQL=UPDATE t_customer SET name=?, address=?, tel=?, fax=?, code=?, bank=?, account=? WHERE num=?");
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getAddress());
            pstmt.setString(3, customer.getTel());
            pstmt.setString(4, customer.getFax());
            pstmt.setString(5, customer.getCode());
            pstmt.setString(6, customer.getBank());
            pstmt.setString(7, customer.getAccount());
            pstmt.setString(8, customer.getNum());  // 条件-客户编号
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("CustomerDao", "update", "更新客户" + (result ? "成功" : "失败") + ", 客户编号: " + customer.getNum() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("CustomerDao", "update", "更新客户失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @return true-成功；false-失败
     */
    public boolean delete(int id) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("CustomerDao", "delete", "开始删除客户, 客户ID: " + id);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("DELETE FROM t_customer WHERE id=?");
            FileLogger.debug("CustomerDao", "delete", "SQL=DELETE FROM t_customer WHERE id=?");
            pstmt.setInt(1, id);
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("CustomerDao", "delete", "删除客户" + (result ? "成功" : "失败") + ", 客户ID: " + id + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("CustomerDao", "delete", "删除客户失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 将ResultSet映射为Customer对象的私有方法
     */
    private Customer mapCustomer(ResultSet rs) throws Exception {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setNum(rs.getString("num"));
        c.setName(rs.getString("name"));
        c.setAddress(rs.getString("address"));
        c.setTel(rs.getString("tel"));
        c.setFax(rs.getString("fax"));
        c.setCode(rs.getString("code"));
        c.setBank(rs.getString("bank"));
        c.setAccount(rs.getString("account"));
        return c;
    }
}
