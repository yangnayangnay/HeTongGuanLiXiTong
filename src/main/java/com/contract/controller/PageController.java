package com.contract.controller;

import com.contract.entity.*;
import com.contract.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private LogService logService;

    @GetMapping({"/", "/login"})
    public String login() { return "login"; }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping("/main")
    public String main(HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        if (userName == null) {
            return "redirect:/login";
        }
        @SuppressWarnings("unchecked")
        Set<String> functions = (Set<String>) session.getAttribute("functions");
        if (functions == null) {
            functions = userService.getUserFunctions(userName);
            session.setAttribute("functions", functions);
        }
        model.addAttribute("functions", functions);
        return "main";
    }

    @GetMapping("/draft")
    public String draft(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "draft";
    }

    @GetMapping("/countersign")
    public String countersign(HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        List<Contract> contracts = getContractsForUser(userName, 1);
        model.addAttribute("contracts", contracts);
        return "countersign";
    }

    @GetMapping("/finalize")
    public String finalizePage(Model model) {
        List<Contract> all = contractService.findAll();
        Map<String, Integer> stateMap = contractService.getAllContractStateTypes();
        List<Contract> result = all.stream()
            .filter(c -> stateMap.getOrDefault(c.getNum(), 0) == 2)
            .collect(Collectors.toList());
        model.addAttribute("contracts", result);
        return "finalize";
    }

    @GetMapping("/approve")
    public String approve(HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        List<Contract> contracts = getContractsForUser(userName, 2);
        model.addAttribute("contracts", contracts);
        return "approve";
    }

    @GetMapping("/sign")
    public String sign(HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        List<Contract> contracts = getContractsForUser(userName, 3);
        model.addAttribute("contracts", contracts);
        return "sign";
    }

    @GetMapping("/assign")
    public String assign() {
        return "assign";
    }

    @GetMapping("/query")
    public String query() {
        return "query";
    }

    @GetMapping("/flow")
    public String flow(Model model) {
        List<Map<String, Object>> flowList = new ArrayList<>();
        List<Contract> allContracts = contractService.findAll();
        Map<String, Integer> stateMap = contractService.getAllContractStateTypes();
        for (Contract c : allContracts) {
            Map<String, Object> item = new HashMap<>();
            item.put("conNum", c.getNum());
            item.put("contractName", c.getName());
            item.put("userName", c.getUserName());
            int stateType = stateMap.getOrDefault(c.getNum(), 0);
            item.put("stateType", stateType);
            List<com.contract.entity.ContractState> states = contractService.getContractStates(c.getNum());
            if (states != null && !states.isEmpty()) {
                item.put("time", states.get(states.size() - 1).getTime());
            } else {
                item.put("time", null);
            }
            List<com.contract.entity.ContractProcess> processes = contractService.getContractProcesses(c.getNum());
            StringBuilder countersignUsers = new StringBuilder();
            StringBuilder approveUsers = new StringBuilder();
            StringBuilder signUsers = new StringBuilder();
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
            item.put("countersignUsers", countersignUsers.toString());
            item.put("approveUsers", approveUsers.toString());
            item.put("signUsers", signUsers.toString());
            item.put("finalizer", stateType >= 3 ? c.getUserName() : "");
            flowList.add(item);
        }
        model.addAttribute("processes", flowList);
        return "flow";
    }

    @GetMapping("/customer")
    public String customer(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "customer";
    }

    @GetMapping("/user")
    public String user(Model model) {
        List<User> users = userService.findAll();
        for (User u : users) {
            List<Right> rights = userService.getUserRoles(u.getName());
            if (rights != null && !rights.isEmpty()) {
                u.setRoleName(rights.get(0).getRoleName());
            }
        }
        model.addAttribute("users", users);
        model.addAttribute("pendingUsers", userService.findPendingUsers());
        model.addAttribute("roles", roleService.findAll());
        return "user";
    }

    @GetMapping("/role")
    public String role(Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "role";
    }

    @GetMapping("/log")
    public String log(Model model) {
        model.addAttribute("logs", logService.findAll());
        return "log";
    }

    @GetMapping("/pending")
    public String pending() {
        return "pending";
    }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/settings")
    public String settings() { return "settings"; }

    @GetMapping("/statistics")
    public String statistics() { return "statistics"; }

    @GetMapping("/kanban")
    public String kanban() {
        return "kanban";
    }

    private List<Contract> getContractsForUser(String userName, int type) {
        List<ContractProcess> processes = contractService.getUserPendingProcesses(userName, type);
        Map<String, Integer> stateMap = contractService.getAllContractStateTypes();
        List<Contract> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ContractProcess p : processes) {
            if (!seen.contains(p.getConNum())) {
                Contract c = contractService.findByNum(p.getConNum());
                if (c != null) {
                    int stateType = stateMap.getOrDefault(c.getNum(), 0);
                    boolean canShow = false;
                    if (type == 1 && stateType == 1) canShow = true;
                    else if (type == 2 && stateType == 3) canShow = true;
                    else if (type == 3 && stateType == 4) canShow = true;
                    if (canShow) {
                        result.add(c);
                        seen.add(p.getConNum());
                    }
                }
            }
        }
        return result;
    }
}
