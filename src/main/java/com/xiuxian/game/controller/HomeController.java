package com.xiuxian.game.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 * 处理网站根路径的访问请求
 */
@Controller
public class HomeController {

    /**
     * 处理根路径访问，返回登录页面
     * @return 登录页面HTML文件
     */
    @GetMapping("/")
    public String index() {
        return "forward:/login.html";
    }
}