package com.contract.entity;

import java.util.Date;

/**
 * 合同操作流程实体类
 * <p>
 * 用于记录合同在各审批环节的处理情况。每个合同在流转过程中会经过多个处理节点
 * （会签、审批、签订），本实体用于记录每个节点的操作人、操作时间、处理意见和状态。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>跟踪合同在各个流程节点的处理进度</li>
 *   <li>记录每个环节的操作人和处理意见</li>
 *   <li>维护各节点的完成状态（未完成/已完成/已否决）</li>
 *   <li>支持合同流程的历史追溯和审计</li>
 * </ul>
 *
 * <h3>流程类型说明（type字段）：</h3>
 * <ul>
 *   <li>1 - 会签：多个部门/人员对合同内容进行会签审核</li>
 *   <li>2 - 审批：领导对合同进行最终审批决策</li>
 *   <li>3 - 签订：正式签署合同，双方签字盖章</li>
 * </ul>
 *
 * <h3>状态说明（state字段）：</h3>
 * <ul>
 *   <li>0 - 未完成：等待该环节的操作人处理</li>
 *   <li>1 - 已完成：该环节已通过，进入下一环节</li>
 *   <li>2 - 已否决：该环节被驳回，合同返回修改或终止</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractProcess {
    /** 流程记录唯一标识符，自增主键 */
    private int id;
    /**
     * 关联的合同编号
     * <p>对应Contract表的num字段，用于关联到具体的合同</p>
     */
    private String conNum;
    /**
     * 流程类型
     * <ul>
     *   <li>1 - 会签阶段</li>
     *   <li>2 - 审批阶段</li>
     *   <li>3 - 签订阶段</li>
     * </ul>
     */
    private int type;
    /** 合同名称（非数据库字段，用于页面显示） */
    private String contractName;
    /**
     * 处理状态
     * <ul>
     *   <li>0 - 未完成：待处理</li>
     *   <li>1 - 已已完成：通过</li>
     *   <li>2 - 已否决：被驳回</li>
     * </ul>
     */
    private int state;
    /**
     * 当前环节的操作人/负责人
     * <p>对应User表的name字段</p>
     */
    private String userName;
    /**
     * 处理意见/备注
     * <p>操作人对本环节的意见、建议或审批结论</p>
     */
    private String content;
    /**
     * 操作时间
     * <p>记录本环节的最后操作时间，用于审计和时效控制</p>
     */
    private Date time;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public ContractProcess() {}

    /**
     * 完整构造方法
     * <p>创建流程对象并设置所有属性</p>
     *
     * @param id      流程记录唯一标识
     * @param conNum  关联的合同编号
     * @param type    流程类型（1-会签, 2-审批, 3-签订）
     * @param state   处理状态（0-未完成, 1-已完成, 2-已否决）
     * @param userName 操作人用户名
     * @param content 处理意见
     * @param time    操作时间
     */
    public ContractProcess(int id, String conNum, int type, int state, String userName, String content, Date time) {
        this.id = id;
        this.conNum = conNum;
        this.type = type;
        this.state = state;
        this.userName = userName;
        this.content = content;
        this.time = time;
    }

    /**
     * 获取流程记录ID
     * @return 流程记录唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置流程记录ID
     * @param id 流程记录唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取合同编号
     * @return 关联的合同编号
     */
    public String getConNum() { return conNum; }

    /**
     * 设置合同编号
     * @param conNum 关联的合同编号
     */
    public void setConNum(String conNum) { this.conNum = conNum; }

    /**
     * 获取流程类型
     * @return 类型码（1-会签, 2-审批, 3-签订）
     */
    public int getType() { return type; }

    /**
     * 设置流程类型
     * @param type 类型码（1-会签, 2-审批, 3-签订）
     */
    public void setType(int type) { this.type = type; }

    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }

    /**
     * 获取处理状态
     * @return 状态码（0-未完成, 1-已完成, 2-已否决）
     */
    public int getState() { return state; }

    /**
     * 设置处理状态
     * @param state 状态码（0-未完成, 1-已完成, 2-已否决）
     */
    public void setState(int state) { this.state = state; }

    /**
     * 获取操作人
     * @return 操作人用户名
     */
    public String getUserName() { return userName; }

    /**
     * 设置操作人
     * @param userName 操作人用户名
     */
    public void setUserName(String userName) { this.userName = userName; }

    /**
     * 获取处理意见
     * @return 意见内容
     */
    public String getContent() { return content; }

    /**
     * 设置处理意见
     * @param content 意见内容
     */
    public void setContent(String content) { this.content = content; }

    /**
     * 获取操作时间
     * @return 操作时间
     */
    public Date getTime() { return time; }

    /**
     * 设置操作时间
     * @param time 操作时间
     */
    public void setTime(Date time) { this.time = time; }

    /**
     * 获取流程类型的中文名称
     * <p>将数字类型码转换为可读的中文名称，便于界面显示</p>
     *
     * @return 类型中文名称（"会签"/"审批"/"签订"/"未知"）
     */
    public String getTypeName() {
        switch (type) {
            case 1: return "会签";
            case 2: return "审批";
            case 3: return "签订";
            default: return "未知";
        }
    }

    /**
     * 获取处理状态的中文名称
     * <p>将数字状态码转换为可读的中文名称，便于界面显示</p>
     *
     * @return 状态中文名称（"未完成"/"已完成"/"已否决"/"未知"）
     */
    public String getStateName() {
        switch (state) {
            case 0: return "未完成";
            case 1: return "已完成";
            case 2: return "已否决";
            default: return "未知";
        }
    }
}
