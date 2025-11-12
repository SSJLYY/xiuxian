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
     * 处理根路径访问，返回首页
     * @return 首页HTML文件
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}