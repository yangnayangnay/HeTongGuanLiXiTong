-- 合同管理系统初始数据

-- 插入12个功能项
INSERT INTO t_function (num, name, url, description) VALUES ('F01', '起草合同', '/contract/draft', '起草新合同');
INSERT INTO t_function (num, name, url, description) VALUES ('F02', '会签合同', '/contract/countersign', '会签合同');
INSERT INTO t_function (num, name, url, description) VALUES ('F03', '定稿合同', '/contract/finalize', '定稿合同');
INSERT INTO t_function (num, name, url, description) VALUES ('F04', '审批合同', '/contract/approve', '审批合同');
INSERT INTO t_function (num, name, url, description) VALUES ('F05', '签订合同', '/contract/sign', '签订合同');
INSERT INTO t_function (num, name, url, description) VALUES ('F06', '分配合同', '/contract/assign', '分配合同人员');
INSERT INTO t_function (num, name, url, description) VALUES ('F07', '合同查询', '/contract/query', '查询合同信息');
INSERT INTO t_function (num, name, url, description) VALUES ('F08', '流程查询', '/contract/process', '查询合同流程');
INSERT INTO t_function (num, name, url, description) VALUES ('F09', '客户管理', '/customer/manage', '管理客户信息');
INSERT INTO t_function (num, name, url, description) VALUES ('F10', '用户管理', '/system/user', '管理系统用户');
INSERT INTO t_function (num, name, url, description) VALUES ('F11', '角色管理', '/system/role', '管理角色权限');
INSERT INTO t_function (num, name, url, description) VALUES ('F12', '日志管理', '/system/log', '查看系统日志');

-- 插入初始角色
INSERT INTO t_role (name, description, functions) VALUES ('管理员', '系统管理员，拥有所有权限', 'F01,F02,F03,F04,F05,F06,F07,F08,F09,F10,F11,F12');
INSERT INTO t_role (name, description, functions) VALUES ('合同操作员', '起草和定稿合同', 'F01,F03,F07,F08');
INSERT INTO t_role (name, description, functions) VALUES ('会签人员', '会签合同', 'F02,F07,F08');
INSERT INTO t_role (name, description, functions) VALUES ('审批人员', '审批合同', 'F04,F07,F08');
INSERT INTO t_role (name, description, functions) VALUES ('签订人员', '签订合同', 'F05,F07,F08');

-- 插入初始管理员用户（密码: admin123，状态: 已通过）
INSERT INTO t_user (name, password, status, email) VALUES ('admin', 'admin123', 1, 'admin@example.com');

-- 给管理员分配角色
INSERT INTO t_right (userName, roleName, description) VALUES ('admin', '管理员', '系统管理员');

-- 插入示例客户数据
INSERT INTO t_customer (num, name, address, tel, fax, code, bank, account) VALUES ('C001', '示例科技有限公司', '北京市海淀区中关村大街1号', '010-12345678', '010-87654321', '100080', '中国工商银行', '6222000000001234567');
INSERT INTO t_customer (num, name, address, tel, fax, code, bank, account) VALUES ('C002', '测试贸易公司', '上海市浦东新区陆家嘴路100号', '021-98765432', '021-12345678', '200120', '中国建设银行', '6227000000007654321');
