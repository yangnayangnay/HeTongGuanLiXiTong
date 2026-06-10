# 合同管理系统 (HeTongGuanLiXitong)

## 项目简介

基于 **Java Swing** + **Oracle Database** 的桌面端合同管理系统，实现合同全生命周期管理（起草→会签→定稿→审批→签订），支持 RBAC 角色权限控制、用户审核流程、日志审计等功能。

## 技术栈

| 技术 | 版本/说明 |
|------|-----------|
| Java | JDK 1.8+ |
| 数据库 | Oracle 26ai Free (localhost:1521/freepdb1) |
| 构建工具 | Maven 3.x |
| GUI框架 | Java Swing (javax.swing) |
| JDBC驱动 | ojdbc8 (Oracle官方) |

## 功能概览

```
┌─────────────────────────────────────────────────────┐
│                   合同管理系统                        │
├──────────┬──────────────────────────────────────────┤
│          │  合同管理                                  │
│          │    ├─ 起草合同(F01)   └─ 会签合同(F02)     │
│          │    ├─ 定稿合同(F03)   └─ 审批合同(F04)     │
│ 左侧导航 │    └─ 签订合同(F05)                       │
│          │                                           │
│          │  查询统计                                  │
│          │    ├─ 合同查询(F07)   └─ 流程查询(F08)     │
│          │                                           │
│          │  基础数据管理                              │
│          │    └─ 客户管理(F09)                       │
│          │                                           │
│          │  系统管理（管理员专属）                      │
│          │    ├─ 分配合同(F06)   └─ 用户管理(F10)     │
│          │    ├─ 角色管理(F11)   └─ 日志管理(F12)     │
└──────────┴──────────────────────────────────────────┘
```

## 项目结构

```
HeTongGuanLiXitong/
├── pom.xml                          # Maven配置(含Oracle JDBC)
├── doc/
│   ├── start.bat                    # Oracle启动脚本
│   ├── 综合项目评分标准.txt           # 需求文档
│   ├── README.md                    # 本文件
│   ├── 需求分析报告.md               # 需求分析文档
│   ├── 设计报告.md                   # 系统设计文档
│   ├── 测试报告.md                   # 测试文档
│   ├── 使用手册.md                   # 操作手册
│   ├── 项目启动报告.md               # 启动阶段文档
│   └── 关闭报告.md                   # 结项文档
├── src/main/resources/sql/
│   └── init.sql                     # 数据库初始化脚本
└── src/main/java/com/contract/
    ├── App.java                     # 一键启动入口
    ├── util/DBUtil.java             # 数据库连接工具
    ├── entity/                      # 10个实体类
    ├── dao/                         # 10个数据访问类
    ├── service/                     # 6个业务服务类
    └── view/                        # GUI界面
        ├── LoginFrame.java          # 登录窗口
        ├── RegisterFrame.java       # 注册窗口
        ├── MainFrame.java           # 主窗口
        └── panel/                   # 13个功能面板
```

## 快速开始

### 环境要求

- JDK 1.8 或更高版本
- Oracle Database 26ai Free（或兼容版本）
- IDE: IntelliJ IDEA（推荐）或 Eclipse
- Maven 3.x

### 启动步骤

1. **启动数据库**
   ```bash
   # 双击执行或命令行运行
   D:\Java_IDEA\HeTongGuanLiXitong\doc\start.bat
   ```

2. **打开项目**
   - 用 IntelliJ IDEA 打开 `D:\Java_IDEA\HeTongGuanLiXitong` 目录
   - 等待 Maven 自动下载依赖（ojdbc8驱动）

3. **一键运行**
   - 找到 `src/main/java/com/contract/App.java`
   - 右键 → Run 'App.main()'
   - 程序自动完成：数据库连接检测 → 表结构检查 → 初始化 → 显示登录界面

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `admin123` | 拥有所有功能权限，不可删除 |

## 核心特性

- **一键启动**: 自动检测数据库、初始化表结构、轮询等待数据库就绪
- **用户审核**: 新用户注册后需管理员审核通过才能登录
- **RBAC权限**: 基于角色的访问控制，12个功能点精细授权
- **合同流程**: 起草→分配→会签→定稿→审批→签订 完整生命周期
- **日期智能校验**: 结束时间自动修正、快捷按钮调整（半年/一年）
- **内容权限控制**: 合同详情仅管理员和起草人可查看完整内容
- **操作日志**: 自动记录关键操作，支持日志查询审计

## 数据库设计

共 **10张表** + **11个序列**：

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| t_user | 用户表 | id, name, password, status(0待审/1通过/2拒绝) |
| t_role | 角色表 | id, name, description, functions(逗号分隔功能编号) |
| t_function | 功能表 | id, num(F01-F12), name, url |
| t_right | 权限表(用户-角色关联) | id, userName, roleName |
| t_contract | 合同表 | id, num, name, customer, beginTime, endTime, content(CLOB), userName |
| t_contract_process | 合同操作流程表 | id, conNum, type(1会签/2审批/3签订), state, userName, content, time |
| t_contract_state | 合同状态表 | id, conNum, type(1-5), time |
| t_log | 日志表 | id, userName, content, time |
| t_customer | 客户表 | id, num, name, address, tel, fax, code, bank, account |
| t_contract_attachment | 合同附件表 | id, conNum, fileName, path, type, uploadTime |

## 开发者信息

- **项目名称**: 合同管理系统 (HeTongGuanLiXitong)
- **开发语言**: Java (JDK 1.8)
- **数据库**: Oracle Database 26ai Free
- **构建方式**: Maven
- **GUI框架**: Java Swing
