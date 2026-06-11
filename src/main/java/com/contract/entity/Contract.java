package com.contract.entity;

import java.util.Date;

/**
 * 合同实体类
 * <p>
 * 用于表示合同管理系统中的核心业务对象——合同。合同是整个系统的中心，
 * 所有的业务流程（起草、会签、定稿、审批、签订）都围绕合同展开。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>存储合同的基本信息（编号、名称、客户、时间范围等）</li>
 *   <li>记录合同的详细内容条款</li>
 *   <li>维护合同的创建人信息，用于责任追溯</li>
 *   <li>支持合同的全生命周期管理</li>
 * </ul>
 *
 * <h3>业务流程：</h3>
 * <pre>
 * 起草 → 会签 → 定稿 → 审批 → 签订 → 归档
 * </pre>
 *
 * <h3>数据关系：</h3>
 * <ul>
 *   <li>与Customer关联：通过customer字段关联签约客户</li>
 *   <li>与User关联：通过userName字段关联创建人</li>
 *   <li>与ContractProcess关联：通过num字段关联操作流程</li>
 *   <li>与ContractState关联：通过num字段关联状态变更</li>
 *   <li>与ContractAttachment关联：通过num字段关联附件</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class Contract {
    /** 合同唯一标识符，自增主键 */
    private int id;
    /**
     * 合同编号
     * <p>业务唯一标识，用于业务引用和查询</p>
     * <p>通常按年份+流水号生成（如：HT2024001）</p>
     */
    private String num;
    /** 合同名称/标题，简要概括合同内容 */
    private String name;
    /**
     * 签约客户名称
     * <p>关联Customer表的name字段</p>
     * <p>表示本合同的签约对方</p>
     */
    private String customer;
    /**
     * 合同生效日期
     * <p>合同正式开始执行的日期</p>
     * <p>在此日期之前合同可能处于草拟或审批阶段</p>
     */
    private Date beginTime;
    /**
     * 合同终止日期
     * <p>合同到期日，用于合同续签提醒和归档处理</p>
     */
    private Date endTime;
    /**
     * 合同正文内容
     * <p>存储合同的具体条款和内容</p>
     * <p>可能包含格式化文本或纯文本</p>
     */
    private String content;
    /**
     * 合同创建人/负责人
     * <p>关联User表的name字段</p>
     * <p>记录谁创建或负责此合同，用于责任追溯和工作流分配</p>
     */
    private String userName;
    /**
     * 合同附件的二进制数据
     * <p>存储合同附件文件（如PDF、DOCX等）的完整二进制内容</p>
     * <p>对应数据库t_contract表的FILE_DATA列（BLOB类型）</p>
     */
    private byte[] fileData;
    /**
     * 附件原始文件名
     * <p>保留用户上传时的原始文件名，用于下载时恢复文件名</p>
     * <p>例如："采购合同2024.pdf"</p>
     */
    private String fileName;
    /**
     * 附件文件类型
     * <p>记录文件扩展名类型，用于前端显示图标和校验</p>
     * <p>取值范围：pdf、docx、doc</p>
     */
    private String fileType;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public Contract() {}

    /**
     * 完整构造方法
     * <p>创建合同对象并设置所有属性</p>
     *
     * @param id        合同唯一标识
     * @param num       合同编号
     * @param name      合同名称
     * @param customer  签约客户名称
     * @param beginTime 合同生效日期
     * @param endTime   合同终止日期
     * @param content   合同正文内容
     * @param userName  创建人用户名
     */
    public Contract(int id, String num, String name, String customer, Date beginTime, Date endTime, String content, String userName) {
        this.id = id;
        this.num = num;
        this.name = name;
        this.customer = customer;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.content = content;
        this.userName = userName;
    }

    /**
     * 获取合同ID
     * @return 合同唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置合同ID
     * @param id 合同唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取合同编号
     * @return 合同编号
     */
    public String getNum() { return num; }

    /**
     * 设置合同编号
     * @param num 合同编号
     */
    public void setNum(String num) { this.num = num; }

    /**
     * 获取合同名称
     * @return 合同名称
     */
    public String getName() { return name; }

    /**
     * 设置合同名称
     * @param name 合同名称
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取签约客户
     * @return 客户名称
     */
    public String getCustomer() { return customer; }

    /**
     * 设置签约客户
     * @param customer 客户名称
     */
    public void setCustomer(String customer) { this.customer = customer; }

    /**
     * 获取合同生效日期
     * @return 生效日期
     */
    public Date getBeginTime() { return beginTime; }

    /**
     * 设置合同生效日期
     * @param beginTime 生效日期
     */
    public void setBeginTime(Date beginTime) { this.beginTime = beginTime; }

    /**
     * 获取合同终止日期
     * @return 终止日期
     */
    public Date getEndTime() { return endTime; }

    /**
     * 设置合同终止日期
     * @param endTime 终止日期
     */
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    /**
     * 获取合同内容
     * @return 合同正文
     */
    public String getContent() { return content; }

    /**
     * 设置合同内容
     * @param content 合同正文
     */
    public void setContent(String content) { this.content = content; }

    /**
     * 获取创建人
     * @return 创建人用户名
     */
    public String getUserName() { return userName; }

    /**
     * 设置创建人
     * @param userName 创建人用户名
     */
    public void setUserName(String userName) { this.userName = userName; }

    /**
     * 获取合同附件的二进制数据
     * @return 附件文件内容的字节数组
     */
    public byte[] getFileData() { return fileData; }

    /**
     * 设置合同附件的二进制数据
     * @param fileData 附件文件内容的字节数组
     */
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    /**
     * 获取附件原始文件名
     * @return 文件名字符串
     */
    public String getFileName() { return fileName; }

    /**
     * 设置附件原始文件名
     * @param fileName 原始文件名
     */
    public void setFileName(String fileName) { this.fileName = fileName; }

    /**
     * 获取附件文件类型
     * @return 文件类型（如pdf、docx、doc）
     */
    public String getFileType() { return fileType; }

    /**
     * 设置附件文件类型
     * @param fileType 文件类型扩展名
     */
    public void setFileType(String fileType) { this.fileType = fileType; }

    /**
     * 返回合同编号和名称的组合字符串
     * <p>重写toString方法，方便在列表中显示合同基本信息</p>
     *
     * @return 格式："合同编号 - 合同名称"
     */
    @Override
    public String toString() { return num + " - " + name; }
}
