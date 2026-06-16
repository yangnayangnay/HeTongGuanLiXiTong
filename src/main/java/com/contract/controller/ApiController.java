package com.contract.controller;

import com.contract.entity.*;
import com.contract.service.*;
import com.contract.util.FileLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LogService logService;

    // ==================== 用户相关 ====================

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username,
                                     @RequestParam String password,
                                     HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("currentUser", user);
            session.setAttribute("userName", user.getName());
            session.setAttribute("functions", userService.getUserFunctions(user.getName()));
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", user);
            FileLogger.info("ApiController", "login", "用户登录成功: " + username);
        } else {
            // 检查用户是否存在及状态
            User statusUser = userService.getUserForStatusCheck(username);
            if (statusUser != null && statusUser.getStatus() == UserService.STATUS_PENDING) {
                result.put("success", false);
                result.put("message", "账号待审核，请等待管理员审批");
            } else if (statusUser != null && statusUser.getStatus() == UserService.STATUS_REJECTED) {
                result.put("success", false);
                result.put("message", "账号已被拒绝，请联系管理员");
            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }
        }
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.register(username, password, email);
        if (success) {
            result.put("success", true);
            result.put("message", "注册成功，请等待管理员审核");
        } else {
            result.put("success", false);
            result.put("message", "注册失败，用户名已存在");
        }
        return result;
    }

    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", users);
        return result;
    }

    @GetMapping("/users/pending")
    public Map<String, Object> getPendingUsers() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.findPendingUsers();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", users);
        return result;
    }

    @PostMapping("/users/{id}/approve")
    public Map<String, Object> approveUser(@PathVariable int id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.approveUser(id);
        if (success) {
            result.put("success", true);
            result.put("message", "审核通过成功");
        } else {
            result.put("success", false);
            result.put("message", "审核操作失败");
        }
        return result;
    }

    @PostMapping("/users/{id}/reject")
    public Map<String, Object> rejectUser(@PathVariable int id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.rejectUser(id);
        if (success) {
            result.put("success", true);
            result.put("message", "已拒绝该用户");
        } else {
            result.put("success", false);
            result.put("message", "操作失败");
        }
        return result;
    }

    @PostMapping("/users")
    public Map<String, Object> addUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.addUser(user);
        if (success) {
            result.put("success", true);
            result.put("message", "添加用户成功");
        } else {
            result.put("success", false);
            result.put("message", "添加失败，用户名已存在");
        }
        return result;
    }

    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable int id, @RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        user.setId(id);
        boolean success = userService.updateUser(user);
        if (success) {
            result.put("success", true);
            result.put("message", "修改用户成功");
        } else {
            result.put("success", false);
            result.put("message", "修改用户失败");
        }
        return result;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable int id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.deleteUser(id);
        if (success) {
            result.put("success", true);
            result.put("message", "删除用户成功");
        } else {
            result.put("success", false);
            result.put("message", "删除用户失败");
        }
        return result;
    }

    // ==================== 合同相关 ====================

    @PostMapping("/contracts/draft")
    public Map<String, Object> draftContract(@RequestParam String name,
                                             @RequestParam String customer,
                                             @RequestParam String beginTime,
                                             @RequestParam String endTime,
                                             @RequestParam String content,
                                             @RequestParam(required = false) MultipartFile file,
                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            Contract contract = new Contract();
            contract.setName(name);
            contract.setCustomer(customer);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            contract.setBeginTime(sdf.parse(beginTime));
            contract.setEndTime(sdf.parse(endTime));
            contract.setContent(content);
            contract.setUserName(currentUser.getName());

            // 处理附件
            if (file != null && !file.isEmpty()) {
                contract.setFileName(file.getOriginalFilename());
                contract.setFileType(getFileExtension(file.getOriginalFilename()));
                contract.setFileData(file.getBytes());
            }

            boolean success = contractService.draftContract(contract);
            if (success) {
                result.put("success", true);
                result.put("message", "起草合同成功");
                result.put("data", contract.getNum());
            } else {
                result.put("success", false);
                result.put("message", "起草合同失败");
            }
        } catch (Exception e) {
            FileLogger.error("ApiController", "draftContract", "起草合同异常: " + e.getMessage(), e);
            result.put("success", false);
            result.put("message", "起草合同异常: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/contracts/unassigned")
    public Map<String, Object> getUnassignedContracts() {
        Map<String, Object> result = new HashMap<>();
        List<Contract> contracts = contractService.getUnassignedContracts();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", contracts);
        return result;
    }

    @PostMapping("/contracts/assign")
    public Map<String, Object> assignContract(@RequestParam String conNum,
                                              @RequestParam(value = "countersignUsers[]", required = false) List<String> countersignUsers,
                                              @RequestParam(value = "approveUsers[]", required = false) List<String> approveUsers,
                                              @RequestParam(value = "signUsers[]", required = false) List<String> signUsers) {
        Map<String, Object> result = new HashMap<>();
        if (countersignUsers == null) countersignUsers = new ArrayList<>();
        if (approveUsers == null) approveUsers = new ArrayList<>();
        if (signUsers == null) signUsers = new ArrayList<>();

        boolean success = contractService.assignContract(conNum, countersignUsers, approveUsers, signUsers);
        if (success) {
            result.put("success", true);
            result.put("message", "分配合同成功");
        } else {
            result.put("success", false);
            result.put("message", "分配合同失败，合同状态不正确");
        }
        return result;
    }

    @GetMapping("/contracts/countersign")
    public Map<String, Object> getCountersignContracts(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 1);
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", processes);
        return result;
    }

    @PostMapping("/contracts/countersign")
    public Map<String, Object> countersignContract(@RequestParam int processId,
                                                   @RequestParam String opinion) {
        Map<String, Object> result = new HashMap<>();
        boolean success = contractService.countersignContract(processId, opinion);
        if (success) {
            result.put("success", true);
            result.put("message", "会签提交成功");
        } else {
            result.put("success", false);
            result.put("message", "会签提交失败");
        }
        return result;
    }

    @GetMapping("/contracts/finalize")
    public Map<String, Object> getFinalizeContracts() {
        Map<String, Object> result = new HashMap<>();
        List<ContractState> states = contractService.getContractsByState(2);
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", states);
        return result;
    }

    @PostMapping("/contracts/finalize")
    public Map<String, Object> finalizeContract(@RequestParam String conNum,
                                                @RequestParam String content,
                                                HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        String userName = currentUser != null ? currentUser.getName() : "unknown";
        boolean success = contractService.finalizeContract(conNum, content, userName);
        if (success) {
            result.put("success", true);
            result.put("message", "定稿成功");
        } else {
            result.put("success", false);
            result.put("message", "定稿失败，合同状态不正确");
        }
        return result;
    }

    @GetMapping("/contracts/approve")
    public Map<String, Object> getApproveContracts(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 2);
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", processes);
        return result;
    }

    @PostMapping("/contracts/approve")
    public Map<String, Object> approveContract(@RequestParam int processId,
                                               @RequestParam boolean approved,
                                               @RequestParam String opinion,
                                               HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        String userName = currentUser != null ? currentUser.getName() : "unknown";
        boolean success = contractService.approveContract(processId, approved, opinion, userName);
        if (success) {
            result.put("success", true);
            result.put("message", approved ? "审批通过" : "审批否决");
        } else {
            result.put("success", false);
            result.put("message", "审批操作失败");
        }
        return result;
    }

    @GetMapping("/contracts/sign")
    public Map<String, Object> getSignContracts(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 3);
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", processes);
        return result;
    }

    @PostMapping("/contracts/sign")
    public Map<String, Object> signContract(@RequestParam int processId,
                                            @RequestParam String info,
                                            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        String userName = currentUser != null ? currentUser.getName() : "unknown";
        boolean success = contractService.signContract(processId, info, userName);
        if (success) {
            result.put("success", true);
            result.put("message", "签订成功");
        } else {
            result.put("success", false);
            result.put("message", "签订失败");
        }
        return result;
    }

    @GetMapping("/contracts/query")
    public Map<String, Object> queryContracts(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) Integer state) {
        Map<String, Object> result = new HashMap<>();
        List<Contract> contracts;
        if (name != null && !name.isEmpty()) {
            contracts = contractService.findByName(name);
        } else {
            contracts = contractService.findAll();
        }
        // Filter by state if specified
        if (state != null && state > 0) {
            contracts.removeIf(c -> contractService.getContractStateType(c.getNum()) != state);
        }
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", contracts);
        return result;
    }

    @GetMapping("/contracts/{num}")
    public Map<String, Object> getContractDetail(@PathVariable String num) {
        Map<String, Object> result = new HashMap<>();
        Contract contract = contractService.findByNum(num);
        if (contract != null) {
            // 不返回文件二进制数据到前端
            contract.setFileData(null);
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("data", contract);
        } else {
            result.put("success", false);
            result.put("message", "合同不存在");
        }
        return result;
    }

    @GetMapping("/contracts/{num}/flow")
    public Map<String, Object> getContractFlow(@PathVariable String num) {
        Map<String, Object> result = new HashMap<>();
        List<ContractState> states = contractService.getContractStates(num);
        List<ContractProcess> processes = contractService.getContractProcesses(num);
        Map<String, Object> flowData = new HashMap<>();
        flowData.put("states", states);
        flowData.put("processes", processes);
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", flowData);
        return result;
    }

    // ==================== 角色相关 ====================

    @GetMapping("/roles")
    public Map<String, Object> getAllRoles() {
        Map<String, Object> result = new HashMap<>();
        List<Role> roles = roleService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", roles);
        return result;
    }

    @PostMapping("/roles")
    public Map<String, Object> addRole(@RequestBody Role role) {
        Map<String, Object> result = new HashMap<>();
        boolean success = roleService.addRole(role);
        if (success) {
            result.put("success", true);
            result.put("message", "添加角色成功");
        } else {
            result.put("success", false);
            result.put("message", "添加角色失败");
        }
        return result;
    }

    @PutMapping("/roles/{id}")
    public Map<String, Object> updateRole(@PathVariable int id, @RequestBody Role role) {
        Map<String, Object> result = new HashMap<>();
        role.setId(id);
        boolean success = roleService.updateRole(role);
        if (success) {
            result.put("success", true);
            result.put("message", "修改角色成功");
        } else {
            result.put("success", false);
            result.put("message", "修改角色失败");
        }
        return result;
    }

    @DeleteMapping("/roles/{id}")
    public Map<String, Object> deleteRole(@PathVariable int id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = roleService.deleteRole(id);
        if (success) {
            result.put("success", true);
            result.put("message", "删除角色成功");
        } else {
            result.put("success", false);
            result.put("message", "删除角色失败");
        }
        return result;
    }

    // ==================== 客户相关 ====================

    @GetMapping("/customers")
    public Map<String, Object> getAllCustomers() {
        Map<String, Object> result = new HashMap<>();
        List<Customer> customers = customerService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", customers);
        return result;
    }

    @PostMapping("/customers")
    public Map<String, Object> addCustomer(@RequestBody Customer customer) {
        Map<String, Object> result = new HashMap<>();
        boolean success = customerService.addCustomer(customer);
        if (success) {
            result.put("success", true);
            result.put("message", "添加客户成功");
        } else {
            result.put("success", false);
            result.put("message", "添加客户失败");
        }
        return result;
    }

    @PutMapping("/customers/{id}")
    public Map<String, Object> updateCustomer(@PathVariable int id, @RequestBody Customer customer) {
        Map<String, Object> result = new HashMap<>();
        customer.setId(id);
        boolean success = customerService.updateCustomer(customer);
        if (success) {
            result.put("success", true);
            result.put("message", "修改客户成功");
        } else {
            result.put("success", false);
            result.put("message", "修改客户失败");
        }
        return result;
    }

    @DeleteMapping("/customers/{id}")
    public Map<String, Object> deleteCustomer(@PathVariable int id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = customerService.deleteCustomer(id);
        if (success) {
            result.put("success", true);
            result.put("message", "删除客户成功");
        } else {
            result.put("success", false);
            result.put("message", "删除客户失败");
        }
        return result;
    }

    // ==================== 日志相关 ====================

    @GetMapping("/logs")
    public Map<String, Object> getAllLogs() {
        Map<String, Object> result = new HashMap<>();
        List<Log> logs = logService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", logs);
        return result;
    }

    // ==================== 文件相关 ====================

    @PostMapping("/files/upload")
    public Map<String, Object> uploadFile(@RequestParam String contractNum,
                                          @RequestParam MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return result;
            }

            ContractAttachment attachment = new ContractAttachment();
            attachment.setConNum(contractNum);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setType(getFileExtension(file.getOriginalFilename()));
            attachment.setUploadTime(new Date());

            // 保存文件到服务器
            String uploadDir = "uploads" + File.separator + contractNum;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = uploadDir + File.separator + file.getOriginalFilename();
            file.transferTo(new File(filePath));
            attachment.setPath(filePath);

            boolean success = contractService.addAttachment(attachment);
            if (success) {
                result.put("success", true);
                result.put("message", "上传成功");
            } else {
                result.put("success", false);
                result.put("message", "上传失败");
            }
        } catch (IOException e) {
            FileLogger.error("ApiController", "uploadFile", "文件上传异常: " + e.getMessage(), e);
            result.put("success", false);
            result.put("message", "文件上传异常: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/files/download/{contractNum}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String contractNum) {
        Contract contract = contractService.findByNum(contractNum);
        if (contract != null && contract.getFileData() != null) {
            HttpHeaders headers = new HttpHeaders();
            String fileName = contract.getFileName() != null ? contract.getFileName() : "contract";
            try {
                fileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            } catch (Exception e) {
                // ignore
            }
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(contract.getFileData().length);
            return new ResponseEntity<>(contract.getFileData(), headers, HttpStatus.OK);
        }
        // Check attachments
        List<ContractAttachment> attachments = contractService.getAttachments(contractNum);
        if (!attachments.isEmpty()) {
            ContractAttachment att = attachments.get(0);
            if (att.getFileData() != null) {
                HttpHeaders headers = new HttpHeaders();
                String fileName = att.getFileName() != null ? att.getFileName() : "attachment";
                try {
                    fileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
                } catch (Exception e) {}
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", fileName);
                headers.setContentLength(att.getFileData().length);
                return new ResponseEntity<>(att.getFileData(), headers, HttpStatus.OK);
            }
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== 权限相关 ====================

    @GetMapping("/users/{username}/functions")
    public Map<String, Object> getUserFunctions(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        Set<String> functions = userService.getUserFunctions(username);
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", functions);
        return result;
    }

    @PostMapping("/users/{username}/roles")
    public Map<String, Object> assignUserRoles(@PathVariable String username,
                                               @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<String> roleNames = (List<String>) body.get("roleNames");
            if (roleNames == null) {
                roleNames = new ArrayList<>();
            }

            // 先删除原有角色，再重新分配
            com.contract.dao.RightDao rightDao = new com.contract.dao.RightDao();
            rightDao.deleteByUserName(username);
            for (String roleName : roleNames) {
                Right right = new Right();
                right.setUserName(username);
                right.setRoleName(roleName);
                right.setDescription("分配角色");
                rightDao.insert(right);
            }
            result.put("success", true);
            result.put("message", "分配角色成功");
            FileLogger.info("ApiController", "assignUserRoles", "分配用户角色成功, 用户: " + username + ", 角色: " + roleNames);
        } catch (Exception e) {
            FileLogger.error("ApiController", "assignUserRoles", "分配角色异常: " + e.getMessage(), e);
            result.put("success", false);
            result.put("message", "分配角色失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 前端适配端点（单数路径 + JSON请求体） ====================

    // ---------- 客户相关（前端使用 /api/customer/...） ----------

    @GetMapping("/customer/detail")
    public Map<String, Object> getCustomerDetail(@RequestParam int id) {
        Map<String, Object> result = new HashMap<>();
        List<Customer> all = customerService.findAll();
        Customer found = all.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        if (found != null) {
            result.put("success", true);
            result.put("data", found);
        } else {
            result.put("success", false);
            result.put("message", "客户不存在");
        }
        return result;
    }

    @PostMapping("/customer/add")
    public Map<String, Object> addCustomerJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        Customer customer = new Customer();
        customer.setNum(body.getOrDefault("num", ""));
        customer.setName(body.getOrDefault("name", ""));
        customer.setAddress(body.getOrDefault("address", ""));
        customer.setTel(body.getOrDefault("tel", ""));
        customer.setFax(body.getOrDefault("fax", ""));
        customer.setCode(body.getOrDefault("code", ""));
        customer.setBank(body.getOrDefault("bank", ""));
        customer.setAccount(body.getOrDefault("account", ""));
        boolean ok = customerService.addCustomer(customer);
        result.put("success", ok);
        result.put("message", ok ? "添加成功" : "添加失败");
        return result;
    }

    @PostMapping("/customer/update")
    public Map<String, Object> updateCustomerJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        Customer customer = new Customer();
        customer.setId(Integer.parseInt(body.getOrDefault("id", "0")));
        customer.setNum(body.getOrDefault("num", ""));
        customer.setName(body.getOrDefault("name", ""));
        customer.setAddress(body.getOrDefault("address", ""));
        customer.setTel(body.getOrDefault("tel", ""));
        customer.setFax(body.getOrDefault("fax", ""));
        customer.setCode(body.getOrDefault("code", ""));
        customer.setBank(body.getOrDefault("bank", ""));
        customer.setAccount(body.getOrDefault("account", ""));
        boolean ok = customerService.updateCustomer(customer);
        result.put("success", ok);
        result.put("message", ok ? "更新成功" : "更新失败");
        return result;
    }

    @DeleteMapping("/customer/delete")
    public Map<String, Object> deleteCustomerJson(@RequestParam int id) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = customerService.deleteCustomer(id);
        result.put("success", ok);
        result.put("message", ok ? "删除成功" : "删除失败");
        return result;
    }

    // ---------- 用户相关（前端使用 /api/user/...） ----------

    @GetMapping("/user/detail")
    public Map<String, Object> getUserDetail(@RequestParam int id) {
        Map<String, Object> result = new HashMap<>();
        List<User> all = userService.findAll();
        User found = all.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
        if (found != null) {
            result.put("success", true);
            result.put("data", found);
        } else {
            result.put("success", false);
            result.put("message", "用户不存在");
        }
        return result;
    }

    @PostMapping("/user/add")
    public Map<String, Object> addUserJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String name = body.get("name");
        String password = body.getOrDefault("password", "123456");
        String email = body.getOrDefault("email", "default@example.com");
        boolean ok = userService.register(name, password, email);
        result.put("success", ok);
        result.put("message", ok ? "添加成功" : "添加失败");
        return result;
    }

    @PostMapping("/user/update")
    public Map<String, Object> updateUserJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        User user = new User();
        user.setId(Integer.parseInt(body.getOrDefault("id", "0")));
        user.setName(body.get("name"));
        user.setEmail(body.getOrDefault("email", ""));
        boolean ok = userService.updateUser(user);
        result.put("success", ok);
        result.put("message", ok ? "更新成功" : "更新失败");
        return result;
    }

    @PostMapping("/user/approve")
    public Map<String, Object> approveUserJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        int id = Integer.parseInt(body.getOrDefault("id", "0"));
        int status = Integer.parseInt(body.getOrDefault("status", "1"));
        boolean ok;
        if (status == 1) {
            ok = userService.approveUser(id);
        } else {
            ok = userService.rejectUser(id);
        }
        result.put("success", ok);
        result.put("message", ok ? "操作成功" : "操作失败");
        return result;
    }

    @PostMapping("/user/assignRole")
    public Map<String, Object> assignUserRole(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String username = body.getOrDefault("userId", "");
        String roleName = body.getOrDefault("roleName", "");
        boolean ok = userService.assignRole(username, roleName);
        result.put("success", ok);
        result.put("message", ok ? "分配成功" : "分配失败");
        return result;
    }

    @DeleteMapping("/user/delete")
    public Map<String, Object> deleteUserJson(@RequestParam int id) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = userService.deleteUser(id);
        result.put("success", ok);
        result.put("message", ok ? "删除成功" : "删除失败");
        return result;
    }

    // ---------- 角色相关（前端使用 /api/role/...） ----------

    @GetMapping("/role/detail")
    public Map<String, Object> getRoleDetail(@RequestParam int id) {
        Map<String, Object> result = new HashMap<>();
        List<Role> all = roleService.findAll();
        Role found = all.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
        if (found != null) {
            result.put("success", true);
            result.put("data", found);
        } else {
            result.put("success", false);
            result.put("message", "角色不存在");
        }
        return result;
    }

    @PostMapping("/role/add")
    public Map<String, Object> addRoleJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        Role role = new Role();
        role.setName(body.get("name"));
        role.setDescription(body.getOrDefault("description", ""));
        boolean ok = roleService.addRole(role);
        result.put("success", ok);
        result.put("message", ok ? "添加成功" : "添加失败");
        return result;
    }

    @PostMapping("/role/update")
    public Map<String, Object> updateRoleJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        Role role = new Role();
        role.setId(Integer.parseInt(body.getOrDefault("id", "0")));
        role.setName(body.get("name"));
        role.setDescription(body.getOrDefault("description", ""));
        boolean ok = roleService.updateRole(role);
        result.put("success", ok);
        result.put("message", ok ? "更新成功" : "更新失败");
        return result;
    }

    @DeleteMapping("/role/delete")
    public Map<String, Object> deleteRoleJson(@RequestParam int id) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = roleService.deleteRole(id);
        result.put("success", ok);
        result.put("message", ok ? "删除成功" : "删除失败");
        return result;
    }

    // ---------- 合同流程相关（前端使用 /api/contract/...） ----------

    @GetMapping("/contract/flow")
    public Map<String, Object> getContractFlowList(@RequestParam(required = false) String state,
                                                    @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<ContractProcess> allProcesses = new ArrayList<>();
        List<Contract> contracts = contractService.findAll();
        for (Contract c : contracts) {
            allProcesses.addAll(contractService.getContractProcesses(c.getNum()));
        }
        result.put("success", true);
        result.put("data", allProcesses);
        return result;
    }

    @PostMapping("/contract/assign")
    public Map<String, Object> assignContractJson(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        String conNum = (String) body.get("contractNum");
        @SuppressWarnings("unchecked")
        List<String> countersignUsers = (List<String>) body.getOrDefault("countersignUsers", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> approveUsers = (List<String>) body.getOrDefault("approveUsers", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> signUsers = (List<String>) body.getOrDefault("signUsers", new ArrayList<>());
        boolean ok = contractService.assignContract(conNum, countersignUsers, approveUsers, signUsers);
        result.put("success", ok);
        result.put("message", ok ? "分配成功" : "分配失败");
        return result;
    }

    @PostMapping("/contract/countersign")
    public Map<String, Object> countersignContractJson(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        String contractNum = body.get("contractNum");
        String content = body.getOrDefault("content", "");
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 1);
        int processId = -1;
        for (ContractProcess p : processes) {
            if (p.getConNum().equals(contractNum)) {
                processId = p.getId();
                break;
            }
        }
        if (processId == -1) {
            result.put("success", false);
            result.put("message", "未找到待会签记录");
            return result;
        }
        boolean ok = contractService.countersignContract(processId, content);
        result.put("success", ok);
        result.put("message", ok ? "会签成功" : "会签失败");
        return result;
    }

    @PostMapping("/contract/finalize")
    public Map<String, Object> finalizeContractJson(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        String userName = currentUser != null ? currentUser.getName() : "unknown";
        String contractNum = body.get("contractNum");
        String content = body.getOrDefault("content", "");
        boolean ok = contractService.finalizeContract(contractNum, content, userName);
        result.put("success", ok);
        result.put("message", ok ? "定稿成功" : "定稿失败");
        return result;
    }

    @PostMapping("/contract/approve")
    public Map<String, Object> approveContractJson(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        String contractNum = (String) body.get("contractNum");
        int state = Integer.parseInt(body.getOrDefault("state", "1").toString());
        String content = (String) body.getOrDefault("content", state == 1 ? "审批通过" : "审批拒绝");
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 2);
        int processId = -1;
        for (ContractProcess p : processes) {
            if (p.getConNum().equals(contractNum)) {
                processId = p.getId();
                break;
            }
        }
        if (processId == -1) {
            result.put("success", false);
            result.put("message", "未找到待审批记录");
            return result;
        }
        boolean approved = state == 1;
        boolean ok = contractService.approveContract(processId, approved, content, currentUser.getName());
        result.put("success", ok);
        result.put("message", ok ? (approved ? "审批通过" : "已拒绝") : "审批操作失败");
        return result;
    }

    @PostMapping("/contract/sign")
    public Map<String, Object> signContractJson(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        String contractNum = body.get("contractNum");
        String info = body.getOrDefault("content", "签订确认");
        List<ContractProcess> processes = contractService.getUserPendingProcesses(currentUser.getName(), 3);
        int processId = -1;
        for (ContractProcess p : processes) {
            if (p.getConNum().equals(contractNum)) {
                processId = p.getId();
                break;
            }
        }
        if (processId == -1) {
            result.put("success", false);
            result.put("message", "未找到待签订记录");
            return result;
        }
        boolean ok = contractService.signContract(processId, info, currentUser.getName());
        result.put("success", ok);
        result.put("message", ok ? "签订成功" : "签订失败");
        return result;
    }

    @PostMapping("/contract/draft")
    public Map<String, Object> draftContractJson(@RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String customer,
                                                  @RequestParam(required = false) String beginTime,
                                                  @RequestParam(required = false) String endTime,
                                                  @RequestParam(required = false) String content,
                                                  @RequestParam(required = false) String userName,
                                                  @RequestParam(required = false) MultipartFile file,
                                                  HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        Contract contract = new Contract();
        contract.setName(name);
        contract.setCustomer(customer);
        contract.setContent(content);
        contract.setUserName(currentUser != null ? currentUser.getName() : "unknown");

        try {
            if (beginTime != null && !beginTime.isEmpty()) {
                contract.setBeginTime(new SimpleDateFormat("yyyy-MM-dd").parse(beginTime));
            }
            if (endTime != null && !endTime.isEmpty()) {
                contract.setEndTime(new SimpleDateFormat("yyyy-MM-dd").parse(endTime));
            }
        } catch (Exception e) {
            // ignore parse errors
        }

        if (file != null && !file.isEmpty()) {
            try {
                contract.setFileData(file.getBytes());
                contract.setFileName(file.getOriginalFilename());
                contract.setFileType(file.getContentType());
            } catch (Exception e) {
                FileLogger.error("ApiController", "draftContractJson", "文件读取失败", e);
            }
        }

        boolean ok = contractService.draftContract(contract);
        result.put("success", ok);
        result.put("message", ok ? "起草成功" : "起草失败");
        return result;
    }

    @GetMapping("/contract/opinions")
    public Map<String, Object> getContractOpinions(@RequestParam String num) {
        Map<String, Object> result = new HashMap<>();
        List<ContractProcess> processes = contractService.getContractProcessesByType(num, 1);
        result.put("success", true);
        result.put("data", processes);
        return result;
    }

    @GetMapping("/contract/detail")
    public Map<String, Object> getContractDetailByNum(@RequestParam String num) {
        Map<String, Object> result = new HashMap<>();
        Contract contract = contractService.findByNum(num);
        if (contract != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", contract.getId());
            data.put("num", contract.getNum());
            data.put("name", contract.getName());
            data.put("customer", contract.getCustomer());
            data.put("userName", contract.getUserName());
            data.put("beginTime", contract.getBeginTime());
            data.put("endTime", contract.getEndTime());
            data.put("content", contract.getContent());
            data.put("fileName", contract.getFileName());
            data.put("fileType", contract.getFileType());
            data.put("stateType", contractService.getContractStateType(num));
            data.put("stateName", contractService.getContractStateName(num));
            data.put("canViewFull", true);
            result.put("success", true);
            result.put("data", data);
        } else {
            result.put("success", false);
            result.put("message", "合同不存在");
        }
        return result;
    }

    // ---------- 邮件设置相关 ----------

    @PostMapping("/settings/email")
    public Map<String, Object> saveEmailSettings(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String host = body.get("host");
            int port = Integer.parseInt(body.getOrDefault("port", "465"));
            String email = body.get("email");
            String password = body.get("password");
            com.contract.util.EmailService.configure(host, port, email, password);
            result.put("success", true);
            result.put("message", "邮件配置已保存");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "配置保存失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/settings/ai")
    public Map<String, Object> saveAISettings(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = body.getOrDefault("url", "http://localhost:11434");
            String model = body.getOrDefault("model", "qwen2.5");
            com.contract.util.AIAssistantService.configure(url, model);
            result.put("success", true);
            result.put("message", "AI配置已保存");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "配置保存失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 合同内容下载/上传/签名 ====================

    @PostMapping("/contract/downloadContent")
    public ResponseEntity<byte[]> downloadContractContent(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        String contractName = body.getOrDefault("name", "合同");
        String fileName = contractName + ".txt";

        byte[] fileBytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (Exception e) {
            // ignore
        }
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(fileBytes.length);
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/contract/uploadContent")
    public Map<String, Object> uploadContractContent(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            result.put("success", true);
            result.put("content", content);
            result.put("fileName", file.getOriginalFilename());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件读取失败");
        }
        return result;
    }

    @PostMapping("/contract/uploadSignature")
    public Map<String, Object> uploadSignature(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String base64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String dataUrl = "data:" + file.getContentType() + ";base64," + base64;
            result.put("success", true);
            result.put("signatureUrl", dataUrl);
            result.put("message", "签名图片上传成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "签名图片上传失败");
        }
        return result;
    }

    // ==================== 工具方法 ====================

    @GetMapping("/contract/template")
    public Map<String, Object> getContractTemplate() {
        Map<String, Object> result = new HashMap<>();
        String template = "合同编号：__________\n\n" +
            "甲方（委托方）：__________\n" +
            "乙方（受托方）：__________\n\n" +
            "根据《中华人民共和国合同法》及相关法律法规的规定，甲乙双方在平等、自愿、公平、诚实信用的原则基础上，经协商一致，订立本合同。\n\n" +
            "第一条 合同标的\n__________\n\n" +
            "第二条 合同金额\n本合同总金额为人民币__________元整（￥__________）。\n\n" +
            "第三条 付款方式\n__________\n\n" +
            "第四条 履行期限\n本合同自____年____月____日起至____年____月____日止。\n\n" +
            "第五条 双方权利义务\n1. 甲方权利义务：__________\n2. 乙方权利义务：__________\n\n" +
            "第六条 违约责任\n__________\n\n" +
            "第七条 争议解决\n本合同在履行过程中发生的争议，由双方当事人协商解决；协商不成的，依法向人民法院起诉。\n\n" +
            "第八条 其他约定\n__________\n\n" +
            "甲方（签章）：__________          乙方（签章）：__________\n" +
            "日期：____年____月____日           日期：____年____月____日";
        result.put("success", true);
        result.put("data", template);
        return result;
    }

    @PostMapping("/contract/ai-review")
    public Map<String, Object> aiReviewContract(@RequestParam String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            com.contract.util.AIAssistantService aiService = new com.contract.util.AIAssistantService();
            if (!aiService.isAvailable()) {
                result.put("success", false);
                result.put("message", "AI服务未配置或不可用，请在设置中配置Ollama服务");
                return result;
            }
            String review = aiService.reviewContract(content);
            result.put("success", true);
            result.put("data", review);
        } catch (Exception e) {
            FileLogger.error("ApiController", "aiReviewContract", "AI审查异常: " + e.getMessage(), e);
            result.put("success", false);
            result.put("message", "AI审查失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/contracts/export")
    public ResponseEntity<byte[]> exportContracts(@RequestParam(required = false) String format) {
        try {
            List<Contract> contracts = contractService.findAll();
            byte[] data;
            String fileName;
            if ("html".equals(format)) {
                data = com.contract.util.DataExportUtil.exportContractsHtml(contracts).getBytes("UTF-8");
                fileName = "contracts.html";
            } else {
                data = com.contract.util.DataExportUtil.exportContractsCsv(contracts).getBytes("UTF-8");
                fileName = "contracts.csv";
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(data.length);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        List<Contract> all = contractService.findAll();
        int total = all.size();
        int draft = 0, countersign = 0, finalize = 0, approve = 0, sign = 0;
        double totalAmount = 0;
        for (Contract c : all) {
            int st = contractService.getContractStateType(c.getNum());
            switch (st) {
                case 1: draft++; break;
                case 2: countersign++; break;
                case 3: finalize++; break;
                case 4: approve++; break;
                case 5: sign++; break;
            }
            if (c.getAmount() > 0) totalAmount += c.getAmount();
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("draft", draft);
        stats.put("countersign", countersign);
        stats.put("finalize", finalize);
        stats.put("approve", approve);
        stats.put("sign", sign);
        stats.put("totalAmount", totalAmount);
        result.put("success", true);
        result.put("data", stats);
        return result;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
