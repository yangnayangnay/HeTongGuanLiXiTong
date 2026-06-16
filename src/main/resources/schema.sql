-- 合同管理系统数据库建表脚本（MySQL）
-- 数据库: contract_db

-- 1. 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    password VARCHAR(100) NOT NULL,
    status INT DEFAULT 0,
    email VARCHAR(100) DEFAULT 'default@example.com',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    description VARCHAR(100),
    functions VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 功能表
CREATE TABLE IF NOT EXISTS t_function (
    id INT AUTO_INCREMENT PRIMARY KEY,
    num VARCHAR(10) NOT NULL,
    name VARCHAR(40) NOT NULL,
    url VARCHAR(100),
    description VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 权限表（用户-角色关联）
CREATE TABLE IF NOT EXISTS t_right (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userName VARCHAR(40) NOT NULL,
    roleName VARCHAR(40) NOT NULL,
    description VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 合同表
CREATE TABLE IF NOT EXISTS t_contract (
    id INT AUTO_INCREMENT PRIMARY KEY,
    num VARCHAR(20) NOT NULL,
    name VARCHAR(40) NOT NULL,
    customer VARCHAR(40),
    beginTime DATE,
    endTime DATE,
    content LONGTEXT,
    userName VARCHAR(40),
    file_data LONGBLOB,
    file_name VARCHAR(100),
    file_type VARCHAR(20),
    amount DECIMAL(14,2) DEFAULT 0,
    UNIQUE KEY uk_num (num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 合同操作流程表
CREATE TABLE IF NOT EXISTS t_contract_process (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conNum VARCHAR(20) NOT NULL,
    type INT NOT NULL,
    state INT DEFAULT 0,
    userName VARCHAR(40) NOT NULL,
    content LONGTEXT,
    time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 合同操作状态表
CREATE TABLE IF NOT EXISTS t_contract_state (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conNum VARCHAR(20) NOT NULL,
    type INT NOT NULL,
    time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 日志表
CREATE TABLE IF NOT EXISTS t_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userName VARCHAR(40),
    content LONGTEXT,
    time DATETIME,
    ip_address VARCHAR(45),
    old_value VARCHAR(500),
    new_value VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 客户表
CREATE TABLE IF NOT EXISTS t_customer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    num VARCHAR(20) NOT NULL,
    name VARCHAR(40) NOT NULL,
    address VARCHAR(100),
    tel VARCHAR(20),
    fax VARCHAR(20),
    code VARCHAR(10),
    bank VARCHAR(50),
    account VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 合同附件表
CREATE TABLE IF NOT EXISTS t_contract_attachment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conNum VARCHAR(20) NOT NULL,
    fileName VARCHAR(100),
    path VARCHAR(200),
    type VARCHAR(20),
    uploadTime DATETIME,
    file_data LONGBLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 合同版本历史表
CREATE TABLE IF NOT EXISTS t_contract_version (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contract_num VARCHAR(50) NOT NULL,
    version_no INT NOT NULL,
    content LONGTEXT,
    file_data LONGBLOB,
    file_name VARCHAR(100),
    modifier VARCHAR(40),
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    change_summary VARCHAR(500),
    UNIQUE KEY uk_contract_version (contract_num, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
