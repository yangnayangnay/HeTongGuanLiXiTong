package com.contract.entity;

import java.util.Date;

/**
 * 合同附件实体类
 * <p>
 * 用于管理与合同相关的电子附件文件。在合同管理过程中，往往需要上传各种支撑材料，
 * 如合同扫描件、资质证明、补充协议等。本实体用于记录这些附件的元数据信息。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>记录附件与合同的关联关系</li>
 *   <li>维护附件的文件名、存储路径、类型等信息</li>
 *   <li>记录附件的上传时间，用于版本管理</li>
 *   <li>支持附件的增删改查操作</li>
 * </ul>
 *
 * <h3>支持的附件类型：</h3>
 * <ul>
 *   <li>合同正文：合同的主要文档（PDF、Word等）</li>
 *   <li>资质证明：营业执照、资质证书等</li>
 *   <li>补充协议：对主合同的补充或修订</li>
 *   <li>其他材料：其他相关支撑文件</li>
 * </ul>
 *
 * <h3>存储策略：</h3>
 * <p>
 * 文件本身存储在服务器文件系统中，path字段记录文件的相对或绝对路径。
 * 数据库仅保存文件的元数据，不直接存储文件二进制内容。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractAttachment {
    /** 附件记录唯一标识符，自增主键 */
    private int id;
    /**
     * 关联的合同编号
     * <p>对应Contract表的num字段，用于关联到具体的合同</p>
     */
    private String conNum;
    /**
     * 原始文件名
     * <p>用户上传时的原始文件名，包含扩展名</p>
     * <p>例如："合同正文_2024版.pdf"</p>
     */
    private String fileName;
    /**
     * 文件存储路径
     * <p>文件在服务器上的实际存储位置（绝对路径或相对路径）</p>
     * <p>例如："/uploads/contracts/HT2024001/附件1.pdf"</p>
     */
    private String path;
    /**
     * 附件类型/分类
     * <p>用于区分不同用途的附件</p>
     * <p>例如："合同正文"、"资质证明"、"补充协议"等</p>
     */
    private String type;
    /**
     * 上传时间
     * <p>记录文件上传到系统的时间戳</p>
     * <p>用于确定附件版本和排序显示</p>
     */
    private Date uploadTime;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public ContractAttachment() {}

    /**
     * 完整构造方法
     * <p>创建附件对象并设置所有属性</p>
     *
     * @param id         附件记录唯一标识
     * @param conNum     关联的合同编号
     * @param fileName   原始文件名
     * @param path       文件存储路径
     * @param type       附件类型
     * @param uploadTime 上传时间
     */
    public ContractAttachment(int id, String conNum, String fileName, String path, String type, Date uploadTime) {
        this.id = id;
        this.conNum = conNum;
        this.fileName = fileName;
        this.path = path;
        this.type = type;
        this.uploadTime = uploadTime;
    }

    /**
     * 获取附件记录ID
     * @return 附件记录唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置附件记录ID
     * @param id 附件记录唯一标识符
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
     * 获取文件名
     * @return 原始文件名
     */
    public String getFileName() { return fileName; }

    /**
     * 设置文件名
     * @param fileName 原始文件名
     */
    public void setFileName(String fileName) { this.fileName = fileName; }

    /**
     * 获取文件路径
     * @return 文件存储路径
     */
    public String getPath() { return path; }

    /**
     * 设置文件路径
     * @param path 文件存储路径
     */
    public void setPath(String path) { this.path = path; }

    /**
     * 获取附件类型
     * @return 附件类型描述
     */
    public String getType() { return type; }

    /**
     * 设置附件类型
     * @param type 附件类型描述
     */
    public void setType(String type) { this.type = type; }

    /**
     * 获取上传时间
     * @return 上传时间戳
     */
    public Date getUploadTime() { return uploadTime; }

    /**
     * 设置上传时间
     * @param uploadTime 上传时间戳
     */
    public void setUploadTime(Date uploadTime) { this.uploadTime = uploadTime; }
}
