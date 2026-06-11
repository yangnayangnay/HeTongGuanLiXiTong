package com.contract.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 待办任务通知服务类
 * <p>
 * 负责查询当前用户的待办任务数量，用于在用户登录后弹窗提示。
 * 通过查询t_contract_process表获取state=0（待处理状态）且
 * userName匹配当前用户的流程记录数来统计待办数量。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>查询指定用户的待处理任务数量</li>
 *   <li>基于t_contract_process表的state字段判断任务状态</li>
 *   <li>提供统一的待办任务统计接口供UI层调用</li>
 * </ul>
 *
 * <h3>数据来源：</h3>
 * <ul>
 *   <li>t_contract_process表：存储合同流程节点记录</li>
 *   <li>state=0 表示该流程节点处于待处理状态</li>
 *   <li>userName字段匹配当前登录用户的用户名</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class NotificationService {

    /**
     * 查询指定用户的待办任务数量
     * <p>
     * 统计t_contract_process表中state=0（待处理）且userName等于
     * 指定用户名的记录条数。这些记录代表需要该用户处理的合同流程节点，
     * 包括会签、审批、签订等各类待办任务。
     * </p>
     *
     * @param userName 当前登录用户的用户名
     * @return 待办任务数量（大于0表示有待办任务）；查询异常时返回0
     */
    public static int getPendingTaskCount(String userName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            // 查询待办任务数量：state=0表示待处理，userName匹配当前用户
            String sql = "SELECT COUNT(*) FROM t_contract_process WHERE state = 0 AND user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);  // 设置查询参数：当前用户名
            rs = pstmt.executeQuery();
            // 返回查询到的待办任务数量
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            // 查询异常时打印错误日志，返回0避免影响主流程
            System.err.println("[通知] 待办任务查询异常: " + e.getMessage());
        } finally {
            // 确保数据库资源被正确释放
            DBUtil.close(conn, pstmt, rs);
        }
        return 0;  // 默认返回0（无待办任务或查询异常）
    }

    /**
     * 待办任务详细信息内部数据类
     * <p>用于封装待办任务的详细字段信息，供UI层展示使用</p>
     */
    public static class PendingTaskInfo {
        /** 合同编号 */
        private String conNum;
        /** 合同名称 */
        private String contractName;
        /** 任务类型名称（会签/审批/签订） */
        private String typeName;
        /** 状态名称（待处理） */
        private String stateName;
        /** 分配时间 */
        private String time;

        public PendingTaskInfo(String conNum, String contractName, String typeName, String stateName, String time) {
            this.conNum = conNum;
            this.contractName = contractName;
            this.typeName = typeName;
            this.stateName = stateName;
            this.time = time;
        }

        public String getConNum() { return conNum; }
        public String getContractName() { return contractName; }
        public String getTypeName() { return typeName; }
        public String getStateName() { return stateName; }
        public String getTime() { return time; }
    }

    /**
     * 查询指定用户的待办任务详细列表
     * <p>
     * 通过关联查询t_contract_process表和t_contract表，获取当前用户所有待处理任务的详细信息。
     * 查询条件：state=0（待处理）且userName匹配当前用户。
     * 返回包含合同编号、合同名称、任务类型、状态和分配时间的列表。
     * </p>
     *
     * @param userName 当前登录用户的用户名
     * @return 待办任务详细列表；查询异常时返回空列表
     */
    public static List<PendingTaskInfo> getPendingTaskDetails(String userName) {
        List<PendingTaskInfo> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 关联查询t_contract_process和t_contract表，获取待办任务详情
            // type: 1-会签, 2-审批, 3-签订；state=0表示待处理
            String sql = "SELECT p.con_num, c.name AS contract_name, p.type, p.state, p.time " +
                         "FROM t_contract_process p LEFT JOIN t_contract c ON p.con_num = c.num " +
                         "WHERE p.state = 0 AND p.user_name = ? ORDER BY p.time DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = pstmt.executeQuery();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                String conNum = rs.getString("con_num");
                String contractName = rs.getString("contract_name");
                int type = rs.getInt("type");
                // 将类型码转换为中文名称
                String typeName;
                switch (type) {
                    case 1: typeName = "会签"; break;
                    case 2: typeName = "审批"; break;
                    case 3: typeName = "签订"; break;
                    default: typeName = "未知"; break;
                }
                String stateName = "待处理";
                String time = rs.getTimestamp("time") != null ? sdf.format(rs.getTimestamp("time")) : "";
                list.add(new PendingTaskInfo(conNum, contractName, typeName, stateName, time));
            }
        } catch (Exception e) {
            System.err.println("[通知] 待办任务详情查询异常: " + e.getMessage());
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
}
