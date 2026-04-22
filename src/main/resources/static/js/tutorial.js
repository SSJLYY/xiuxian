/**
 * 新手引导任务链系统
 * GDD 8.2: 5步串行新手任务，帮助玩家逐步了解游戏核心机制
 */

class TutorialSystem {
    constructor() {
        // 5步新手任务定义（与GDD完全对应）
        this.TUTORIAL_STEPS = [
            {
                id: 1,
                title: '踏入修炼之门',
                desc: '点击"开始修炼"按钮，开启你的修仙之旅',
                icon: '🧘',
                action: '开始修炼',
                reward: { desc: '破境丹×1', icon: '💊' },
                checkFn: (ctx) => {
                    const profile = ctx.profile || {};
                    return (profile.totalCultivationTime || 0) > 0 || profile.isCultivating;
                },
                hint: '在主界面找到修炼区域，点击"开始修炼"按钮',
                npcTip: '苏玄清: "修炼是修仙者的根基，万里之行始于足下。"'
            },
            {
                id: 2,
                title: '初试锋芒',
                desc: '完成第一场战斗，感受炼气期的战斗力量',
                icon: '⚔️',
                action: '前往战斗',
                reward: { desc: '基础铁剑×1', icon: '🗡️' },
                checkFn: (ctx) => {
                    const profile = ctx.profile || {};
                    const totalBattles = Number(profile.totalBattles || profile.total_battles || 0);
                    return totalBattles >= 1 || (ctx.combatHistoryCount || 0) >= 1;
                },
                hint: '点击导航栏的"战斗"进入战斗界面，挑战野外妖兽',
                npcTip: '剑无痕: "师弟，战场上没有温柔，先活下来再说其他。"'
            },
            {
                id: 3,
                title: '灵兽初遇',
                desc: '给你的宠物喂食一次，建立与灵兽的联系',
                icon: '🦊',
                action: '前往宠物',
                reward: { desc: '宠物技能书×1', icon: '📖' },
                checkFn: (ctx) => {
                    const profile = ctx.profile || {};
                    return Number(profile.petFeedCount || 0) >= 1 || ctx.anyPetFed === true || ctx.petFeedMarked === true;
                },
                hint: '去宠物系统，找到你的宠物，点击"喂食"按钮',
                npcTip: '白鹿真人: "灵兽有情，以诚相待，方可心意相通。"'
            },
            {
                id: 4,
                title: '初悟大道',
                desc: '学习你的第一个技能，踏上属于自己的修炼之路',
                icon: '✨',
                action: '前往技能',
                reward: { desc: '高级修炼丹×3', icon: '💎' },
                checkFn: (ctx) => {
                    const profile = ctx.profile || {};
                    return Number(profile.skillCount || 0) >= 1 || (ctx.learnedSkillCount || 0) >= 1;
                },
                hint: '在技能界面购买并学习一个技能',
                npcTip: '苏玄清: "天道万千，择一而精，此乃真修之道。"'
            },
            {
                id: 5,
                title: '根基初成',
                desc: '修炼至练气期三层，奠定修仙根基',
                icon: '🌟',
                action: '继续修炼',
                reward: { desc: '稀有宠物捕获令×1', icon: '🎫' },
                checkFn: (ctx) => ((ctx.profile || {}).level || 1) >= 3,
                hint: '坚持修炼和战斗，升级至练气期三层',
                npcTip: '苏玄清: "练气三层，根基已稳。从今日起，你已是正式弟子。"'
            }
        ];

        this.currentStep = 0;
        this.isCompleted = false;
        this.isVisible = false;
        this.playerData = null;
        this.progressContext = null;
        this.hasShownExitHook = false;
        this.isActionProcessing = false;
        this._highlightTimer = null;
        this._focusedElement = null;
        this.tutorialKey = 'tutorial_completed';
        this.tutorialStepKey = 'tutorial_step';
        
        this.init();
    }

    // 初始化系统
    async init() {
        // 已完成则不展示
        const tutorialState = localStorage.getItem(this.tutorialKey);
        if (tutorialState === 'true' || tutorialState === 'skipped') {
            this.isCompleted = true;
            this.destroyUI();
            this.setupExitHook(); // 仍然挂载退出钩子
            return;
        }

        // 恢复进度
        const savedStep = parseInt(localStorage.getItem(this.tutorialStepKey) || '0', 10);
        if (Number.isFinite(savedStep) && savedStep >= this.TUTORIAL_STEPS.length) {
            this.completeAll();
            return;
        }
        this.currentStep = Number.isFinite(savedStep)
            ? Math.max(0, Math.min(savedStep, this.TUTORIAL_STEPS.length - 1))
            : 0;

        this.createUI();
        await this.checkProgress();
        this.setupExitHook();

        // 首次进入：延迟1.5s显示引导
        if (this.currentStep === 0) {
            setTimeout(() => this.show(), 1500);
        } else {
            setTimeout(() => this.show(), 800);
        }
    }

    // 创建UI
    createUI() {
        if (document.getElementById('tutorialPanel')) return;

        const panel = document.createElement('div');
        panel.id = 'tutorialPanel';
        panel.innerHTML = `
            <div class="tutorial-header">
                <div class="tutorial-title-row">
                    <span class="tutorial-icon">📜</span>
                    <span class="tutorial-title">新手引导</span>
                    <div class="tutorial-progress-text" id="tutorialProgress">1/5</div>
                </div>
                <button class="tutorial-minimize-btn" id="tutorialMinBtn" title="最小化">—</button>
            </div>
            <div class="tutorial-body" id="tutorialBody">
                <div class="tutorial-step-indicator" id="tutorialStepIndicator"></div>
                <div class="tutorial-current" id="tutorialCurrent"></div>
                <div class="tutorial-npc-tip" id="tutorialNpcTip"></div>
                <div class="tutorial-hint" id="tutorialHint"></div>
                <div class="tutorial-action-row">
                    <button class="tutorial-action-btn" id="tutorialActionBtn">开始修炼</button>
                    <button class="tutorial-skip-btn" id="tutorialSkipBtn">跳过引导</button>
                </div>
            </div>
        `;
        document.body.appendChild(panel);

        // 注入样式
        this.injectStyles();

        // 完成弹窗
        const modal = document.createElement('div');
        modal.id = 'tutorialCompleteModal';
        modal.innerHTML = `
            <div class="tutorial-complete-overlay" id="tutorialCompleteOverlay"></div>
            <div class="tutorial-complete-content">
                <div class="tutorial-complete-firework">🎊</div>
                <h2 class="tutorial-complete-title">引导完成！</h2>
                <p class="tutorial-complete-subtitle">恭喜你完成了新手引导，<br>你已经踏上了真正的修仙之路！</p>
                <div class="tutorial-complete-rewards" id="tutorialCompleteRewards"></div>
                <div class="tutorial-complete-npc">
                    <span class="tutorial-complete-npc-avatar">👨‍🦳</span>
                    <p class="tutorial-complete-npc-text">"从今往后，苍玄界的风雨，就由你来见证。" —— 苏玄清</p>
                </div>
                <button class="tutorial-complete-btn" id="tutorialCompleteBtn">
                    踏入仙途 →
                </button>
            </div>
        `;
        document.body.appendChild(modal);

        // 退出钩子弹窗
        const exitModal = document.createElement('div');
        exitModal.id = 'tutorialExitModal';
        exitModal.innerHTML = `
            <div class="tutorial-exit-overlay"></div>
            <div class="tutorial-exit-content">
                <div class="tutorial-exit-pet">🦊</div>
                <h3 class="tutorial-exit-title">你的灵兽在等你回来</h3>
                <p class="tutorial-exit-desc" id="tutorialExitDesc">你的宠物饱食度将在<strong>约12小时</strong>后耗尽，记得明天回来喂食哦！</p>
                <div class="tutorial-exit-btn-row">
                    <button class="tutorial-exit-stay-btn" id="tutorialExitStayBtn">继续游戏</button>
                    <button class="tutorial-exit-leave-btn" id="tutorialExitLeaveBtn">离开游戏</button>
                </div>
            </div>
        `;
        document.body.appendChild(exitModal);

        this.bindUIEvents();
        this.ensureFocusMask();
    }

    async getProgressContext() {
        const context = {
            profile: null,
            learnedSkillCount: 0,
            anyPetFed: false,
            combatHistoryCount: 0,
            petFeedMarked: sessionStorage.getItem('tutorial_pet_fed_once') === 'true'
        };

        const [profileRes, petsRes, skillsRes, combatHistoryRes] = await Promise.allSettled([
            gameAPI.getCurrentPlayerProfile(),
            gameAPI.getMyPets ? gameAPI.getMyPets() : Promise.resolve(null),
            gameAPI.getSkills ? gameAPI.getSkills() : Promise.resolve(null),
            gameAPI.getCombatHistory ? gameAPI.getCombatHistory(1) : Promise.resolve(null)
        ]);

        if (profileRes.status === 'fulfilled' && profileRes.value?.success) {
            context.profile = profileRes.value.data || {};
        }

        if (petsRes.status === 'fulfilled' && petsRes.value?.success) {
            const pets = Array.isArray(petsRes.value.data) ? petsRes.value.data : [];
            context.anyPetFed = pets.some(pet => !!pet?.lastFeedTime);
        }

        if (skillsRes.status === 'fulfilled' && skillsRes.value?.success) {
            const skills = Array.isArray(skillsRes.value.data) ? skillsRes.value.data : [];
            context.learnedSkillCount = skills.length;
        }

        if (combatHistoryRes.status === 'fulfilled' && combatHistoryRes.value?.success) {
            const logs = Array.isArray(combatHistoryRes.value.data) ? combatHistoryRes.value.data : [];
            context.combatHistoryCount = logs.length;
        }

        return context;
    }

    bindUIEvents() {
        const minBtn = document.getElementById('tutorialMinBtn');
        if (minBtn) minBtn.onclick = () => this.toggle();

        const skipBtn = document.getElementById('tutorialSkipBtn');
        if (skipBtn) skipBtn.onclick = () => this.skipAll();

        const completeOverlay = document.getElementById('tutorialCompleteOverlay');
        if (completeOverlay) completeOverlay.onclick = () => this.closeCompleteModal();

        const completeBtn = document.getElementById('tutorialCompleteBtn');
        if (completeBtn) completeBtn.onclick = () => this.closeCompleteModal();

        const exitStayBtn = document.getElementById('tutorialExitStayBtn');
        if (exitStayBtn) exitStayBtn.onclick = () => this.closeExitModal();
    }

    // 注入CSS样式
    injectStyles() {
        if (document.getElementById('tutorialStyles')) return;
        const style = document.createElement('style');
        style.id = 'tutorialStyles';
        style.textContent = `
            /* 新手引导面板 */
            #tutorialPanel {
                position: fixed;
                bottom: 20px;
                left: 20px;
                width: 320px;
                background: linear-gradient(135deg, rgba(10,10,30,0.97) 0%, rgba(20,20,50,0.97) 100%);
                border: 1px solid rgba(212,175,55,0.5);
                border-radius: 12px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.6), 0 0 20px rgba(212,175,55,0.15);
                z-index: 9000;
                transition: all 0.3s ease;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            #tutorialPanel.minimized .tutorial-body {
                display: none;
            }
            #tutorialPanel.minimized {
                width: 200px;
            }
            .tutorial-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 16px 8px;
                border-bottom: 1px solid rgba(212,175,55,0.2);
            }
            .tutorial-title-row {
                display: flex;
                align-items: center;
                gap: 8px;
            }
            .tutorial-icon { font-size: 18px; }
            .tutorial-title {
                font-size: 14px;
                font-weight: bold;
                color: #d4af37;
                letter-spacing: 1px;
            }
            .tutorial-progress-text {
                font-size: 11px;
                color: #7fffd4;
                background: rgba(127,255,212,0.1);
                padding: 2px 8px;
                border-radius: 10px;
                border: 1px solid rgba(127,255,212,0.3);
            }
            .tutorial-minimize-btn {
                background: none;
                border: 1px solid rgba(255,255,255,0.2);
                color: #aaa;
                cursor: pointer;
                width: 24px;
                height: 24px;
                border-radius: 50%;
                font-size: 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.2s;
            }
            .tutorial-minimize-btn:hover { color: #fff; border-color: #fff; }
            .tutorial-body { padding: 12px 16px 16px; }

            .tutorial-guide-highlight {
                position: relative;
                z-index: 9100;
                box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.9), 0 0 18px rgba(255, 215, 0, 0.6);
                animation: tutorialGuidePulse 1.2s ease-in-out infinite;
            }

            #tutorialFocusMask {
                position: fixed;
                inset: 0;
                pointer-events: none;
                z-index: 9050;
                display: none;
            }

            #tutorialFocusHole {
                position: fixed;
                border-radius: 10px;
                box-shadow: 0 0 0 9999px rgba(5, 8, 20, 0.62);
                transition: all 0.2s ease;
                pointer-events: none;
            }

            #tutorialFocusBubble {
                position: fixed;
                max-width: 280px;
                min-width: 180px;
                background: linear-gradient(135deg, rgba(20, 26, 50, 0.97), rgba(10, 14, 30, 0.97));
                border: 1px solid rgba(212, 175, 55, 0.55);
                border-radius: 10px;
                padding: 10px 12px;
                color: #f4f2e6;
                font-size: 12px;
                line-height: 1.5;
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.45);
                pointer-events: none;
                z-index: 9060;
            }

            #tutorialFocusBubble::before {
                content: '';
                position: absolute;
                width: 0;
                height: 0;
                border-left: 8px solid transparent;
                border-right: 8px solid transparent;
            }

            #tutorialFocusBubble.arrow-up::before {
                top: -8px;
                left: 22px;
                border-bottom: 8px solid rgba(212, 175, 55, 0.8);
            }

            #tutorialFocusBubble.arrow-down::before {
                bottom: -8px;
                left: 22px;
                border-top: 8px solid rgba(212, 175, 55, 0.8);
            }

            .tutorial-focus-title {
                color: #d4af37;
                font-weight: bold;
                margin-bottom: 4px;
            }

            .tutorial-focus-desc {
                color: rgba(255, 255, 255, 0.9);
            }

            @keyframes tutorialGuidePulse {
                0% { transform: scale(1); }
                50% { transform: scale(1.03); }
                100% { transform: scale(1); }
            }
            
            /* 步骤指示器 */
            .tutorial-step-indicator {
                display: flex;
                gap: 6px;
                margin-bottom: 12px;
            }
            .tutorial-step-dot {
                width: 8px; height: 8px;
                border-radius: 50%;
                background: rgba(255,255,255,0.15);
                transition: all 0.3s;
            }
            .tutorial-step-dot.done { background: #2ecc71; }
            .tutorial-step-dot.current {
                background: #d4af37;
                box-shadow: 0 0 8px rgba(212,175,55,0.8);
                transform: scale(1.3);
            }

            /* 当前步骤 */
            .tutorial-current {
                background: rgba(212,175,55,0.08);
                border: 1px solid rgba(212,175,55,0.2);
                border-radius: 8px;
                padding: 12px;
                margin-bottom: 10px;
            }
            .tutorial-step-title {
                font-size: 15px;
                font-weight: bold;
                color: #d4af37;
                margin-bottom: 6px;
                display: flex;
                align-items: center;
                gap: 8px;
            }
            .tutorial-step-desc {
                font-size: 13px;
                color: #e8e8e8;
                line-height: 1.5;
                margin-bottom: 8px;
            }
            .tutorial-reward-badge {
                display: inline-flex;
                align-items: center;
                gap: 4px;
                background: rgba(127,255,212,0.1);
                border: 1px solid rgba(127,255,212,0.3);
                border-radius: 6px;
                padding: 3px 10px;
                font-size: 12px;
                color: #7fffd4;
            }
            .tutorial-step-done {
                display: flex;
                align-items: center;
                gap: 6px;
                color: #2ecc71;
                font-size: 13px;
                font-weight: bold;
            }

            /* NPC提示 */
            .tutorial-npc-tip {
                font-size: 12px;
                color: #aaaacc;
                font-style: italic;
                padding: 8px 12px;
                border-left: 2px solid rgba(127,255,212,0.4);
                margin-bottom: 8px;
                background: rgba(127,255,212,0.04);
                border-radius: 0 6px 6px 0;
                line-height: 1.6;
            }

            /* 操作提示 */
            .tutorial-hint {
                font-size: 11px;
                color: rgba(255,255,255,0.45);
                margin-bottom: 12px;
                line-height: 1.5;
            }
            .tutorial-hint::before { content: '💡 '; }

            /* 按钮行 */
            .tutorial-action-row {
                display: flex;
                gap: 8px;
                align-items: center;
            }
            .tutorial-action-btn {
                flex: 1;
                background: linear-gradient(135deg, #d4af37, #b8972e);
                color: #1a1a2e;
                border: none;
                border-radius: 8px;
                padding: 9px 14px;
                font-size: 13px;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.2s;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .tutorial-action-btn:hover {
                transform: translateY(-1px);
                box-shadow: 0 4px 12px rgba(212,175,55,0.4);
            }
            .tutorial-skip-btn {
                background: none;
                border: 1px solid rgba(255,255,255,0.15);
                color: rgba(255,255,255,0.3);
                border-radius: 8px;
                padding: 9px 10px;
                font-size: 11px;
                cursor: pointer;
                transition: all 0.2s;
                white-space: nowrap;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .tutorial-skip-btn:hover { color: rgba(255,255,255,0.6); border-color: rgba(255,255,255,0.3); }

            /* 完成弹窗 */
            #tutorialCompleteModal {
                display: none;
                position: fixed;
                inset: 0;
                z-index: 10100;
                align-items: center;
                justify-content: center;
            }
            #tutorialCompleteModal.show { display: flex; }
            .tutorial-complete-overlay {
                position: absolute;
                inset: 0;
                background: rgba(0,0,0,0.75);
                backdrop-filter: blur(4px);
            }
            .tutorial-complete-content {
                position: relative;
                background: linear-gradient(135deg, #0d0d2b 0%, #1a1a3e 100%);
                border: 2px solid rgba(212,175,55,0.6);
                border-radius: 20px;
                padding: 40px 36px;
                max-width: 420px;
                width: 90%;
                text-align: center;
                box-shadow: 0 20px 60px rgba(0,0,0,0.8), 0 0 40px rgba(212,175,55,0.2);
                animation: tutorialComplete 0.5s ease;
            }
            @keyframes tutorialComplete {
                from { opacity: 0; transform: scale(0.8) translateY(20px); }
                to { opacity: 1; transform: scale(1) translateY(0); }
            }
            .tutorial-complete-firework {
                font-size: 64px;
                margin-bottom: 16px;
                animation: firework 1s ease infinite alternate;
            }
            @keyframes firework {
                from { transform: scale(1) rotate(-5deg); }
                to { transform: scale(1.1) rotate(5deg); }
            }
            .tutorial-complete-title {
                font-size: 28px;
                color: #d4af37;
                margin-bottom: 12px;
                text-shadow: 0 0 20px rgba(212,175,55,0.5);
            }
            .tutorial-complete-subtitle {
                font-size: 15px;
                color: #e8e8e8;
                line-height: 1.8;
                margin-bottom: 24px;
            }
            .tutorial-complete-rewards {
                display: flex;
                flex-wrap: wrap;
                gap: 8px;
                justify-content: center;
                margin-bottom: 24px;
            }
            .tutorial-reward-item {
                background: rgba(212,175,55,0.1);
                border: 1px solid rgba(212,175,55,0.3);
                border-radius: 8px;
                padding: 6px 14px;
                font-size: 13px;
                color: #d4af37;
            }
            .tutorial-complete-npc {
                display: flex;
                align-items: center;
                gap: 12px;
                background: rgba(127,255,212,0.05);
                border: 1px solid rgba(127,255,212,0.2);
                border-radius: 10px;
                padding: 12px 16px;
                margin-bottom: 24px;
                text-align: left;
            }
            .tutorial-complete-npc-avatar { font-size: 28px; flex-shrink: 0; }
            .tutorial-complete-npc-text {
                font-size: 13px;
                color: #aaaacc;
                font-style: italic;
                line-height: 1.6;
            }
            .tutorial-complete-btn {
                background: linear-gradient(135deg, #d4af37, #b8972e);
                color: #1a1a2e;
                border: none;
                border-radius: 10px;
                padding: 14px 40px;
                font-size: 16px;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.2s;
                font-family: 'Microsoft YaHei', sans-serif;
                width: 100%;
            }
            .tutorial-complete-btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(212,175,55,0.5);
            }

            /* 退出钩子弹窗 */
            #tutorialExitModal {
                display: none;
                position: fixed;
                inset: 0;
                z-index: 10200;
                align-items: center;
                justify-content: center;
            }
            #tutorialExitModal.show { display: flex; }
            .tutorial-exit-overlay {
                position: absolute;
                inset: 0;
                background: rgba(0,0,0,0.6);
            }
            .tutorial-exit-content {
                position: relative;
                background: linear-gradient(135deg, #0d0d2b 0%, #1a1a3e 100%);
                border: 1px solid rgba(127,255,212,0.4);
                border-radius: 16px;
                padding: 32px 28px;
                max-width: 360px;
                width: 90%;
                text-align: center;
                box-shadow: 0 16px 48px rgba(0,0,0,0.7);
                animation: slideUp 0.3s ease;
            }
            @keyframes slideUp {
                from { transform: translateY(20px); opacity: 0; }
                to { transform: translateY(0); opacity: 1; }
            }
            .tutorial-exit-pet { font-size: 52px; margin-bottom: 12px; }
            .tutorial-exit-title {
                font-size: 20px;
                color: #d4af37;
                margin-bottom: 10px;
            }
            .tutorial-exit-desc {
                font-size: 14px;
                color: #e8e8e8;
                line-height: 1.7;
                margin-bottom: 24px;
            }
            .tutorial-exit-desc strong { color: #7fffd4; }
            .tutorial-exit-btn-row { display: flex; gap: 10px; }
            .tutorial-exit-stay-btn {
                flex: 1;
                background: linear-gradient(135deg, #d4af37, #b8972e);
                color: #1a1a2e;
                border: none;
                border-radius: 8px;
                padding: 12px;
                font-size: 14px;
                font-weight: bold;
                cursor: pointer;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .tutorial-exit-leave-btn {
                flex: 1;
                background: none;
                border: 1px solid rgba(255,255,255,0.2);
                color: rgba(255,255,255,0.5);
                border-radius: 8px;
                padding: 12px;
                font-size: 14px;
                cursor: pointer;
                font-family: 'Microsoft YaHei', sans-serif;
            }
        `;
        document.head.appendChild(style);
    }

    // 检查任务进度
    async checkProgress() {
        try {
            const progressContext = await this.getProgressContext();
            if (!progressContext.profile) return;
            this.progressContext = progressContext;
            this.playerData = progressContext.profile;

            // 逐步推进到当前应有的进度
            let advanced = false;
            for (let i = this.currentStep; i < this.TUTORIAL_STEPS.length; i++) {
                if (this.TUTORIAL_STEPS[i].checkFn(progressContext)) {
                    this.currentStep = i + 1;
                    advanced = true;
                } else {
                    break;
                }
            }

            if (advanced) {
                localStorage.setItem(this.tutorialStepKey, this.currentStep);
            }

            // 全部完成
            if (this.currentStep >= this.TUTORIAL_STEPS.length) {
                this.completeAll();
                return;
            }

            this.render();
        } catch (e) {
            console.warn('Tutorial progress check failed:', e);
        }
    }

    clearStepHighlights() {
        document.querySelectorAll('.tutorial-guide-highlight').forEach(el => {
            el.classList.remove('tutorial-guide-highlight');
        });
        this.hideFocusMask();
    }

    ensureFocusMask() {
        if (document.getElementById('tutorialFocusMask')) return;
        const mask = document.createElement('div');
        mask.id = 'tutorialFocusMask';
        mask.innerHTML = '<div id="tutorialFocusHole"></div><div id="tutorialFocusBubble"></div>';
        document.body.appendChild(mask);
    }

    getFocusBubbleText() {
        const step = this.TUTORIAL_STEPS[this.currentStep];
        if (!step) return { title: '引导提示', desc: '请按提示完成当前步骤' };
        return {
            title: `${step.icon} ${step.action}`,
            desc: step.hint
        };
    }

    updateFocusMask() {
        if (!this._focusedElement) return;
        const mask = document.getElementById('tutorialFocusMask');
        const hole = document.getElementById('tutorialFocusHole');
        const bubble = document.getElementById('tutorialFocusBubble');
        if (!mask || !hole || !bubble) return;

        const rect = this._focusedElement.getBoundingClientRect();
        if (!rect || rect.width <= 0 || rect.height <= 0) {
            this.hideFocusMask();
            return;
        }

        const padding = 8;
        hole.style.left = `${Math.max(0, rect.left - padding)}px`;
        hole.style.top = `${Math.max(0, rect.top - padding)}px`;
        hole.style.width = `${rect.width + padding * 2}px`;
        hole.style.height = `${rect.height + padding * 2}px`;

        const bubbleText = this.getFocusBubbleText();
        bubble.innerHTML = `
            <div class="tutorial-focus-title">${bubbleText.title}</div>
            <div class="tutorial-focus-desc">${bubbleText.desc}</div>
        `;

        const margin = 12;
        const bubbleRect = bubble.getBoundingClientRect();
        const viewportW = window.innerWidth;
        const viewportH = window.innerHeight;
        const preferBelow = rect.bottom + margin + bubbleRect.height < viewportH - 8;
        let bubbleLeft = rect.left;
        let bubbleTop;

        if (preferBelow) {
            bubbleTop = rect.bottom + margin;
            bubble.classList.add('arrow-up');
            bubble.classList.remove('arrow-down');
        } else {
            bubbleTop = Math.max(8, rect.top - bubbleRect.height - margin);
            bubble.classList.add('arrow-down');
            bubble.classList.remove('arrow-up');
        }

        const maxLeft = Math.max(8, viewportW - bubbleRect.width - 8);
        bubbleLeft = Math.max(8, Math.min(bubbleLeft, maxLeft));
        if (bubbleTop + bubbleRect.height > viewportH - 8) {
            bubbleTop = Math.max(8, viewportH - bubbleRect.height - 8);
        }

        bubble.style.left = `${bubbleLeft}px`;
        bubble.style.top = `${bubbleTop}px`;
        mask.style.display = 'block';
    }

    showFocusMask(element) {
        if (!element) {
            this.hideFocusMask();
            return;
        }
        this.ensureFocusMask();
        this._focusedElement = element;
        this.updateFocusMask();
    }

    hideFocusMask() {
        const mask = document.getElementById('tutorialFocusMask');
        if (mask) mask.style.display = 'none';
        this._focusedElement = null;
    }

    destroyUI() {
        this.clearStepHighlights();
        const panel = document.getElementById('tutorialPanel');
        if (panel) panel.remove();
        const focusMask = document.getElementById('tutorialFocusMask');
        if (focusMask) focusMask.remove();
    }

    findButtonByText(containerSelector, keywords) {
        const buttons = document.querySelectorAll(`${containerSelector} button`);
        for (const btn of buttons) {
            const text = (btn.textContent || '').trim();
            if (keywords.some(k => text.includes(k))) {
                return btn;
            }
        }
        return null;
    }

    applyStepHighlight() {
        const step = this.TUTORIAL_STEPS[this.currentStep];
        if (!step) return false;

        this.clearStepHighlights();

        const navModuleMap = {
            1: 'dashboard',
            2: 'combat',
            3: 'pets',
            4: 'skills',
            5: 'dashboard'
        };

        const navModule = navModuleMap[step.id];
        let navBtn = null;
        if (navModule) {
            navBtn = document.querySelector(`.nav-item[data-module="${navModule}"] .nav-button`) ||
                document.querySelector(`.nav-item[data-module="${navModule}"] button`);
            if (navBtn) {
                navBtn.classList.add('tutorial-guide-highlight');
            }
        }

        let target = null;
        if (step.id === 1 || step.id === 5) {
            target = document.querySelector('#cultivation-btn') ||
                document.querySelector('#startCultivateBtn');
        } else if (step.id === 2) {
            target = document.querySelector('#startCombatBtn') ||
                this.findButtonByText('#combat-module', ['开始挑战', '开始战斗']);
        } else if (step.id === 3) {
            target = document.querySelector('#myPetsList button[onclick*="feedPet"]') ||
                this.findButtonByText('#pets-module', ['喂食']);
        } else if (step.id === 4) {
            target = document.querySelector('#skills-module button[onclick*="learnSkill"]') ||
                document.querySelector('#skills-module button[data-action="learn"]') ||
                this.findButtonByText('#skills-module', ['学习', '学习技能']);
        }

        if (target) {
            target.classList.add('tutorial-guide-highlight');
            this.showFocusMask(target);
            return true;
        }
        if (navBtn) {
            this.showFocusMask(navBtn);
            return true;
        }
        this.hideFocusMask();
        return false;
    }

    scheduleStepHighlight() {
        if (this._highlightTimer) {
            clearInterval(this._highlightTimer);
        }

        let tries = 0;
        this._highlightTimer = setInterval(() => {
            tries += 1;
            const found = this.applyStepHighlight();
            if (found || tries >= 12 || this.isCompleted) {
                clearInterval(this._highlightTimer);
                this._highlightTimer = null;
            }
        }, 300);
    }

    // 渲染当前步骤
    render() {
        const panel = document.getElementById('tutorialPanel');
        if (!panel) return;

        const step = this.TUTORIAL_STEPS[this.currentStep];
        if (!step) return;

        // 更新进度文字
        const progressEl = document.getElementById('tutorialProgress');
        if (progressEl) progressEl.textContent = `${this.currentStep + 1}/5`;

        // 步骤指示器
        const indicator = document.getElementById('tutorialStepIndicator');
        if (indicator) {
            indicator.innerHTML = this.TUTORIAL_STEPS.map((s, i) => {
                let cls = 'tutorial-step-dot';
                if (i < this.currentStep) cls += ' done';
                else if (i === this.currentStep) cls += ' current';
                return `<div class="${cls}" title="${s.title}"></div>`;
            }).join('');
        }

        // 当前步骤内容
        const currentEl = document.getElementById('tutorialCurrent');
        if (currentEl) {
            currentEl.innerHTML = `
                <div class="tutorial-step-title">${step.icon} ${step.title}</div>
                <div class="tutorial-step-desc">${step.desc}</div>
                <div class="tutorial-reward-badge">${step.reward.icon} 奖励: ${step.reward.desc}</div>
            `;
        }

        // NPC提示
        const npcEl = document.getElementById('tutorialNpcTip');
        if (npcEl) npcEl.textContent = step.npcTip;

        // 操作提示
        const hintEl = document.getElementById('tutorialHint');
        if (hintEl) hintEl.textContent = step.hint;

        // 行动按钮
        const btnEl = document.getElementById('tutorialActionBtn');
        if (btnEl) {
            btnEl.textContent = step.action;
            btnEl.onclick = () => {
                if (this.isActionProcessing) {
                    return;
                }
                this.isActionProcessing = true;
                btnEl.disabled = true;
                console.log('[Tutorial] 行动按钮被点击，步骤:', step.id, step.title);
                this.handleAction(step);
                setTimeout(() => {
                    this.isActionProcessing = false;
                    const activeBtnEl = document.getElementById('tutorialActionBtn');
                    if (activeBtnEl) activeBtnEl.disabled = false;
                }, 1200);
            };
        }

        this.scheduleStepHighlight();
    }

    // 处理行动按钮点击
    handleAction(step) {
        console.log('[Tutorial] 处理行动按钮点击:', step.title);
        // 根据步骤导航到对应页面/模块
        switch (step.id) {
            case 1: // 开始修炼
                this.navigateTo('cultivation');
                setTimeout(() => {
                    if (typeof window.startCultivation === 'function') {
                        console.log('[Tutorial] 自动触发开始修炼');
                        window.startCultivation();
                    }
                }, 200);
                this.scheduleStepHighlight();
                break;
            case 2: // 前往战斗
                this.navigateTo('combat');
                setTimeout(() => {
                    if (typeof window.startBattle === 'function') {
                        console.log('[Tutorial] 自动触发开始挑战');
                        window.startBattle();
                    }
                }, 200);
                this.scheduleStepHighlight();
                break;
            case 3: // 前往宠物
                this.navigateTo('pets');
                this.scheduleStepHighlight();
                break;
            case 4: // 前往技能
                this.navigateTo('skills');
                this.scheduleStepHighlight();
                break;
            case 5: // 继续修炼
                this.navigateTo('cultivation');
                setTimeout(() => {
                    if (typeof window.startCultivation === 'function') {
                        console.log('[Tutorial] 自动触发继续修炼');
                        window.startCultivation();
                    }
                }, 200);
                this.scheduleStepHighlight();
                break;
        }
        // 3秒后检查是否已完成
        setTimeout(() => this.checkProgress(), 3000);
    }

    // 导航到对应模块
    navigateTo(module) {
        console.log('[Tutorial] 导航到模块:', module);

        const moduleAliasMap = {
            cultivation: 'dashboard'
        };
        const targetModule = moduleAliasMap[module] || module;
        
        // 直接使用 showModule 函数（如果可用）
        if (window.showModule) {
            console.log('[Tutorial] 使用 showModule 函数切换模块:', targetModule);
            window.showModule(targetModule);
            return;
        }
        
        // 尝试点击侧边栏导航
        const navMap = {
            cultivation: ['.nav-item[data-module="dashboard"] .nav-button', '.nav-item[data-module="dashboard"] button'],
            combat: ['.nav-item[data-module="combat"] .nav-button', '.nav-item[data-module="combat"] button', 'a[href="enhanced_combat.html"]'],
            pets: ['.nav-item[data-module="pets"] .nav-button', '.nav-item[data-module="pets"] button', 'a[href="pets.html"]'],
            skills: ['.nav-item[data-module="skills"] .nav-button', '.nav-item[data-module="skills"] button', 'a[href="skills.html"]'],
            inventory: ['.nav-item[data-module="inventory"] .nav-button', '.nav-item[data-module="inventory"] button'],
            shop: ['.nav-item[data-module="shop"] .nav-button', '.nav-item[data-module="shop"] button'],
            quests: ['.nav-item[data-module="quests"] .nav-button', '.nav-item[data-module="quests"] button']
        };

        const selectors = navMap[module] || [];
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el) {
                console.log('[Tutorial] 找到导航元素:', sel, el);
                el.click();
                return;
            }
        }
        console.warn('[Tutorial] 未找到导航元素:', module, selectors);
    }

    // 定期轮询进度（玩家完成任务后自动推进）
    startPolling() {
        if (this._pollTimer) return;
        this._pollTimer = setInterval(() => {
            if (!this.isCompleted) {
                this.checkProgress();
            } else {
                clearInterval(this._pollTimer);
            }
        }, 8000); // 每8秒检查一次
    }

    // 显示面板
    show() {
        if (this.isCompleted) return;
        const panel = document.getElementById('tutorialPanel');
        if (panel) {
            this.isVisible = true;
            panel.style.display = 'block';
            this.render();
            this.startPolling();

            if (!this._focusSyncHandler) {
                this._focusSyncHandler = () => this.updateFocusMask();
                window.addEventListener('resize', this._focusSyncHandler);
                window.addEventListener('scroll', this._focusSyncHandler, true);
            }
        }
    }

    // 切换最小化
    toggle() {
        const panel = document.getElementById('tutorialPanel');
        if (!panel) return;
        panel.classList.toggle('minimized');
        const btn = document.getElementById('tutorialMinBtn');
        if (btn) btn.textContent = panel.classList.contains('minimized') ? '＋' : '—';
    }

    // 全部完成
    completeAll() {
        this.isCompleted = true;
        localStorage.setItem(this.tutorialKey, 'true');
        localStorage.removeItem(this.tutorialStepKey);
        sessionStorage.removeItem('tutorial_pet_fed_once');
        this.clearStepHighlights();
        if (this._highlightTimer) {
            clearInterval(this._highlightTimer);
            this._highlightTimer = null;
        }

        // 隐藏面板
        this.destroyUI();

        if (this._focusSyncHandler) {
            window.removeEventListener('resize', this._focusSyncHandler);
            window.removeEventListener('scroll', this._focusSyncHandler, true);
            this._focusSyncHandler = null;
        }

        // 显示完成弹窗
        this.showCompleteModal();

        if (this._pollTimer) {
            clearInterval(this._pollTimer);
        }
    }

    // 显示完成弹窗
    showCompleteModal() {
        const modal = document.getElementById('tutorialCompleteModal');
        if (!modal) return;

        // 填充奖励列表
        const rewardsEl = document.getElementById('tutorialCompleteRewards');
        if (rewardsEl) {
            const allRewards = this.TUTORIAL_STEPS.map(s => `
                <div class="tutorial-reward-item">${s.reward.icon} ${s.reward.desc}</div>
            `).join('');
            rewardsEl.innerHTML = allRewards;
        }

        modal.classList.add('show');
    }

    // 关闭完成弹窗
    closeCompleteModal() {
        const modal = document.getElementById('tutorialCompleteModal');
        if (modal) modal.classList.remove('show');
    }

    // 设置退出钩子
    setupExitHook() {
        // 监听页面卸载事件，展示宠物饥饿提示
        window.addEventListener('beforeunload', (e) => {
            // 仅在有宠物时触发（通过localStorage记录是否有宠物）
            const hasPet = localStorage.getItem('hasPet') === 'true';
            if (hasPet && !this.hasShownExitHook) {
                this.hasShownExitHook = true;
                // 注意：现代浏览器不允许在beforeunload中弹窗，改用visibilitychange
            }
        });

        // 更好的方式：visibilitychange 隐藏页面时弹出
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'hidden') {
                // 页面进入后台（切换标签等）
                // 标记需要提示，下次回来时展示
                localStorage.setItem('showPetHint', 'true');
            } else if (document.visibilityState === 'visible') {
                const showHint = localStorage.getItem('showPetHint');
                const hasPet = localStorage.getItem('hasPet') === 'true';
                if (showHint === 'true' && hasPet) {
                    localStorage.removeItem('showPetHint');
                    // 延迟3秒显示，让页面先渲染
                    setTimeout(() => this.updateAndShowExitHint(), 3000);
                }
            }
        });
    }

    // 显示退出钩子
    async updateAndShowExitHint() {
        try {
            // 获取宠物数据
            const res = await gameAPI.getActivePet();
            if (!res || !res.success || !res.data) return;

            const pet = res.data;
            const hunger = pet.hunger || 0;
            const hoursLeft = Math.ceil(hunger / 2); // 每小时掉2点

            let hintText = '';
            let petEmoji = '🦊';

            if (hunger < 20) {
                hintText = `你的<strong>${pet.nickname || '灵兽'}</strong>已经<strong style="color:#e74c3c">极度饥饿</strong>了！快回来喂食，否则它的参战效果会大幅降低！`;
            } else if (hunger < 50) {
                hintText = `你的<strong>${pet.nickname || '灵兽'}</strong>有点饿了，大约<strong>${hoursLeft}小时</strong>后需要喂食。`;
            } else {
                hintText = `你的<strong>${pet.nickname || '灵兽'}</strong>目前状态良好，大约<strong>${hoursLeft}小时</strong>后需要回来喂食。`;
            }

            const descEl = document.getElementById('tutorialExitDesc');
            if (descEl) descEl.innerHTML = hintText;

            // 设置离开按钮
            const leaveBtn = document.getElementById('tutorialExitLeaveBtn');
            if (leaveBtn) {
                leaveBtn.onclick = () => this.closeExitModal();
            }

            this.showExitModal();
        } catch (e) {
            // 静默失败
        }
    }

    showExitModal() {
        const modal = document.getElementById('tutorialExitModal');
        if (modal) modal.classList.add('show');
    }

    closeExitModal() {
        const modal = document.getElementById('tutorialExitModal');
        if (modal) modal.classList.remove('show');
    }

    // 跳过全部引导
    skipAll() {
        if (confirm('确定跳过新手引导吗？跳过后将无法重新触发引导流程。')) {
            this.isCompleted = true;
            localStorage.setItem(this.tutorialKey, 'true');
            localStorage.removeItem(this.tutorialStepKey);
            sessionStorage.removeItem('tutorial_pet_fed_once');
            this.clearStepHighlights();
            if (this._highlightTimer) {
                clearInterval(this._highlightTimer);
                this._highlightTimer = null;
            }
            this.destroyUI();
            if (this._pollTimer) clearInterval(this._pollTimer);
            if (this._focusSyncHandler) {
                window.removeEventListener('resize', this._focusSyncHandler);
                window.removeEventListener('scroll', this._focusSyncHandler, true);
                this._focusSyncHandler = null;
            }
        }
    }

    // 重置引导（开发调试用，可通过控制台调用）
    reset() {
        localStorage.removeItem(this.tutorialKey);
        localStorage.removeItem(this.tutorialStepKey);
        sessionStorage.removeItem('tutorial_pet_fed_once');
        this.clearStepHighlights();
        if (this._highlightTimer) {
            clearInterval(this._highlightTimer);
            this._highlightTimer = null;
        }
        this.currentStep = 0;
        this.isCompleted = false;
        this.init();
    }
}

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    // 等待游戏认证完成后再初始化引导
    const tryInit = () => {
        if (window.gameAPI && localStorage.getItem('authToken')) {
            window.tutorialSystem = new TutorialSystem();
        } else {
            setTimeout(tryInit, 500);
        }
    };
    tryInit();
});
