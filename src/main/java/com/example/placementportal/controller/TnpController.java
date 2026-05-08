package com.example.placementportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TnpController {

    @GetMapping("/dashboard")
    public String tnpDashboard() {
     //   return "tnp_dashboard";  // name of HTML file (no .html needed)
        return "redirect:/tnp_dashboard.html";

    }
}

