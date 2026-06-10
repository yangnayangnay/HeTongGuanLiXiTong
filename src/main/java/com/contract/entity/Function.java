package com.contract.entity;

/**
 * 功能实体类
 * <p>
 * 用于表示合同管理系统中的功能菜单项。功能是权限控制的最小粒度单元，
 * 每个功能对应系统中一个具体的操作或页面。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>定义系统中的功能点（如：合同起草、合同查询、用户管理等）</li>
 *   <li>维护功能的编号、名称、URL路径等信息</li>
 *   <li>作为角色权限分配的基础数据</li>
 * </ul>
 *
 * <h3>设计思路：</h3>
 * <p>
 * 功能采用层级编号体系（num字段），便于组织和管理。
 * 每个功能都有对应的URL地址，用于前端路由或页面跳转。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class Function {
    /** 功能唯一标识符，自增主键 */
    private int id;
    /**
     * 功能编号
     * <p>采用层级编码方式，便于组织功能的层次结构</p>
     * <p>例如："01"表示一级菜单，"0101"表示二级子菜单</p>
     */
    private String num;
    /** 功能显示名称（如：合同管理、客户管理、系统设置） */
    private String name;
    /**
     * 功能对应的URL地址
     * <p>用于前端页面跳转或路由配置</p>
     * <p>例如："/contract/draft" 表示合同起草页面</p>
     */
    private String url;
    /** 功能描述信息，用于说明该功能的具体用途 */
    private String description;

    /**
     * 无参构造方法
     * <p>用于框架反射创建对象或手动设置属性值</p>
     */
    public Function() {}

    /**
     * 完整构造方法
     * <p>创建功能对象并设置所有属性</p>
     *
     * @param id          功能唯一标识
     * @param num         功能编号
     * @param name        功能名称
     * @param url         功能URL地址
     * @param description 功能描述
     */
    public Function(int id, String num, String name, String url, String description) {
        this.id = id;
        this.num = num;
        this.name = name;
        this.url = url;
        this.description = description;
    }

    /**
     * 获取功能ID
     * @return 功能唯一标识符
     */
    public int getId() { return id; }

    /**
     * 设置功能ID
     * @param id 功能唯一标识符
     */
    public void setId(int id) { this.id = id; }

    /**
     * 获取功能编号
     * @return 功能编号字符串
     */
    public String getNum() { return num; }

    /**
     * 设置功能编号
     * @param num 功能编号字符串
     */
    public void setNum(String num) { this.num = num; }

    /**
     * 获取功能名称
     * @return 功能显示名称
     */
    public String getName() { return name; }

    /**
     * 设置功能名称
     * @param name 功能显示名称
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取功能URL
     * @return 功能URL地址
     */
    public String getUrl() { return url; }

    /**
     * 设置功能URL
     * @param url 功能URL地址
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * 获取功能描述
     * @return 功能描述信息
     */
    public String getDescription() { return description; }

    /**
     * 设置功能描述
     * @param description 功能描述信息
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * 返回功能名称的字符串表示
     * <p>重写toString方法，方便在UI组件中显示功能名</p>
     *
     * @return 功能名称字符串
     */
    @Override
    public String toString() { return name; }
}
