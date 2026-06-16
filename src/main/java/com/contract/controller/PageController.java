package com.contract.controller;

import com.contract.entity.*;
import com.contract.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PageController {

    private UserService userService = new UserService();
    private RoleService roleService = new RoleService();
    private CustomerService customerService = new CustomerService();
    private ContractService contractService = new ContractService();
    private LogService logService = new LogService();

    @GetMapping({"/", "/login"})
    public String login() { return "login"; }

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
        List<Contract> result = all.stream()
            .filter(c -> contractService.getContractStateType(c.getNum()) == 2)
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
    public String assign(Model model) {
        model.addAttribute("contracts", contractService.getUnassignedContracts());
        List<User> allUsers = userService.findAll();
        model.addAttribute("countersignUsers", allUsers);
        model.addAttribute("approveUsers", allUsers);
        model.addAttribute("signUsers", allUsers);
        return "assign";
    }

    @GetMapping("/query")
    public String query(Model model) {
        List<Contract> contracts = contractService.findAll();
        for (Contract c : contracts) {
            c.setStateType(contractService.getContractStateType(c.getNum()));
        }
        model.addAttribute("contracts", contracts);
        return "query";
    }

    @GetMapping("/flow")
    public String flow(Model model) {
        List<ContractProcess> allProcesses = new ArrayList<>();
        List<Contract> allContracts = contractService.findAll();
        for (Contract c : allContracts) {
            List<ContractProcess> procs = contractService.getContractProcesses(c.getNum());
            for (ContractProcess p : procs) {
                p.setContractName(c.getName());
            }
            allProcesses.addAll(procs);
        }
        model.addAttribute("processes", allProcesses);
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
    public String pending(HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        if (userName == null) {
            model.addAttribute("countersignTasks", Collections.emptyList());
            model.addAttribute("approveTasks", Collections.emptyList());
            model.addAttribute("signTasks", Collections.emptyList());
            return "pending";
        }
        model.addAttribute("countersignTasks", contractService.getUserPendingProcesses(userName, 1));
        model.addAttribute("approveTasks", contractService.getUserPendingProcesses(userName, 2));
        model.addAttribute("signTasks", contractService.getUserPendingProcesses(userName, 3));
        return "pending";
    }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/settings")
    public String settings() { return "settings"; }

    @GetMapping("/statistics")
    public String statistics() { return "statistics"; }

    @GetMapping("/kanban")
    public String kanban() { return "kanban"; }

    private List<Contract> getContractsForUser(String userName, int type) {
        List<ContractProcess> processes = contractService.getUserPendingProcesses(userName, type);
        List<Contract> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ContractProcess p : processes) {
            if (!seen.contains(p.getConNum())) {
                Contract c = contractService.findByNum(p.getConNum());
                if (c != null) {
                    result.add(c);
                    seen.add(p.getConNum());
                }
            }
        }
        return result;
    }
}
