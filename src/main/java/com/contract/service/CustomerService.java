package com.contract.service;

import com.contract.dao.CustomerDao;
import com.contract.dao.LogDao;
import com.contract.entity.Customer;
import com.contract.entity.Log;

import java.util.List;

/**
 * 客户业务逻辑类（Customer Service）
 * <p>
 * 处理客户信息的管理业务，包括客户的增删改查。
 * 客户信息在合同签订时使用，统一管理有助于提高数据质量。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class CustomerService {
    /** 客户数据访问对象 */
    private CustomerDao customerDao = new CustomerDao();
    /** 日志数据访问对象 */
    private LogDao logDao = new LogDao();

    /**
     * 获取所有客户列表
     * @return 所有客户
     *
     * [REST-API] GET /api/customers
     */
    public List<Customer> findAll() {
        return customerDao.findAll();
    }

    /**
     * 根据客户编号查找客户
     *
     * @param num 客户编号
     * @return Customer对象；不存在返回null
     */
    public Customer findByNum(String num) {
        return customerDao.findByNum(num);
    }

    /**
     * 根据客户名称模糊搜索
     *
     * @param name 搜索关键词
     * @return 匹配的客户列表
     */
    public List<Customer> findByName(String name) {
        return customerDao.findByName(name);
    }

    /**
     * 新增客户
     *
     * @param customer 客户对象
     * @return true-添加成功；false-添加失败
     */
    public boolean addCustomer(Customer customer) {
        boolean result = customerDao.insert(customer);
        if (result) {
            logDao.insert(new Log(0, "admin", "添加客户: " + customer.getName(), null));
        }
        return result;
    }

    /**
     * 更新客户信息
     *
     * @param customer 包含更新信息的客户对象
     * @return true-更新成功；false-更新失败
     */
    public boolean updateCustomer(Customer customer) {
        boolean result = customerDao.update(customer);
        if (result) {
            logDao.insert(new Log(0, "admin", "修改客户: " + customer.getName(), null));
        }
        return result;
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @return true-删除成功；false-删除失败
     */
    public boolean deleteCustomer(int id) {
        // 先查询客户名称用于日志
        Customer c = customerDao.findAll().stream().filter(cu -> cu.getId() == id).findFirst().orElse(null);
        boolean result = customerDao.delete(id);
        if (result && c != null) {
            logDao.insert(new Log(0, "admin", "删除客户: " + c.getName(), null));
        }
        return result;
    }
}
