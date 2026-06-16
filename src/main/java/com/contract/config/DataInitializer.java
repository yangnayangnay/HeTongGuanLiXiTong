package com.contract.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            // Check if tables exist by querying t_function
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT COUNT(*) FROM user_tables WHERE table_name = 'T_FUNCTION'");
            int count = ((Number) result.get(0).values().iterator().next()).intValue();

            if (count == 0) {
                log.info("数据库表不存在，开始执行建表脚本...");
                executeSchema();
            }

            // Check if initial data exists
            int functionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_function", Integer.class);
            if (functionCount == 0) {
                log.info("初始数据为空，开始插入初始数据...");
                executeData();
            } else {
                log.info("初始数据已存在，跳过初始化");
            }
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }

    private void executeSchema() {
        // Create tables
        jdbcTemplate.execute("CREATE TABLE t_user (id NUMBER PRIMARY KEY, name VARCHAR2(40) NOT NULL, password VARCHAR2(100) NOT NULL, status NUMBER DEFAULT 0, email VARCHAR2(100) DEFAULT 'default@example.com', CONSTRAINT uk_user_name UNIQUE (name))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_user START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_role (id NUMBER PRIMARY KEY, name VARCHAR2(40) NOT NULL, description VARCHAR2(100), functions VARCHAR2(500))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_role START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_function (id NUMBER PRIMARY KEY, num VARCHAR2(10) NOT NULL, name VARCHAR2(40) NOT NULL, url VARCHAR2(100), description VARCHAR2(100))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_function START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_right (id NUMBER PRIMARY KEY, userName VARCHAR2(40) NOT NULL, roleName VARCHAR2(40) NOT NULL, description VARCHAR2(100))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_right START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_contract (id NUMBER PRIMARY KEY, num VARCHAR2(20) NOT NULL, name VARCHAR2(40) NOT NULL, customer VARCHAR2(40), beginTime DATE, endTime DATE, content CLOB, userName VARCHAR2(40), file_data BLOB, file_name VARCHAR2(100), file_type VARCHAR2(20), amount NUMBER(14,2) DEFAULT 0, CONSTRAINT uk_contract_num UNIQUE (num))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_contract START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_contract_process (id NUMBER PRIMARY KEY, conNum VARCHAR2(20) NOT NULL, type NUMBER NOT NULL, state NUMBER DEFAULT 0, userName VARCHAR2(40) NOT NULL, content CLOB, time TIMESTAMP)");
        jdbcTemplate.execute("CREATE SEQUENCE seq_contract_process START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_contract_state (id NUMBER PRIMARY KEY, conNum VARCHAR2(20) NOT NULL, type NUMBER NOT NULL, time TIMESTAMP)");
        jdbcTemplate.execute("CREATE SEQUENCE seq_contract_state START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_log (id NUMBER PRIMARY KEY, userName VARCHAR2(40), content CLOB, time TIMESTAMP, ip_address VARCHAR2(45), old_value VARCHAR2(500), new_value VARCHAR2(500))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_log START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_customer (id NUMBER PRIMARY KEY, num VARCHAR2(20) NOT NULL, name VARCHAR2(40) NOT NULL, address VARCHAR2(100), tel VARCHAR2(20), fax VARCHAR2(20), code VARCHAR2(10), bank VARCHAR2(50), account VARCHAR2(50))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_customer START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_contract_attachment (id NUMBER PRIMARY KEY, conNum VARCHAR2(20) NOT NULL, fileName VARCHAR2(100), path VARCHAR2(200), type VARCHAR2(20), uploadTime TIMESTAMP, file_data BLOB)");
        jdbcTemplate.execute("CREATE SEQUENCE seq_contract_attachment START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("CREATE TABLE t_contract_version (id NUMBER PRIMARY KEY, contract_num VARCHAR2(50) NOT NULL, version_no NUMBER NOT NULL, content CLOB, file_data BLOB, file_name VARCHAR2(100), modifier VARCHAR2(40), modify_time TIMESTAMP DEFAULT SYSTIMESTAMP, change_summary VARCHAR2(500), CONSTRAINT uk_contract_version UNIQUE (contract_num, version_no))");
        jdbcTemplate.execute("CREATE SEQUENCE seq_contract_version START WITH 1 INCREMENT BY 1");

        log.info("建表脚本执行完成");
    }

    private void executeData() {
        // Insert 12 function items
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F01', '起草合同', '/contract/draft', '起草新合同')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F02', '会签合同', '/contract/countersign', '会签合同')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F03', '定稿合同', '/contract/finalize', '定稿合同')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F04', '审批合同', '/contract/approve', '审批合同')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F05', '签订合同', '/contract/sign', '签订合同')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F06', '分配合同', '/contract/assign', '分配合同人员')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F07', '合同查询', '/contract/query', '查询合同信息')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F08', '流程查询', '/contract/process', '查询合同流程')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F09', '客户管理', '/customer/manage', '管理客户信息')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F10', '用户管理', '/system/user', '管理系统用户')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F11', '角色管理', '/system/role', '管理角色权限')");
        jdbcTemplate.update("INSERT INTO t_function (id, num, name, url, description) VALUES (seq_function.NEXTVAL, 'F12', '日志管理', '/system/log', '查看系统日志')");

        // Insert initial roles
        jdbcTemplate.update("INSERT INTO t_role (id, name, description, functions) VALUES (seq_role.NEXTVAL, '管理员', '系统管理员，拥有所有权限', 'F01,F02,F03,F04,F05,F06,F07,F08,F09,F10,F11,F12')");
        jdbcTemplate.update("INSERT INTO t_role (id, name, description, functions) VALUES (seq_role.NEXTVAL, '合同操作员', '起草和定稿合同', 'F01,F03,F07,F08')");
        jdbcTemplate.update("INSERT INTO t_role (id, name, description, functions) VALUES (seq_role.NEXTVAL, '会签人员', '会签合同', 'F02,F07,F08')");
        jdbcTemplate.update("INSERT INTO t_role (id, name, description, functions) VALUES (seq_role.NEXTVAL, '审批人员', '审批合同', 'F04,F07,F08')");
        jdbcTemplate.update("INSERT INTO t_role (id, name, description, functions) VALUES (seq_role.NEXTVAL, '签订人员', '签订合同', 'F05,F07,F08')");

        // Insert admin user
        jdbcTemplate.update("INSERT INTO t_user (id, name, password, status, email) VALUES (seq_user.NEXTVAL, 'admin', 'admin123', 1, 'admin@example.com')");

        // Assign admin role
        jdbcTemplate.update("INSERT INTO t_right (id, userName, roleName, description) VALUES (seq_right.NEXTVAL, 'admin', '管理员', '系统管理员')");

        // Insert sample customers
        jdbcTemplate.update("INSERT INTO t_customer (id, num, name, address, tel, fax, code, bank, account) VALUES (seq_customer.NEXTVAL, 'C001', '示例科技有限公司', '北京市海淀区中关村大街1号', '010-12345678', '010-87654321', '100080', '中国工商银行', '6222000000001234567')");
        jdbcTemplate.update("INSERT INTO t_customer (id, num, name, address, tel, fax, code, bank, account) VALUES (seq_customer.NEXTVAL, 'C002', '测试贸易公司', '上海市浦东新区陆家嘴路100号', '021-98765432', '021-12345678', '200120', '中国建设银行', '6227000000007654321')");

        log.info("初始数据插入完成");
    }
}
