package com.contract.service;

import com.contract.dao.UserDao;
import com.contract.dao.RightDao;
import com.contract.dao.LogDao;
import com.contract.entity.User;
import com.contract.entity.Right;
import com.contract.entity.Log;

import java.util.List;

/**
 * 用户业务逻辑类（User Service）
 * <p>
 * 处理用户相关的业务逻辑，包括用户登录验证、注册、审核、
 * 权限查询等。作为表示层和数据访问层之间的桥梁，
 * 封装业务规则并协调多个DAO完成复合操作。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>用户认证：登录验证、密码校验</li>
 *   <li>用户注册：新用户注册及状态管理</li>
 *   <li>用户审核：管理员审批/拒绝注册申请</li>
 *   <li>权限查询：获取用户的角色和功能权限</li>
 *   <li>用户维护：增删改查用户信息</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class UserService {
    /** 用户数据访问对象 */
    private UserDao userDao = new UserDao();
    /** 权限数据访问对象 */
    private RightDao rightDao = new RightDao();
    /** 日志数据访问对象 */
    private LogDao logDao = new LogDao();

    /**
     * 用户审核状态常量
     * <p>定义系统中用户可能的三种审核状态，便于代码中引用</p>
     */
    public static final int STATUS_PENDING = 0;  // 待审核：新注册用户等待审批
    public static final int STATUS_APPROVED = 1; // 已通过：可正常使用系统
    public static final int STATUS_REJECTED = 2; // 已拒绝：无法使用系统

    /**
     * 用户登录验证
     * <p>验证用户名密码是否正确，且用户必须已通过审核才能登录</p>
     *
     * @param name     用户登录名
     * @param password 用户密码（明文）
     * @return 登录成功返回User对象；未审核/密码错误/用户不存在返回null
     */
    public User login(String name, String password) {
        User user = userDao.findByName(name);  // 先根据用户名查找用户
        if (user != null && user.getPassword().equals(password)) {
            // 密码正确后检查审核状态
            if (user.getStatus() == STATUS_APPROVED) {
                return user;  // 审核通过才允许登录
            }
        }
        return null;  // 验证失败返回null
    }

    /**
     * 获取用户信息（用于状态检查）
     * <p>不校验密码，仅用于查看用户是否存在及其当前状态</p>
     *
     * @param name 用户名
     * @return User对象；不存在返回null
     */
    public User getUserForStatusCheck(String name) {
        return userDao.findByName(name);
    }

    /**
     * 用户注册
     * <p>创建新用户账户，默认状态为"待审核"，需要管理员审批后才能使用</p>
     *
     * @param name     用户名（不能重复）
     * @param password 密码
     * @return true-注册成功；false-用户名已存在或数据库操作失败
     */
    public boolean register(String name, String password) {
        // 检查用户名是否已存在，防止重复注册
        if (userDao.findByName(name) != null) {
            return false;  // 用户名已存在，注册失败
        }
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setStatus(STATUS_PENDING);  // 新注册用户默认待审核状态
        boolean result = userDao.insert(user);
        if (result) {
            // 注册成功后记录日志
            logDao.insert(new Log(0, name, "注册新用户: " + name + "，等待管理员审核", null));
        }
        return result;
    }

    /**
     * 审核通过用户
     * <p>将待审核用户的状态改为"已通过"，使其可以正常使用系统</p>
     *
     * @param id 用户ID
     * @return true-审核成功；false-操作失败
     */
    public boolean approveUser(int id) {
        boolean result = userDao.updateStatus(id, STATUS_APPROVED);
        if (result) {
            // 查询用户名用于日志记录
            List<User> all = userDao.findAll();
            String userName = all.stream().filter(u -> u.getId() == id).map(User::getName).findFirst().orElse("未知");
            logDao.insert(new Log(0, "admin", "审核通过用户: " + userName, null));
        }
        return result;
    }

    /**
     * 拒绝用户注册
     * <p>将待审核用户的状态改为"已拒绝"</p>
     *
     * @param id 用户ID
     * @return true-操作成功；false-操作失败
     */
    public boolean rejectUser(int id) {
        boolean result = userDao.updateStatus(id, STATUS_REJECTED);
        if (result) {
            List<User> all = userDao.findAll();
            String userName = all.stream().filter(u -> u.getId() == id).map(User::getName).findFirst().orElse("未知");
            logDao.insert(new Log(0, "admin", "拒绝用户: " + userName, null));
        }
        return result;
    }

    /**
     * 获取待审核用户列表
     * <p>用于管理员审核界面展示</p>
     *
     * @return 所有状态为"待审核"的用户列表
     */
    public List<User> findPendingUsers() {
        return userDao.findPending();
    }

    /**
     * 获取用户的角色列表
     * <p>查询某用户被分配的所有角色</p>
     *
     * @param userName 用户名
     * @return 该用户的角色权限列表
     */
    public List<Right> getUserRoles(String userName) {
        return rightDao.findByUserName(userName);
    }

    /**
     * 判断用户是否是管理员
     * <p>通过检查用户角色中是否包含"管理员"来判断</p>
     *
     * @param userName 用户名
     * @return true-是管理员；false-不是管理员
     */
    public boolean isAdmin(String userName) {
        List<Right> rights = rightDao.findByUserName(userName);
        for (Right r : rights) {
            if ("管理员".equals(r.getRoleName())) {  // 角色名为"管理员"则判定为管理员
                return true;
            }
        }
        return false;  // 未找到管理员角色
    }

    /**
     * 获取用户拥有的所有功能编号集合
     * <p>遍历用户的所有角色，收集每个角色关联的功能ID</p>
     *
     * @param userName 用户名
     * @return 功能编号的Set集合（去重）
     */
    public java.util.Set<String> getUserFunctions(String userName) {
        java.util.Set<String> funcSet = new java.util.HashSet<>();  // 使用Set自动去重
        List<Right> rights = rightDao.findByUserName(userName);
        RoleService roleService = new RoleService();  // 用于根据角色名获取角色详情
        for (Right r : rights) {
            com.contract.entity.Role role = roleService.findByName(r.getRoleName());
            if (role != null && role.getFunctions() != null) {
                // functions字段存储的是逗号分隔的功能ID字符串
                String[] funcs = role.getFunctions().split(",");
                for (String f : funcs) {
                    funcSet.add(f.trim());  // 去除空格后加入集合
                }
            }
        }
        return funcSet;
    }

    /**
     * 获取所有用户列表
     * @return 所有用户
     */
    public List<User> findAll() {
        return userDao.findAll();
    }

    /**
     * 管理员添加用户
     * <p>管理员直接添加的用户默认为"已通过"状态，无需再审核</p>
     *
     * @param user 用户对象
     * @return true-添加成功；false-用户名已存在
     */
    public boolean addUser(User user) {
        if (userDao.findByName(user.getName()) != null) {
            return false;  // 用户名重复
        }
        user.setStatus(STATUS_APPROVED);  // 管理员直接添加默认通过
        boolean result = userDao.insert(user);
        if (result) {
            logDao.insert(new Log(0, "admin", "添加用户: " + user.getName(), null));
        }
        return result;
    }

    /**
     * 更新用户信息
     *
     * @param user 包含更新信息的用户对象
     * @return true-更新成功；false-更新失败
     */
    public boolean updateUser(User user) {
        boolean result = userDao.update(user);
        if (result) {
            logDao.insert(new Log(0, "admin", "修改用户: " + user.getName(), null));
        }
        return result;
    }

    /**
     * 删除用户
     * <p>删除用户时同时清理其权限分配记录</p>
     *
     * @param id 用户ID
     * @return true-删除成功；false-删除失败
     */
    public boolean deleteUser(int id) {
        // 先查询用户信息以便后续清理和日志
        User user = userDao.findAll().stream().filter(u -> u.getId() == id).findFirst().orElse(null);
        boolean result = userDao.delete(id);
        if (result && user != null) {
            // 级联删除该用户的权限记录
            rightDao.deleteByUserName(user.getName());
            logDao.insert(new Log(0, "admin", "删除用户: " + user.getName(), null));
        }
        return result;
    }

    /**
     * 根据用户名查找用户
     *
     * @param name 用户名
     * @return User对象；不存在返回null
     */
    public User findByName(String name) {
        return userDao.findByName(name);
    }
}
