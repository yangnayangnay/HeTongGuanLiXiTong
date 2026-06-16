package com.contract.service;

import org.springframework.stereotype.Service;

import com.contract.dao.RoleDao;
import com.contract.dao.LogDao;
import com.contract.entity.Role;
import com.contract.entity.Log;

import java.util.List;

/**
 * 角色业务逻辑类（Role Service）
 * <p>
 * 处理角色相关的业务逻辑，包括角色的增删改查。
 * 角色是权限管理的核心，每个角色关联一组系统功能，
 * 通过为用户分配角色来控制其可访问的系统功能。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
@Service
public class RoleService {
    /** 角色数据访问对象 */
    private RoleDao roleDao = new RoleDao();
    /** 日志数据访问对象 */
    private LogDao logDao = new LogDao();

    /**
     * 获取所有角色列表
     * @return 所有角色
     */
    public List<Role> findAll() {
        return roleDao.findAll();
    }

    /**
     * 根据角色名称查找角色
     *
     * @param name 角色名称
     * @return Role对象；不存在返回null
     */
    public Role findByName(String name) {
        return roleDao.findByName(name);
    }

    /**
     * 新增角色
     * <p>创建新的角色，需指定角色名称、描述和关联的功能列表</p>
     *
     * @param role 角色对象
     * @return true-添加成功；false-添加失败
     */
    public boolean addRole(Role role) {
        boolean result = roleDao.insert(role);
        if (result) {
            logDao.insert(new Log(0, "admin", "添加角色: " + role.getName(), null));
        }
        return result;
    }

    /**
     * 更新角色信息
     *
     * @param role 包含更新信息的角色对象
     * @return true-更新成功；false-更新失败
     */
    public boolean updateRole(Role role) {
        boolean result = roleDao.update(role);
        if (result) {
            logDao.insert(new Log(0, "admin", "修改角色: " + role.getName(), null));
        }
        return result;
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return true-删除成功；false-删除失败
     */
    public boolean deleteRole(int id) {
        // 先查询角色名称用于日志
        Role role = roleDao.findAll().stream().filter(r -> r.getId() == id).findFirst().orElse(null);
        boolean result = roleDao.delete(id);
        if (result && role != null) {
            logDao.insert(new Log(0, "admin", "删除角色: " + role.getName(), null));
        }
        return result;
    }
}
