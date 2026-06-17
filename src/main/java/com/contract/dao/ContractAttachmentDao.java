package com.contract.dao;

import com.contract.entity.ContractAttachment;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同附件数据访问对象（ContractAttachment DAO）
 * <p>
 * 提供对合同附件表（t_contract_attachment）的数据访问操作。
 * 管理与合同关联的各种电子文件（合同正文、资质证明、补充协议等）。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractAttachmentDao {

    /**
     * 新增附件记录
     * <p>上传文件后调用，将文件元数据存入数据库</p>
     *
     * @param ca 附件对象（包含文件名、路径、类型等）
     * @return true-成功；false-失败
     */
    public boolean insert(ContractAttachment ca) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractAttachmentDao", "insert", "开始新增附件, 合同编号: " + ca.getConNum() + ", 文件名: " + ca.getFileName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_contract_attachment");
            pstmt = conn.prepareStatement("INSERT INTO t_contract_attachment(id, conNum, fileName, path, type, uploadTime, file_data) VALUES(?, ?, ?, ?, ?, ?, ?)");
            FileLogger.debug("ContractAttachmentDao", "insert", "SQL=INSERT INTO t_contract_attachment(id, conNum, fileName, path, type, uploadTime, file_data) VALUES(...)");
            pstmt.setInt(1, id);
            pstmt.setString(2, ca.getConNum());       // 关联合同编号
            pstmt.setString(3, ca.getFileName());     // 原始文件名
            pstmt.setString(4, ca.getPath());         // 存储路径
            pstmt.setString(5, ca.getType());         // 附件类型分类
            // 上传时间使用当前系统时间
            pstmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
            if (ca.getFileData() != null) {
                pstmt.setBinaryStream(7, new java.io.ByteArrayInputStream(ca.getFileData()), ca.getFileData().length);
            } else {
                pstmt.setNull(7, java.sql.Types.BLOB);
            }
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractAttachmentDao", "insert", "新增附件" + (result ? "成功" : "失败") + ", 合同编号: " + ca.getConNum() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractAttachmentDao", "insert", "新增附件失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 根据合同编号查询所有附件
     * <p>获取某合同关联的所有附件列表</p>
     *
     * @param conNum 合同编号
     * @return 附件列表
     */
    public List<ContractAttachment> findByConNum(String conNum) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractAttachmentDao", "findByConNum", "开始根据合同编号查询附件, 合同编号: " + conNum);
        List<ContractAttachment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_contract_attachment WHERE conNum = ? ORDER BY id");
            FileLogger.debug("ContractAttachmentDao", "findByConNum", "SQL=SELECT * FROM t_contract_attachment WHERE conNum = ? ORDER BY id");
            pstmt.setString(1, conNum);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                // 内联方式构建附件对象
                ContractAttachment ca = new ContractAttachment();
                ca.setId(rs.getInt("id"));
                ca.setConNum(rs.getString("conNum"));
                ca.setFileName(rs.getString("fileName"));
                ca.setPath(rs.getString("path"));
                ca.setType(rs.getString("type"));
                ca.setUploadTime(rs.getTimestamp("uploadTime"));
                try {
                    ca.setFileData(rs.getBytes("file_data"));
                } catch (Exception e) {
                    // 某些数据库驱动可能不支持直接getBytes
                }
                list.add(ca);
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractAttachmentDao", "findByConNum", "查询完成，共" + list.size() + "条附件记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractAttachmentDao", "findByConNum", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 删除合同的所有附件记录
     * <p>删除合同时级联清理附件元数据（注意：不删除实际文件）</p>
     *
     * @param conNum 合同编号
     * @return true-成功；false-失败
     */
    public boolean deleteByConNum(String conNum) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractAttachmentDao", "deleteByConNum", "开始删除合同附件记录, 合同编号: " + conNum);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("DELETE FROM t_contract_attachment WHERE conNum=?");
            FileLogger.debug("ContractAttachmentDao", "deleteByConNum", "SQL=DELETE FROM t_contract_attachment WHERE conNum=?");
            pstmt.setString(1, conNum);
            boolean result = pstmt.executeUpdate() >= 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractAttachmentDao", "deleteByConNum", "删除附件记录" + (result ? "成功" : "失败") + ", 合同编号: " + conNum + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractAttachmentDao", "deleteByConNum", "删除附件记录失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }
}
