package com.contract.controller;

import com.contract.entity.*;
import com.contract.service.*;
import com.contract.util.FileLogger;

import org.springframework.beans.factory.annotation.Autowired;
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
    public Map<String, Object> queryContracts(@RequestParam(required = false) String name) {
        Map<String, Object> result = new HashMap<>();
        List<Contract> contracts;
        if (name != null && !name.isEmpty()) {
            contracts = contractService.findByName(name);
        } else {
            contracts = contractService.findAll();
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
    public Map<String, Object> downloadFile(@PathVariable String contractNum) {
        Map<String, Object> result = new HashMap<>();
        Contract contract = contractService.findByNum(contractNum);
        if (contract != null && contract.getFileData() != null) {
            result.put("success", true);
            result.put("message", "查询成功");
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("fileName", contract.getFileName());
            fileData.put("fileType", contract.getFileType());
            result.put("data", fileData);
        } else {
            List<ContractAttachment> attachments = contractService.getAttachments(contractNum);
            if (!attachments.isEmpty()) {
                result.put("success", true);
                result.put("message", "查询成功");
                result.put("data", attachments);
            } else {
                result.put("success", false);
                result.put("message", "未找到附件");
            }
        }
        return result;
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

    // ==================== 工具方法 ====================

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
