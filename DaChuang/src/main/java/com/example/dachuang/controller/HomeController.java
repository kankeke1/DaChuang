package com.example.dachuang.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author mmy
 * @data 2023/10/11 23:07
 * @description
 */
@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "index.html";
    }
    @RequestMapping("/algorithm")
    public String algorithm(){return "";}
}