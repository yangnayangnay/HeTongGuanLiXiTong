package com.contract.dao;

import com.contract.entity.Contract;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同数据访问对象（Contract DAO）
 * <p>
 * 提供对合同表（t_contract）的数据访问操作。合同是本系统的核心业务实体，
 * 本DAO提供了合同的完整CRUD操作以及多种查询方式。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>合同创建：新增合同记录</li>
 *   <li>合同维护：修改合同基本信息</li>
 *   <li>合同查询：支持按编号、名称、负责人等多种方式查询</li>
 *   <li>合同删除：按合同编号删除合同及其关联数据</li>
 * </ul>
 *
 * <h3>数据库表结构：</h3>
 * <pre>
 * 表名：t_contract
 * 字段：id(主键), num(合同编号), name(合同名称), customer(客户),
 *       beginTime(生效日期), endTime(终止日期), content(内容), userName(创建人)
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractDao {

    /**
     * 新增合同
     * <p>向数据库插入一条新的合同记录</p>
     * <p>合同ID由序列自动生成，合同编号应在业务层预先生成</p>
     *
     * @param contract 要新增的合同对象（包含完整的合同信息）
     * @return true-插入成功；false-插入失败
     */
    public boolean insert(Contract contract) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractDao", "insert", "开始新增合同, 合同编号: " + contract.getNum() + ", 合同名称: " + contract.getName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 获取下一个自增ID
            int id = DBUtil.getNextId("seq_contract");
            // 插入合同记录，包含所有业务字段（含附件字段和金额）
            pstmt = conn.prepareStatement("INSERT INTO t_contract(id, num, name, customer, beginTime, endTime, content, userName, file_data, file_name, file_type, amount) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            FileLogger.debug("ContractDao", "insert", "SQL=INSERT INTO t_contract(id, num, name, customer, beginTime, endTime, content, userName, file_data, file_name, file_type, amount) VALUES(...)");
            pstmt.setInt(1, id);  // 参数1：合同ID
            pstmt.setString(2, contract.getNum());      // 参数2：合同编号（业务主键）
            pstmt.setString(3, contract.getName());     // 参数3：合同名称
            pstmt.setString(4, contract.getCustomer()); // 参数4：签约客户
            // 将java.util.Date转换为java.sql.Date存入数据库
            pstmt.setDate(5, contract.getBeginTime() != null ? new java.sql.Date(contract.getBeginTime().getTime()) : null);
            pstmt.setDate(6, contract.getEndTime() != null ? new java.sql.Date(contract.getEndTime().getTime()) : null);
            String content = contract.getContent();
            if (content != null && content.length() > 30000) {
                pstmt.setClob(7, new java.io.StringReader(content));
            } else {
                pstmt.setString(7, content);
            }
            pstmt.setString(8, contract.getUserName());
            byte[] fileData = contract.getFileData();
            if (fileData != null && fileData.length > 0) {
                pstmt.setBinaryStream(9, new java.io.ByteArrayInputStream(fileData), fileData.length);
            } else {
                pstmt.setBytes(9, fileData);
            }
            pstmt.setString(10, contract.getFileName());
            pstmt.setString(11, contract.getFileType());
            pstmt.setDouble(12, contract.getAmount());
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractDao", "insert", "新增合同" + (result ? "成功" : "失败") + ", 合同编号: " + contract.getNum() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractDao", "insert", "新增合同失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 更新合同信息
     * <p>根据合同编号更新合同的基本信息</p>
     * <p>注意：不更新创建人字段，创建人一旦确定不可更改</p>
     *
     * @param contract 包含更新后信息的合同对象（必须包含有效的合同编号）
     * @return true-更新成功；false-更新失败
     */
    public boolean update(Contract contract) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractDao", "update", "开始更新合同, 合同编号: " + contract.getNum() + ", 合同名称: " + contract.getName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("UPDATE t_contract SET name=?, customer=?, beginTime=?, endTime=?, content=?, file_data=?, file_name=?, file_type=?, amount=? WHERE num=?");
            pstmt.setString(1, contract.getName());
            pstmt.setString(2, contract.getCustomer());
            pstmt.setDate(3, contract.getBeginTime() != null ? new java.sql.Date(contract.getBeginTime().getTime()) : null);
            pstmt.setDate(4, contract.getEndTime() != null ? new java.sql.Date(contract.getEndTime().getTime()) : null);
            String content = contract.getContent();
            if (content != null && content.length() > 30000) {
                pstmt.setClob(5, new java.io.StringReader(content));
            } else {
                pstmt.setString(5, content);
            }
            byte[] fileData = contract.getFileData();
            if (fileData != null && fileData.length > 0) {
                pstmt.setBinaryStream(6, new java.io.ByteArrayInputStream(fileData), fileData.length);
            } else {
                pstmt.setBytes(6, fileData);
            }
            pstmt.setString(7, contract.getFileName());
            pstmt.setString(8, contract.getFileType());
            pstmt.setDouble(9, contract.getAmount());
            pstmt.setString(10, contract.getNum());
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractDao", "update", "更新合同" + (result ? "成功" : "失败") + ", 合同编号: " + contract.getNum() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractDao", "update", "更新合同失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 根据合同编号查找合同
     * <p>按合同编号精确查询，用于获取单个合同的详细信息</p>
     *
     * @param num 合同编号（业务唯一标识）
     * @return 找到的合同对象；如果未找到则返回null
     */
    public Contract findByNum(String num) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractDao", "findByNum", "开始根据编号查询合同, 合同编号: " + num);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 按合同编号精确查询
            pstmt = conn.prepareStatement("SELECT * FROM t_contract WHERE num = ?");
            FileLogger.debug("ContractDao", "findByNum", "SQL=SELECT * FROM t_contract WHERE num = ?");
            pstmt.setString(1, num);  // 设置合同编号参数
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Contract c = mapContract(rs);  // 使用映射方法转换结果集
                long cost = System.currentTimeMillis() - startTime;
                FileLogger.info("ContractDao", "findByNum", "查询成功, 找到合同: " + num + ", 耗时" + cost + "ms");
                return c;
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractDao", "findByNum", "查询完成, 未找到合同: " + num + ", 耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractDao", "findByNum", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;  // 未找到返回null
    }

    /**
     * 查找所有合同
     * <p>获取系统中所有合同的列表，按ID降序排列（最新的在前）</p>
     * <p>用于合同列表页面展示</p>
     *
     * @return 合同列表（可能为空列表，但不会为null）
     */
    public List<Contract> findAll() {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractDao", "findAll", "开始查询所有合同");
        List<Contract> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 查询所有合同，按id降序排列使最新合同排在前面
            pstmt = conn.prepareStatement("SELECT * FROM t_contract ORDER BY id DESC");
            FileLogger.debug("ContractDao", "findAll", "SQL=SELECT * FROM t_contract ORDER BY id DESC");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapContract(rs));  // 使用映射方法转换每条记录
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractDao", "findAll", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractDao", "findAll", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据合同名称模糊查询合同
     * <p>支持模糊搜索，用于合同搜索功能</p>
     *
     * @param name 搜索关键词（合同名称中包含的关键字）
     * @return 匹配的合同列表（按ID降序排列）
     */
    public List<Contract> findByName(String name) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractDao", "findByName", "开始根据名称模糊查询合同, 关键词: " + name);
        List<Contract> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 使用LIKE进行模糊匹配，%表示任意字符通配符
            pstmt = conn.prepareStatement("SELECT * FROM t_contract WHERE name LIKE ? ORDER BY id DESC");
            FileLogger.debug("ContractDao", "findByName", "SQL=SELECT * FROM t_contract WHERE name LIKE ? ORDER BY id DESC");
            pstmt.setString(1, "%" + name + "%");  // 前后加%实现包含匹配
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapContract(rs));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractDao", "findByName", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractDao", "findByName", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据创建人查询合同
     * <p>查询指定用户负责/创建的所有合同</p>
     * <p>用于"我的合同"功能展示</p>
     *
     * @param userName 创建人用户名
     * @return 该用户创建的合同列表（按ID降序排列）
     */
    public List<Contract> findByUserName(String userName) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractDao", "findByUserName", "开始根据创建人查询合同, 创建人: " + userName);
        List<Contract> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 按创建人查询
            pstmt = conn.prepareStatement("SELECT * FROM t_contract WHERE userName = ? ORDER BY id DESC");
            FileLogger.debug("ContractDao", "findByUserName", "SQL=SELECT * FROM t_contract WHERE userName = ? ORDER BY id DESC");
            pstmt.setString(1, userName);  // 设置创建人参数
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapContract(rs));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractDao", "findByUserName", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractDao", "findByUserName", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据合同编号删除合同
     * <p>删除指定的合同记录</p>
     * <p>注意：删除合同前应先删除其关联的流程、状态、附件等子表数据</p>
     *
     * @param num 要删除的合同编号
     * @return true-删除成功；false-删除失败
     */
    public boolean deleteByNum(String num) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractDao", "deleteByNum", "开始删除合同, 合同编号: " + num);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 根据合同编号删除
            pstmt = conn.prepareStatement("DELETE FROM t_contract WHERE num=?");
            FileLogger.debug("ContractDao", "deleteByNum", "SQL=DELETE FROM t_contract WHERE num=?");
            pstmt.setString(1, num);  // 设置合同编号参数
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractDao", "deleteByNum", "删除合同" + (result ? "成功" : "失败") + ", 合同编号: " + num + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractDao", "deleteByNum", "删除合同失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 将ResultSet映射为Contract对象的私有方法
     * <p>封装结果集到实体的转换逻辑，避免代码重复</p>
     *
     * @param rs 数据库查询结果集
     * @return 映射后的Contract对象
     * @throws Exception 数据库访问异常
     */
    private Contract mapContract(ResultSet rs) throws Exception {
        Contract c = new Contract();
        c.setId(rs.getInt("id"));              // 主键ID
        c.setNum(rs.getString("num"));          // 合同编号
        c.setName(rs.getString("name"));        // 合同名称
        c.setCustomer(rs.getString("customer"));// 客户名称
        c.setBeginTime(rs.getDate("beginTime"));// 生效日期
        c.setEndTime(rs.getDate("endTime"));    // 终止日期
        c.setContent(rs.getString("content"));  // 合同内容
        c.setUserName(rs.getString("userName"));// 创建人
        // 读取附件相关字段（BLOB类型使用getBytes读取）
        c.setFileData(rs.getBytes("file_data"));    // 附件二进制数据
        c.setFileName(rs.getString("file_name"));   // 附件文件名
        c.setFileType(rs.getString("file_type"));   // 文件类型
        c.setAmount(rs.getDouble("amount"));        // 合同金额
        return c;
    }
}
