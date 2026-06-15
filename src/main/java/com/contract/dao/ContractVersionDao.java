package com.contract.dao;

import com.contract.entity.ContractVersion;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同版本历史数据访问对象（ContractVersion DAO）
 * <p>
 * 提供对合同版本表（t_contract_version）的数据访问操作。
 * 支持版本的插入、查询、删除等操作，用于合同版本控制和变更追踪。
 * </p>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class ContractVersionDao {

    /**
     * 插入新的版本记录
     *
     * @param ver 版本对象
     * @return true-成功；false-失败
     */
    public boolean insert(ContractVersion ver) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractVersionDao", "insert", "开始插入版本记录, 合同编号: " + ver.getContractNum() + ", 版本号: " + ver.getVersionNo() + ", 修改人: " + ver.getModifier());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_contract_version");
            pstmt = conn.prepareStatement(
                "INSERT INTO t_contract_version(id, contract_num, version_no, content, file_data, file_name, modifier, modify_time, change_summary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?)");
            FileLogger.debug("ContractVersionDao", "insert", "SQL=INSERT INTO t_contract_version(id, contract_num, version_no, content, file_data, file_name, modifier, modify_time, change_summary) VALUES(...)");
            pstmt.setInt(1, id);
            pstmt.setString(2, ver.getContractNum());
            pstmt.setInt(3, ver.getVersionNo());
            // CLOB字段使用setString处理
            if (ver.getContent() != null) {
                pstmt.setString(4, ver.getContent());
            } else {
                pstmt.setNull(4, java.sql.Types.CLOB);
            }
            // BLOB字段处理
            if (ver.getFileData() != null) {
                pstmt.setBytes(5, ver.getFileData());
            } else {
                pstmt.setNull(5, java.sql.Types.BLOB);
            }
            pstmt.setString(6, ver.getFileName());
            pstmt.setString(7, ver.getModifier());
            pstmt.setString(8, ver.getChangeSummary());
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractVersionDao", "insert", "插入版本记录" + (result ? "成功" : "失败") + ", 合同编号: " + ver.getContractNum() + ", 版本号: " + ver.getVersionNo() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractVersionDao", "insert", "插入版本记录失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 根据合同编号查询所有版本记录（按版本号升序）
     *
     * @param contractNum 合同编号
     * @return 版本列表
     */
    public List<ContractVersion> findByContractNum(String contractNum) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractVersionDao", "findByContractNum", "开始根据合同编号查询版本, 合同编号: " + contractNum);
        List<ContractVersion> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT id, contract_num, version_no, content, file_data, file_name, modifier, modify_time, change_summary " +
                "FROM t_contract_version WHERE contract_num = ? ORDER BY version_no ASC");
            FileLogger.debug("ContractVersionDao", "findByContractNum", "SQL=SELECT ... FROM t_contract_version WHERE contract_num = ? ORDER BY version_no ASC");
            pstmt.setString(1, contractNum);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ContractVersion ver = new ContractVersion();
                ver.setId(rs.getInt("id"));
                ver.setContractNum(rs.getString("contract_num"));
                ver.setVersionNo(rs.getInt("version_no"));
                // CLOB读取：优先使用getString
                try { ver.setContent(rs.getString("content")); } catch (Exception ignored) {}
                // BLOB读取
                try { ver.setFileData(rs.getBytes("file_data")); } catch (Exception ignored) {}
                ver.setFileName(rs.getString("file_name"));
                ver.setModifier(rs.getString("modifier"));
                ver.setModifyTime(rs.getTimestamp("modify_time"));
                ver.setChangeSummary(rs.getString("change_summary"));
                list.add(ver);
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractVersionDao", "findByContractNum", "查询完成，共" + list.size() + "条版本记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractVersionDao", "findByContractNum", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据合同编号和版本号查询单条版本记录
     *
     * @param contractNum 合同编号
     * @param versionNo  版本号
     * @return 版本对象；不存在返回null
     */
    public ContractVersion findByVersionNo(String contractNum, int versionNo) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractVersionDao", "findByVersionNo", "开始根据版本号查询, 合同编号: " + contractNum + ", 版本号: " + versionNo);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT id, contract_num, version_no, content, file_data, file_name, modifier, modify_time, change_summary " +
                "FROM t_contract_version WHERE contract_num = ? AND version_no = ?");
            FileLogger.debug("ContractVersionDao", "findByVersionNo", "SQL=SELECT ... FROM t_contract_version WHERE contract_num = ? AND version_no = ?");
            pstmt.setString(1, contractNum);
            pstmt.setInt(2, versionNo);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ContractVersion ver = new ContractVersion();
                ver.setId(rs.getInt("id"));
                ver.setContractNum(rs.getString("contract_num"));
                ver.setVersionNo(rs.getInt("version_no"));
                try { ver.setContent(rs.getString("content")); } catch (Exception ignored) {}
                try { ver.setFileData(rs.getBytes("file_data")); } catch (Exception ignored) {}
                ver.setFileName(rs.getString("file_name"));
                ver.setModifier(rs.getString("modifier"));
                ver.setModifyTime(rs.getTimestamp("modify_time"));
                ver.setChangeSummary(rs.getString("change_summary"));
                long cost = System.currentTimeMillis() - startTime;
                FileLogger.info("ContractVersionDao", "findByVersionNo", "查询成功, 合同编号: " + contractNum + ", 版本号: " + versionNo + ", 耗时" + cost + "ms");
                return ver;
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractVersionDao", "findByVersionNo", "查询完成, 未找到版本, 合同编号: " + contractNum + ", 版本号: " + versionNo + ", 耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractVersionDao", "findByVersionNo", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 获取某合同的下一个版本号
     * <p>查询当前最大版本号+1，如果没有版本记录则返回1</p>
     *
     * @param contractNum 合同编号
     * @return 下一个版本号
     */
    public int getNextVersionNumber(String contractNum) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractVersionDao", "getNextVersionNumber", "开始获取下一版本号, 合同编号: " + contractNum);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT NVL(MAX(version_no), 0) FROM t_contract_version WHERE contract_num = ?");
            FileLogger.debug("ContractVersionDao", "getNextVersionNumber", "SQL=SELECT NVL(MAX(version_no), 0) FROM t_contract_version WHERE contract_num = ?");
            pstmt.setString(1, contractNum);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int nextVer = rs.getInt(1) + 1;
                long cost = System.currentTimeMillis() - startTime;
                FileLogger.info("ContractVersionDao", "getNextVersionNumber", "获取下一版本号: " + nextVer + ", 合同编号: " + contractNum + ", 耗时" + cost + "ms");
                return nextVer;
            }
        } catch (Exception e) {
            FileLogger.error("ContractVersionDao", "getNextVersionNumber", "获取下一版本号异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return 1;  // 默认从版本1开始
    }

    /**
     * 删除某合同的所有版本记录（合同删除时级联清理）
     *
     * @param contractNum 合同编号
     * @return true-成功；false-失败
     */
    public boolean deleteByContractNum(String contractNum) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractVersionDao", "deleteByContractNum", "开始删除合同版本记录, 合同编号: " + contractNum);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("DELETE FROM t_contract_version WHERE contract_num = ?");
            FileLogger.debug("ContractVersionDao", "deleteByContractNum", "SQL=DELETE FROM t_contract_version WHERE contract_num = ?");
            pstmt.setString(1, contractNum);
            boolean result = pstmt.executeUpdate() >= 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractVersionDao", "deleteByContractNum", "删除版本记录" + (result ? "成功" : "失败") + ", 合同编号: " + contractNum + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractVersionDao", "deleteByContractNum", "删除版本记录失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }
}
