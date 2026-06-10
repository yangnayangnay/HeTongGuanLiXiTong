package com.contract.dao;

import com.contract.entity.ContractState;
import com.contract.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同状态变更数据访问对象（ContractState DAO）
 * <p>
 * 提供对合同状态表（t_contract_state）的数据访问操作。
 * 记录合同在整个生命周期中的关键里程碑事件（起草、会签完成、定稿完成等）。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>状态记录：每当合同达到一个里程碑时插入状态记录</li>
 *   <li>最新状态查询：获取合同的当前所处阶段</li>
 *   <li>历史轨迹查询：查看合同的完整状态变更历史</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractStateDao {

    /**
     * 新增合同状态记录
     * <p>当合同完成某个里程碑时调用，记录状态变更事件</p>
     *
     * @param cs 状态对象（包含合同编号、状态类型、时间等）
     * @return true-插入成功；false-插入失败
     */
    public boolean insert(ContractState cs) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_contract_state");
            pstmt = conn.prepareStatement("INSERT INTO t_contract_state(id, conNum, type, time) VALUES(?, ?, ?, ?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, cs.getConNum());
            pstmt.setInt(3, cs.getType());  // 状态类型（1-5）
            // 时间戳转换，默认使用当前时间
            pstmt.setTimestamp(4, cs.getTime() != null ? new java.sql.Timestamp(cs.getTime().getTime()) : new java.sql.Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 查询合同的最新状态记录
     * <p>用于判断合同当前所处的阶段</p>
     *
     * @param conNum 合同编号
     * @return 最新的状态对象；无记录则返回null
     */
    public ContractState findLatestByConNum(String conNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 使用FETCH FIRST 1 ROWS ONLY只取最新一条记录（DB2语法）
            pstmt = conn.prepareStatement("SELECT * FROM t_contract_state WHERE conNum = ? ORDER BY id DESC FETCH FIRST 1 ROWS ONLY");
            pstmt.setString(1, conNum);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapState(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询合同的所有状态变更历史
     * <p>用于展示合同的时间线/进度视图</p>
     *
     * @param conNum 合同编号
     * @return 状态变更历史列表（按时间顺序）
     */
    public List<ContractState> findByConNum(String conNum) {
        List<ContractState> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_contract_state WHERE conNum = ? ORDER BY id");
            pstmt.setString(1, conNum);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapState(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据状态类型查询
     * <p>查询所有处于某种状态的合同记录，用于统计报表</p>
     *
     * @param type 状态类型（1-起草, 2-会签完成, 3-定稿完成, 4-审批完成, 5-签订完成）
     * @return 符合条件的状态记录列表
     */
    public List<ContractState> findByType(int type) {
        List<ContractState> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_contract_state WHERE type = ? ORDER BY id DESC");
            pstmt.setInt(1, type);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapState(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 删除合同的所有状态记录
     * <p>删除合同时级联清理状态数据</p>
     *
     * @param conNum 合同编号
     * @return true-删除成功；false-删除失败
     */
    public boolean deleteByConNum(String conNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("DELETE FROM t_contract_state WHERE conNum=?");
            pstmt.setString(1, conNum);
            return pstmt.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 将ResultSet映射为ContractState对象的私有方法
     */
    private ContractState mapState(ResultSet rs) throws Exception {
        ContractState cs = new ContractState();
        cs.setId(rs.getInt("id"));
        cs.setConNum(rs.getString("conNum"));
        cs.setType(rs.getInt("type"));
        cs.setTime(rs.getTimestamp("time"));
        return cs;
    }
}
