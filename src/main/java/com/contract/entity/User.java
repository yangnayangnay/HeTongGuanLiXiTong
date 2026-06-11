package com.contract.entity;

/**
 * 用户实体类
 * <p>
 * 用于表示合同管理系统中的用户信息，包括用户的基本属性和账户状态。
 * 用户是系统的核心主体，通过角色和权限机制控制对不同功能的访问。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>存储用户的基本信息（ID、用户名、密码）</li>
 *   <li>记录用户的审核状态（待审核/已通过/已拒绝）</li>
 *   <li>提供用户信息的封装和访问方法</li>
 * </ul>
 *
 * <h3>业务规则：</h3>
 * <ul>
 *   <li>新注册的用户默认状态为"待审核"，需要管理员审批后才能使用系统</li>
 *   <li>用户名在系统中唯一，用于登录认证</li>
 *   <li>密码采用明文存储（实际项目中应加密存储）</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class User {
    /** 用户唯一标识符，自增主键 */
    private int id;
    /** 用户登录名称，用于身份验证 */
    private String name;
    /** 用户登录密码 */
    private String password;
    /**
     * 用户审核状态
     * <ul>
     *   <li>0 - 待审核：新注册用户等待管理员审批</li>
     *   <li>1 - 已通过：审核通过，可正常使用系统</li>
     *   <li>2 - 已拒绝：审核被拒绝，无法使用系统</li>
     * </ul>
     */
    private int status;
    /** 用户邮箱地址，用于接收任务通知邮件 */
    private String email;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public User() {}

    /**
     * 基础构造方法（兼容旧版本）
     * <p>创建用户对象并设置基本属性，状态默认为"已通过"</p>
     *
     * @param id       用户唯一标识
     * @param name     用户登录名
     * @param password 用户密码
     */
    public User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.status = 1; // 兼容旧构造，默认设为已通过状态
        this.email = ""; // 默认空邮箱
    }

    /**
     * 完整构造方法
     * <p>创建用户对象并设置所有属性</p>
     *
     * @param id       用户唯一标识
     * @param name     用户登录名
     * @param password 用户密码
     * @param status   用户审核状态（0-待审核, 1-已通过, 2-已拒绝）
     */
    public User(int id, String name, String password, int status) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.status = status;
        this.email = ""; // 默认空邮箱
    }

    /**
     * 获取用户ID
     * @return 用户唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置用户ID
     * @param id 用户唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取用户名
     * @return 用户登录名称
     */
    public String getName() { return name; }

    /**
     * 设置用户名
     * @param name 用户登录名称
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取用户密码
     * @return 用户登录密码
     */
    public String getPassword() { return password; }

    /**
     * 设置用户密码
     * @param password 用户登录密码
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * 获取用户状态
     * @return 状态码（0-待审核, 1-已通过, 2-已拒绝）
     */
    public int getStatus() { return status; }

    /**
     * 设置用户状态
     * @param status 状态码（0-待审核, 1-已通过, 2-已拒绝）
     */
    public void setStatus(int status) { this.status = status; }

    /**
     * 获取用户邮箱
     * @return 邮箱地址
     */
    public String getEmail() { return email; }

    /**
     * 设置用户邮箱
     * @param email 邮箱地址
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * 返回用户名的字符串表示
     * <p>重写toString方法，方便在下拉框等UI组件中显示用户名</p>
     *
     * @return 用户名字符串
     */
    @Override
    public String toString() { return name; }
}
