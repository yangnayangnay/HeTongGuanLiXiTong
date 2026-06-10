package com.contract.entity;

import java.util.Date;

/**
 * 合同状态变更实体类
 * <p>
 * 用于记录合同在整个生命周期中的状态变更历史。每当合同完成一个关键里程碑
 * （如起草完成、会签完成、定稿完成等），就会在此表中插入一条记录，
 * 形成完整的状态变更轨迹。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>记录合同的状态变更历史</li>
 *   <li>追踪合同的当前所处阶段</li>
 *   <li>提供合同进度的可视化依据</li>
 *   <li>支持合同流程的审计和回溯</li>
 * </ul>
 *
 * <h3>状态类型说明（type字段）：</h3>
 * <ul>
 *   <li>1 - 起草：合同初稿已完成，等待提交会签</li>
 *   <li>2 - 会签完成：所有会签人员已完成审核</li>
 *   <li>3 - 定稿完成：根据会签意见修改后形成终稿</li>
 *   <li>4 - 审批完成：领导审批通过，可以签订</li>
 *   <li>5 - 签订完成：合同正式签署生效</li>
 * </ul>
 *
 * <h3>与ContractProcess的区别：</h3>
 * <p>
 * ContractState记录的是合同达到某个里程碑的时间点（结果），
 * 而ContractProcess记录的是每个环节的详细处理过程（过程）。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractState {
    /** 状态记录唯一标识符，自增主键 */
    private int id;
    /**
     * 关联的合同编号
     * <p>对应Contract表的num字段，用于关联到具体的合同</p>
     */
    private String conNum;
    /**
     * 状态类型
     * <ul>
     *   <li>1 - 起草完成</li>
     *   <li>2 - 会签完成</li>
     *   <li>3 - 定稿完成</li>
     *   <li>4 - 审批完成</li>
     *   <li>5 - 签订完成</li>
     * </ul>
     */
    private int type;
    /**
     * 状态变更时间
     * <p>记录到达此状态的具体时间点</p>
     * <p>用于计算各环节的处理耗时和效率分析</p>
     */
    private Date time;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public ContractState() {}

    /**
     * 完整构造方法
     * <p>创建状态对象并设置所有属性</p>
     *
     * @param id     状态记录唯一标识
     * @param conNum 关联的合同编号
     * @param type   状态类型（1-起草, 2-会签完成, 3-定稿完成, 4-审批完成, 5-签订完成）
     * @param time   状态变更时间
     */
    public ContractState(int id, String conNum, int type, Date time) {
        this.id = id;
        this.conNum = conNum;
        this.type = type;
        this.time = time;
    }

    /**
     * 获取状态记录ID
     * @return 状态记录唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置状态记录ID
     * @param id 状态记录唯一标识符
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
     * 获取状态类型
     * @return 类型码（1-起草, 2-会签完成, 3-定稿完成, 4-审批完成, 5-签订完成）
     */
    public int getType() { return type; }

    /**
     * 设置状态类型
     * @param type 类型码（1-起草, 2-会签完成, 3-定稿完成, 4-审批完成, 5-签订完成）
     */
    public void setType(int type) { this.type = type; }

    /**
     * 获取状态变更时间
     * @return 时间戳
     */
    public Date getTime() { return time; }

    /**
     * 设置状态变更时间
     * @param time 时间戳
     */
    public void setTime(Date time) { this.time = time; }

    /**
     * 获取状态类型的中文名称
     * <p>将数字类型码转换为可读的中文名称，便于界面显示</p>
     *
     * @return 状态中文名称（"起草"/"会签完成"/"定稿完成"/"审批完成"/"签订完成"/"未知"）
     */
    public String getTypeName() {
        switch (type) {
            case 1: return "起草";
            case 2: return "会签完成";
            case 3: return "定稿完成";
            case 4: return "审批完成";
            case 5: return "签订完成";
            default: return "未知";
        }
    }
}
