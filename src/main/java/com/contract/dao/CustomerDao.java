package com.contract.dao;

import com.contract.entity.Customer;
import com.contract.util.DBUtil;

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
        List<Customer> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_customer ORDER BY id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        } catch (Exception e) {
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_customer WHERE num = ?");
            pstmt.setString(1, num);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapCustomer(rs);
            }
        } catch (Exception e) {
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
        List<Customer> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_customer WHERE name LIKE ? ORDER BY id");
            pstmt.setString(1, "%" + name + "%");  // LIKE模糊匹配
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        } catch (Exception e) {
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_customer");
            pstmt = conn.prepareStatement("INSERT INTO t_customer(id, num, name, address, tel, fax, code, bank, account) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, customer.getNum());      // 客户编号
            pstmt.setString(3, customer.getName());     // 客户名称
            pstmt.setString(4, customer.getAddress());  // 联系地址
            pstmt.setString(5, customer.getTel());      // 电话
            pstmt.setString(6, customer.getFax());      // 传真
            pstmt.setString(7, customer.getCode());     // 邮编
            pstmt.setString(8, customer.getBank());     // 开户银行
            pstmt.setString(9, customer.getAccount());  // 银行账号
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("UPDATE t_customer SET name=?, address=?, tel=?, fax=?, code=?, bank=?, account=? WHERE num=?");
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getAddress());
            pstmt.setString(3, customer.getTel());
            pstmt.setString(4, customer.getFax());
            pstmt.setString(5, customer.getCode());
            pstmt.setString(6, customer.getBank());
            pstmt.setString(7, customer.getAccount());
            pstmt.setString(8, customer.getNum());  // 条件-客户编号
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("DELETE FROM t_customer WHERE id=?");
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
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
