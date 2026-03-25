package com.xiuxian.game.common.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sentinel 限流熔断配置
 *
 * @author shaun.sheng
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * Sentinel AOP 切面配置
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 初始化限流规�?
     * 注意：生产环境建议从 Nacos 配置中心动态加�?
     */
    public static void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 战斗接口限流 - 每秒100�?
        FlowRule combatRule = createFlowRule("/api/combat/**", 30, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(combatRule);

        // 核心玩家接口限流 - 每秒200�?
        FlowRule playerRule = createFlowRule("/api/player/**", 50, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(playerRule);

        // 技能接口限�?- 每秒150�?
        FlowRule skillRule = createFlowRule("/api/skill/**", 40, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(skillRule);

        // 宠物接口限流 - 每秒150�?
        FlowRule petRule = createFlowRule("/api/pet/**", 40, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(petRule);

        // 修炼接口限流 - 每秒100�?
        FlowRule cultivationRule = createFlowRule("/api/cultivation/**", 30, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(cultivationRule);

        // 社交接口限流 - 每秒100�?
        FlowRule socialRule = createFlowRule("/api/guild/**", 30, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(socialRule);

        // 排行榜接口限�?- 每秒50�?
        FlowRule rankingRule = createFlowRule("/api/ranking/**", 20, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(rankingRule);

        // 拍卖行接口限�?- 每秒80�?
        FlowRule auctionRule = createFlowRule("/api/auction/**", 15, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(auctionRule);

        // 认证接口限流 - 每秒20次（防暴力破解）
        FlowRule authRule = createFlowRule("/api/auth/**", 20, RuleConstant.FLOW_GRADE_QPS);
        rules.add(authRule);

        // 管理后台接口限流 - 每秒100�?
        FlowRule adminRule = createFlowRule("/api/admin/**", 20, RuleConstant.FLOW_GRADE_QPS); // 2H4G optimized
        rules.add(adminRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 限流规则初始化完成，�?{} 条规�?, rules.size());
    }

    /**
     * 初始化系统规�?
     */
    public static void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();

        // CPU 使用率超�?80% 时触发限�?
        SystemRule cpuRule = new SystemRule();
        cpuRule.setGrade(RuleConstant.SYSTEM_LOAD);
        cpuRule.setCount(0.8);
        rules.add(cpuRule);

        // 平均响应时间超过 1000ms 时触发限�?
        SystemRule rtRule = new SystemRule();
        rtRule.setGrade(RuleConstant.AVG_RT);
        rtRule.setCount(1000);
        rules.add(rtRule);

        // 线程数超�?200 时触发限�?
        SystemRule threadRule = new SystemRule();
        threadRule.setGrade(RuleConstant.THREAD_NUM);
        threadRule.setCount(50); // 2H4G optimized
        rules.add(threadRule);

        // QPS 超过 500 时触发限�?
        SystemRule qpsRule = new SystemRule();
        qpsRule.setGrade(RuleConstant.QPS);
        qpsRule.setCount(150); // 2H4G optimized
        rules.add(qpsRule);

        SystemRuleManager.loadRules(rules);
        log.info("Sentinel 系统规则初始化完�?);
    }

    private static FlowRule createFlowRule(String resource, int count, int grade) {
        FlowRule rule = new FlowRule(resource);
        rule.setGrade(grade);
        rule.setCount(count);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rule.setMaxQueueingTimeMs(500);
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        return rule;
    }

    /**
     * 静态初始化�?- 应用启动时加载规�?
     */
    static {
        initFlowRules();
        initSystemRules();
    }
}

