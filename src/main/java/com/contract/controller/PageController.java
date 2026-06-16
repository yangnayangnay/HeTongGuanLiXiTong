package com.contract.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {
    @GetMapping("/")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping("/main")
    public String main() { return "main"; }

    @GetMapping("/draft")
    public String draft() { return "draft"; }

    @GetMapping("/countersign")
    public String countersign() { return "countersign"; }

    @GetMapping("/finalize")
    public String finalizePage() { return "finalize"; }

    @GetMapping("/approve")
    public String approve() { return "approve"; }

    @GetMapping("/sign")
    public String sign() { return "sign"; }

    @GetMapping("/assign")
    public String assign() { return "assign"; }

    @GetMapping("/query")
    public String query() { return "query"; }

    @GetMapping("/flow")
    public String flow() { return "flow"; }

    @GetMapping("/customer")
    public String customer() { return "customer"; }

    @GetMapping("/user")
    public String user() { return "user"; }

    @GetMapping("/role")
    public String role() { return "role"; }

    @GetMapping("/log")
    public String log() { return "log"; }

    @GetMapping("/pending")
    public String pending() { return "pending"; }
}
