package com.contract.dao;

import com.contract.entity.ContractVersion;
import com.contract.util.DBUtil;

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
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_contract_version");
            pstmt = conn.prepareStatement(
                "INSERT INTO t_contract_version(id, contract_num, version_no, content, file_data, file_name, modifier, modify_time, change_summary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?)");
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
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
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
        List<ContractVersion> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT id, contract_num, version_no, content, file_data, file_name, modifier, modify_time, change_summary " +
                "FROM t_contract_version WHERE contract_num = ? ORDER BY version_no ASC");
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
        } catch (Exception e) {
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT id, contract_num, version_no, content, file_data, file_name, modifier, modify_time, change_summary " +
                "FROM t_contract_version WHERE contract_num = ? AND version_no = ?");
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
                return ver;
            }
        } catch (Exception e) {
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT NVL(MAX(version_no), 0) FROM t_contract_version WHERE contract_num = ?");
            pstmt.setString(1, contractNum);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (Exception e) {
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("DELETE FROM t_contract_version WHERE contract_num = ?");
            pstmt.setString(1, contractNum);
            return pstmt.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }
}
