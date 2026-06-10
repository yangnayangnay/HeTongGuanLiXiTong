package com.contract.entity;

/**
 * 权限实体类（用户-角色关联表）
 * <p>
 * 用于建立用户与角色之间的多对多关联关系。在RBAC（基于角色的访问控制）模型中，
 * 权限表是连接用户和角色的桥梁，实现灵活的权限分配机制。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>记录用户与角色的分配关系</li>
 *   <li>一个用户可以拥有多个角色</li>
 *   <li>一个角色可以被分配给多个用户</li>
 *   <li>支持权限的动态调整和审计追踪</li>
 * </ul>
 *
 * <h3>业务场景：</h3>
 * <ul>
 *   <li>管理员为新用户分配初始角色</li>
 *   <li>根据工作需要调整用户角色</li>
 *   <li>查询某个用户拥有的所有权限</li>
 *   <li>查询某个角色下的所有用户</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class Right {
    /** 权限记录唯一标识符，自增主键 */
    private int id;
    /** 关联的用户名称，对应User表的name字段 */
    private String userName;
    /** 分配的角色名称，对应Role表的name字段 */
    private String roleName;
    /** 权限分配的备注说明（如：分配原因、有效期等） */
    private String description;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public Right() {}

    /**
     * 完整构造方法
     * <p>创建权限对象并设置所有属性</p>
     *
     * @param id          权限记录唯一标识
     * @param userName    用户名称
     * @param roleName    角色名称
     * @param description 备注说明
     */
    public Right(int id, String userName, String roleName, String description) {
        this.id = id;
        this.userName = userName;
        this.roleName = roleName;
        this.description = description;
    }

    /**
     * 获取权限记录ID
     * @return 权限记录唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置权限记录ID
     * @param id 权限记录唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取用户名称
     * @return 关联的用户名称
     */
    public String getUserName() { return userName; }

    /**
     * 设置用户名称
     * @param userName 关联的用户名称
     */
    public void setUserName(String userName) { this.userName = userName; }

    /**
     * 获取角色名称
     * @return 分配的角色名称
     */
    public String getRoleName() { return roleName; }

    /**
     * 设置角色名称
     * @param roleName 分配的角色名称
     */
    public void setRoleName(String roleName) { this.roleName = roleName; }

    /**
     * 获取描述信息
     * @return 备注说明
     */
    public String getDescription() { return description; }

    /**
     * 设置描述信息
     * @param description 备注说明
     */
    public void setDescription(String description) { this.description = description; }
}
