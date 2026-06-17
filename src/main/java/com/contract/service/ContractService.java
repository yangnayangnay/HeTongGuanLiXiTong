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
import com.contract.entity.User;
import com.contract.util.NetworkUtil;
import com.contract.util.FileLogger;
import com.contract.util.EmailService;
import com.contract.util.DBUtil;
import com.contract.dao.UserDao;
import com.contract.service.ContractVersionService;
import org.springframework.stereotype.Service;

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
@Service
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
    /** 用户数据访问对象 */
    private UserDao userDao = new UserDao();
    /** 版本控制服务 */
    private ContractVersionService versionService = new ContractVersionService();

    /**
     * 起草合同
     * <p>创建新的合同记录，生成唯一的合同编号，并将状态设为"起草"</p>
     * <p>合同编号格式：HT + 年月日 + 4位流水号（如HT202401010001）</p>
     *
     * @param contract 合同对象（包含名称、客户、时间等信息）
     * @return true-起草成功；false-起草失败
     *
     * [REST-API] POST /api/contracts
     */
    public boolean draftContract(Contract contract) {
        FileLogger.info("ContractService", "draftContract", "开始起草合同, 合同名称: " + contract.getName() + ", 客户: " + contract.getCustomer());
        // 生成合同编号：HT + 当前日期 + 序列号，确保唯一
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new Date());
        String num;
        int seq = DBUtil.getNextId("seq_contract");
        num = "HT" + datePart + String.format("%04d", seq);
        // 确保编号唯一，如果已存在则追加序号
        while (contractDao.findByNum(num) != null) {
            seq++;
            num = "HT" + datePart + String.format("%04d", seq);
        }
        contract.setNum(num);
        FileLogger.info("ContractService", "draftContract", "生成合同编号: " + num);

        // 将合同内容中的合同编号占位符替换为实际编号
        String contractContent = contract.getContent();
        if (contractContent != null && !contractContent.isEmpty()) {
            contractContent = contractContent.replace("{{合同编号}}", num);
            contractContent = contractContent.replace("{{contractNo}}", num);
            contractContent = contractContent.replace("{{编号}}", num);
            contract.setContent(contractContent);
        }

        boolean result = contractDao.insert(contract);
        if (result) {
            // 插入状态记录：标记为"起草"状态（type=1）
            ContractState cs = new ContractState();
            cs.setConNum(num);
            cs.setType(1); // 1-起草
            cs.setTime(new Date());
            stateDao.insert(cs);
            FileLogger.info("ContractService", "draftContract", "状态变更: 合同创建 -> 起草, 合同编号: " + num);

            // 记录操作日志（含IP和变更信息）
            Log draftLog = new Log(0, contract.getUserName(), "起草合同: " + contract.getName() + "(" + num + ")", null);
            draftLog.setIpAddress(NetworkUtil.getLocalIPAddress());
            draftLog.setOldValue("合同不存在");
            draftLog.setNewValue("状态=起草");
            logDao.insert(draftLog);
            FileLogger.info("ContractService", "draftContract", "起草合同成功, 合同编号: " + num);
        } else {
            FileLogger.error("ContractService", "draftContract", "起草合同失败, 合同名称: " + contract.getName(), null);
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
     *
     * [REST-API] POST /api/contracts/{conNum}/assign
     */
    public boolean assignContract(String conNum, List<String> countersignUsers, List<String> approveUsers, List<String> signUsers) {
        FileLogger.info("ContractService", "assignContract", "开始分配合同人员, 合同编号: " + conNum + ", 会签人数: " + countersignUsers.size() + ", 审批人数: " + approveUsers.size() + ", 签订人数: " + signUsers.size());
        ContractState state = stateDao.findLatestByConNum(conNum);
        if (state == null || state.getType() != 1) {
            FileLogger.info("ContractService", "assignContract", "分配失败, 合同状态不正确, 合同编号: " + conNum + ", 当前状态: " + (state != null ? state.getType() : "无"));
            return false;
        }
        List<ContractProcess> existingProcesses = processDao.findByConNum(conNum);
        if (existingProcesses != null && !existingProcesses.isEmpty()) {
            FileLogger.warn("ContractService", "assignContract", "分配失败, 合同已分配过人员, 合同编号: " + conNum);
            return false;
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

        // 发送邮件通知会签人员（附带合同附件）
        Contract contractForEmail = contractDao.findByNum(conNum);
        for (String userName : countersignUsers) {
            try {
                com.contract.entity.User u = userDao.findByName(userName);
                if (u != null && u.getEmail() != null) {
                    sendEmailWithContract(u.getEmail(), userName, conNum,
                        contractForEmail != null ? contractForEmail.getName() : conNum,
                        "会签", contractForEmail);
                }
            } catch (Exception e) {
                FileLogger.error("ContractService", "assignContract", "发送邮件失败: " + userName + ", " + e.getMessage(), e);
            }
        }

        // 为每位审批人员创建流程记录（type=2表示审批）
        // 注意：审批人员此时不发送邮件通知，等合同定稿后才通知
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
        // 注意：签订人员此时不发送邮件通知，等合同审批通过后才通知
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

        FileLogger.info("ContractService", "assignContract", "分配合同人员成功, 合同编号: " + conNum);
        Log assignLog = new Log(0, "admin", "分配合同: " + conNum, null);
        assignLog.setIpAddress(NetworkUtil.getLocalIPAddress());
        assignLog.setOldValue("状态=起草（未分配）");
        assignLog.setNewValue("已分配会签/审批/签订人员");
        logDao.insert(assignLog);
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
     *
     * [REST-API] POST /api/contracts/{conNum}/countersign
     */
    public boolean countersignContract(int processId, String opinion) {
        FileLogger.info("ContractService", "countersignContract", "开始会签合同, 流程ID: " + processId);
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
                    FileLogger.info("ContractService", "countersignContract", "状态变更: 会签中 -> 会签完成, 合同编号: " + cp.getConNum());
                } else {
                    FileLogger.info("ContractService", "countersignContract", "会签进行中, 合同编号: " + cp.getConNum() + ", 尚有未完成会签");
                }
                Log csLog = new Log(0, cp.getUserName(), "会签合同: " + cp.getConNum(), null);
                csLog.setIpAddress(NetworkUtil.getLocalIPAddress());
                csLog.setOldValue("状态=会签中");
                csLog.setNewValue("状态=会签完成");
                logDao.insert(csLog);
                FileLogger.info("ContractService", "countersignContract", "会签成功, 合同编号: " + cp.getConNum() + ", 操作人: " + cp.getUserName());
            }
        } else {
            FileLogger.error("ContractService", "countersignContract", "会签失败, 流程ID: " + processId, null);
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
     *
     * [REST-API] POST /api/contracts/{conNum}/finalize
     */
    public boolean finalizeContract(String conNum, String content, String userName) {
        FileLogger.info("ContractService", "finalizeContract", "开始定稿合同, 合同编号: " + conNum + ", 操作人: " + userName);
        // 验证合同状态必须为"会签完成"
        ContractState state = stateDao.findLatestByConNum(conNum);
        if (state == null || state.getType() != 2) {
            FileLogger.info("ContractService", "finalizeContract", "定稿失败, 合同状态不正确, 合同编号: " + conNum + ", 当前状态: " + (state != null ? state.getType() : "无"));
            return false;
        }

        Contract contract = contractDao.findByNum(conNum);
        if (contract == null) {
            FileLogger.info("ContractService", "finalizeContract", "定稿失败, 合同不存在, 合同编号: " + conNum);
            return false;
        }
        // 保存定稿前版本
        try {
            Contract oldContract = contractDao.findByNum(conNum);
            if (oldContract != null && oldContract.getContent() != null && !oldContract.getContent().equals(content)) {
                versionService.saveVersion(conNum, oldContract.getContent(),
                    oldContract.getFileData(), oldContract.getFileName(), userName, "定稿前自动保存版本");
            }
        } catch (Exception e) {
            FileLogger.error("ContractService", "finalizeContract", "保存版本失败: " + e.getMessage(), e);
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
            FileLogger.info("ContractService", "finalizeContract", "状态变更: 会签完成 -> 定稿完成, 合同编号: " + conNum);

            // 定稿完成后通知审批人员
            List<ContractProcess> approveList = processDao.findByConNumAndType(conNum, 2);
            for (ContractProcess ap : approveList) {
                if (ap.getState() == 0) { // 只通知待处理的审批人员
                    try {
                        User u = userDao.findByName(ap.getUserName());
                        if (u != null && u.getEmail() != null) {
                            sendEmailWithContract(u.getEmail(), ap.getUserName(), conNum,
                                contract.getName(), "审批", contract);
                        }
                    } catch (Exception e) {
                        FileLogger.error("ContractService", "finalizeContract", "通知审批人员失败: " + e.getMessage(), e);
                    }
                }
            }

            Log finalizeLog = new Log(0, userName, "定稿合同: " + conNum, null);
            finalizeLog.setIpAddress(NetworkUtil.getLocalIPAddress());
            finalizeLog.setOldValue("状态=会签完成");
            finalizeLog.setNewValue("状态=定稿完成");
            logDao.insert(finalizeLog);
            FileLogger.info("ContractService", "finalizeContract", "定稿成功, 合同编号: " + conNum);
        } else {
            FileLogger.error("ContractService", "finalizeContract", "定稿失败, 合同编号: " + conNum, null);
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
     *
     * [REST-API] POST /api/contracts/{conNum}/approve
     */
    public boolean approveContract(int processId, boolean approved, String opinion, String userName) {
        FileLogger.warn("ContractService", "approveContract", "开始审批合同, 流程ID: " + processId + ", 是否通过: " + approved + ", 审批人: " + userName);
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
                        FileLogger.info("ContractService", "approveContract", "状态变更: 定稿完成 -> 审批完成, 合同编号: " + cp.getConNum());

                        // 审批全部通过后通知签订人员
                        Contract contract = contractDao.findByNum(cp.getConNum());
                        String contractName = contract != null ? contract.getName() : cp.getConNum();
                        List<ContractProcess> signList = processDao.findByConNumAndType(cp.getConNum(), 3);
                        for (ContractProcess sp : signList) {
                            if (sp.getState() == 0) {
                                try {
                                    User u = userDao.findByName(sp.getUserName());
                                    if (u != null && u.getEmail() != null) {
                                        sendEmailWithContract(u.getEmail(), sp.getUserName(), cp.getConNum(),
                                            contractName, "签订", contract);
                                    }
                                } catch (Exception e) {
                                    FileLogger.error("ContractService", "approveContract", "通知签订人员失败: " + e.getMessage(), e);
                                }
                            }
                        }
                    } else {
                        FileLogger.info("ContractService", "approveContract", "审批进行中, 合同编号: " + cp.getConNum() + ", 尚有未完成审批");
                    }
                } else {
                    // 审批否决：状态回退到"会签完成"（type=2），需重新定稿
                    ContractState cs = new ContractState();
                    cs.setConNum(cp.getConNum());
                    cs.setType(2); // 回退到会签完成
                    cs.setTime(new Date());
                    stateDao.insert(cs);
                    FileLogger.warn("ContractService", "approveContract", "状态变更: 定稿完成 -> 会签完成（审批否决回退）, 合同编号: " + cp.getConNum());

                    // 为每个审批人创建新的待处理记录，保留否决意见作为历史
                    List<ContractProcess> approveList = processDao.findByConNumAndType(cp.getConNum(), 2);
                    for (ContractProcess ap : approveList) {
                        if (ap.getState() == 1 || ap.getState() == 2) {
                            ContractProcess newProcess = new ContractProcess();
                            newProcess.setConNum(cp.getConNum());
                            newProcess.setType(2);
                            newProcess.setState(0);
                            newProcess.setUserName(ap.getUserName());
                            newProcess.setContent("");
                            newProcess.setTime(new Date());
                            processDao.insert(newProcess);
                        }
                    }
                    FileLogger.info("ContractService", "approveContract", "已为审批人创建新待处理记录, 合同编号: " + cp.getConNum());

                    // 通知起草人和会签人员
                    Contract contract = contractDao.findByNum(cp.getConNum());
                    String contractName = contract != null ? contract.getName() : cp.getConNum();
                    // 通知起草人
                    if (contract != null && contract.getUserName() != null) {
                        try {
                            User drafter = userDao.findByName(contract.getUserName());
                            if (drafter != null && drafter.getEmail() != null) {
                                EmailService.sendTaskNotification(drafter.getEmail(), contract.getUserName(),
                                    cp.getConNum(), contractName, "审批否决通知");
                            }
                        } catch (Exception ex) {
                            FileLogger.error("ContractService", "approveContract", "通知起草人失败: " + ex.getMessage(), ex);
                        }
                    }
                    List<ContractProcess> countersignList = processDao.findByConNumAndType(cp.getConNum(), 1);
                    for (ContractProcess csp : countersignList) {
                        try {
                            User csUser = userDao.findByName(csp.getUserName());
                            if (csUser != null && csUser.getEmail() != null) {
                                EmailService.sendTaskNotification(csUser.getEmail(), csp.getUserName(),
                                    cp.getConNum(), contractName, "审批否决通知");
                            }
                        } catch (Exception ex) {
                            FileLogger.error("ContractService", "approveContract", "通知会签人失败: " + ex.getMessage(), ex);
                        }
                    }
                }
                Log approveLog = new Log(0, userName, (approved ? "审批通过" : "审批否决") + "合同: " + cp.getConNum(), null);
                approveLog.setIpAddress(NetworkUtil.getLocalIPAddress());
                if (approved) {
                    approveLog.setOldValue("状态=定稿完成");
                    approveLog.setNewValue("状态=审批完成");
                } else {
                    approveLog.setOldValue("状态=定稿完成");
                    approveLog.setNewValue("状态=会签完成（审批否决回退）");
                }
                logDao.insert(approveLog);
                FileLogger.info("ContractService", "approveContract", "审批操作完成, 合同编号: " + cp.getConNum() + ", 结果: " + (approved ? "通过" : "否决"));
            }
        } else {
            FileLogger.error("ContractService", "approveContract", "审批操作失败, 流程ID: " + processId, null);
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
     *
     * [REST-API] POST /api/contracts/{conNum}/sign
     */
    public boolean signContract(int processId, String info, String userName) {
        FileLogger.warn("ContractService", "signContract", "开始签订合同, 流程ID: " + processId + ", 签署人: " + userName);
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
                    FileLogger.info("ContractService", "signContract", "状态变更: 审批完成 -> 签订完成, 合同编号: " + cp.getConNum());
                } else {
                    FileLogger.info("ContractService", "signContract", "签订进行中, 合同编号: " + cp.getConNum() + ", 尚有未完成签订");
                }
                Log signLog = new Log(0, userName, "签订合同: " + cp.getConNum(), null);
            signLog.setIpAddress(NetworkUtil.getLocalIPAddress());
            signLog.setOldValue("状态=审批完成");
            signLog.setNewValue("状态=签订完成");
            logDao.insert(signLog);
            FileLogger.info("ContractService", "signContract", "签订成功, 合同编号: " + cp.getConNum() + ", 签署人: " + userName);
            }
        } else {
            FileLogger.error("ContractService", "signContract", "签订失败, 流程ID: " + processId, null);
        }
        return result;
    }

    /**
     * 将签名信息插入到合同内容的对应位置
     * 合同内容中应包含【甲方签名】和【乙方签名】标记，签名将替换对应标记
     * 如果没有标记，则在内容末尾追加签名区
     */
    public void insertSignatureToContract(String conNum, String party, String userName, String signInfo) {
        Contract contract = contractDao.findByNum(conNum);
        if (contract == null) return;
        String content = contract.getContent();
        if (content == null) content = "";

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

        String signatureContent;
        if (signInfo.contains("[签名图片]") && signInfo.contains("data:")) {
            String imgUrl = signInfo.replace("[签名图片]", "").trim();
            signatureContent = "\n" + party + "签名: " + userName + " (" + timestamp + ")\n" +
                "<img src=\"" + imgUrl + "\" alt=\"" + party + "签名\" style=\"max-height:80px;border:1px solid #ccc;border-radius:4px;padding:2px;display:block;\">\n";
        } else {
            signatureContent = "\n" + party + "签名: " + userName + " (" + timestamp + ")\n";
        }

        String marker = "【" + party + "签名】";
        if (content.contains(marker)) {
            content = content.replace(marker, signatureContent);
        } else {
            if (!content.contains("甲方签名:") && !content.contains("乙方签名:")) {
                content += "\n\n========== 签名区 ==========\n";
            }
            content += "\n" + signatureContent;
        }

        contract.setContent(content);

        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();
            com.itextpdf.text.pdf.BaseFont bfChinese = com.itextpdf.text.pdf.BaseFont.createFont(
                "STSong-Light", "UniGB-UCS2-H", com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            com.itextpdf.text.Font fontChinese = new com.itextpdf.text.Font(bfChinese, 12, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fontTitle = new com.itextpdf.text.Font(bfChinese, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(contract.getName() != null ? contract.getName() : "合同", fontTitle);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) { document.add(new com.itextpdf.text.Paragraph(" ", fontChinese)); continue; }
                if (line.contains("<img") && line.contains("src=")) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("src=\"([^\"]+)\"").matcher(line);
                    if (m.find()) {
                        String src = m.group(1);
                        try {
                            if (src.startsWith("data:image")) {
                                String[] parts = src.split(",", 2);
                                if (parts.length == 2) {
                                    byte[] imgBytes = java.util.Base64.getDecoder().decode(parts[1]);
                                    com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(imgBytes);
                                    img.scaleToFit(150, 60);
                                    document.add(img);
                                }
                            }
                        } catch (Exception imgEx) {
                            document.add(new com.itextpdf.text.Paragraph("[签名图片]", fontChinese));
                        }
                    }
                } else {
                    document.add(new com.itextpdf.text.Paragraph(line, fontChinese));
                }
            }
            document.close();
            contract.setFileData(baos.toByteArray());
            contract.setFileName(contract.getName() != null ? contract.getName() + "_signed.pdf" : "contract_signed.pdf");
            contract.setFileType("pdf");
        } catch (Exception e) {
            FileLogger.error("ContractService", "insertSignatureToContract", "生成签名PDF失败: " + e.getMessage(), e);
        }

        contractDao.update(contract);
        FileLogger.info("ContractService", "insertSignatureToContract",
            "签名已插入合同并更新PDF, 编号: " + conNum + ", 签约方: " + party + ", 签署人: " + userName);
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
        FileLogger.info("ContractService", "getContractStateName", "获取合同状态名称, 合同编号: " + conNum);
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

    public java.util.Map<String, Integer> getAllContractStateTypes() {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        try {
            java.sql.Connection conn = DBUtil.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("SELECT conNum, type FROM (SELECT conNum, type, ROW_NUMBER() OVER (PARTITION BY conNum ORDER BY id DESC) rn FROM t_contract_state) sub WHERE sub.rn = 1");
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("conNum"), rs.getInt("type"));
            }
            DBUtil.close(conn, ps, rs);
            FileLogger.info("ContractService", "getAllContractStateTypes", "批量查询合同状态完成, 共" + map.size() + "条");
        } catch (Exception e) {
            FileLogger.error("ContractService", "getAllContractStateTypes", "批量查询失败，回退逐个查询: " + e.getMessage(), e);
            List<Contract> all = contractDao.findAll();
            for (Contract c : all) {
                ContractState state = stateDao.findLatestByConNum(c.getNum());
                if (state != null) {
                    map.put(c.getNum(), state.getType());
                }
            }
        }
        return map;
    }

    /**
     * 根据合同编号查找合同
     */
    public Contract findByNum(String num) {
        FileLogger.info("ContractService", "findByNum", "根据编号查找合同, 合同编号: " + num);
        return contractDao.findByNum(num);
    }

    public boolean updateContract(Contract contract) {
        return contractDao.update(contract);
    }

    /**
     * 发送邮件并附带合同文件附件
     * 如果合同有附件则附带，没有附件则将合同内容作为txt附件
     */
    private void sendEmailWithContract(String toEmail, String userName, String conNum,
                                        String contractName, String taskType, Contract contract) {
        byte[] attachmentData = null;
        String attachmentName = null;
        // 只有实际上传了合同文件才附附件，没有文件则发纯文本邮件
        if (contract != null && contract.getFileData() != null && contract.getFileData().length > 0) {
            attachmentData = contract.getFileData();
            attachmentName = contract.getFileName() != null ? contract.getFileName() : contractName + ".bin";
        }
        if (attachmentData != null) {
            EmailService.sendTaskNotificationWithAttachment(toEmail, userName, conNum, contractName, taskType, attachmentData, attachmentName);
        } else {
            EmailService.sendWithRetry(toEmail, userName, conNum, contractName, taskType, 5, 5000);
        }
    }

    /**
     * 获取所有合同列表
     *
     * [REST-API] GET /api/contracts
     */
    public List<Contract> findAll() {
        FileLogger.info("ContractService", "findAll", "查询所有合同");
        return contractDao.findAll();
    }

    /**
     * 根据合同名称模糊搜索
     *
     * [REST-API] GET /api/contracts?name=xxx
     */
    public List<Contract> findByName(String name) {
        FileLogger.info("ContractService", "findByName", "根据名称模糊搜索合同, 关键词: " + name);
        return contractDao.findByName(name);
    }

    /**
     * 根据创建人查询合同
     */
    public List<Contract> findByUserName(String userName) {
        FileLogger.info("ContractService", "findByUserName", "根据创建人查询合同, 创建人: " + userName);
        return contractDao.findByUserName(userName);
    }

    /**
     * 获取合同的所有流程记录
     */
    public List<ContractProcess> getContractProcesses(String conNum) {
        FileLogger.info("ContractService", "getContractProcesses", "获取合同流程记录, 合同编号: " + conNum);
        return processDao.findByConNum(conNum);
    }

    /**
     * 获取合同特定类型的流程记录
     */
    public List<ContractProcess> getContractProcessesByType(String conNum, int type) {
        FileLogger.info("ContractService", "getContractProcessesByType", "获取合同特定类型流程, 合同编号: " + conNum + ", 类型: " + type);
        return processDao.findByConNumAndType(conNum, type);
    }

    /**
     * 获取用户的待办任务（指定类型的未完成任务）
     */
    public List<ContractProcess> getUserPendingProcesses(String userName, int type) {
        FileLogger.info("ContractService", "getUserPendingProcesses", "获取用户待办任务, 用户: " + userName + ", 类型: " + type);
        return processDao.findByUserNameAndTypeAndState(userName, type, 0);
    }

    /**
     * 获取合同的状态变更历史
     */
    public List<ContractState> getContractStates(String conNum) {
        FileLogger.info("ContractService", "getContractStates", "获取合同状态历史, 合同编号: " + conNum);
        return stateDao.findByConNum(conNum);
    }

    /**
     * 根据状态类型查询合同
     */
    public List<ContractState> getContractsByState(int stateType) {
        FileLogger.info("ContractService", "getContractsByState", "根据状态类型查询合同, 状态类型: " + stateType);
        return stateDao.findByType(stateType);
    }

    /**
     * 获取合同的附件列表
     */
    public List<ContractAttachment> getAttachments(String conNum) {
        FileLogger.info("ContractService", "getAttachments", "获取合同附件, 合同编号: " + conNum);
        return attachmentDao.findByConNum(conNum);
    }

    /**
     * 上传合同附件
     */
    public boolean addAttachment(ContractAttachment attachment) {
        FileLogger.info("ContractService", "addAttachment", "上传合同附件, 合同编号: " + attachment.getConNum() + ", 文件名: " + attachment.getFileName());
        boolean result = attachmentDao.insert(attachment);
        if (result) {
            FileLogger.info("ContractService", "addAttachment", "上传附件成功, 合同编号: " + attachment.getConNum());
        } else {
            FileLogger.error("ContractService", "addAttachment", "上传附件失败, 合同编号: " + attachment.getConNum(), null);
        }
        return result;
    }

    /**
     * 获取待分配的合同列表
     * <p>条件：状态为"起草"且尚未分配处理人员的合同</p>
     *
     * @return 待分配的合同列表
     */
    public List<Contract> getUnassignedContracts() {
        FileLogger.info("ContractService", "getUnassignedContracts", "获取待分配合同列表");
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
        FileLogger.info("ContractService", "getUnassignedContracts", "查询完成, 待分配合同数: " + result.size());
        return result;
    }
}
