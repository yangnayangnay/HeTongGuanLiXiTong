package com.contract.service;

import com.contract.dao.RightDao;
import com.contract.dao.LogDao;
import com.contract.entity.Right;
import com.contract.entity.Log;

import java.util.List;

/**
 * 权限业务逻辑类（Right Service）
 * <p>
 * 处理用户-角色权限分配的业务逻辑。
 * 实现角色的分配、重新分配等功能，
 * 是RBAC权限模型中用户与角色关联的核心管理类。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class RightService {
    /** 权限数据访问对象 */
    private RightDao rightDao = new RightDao();
    /** 日志数据访问对象 */
    private LogDao logDao = new LogDao();

    /**
     * 根据用户名查询其拥有的角色
     *
     * @param userName 用户名
     * @return 该用户的角色列表
     */
    public List<Right> findByUserName(String userName) {
        return rightDao.findByUserName(userName);
    }

    /**
     * 获取所有权限分配记录
     * @return 所有的用户-角色分配关系列表
     */
    public List<Right> findAll() {
        return rightDao.findAll();
    }

    /**
     * 为用户分配单个角色
     * <p>在不影响现有角色的情况下，新增一个角色分配</p>
     *
     * @param userName 用户名
     * @param roleName 要分配的角色名称
     * @return true-分配成功；false-分配失败
     */
    public boolean assignRole(String userName, String roleName) {
        Right right = new Right();
        right.setUserName(userName);
        right.setRoleName(roleName);
        right.setDescription("分配角色: " + roleName);
        boolean result = rightDao.insert(right);
        if (result) {
            logDao.insert(new Log(0, "admin", "为用户 " + userName + " 分配角色: " + roleName, null));
        }
        return result;
    }

    /**
     * 重新分配用户的所有角色
     * <p>先清除该用户原有的所有角色分配，再批量添加新的角色</p>
     * <p>用于角色管理的保存操作</p>
     *
     * @param userName  用户名
     * @param roleNames 新的角色名称列表
     * @return true-操作成功
     */
    public boolean reassignRoles(String userName, List<String> roleNames) {
        // 先删除该用户的所有旧角色
        rightDao.deleteByUserName(userName);
        // 批量添加新角色
        for (String roleName : roleNames) {
            Right right = new Right();
            right.setUserName(userName);
            right.setRoleName(roleName);
            right.setDescription("分配角色: " + roleName);
            rightDao.insert(right);
        }
        logDao.insert(new Log(0, "admin", "重新分配用户 " + userName + " 的角色", null));
        return true;
    }
}
