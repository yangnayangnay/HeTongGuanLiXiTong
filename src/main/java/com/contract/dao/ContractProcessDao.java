package com.contract.dao;

import com.contract.entity.ContractProcess;
import com.contract.util.DBUtil;
import com.contract.util.FileLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同操作流程数据访问对象（ContractProcess DAO）
 * <p>
 * 提供对合同流程表（t_contract_process）的数据访问操作。
 * 合同流程记录了合同在各审批环节（会签、审批、签订）的处理情况，
 * 是合同工作流引擎的核心数据支撑。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>流程记录创建：在合同进入新环节时插入流程记录</li>
 *   <li>状态更新：更新流程节点的处理状态和意见</li>
 *   <li>流程查询：按合同、类型、用户等多维度查询</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractProcessDao {

    /**
     * 新增合同流程记录
     * <p>当合同进入新的处理环节时调用，记录该环节的操作人等信息</p>
     *
     * @param cp 流程对象（包含合同编号、流程类型、操作人等）
     * @return true-插入成功；false-插入失败
     */
    public boolean insert(ContractProcess cp) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "insert", "开始新增流程记录, 合同编号: " + cp.getConNum() + ", 类型: " + cp.getType() + ", 操作人: " + cp.getUserName());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            int id = DBUtil.getNextId("seq_contract_process");
            // 插入流程记录，time字段若为空则使用当前时间
            pstmt = conn.prepareStatement("INSERT INTO t_contract_process(id, conNum, type, state, userName, content, time) VALUES(?, ?, ?, ?, ?, ?, ?)");
            FileLogger.debug("ContractProcessDao", "insert", "SQL=INSERT INTO t_contract_process(id, conNum, type, state, userName, content, time) VALUES(...)");
            pstmt.setInt(1, id);
            pstmt.setString(2, cp.getConNum());      // 关联合同编号
            pstmt.setInt(3, cp.getType());           // 流程类型（1-会签/2-审批/3-签订）
            pstmt.setInt(4, cp.getState());          // 初始状态（通常为0-未完成）
            pstmt.setString(5, cp.getUserName());     // 操作人
            pstmt.setString(6, cp.getContent());     // 处理意见
            // 时间戳转换，若无指定时间则使用当前系统时间
            pstmt.setTimestamp(7, cp.getTime() != null ? new java.sql.Timestamp(cp.getTime().getTime()) : new java.sql.Timestamp(System.currentTimeMillis()));
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "insert", "新增流程记录" + (result ? "成功" : "失败") + ", 合同编号: " + cp.getConNum() + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "insert", "新增流程记录失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 更新流程节点状态
     * <p>当操作人对流程节点进行处理（通过/否决）时调用</p>
     *
     * @param id     流程记录ID
     * @param state  新的状态（0-未完成, 1-已完成, 2-已否决）
     * @param content 处理意见/备注
     * @return true-更新成功；false-更新失败
     */
    public boolean updateState(int id, int state, String content) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "updateState", "开始更新流程状态, 流程ID: " + id + ", 新状态: " + state);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 更新状态、意见和时间，时间自动设为当前时间
            pstmt = conn.prepareStatement("UPDATE t_contract_process SET state=?, content=?, time=? WHERE id=?");
            FileLogger.debug("ContractProcessDao", "updateState", "SQL=UPDATE t_contract_process SET state=?, content=?, time=? WHERE id=?");
            pstmt.setInt(1, state);                                    // 新状态
            pstmt.setString(2, content);                               // 处理意见
            pstmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis())); // 操作时间
            pstmt.setInt(4, id);                                      // 条件-流程ID
            boolean result = pstmt.executeUpdate() > 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "updateState", "更新流程状态" + (result ? "成功" : "失败") + ", 流程ID: " + id + ", 状态: " + state + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "updateState", "更新流程状态失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 根据合同编号查询所有流程记录
     * <p>获取某合同的所有流程节点信息，用于展示完整流程进度</p>
     *
     * @param conNum 合同编号
     * @return 该合同的流程记录列表
     */
    public List<ContractProcess> findByConNum(String conNum) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "findByConNum", "开始根据合同编号查询流程, 合同编号: " + conNum);
        List<ContractProcess> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_contract_process WHERE conNum = ? ORDER BY id");
            FileLogger.debug("ContractProcessDao", "findByConNum", "SQL=SELECT * FROM t_contract_process WHERE conNum = ? ORDER BY id");
            pstmt.setString(1, conNum);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapProcess(rs));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "findByConNum", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "findByConNum", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据合同编号和流程类型查询
     * <p>获取某合同特定类型的所有流程记录（如所有会签节点）</p>
     *
     * @param conNum 合同编号
     * @param type   流程类型（1-会签, 2-审批, 3-签订）
     * @return 匹配的流程记录列表
     */
    public List<ContractProcess> findByConNumAndType(String conNum, int type) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "findByConNumAndType", "开始根据合同编号和类型查询流程, 合同编号: " + conNum + ", 类型: " + type);
        List<ContractProcess> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_contract_process WHERE conNum = ? AND type = ? ORDER BY id");
            FileLogger.debug("ContractProcessDao", "findByConNumAndType", "SQL=SELECT * FROM t_contract_process WHERE conNum = ? AND type = ? ORDER BY id");
            pstmt.setString(1, conNum);
            pstmt.setInt(2, type);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapProcess(rs));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "findByConNumAndType", "查询完成，共" + list.size() + "条记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "findByConNumAndType", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 查询待处理的流程任务
     * <p>获取指定用户在某类流程中待处理的任务列表</p>
     * <p>用于生成用户的"待办事项"列表</p>
     *
     * @param userName 操作人用户名
     * @param type     流程类型
     * @param state    状态（通常传0表示待处理）
     * @return 待处理的流程任务列表
     */
    public List<ContractProcess> findByUserNameAndTypeAndState(String userName, int type, int state) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "findByUserNameAndTypeAndState", "开始查询待处理流程, 操作人: " + userName + ", 类型: " + type + ", 状态: " + state);
        List<ContractProcess> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // JOIN合同表获取合同名称
            pstmt = conn.prepareStatement("SELECT p.*, c.name AS contractName FROM t_contract_process p LEFT JOIN t_contract c ON p.conNum = c.num WHERE p.userName = ? AND p.type = ? AND p.state = ? ORDER BY p.id");
            pstmt.setString(1, userName);
            pstmt.setInt(2, type);
            pstmt.setInt(3, state);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapProcess(rs));
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "findByUserNameAndTypeAndState", "查询完成，共" + list.size() + "条待处理记录，耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "findByUserNameAndTypeAndState", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据ID查询单条流程记录
     *
     * @param id 流程记录ID
     * @return 流程对象；未找到返回null
     */
    public ContractProcess findById(int id) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "findById", "开始根据ID查询流程, 流程ID: " + id);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM t_contract_process WHERE id = ?");
            FileLogger.debug("ContractProcessDao", "findById", "SQL=SELECT * FROM t_contract_process WHERE id = ?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ContractProcess cp = mapProcess(rs);
                long cost = System.currentTimeMillis() - startTime;
                FileLogger.info("ContractProcessDao", "findById", "查询成功, 找到流程ID: " + id + ", 耗时" + cost + "ms");
                return cp;
            }
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "findById", "查询完成, 未找到流程ID: " + id + ", 耗时" + cost + "ms");
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "findById", "查询异常: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 删除合同的所有流程记录
     * <p>删除合同时级联清理其流程数据</p>
     *
     * @param conNum 合同编号
     * @return true-删除成功；false-删除失败
     */
    public boolean deleteByConNum(String conNum) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "deleteByConNum", "开始删除合同流程记录, 合同编号: " + conNum);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("DELETE FROM t_contract_process WHERE conNum=?");
            FileLogger.debug("ContractProcessDao", "deleteByConNum", "SQL=DELETE FROM t_contract_process WHERE conNum=?");
            pstmt.setString(1, conNum);
            boolean result = pstmt.executeUpdate() >= 0;  // 无记录也算成功
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "deleteByConNum", "删除流程记录" + (result ? "成功" : "失败") + ", 合同编号: " + conNum + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "deleteByConNum", "删除流程记录失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 重置指定合同和类型的所有流程记录状态为待处理（state=0）
     * <p>审批否决回退时使用，确保重新审批时所有人从头开始</p>
     *
     * @param conNum 合同编号
     * @param type   流程类型
     * @return true-重置成功；false-重置失败
     */
    public boolean resetByConNumAndType(String conNum, int type) {
        long startTime = System.currentTimeMillis();
        FileLogger.info("ContractProcessDao", "resetByConNumAndType", "开始重置流程状态, 合同编号: " + conNum + ", 类型: " + type);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("UPDATE t_contract_process SET state=0, time=NULL, content=NULL WHERE conNum=? AND type=? AND state IN (1, 2)");
            pstmt.setString(1, conNum);
            pstmt.setInt(2, type);
            boolean result = pstmt.executeUpdate() >= 0;
            long cost = System.currentTimeMillis() - startTime;
            FileLogger.info("ContractProcessDao", "resetByConNumAndType", "重置流程状态" + (result ? "成功" : "失败") + ", 合同编号: " + conNum + ", 耗时" + cost + "ms");
            return result;
        } catch (Exception e) {
            FileLogger.error("ContractProcessDao", "resetByConNumAndType", "重置流程状态失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    /**
     * 将ResultSet映射为ContractProcess对象的私有方法
     */
    private ContractProcess mapProcess(ResultSet rs) throws Exception {
        ContractProcess cp = new ContractProcess();
        cp.setId(rs.getInt("id"));
        cp.setConNum(rs.getString("conNum"));
        cp.setType(rs.getInt("type"));
        cp.setState(rs.getInt("state"));
        cp.setUserName(rs.getString("userName"));
        cp.setContent(rs.getString("content"));
        cp.setTime(rs.getTimestamp("time"));  // 使用getTimestamp保留时分秒精度
        // 尝试从结果集中获取合同名称（JOIN查询时可用）
        try {
            cp.setContractName(rs.getString("contractName"));
        } catch (Exception e) {
            // 如果结果集中没有contractName列，忽略
        }
        return cp;
    }
}
