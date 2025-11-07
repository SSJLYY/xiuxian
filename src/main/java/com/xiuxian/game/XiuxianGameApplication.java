package com.xiuxian.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
// import org.springframework.boot.web.servlet.support.SpringBootServletInitializer; // 移除WAR部署支持
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.xiuxian.game")
@EnableScheduling
public class XiuxianGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiuxianGameApplication.class, args);
    }
}