package com.contract.entity;

import java.util.Date;

/**
 * 合同版本历史实体类
 * <p>
 * 用于记录合同每次修改时的版本快照，支持版本对比和回滚功能。
 * 每次合同被修改（起草、定稿、审批通过、签订）时自动保存一个版本。
 * </p>
 *
 * @author 合同管理系统
 * @version 2.0
 */
public class ContractVersion {
    /** 版本记录唯一标识符 */
    private int id;
    /** 关联的合同编号 */
    private String contractNum;
    /** 版本号（1,2,3...递增） */
    private int versionNo;
    /** 该版本的合同正文内容 */
    private String content;
    /** 该版本的附件二进制数据 */
    private byte[] fileData;
    /** 该版本的附件文件名 */
    private String fileName;
    /** 修改人用户名 */
    private String modifier;
    /** 修改时间 */
    private Date modifyTime;
    /** 变更摘要描述 */
    private String changeSummary;

    public ContractVersion() {}

    // ===== Getter和Setter方法 =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContractNum() { return contractNum; }
    public void setContractNum(String contractNum) { this.contractNum = contractNum; }

    public int getVersionNo() { return versionNo; }
    public void setVersionNo(int versionNo) { this.versionNo = versionNo; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }

    public Date getModifyTime() { return modifyTime; }
    public void setModifyTime(Date modifyTime) { this.modifyTime = modifyTime; }

    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }
}
