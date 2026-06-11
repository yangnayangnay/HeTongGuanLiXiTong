-- 合同管理系统数据库初始化脚本
-- 数据库: Oracle (scott/tiger@localhost:1521/freepdb1)

-- 删除已有表（按依赖顺序）
DROP TABLE t_contract_attachment CASCADE CONSTRAINTS;
DROP TABLE t_contract_process CASCADE CONSTRAINTS;
DROP TABLE t_contract_state CASCADE CONSTRAINTS;
DROP TABLE t_contract CASCADE CONSTRAINTS;
DROP TABLE t_right CASCADE CONSTRAINTS;
DROP TABLE t_log CASCADE CONSTRAINTS;
DROP TABLE t_customer CASCADE CONSTRAINTS;
DROP TABLE t_function CASCADE CONSTRAINTS;
DROP TABLE t_role CASCADE CONSTRAINTS;
DROP TABLE t_user CASCADE CONSTRAINTS;

-- 删除序列
DROP SEQUENCE seq_user;
DROP SEQUENCE seq_role;
DROP SEQUENCE seq_function;
DROP SEQUENCE seq_right;
DROP SEQUENCE seq_contract;
DROP SEQUENCE seq_contract_process;
DROP SEQUENCE seq_contract_state;
DROP SEQUENCE seq_log;
DROP SEQUENCE seq_customer;
DROP SEQUENCE seq_contract_attachment;

-- 创建序列
CREATE SEQUENCE seq_user START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_role START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_function START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_right START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_contract START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_contract_process START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_contract_state START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_log START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_customer START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_contract_attachment START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- 1. 用户表
CREATE TABLE t_user (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(40) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    status NUMBER(1) DEFAULT 0,   -- 0:待审核 1:已通过 2:已拒绝
    email VARCHAR2(100) DEFAULT 'default@example.com'  -- 邮箱地址，用于接收任务通知
);

-- 2. 角色表
CREATE TABLE t_role (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(40) NOT NULL,
    description VARCHAR2(100),
    functions VARCHAR2(500)
);

-- 3. 功能表
CREATE TABLE t_function (
    id NUMBER PRIMARY KEY,
    num VARCHAR2(10) NOT NULL,
    name VARCHAR2(40) NOT NULL,
    url VARCHAR2(100),
    description VARCHAR2(100)
);

-- 4. 权限表（用户-角色关联）
CREATE TABLE t_right (
    id NUMBER PRIMARY KEY,
    userName VARCHAR2(40) NOT NULL,
    roleName VARCHAR2(40) NOT NULL,
    description VARCHAR2(100)
);

-- 5. 合同表
CREATE TABLE t_contract (
    id NUMBER PRIMARY KEY,
    num VARCHAR2(20) NOT NULL UNIQUE,
    name VARCHAR2(40) NOT NULL,
    customer VARCHAR2(40),
    beginTime DATE,
    endTime DATE,
    content CLOB,
    userName VARCHAR2(40),
    file_data BLOB,           -- 合同附件二进制数据（PDF/DOCX等）
    file_name VARCHAR2(100),  -- 附件原始文件名
    file_type VARCHAR2(20)    -- 文件类型（pdf/docx/doc等）
);

-- 6. 合同操作流程表
CREATE TABLE t_contract_process (
    id NUMBER PRIMARY KEY,
    conNum VARCHAR2(20) NOT NULL,
    type NUMBER(1) NOT NULL,  -- 1:会签 2:审批 3:签订
    state NUMBER(1) DEFAULT 0, -- 0:未完成 1:已完成 2:已否决
    userName VARCHAR2(40) NOT NULL,
    content CLOB,
    time DATE
);

-- 7. 合同操作状态表
CREATE TABLE t_contract_state (
    id NUMBER PRIMARY KEY,
    conNum VARCHAR2(20) NOT NULL,
    type NUMBER(1) NOT NULL,  -- 1:起草 2:会签完成 3:定稿完成 4:审批完成 5:签订完成
    time DATE
);

-- 8. 日志表
CREATE TABLE t_log (
    id NUMBER PRIMARY KEY,
    userName VARCHAR2(40),
    content CLOB,
    time DATE
);

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

-- 10. 合同附件表
CREATE TABLE t_contract_attachment (
    id NUMBER PRIMARY KEY,
    conNum VARCHAR2(20) NOT NULL,
    fileName VARCHAR2(100),
    path VARCHAR2(200),
    type VARCHAR2(20),
    uploadTime DATE,
    file_data BLOB            -- 附件实际二进制数据
);

-- 插入初始功能数据
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F01', '起草合同', '/contract/draft', '起草新合同');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F02', '会签合同', '/contract/countersign', '会签合同');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F03', '定稿合同', '/contract/finalize', '定稿合同');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F04', '审批合同', '/contract/approve', '审批合同');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F05', '签订合同', '/contract/sign', '签订合同');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F06', '分配合同', '/contract/assign', '分配合同人员');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F07', '合同查询', '/contract/query', '查询合同信息');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F08', '流程查询', '/contract/process', '查询合同流程');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F09', '客户管理', '/customer/manage', '管理客户信息');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F10', '用户管理', '/system/user', '管理系统用户');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F11', '角色管理', '/system/role', '管理角色权限');
INSERT INTO t_function VALUES (seq_function.NEXTVAL, 'F12', '日志管理', '/system/log', '查看系统日志');

-- 插入初始角色
INSERT INTO t_role VALUES (seq_role.NEXTVAL, '管理员', '系统管理员，拥有所有权限', 'F01,F02,F03,F04,F05,F06,F07,F08,F09,F10,F11,F12');
INSERT INTO t_role VALUES (seq_role.NEXTVAL, '合同操作员', '起草和定稿合同', 'F01,F03,F07,F08');
INSERT INTO t_role VALUES (seq_role.NEXTVAL, '会签人员', '会签合同', 'F02,F07,F08');
INSERT INTO t_role VALUES (seq_role.NEXTVAL, '审批人员', '审批合同', 'F04,F07,F08');
INSERT INTO t_role VALUES (seq_role.NEXTVAL, '签订人员', '签订合同', 'F05,F07,F08');

-- 插入初始管理员用户（密码: admin123，状态: 已通过）
INSERT INTO t_user VALUES (seq_user.NEXTVAL, 'admin', 'admin123', 1, 'admin@example.com');

-- 给管理员分配角色
INSERT INTO t_right VALUES (seq_right.NEXTVAL, 'admin', '管理员', '系统管理员');

-- 插入示例客户数据
INSERT INTO t_customer VALUES (seq_customer.NEXTVAL, 'C001', '示例科技有限公司', '北京市海淀区中关村大街1号', '010-12345678', '010-87654321', '100080', '中国工商银行', '6222000000001234567');
INSERT INTO t_customer VALUES (seq_customer.NEXTVAL, 'C002', '测试贸易公司', '上海市浦东新区陆家嘴路100号', '021-98765432', '021-12345678', '200120', '中国建设银行', '6227000000007654321');

COMMIT;
