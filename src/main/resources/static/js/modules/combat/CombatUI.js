/**
 * 战斗模块 - UI渲染层
 * 负责战斗界面的渲染和事件绑定
 */
import { combatService } from './CombatService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';

export class CombatUI {
    constructor() {
        this.combatContainer = null;
        this.healthBars = {};
        this.actionButtons = {};
        this.currentTurn = 0;
        this.combatTimer = null;
    }

    /**
     * 初始化战斗UI
     */
    init() {
        this.combatContainer = document.getElementById('combatContainer');
        this.setupElements();
        this.bindEvents();
        this.loadCombatHistory();
    }

    /**
     * 设置DOM元素
     */
    setupElements() {
        this.elements = {
            playerInfo: document.getElementById('playerInfo'),
            monsterInfo: document.getElementById('monsterInfo'),
            combatLog: document.getElementById('combatLog'),
            playerHealthBar: document.getElementById('playerHealthBar'),
            monsterHealthBar: document.getElementById('monsterHealthBar'),
            playerHealthText: document.getElementById('playerHealthText'),
            monsterHealthText: document.getElementById('monsterHealthText'),
            turnIndicator: document.getElementById('turnIndicator'),
            actionButtons: document.querySelectorAll('.combat-action'),
            skillButtons: document.querySelectorAll('.skill-button'),
            itemButtons: document.querySelectorAll('.item-button'),
            fleeButton: document.getElementById('fleeButton')
        };
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        // 攻击按钮
        this.elements.actionButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const action = e.target.dataset.action;
                this.handleAction(action);
            });
        });

        // 技能按钮
        this.elements.skillButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const skillId = parseInt(e.target.dataset.skillId);
                this.handleSkillUse(skillId);
            });
        });

        // 道具按钮
        this.elements.itemButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const itemId = parseInt(e.target.dataset.itemId);
                this.handleItemUse(itemId);
            });
        });

        // 逃跑按钮
        if (this.elements.fleeButton) {
            this.elements.fleeButton.addEventListener('click', () => {
                this.handleFlee();
            });
        }
    }

    /**
     * 处理战斗动作
     */
    async handleAction(action) {
        loading.show();

        try {
            let result;
            if (action === 'attack') {
                result = await combatService.executeAttack();
            } else if (action === 'skill') {
                // 使用默认技能
                result = await combatService.executeAttack(1);
            }

            loading.hide();
            this.updateCombatDisplay(result);
        } catch (error) {
            loading.hide();
            toast.error('动作执行失败');
        }
    }

    /**
     * 处理技能使用
     */
    async handleSkillUse(skillId) {
        loading.show();

        try {
            const result = await combatService.executeAttack(skillId);
            loading.hide();
            this.updateCombatDisplay(result);
        } catch (error) {
            loading.hide();
            toast.error('技能使用失败');
        }
    }

    /**
     * 处理道具使用
     */
    async handleItemUse(itemId) {
        loading.show();

        try {
            const result = await combatService.useItem(itemId);
            loading.hide();
            this.updateCombatDisplay(result);
        } catch (error) {
            loading.hide();
            toast.error('道具使用失败');
        }
    }

    /**
     * 处理逃跑
     */
    async handleFlee() {
        if (!confirm('确定要逃跑吗?')) return;

        loading.show();

        try {
            await combatService.flee();
            loading.hide();
            this.endCombat();
        } catch (error) {
            loading.hide();
            toast.error('逃跑失败');
        }
    }

    /**
     * 更新战斗显示
     */
    updateCombatDisplay(combatData) {
        const combat = combatService.formatCombatData(combatData.combat);

        // 更新血条
        this.updateHealthBar('player', combat.player);
        this.updateHealthBar('monster', combat.monster);

        // 更新回合信息
        this.elements.turnIndicator.textContent = `第 ${combat.turn} 回合`;

        // 添加战斗日志
        this.addCombatLog(combatData.logs || []);

        // 检查战斗是否结束
        if (combat.status === 'victory' || combat.status === 'defeat') {
            this.endCombat(combatData);
        }
    }

    /**
     * 更新血条
     */
    updateHealthBar(type, entity) {
        const bar = type === 'player' ? this.elements.playerHealthBar : this.elements.monsterHealthBar;
        const text = type === 'player' ? this.elements.playerHealthText : this.elements.monsterHealthText;

        if (bar) {
            bar.style.width = `${entity.healthPercent}%`;
            bar.className = `health-bar ${entity.healthPercent < 30 ? 'critical' : entity.healthPercent < 60 ? 'warning' : 'healthy'}`;
        }

        if (text) {
            text.textContent = `${entity.health}/${entity.maxHealth}`;
        }
    }

    /**
     * 添加战斗日志
     */
    addCombatLog(logs) {
        if (!this.elements.combatLog) return;

        logs.forEach(log => {
            const logEntry = document.createElement('div');
            logEntry.className = 'combat-log-entry';
            logEntry.textContent = log;
            this.elements.combatLog.appendChild(logEntry);
        });

        // 滚动到最新日志
        this.elements.combatLog.scrollTop = this.elements.combatLog.scrollHeight;
    }

    /**
     * 结束战斗
     */
    endCombat(combatResult = null) {
        clearInterval(this.combatTimer);

        // 显示结果
        if (combatResult) {
            const rewards = combatService.calculateRewards(combatResult);
            const isVictory = combatResult.combat.status === 'victory';

            if (isVictory) {
                toast.success(`战斗胜利! 获得: ${rewards.text}`);
            } else {
                toast.error('战斗失败!');
            }
        }

        // 禁用操作按钮
        this.elements.actionButtons.forEach(btn => {
            btn.disabled = true;
        });

        // 3秒后返回
        setTimeout(() => {
            window.location.href = '/game.html';
        }, 3000);
    }

    /**
     * 加载战斗历史
     */
    async loadCombatHistory() {
        try {
            const history = await combatService.getCombatHistory();
            this.renderCombatHistory(history);
        } catch (error) {
            console.error('加载战斗历史失败:', error);
        }
    }

    /**
     * 渲染战斗历史
     */
    renderCombatHistory(history) {
        const container = document.getElementById('combatHistory');
        if (!container) return;

        if (history.length === 0) {
            container.innerHTML = '<p>暂无战斗记录</p>';
            return;
        }

        container.innerHTML = history.map(record => `
            <div class="combat-history-item">
                <div class="history-time">${new Date(record.timestamp).toLocaleString()}</div>
                <div class="history-result ${record.result}">${this.translateResult(record.result)}</div>
            </div>
        `).join('');
    }

    /**
     * 翻译战斗结果
     */
    translateResult(result) {
        const resultMap = {
            'victory': '胜利',
            'defeat': '失败',
            'flee': '逃跑'
        };
        return resultMap[result] || result;
    }

    /**
     * 自动刷新战斗状态
     */
    startAutoRefresh(interval = 1000) {
        this.combatTimer = setInterval(async () => {
            try {
                const response = await gameAPI.combat.getStatus();
                if (response.success) {
                    this.updateCombatDisplay(response.data);
                }
            } catch (error) {
                console.error('刷新战斗状态失败:', error);
            }
        }, interval);
    }

    /**
     * 停止自动刷新
     */
    stopAutoRefresh() {
        if (this.combatTimer) {
            clearInterval(this.combatTimer);
            this.combatTimer = null;
        }
    }

    /**
     * 销毁战斗 UI
     */
    destroy() {
        this.stopAutoRefresh();
        this.elements = null;
        this.currentCombat = null;
        this.isInitialized = false;
    }
}

// 导出单例
export const combatUI = new CombatUI();
