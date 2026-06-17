# 合同管理系统 (HeTongGuanLiXitong)

## 项目简介

基于 **Spring Boot 2.7.18** + **Thymeleaf** + **Oracle Database** 的B/S架构合同管理系统，实现合同全生命周期管理（起草→分配→会签→定稿→审批→签订），支持 RBAC 角色权限控制、电子签名、AI智能起草、邮件通知、流程看板、统计分析等功能。

## 技术栈

| 技术 | 版本/说明 |
|------|-----------|
| 后端框架 | Spring Boot 2.7.18 |
| 模板引擎 | Thymeleaf |
| 数据库 | Oracle 26ai Free (localhost:1521/freepdb1) |
| 构建工具 | Maven 3.x |
| 前端框架 | Bootstrap 4.6.2 + Font Awesome 5.15.4 |
| AI辅助 | Ollama (qwen2:7b模型，端口11434) |
| 邮件通知 | JavaMail + SMTP |
| PDF导出 | iText |
| 文件上传 | Multipart + 数据库存储 |
| JDK | 1.8+ |

## 功能概览

```
┌──────────────────────────────────────────────────────────────────┐
│                        合同管理系统                                │
├───────────┬──────────────────────────────────────────────────────┤
│           │  合同管理                                            │
│           │    ├─ 起草合同(F01)   ├─ 分配合同(F06)               │
│           │    ├─ 会签合同(F02)   ├─ 定稿合同(F03)               │
│ 顶部导航  │    ├─ 审批合同(F04)   └─ 签订合同(F05)               │
│ + 侧边栏  │                                                     │
│           │  查询统计                                            │
│           │    ├─ 合同查询(F07)   ├─ 流程查询(F08)               │
│           │    ├─ 流程看板        └─ 统计分析                    │
│           │                                                     │
│           │  基础数据管理                                        │
│           │    └─ 客户管理(F09)                                  │
│           │                                                     │
│           │  系统管理（管理员专属）                                │
│           │    ├─ 用户管理(F10)   ├─ 角色管理(F11)               │
│           │    ├─ 日志管理(F12)   └─ 系统设置                    │
│           │                                                     │
│           │  智能辅助                                            │
│           │    ├─ AI智能起草      ├─ 电子签名                    │
│           │    └─ 邮件通知        └─ 待办任务铃铛提醒            │
└───────────┴──────────────────────────────────────────────────────┘
```

## 项目结构

```
HeTongGuanLiXitong/
├── pom.xml                          # Maven配置(含Spring Boot + Oracle JDBC)
├── doc/
│   ├── start.bat                    # Oracle启动脚本
│   ├── README.md                    # 本文件
│   ├── 需求分析报告.md               # 需求分析文档
│   ├── 设计报告.md                   # 系统设计文档
│   ├── 测试报告.md                   # 测试文档
│   ├── 使用手册.md                   # 操作手册
│   ├── 项目启动报告.md               # 启动阶段文档
│   ├── 关闭报告.md                   # 结项文档
│   └── 系统架构说明.md               # B/S架构说明
├── src/main/java/com/contract/
│   ├── ContractApplication.java     # Spring Boot启动入口
│   ├── config/
│   │   └── DataInitializer.java     # 数据库自动初始化
│   ├── controller/
│   │   ├── PageController.java      # 页面路由控制器
│   │   └── ApiController.java       # RESTful API控制器
│   ├── dao/                         # 10个数据访问类
│   ├── entity/                      # 10个实体类
│   ├── service/                     # 业务服务类
│   │   ├── ContractService.java
│   │   ├── UserService.java
│   │   ├── CustomerService.java
│   │   ├── RoleService.java
│   │   ├── RightService.java
│   │   ├── LogService.java
│   │   ├── EmailService.java
│   │   └── AIAssistantService.java
│   └── util/
│       ├── DBUtil.java
│       ├── FileLogger.java
│       └── NetworkUtil.java
├── src/main/resources/
│   ├── templates/                   # Thymeleaf HTML模板
│   │   ├── login.html               # 登录页
│   │   ├── register.html            # 注册页
│   │   ├── main.html                # 主框架(含iframe)
│   │   ├── draft.html               # 起草合同
│   │   ├── assign.html              # 分配合同
│   │   ├── countersign.html         # 会签合同
│   │   ├── finalize.html            # 定稿合同
│   │   ├── approve.html             # 审批合同
│   │   ├── sign.html                # 签订合同
│   │   ├── query.html               # 合同查询
│   │   ├── flow.html                # 流程查询
│   │   ├── kanban.html              # 流程看板
│   │   ├── statistics.html          # 统计分析
│   │   ├── pending.html             # 待办任务
│   │   ├── customer.html            # 客户管理
│   │   ├── user.html                # 用户管理
│   │   ├── role.html                # 角色管理
│   │   ├── log.html                 # 日志管理
│   │   └── settings.html            # 系统设置
│   ├── static/
│   │   ├── css/style.css
│   │   └── js/app.js                # 全局JS工具函数
│   └── application.properties       # Spring Boot配置
└── src/main/resources/sql/
    └── init.sql                     # 数据库初始化脚本
```

## 快速开始

### 环境要求

- JDK 1.8 或更高版本
- Oracle Database 26ai Free（或兼容版本）
- Maven 3.x
- （可选）Ollama + qwen2:7b模型（用于AI智能起草功能）
- 现代浏览器（Chrome/Firefox/Edge）

### 启动步骤

1. **启动数据库**
   ```bash
   # 双击执行或命令行运行
   D:\Java_IDEA\HeTongGuanLiXitong\doc\start.bat
   ```

2. **打开项目**
   - 用 IntelliJ IDEA 打开 `D:\Java_IDEA\HeTongGuanLiXitong` 目录
   - 等待 Maven 自动下载依赖

3. **启动应用**
   - 找到 `src/main/java/com/contract/ContractApplication.java`
   - 右键 → Run 'ContractApplication.main()'
   - 程序自动完成：数据库连接检测 → 表结构检查 → 初始化 → 启动Web服务

4. **访问系统**
   - 浏览器打开 http://localhost:8080
   - 进入登录页面

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `admin123` | 拥有所有功能权限，不可删除 |

## 核心特性

- **B/S架构**: Spring Boot + Thymeleaf，浏览器访问，无需安装客户端
- **合同全流程**: 起草→分配→会签→定稿→审批→签订 完整生命周期
- **审批否决退回**: 审批否决后退回定稿阶段，保留意见供查看
- **RBAC权限**: 基于角色的访问控制，后端API验证，12个功能点精细授权
- **电子签名**: Canvas手写签名 + 图片插入合同
- **AI智能起草**: 基于Ollama本地AI，模板填充模式自动生成合同内容
- **邮件通知**: SMTP邮件，任务分配/完成/否决时自动通知相关人员
- **流程看板**: 6列看板（起草中/会签中/定稿中/审批中/签订中/已完成）
- **统计分析**: 合同状态分布图表
- **待办任务铃铛**: 实时更新待办任务提醒
- **合同导出**: 支持PDF/DOCX格式导出
- **文件上传**: 文件上传至数据库存储
- **历史意见查看**: 每步操作可查看之前所有人的意见
- **合同查询**: 模糊搜索 + 时间范围 + 进度筛选
- **流程查询**: 时间范围 + 状态筛选
- **系统设置**: SMTP邮件配置、AI配置、电子签名设置
- **用户审核**: 新用户注册后需管理员审核通过才能登录
- **操作日志**: 自动记录关键操作，支持日志查询审计
- **防重复提交**: 按钮锁定机制
- **日期智能校验**: 结束时间不能早于开始时间
- **合同编号自动生成**: Oracle序列，格式HT+日期+序号

## 数据库设计

共 **12张表** + **序列**：

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
| t_settings | 系统设置表 | id, key, value, description |
| t_contract_file | 合同文件表 | id, conNum, fileName, fileData(BLOB), uploadTime |

## 开发者信息

- **项目名称**: 合同管理系统 (HeTongGuanLiXitong)
- **架构模式**: B/S (Spring Boot + Thymeleaf)
- **开发语言**: Java (JDK 1.8)
- **数据库**: Oracle Database 26ai Free
- **构建方式**: Maven
- **代码规模**: Java源文件约50个，HTML模板约20个，代码总行数约15000行
