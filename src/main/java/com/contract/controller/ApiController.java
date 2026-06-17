package com.contract.controller;

import com.contract.entity.*;
import com.contract.service.*;
import com.contract.util.ChunkUploadManager;
import com.contract.util.FileLogger;
import com.contract.util.FileUploadUtil;

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

    private static final java.util.concurrent.ConcurrentHashMap<String, ChunkUploadManager> uploadSessions = new java.util.concurrent.ConcurrentHashMap<>();

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
    public Map<String, Object> getAllUsers(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F10")) {
            result.put("success", false);
            result.put("message", "您没有用户管理权限");
            return result;
        }
        List<User> users = userService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", users);
        return result;
    }

    @GetMapping("/users/pending")
    public Map<String, Object> getPendingUsers(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F10")) {
            result.put("success", false);
            result.put("message", "您没有用户管理权限");
            return result;
        }
        List<User> users = userService.findPendingUsers();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", users);
        return result;
    }

    @PostMapping("/users/{id}/approve")
    public Map<String, Object> approveUser(@PathVariable int id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F10")) {
            result.put("success", false);
            result.put("message", "您没有用户管理权限");
            return result;
        }
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
    public Map<String, Object> rejectUser(@PathVariable int id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F10")) {
            result.put("success", false);
            result.put("message", "您没有用户管理权限");
            return result;
        }
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
    public Map<String, Object> addUser(@RequestBody User user, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F10")) {
            result.put("success", false);
            result.put("message", "您没有用户管理权限");
            return result;
        }
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
    public Map<String, Object> updateUser(@PathVariable int id, @RequestBody User user, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F10")) {
            result.put("success", false);
            result.put("message", "您没有用户管理权限");
            return result;
        }
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
    public Map<String, Object> deleteUser(@PathVariable int id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F10")) {
            result.put("success", false);
            result.put("message", "您没有用户管理权限");
            return result;
        }
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
    public Map<String, Object> getUnassignedContracts(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F06")) {
            result.put("success", false);
            result.put("message", "您没有分配合同权限");
            return result;
        }
        List<Contract> contracts = contractService.getUnassignedContracts();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", contracts);
        return result;
    }

    /**
     * 获取可分配的用户列表（按功能权限过滤）
     * 返回会签人员(F02)、审批人员(F04)、签订人员(F05)
     */
    @GetMapping("/contracts/assignable-users")
    public Map<String, Object> getAssignableUsers(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        List<User> allUsers = userService.findAll();
        List<Map<String, Object>> countersignUsers = filterUsersByFunction(allUsers, "F02");
        List<Map<String, Object>> approveUsers = filterUsersByFunction(allUsers, "F04");
        List<Map<String, Object>> signUsers = filterUsersByFunction(allUsers, "F05");
        Map<String, Object> data = new HashMap<>();
        data.put("countersignUsers", countersignUsers);
        data.put("approveUsers", approveUsers);
        data.put("signUsers", signUsers);
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    /**
     * 根据功能编号过滤用户列表（只返回拥有该功能权限的用户）
     */
    private List<Map<String, Object>> filterUsersByFunction(List<User> users, String functionNum) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (User user : users) {
            List<Right> rights = userService.getUserRoles(user.getName());
            boolean hasFunc = false;
            for (Right right : rights) {
                Role role = roleService.findByName(right.getRoleName());
                if (role != null && role.getFunctions() != null) {
                    String[] funcs = role.getFunctions().split(",");
                    for (String func : funcs) {
                        if (func.trim().equals(functionNum)) {
                            hasFunc = true;
                            break;
                        }
                    }
                }
                if (hasFunc) break;
            }
            if (hasFunc) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", user.getName());
                filtered.add(item);
            }
        }
        return filtered;
    }

    /**
     * 检查当前登录用户是否拥有指定功能权限
     */
    private boolean hasPermission(HttpSession session, String functionNum) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return false;
        List<Right> rights = userService.getUserRoles(currentUser.getName());
        for (Right right : rights) {
            Role role = roleService.findByName(right.getRoleName());
            if (role != null && role.getFunctions() != null) {
                String[] funcs = role.getFunctions().split(",");
                for (String func : funcs) {
                    if (func.trim().equals(functionNum)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查指定用户是否拥有指定功能权限
     */
    private boolean userHasPermission(String userName, String functionNum) {
        List<Right> rights = userService.getUserRoles(userName);
        for (Right right : rights) {
            Role role = roleService.findByName(right.getRoleName());
            if (role != null && role.getFunctions() != null) {
                String[] funcs = role.getFunctions().split(",");
                for (String func : funcs) {
                    if (func.trim().equals(functionNum)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
    public Map<String, Object> getFinalizeContracts(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        List<Contract> all = contractService.findAll();
        Map<String, Integer> stateMap = contractService.getAllContractStateTypes();
        List<Contract> filtered = new ArrayList<>();
        for (Contract c : all) {
            int st = stateMap.getOrDefault(c.getNum(), 0);
            c.setStateType(st);
            if (st == 2) {
                filtered.add(c);
            }
        }
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", filtered);
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
                                               @RequestParam(required = false) Integer state,
                                               @RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        List<Contract> contracts;
        if (name != null && !name.isEmpty()) {
            contracts = contractService.findByName(name);
            List<Contract> byNum = new ArrayList<>();
            List<Contract> allContracts = contractService.findAll();
            for (Contract c : allContracts) {
                if (c.getNum() != null && c.getNum().toLowerCase().contains(name.toLowerCase())) {
                    boolean alreadyIn = false;
                    for (Contract existing : contracts) {
                        if (existing.getNum() != null && existing.getNum().equals(c.getNum())) {
                            alreadyIn = true;
                            break;
                        }
                    }
                    if (!alreadyIn) byNum.add(c);
                }
            }
            contracts.addAll(byNum);
        } else {
            contracts = contractService.findAll();
        }
        Map<String, Integer> stateMap = contractService.getAllContractStateTypes();
        if (state != null && state > 0) {
            contracts.removeIf(c -> stateMap.getOrDefault(c.getNum(), 0) != state);
        }
        if (startDate != null && !startDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date start = sdf.parse(startDate);
                contracts.removeIf(c -> c.getBeginTime() == null || c.getBeginTime().before(start));
            } catch (Exception e) {
            }
        }
        if (endDate != null && !endDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date end = sdf.parse(endDate);
                contracts.removeIf(c -> c.getEndTime() == null || c.getEndTime().after(end));
            } catch (Exception e) {
            }
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Contract c : contracts) {
            int st = stateMap.getOrDefault(c.getNum(), 0);
            c.setStateType(st);
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("num", c.getNum());
            item.put("name", c.getName());
            item.put("customer", c.getCustomer());
            item.put("beginTime", c.getBeginTime());
            item.put("endTime", c.getEndTime());
            item.put("userName", c.getUserName());
            item.put("amount", c.getAmount());
            item.put("stateType", st);
            item.put("stateName", contractService.getContractStateName(c.getNum()));
            List<com.contract.entity.ContractProcess> processes = contractService.getContractProcesses(c.getNum());
            StringBuilder countersignUsers = new StringBuilder();
            StringBuilder approveUsers = new StringBuilder();
            StringBuilder signUsers = new StringBuilder();
            String finalizer = "";
            if (processes != null) {
                for (com.contract.entity.ContractProcess p : processes) {
                    String status = p.getState() == 1 ? "✓" : "待";
                    if (p.getType() == 1) {
                        if (countersignUsers.length() > 0) countersignUsers.append(", ");
                        countersignUsers.append(p.getUserName()).append(status);
                    } else if (p.getType() == 2) {
                        if (approveUsers.length() > 0) approveUsers.append(", ");
                        approveUsers.append(p.getUserName()).append(status);
                    } else if (p.getType() == 3) {
                        if (signUsers.length() > 0) signUsers.append(", ");
                        signUsers.append(p.getUserName()).append(status);
                    }
                }
            }
            if (st >= 3) {
                List<com.contract.entity.ContractProcess> finalizers = contractService.getContractProcessesByType(c.getNum(), 1);
                for (com.contract.entity.ContractProcess p : finalizers) {
                    if (p.getState() == 1 && p.getContent() != null && p.getContent().contains("定稿")) {
                        finalizer = p.getUserName();
                        break;
                    }
                }
                if (finalizer.isEmpty() && !processes.isEmpty()) {
                    finalizer = c.getUserName();
                }
            }
            item.put("countersignUsers", countersignUsers.toString());
            item.put("approveUsers", approveUsers.toString());
            item.put("signUsers", signUsers.toString());
            item.put("finalizer", finalizer);
            dataList.add(item);
        }
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", dataList);
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
    public Map<String, Object> getAllRoles(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F11")) {
            result.put("success", false);
            result.put("message", "您没有角色管理权限");
            return result;
        }
        List<Role> roles = roleService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", roles);
        return result;
    }

    @PostMapping("/roles")
    public Map<String, Object> addRole(@RequestBody Role role, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F11")) {
            result.put("success", false);
            result.put("message", "您没有角色管理权限");
            return result;
        }
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
    public Map<String, Object> updateRole(@PathVariable int id, @RequestBody Role role, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F11")) {
            result.put("success", false);
            result.put("message", "您没有角色管理权限");
            return result;
        }
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
    public Map<String, Object> deleteRole(@PathVariable int id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F11")) {
            result.put("success", false);
            result.put("message", "您没有角色管理权限");
            return result;
        }
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
    public Map<String, Object> getAllCustomers(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        List<Customer> customers = customerService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", customers);
        return result;
    }

    @PostMapping("/customers")
    public Map<String, Object> addCustomer(@RequestBody Customer customer, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F09")) {
            result.put("success", false);
            result.put("message", "您没有客户管理权限");
            return result;
        }
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
    public Map<String, Object> updateCustomer(@PathVariable int id, @RequestBody Customer customer, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F09")) {
            result.put("success", false);
            result.put("message", "您没有客户管理权限");
            return result;
        }
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
    public Map<String, Object> deleteCustomer(@PathVariable int id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F09")) {
            result.put("success", false);
            result.put("message", "您没有客户管理权限");
            return result;
        }
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
    public Map<String, Object> getAllLogs(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F12")) {
            result.put("success", false);
            result.put("message", "您没有日志查看权限");
            return result;
        }
        List<Log> logs = logService.findAll();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", logs);
        return result;
    }

    // ==================== 文件相关 ====================

    @PostMapping("/files/upload")
    public Map<String, Object> uploadFile(@RequestParam String contractNum,
                                          @RequestParam MultipartFile file,
                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        // 添加登录验证
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return result;
            }
            // 文件类型校验
            if (!FileUploadUtil.isAllowedFileType(file.getOriginalFilename())) {
                result.put("success", false);
                result.put("message", "不支持的文件类型，仅允许: pdf, docx, doc, jpg, jpeg, png, bmp, gif, txt");
                return result;
            }

            ContractAttachment attachment = new ContractAttachment();
            attachment.setConNum(contractNum);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setType(FileUploadUtil.getFileExtension(file.getOriginalFilename()));
            attachment.setUploadTime(new Date());
            attachment.setFileData(file.getBytes());  // 直接存BLOB
            attachment.setPath("");  // 不再使用文件系统路径

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
        String email = body.getOrDefault("email", "18873604101@163.com");
        boolean ok = userService.register(name, password, email);
        result.put("success", ok);
        result.put("message", ok ? "添加成功" : "添加失败");
        return result;
    }

    @PostMapping("/user/update")
    public Map<String, Object> updateUserJson(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        int id = Integer.parseInt(body.getOrDefault("id", "0"));
        // 先获取原有用户信息
        User existing = userService.findById(id);
        if (existing == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        User user = new User();
        user.setId(id);
        user.setName(body.getOrDefault("name", existing.getName()));
        user.setPassword(body.getOrDefault("password", existing.getPassword()));
        user.setStatus(existing.getStatus());
        user.setEmail(body.getOrDefault("email", existing.getEmail()));
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
        role.setFunctions(body.getOrDefault("functions", ""));
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
        role.setFunctions(body.getOrDefault("functions", ""));
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
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String startDate,
                                                    @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> flowList = new ArrayList<>();
        List<Contract> contracts = contractService.findAll();
        for (Contract c : contracts) {
            int stateType = contractService.getContractStateType(c.getNum());
            // 按状态筛选
            if (state != null && !state.isEmpty()) {
                try {
                    int filterState = Integer.parseInt(state);
                    if (stateType != filterState) continue;
                } catch (NumberFormatException e) { /* ignore */ }
            }
            // 按关键词筛选
            if (keyword != null && !keyword.isEmpty()) {
                if (!c.getNum().toLowerCase().contains(keyword.toLowerCase()) &&
                    (c.getName() == null || !c.getName().toLowerCase().contains(keyword.toLowerCase()))) {
                    continue;
                }
            }
            // 按时间范围筛选
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date start = sdf.parse(startDate);
                    if (c.getBeginTime() == null || c.getBeginTime().before(start)) continue;
                } catch (Exception e) { /* ignore */ }
            }
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date end = sdf.parse(endDate);
                    if (c.getEndTime() == null || c.getEndTime().after(end)) continue;
                } catch (Exception e) { /* ignore */ }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("conNum", c.getNum());
            item.put("contractName", c.getName());
            item.put("userName", c.getUserName());
            item.put("stateType", stateType);
            List<com.contract.entity.ContractState> states = contractService.getContractStates(c.getNum());
            if (states != null && !states.isEmpty()) {
                item.put("time", states.get(states.size() - 1).getTime());
            }
            List<com.contract.entity.ContractProcess> processes = contractService.getContractProcesses(c.getNum());
            StringBuilder countersignUsers = new StringBuilder();
            StringBuilder approveUsers = new StringBuilder();
            StringBuilder signUsers = new StringBuilder();
            String finalizer = "";
            if (processes != null) {
                for (com.contract.entity.ContractProcess p : processes) {
                    String status = p.getState() == 1 ? "✓" : "待";
                    if (p.getType() == 1) {
                        if (countersignUsers.length() > 0) countersignUsers.append(", ");
                        countersignUsers.append(p.getUserName()).append(status);
                    } else if (p.getType() == 2) {
                        if (approveUsers.length() > 0) approveUsers.append(", ");
                        approveUsers.append(p.getUserName()).append(status);
                    } else if (p.getType() == 3) {
                        if (signUsers.length() > 0) signUsers.append(", ");
                        signUsers.append(p.getUserName()).append(status);
                    }
                }
            }
            if (stateType >= 3) {
                List<com.contract.entity.ContractProcess> finalizers = contractService.getContractProcessesByType(c.getNum(), 1);
                for (com.contract.entity.ContractProcess p : finalizers) {
                    if (p.getState() == 1 && p.getContent() != null && p.getContent().contains("定稿")) {
                        finalizer = p.getUserName();
                        break;
                    }
                }
                if (finalizer.isEmpty() && !processes.isEmpty()) {
                    finalizer = c.getUserName();
                }
            }
            item.put("countersignUsers", countersignUsers.toString());
            item.put("approveUsers", approveUsers.toString());
            item.put("signUsers", signUsers.toString());
            item.put("finalizer", finalizer);
            flowList.add(item);
        }
        result.put("success", true);
        result.put("data", flowList);
        return result;
    }

    @PostMapping("/contract/assign")
    public Map<String, Object> assignContractJson(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F06")) {
            result.put("success", false);
            result.put("message", "您没有分配合同权限");
            return result;
        }
        String conNum = (String) body.get("contractNum");
        @SuppressWarnings("unchecked")
        List<String> countersignUsers = (List<String>) body.getOrDefault("countersignUsers", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> approveUsers = (List<String>) body.getOrDefault("approveUsers", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> signUsers = (List<String>) body.getOrDefault("signUsers", new ArrayList<>());
        // 验证被分配用户是否拥有对应权限
        for (String u : countersignUsers) {
            if (!userHasPermission(u, "F02")) {
                result.put("success", false);
                result.put("message", "用户 " + u + " 没有会签权限，无法分配会签任务");
                return result;
            }
        }
        for (String u : approveUsers) {
            if (!userHasPermission(u, "F04")) {
                result.put("success", false);
                result.put("message", "用户 " + u + " 没有审批权限，无法分配审批任务");
                return result;
            }
        }
        for (String u : signUsers) {
            if (!userHasPermission(u, "F05")) {
                result.put("success", false);
                result.put("message", "用户 " + u + " 没有签订权限，无法分配签订任务");
                return result;
            }
        }
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
        if (!hasPermission(session, "F02")) {
            result.put("success", false);
            result.put("message", "您没有会签权限");
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
        if (contractNum == null || contractNum.isEmpty()) {
            result.put("success", false);
            result.put("message", "合同编号不能为空");
            return result;
        }
        Contract contractToFinalize = contractService.findByNum(contractNum);
        if (contractToFinalize == null || !contractToFinalize.getUserName().equals(currentUser.getName())) {
            result.put("success", false);
            result.put("message", "只有合同起草人才能定稿");
            return result;
        }
        int stateType = contractService.getContractStateType(contractNum);
        if (stateType == 3 || stateType == 4 || stateType == 5) {
            result.put("success", true);
            result.put("message", "该合同已定稿完成，无需重复操作");
            return result;
        }
        if (stateType != 2) {
            String stateName = getStateTypeName(stateType);
            result.put("success", false);
            result.put("message", "定稿失败：合同当前状态为「" + stateName + "」，需要「会签完成」状态才能定稿。请确认所有会签人员已完成会签。");
            return result;
        }
        boolean ok = contractService.finalizeContract(contractNum, content, userName);
        result.put("success", ok);
        result.put("message", ok ? "定稿成功" : "定稿失败，请稍后重试");
        return result;
    }

    private String getStateTypeName(int type) {
        switch (type) {
            case 0: return "无状态";
            case 1: return "起草/已分配";
            case 2: return "会签完成";
            case 3: return "定稿完成";
            case 4: return "审批完成";
            case 5: return "签订完成";
            default: return "未知(" + type + ")";
        }
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
        if (!hasPermission(session, "F04")) {
            result.put("success", false);
            result.put("message", "您没有审批权限");
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
    public Map<String, Object> signContractJson(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        if (!hasPermission(session, "F05")) {
            result.put("success", false);
            result.put("message", "您没有签订权限");
            return result;
        }
        String contractNum = (String) body.get("contractNum");
        String info = (String) body.getOrDefault("content", "签订确认");
        String party = (String) body.getOrDefault("party", "");
        String signatureImage = (String) body.getOrDefault("signatureImage", "");
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
        if (ok && !party.isEmpty()) {
            String signInfo = info;
            if (signatureImage != null && !signatureImage.isEmpty()) {
                signInfo = "[签名图片]\n" + signatureImage;
            }
            contractService.insertSignatureToContract(contractNum, party, currentUser.getName(), signInfo);
        }
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
        // 返回所有类型的流程意见（会签+审批+签订），按时间排序
        List<ContractProcess> processes = contractService.getContractProcesses(num);
        String[] typeNames = {"", "会签", "审批", "签订"};
        String[] stateLabels = {"待处理", "已完成", "已否决"};
        List<Map<String, Object>> opinions = new ArrayList<>();
        for (ContractProcess p : processes) {
            if (p.getContent() == null || p.getContent().isEmpty()) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("userName", p.getUserName());
            item.put("type", p.getType());
            item.put("typeName", p.getType() >= 1 && p.getType() <= 3 ? typeNames[p.getType()] : "未知");
            item.put("state", p.getState());
            item.put("stateName", p.getState() >= 0 && p.getState() <= 2 ? stateLabels[p.getState()] : "未知");
            item.put("content", p.getContent());
            item.put("time", p.getTime());
            opinions.add(item);
        }
        result.put("success", true);
        result.put("data", opinions);
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

    @PostMapping("/settings/testEmail")
    public Map<String, Object> testEmail(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String toEmail = body.get("toEmail");
        if (toEmail == null || toEmail.isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供收件人邮箱");
            return result;
        }
        boolean ok = com.contract.util.EmailService.sendTaskNotification(
            toEmail, "测试用户", "TEST-001", "测试合同", "测试任务");
        if (ok) {
            result.put("success", true);
            result.put("message", "测试邮件已发送");
        } else {
            result.put("success", false);
            result.put("message", "邮件发送失败，请检查SMTP配置和授权码是否正确");
        }
        return result;
    }

    // ---------- 邮件设置相关 ----------

    @PostMapping("/settings/email")
    public Map<String, Object> saveEmailSettings(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        try {
            String host = body.get("host");
            int port = Integer.parseInt(body.getOrDefault("port", "465"));
            String email = body.get("email");
            String password = body.get("password");
            com.contract.util.EmailService.configure(host, port, email, password);
            // 持久化到数据库
            saveSetting("smtp_host", host);
            saveSetting("smtp_port", String.valueOf(port));
            saveSetting("smtp_email", email);
            saveSetting("smtp_password", password);
            result.put("success", true);
            result.put("message", "邮件配置已保存");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "配置保存失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 保存设置项到数据库（存在则更新，不存在则插入）
     */
    private void saveSetting(String key, String value) {
        try (java.sql.Connection conn = com.contract.util.DBUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "MERGE INTO t_settings t USING (SELECT ? AS key_name FROM dual) s ON (t.key_name = s.key_name) WHEN MATCHED THEN UPDATE SET key_value = ? WHEN NOT MATCHED THEN INSERT (key_name, key_value) VALUES (?, ?)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, key);
            ps.setString(4, value);
            ps.executeUpdate();
        } catch (Exception e) {
            FileLogger.error("ApiController", "saveSetting", "保存设置失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/settings")
    public Map<String, Object> getSettings(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        try (java.sql.Connection conn = com.contract.util.DBUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT key_name, key_value FROM t_settings");
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("key_name"), rs.getString("key_value"));
            }
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @PostMapping("/settings/ai")
    public Map<String, Object> saveAISettings(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        try {
            String url = body.getOrDefault("url", "http://localhost:11434");
            String model = body.getOrDefault("model", "qwen2:7b");
            com.contract.util.AIAssistantService.configure(url, model);
            result.put("success", true);
            result.put("message", "AI配置已保存");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "配置保存失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * AI合同审查
     */
    @PostMapping("/ai/review")
    public Map<String, Object> aiReviewContract(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String content = body.getOrDefault("content", "");
            if (content.isEmpty()) {
                result.put("success", false);
                result.put("message", "合同内容为空");
                return result;
            }
            String reviewResult = com.contract.util.AIAssistantService.reviewContract(content);
            result.put("success", true);
            result.put("data", reviewResult);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "AI审查失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * AI自动填入合同信息
     */
    @PostMapping("/ai/fillContract")
    public Map<String, Object> aiFillContract(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            String contractName = body.getOrDefault("name", "");
            String customerName = body.getOrDefault("customer", "");
            String userName = (String) session.getAttribute("userName");
            String draft = com.contract.util.AIAssistantService.generateContractDraft(contractName, customerName, userName);
            if (draft != null && !draft.isEmpty()) {
                result.put("success", true);
                result.put("data", draft);
            } else {
                result.put("success", false);
                result.put("message", "AI服务不可用或生成失败，请确认Ollama已启动");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "AI填入失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 合同内容下载/上传/签名 ====================

    @PostMapping("/contract/downloadContent")
    public ResponseEntity<byte[]> downloadContractContent(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        String contractName = body.getOrDefault("name", "合同");
        String format = body.getOrDefault("format", "pdf");

        if ("pdf".equalsIgnoreCase(format)) {
            return exportAsPdf(content, contractName);
        }
        // 默认txt格式
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

    /**
     * 导出合同为PDF格式（支持中文和签名图片）
     */
    private ResponseEntity<byte[]> exportAsPdf(String content, String contractName) {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();

            // 使用中文字体
            com.itextpdf.text.pdf.BaseFont bfChinese = com.itextpdf.text.pdf.BaseFont.createFont(
                "STSong-Light", "UniGB-UCS2-H", com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            com.itextpdf.text.Font fontChinese = new com.itextpdf.text.Font(bfChinese, 12, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fontTitle = new com.itextpdf.text.Font(bfChinese, 18, com.itextpdf.text.Font.BOLD);

            // 标题
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(contractName, fontTitle);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 解析内容：逐行处理，识别<img>标签嵌入图片
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    document.add(new com.itextpdf.text.Paragraph(" ", fontChinese));
                    continue;
                }
                // 处理包含<img>标签的行（签名图片）
                if (line.contains("<img") && line.contains("src=")) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("src=\"([^\"]+)\"").matcher(line);
                    if (m.find()) {
                        String src = m.group(1);
                        try {
                            if (src.startsWith("data:image")) {
                                // base64编码的图片
                                String[] parts = src.split(",", 2);
                                if (parts.length == 2) {
                                    byte[] imgBytes = java.util.Base64.getDecoder().decode(parts[1]);
                                    com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(imgBytes);
                                    img.scaleToFit(150, 60);
                                    document.add(img);
                                }
                            } else if (src.startsWith("http")) {
                                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(new java.net.URL(src));
                                img.scaleToFit(150, 60);
                                document.add(img);
                            }
                        } catch (Exception imgEx) {
                            document.add(new com.itextpdf.text.Paragraph("[签名图片]", fontChinese));
                        }
                    }
                } else if (line.contains("[电子签名]")) {
                    // 跳过旧格式标记
                    continue;
                } else if (line.contains("[/电子签名]")) {
                    continue;
                } else {
                    // 普通文本行
                    com.itextpdf.text.Paragraph p = new com.itextpdf.text.Paragraph(line, fontChinese);
                    p.setSpacingAfter(4);
                    document.add(p);
                }
            }

            document.close();
            byte[] pdfBytes = baos.toByteArray();

            String fileName = contractName + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            FileLogger.error("ApiController", "exportAsPdf", "PDF导出失败: " + e.getMessage());
            byte[] fallback = ("PDF导出失败，已转为文本格式\n\n" + content).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", contractName + ".txt");
            return new ResponseEntity<>(fallback, headers, HttpStatus.OK);
        }
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

    /**
     * 分块上传合同文件（支持断点续传）
     */
    @PostMapping("/contract/uploadChunk")
    public Map<String, Object> uploadChunk(@RequestParam("file") MultipartFile file,
                                            @RequestParam("chunkIndex") int chunkIndex,
                                            @RequestParam("totalChunks") int totalChunks,
                                            @RequestParam("uploadId") String uploadId,
                                            @RequestParam("fileName") String fileName,
                                            @RequestParam("contractNum") String contractNum,
                                            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        try {
            // 获取或创建上传会话
            ChunkUploadManager manager = uploadSessions.computeIfAbsent(uploadId,
                id -> new ChunkUploadManager(fileName, totalChunks, 0));

            // 检查该分块是否已上传（断点续传）
            if (manager.isChunkUploaded(chunkIndex)) {
                result.put("success", true);
                result.put("message", "分块已存在，跳过");
                result.put("skipped", true);
                return result;
            }

            String uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "contract_upload" + File.separator + uploadId;
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            File chunkFile = new File(dir, chunkIndex + ".part");
            file.transferTo(chunkFile);

            // 标记该分块已上传
            manager.markChunkUploaded(chunkIndex);

            result.put("success", true);
            result.put("message", "分块上传成功");
            result.put("progress", manager.getProgress());
            FileLogger.info("ApiController", "uploadChunk", "分块上传: " + fileName + " 块" + chunkIndex + "/" + totalChunks + " 进度:" + manager.getProgress() + "%");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "分块上传失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 合并分块上传的文件
     */
    @PostMapping("/contract/mergeChunks")
    public Map<String, Object> mergeChunks(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String uploadId = (String) body.get("uploadId");
            String fileName = (String) body.get("fileName");
            String contractNum = (String) body.get("contractNum");
            int totalChunks = Integer.parseInt(body.getOrDefault("totalChunks", "0").toString());

            String uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "contract_upload" + File.separator + uploadId;
            File dir = new File(uploadDir);

            // 合并文件
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            for (int i = 0; i < totalChunks; i++) {
                File chunkFile = new File(dir, i + ".part");
                if (!chunkFile.exists()) {
                    result.put("success", false);
                    result.put("message", "缺少分块: " + i);
                    return result;
                }
                byte[] chunkBytes = java.nio.file.Files.readAllBytes(chunkFile.toPath());
                baos.write(chunkBytes);
            }
            byte[] mergedBytes = baos.toByteArray();

            // 更新合同附件
            Contract contract = contractService.findByNum(contractNum);
            if (contract != null) {
                contract.setFileData(mergedBytes);
                contract.setFileName(fileName);
                contractService.updateContract(contract);
            }

            // 清理临时文件
            for (int i = 0; i < totalChunks; i++) {
                new File(dir, i + ".part").delete();
            }
            dir.delete();

            uploadSessions.remove(uploadId);

            result.put("success", true);
            result.put("message", "文件合并成功");
            FileLogger.info("ApiController", "mergeChunks", "文件合并完成: " + fileName + ", 大小: " + mergedBytes.length);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "合并失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 查询断点续传进度
     */
    @GetMapping("/contract/uploadProgress")
    public Map<String, Object> getUploadProgress(@RequestParam String uploadId) {
        Map<String, Object> result = new HashMap<>();
        ChunkUploadManager manager = uploadSessions.get(uploadId);
        if (manager != null) {
            result.put("success", true);
            result.put("progress", manager.getProgress());
            result.put("pendingChunks", manager.getPendingChunks());
            result.put("isComplete", manager.isComplete());
        } else {
            result.put("success", false);
            result.put("message", "上传会话不存在");
        }
        return result;
    }

    // ==================== 工具方法 ====================

    @GetMapping("/contract/template")
    public Map<String, Object> getContractTemplate() {
        Map<String, Object> result = new HashMap<>();
        String template = "合同编号：{{合同编号}}\n\n" +
            "合同名称：{{合同名称}}\n\n" +
            "甲方（委托方）：{{甲方}}\n" +
            "乙方（受托方）：{{乙方}}\n\n" +
            "根据《中华人民共和国合同法》及相关法律法规的规定，甲乙双方在平等、自愿、公平、诚实信用的原则基础上，经协商一致，订立本合同。\n\n" +
            "第一条 合同标的\n{{合同名称}}\n\n" +
            "第二条 合同金额\n本合同总金额为人民币__________元整（￥__________）。\n\n" +
            "第三条 付款方式\n__________\n\n" +
            "第四条 履行期限\n本合同自{{开始日期}}起至{{结束日期}}止。\n\n" +
            "第五条 双方权利义务\n1. 甲方权利义务：__________\n2. 乙方权利义务：__________\n\n" +
            "第六条 违约责任\n__________\n\n" +
            "第七条 争议解决\n本合同在履行过程中发生的争议，由双方当事人协商解决；协商不成的，依法向人民法院起诉。\n\n" +
            "第八条 其他约定\n__________\n\n" +
            "甲方（签章）：【甲方签名】\n" +
            "日期：{{开始日期}}\n\n" +
            "乙方（签章）：【乙方签名】\n" +
            "日期：{{结束日期}}";
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
    public Map<String, Object> getStatistics(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        List<Contract> all = contractService.findAll();
        Map<String, Integer> stateMap = contractService.getAllContractStateTypes();
        int total = all.size();
        // 状态映射：stateType表示"已完成到哪一步"，看板/统计应显示"当前进行中"
        // stateType=0: 起草中(未分配), stateType=1: 会签中, stateType=2: 定稿中,
        // stateType=3: 审批中, stateType=4: 签订中, stateType=5: 已完成
        int draft = 0, countersign = 0, finalize = 0, approve = 0, sign = 0, completed = 0;
        double totalAmount = 0;
        for (Contract c : all) {
            int st = stateMap.getOrDefault(c.getNum(), 0);
            switch (st) {
                case 0: draft++; break;
                case 1: countersign++; break;
                case 2: finalize++; break;
                case 3: approve++; break;
                case 4: sign++; break;
                case 5: completed++; break;
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
        stats.put("completed", completed);
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

    /**
     * 获取当前用户的待办任务列表
     */
    @GetMapping("/tasks/pending")
    public Map<String, Object> getPendingTasks(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("data", new ArrayList<>());
            return result;
        }
        List<Map<String, Object>> tasks = new ArrayList<>();
        Map<String, Integer> stateMap = contractService.getAllContractStateTypes();
        List<ContractProcess> csList = contractService.getUserPendingProcesses(currentUser.getName(), 1);
        for (ContractProcess p : csList) {
            if (stateMap.getOrDefault(p.getConNum(), 0) != 1) continue;
            Contract c = contractService.findByNum(p.getConNum());
            if (c == null) continue;
            Map<String, Object> task = new HashMap<>();
            task.put("conNum", p.getConNum());
            task.put("type", 1);
            task.put("contractName", c.getName());
            task.put("customer", c.getCustomer());
            task.put("userName", c.getUserName());
            tasks.add(task);
        }
        List<ContractProcess> apList = contractService.getUserPendingProcesses(currentUser.getName(), 2);
        for (ContractProcess p : apList) {
            if (stateMap.getOrDefault(p.getConNum(), 0) != 3) continue;
            Contract c = contractService.findByNum(p.getConNum());
            if (c == null) continue;
            Map<String, Object> task = new HashMap<>();
            task.put("conNum", p.getConNum());
            task.put("type", 2);
            task.put("contractName", c.getName());
            task.put("customer", c.getCustomer());
            task.put("userName", c.getUserName());
            tasks.add(task);
        }
        List<ContractProcess> sgList = contractService.getUserPendingProcesses(currentUser.getName(), 3);
        for (ContractProcess p : sgList) {
            if (stateMap.getOrDefault(p.getConNum(), 0) != 4) continue;
            Contract c = contractService.findByNum(p.getConNum());
            if (c == null) continue;
            Map<String, Object> task = new HashMap<>();
            task.put("conNum", p.getConNum());
            task.put("type", 3);
            task.put("contractName", c.getName());
            task.put("customer", c.getCustomer());
            task.put("userName", c.getUserName());
            tasks.add(task);
        }
        // 待定稿任务：合同状态为2（会签完成/待定稿）且当前用户是起草人
        List<Contract> allContracts = contractService.findAll();
        for (Contract c : allContracts) {
            int st = stateMap.getOrDefault(c.getNum(), 0);
            if (st == 2 && currentUser.getName().equals(c.getUserName())) {
                Map<String, Object> task = new HashMap<>();
                task.put("conNum", c.getNum());
                task.put("type", 0);
                task.put("contractName", c.getName());
                task.put("customer", c.getCustomer());
                task.put("userName", c.getUserName());
                tasks.add(task);
            }
        }
        // 待审核用户：管理员可看到待审核的新注册用户
        if (hasPermission(session, "F10")) {
            List<User> pendingUsers = userService.findPendingUsers();
            for (User u : pendingUsers) {
                Map<String, Object> task = new HashMap<>();
                task.put("conNum", "");
                task.put("type", 4);
                task.put("contractName", "用户审核: " + u.getName());
                task.put("customer", "");
                task.put("userName", u.getName());
                tasks.add(task);
            }
        }
        result.put("success", true);
        result.put("data", tasks);
        return result;
    }
}
