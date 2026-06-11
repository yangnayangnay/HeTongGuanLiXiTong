# B/S架构迁移指南

## 一、当前架构分析

### 现状
- **架构**: C/S (Client-Server)
- **前端**: Java Swing桌面GUI应用
- **通信**: JDBC直连Oracle数据库
- **部署**: 用户需安装JRE运行JAR包

### 迁移目标
- **架构**: B/S (Browser-Server)
- **前端**: Vue.js 3 + Element Plus
- **后端**: Spring Boot RESTful API
- **通信**: HTTP/HTTPS JSON
- **部署**: Docker容器化部署

## 二、迁移步骤

### Phase 1: 后端API化 (预计工作量: 3-5天)

#### 1.1 引入Spring Boot依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

#### 1.2 RESTful API设计
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| GET | /api/contracts | 合同列表 |
| GET | /api/contracts/{id} | 合同详情 |
| POST | /api/contracts | 创建合同 |
| PUT | /api/contracts/{id} | 更新合同 |
| DELETE | /api/contracts/{id} | 删除合同 |
| GET | /api/contracts/{id}/processes | 流程记录 |
| POST | /api/contracts/{id}/countersign | 会签操作 |
| POST | /api/contracts/{id}/finalize | 定稿操作 |
| POST | /api/contracts/{id}/approve | 审批操作 |
| POST | /api/contracts/{id}/sign | 签订操作 |
| GET | /api/customers | 客户列表 |
| GET | /api/users | 用户列表 |
| GET | /api/users/{id}/roles | 用户角色 |
| GET | /api/logs | 日志列表 |
| GET | /api/statistics | 统计数据 |

#### 1.3 Controller层示例
```java
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    /**
     * 获取合同列表（支持分页和筛选）
     */
    @GetMapping
    public ResponseEntity<PageResult<Contract>> listContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        // 调用Service层查询并返回分页结果
    }

    /**
     * 创建新合同
     */
    @PostMapping
    public ResponseEntity<Result<Contract>> createContract(@RequestBody ContractDTO dto) {
        // 验证数据 -> 调用Service -> 返回创建的合同对象
    }

    /**
     * 会签操作
     */
    @PostMapping("/{id}/countersign")
    public ResponseEntity<Result<Void>> countersign(
            @PathVariable Long id,
            @RequestBody CountersignRequest request) {
        // 验证权限 -> 执行会签逻辑 -> 返回结果
    }
}
```

### Phase 2: 前端Vue.js重构 (预计工作量: 5-7天)

#### 2.1 项目结构
```
frontend/
├── src/
│   ├── views/
│   │   ├── Login.vue
│   │   ├── Dashboard.vue
│   │   ├── ContractList.vue
│   │   ├── ContractDetail.vue
│   │   ├── ContractDraft.vue
│   │   └── ...
│   ├── components/
│   │   ├── SignaturePad.vue
│   │   ├── CalendarPicker.vue
│   │   └── KanbanBoard.vue
│   ├── api/
│   │   ├── contract.js
│   │   ├── user.js
│   │   └── auth.js
│   └── router/index.js
├── package.json
└── vite.config.js
```

#### 2.2 页面映射关系
| Swing Panel | Vue Component | 说明 |
|-------------|---------------|------|
| LoginFrame | Login.vue | 登录页 |
| MainFrame | Dashboard.vue + Layout | 主框架+导航 |
| ContractDraftPanel | ContractDraft.vue | 起草 |
| ContractCountersignPanel | ContractProcess.vue?type=countersign | 会签 |
| ContractQueryPanel | ContractList.vue | 查询 |
| KanbanBoardPanel | KanbanBoard.vue | 看板 |
| SettingsPanel | Settings.vue | 设置 |

### Phase 3: 安全与部署 (预计工作量: 2-3天)

- Spring Security + JWT认证
- HTTPS加密传输
- Docker Compose一键部署
- Nginx反向代理

## 三、保留C/S优势的功能

以下Swing特有功能在Web端需要特殊处理：
1. **电子签名** → Canvas手写组件（已有成熟库signature_pad.js）
2. **日历选择器** → Element Plus DatePicker
3. **拖拽看板** → Vue.Draggable + vuedraggable
4. **本地AI(Ollama)** → 后端代理转发API请求
5. **断点续传** → 前端分片上传 + 后端合并

## 四、总结

迁移工作量估计: **10-15个工作日**
建议采用渐进式迁移: 先API化后端，再用Vue逐步替换前端
