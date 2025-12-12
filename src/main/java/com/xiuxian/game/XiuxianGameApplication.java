package com.xiuxian.game;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * xiuxian挂机游戏主应用类
 * 
 * <p>这是一个基于Spring Boot的xiuxian主题挂机游戏后端应用。</p>
 * 
 * <p>主要功能模块：</p>
 * <ul>
 *   <li>用户认证系统 - JWT Token认证</li>
 *   <li>玩家管理系统 - 玩家档案、属性、等级</li>
 *   <li>修炼系统 - 挂机修炼、经验获取、境界突破</li>
 *   <li>战斗系统 - PVE战斗、怪物生成、奖励计算</li>
 *   <li>装备系统 - 装备管理、属性加成、强化</li>
 *   <li>技能系统 - 技能学习、升级、使用</li>
 *   <li>宠物系统 - 宠物获取、培养、战斗</li>
 *   <li>任务系统 - 每日/每周/每月任务</li>
 *   <li>商城系统 - 物品购买、技能购买</li>
 *   <li>背包系统 - 物品管理、使用</li>
 * </ul>
 * 
 * <p>技术栈：</p>
 * <ul>
 *   <li>Java 1.8</li>
 *   <li>Spring Boot 2.7.18</li>
 *   <li>MyBatis-Plus 3.5.3.1</li>
 *   <li>MySQL 8.0</li>
 *   <li>JWT Token认证</li>
 *   <li>Log4j2日志框架</li>
 * </ul>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-11-28
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.xiuxian.game.mapper")
@EnableScheduling
@EnableCaching
public class XiuxianGameApplication {

    /**
     * 应用程序入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        log.info("========================================");
        log.info("xiuxian挂机游戏应用启动中...");
        log.info("========================================");
        
        try {
            // 启动Spring Boot应用
            ConfigurableApplicationContext context = SpringApplication.run(XiuxianGameApplication.class, args);
            
            // 获取环境信息
            Environment env = context.getEnvironment();
            String protocol = "http";
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https";
            }
            
            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String hostAddress = "localhost";
            
            try {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.warn("无法获取本机IP地址，使用localhost: {}", e.getMessage());
            }
            
            // 打印启动成功信息
            log.info("========================================");
            log.info("🎮 xiuxian挂机游戏应用启动成功！");
            log.info("========================================");
            log.info("📊 系统信息:");
            log.info("  Java版本: {}", System.getProperty("java.version"));
            log.info("  Spring Boot版本: {}", env.getProperty("spring.boot.version", "2.7.18"));
            log.info("  活跃配置: {}", String.join(", ", env.getActiveProfiles().length > 0 ? env.getActiveProfiles() : new String[]{"default"}));
            log.info("🌐 访问地址:");
            log.info("  本地访问: {}://localhost:{}{}", protocol, serverPort, contextPath);
            log.info("  外部访问: {}://{}:{}{}", protocol, hostAddress, serverPort, contextPath);
            log.info("🎯 游戏入口:");
            log.info("  🔐 玩家登录: {}://localhost:{}{}/login.html", protocol, serverPort, contextPath);
            log.info("  👑 后台管理: {}://localhost:{}{}/admin.html (包含监控端点)", protocol, serverPort, contextPath);
            log.info("📝 日志文件:");
            log.info("  应用日志: logs/xiuxian-game.log");
            log.info("  错误日志: logs/xiuxian-game-error.log");
            log.info("  SQL日志: logs/xiuxian-game-sql.log");
            log.info("  性能日志: logs/xiuxian-game-performance.log");
            log.info("========================================");
            log.info("🚀 应用已就绪，开始你的xiuxian之旅吧！");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("应用启动失败: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}