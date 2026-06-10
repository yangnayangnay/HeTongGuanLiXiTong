package com.contract.entity;

/**
 * 客户实体类
 * <p>
 * 用于表示合同管理系统中的客户/合作伙伴信息。客户是合同签约的另一方，
 * 维护客户的详细联系信息有助于合同管理和后续的业务往来。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>存储客户的基本信息（名称、地址等）</li>
 *   <li>维护客户的联系方式（电话、传真等）</li>
 *   <li>记录客户的银行账户信息，用于财务结算</li>
 *   <li>支持客户的增删改查管理</li>
 * </ul>
 *
 * <h3>业务价值：</h3>
 * <ul>
 *   <li>在签订合同时快速选择客户信息</li>
 *   <li>统一管理客户资料，避免信息分散</li>
 *   <li>为合同统计分析提供数据基础</li>
 *   <li>支持客户信用评估和历史合作查询</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class Customer {
    /** 客户唯一标识符，自增主键 */
    private int id;
    /**
     * 客户编号
     * <p>业务唯一标识，用于业务引用和查询</p>
     * <p>通常按类别+流水号生成（如：KH2024001）</p>
     */
    private String num;
    /** 客户/公司全称，用于合同显示和正式文档 */
    private String name;
    /**
     * 客户联系地址
     * <p>详细的通讯地址，用于邮寄合同原件或其他文件</p>
     */
    private String address;
    /**
     * 联系电话
     * <p>主要联系电话，用于业务沟通</p>
     */
    private String tel;
    /**
     * 传真号码
     * <p>用于传输合同扫描件或正式函件</p>
     */
    private String fax;
    /**
     * 邮政编码
     * <p>配合地址使用，确保邮寄准确</p>
     */
    private String code;
    /**
     * 开户银行
     * <p>客户的开户银行名称，用于合同款项结算</p>
     */
    private String bank;
    /**
     * 银行账号
     * <p>客户在开户银行的账号，用于转账付款</p>
     */
    private String account;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public Customer() {}

    /**
     * 完整构造方法
     * <p>创建客户对象并设置所有属性</p>
     *
     * @param id      客户唯一标识
     * @param num     客户编号
     * @param name    客户名称
     * @param address 联系地址
     * @param tel     联系电话
     * @param fax     传真号码
     * @param code    邮政编码
     * @param bank    开户银行
     * @param account 银行账号
     */
    public Customer(int id, String num, String name, String address, String tel, String fax, String code, String bank, String account) {
        this.id = id;
        this.num = num;
        this.name = name;
        this.address = address;
        this.tel = tel;
        this.fax = fax;
        this.code = code;
        this.bank = bank;
        this.account = account;
    }

    /**
     * 获取客户ID
     * @return 客户唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置客户ID
     * @param id 客户唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取客户编号
     * @return 客户编号
     */
    public String getNum() { return num; }

    /**
     * 设置客户编号
     * @param num 客户编号
     */
    public void setNum(String num) { this.num = num; }

    /**
     * 获取客户名称
     * @return 客户名称
     */
    public String getName() { return name; }

    /**
     * 设置客户名称
     * @param name 客户名称
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取联系地址
     * @return 地址信息
     */
    public String getAddress() { return address; }

    /**
     * 设置联系地址
     * @param address 地址信息
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * 获取联系电话
     * @return 电话号码
     */
    public String getTel() { return tel; }

    /**
     * 设置联系电话
     * @param tel 电话号码
     */
    public void setTel(String tel) { this.tel = tel; }

    /**
     * 获取传真号码
     * @return 传真号码
     */
    public String getFax() { return fax; }

    /**
     * 设置传真号码
     * @param fax 传真号码
     */
    public void setFax(String fax) { this.fax = fax; }

    /**
     * 获取邮政编码
     * @return 邮编
     */
    public String getCode() { return code; }

    /**
     * 设置邮政编码
     * @param code 邮编
     */
    public void setCode(String code) { this.code = code; }

    /**
     * 获取开户银行
     * @return 银行名称
     */
    public String getBank() { return bank; }

    /**
     * 设置开户银行
     * @param bank 银行名称
     */
    public void setBank(String bank) { this.bank = bank; }

    /**
     * 获取银行账号
     * @return 账号
     */
    public String getAccount() { return account; }

    /**
     * 设置银行账号
     * @param account 账号
     */
    public void setAccount(String account) { this.account = account; }

    /**
     * 返回客户名称的字符串表示
     * <p>重写toString方法，方便在下拉框等UI组件中显示客户名</p>
     *
     * @return 客户名称字符串
     */
    @Override
    public String toString() { return name; }
}
