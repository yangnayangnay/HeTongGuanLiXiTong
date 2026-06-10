<p align="center">
  <h1 align="center">合同管理系统</h1>
  <p align="center">HeTongGuanLiXitong — 基于 Java Swing + Oracle 的桌面端合同全生命周期管理平台</p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-1.8%2B-orange" alt="Java" />
  <img src="https://img.shields.io/badge/Swing-GUI-blue" alt="Swing" />
  <img src="https://img.shields.io/badge/Oracle-26ai_Free-red" alt="Oracle" />
  <img src="https://img.shields.io/badge/Maven-3.x-green" alt="Maven" />
  <img src="https://img.shields.io/badge/功能点-12个-brightgreen" alt="Features" />
  <img src="https://img.shields.io/badge/状态-已完成-success" alt="Status" />
</p>

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [系统截图](#系统截图)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
- [功能模块](#功能模块)
- [数据库设计](#数据库设计)
- [权限体系](#权限体系)
- [项目文档](#项目文档)
- [开发者信息](#开发者信息)

---

## 项目简介

本系统是一套面向企业的**合同全生命周期管理平台**，基于 **Java Swing** 桌面端 GUI + **Oracle Database** 数据库开发。实现了从合同**起草 → 分配 → 会签 → 定稿 → 审批 → 签订**的完整业务流程，并内置 **RBAC 角色权限控制**、**用户审核机制**、**操作日志审计**等企业级能力。

### 核心价值

| 能力 | 说明 |
|------|------|
| 全流程覆盖 | 合同从创建到归档的6个环节闭环管理 |
| 权限精细控制 | 12个功能点独立授权，5种预置角色 |
| 一键部署 | 运行一个入口即可自动完成DB检测→初始化→启动 |
| 安全合规 | 用户注册审核、管理员自保护、内容级权限隔离 |

---

## 功能特性

### A级核心功能（12项）

| 编号 | 功能 | 描述 |
|------|------|------|
| F01 | 起草合同 | 创建新合同，智能日期默认值与校验 |
| F02 | 会签合同 | 多人会签审阅，填写会签意见 |
| F03 | 定稿合同 | 汇总会签意见，确定最终文本（自动加载原文） |
| F04 | 审批合同 | 管理层审批通过或驳回 |
| F05 | 签订合同 | 正式签订，流程结束 |
| F06 | 分配合同 | 管理员指定各环节负责人（按权限过滤） |
| F07 | 合同查询 | 模糊搜索 + 详情展示（权限控制内容可见性） |
| F08 | 流程查询 | 按状态筛选查看完整流转历史 |
| F09 | 客户管理 | 客户信息增删改查 |
| F10 | 用户管理 | 用户CRUD + 注册审核 + 角色分配 |
| F11 | 角色管理 | 创建角色并配置功能权限 |
| F12 | 日志管理 | 操作日志查询审计 |

### B/C级扩展功能

- **用户审核流程**：新用户注册后需管理员审核才能登录
- **权限过滤分配**：分配合同时只显示有对应权限的人员
- **内容权限控制**：合同详情仅管理员和起草人可查看全文
- **日期智能调整**：半年/一年快捷按钮 + 自动最小值保护
- **一键启动**：自动检测DB、初始化表、轮询等待就绪
- **管理员自保护**：不能取消自身管理员角色

---

## 系统截图

<!-- 在此处添加系统截图 -->

<table>
<tr>
<td width="50%"><b>登录界面</b><br><!-- 登录界面截图 --></td>
<td width="50%"><b>主界面（管理员）</b><br><!-- 主界面截图 --></td>
</tr>
<tr>
<td width="50%"><b>起草合同</b><br><!-- 起草合同截图 --></td>
<td width="50%"><b>分配合同（权限过滤）</b><br><!-- 分配合同截图 --></td>
</tr>
<tr>
<td width="50%"><b>定稿合同（自动加载）</b><br><!-- 定稿合同截图 --></td>
<td width="50%"><b>合同查询（权限控制）</b><br><!-- 合同查询截图 --></td>
</tr>
<tr>
<td width="50%"><b>用户管理（含待审核区域）</b><br><!-- 用户管理截图 --></td>
<td width="50%"><b>日志管理</b><br><!-- 日志管理截图 --></td>
</tr>
</table>

---

## 技术架构

```
┌─────────────────────────────────────────────────────┐
│                  表现层 (View Layer)                   │
│   LoginFrame / RegisterFrame / MainFrame              │
│   13个功能面板 (panel/*.java)                         │
├─────────────────────────────────────────────────────┤
│                业务逻辑层 (Service Layer)               │
│   UserService / ContractService / ...                 │
├─────────────────────────────────────────────────────┤
│                 数据访问层 (DAO Layer)                  │
│   UserDao / ContractDao / ...                        │
├─────────────────────────────────────────────────────┤
│                    工具层 (Util)                       │
│                DBUtil (连接池管理)                     │
├─────────────────────────────────────────────────────┤
│              数据持久层 (Oracle 26ai)                  │
│           10张表 + 11个序列                            │
└─────────────────────────────────────────────────────┘
```

---

## 快速开始

### 环境要求

| 组件 | 要求 |
|------|------|
| JDK | 1.8 或更高版本 |
| Oracle Database | 26ai Free (或其他兼容版本) |
| IDE | IntelliJ IDEA (推荐) 或 Eclipse |
| Maven | 3.x |

### 三步启动

```bash
# 第1步：启动数据库（如未运行）
双击 doc\start.bat

# 第2步：用IDEA打开项目
打开 D:\Java_IDEA\HeTongGuanLiXitong 目录

# 第3步：运行程序
右键 App.java → Run 'App.main()'
# 程序自动完成: 连接检测 → 表检查 → 初始化 → 登录界面
```

### 默认账号

| 角色 | 用户名 | 密码 | 权限范围 |
|------|--------|------|----------|
| 管理员 | `admin` | `admin123` | 全部12个功能 |

> 登录后请及时修改密码。

---

## 功能模块

### 合同完整流程

```
  ┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐
  │ 起草  │ →  │ 分配  │ →  │ 会签  │ →  │ 定稿  │ →  │ 审批  │ →  签订
  └──────┘    └──────┘    └──────┘    └──────┘    └──────┘
   F01         F06(管理员)  F02         F03         F04        F05
```

### 启动流程

```
运行 App.main()
    │
    ├─ 显示 Splash 画面 + 进度条
    │
    ├─ tryConnect(): 连接数据库
    │   ├─ 成功 → 继续
    │   └─ 失败 → 弹窗询问 → 执行start.bat → 每5秒轮询(最长5分钟)
    │
    ├─ checkTablesExist(): 检查表结构
    │   └─ 不存在 → initDatabase()
    │
    ├─ migrateDatabase(): 兼容性迁移(自动ALTER TABLE)
    │
    └─ 显示 LoginFrame
```

---

## 数据库设计

### ER关系概览

```
t_user ──1:N── t_right ──N:1── t_role ──N:M── t_function
 (用户)            (权限表)      (角色)          (功能)
                                    │
                              functions字段
                           (逗号分隔F01,F02...)

t_contract ──1:N── t_contract_process   (合同流程记录)
     │
     ├── N:1 ── t_customer             (客户信息)
     ├── 1:N ── t_contract_state       (状态变更记录)
     └── 1:N ── t_contract_attachment  (附件)

t_log                                 (操作日志)
```

### 数据表清单

| 表名 | 说明 | 记录数 |
|------|------|--------|
| `t_user` | 用户表（含审核状态） | - |
| `t_role` | 角色表（含功能列表） | 5条预置 |
| `t_function` | 功能定义表 | 12条(F01~F12) |
| `t_right` | 用户-角色关联表 | - |
| `t_contract` | 合同主表 | - |
| `t_contract_process` | 合同操作流程表 | - |
| `t_contract_state` | 合同状态变更表 | - |
| `t_customer` | 客户信息表 | 2条示例 |
| `t_log` | 操作日志表 | - |
| `t_contract_attachment` | 合同附件表 | - |

---

## 权限体系 (RBAC)

### 预置角色与权限矩阵

| 功能 | 管理员 | 合同操作员 | 会签人员 | 审批人员 | 签订人员 |
|------|:------:|:----------:|:--------:|:--------:|:--------:|
| F01 起草合同 | ✓ | ✓ | ✗ | ✗ | ✗ |
| F02 会签合同 | ✓ | ✗ | ✓ | ✗ | ✗ |
| F03 定稿合同 | ✓ | ✓ | ✗ | ✗ | ✗ |
| F04 审批合同 | ✓ | ✗ | ✗ | ✓ | ✗ |
| F05 签订合同 | ✓ | ✗ | ✗ | ✗ | ✓ |
| F06 分配合同 | ✓ | ✗ | ✗ | ✗ | ✗ |
| F07 合同查询 | ✓ | ✓ | ✓ | ✓ | ✓ |
| F08 流程查询 | ✓ | ✓ | ✓ | ✓ | ✓ |
| F09 客户管理 | ✓ | ✗ | ✗ | ✗ | ✗ |
| F10 用户管理 | ✓ | ✗ | ✗ | ✗ | ✗ |
| F11 角色管理 | ✓ | ✗ | ✗ | ✗ | ✗ |
| F12 日志管理 | ✓ | ✗ | ✗ | ✗ | ✗ |

### 内容级权限

- **管理员 / 合同起草人** → 可查看完整合同正文
- **其他用户** → 仅显示基本信息，正文区显示权限提示

---

## 项目文档

| 文档 | 格式 | 说明 |
|------|------|------|
| [需求分析报告](doc/需求分析报告.md) | MD / DOCX | 功能需求、角色分析、用例详述 |
| [设计报告](doc/设计报告.md) | MD / DOCX | 架构设计、数据库设计、类图、状态机 |
| [测试报告](doc/测试报告.md) | MD / DOCX | 18个测试用例、Bug记录、结论 |
| [使用手册](doc/使用手册.md) | MD / DOCX | 安装指南、操作步骤、FAQ |
| [项目启动报告](doc/项目启动报告.md) | MD / DOCX | 项目背景、目标、计划、风险 |
| [关闭报告](doc/关闭报告.md) | MD / DOCX | 交付清单、达成统计、经验总结 |

---

## 项目结构

```
HeTongGuanLiXitong/
├── pom.xml                          # Maven 配置
├── .gitignore
├── doc/
│   ├── start.bat                    # Oracle 启动脚本
│   ├── README.md                    # 本文件
│   ├── 需求分析报告.md / .docx
│   ├── 设计报告.md / .docx
│   ├── 测试报告.md / .docx
│   ├── 使用手册.md / .docx
│   ├── 项目启动报告.md / .docx
│   └── 关闭报告.md / .docx
├── src/main/
│   ├── resources/sql/
│   │   └── init.sql                 # 数据库初始化脚本
│   └── java/com/contract/
│       ├── App.java                 # 一键启动入口
│       ├── util/
│       │   └── DBUtil.java          # 数据库连接工具
│       ├── entity/                  # 10 个实体类
│       │   ├── User, Role, Function, Right
│       │   ├── Contract, ContractProcess, ContractState
│       │   └── Customer, ContractAttachment, Log
│       ├── dao/                     # 10 个数据访问类
│       ├── service/                 # 6 个业务服务类
│       └── view/                    # GUI 界面
│           ├── LoginFrame.java      # 登录窗口
│           ├── RegisterFrame.java   # 注册窗口
│           ├── MainFrame.java       # 主窗口
│           └── panel/               # 13 个功能面板
│               ├── ContractDraftPanel.java
│               ├── ContractCountersignPanel.java
│               ├── ContractFinalizePanel.java
│               ├── ContractApprovePanel.java
│               ├── ContractSignPanel.java
│               ├── ContractAssignPanel.java
│               ├── ContractQueryPanel.java
│               ├── ContractProcessQueryPanel.java
│               ├── CustomerManagePanel.java
│               ├── UserManagePanel.java
│               ├── RoleManagePanel.java
│               ├── PermissionManagePanel.java
│               └── LogPanel.java
```

---

## 开发者信息

| 项目属性 | 内容 |
|----------|------|
| 项目名称 | 合同管理系统 (HeTongGuanLiXitong) |
| 开发语言 | Java (JDK 1.8) |
| GUI框架 | Java Swing (javax.swing) |
| 数据库 | Oracle Database 26ai Free |
| 构建工具 | Apache Maven 3.x |
| 代码规模 | 47个源文件 / ~12000行 |
| 功能达成 | 18/18 需求 (100%) |
| 文档交付 | 7份 (MD + DOCX 双版本) |

---

<p align="center">
  Made with ❤️ for Course Project
</p>
