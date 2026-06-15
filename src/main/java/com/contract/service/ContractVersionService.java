package com.contract.service;

import com.contract.entity.ContractVersion;
import com.contract.dao.ContractVersionDao;
import com.contract.util.FileLogger;

import java.util.List;

/**
 * 合同版本控制服务
 * <p>管理合同的历史版本记录，支持版本对比和回滚</p>
 * <p>在合同每次被修改时（起草、定稿、审批通过、签订）自动保存版本快照</p>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class ContractVersionService {

    /** 版本数据访问对象 */
    private ContractVersionDao versionDao;

    public ContractVersionService() {
        versionDao = new ContractVersionDao();
    }

    /**
     * 保存新版本（在每次修改合同时自动调用）
     *
     * @param conNum   合同编号
     * @param content  合同内容
     * @param fileData 附件数据
     * @param fileName 附件名
     * @param modifier 修改人
     * @param summary  变更摘要
     * @return true-保存成功；false-失败
     */
    public boolean saveVersion(String conNum, String content, byte[] fileData,
            String fileName, String modifier, String summary) {
        FileLogger.info("ContractVersionService", "saveVersion", "开始保存版本, 合同编号: " + conNum + ", 修改人: " + modifier + ", 变更摘要: " + summary);
        // 获取下一个版本号
        int nextVersion = versionDao.getNextVersionNumber(conNum);
        FileLogger.info("ContractVersionService", "saveVersion", "获取下一版本号: " + nextVersion + ", 合同编号: " + conNum);
        // 构建版本对象
        ContractVersion ver = new ContractVersion();
        ver.setContractNum(conNum);
        ver.setVersionNo(nextVersion);
        ver.setContent(content);
        ver.setFileData(fileData);
        ver.setFileName(fileName);
        ver.setModifier(modifier);
        ver.setChangeSummary(summary);
        // 插入版本记录
        boolean result = versionDao.insert(ver);
        if (result) {
            FileLogger.info("ContractVersionService", "saveVersion", "保存版本成功, 合同编号: " + conNum + ", 版本号: " + nextVersion);
        } else {
            FileLogger.error("ContractVersionService", "saveVersion", "保存版本失败, 合同编号: " + conNum, null);
        }
        return result;
    }

    /**
     * 获取某合同的所有版本历史（按版本号升序）
     *
     * @param conNum 合同编号
     * @return 版本列表
     */
    public List<ContractVersion> getVersions(String conNum) {
        FileLogger.info("ContractVersionService", "getVersions", "获取版本历史, 合同编号: " + conNum);
        return versionDao.findByContractNum(conNum);
    }

    /**
     * 获取最新版本
     *
     * @param conNum 合同编号
     * @return 最新版本对象；无版本记录返回null
     */
    public ContractVersion getLatestVersion(String conNum) {
        FileLogger.info("ContractVersionService", "getLatestVersion", "获取最新版本, 合同编号: " + conNum);
        List<ContractVersion> versions = getVersions(conNum);
        if (versions.isEmpty()) {
            FileLogger.info("ContractVersionService", "getLatestVersion", "无版本记录, 合同编号: " + conNum);
            return null;
        }
        ContractVersion latest = versions.get(versions.size() - 1);  // 返回最后一个（最大版本号）
        FileLogger.info("ContractVersionService", "getLatestVersion", "最新版本号: " + latest.getVersionNo() + ", 合同编号: " + conNum);
        return latest;
    }

    /**
     * 版本对比：返回两版本的差异描述
     * <p>采用简单的逐行比对算法，逐行比较两个版本的内容差异</p>
     *
     * @param conNum 合同编号
     * @param v1     第一个版本号
     * @param v2     第二个版本号
     * @return 差异文本描述；版本不存在时返回错误提示
     */
    public String compareVersions(String conNum, int v1, int v2) {
        FileLogger.info("ContractVersionService", "compareVersions", "开始版本对比, 合同编号: " + conNum + ", 版本1: " + v1 + ", 版本2: " + v2);
        ContractVersion ver1 = versionDao.findByVersionNo(conNum, v1);
        ContractVersion ver2 = versionDao.findByVersionNo(conNum, v2);
        if (ver1 == null || ver2 == null) {
            FileLogger.info("ContractVersionService", "compareVersions", "版本不存在, 合同编号: " + conNum + ", 版本1存在: " + (ver1 != null) + ", 版本2存在: " + (ver2 != null));
            return "版本不存在";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 版本 ").append(v1).append(" vs 版本 ").append(v2).append(" ===\n\n");

        // 将内容按行分割进行比对
        String[] lines1 = ver1.getContent() != null ? ver1.getContent().split("\n") : new String[0];
        String[] lines2 = ver2.getContent() != null ? ver2.getContent().split("\n") : new String[0];

        int maxLen = Math.max(lines1.length, lines2.length);
        int changes = 0;
        for (int i = 0; i < maxLen; i++) {
            String l1 = i < lines1.length ? lines1[i] : "[已删除]";
            String l2 = i < lines2.length ? lines2[i] : "[新增]";
            if (!l1.equals(l2)) {
                sb.append("行").append(i + 1).append(": \n");
                sb.append("  -v").append(v1).append(": ").append(l1).append("\n");
                sb.append("  +v").append(v2).append(": ").append(l2).append("\n");
                changes++;
            }
        }
        sb.append("\n共发现 ").append(changes).append(" 处差异");
        FileLogger.info("ContractVersionService", "compareVersions", "版本对比完成, 合同编号: " + conNum + ", 差异数: " + changes);
        return sb.toString();
    }

    /**
     * 删除某合同的所有版本记录
     *
     * @param conNum 合同编号
     */
    public void deleteVersions(String conNum) {
        FileLogger.info("ContractVersionService", "deleteVersions", "开始删除合同版本记录, 合同编号: " + conNum);
        boolean result = versionDao.deleteByContractNum(conNum);
        if (result) {
            FileLogger.info("ContractVersionService", "deleteVersions", "删除版本记录成功, 合同编号: " + conNum);
        } else {
            FileLogger.error("ContractVersionService", "deleteVersions", "删除版本记录失败, 合同编号: " + conNum, null);
        }
    }
}
