package com.roomledger.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Ui_Controller {

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String index(HttpServletRequest request) {
        String path = request.getServletPath();

        // Let Spring handle actual resources
        if (path.startsWith("/assets/") ||
            path.startsWith("/api/") ||
            path.startsWith("/h2-console/") ||
            path.startsWith("/swagger")) {
            return null; // don't intercept
        }

        return "forward:/index.html";
    }
}