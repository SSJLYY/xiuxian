package com.xiuxian.game;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xiuxian.game.mapper")
@EnableScheduling
public class XiuxianGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiuxianGameApplication.class, args);
    }
}