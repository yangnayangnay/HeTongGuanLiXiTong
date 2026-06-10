package com.contract.service;

import com.contract.dao.ContractDao;
import com.contract.dao.ContractProcessDao;
import com.contract.dao.ContractStateDao;
import com.contract.dao.ContractAttachmentDao;
import com.contract.dao.LogDao;
import com.contract.entity.Contract;
import com.contract.entity.ContractProcess;
import com.contract.entity.ContractState;
import com.contract.entity.ContractAttachment;
import com.contract.entity.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 合同业务逻辑类（Contract Service）
 * <p>
 * 合同管理系统的核心业务类，实现合同的完整生命周期管理：
 * 起草 → 分配 → 会签 → 定稿 → 审批 → 签订
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>合同起草：创建新合同并生成唯一编号</li>
 *   <li>流程分配：指定各环节的处理人员</li>
 *   <li>会签处理：多人会签审核</li>
 *   <li>定稿确认：会签意见汇总后形成终稿</li>
 *   <li>审批决策：领导最终审批（通过/否决）</li>
 *   <li>签订归档：正式签署合同</li>
 * </ul>
 *
 * <h3>状态流转规则：</h3>
 * <pre>
 * 起草(1) → 会签完成(2) → 定稿完成(3) → 审批完成(4) → 签订完成(5)
 *              ↑                                    |
 *              └──── 审批否决时回退到起草 ←──────────┘
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractService {
    /** 合同数据访问对象 */
    private ContractDao contractDao = new ContractDao();
    /** 流程数据访问对象 */
    private ContractProcessDao processDao = new ContractProcessDao();
    /** 状态数据访问对象 */
    private ContractStateDao stateDao = new ContractStateDao();
    /** 附件数据访问对象 */
    private ContractAttachmentDao attachmentDao = new ContractAttachmentDao();
    /** 日志数据访问对象 */
    private LogDao logDao = new LogDao();

    /**
     * 起草合同
     * <p>创建新的合同记录，生成唯一的合同编号，并将状态设为"起草"</p>
     * <p>合同编号格式：HT + 年月日 + 4位流水号（如HT202401010001）</p>
     *
     * @param contract 合同对象（包含名称、客户、时间等信息）
     * @return true-起草成功；false-起草失败
     */
    public boolean draftContract(Contract contract) {
        // 生成合同编号：HT + 当前日期 + 4位流水号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String num = "HT" + sdf.format(new Date()) + String.format("%04d", contractDao.findAll().size() + 1);
        contract.setNum(num);

        boolean result = contractDao.insert(contract);
        if (result) {
            // 插入状态记录：标记为"起草"状态（type=1）
            ContractState cs = new ContractState();
            cs.setConNum(num);
            cs.setType(1); // 1-起草
            cs.setTime(new Date());
            stateDao.insert(cs);

            // 记录操作日志
            logDao.insert(new Log(0, contract.getUserName(), "起草合同: " + contract.getName() + "(" + num + ")", null));
        }
        return result;
    }

    /**
     * 分配合同处理人员
     * <p>为合同指定会签、审批、签订三个环节的处理人员</p>
     * <p>只有处于"起草"状态的合同才能进行分配</p>
     *
     * @param conNum           合同编号
     * @param countersignUsers 会签人员列表
     * @param approveUsers      审批人员列表
     * @param signUsers         签订人员列表
     * @return true-分配成功；false-合同状态不正确
     */
    public boolean assignContract(String conNum, List<String> countersignUsers, List<String> approveUsers, List<String> signUsers) {
        // 验证合同当前状态必须是"起草"才能分配
        ContractState state = stateDao.findLatestByConNum(conNum);
        if (state == null || state.getType() != 1) {
            return false;  // 状态不对，不允许分配
        }

        // 为每位会签人员创建流程记录（type=1表示会签）
        for (String userName : countersignUsers) {
            ContractProcess cp = new ContractProcess();
            cp.setConNum(conNum);
            cp.setType(1); // 会签类型
            cp.setState(0); // 初始状态：未完成
            cp.setUserName(userName);
            cp.setContent("");
            cp.setTime(new Date());
            processDao.insert(cp);
        }

        // 为每位审批人员创建流程记录（type=2表示审批）
        for (String userName : approveUsers) {
            ContractProcess cp = new ContractProcess();
            cp.setConNum(conNum);
            cp.setType(2); // 审批类型
            cp.setState(0); // 初始状态：未完成
            cp.setUserName(userName);
            cp.setContent("");
            cp.setTime(new Date());
            processDao.insert(cp);
        }

        // 为每位签订人员创建流程记录（type=3表示签订）
        for (String userName : signUsers) {
            ContractProcess cp = new ContractProcess();
            cp.setConNum(conNum);
            cp.setType(3); // 签订类型
            cp.setState(0); // 初始状态：未完成
            cp.setUserName(userName);
            cp.setContent("");
            cp.setTime(new Date());
            processDao.insert(cp);
        }

        logDao.insert(new Log(0, "admin", "分配合同: " + conNum, null));
        return true;
    }

    /**
     * 会签合同
     * <p>会签人员对合同发表会签意见</p>
     * <p>当所有会签人员都完成后，合同状态自动变为"会签完成"</p>
     *
     * @param processId 流程记录ID
     * @param opinion   会签意见
     * @return true-会签成功；false-会签失败
     */
    public boolean countersignContract(int processId, String opinion) {
        // 更新流程状态为已完成（state=1）
        boolean result = processDao.updateState(processId, 1, opinion);
        if (result) {
            ContractProcess cp = getProcessById(processId);
            if (cp != null) {
                // 检查该合同的所有会签节点是否都已完成
                List<ContractProcess> list = processDao.findByConNumAndType(cp.getConNum(), 1);
                boolean allDone = list.stream().allMatch(p -> p.getState() == 1);
                if (allDone) {
                    // 所有会签人都完成，更新合同状态为"会签完成"（type=2）
                    ContractState cs = new ContractState();
                    cs.setConNum(cp.getConNum());
                    cs.setType(2); // 会签完成
                    cs.setTime(new Date());
                    stateDao.insert(cs);
                }
                logDao.insert(new Log(0, cp.getUserName(), "会签合同: " + cp.getConNum(), null));
            }
        }
        return result;
    }

    /**
     * 定稿合同
     * <p>根据会签意见修改合同内容后形成终稿</p>
     * <p>只有"会签完成"状态的合同才能定稿</p>
     *
     * @param conNum   合同编号
     * @param content  定稿后的合同正文内容
     * @param userName 操作人用户名
     * @return true-定稿成功；false-状态不符或操作失败
     */
    public boolean finalizeContract(String conNum, String content, String userName) {
        // 验证合同状态必须为"会签完成"
        ContractState state = stateDao.findLatestByConNum(conNum);
        if (state == null || state.getType() != 2) {
            return false;
        }

        Contract contract = contractDao.findByNum(conNum);
        if (contract == null) {
            return false;
        }
        contract.setContent(content);  // 更新合同内容为定稿版本
        boolean result = contractDao.update(contract);
        if (result) {
            // 更新状态为"定稿完成"（type=3）
            ContractState cs = new ContractState();
            cs.setConNum(conNum);
            cs.setType(3); // 定稿完成
            cs.setTime(new Date());
            stateDao.insert(cs);

            logDao.insert(new Log(0, userName, "定稿合同: " + conNum, null));
        }
        return result;
    }

    /**
     * 审批合同
     * <p>领导对合同进行最终审批决策</p>
     * <ul>
     *   <li>审批通过：当所有审批人都通过后，状态变为"审批完成"</li>
     *   <li>审批否决：合同状态回退到"起草"，需要重新走流程</li>
     * </ul>
     *
     * @param processId 流程记录ID
     * @param approved  是否通过（true-通过, false-否决）
     * @param opinion   审批意见
     * @param userName  审批人用户名
     * @return true-操作成功；false-操作失败
     */
    public boolean approveContract(int processId, boolean approved, String opinion, String userName) {
        int newState = approved ? 1 : 2; // 1-已完成（通过）, 2-已否决
        boolean result = processDao.updateState(processId, newState, opinion);
        if (result) {
            ContractProcess cp = getProcessById(processId);
            if (cp != null) {
                if (approved) {
                    // 审批通过：检查所有审批人是否都已通过
                    List<ContractProcess> list = processDao.findByConNumAndType(cp.getConNum(), 2);
                    boolean allApproved = list.stream().allMatch(p -> p.getState() == 1);
                    if (allApproved) {
                        // 全部通过，状态变为"审批完成"（type=4）
                        ContractState cs = new ContractState();
                        cs.setConNum(cp.getConNum());
                        cs.setType(4); // 审批完成
                        cs.setTime(new Date());
                        stateDao.insert(cs);
                    }
                } else {
                    // 审批否决：状态回退到"起草"（type=1），需重新走流程
                    ContractState cs = new ContractState();
                    cs.setConNum(cp.getConNum());
                    cs.setType(1); // 回退到起草
                    cs.setTime(new Date());
                    stateDao.insert(cs);
                }
                logDao.insert(new Log(0, userName, (approved ? "审批通过" : "审批否决") + "合同: " + cp.getConNum(), null));
            }
        }
        return result;
    }

    /**
     * 签订合同
     * <p>正式签署合同，所有签订人完成后状态变为"签订完成"</p>
     *
     * @param processId 流程记录ID
     * @param info      签署信息/备注
     * @param userName  签署人用户名
     * @return true-签订成功；false-签订失败
     */
    public boolean signContract(int processId, String info, String userName) {
        boolean result = processDao.updateState(processId, 1, info);
        if (result) {
            ContractProcess cp = getProcessById(processId);
            if (cp != null) {
                // 检查所有签订人是否都已完成
                List<ContractProcess> list = processDao.findByConNumAndType(cp.getConNum(), 3);
                boolean allDone = list.stream().allMatch(p -> p.getState() == 1);
                if (allDone) {
                    // 全部签订完成，状态变为"签订完成"（type=5）
                    ContractState cs = new ContractState();
                    cs.setConNum(cp.getConNum());
                    cs.setType(5); // 签订完成
                    cs.setTime(new Date());
                    stateDao.insert(cs);
                }
                logDao.insert(new Log(0, userName, "签订合同: " + cp.getConNum(), null));
            }
        }
        return result;
    }

    /**
     * 根据ID获取流程记录
     *
     * @param id 流程记录ID
     * @return ContractProcess对象
     */
    private ContractProcess getProcessById(int id) {
        return processDao.findById(id);
    }

    /**
     * 获取合同当前状态的中文名称
     *
     * @param conNum 合同编号
     * @return 状态名称（如："起草"、"会签完成"等）；无记录返回"未知"
     */
    public String getContractStateName(String conNum) {
        ContractState state = stateDao.findLatestByConNum(conNum);
        if (state != null) {
            return state.getTypeName();  // 返回中文状态名
        }
        return "未知";
    }

    /**
     * 获取合同当前状态类型码
     *
     * @param conNum 合同编号
     * @return 状态类型码（1-5）；无记录返回0
     */
    public int getContractStateType(String conNum) {
        ContractState state = stateDao.findLatestByConNum(conNum);
        if (state != null) {
            return state.getType();
        }
        return 0;
    }

    /**
     * 根据合同编号查找合同
     */
    public Contract findByNum(String num) {
        return contractDao.findByNum(num);
    }

    /**
     * 获取所有合同列表
     */
    public List<Contract> findAll() {
        return contractDao.findAll();
    }

    /**
     * 根据合同名称模糊搜索
     */
    public List<Contract> findByName(String name) {
        return contractDao.findByName(name);
    }

    /**
     * 根据创建人查询合同
     */
    public List<Contract> findByUserName(String userName) {
        return contractDao.findByUserName(userName);
    }

    /**
     * 获取合同的所有流程记录
     */
    public List<ContractProcess> getContractProcesses(String conNum) {
        return processDao.findByConNum(conNum);
    }

    /**
     * 获取合同特定类型的流程记录
     */
    public List<ContractProcess> getContractProcessesByType(String conNum, int type) {
        return processDao.findByConNumAndType(conNum, type);
    }

    /**
     * 获取用户的待办任务（指定类型的未完成任务）
     */
    public List<ContractProcess> getUserPendingProcesses(String userName, int type) {
        return processDao.findByUserNameAndTypeAndState(userName, type, 0);
    }

    /**
     * 获取合同的状态变更历史
     */
    public List<ContractState> getContractStates(String conNum) {
        return stateDao.findByConNum(conNum);
    }

    /**
     * 根据状态类型查询合同
     */
    public List<ContractState> getContractsByState(int stateType) {
        return stateDao.findByType(stateType);
    }

    /**
     * 获取合同的附件列表
     */
    public List<ContractAttachment> getAttachments(String conNum) {
        return attachmentDao.findByConNum(conNum);
    }

    /**
     * 上传合同附件
     */
    public boolean addAttachment(ContractAttachment attachment) {
        return attachmentDao.insert(attachment);
    }

    /**
     * 获取待分配的合同列表
     * <p>条件：状态为"起草"且尚未分配处理人员的合同</p>
     *
     * @return 待分配的合同列表
     */
    public List<Contract> getUnassignedContracts() {
        List<Contract> result = new java.util.ArrayList<>();
        List<Contract> all = contractDao.findAll();
        for (Contract c : all) {
            ContractState state = stateDao.findLatestByConNum(c.getNum());
            if (state != null && state.getType() == 1) {
                // 状态为起草，进一步检查是否有流程记录
                List<ContractProcess> processes = processDao.findByConNum(c.getNum());
                if (processes.isEmpty()) {
                    // 无流程记录说明还未分配
                    result.add(c);
                }
            }
        }
        return result;
    }
}
