/**
 * 技能连招前端系统 + 宠物图鉴系统
 *
 * 技能连招: GDD 6.3 - 烈焰冲击/雷火交织/极速斩杀等连招
 * 对接后端: /api/skills/combos/*
 *
 * 宠物图鉴: GDD 7.4 - 收藏展示+图鉴全局加成
 */

// =====================================================
// 技能连招系统
// =====================================================
class SkillComboSystem {
    constructor() {
        this.availableCombos = [];
        this.lastTriggeredCombo = null;
        this.createUI();
        this.loadCombos();
    }

    async loadCombos() {
        try {
            const res = await gameAPI.getAvailableCombos();
            if (res && res.success) {
                this.availableCombos = res.data || [];
                this.renderComboList();
            }
        } catch (e) {
            // 静默
        }
    }

    // 在战斗中使用技能后触发连招检测
    async onSkillUsed(skillId) {
        try {
            const res = await gameAPI.checkCombo(skillId);
            if (res && res.success && res.data?.triggered) {
                this.showComboTrigger(res.data);
            }
        } catch (e) {
            // 静默
        }
    }

    // 显示连招触发效果
    showComboTrigger(comboData) {
        this.lastTriggeredCombo = comboData;

        // 清除旧的触发效果
        document.querySelectorAll('.combo-trigger-overlay').forEach(el => el.remove());

        const overlay = document.createElement('div');
        overlay.className = 'combo-trigger-overlay';
        overlay.innerHTML = `
            <div class="combo-trigger-inner">
                <div class="combo-trigger-label">COMBO!</div>
                <div class="combo-trigger-name">${comboData.comboName || '连招触发'}</div>
                <div class="combo-trigger-bonus">+${comboData.bonusPercent || 0}% 伤害</div>
            </div>
        `;
        document.body.appendChild(overlay);

        // 2.5秒后移除
        setTimeout(() => overlay.remove(), 2500);
    }

    createUI() {
        // 在战斗界面插入连招面板
        this.injectComboPanelToBattle();
        this.injectStyles();
    }

    injectComboPanelToBattle() {
        // 在 enhanced_combat.html 的战斗区域底部插入
        const tryInject = () => {
            const battleArea = document.getElementById('battleArea') ||
                               document.getElementById('combatSection') ||
                               document.querySelector('.battle-container, .combat-area');
            if (battleArea) {
                if (document.getElementById('comboPanel')) return;
                const panel = document.createElement('div');
                panel.id = 'comboPanel';
                panel.innerHTML = `
                    <div class="combo-panel-header">
                        <span class="combo-panel-icon">⚡</span>
                        <span class="combo-panel-title">可用连招</span>
                        <span class="combo-panel-tip">连续使用指定技能触发</span>
                    </div>
                    <div class="combo-list" id="comboList">
                        <div class="combo-loading">加载中...</div>
                    </div>
                `;
                battleArea.appendChild(panel);
                return true;
            }
            return false;
        };

        if (!tryInject()) {
            const obs = new MutationObserver(() => {
                if (tryInject()) obs.disconnect();
            });
            obs.observe(document.body, { childList: true, subtree: true });
        }
    }

    renderComboList() {
        const list = document.getElementById('comboList');
        if (!list) return;

        if (this.availableCombos.length === 0) {
            list.innerHTML = '<div class="combo-empty">学习更多技能来解锁连招</div>';
            return;
        }

        list.innerHTML = this.availableCombos.map(combo => `
            <div class="combo-item" title="${combo.description || ''}">
                <div class="combo-item-name">⚡ ${combo.name}</div>
                <div class="combo-item-seq">${this.renderSkillSequence(combo.skillSequence)}</div>
                <div class="combo-item-bonus">+${combo.comboBonus}% 伤害</div>
            </div>
        `).join('');
    }

    renderSkillSequence(sequence) {
        // sequence 可能是 JSON 字符串或数组
        try {
            const seq = typeof sequence === 'string' ? JSON.parse(sequence) : sequence;
            return seq.map((id, i) => `
                <span class="combo-seq-item">技能${id}</span>
                ${i < seq.length - 1 ? '<span class="combo-seq-arrow">→</span>' : ''}
            `).join('');
        } catch {
            return String(sequence);
        }
    }

    injectStyles() {
        if (document.getElementById('comboStyles')) return;
        const style = document.createElement('style');
        style.id = 'comboStyles';
        style.textContent = `
            /* 连招触发动画 */
            .combo-trigger-overlay {
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                z-index: 9800;
                text-align: center;
                pointer-events: none;
                animation: comboIn 0.3s ease, comboOut 0.4s ease 2.1s forwards;
            }
            @keyframes comboIn {
                from { opacity: 0; transform: translate(-50%, -50%) scale(0.5); }
                to { opacity: 1; transform: translate(-50%, -50%) scale(1); }
            }
            @keyframes comboOut {
                to { opacity: 0; transform: translate(-50%, -70%) scale(0.8); }
            }
            .combo-trigger-inner {
                background: linear-gradient(135deg, rgba(255,180,0,0.95), rgba(255,100,0,0.95));
                border: 2px solid rgba(255,220,0,0.8);
                border-radius: 16px;
                padding: 20px 32px;
                box-shadow: 0 0 40px rgba(255,160,0,0.6), 0 8px 30px rgba(0,0,0,0.5);
            }
            .combo-trigger-label {
                font-size: 36px;
                font-weight: 900;
                color: #fff;
                letter-spacing: 6px;
                text-shadow: 0 0 20px rgba(255,220,0,0.8), 2px 2px 0 rgba(0,0,0,0.5);
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .combo-trigger-name {
                font-size: 18px;
                color: #fff3cd;
                margin-top: 6px;
                font-weight: bold;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .combo-trigger-bonus {
                font-size: 14px;
                color: #fff;
                margin-top: 4px;
                opacity: 0.9;
                font-family: 'Microsoft YaHei', sans-serif;
            }

            /* 连招面板 */
            #comboPanel {
                background: rgba(10,10,30,0.8);
                border: 1px solid rgba(255,180,0,0.3);
                border-radius: 10px;
                padding: 12px;
                margin-top: 12px;
            }
            .combo-panel-header {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 10px;
            }
            .combo-panel-icon { font-size: 16px; }
            .combo-panel-title {
                font-size: 13px;
                font-weight: bold;
                color: #ffa500;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .combo-panel-tip {
                font-size: 11px;
                color: rgba(255,255,255,0.35);
                margin-left: auto;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .combo-list { display: flex; flex-wrap: wrap; gap: 8px; }
            .combo-item {
                background: rgba(255,180,0,0.05);
                border: 1px solid rgba(255,180,0,0.2);
                border-radius: 8px;
                padding: 8px 12px;
                min-width: 120px;
                transition: border-color 0.2s;
            }
            .combo-item:hover { border-color: rgba(255,180,0,0.5); }
            .combo-item-name { font-size: 12px; color: #ffa500; font-weight: bold; margin-bottom: 4px; font-family: 'Microsoft YaHei', sans-serif; }
            .combo-item-seq { display: flex; align-items: center; gap: 2px; flex-wrap: wrap; margin-bottom: 4px; }
            .combo-seq-item {
                background: rgba(255,180,0,0.1);
                border: 1px solid rgba(255,180,0,0.2);
                border-radius: 4px;
                padding: 2px 6px;
                font-size: 10px;
                color: rgba(255,255,255,0.7);
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .combo-seq-arrow { color: rgba(255,255,255,0.3); font-size: 10px; }
            .combo-item-bonus { font-size: 12px; color: #2ecc71; font-family: 'Microsoft YaHei', sans-serif; }
            .combo-loading, .combo-empty { font-size: 12px; color: rgba(255,255,255,0.3); padding: 8px; font-family: 'Microsoft YaHei', sans-serif; }
        `;
        document.head.appendChild(style);
    }
}

// =====================================================
// 宠物图鉴系统
// GDD 7.4: 收藏5只+10只+全普通+全神话有累积加成
// =====================================================
class PetPokedexSystem {
    constructor() {
        this.pokedexData = null;
        this.createUI();
        this.loadPokedex();
    }

    async loadPokedex() {
        try {
            // 使用现有的宠物API，在客户端聚合图鉴信息
            const myPetsRes = await gameAPI.getMyPets();
            const availRes = await gameAPI.getAvailablePets();

            if (myPetsRes.success && availRes.success) {
                this.buildPokedex(myPetsRes.data || [], availRes.data || []);
            }
        } catch (e) {
            // 静默
        }
    }

    buildPokedex(myPets, allPets) {
        // 构建已收集的宠物ID集合
        const collectedPetIds = new Set(myPets.map(p => p.petId));

        this.pokedexData = {
            total: allPets.length,
            collected: collectedPetIds.size,
            pets: allPets.map(pet => ({
                ...pet,
                isCollected: collectedPetIds.has(pet.id),
                myPetData: myPets.find(p => p.petId === pet.id)
            }))
        };

        this.updatePokedexPanel();
        this.updateBonuses(collectedPetIds.size);
    }

    createUI() {
        // 在pets.html中注入图鉴标签页
        this.injectPokedexTab();
        // 在主界面玩家信息旁注入图鉴加成小标
        this.injectBonusBadge();
        this.injectStyles();
    }

    injectPokedexTab() {
        const tryInject = () => {
            const tabContainer = document.querySelector('.tab-btn')?.parentElement;
            if (tabContainer) {
                if (tabContainer.querySelector('[data-tab-pokedex]')) return true;
                const btn = document.createElement('button');
                btn.className = 'tab-btn';
                btn.setAttribute('data-tab-pokedex', '1');
                btn.textContent = '📚 图鉴';
                btn.onclick = (e) => {
                    // 切换到图鉴
                    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                    btn.classList.add('active');
                    document.querySelectorAll('.tab-content').forEach(t => t.style.display = 'none');
                    const pokedexTab = document.getElementById('pokedexTab');
                    if (pokedexTab) pokedexTab.style.display = 'block';
                };
                tabContainer.appendChild(btn);

                // 创建图鉴内容区
                const content = document.createElement('div');
                content.id = 'pokedexTab';
                content.className = 'tab-content';
                content.style.display = 'none';
                content.innerHTML = `
                    <div class="pokedex-header">
                        <div class="pokedex-stats" id="pokedexStats">收集进度加载中...</div>
                        <div class="pokedex-bonuses" id="pokedexBonuses"></div>
                    </div>
                    <div class="pokedex-grid" id="pokedexGrid">加载中...</div>
                `;
                tabContainer.closest('.content-area, main, .main-content, body') ?.appendChild(content);
                return true;
            }
            return false;
        };

        if (!tryInject()) {
            const obs = new MutationObserver(() => {
                if (tryInject()) obs.disconnect();
            });
            obs.observe(document.body, { childList: true, subtree: true });
        }
    }

    injectBonusBadge() {
        // 在玩家属性区附近注入图鉴加成显示
        const tryInject = () => {
            const statsArea = document.querySelector('.player-stats, .profile-card, .player-info');
            if (statsArea && !document.getElementById('pokedexBonusBadge')) {
                const badge = document.createElement('div');
                badge.id = 'pokedexBonusBadge';
                badge.className = 'pokedex-bonus-badge pokedex-bonus-badge--hidden';
                badge.innerHTML = `
                    <span class="pbb-icon">📚</span>
                    <span class="pbb-text" id="pbbText">图鉴加成</span>
                `;
                statsArea.appendChild(badge);
                return true;
            }
            return false;
        };
        if (!tryInject()) {
            const obs = new MutationObserver(() => {
                if (tryInject()) obs.disconnect();
            });
            obs.observe(document.body, { childList: true, subtree: true });
        }
    }

    updatePokedexPanel() {
        if (!this.pokedexData) return;

        const { total, collected, pets } = this.pokedexData;

        const statsEl = document.getElementById('pokedexStats');
        if (statsEl) {
            statsEl.innerHTML = `
                <div class="pdx-stat-row">
                    <span class="pdx-stat-label">收集进度</span>
                    <span class="pdx-stat-value">${collected} / ${total}</span>
                </div>
                <div class="pdx-progress-bar">
                    <div class="pdx-progress-fill" style="width: ${total > 0 ? (collected/total*100) : 0}%"></div>
                </div>
            `;
        }

        const grid = document.getElementById('pokedexGrid');
        if (grid) {
            grid.innerHTML = pets.map(pet => `
                <div class="pdx-card ${pet.isCollected ? 'pdx-card--collected' : 'pdx-card--locked'} rarity-${pet.rarity || 1}">
                    <div class="pdx-card-emoji">${pet.isCollected ? this.getPetEmoji(pet.id) : '❓'}</div>
                    <div class="pdx-card-name">${pet.isCollected ? escapeHtml(pet.name) : '???'}</div>
                    <div class="pdx-card-rarity">${this.getRarityName(pet.rarity)}</div>
                    ${pet.isCollected ? `
                        <div class="pdx-card-level">Lv.${pet.myPetData?.level || 1}</div>
                    ` : `
                        <div class="pdx-card-locked">未收集</div>
                    `}
                </div>
            `).join('');
        }
    }

    updateBonuses(collected) {
        // GDD 7.4 图鉴加成里程碑
        const milestones = [
            { threshold: 5, bonus: '修炼速度 +2%', icon: '⚗️' },
            { threshold: 10, bonus: '全局攻击 +5', icon: '⚔️' },
            { threshold: 20, bonus: '全局防御 +3', icon: '🛡️' }
        ];

        const bonusesEl = document.getElementById('pokedexBonuses');
        if (bonusesEl) {
            bonusesEl.innerHTML = `
                <div class="pdx-bonuses-title">收集加成</div>
                ${milestones.map(m => `
                    <div class="pdx-bonus-item ${collected >= m.threshold ? 'active' : 'locked'}">
                        <span>${m.icon}</span>
                        <span class="pdx-bonus-label">收集 ${m.threshold} 只：${m.bonus}</span>
                        <span class="pdx-bonus-status">${collected >= m.threshold ? '✅ 已激活' : `需再收集 ${m.threshold - collected} 只`}</span>
                    </div>
                `).join('')}
            `;
        }

        // 更新主界面的加成徽章
        const badge = document.getElementById('pokedexBonusBadge');
        const badgeText = document.getElementById('pbbText');
        if (badge && badgeText) {
            const activeCount = milestones.filter(m => collected >= m.threshold).length;
            if (activeCount > 0) {
                badgeText.textContent = `图鉴加成 ×${activeCount}`;
                badge.classList.remove('pokedex-bonus-badge--hidden');
            }
        }
    }

    getPetEmoji(petId) {
        const emojis = { 1: '🦊', 2: '🦄', 3: '🐉', 4: '🐯', 5: '🐢', 6: '🦅', 7: '🐱', 9: '🐺' };
        return emojis[petId] || '🐾';
    }

    getRarityName(rarity) {
        const names = { 1: '普通', 2: '稀有', 3: '史诗', 4: '传说', 5: '神话' };
        return names[rarity] || '未知';
    }

    injectStyles() {
        if (document.getElementById('pokedexStyles')) return;
        const style = document.createElement('style');
        style.id = 'pokedexStyles';
        style.textContent = `
            /* 图鉴面板 */
            .pokedex-header {
                padding: 12px;
                background: rgba(127,255,212,0.04);
                border: 1px solid rgba(127,255,212,0.15);
                border-radius: 10px;
                margin-bottom: 16px;
            }
            .pdx-stat-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 6px;
            }
            .pdx-stat-label { font-size: 13px; color: rgba(255,255,255,0.6); font-family: 'Microsoft YaHei', sans-serif; }
            .pdx-stat-value { font-size: 16px; color: #7fffd4; font-weight: bold; font-family: 'Microsoft YaHei', sans-serif; }
            .pdx-progress-bar {
                height: 6px; background: rgba(255,255,255,0.1);
                border-radius: 3px; overflow: hidden; margin-bottom: 12px;
            }
            .pdx-progress-fill {
                height: 100%; background: linear-gradient(90deg, #7fffd4, #2ecc71);
                border-radius: 3px; transition: width 0.5s ease;
            }
            .pdx-bonuses-title { font-size: 12px; color: rgba(255,255,255,0.4); margin-bottom: 8px; font-family: 'Microsoft YaHei', sans-serif; }
            .pdx-bonus-item {
                display: flex; align-items: center; gap: 8px;
                padding: 6px 0; font-size: 12px; border-bottom: 1px solid rgba(255,255,255,0.05);
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .pdx-bonus-label { flex: 1; color: rgba(255,255,255,0.6); }
            .pdx-bonus-item.active .pdx-bonus-label { color: #e8e8e8; }
            .pdx-bonus-status { font-size: 11px; color: rgba(255,255,255,0.3); }
            .pdx-bonus-item.active .pdx-bonus-status { color: #2ecc71; }

            /* 图鉴网格 */
            .pokedex-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
                gap: 10px;
            }
            .pdx-card {
                background: rgba(255,255,255,0.04);
                border: 1px solid rgba(255,255,255,0.1);
                border-radius: 10px;
                padding: 12px 8px;
                text-align: center;
                transition: all 0.2s;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .pdx-card:hover { transform: translateY(-2px); }
            .pdx-card--collected {
                border-color: rgba(127,255,212,0.3);
                background: rgba(127,255,212,0.04);
            }
            .pdx-card--locked { opacity: 0.5; filter: grayscale(0.8); }
            .pdx-card-emoji { font-size: 32px; margin-bottom: 6px; }
            .pdx-card-name { font-size: 11px; color: #e8e8e8; margin-bottom: 3px; }
            .pdx-card-rarity { font-size: 10px; color: rgba(255,255,255,0.4); margin-bottom: 3px; }
            .pdx-card-level { font-size: 11px; color: #7fffd4; }
            .pdx-card-locked { font-size: 10px; color: rgba(255,255,255,0.25); }

            /* 图鉴加成徽章 */
            .pokedex-bonus-badge {
                display: inline-flex; align-items: center; gap: 4px;
                background: rgba(127,255,212,0.08);
                border: 1px solid rgba(127,255,212,0.25);
                border-radius: 6px; padding: 3px 10px;
                font-size: 12px; color: #7fffd4;
                margin-top: 6px;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .pokedex-bonus-badge--hidden { display: none !important; }
            .pbb-icon { font-size: 14px; }
        `;
        document.head.appendChild(style);
    }
}

// =====================================================
// 扩展 gameAPI 并初始化
// =====================================================
document.addEventListener('DOMContentLoaded', () => {
    const tryInit = () => {
        if (!window.gameAPI) { setTimeout(tryInit, 300); return; }

        // 扩展连招相关API
        if (!window.gameAPI.getAvailableCombos) {
            window.gameAPI.getAvailableCombos = () => window.api.get('/skills/combos/available');
        }
        if (!window.gameAPI.checkCombo) {
            window.gameAPI.checkCombo = (skillId) => window.api.post('/skills/combos/check', { skillId });
        }
        if (!window.gameAPI.getComboStats) {
            window.gameAPI.getComboStats = () => window.api.get('/skills/combos/stats');
        }

        if (localStorage.getItem('authToken')) {
            // 仅在战斗或技能页面初始化连招系统
            if (window.location.pathname.includes('combat') || window.location.pathname.includes('skill') ||
                document.getElementById('battleArea') || document.getElementById('skillsSection')) {
                window.skillComboSystem = new SkillComboSystem();
            }

            // 图鉴系统仅在宠物相关页面初始化
            if (window.location.pathname.includes('pets') || document.querySelector('.tab-btn')) {
                window.petPokedexSystem = new PetPokedexSystem();
            }
        }
    };
    tryInit();
});

// 对外暴露连招检测，可从战斗JS中调用
window.onSkillUsedForCombo = function(skillId) {
    if (window.skillComboSystem) {
        window.skillComboSystem.onSkillUsed(skillId);
    }
};
