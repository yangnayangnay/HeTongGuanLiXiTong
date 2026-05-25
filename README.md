# 合同管理系统

基于 C++ / Qt 6 / SQLite 的桌面端合同全生命周期管理系统，支持多角色协作完成合同起草、会签、定稿、审批、签订全流程。

## 环境要求

| 依赖 | 版本 |
|------|------|
| Qt | 6.10.0 (msvc2022_64) |
| Visual Studio | 2022 (v145 工具集) |
| MSVC Build Tools | C++ 桌面开发工作负载 |
| C++ 标准 | C++20 |

## 项目结构

```
合同管理系统/
├── 合同管理系统.slnx                    # VS 解决方案
└── 合同管理系统/
    ├── 合同管理系统.vcxproj             # VS 项目文件
    ├── 合同管理系统.vcxproj.filters     # 筛选器
    ├── 合同管理系统.vcxproj.user        # 用户配置
    ├── 合同管理系统.qtvscr              # Qt VS 配置
    ├── run_moc.bat                      # MOC 预处理脚本
    │
    ├── main.cpp                         # 程序入口，全局用户状态
    ├── logger.h / logger.cpp            # 日志系统（单例，线程安全，写文件）
    ├── databasemanager.h / .cpp         # 数据库管理（SQLite，单例）
    ├── logindialog.h / .cpp             # 登录对话框
    ├── registerdialog.h / .cpp          # 注册对话框
    ├── mainwindow.h / .cpp              # 主窗口 + 侧边栏导航
    ├── dashboardwidget.h / .cpp         # 系统首页 / 仪表盘
    ├── contractcreatedialog.h / .cpp    # 起草合同对话框
    ├── contractlistwidget.h / .cpp      # 合同列表（搜索 + 状态筛选）
    ├── contractdetailwidget.h / .cpp    # 合同详情 + 流程人员表
    ├── contractassigndialog.h / .cpp    # 分配合同对话框
    ├── countersigndialog.h / .cpp       # 会签对话框
    ├── finalizedialog.h / .cpp          # 定稿对话框
    ├── approvedialog.h / .cpp           # 审批对话框（通过/拒绝）
    ├── signdialog.h / .cpp              # 签订对话框
    ├── mytaskswidget.h / .cpp           # 我的任务（待处理 + 已处理）
    ├── flowquerywidget.h / .cpp         # 流程查询
    ├── customerwidget.h / .cpp          # 客户管理 CRUD
    ├── userwidget.h / .cpp              # 用户管理（角色分配）
    ├── logswidget.h / .cpp              # 操作日志表格
    │
    └── moc_*.cpp                        # MOC 自动生成的元对象代码
```

## 编译步骤

1. 用 Visual Studio 2022 打开 `合同管理系统.slnx`
2. 选择 **Debug x64** 或 **Release x64** 配置
3. 按 **Ctrl+Shift+B** 编译

> 编译前 `run_moc.bat` 会通过 PreBuildEvent 自动运行，为含 `Q_OBJECT` 宏的头文件生成 `moc_*.cpp`。

## 运行

编译成功后直接运行，或在 VS 中按 **F5** 调试运行。

首次运行自动在 `%AppData%` 下创建 SQLite 数据库并插入默认账户。

### 测试账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 (admin) | admin | admin123 |
| 操作员 (operator) | operator | operator123 |

> 新注册用户默认角色为"新用户"（new），需管理员在用户管理中分配权限。

## 角色与权限

| 功能 | 管理员 (admin) | 操作员 (operator) | 新用户 (new) |
|------|:-:|:-:|:-:|
| 系统首页 | ✅ | ✅ | ✅ |
| 起草合同 | ✅ | ✅ | ❌ |
| 合同列表 | ✅ | ✅ | ✅（仅可见自己参与的） |
| 我的任务 | ✅ | ✅ | ✅ |
| 流程查询 | ✅ | ✅ | ✅ |
| 分配合同 | ✅ | ❌ | ❌ |
| 客户管理 | ✅ | ❌ | ❌ |
| 用户管理 | ✅ | ❌ | ❌ |
| 操作日志 | ✅ | ❌ | ❌ |

## 功能模块

### 注册与登录

- 用户注册：填写用户名、密码、真实姓名，默认角色为"新用户"
- 用户名至少3个字符，密码至少6个字符
- 登录后根据角色显示不同的侧边栏导航项

### 合同管理

- **起草合同**：操作员/管理员填写合同名称、客户、起止日期、合同内容，可上传附件
- **分配合同**：管理员指定会签人员、审批人员、签订人员（支持多选）
- **会签合同**：被分配的会签人员填写会签意见，所有人完成后合同状态流转至"会签完成"
- **定稿合同**：起草人根据会签意见修改合同内容后定稿
- **审批合同**：审批人通过或拒绝合同，拒绝时需填写拒绝理由
- **签订合同**：签订人录入签订信息完成签订

### 合同流程

```
起草(drafted)
  ↓ 管理员分配人员
会签中(countersigning)
  ↓ 所有会签人完成
会签完成(countersigned)
  ↓ 起草人定稿
定稿完成(finalized)
  ↓ 审批人通过
审批完成(approved)
  ↓ 签订人签订
签订完成(signed)
```

### 查询统计

- 仪表盘：合同总数、各状态数量统计、待办任务、最近合同
- 合同列表：按名称/内容模糊搜索，按状态筛选
- 流程查询：按流程阶段查看合同列表
- 我的任务：查看待处理和已处理的任务

### 基础数据管理（管理员）

- 客户信息 CRUD（名称、联系人、电话、邮箱、地址）
- 用户角色管理（管理员/操作员/新用户）

### 系统管理（管理员）

- 操作日志查看（用户、操作、对象、详情、时间）
- 用户权限分配

## UI 设计

采用紫色渐变主题，左侧固定导航栏 + 右侧内容区布局：

- **侧边栏**：紫色渐变背景（#2d1b69 → #6c5ce7），白色文字，选中项左侧金色边框
- **顶栏**：白色背景，显示当前用户信息
- **表格**：圆角边框，紫色表头，选中行紫色高亮
- **按钮**：紫色渐变，圆角，悬停/按下状态变化
- **输入框**：圆角边框，聚焦时紫色边框

## 日志系统

自定义 Logger 单例类，线程安全（QMutex），同时输出到文件和 Qt 调试通道。

日志自动写入 `%AppData%/contract_system.log`，格式：

```
[2026-05-14 10:30:00.123] [INFO] [MAIN] Application starting...
[2026-05-14 10:30:00.456] [ERROR] [DB] Database open failed: ...
```

日志级别：INFO / WARN / ERROR

使用方式：

```cpp
LOG_INFO("MODULE", "message");
LOG_WARN("MODULE", "message");
LOG_ERROR("MODULE", "message");
```

## 数据库

使用 SQLite，数据文件位于 `%AppData%/contract.db`。

### 数据表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| users | 用户表 | id, username, password(SHA256), role, real_name, created_at |
| customers | 客户表 | id, name, contact, phone, email, address, created_at |
| contracts | 合同表 | id, name, customer_id, start_date, end_date, content, status, creator_id, attachment, created_at, updated_at |
| contract_assignments | 合同分配表 | id, contract_id, user_id, role_type, status, opinion, created_at |
| operation_logs | 操作日志表 | id, user_id, username, operation, target, detail, created_at |

### 合同状态

| 状态值 | 显示名 | 说明 |
|--------|--------|------|
| drafted | 起草 | 刚创建的合同 |
| countersigning | 会签中 | 管理员已分配人员，等待会签 |
| countersigned | 会签完成 | 所有人完成会签 |
| finalized | 定稿完成 | 起草人修改定稿 |
| approved | 审批完成 | 审批人通过 |
| signed | 签订完成 | 签订人完成签订 |

### 分配角色类型

| 角色类型值 | 显示名 | 说明 |
|-----------|--------|------|
| countersign | 会签人 | 负责会签合同 |
| approve | 审批人 | 负责审批合同 |
| sign | 签订人 | 负责签订合同 |

## 技术要点

- **Qt MOC**：通过 `run_moc.bat` 在编译前自动为含 Q_OBJECT 的头文件生成元对象代码，解决 VS 项目不自动运行 MOC 的问题
- **编码处理**：vcxproj 中配置 `/utf-8` 编译选项，确保 MSVC 正确处理中文字符串
- **弃用警告**：定义 `QT_NO_DEPRECATED_WARNINGS` 宏 + `/wd4996` 编译选项，屏蔽 Qt 弃用 API 警告
- **数据库**：使用 QtSql 模块操作 SQLite，密码使用 SHA-256 哈希存储
- **日志**：自定义 Logger 单例类，线程安全（QMutex），同时输出到文件和 Qt 调试通道
- **全局状态**：`main.cpp` 中定义全局变量 `g_currentUserId`、`g_currentUsername`、`g_currentRole`、`g_currentRealName`，登录后设置，各模块通过 `extern` 引用

## 常见问题

### 编译报错"无法解析的外部符号"

MOC 生成的 `moc_*.cpp` 文件缺失或过期。确认：
1. `run_moc.bat` 中的 moc.exe 路径正确（默认 `D:\Qt\6.10.0\msvc2022_64\bin\moc.exe`）
2. vcxproj 的 PreBuildEvent 配置了调用 `run_moc.bat`
3. 所有 `moc_*.cpp` 文件已加入 vcxproj 的编译列表

### 编译报错中文乱码

确认 vcxproj 中已添加 `/utf-8` 编译选项：
```xml
<AdditionalOptions>/utf-8 %(AdditionalOptions)</AdditionalOptions>
```

### 运行时缺少 Qt DLL

将 Qt 的 bin 目录加入 PATH，或使用 `windeployqt` 工具部署：
```bash
D:\Qt\6.10.0\msvc2022_64\bin\windeployqt.exe 合同管理系统.exe
```

### 数据库文件位置

SQLite 数据库文件位于 `%AppData%/contract.db`，日志文件位于 `%AppData%/contract_system.log`。可在文件资源管理器地址栏输入 `%AppData%` 快速定位。
