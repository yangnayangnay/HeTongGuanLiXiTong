-- 合同管理系统数据库建表脚本（Oracle）
-- 数据库: Oracle 26ai Free (freepdb1)
-- 用户: scott/tiger

-- 1. 用户表
CREATE TABLE t_user (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(40) NOT NULL,
    password VARCHAR2(100) NOT NULL,
    status NUMBER DEFAULT 0,
    email VARCHAR2(100) DEFAULT 'default@example.com',
    CONSTRAINT uk_user_name UNIQUE (name)
);
CREATE SEQUENCE seq_user START WITH 1 INCREMENT BY 1;

-- 2. 角色表
CREATE TABLE t_role (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(40) NOT NULL,
    description VARCHAR2(100),
    functions VARCHAR2(500)
);
CREATE SEQUENCE seq_role START WITH 1 INCREMENT BY 1;

-- 3. 功能表
CREATE TABLE t_function (
    id NUMBER PRIMARY KEY,
    num VARCHAR2(10) NOT NULL,
    name VARCHAR2(40) NOT NULL,
    url VARCHAR2(100),
    description VARCHAR2(100)
);
CREATE SEQUENCE seq_function START WITH 1 INCREMENT BY 1;

-- 4. 权限表（用户-角色关联）
CREATE TABLE t_right (
    id NUMBER PRIMARY KEY,
    userName VARCHAR2(40) NOT NULL,
    roleName VARCHAR2(40) NOT NULL,
    description VARCHAR2(100)
);
CREATE SEQUENCE seq_right START WITH 1 INCREMENT BY 1;

-- 5. 合同表
CREATE TABLE t_contract (
    id NUMBER PRIMARY KEY,
    num VARCHAR2(20) NOT NULL,
    name VARCHAR2(40) NOT NULL,
    customer VARCHAR2(40),
    beginTime DATE,
    endTime DATE,
    content CLOB,
    userName VARCHAR2(40),
    file_data BLOB,
    file_name VARCHAR2(100),
    file_type VARCHAR2(20),
    amount NUMBER(14,2) DEFAULT 0,
    CONSTRAINT uk_contract_num UNIQUE (num)
);
CREATE SEQUENCE seq_contract START WITH 1 INCREMENT BY 1;

-- 6. 合同操作流程表
CREATE TABLE t_contract_process (
    id NUMBER PRIMARY KEY,
    conNum VARCHAR2(20) NOT NULL,
    type NUMBER NOT NULL,
    state NUMBER DEFAULT 0,
    userName VARCHAR2(40) NOT NULL,
    content CLOB,
    time TIMESTAMP
);
CREATE SEQUENCE seq_contract_process START WITH 1 INCREMENT BY 1;

-- 7. 合同操作状态表
CREATE TABLE t_contract_state (
    id NUMBER PRIMARY KEY,
    conNum VARCHAR2(20) NOT NULL,
    type NUMBER NOT NULL,
    time TIMESTAMP
);
CREATE SEQUENCE seq_contract_state START WITH 1 INCREMENT BY 1;

-- 8. 日志表
CREATE TABLE t_log (
    id NUMBER PRIMARY KEY,
    userName VARCHAR2(40),
    content CLOB,
    time TIMESTAMP,
    ip_address VARCHAR2(45),
    old_value VARCHAR2(500),
    new_value VARCHAR2(500)
);
CREATE SEQUENCE seq_log START WITH 1 INCREMENT BY 1;

-- 9. 客户表
CREATE TABLE t_customer (
    id NUMBER PRIMARY KEY,
    num VARCHAR2(20) NOT NULL,
    name VARCHAR2(40) NOT NULL,
    address VARCHAR2(100),
    tel VARCHAR2(20),
    fax VARCHAR2(20),
    code VARCHAR2(10),
    bank VARCHAR2(50),
    account VARCHAR2(50)
);
CREATE SEQUENCE seq_customer START WITH 1 INCREMENT BY 1;

-- 10. 合同附件表
CREATE TABLE t_contract_attachment (
    id NUMBER PRIMARY KEY,
    conNum VARCHAR2(20) NOT NULL,
    fileName VARCHAR2(100),
    path VARCHAR2(200),
    type VARCHAR2(20),
    uploadTime TIMESTAMP,
    file_data BLOB
);
CREATE SEQUENCE seq_contract_attachment START WITH 1 INCREMENT BY 1;

-- 11. 合同版本历史表
CREATE TABLE t_contract_version (
    id NUMBER PRIMARY KEY,
    contract_num VARCHAR2(50) NOT NULL,
    version_no NUMBER NOT NULL,
    content CLOB,
    file_data BLOB,
    file_name VARCHAR2(100),
    modifier VARCHAR2(40),
    modify_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    change_summary VARCHAR2(500),
    CONSTRAINT uk_contract_version UNIQUE (contract_num, version_no)
);
CREATE SEQUENCE seq_contract_version START WITH 1 INCREMENT BY 1;
