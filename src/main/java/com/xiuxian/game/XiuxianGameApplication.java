package com.xiuxian.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.xiuxian.game")
@EnableJpaRepositories(basePackages = "com.xiuxian.game.repository")
@EntityScan(basePackages = "com.xiuxian.game.entity")
@EnableScheduling
public class XiuxianGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiuxianGameApplication.class, args);
    }
}