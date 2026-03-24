/**
 * 战斗反馈视觉系统 (Combat Visual Feedback System)
 * ================================================
 * 技术美术 P0 优先级：即时视觉反馈系统
 * 
 * 功能：
 * 1. 伤害数字飘字 - 金色(我方攻击)、红色(受击)、橙色(暴击)
 * 2. 颜色分级系统 - 根据伤害/效果类型区分
 * 3. 动画序列 - 缩放、淡出、移动
 * 4. 特效强调 - 暴击闪光、治疗绿光
 * 5. 性能优化 - 使用对象池、最大并发数限制
 */

class CombatVisualFeedback {
    constructor() {
        // 颜色方案定义（美术标准）
        this.colorScheme = {
            // 伤害相关
            playerDamage: '#d4af37',      // 金色 - 玩家输出伤害
            playerCritical: '#ff6b35',    // 橙红色 - 玩家暴击
            monsterDamage: '#ef4444',     // 红色 - 玩家受击
            monsterCritical: '#ff1744',   // 深红 - 怪物暴击
            
            // 治疗/状态
            heal: '#10b981',              // 绿色 - 治疗
            shielding: '#3b82f6',         // 蓝色 - 护盾
            buff: '#8b5cf6',              // 紫色 - 增益
            debuff: '#f59e0b',            // 黄色 - 减益
            
            // 特殊效果
            dodge: '#a0a0a0',             // 灰色 - 闪避
            miss: '#a0a0a0',              // 灰色 - 未命中
        };
        
        // 飘字配置
        this.floatingTextConfig = {
            duration: 1200,               // 飘字持续时间(ms)
            horizontalRange: 40,          // 水平偏移范围(px)
            verticalDistance: 80,         // 竖直飘动距离(px)
            maxConcurrent: 20,            // 最大并发飘字数
            fontSize: {
                normal: '24px',
                critical: '36px',
                heal: '24px'
            }
        };
        
        // 对象池
        this.floatingTextPool = [];
        this.activeFloatingTexts = [];
        this.maxPoolSize = 30;
        
        // 特效容器
        this.effectContainer = null;
        this.initEffectContainer();
        
        // 性能指标
        this.stats = {
            totalFloatingTexts: 0,
            activeCount: 0,
            poolSize: 0
        };
    }
    
    /**
     * 初始化特效容器
     */
    initEffectContainer() {
        const container = document.getElementById('combatEffectContainer');
        if (!container) {
            const newContainer = document.createElement('div');
            newContainer.id = 'combatEffectContainer';
            newContainer.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                pointer-events: none;
                z-index: 1000;
            `;
            document.body.appendChild(newContainer);
            this.effectContainer = newContainer;
        } else {
            this.effectContainer = container;
        }
        
        // 添加CSS样式
        this.injectStyles();
    }
    
    /**
     * 注入全局CSS样式
     */
    injectStyles() {
        if (document.getElementById('combatVisualFeedbackStyles')) return;
        
        const style = document.createElement('style');
        style.id = 'combatVisualFeedbackStyles';
        style.textContent = `
            /* 伤害飘字样式 */
            .floating-damage {
                position: fixed;
                font-weight: bold;
                text-shadow: 0 0 8px rgba(0, 0, 0, 0.8);
                pointer-events: none;
                font-family: 'Ma Shan Zheng', 'Noto Serif SC', serif;
                z-index: 1001;
                white-space: nowrap;
                letter-spacing: 1px;
            }
            
            /* 玩家伤害 - 金色 */
            .floating-damage.player-damage {
                color: #d4af37;
                text-shadow: 
                    0 0 8px rgba(212, 175, 55, 0.8),
                    0 0 16px rgba(212, 175, 55, 0.4);
            }
            
            /* 玩家暴击 - 橙红 */
            .floating-damage.player-critical {
                color: #ff6b35;
                text-shadow: 
                    0 0 8px rgba(255, 107, 53, 0.8),
                    0 0 16px rgba(255, 107, 53, 0.4);
                font-size: 36px !important;
            }
            
            /* 受击伤害 - 红色 */
            .floating-damage.monster-damage {
                color: #ef4444;
                text-shadow: 
                    0 0 8px rgba(239, 68, 68, 0.8),
                    0 0 16px rgba(239, 68, 68, 0.4);
            }
            
            /* 怪物暴击 - 深红 */
            .floating-damage.monster-critical {
                color: #ff1744;
                text-shadow: 
                    0 0 8px rgba(255, 23, 68, 0.8),
                    0 0 16px rgba(255, 23, 68, 0.4);
                font-size: 36px !important;
            }
            
            /* 治疗 - 绿色 */
            .floating-damage.heal {
                color: #10b981;
                text-shadow: 
                    0 0 8px rgba(16, 185, 129, 0.8),
                    0 0 16px rgba(16, 185, 129, 0.4);
            }
            
            /* 闪避 - 灰色 */
            .floating-damage.dodge {
                color: #a0a0a0;
                font-size: 20px;
                font-style: italic;
            }
            
            /* 暴击闪光效果 */
            @keyframes criticalFlash {
                0%, 100% { 
                    opacity: 1;
                    transform: scale(1) rotate(0deg);
                }
                50% { 
                    opacity: 0.7;
                    transform: scale(1.1) rotate(5deg);
                }
            }
            
            .floating-damage.critical-flash {
                animation: criticalFlash 0.4s ease-out;
            }
            
            /* 飘字淡出动画 */
            @keyframes floatAndFade {
                0% {
                    opacity: 1;
                    transform: translateX(0) translateY(0);
                }
                100% {
                    opacity: 0;
                    transform: translateX(var(--offset-x)) translateY(var(--offset-y));
                }
            }
            
            .floating-text-animation {
                animation: floatAndFade 1.2s ease-out forwards;
            }
            
            /* 血条变色动画 */
            @keyframes healthDamage {
                0%, 100% { 
                    filter: drop-shadow(0 0 0 rgba(239, 68, 68, 0));
                }
                50% { 
                    filter: drop-shadow(0 0 8px rgba(239, 68, 68, 0.8));
                }
            }
            
            .health-bar-damaged {
                animation: healthDamage 0.3s ease-out;
            }
            
            /* 治疗脉冲 */
            @keyframes healPulse {
                0%, 100% {
                    box-shadow: 0 0 0 rgba(16, 185, 129, 0);
                }
                50% {
                    box-shadow: 0 0 12px rgba(16, 185, 129, 0.6);
                }
            }
            
            .health-bar-healed {
                animation: healPulse 0.4s ease-out;
            }
            
            /* 暴击指示 */
            .critical-indicator {
                position: fixed;
                width: 40px;
                height: 40px;
                pointer-events: none;
                z-index: 999;
            }
            
            @keyframes criticalRing {
                0% {
                    width: 20px;
                    height: 20px;
                    opacity: 1;
                }
                100% {
                    width: 60px;
                    height: 60px;
                    opacity: 0;
                }
            }
            
            .critical-ring {
                position: absolute;
                border: 2px solid #ff6b35;
                border-radius: 50%;
                left: 50%;
                top: 50%;
                transform: translate(-50%, -50%);
                animation: criticalRing 0.6s ease-out;
            }
        `;
        document.head.appendChild(style);
    }
    
    /**
     * 创建伤害飘字
     * @param {number} damage - 伤害值
     * @param {object} position - 位置 {x, y}
     * @param {object} options - 选项 {type, isCritical, source}
     */
    showDamageFloat(damage, position, options = {}) {
        const {
            type = 'damage',           // damage, heal, dodge, miss
            isCritical = false,
            source = 'player',         // player, monster
            duration = this.floatingTextConfig.duration
        } = options;
        
        // 性能检查 - 超过最大并发数时丢弃新飘字
        if (this.activeFloatingTexts.length >= this.floatingTextConfig.maxConcurrent) {
            return;
        }
        
        // 创建飘字元素
        const floatingText = this.createFloatingTextElement(
            damage,
            type,
            isCritical,
            source
        );
        
        // 设置位置
        floatingText.style.left = position.x + 'px';
        floatingText.style.top = position.y + 'px';
        
        // 计算动画偏移
        const offsetX = (Math.random() - 0.5) * this.floatingTextConfig.horizontalRange * 2;
        const offsetY = -this.floatingTextConfig.verticalDistance;
        
        floatingText.style.setProperty('--offset-x', offsetX + 'px');
        floatingText.style.setProperty('--offset-y', offsetY + 'px');
        
        // 添加动画类
        floatingText.classList.add('floating-text-animation');
        
        // 如果是暴击，添加闪光效果
        if (isCritical) {
            floatingText.classList.add('critical-flash');
        }
        
        // 添加到容器
        this.effectContainer.appendChild(floatingText);
        
        // 记录活跃飘字
        const floatingTextObj = {
            element: floatingText,
            startTime: Date.now(),
            duration: duration,
            type: type
        };
        
        this.activeFloatingTexts.push(floatingTextObj);
        this.stats.totalFloatingTexts++;
        this.stats.activeCount = this.activeFloatingTexts.length;
        
        // 自动移除
        setTimeout(() => {
            this.removeFloatingText(floatingTextObj);
        }, duration);
        
        return floatingText;
    }
    
    /**
     * 创建飘字DOM元素
     */
    createFloatingTextElement(damage, type, isCritical, source) {
        const element = document.createElement('div');
        element.className = 'floating-damage';
        
        // 确定文本内容和样式
        let text, className;
        
        switch (type) {
            case 'damage':
                text = damage > 0 ? `-${damage}` : damage.toString();
                className = source === 'player'
                    ? (isCritical ? 'player-critical' : 'player-damage')
                    : (isCritical ? 'monster-critical' : 'monster-damage');
                break;
            case 'heal':
                text = `+${damage}`;
                className = 'heal';
                break;
            case 'dodge':
                text = '闪避';
                className = 'dodge';
                break;
            case 'miss':
                text = '未命中';
                className = 'dodge';
                break;
            default:
                text = damage.toString();
                className = 'player-damage';
        }
        
        element.textContent = text;
        element.classList.add(className);
        
        return element;
    }
    
    /**
     * 移除飘字
     */
    removeFloatingText(floatingTextObj) {
        if (floatingTextObj.element && floatingTextObj.element.parentNode) {
            floatingTextObj.element.remove();
        }
        
        const index = this.activeFloatingTexts.indexOf(floatingTextObj);
        if (index > -1) {
            this.activeFloatingTexts.splice(index, 1);
        }
        
        this.stats.activeCount = this.activeFloatingTexts.length;
    }
    
    /**
     * 显示血条伤害闪烁效果
     */
    showHealthBarDamage(healthBar, isCritical = false) {
        if (!healthBar) return;
        
        // 移除旧动画类
        healthBar.classList.remove('health-bar-damaged', 'health-bar-healed');
        
        // 强制重排以重新启动动画
        void healthBar.offsetWidth;
        
        if (isCritical) {
            healthBar.classList.add('health-bar-damaged');
        }
    }
    
    /**
     * 显示治疗脉冲
     */
    showHealPulse(healthBar) {
        if (!healthBar) return;
        
        healthBar.classList.remove('health-bar-damaged', 'health-bar-healed');
        void healthBar.offsetWidth;
        healthBar.classList.add('health-bar-healed');
    }
    
    /**
     * 显示暴击指示器（圆形脉冲）
     */
    showCriticalIndicator(position) {
        const indicator = document.createElement('div');
        indicator.className = 'critical-indicator';
        indicator.style.left = position.x + 'px';
        indicator.style.top = position.y + 'px';
        
        const ring = document.createElement('div');
        ring.className = 'critical-ring';
        indicator.appendChild(ring);
        
        this.effectContainer.appendChild(indicator);
        
        setTimeout(() => {
            indicator.remove();
        }, 600);
    }
    
    /**
     * 解析战斗日志并生成视觉反馈
     */
    processCombatLog(battleLog, combatResult) {
        // 这个方法会在displayCombatResult中调用
        // 用于自动生成对应的视觉反馈
        
        if (!battleLog || !Array.isArray(battleLog)) return;
        
        battleLog.forEach((logEntry, index) => {
            // 延迟显示，营造逐步反馈效果
            setTimeout(() => {
                this.parseAndVisualize(logEntry, combatResult);
            }, index * 200);
        });
    }
    
    /**
     * 解析日志文本并生成对应视觉效果
     */
    parseAndVisualize(logEntry, combatResult) {
        // 获取战斗目标区域（用于飘字位置）
        const monsterElement = document.getElementById('monsterStats');
        const playerElement = document.getElementById('playerStats');
        
        const monsterPos = monsterElement ? this.getElementCenter(monsterElement) : {x: window.innerWidth * 0.7, y: window.innerHeight * 0.3};
        const playerPos = playerElement ? this.getElementCenter(playerElement) : {x: window.innerWidth * 0.3, y: window.innerHeight * 0.3};
        
        // 解析日志文本
        if (logEntry.includes('暴击')) {
            // 暴击检测
            const damageMatch = logEntry.match(/(\d+)/);
            const damage = damageMatch ? parseInt(damageMatch[1]) : 0;
            const isPlayerAttack = logEntry.includes('玩家') || logEntry.includes('攻击');
            
            const pos = isPlayerAttack ? monsterPos : playerPos;
            this.showDamageFloat(damage, pos, {
                type: 'damage',
                isCritical: true,
                source: isPlayerAttack ? 'player' : 'monster'
            });
            this.showCriticalIndicator(pos);
        } else if (logEntry.includes('造成伤害')) {
            // 普通伤害
            const damageMatch = logEntry.match(/(\d+)/);
            const damage = damageMatch ? parseInt(damageMatch[1]) : 0;
            const isPlayerAttack = logEntry.includes('玩家') || logEntry.includes('攻击');
            
            this.showDamageFloat(damage, isPlayerAttack ? monsterPos : playerPos, {
                type: 'damage',
                isCritical: false,
                source: isPlayerAttack ? 'player' : 'monster'
            });
        } else if (logEntry.includes('恢复') || logEntry.includes('治疗')) {
            // 治疗
            const healMatch = logEntry.match(/(\d+)/);
            const heal = healMatch ? parseInt(healMatch[1]) : 0;
            const isPlayerHeal = logEntry.includes('玩家');
            
            this.showDamageFloat(heal, isPlayerHeal ? playerPos : monsterPos, {
                type: 'heal'
            });
        } else if (logEntry.includes('闪避')) {
            // 闪避
            const isPlayerDodge = logEntry.includes('玩家');
            this.showDamageFloat(0, isPlayerDodge ? playerPos : monsterPos, {
                type: 'dodge'
            });
        }
    }
    
    /**
     * 获取元素中心位置
     */
    getElementCenter(element) {
        const rect = element.getBoundingClientRect();
        return {
            x: rect.left + rect.width / 2,
            y: rect.top + rect.height / 2
        };
    }
    
    /**
     * 获取性能统计
     */
    getStats() {
        return {
            ...this.stats,
            poolSize: this.floatingTextPool.length,
            colorScheme: this.colorScheme
        };
    }
    
    /**
     * 清理所有活跃飘字（用于战斗结束）
     */
    clearAllFloatingTexts() {
        this.activeFloatingTexts.forEach(obj => {
            if (obj.element && obj.element.parentNode) {
                obj.element.remove();
            }
        });
        this.activeFloatingTexts = [];
        this.stats.activeCount = 0;
    }
}

// 全局实例
window.combatVisualFeedback = new CombatVisualFeedback();
