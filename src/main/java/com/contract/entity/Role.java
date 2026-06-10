package com.contract.entity;

/**
 * 角色实体类
 * <p>
 * 用于表示合同管理系统中的角色信息。角色是权限控制的核心概念，
 * 通过将功能分配给角色，再将角色分配给用户来实现灵活的权限管理。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>定义系统中的角色类型（如管理员、普通员工等）</li>
 *   <li>维护角色的功能权限列表</li>
 *   <li>支持角色的增删改查操作</li>
 * </ul>
 *
 * <h3>设计思路：</h3>
 * <p>
 * 采用基于角色的访问控制（RBAC）模型，functions字段存储该角色拥有的功能ID列表，
 * 格式为逗号分隔的字符串（如："1,2,3,5"），便于存储和查询。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class Role {
    /** 角色唯一标识符，自增主键 */
    private int id;
    /** 角色名称（如：管理员、普通员工、合同管理员） */
    private String name;
    /** 角色描述信息，用于说明角色的职责范围 */
    private String description;
    /**
     * 角色拥有的功能列表
     * <p>存储格式：以逗号分隔的功能ID字符串（如："1,3,5,7"）</p>
     * <p>对应Function表中的id字段，用于控制该角色可以访问的系统功能</p>
     */
    private String functions;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public Role() {}

    /**
     * 完整构造方法
     * <p>创建角色对象并设置所有属性</p>
     *
     * @param id          角色唯一标识
     * @param name        角色名称
     * @param description 角色描述
     * @param functions   功能ID列表（逗号分隔）
     */
    public Role(int id, String name, String description, String functions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.functions = functions;
    }

    /**
     * 获取角色ID
     * @return 角色唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置角色ID
     * @param id 角色唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取角色名称
     * @return 角色名称
     */
    public String getName() { return name; }

    /**
     * 设置角色名称
     * @param name 角色名称
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取角色描述
     * @return 角色描述信息
     */
    public String getDescription() { return description; }

    /**
     * 设置角色描述
     * @param description 角色描述信息
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * 获取功能列表
     * @return 以逗号分隔的功能ID字符串
     */
    public String getFunctions() { return functions; }

    /**
     * 设置功能列表
     * @param functions 以逗号分隔的功能ID字符串
     */
    public void setFunctions(String functions) { this.functions = functions; }

    /**
     * 返回角色名称的字符串表示
     * <p>重写toString方法，方便在下拉框等UI组件中显示角色名</p>
     *
     * @return 角色名称字符串
     */
    @Override
    public String toString() { return name; }
}
