/**
 * 境界突破前端UI系统
 * GDD 3.3: 心魔战斗仪式感机制
 * 对接后端: /api/player/breakthrough, /api/player/breakthrough/can
 */

class BreakthroughSystem {
    constructor() {
        this.canBreakthrough = false;
        this.checking = false;
        this.breakthroughInProgress = false;
        this.createUI();
        this.init();
    }

    async init() {
        await this.checkBreakthroughStatus();
        // 每30秒检查一次突破条件
        setInterval(() => this.checkBreakthroughStatus(), 30000);
    }

    async checkBreakthroughStatus() {
        if (this.checking) return;
        this.checking = true;
        try {
            const res = await gameAPI.canBreakthrough();
            if (res && res.success) {
                const prev = this.canBreakthrough;
                this.canBreakthrough = res.data === true || res.data?.canBreakthrough === true;
                this.updateBreakthroughButton();
                // 首次检测到可突破时，自动弹出提示
                if (!prev && this.canBreakthrough) {
                    this.showBreakthroughAlert();
                }
            }
        } catch (e) {
            // 静默失败
        } finally {
            this.checking = false;
        }
    }

    createUI() {
        // 突破按钮（注入到玩家信息区域）
        this.injectBreakthroughButton();
        // 突破弹窗
        this.createBreakthroughModal();
        // 结果弹窗
        this.createResultModal();
        // 注入样式
        this.injectStyles();
    }

    injectBreakthroughButton() {
        // 在 authManager 渲染玩家信息后插入突破按钮
        const tryInject = () => {
            // 查找玩家信息区或等级展示区
            const levelEl = document.getElementById('playerLevel') || document.getElementById('playerRealm');
            if (levelEl) {
                const existingBtn = document.getElementById('breakthroughBtn');
                if (existingBtn) return;

                const btn = document.createElement('button');
                btn.id = 'breakthroughBtn';
                btn.className = 'breakthrough-btn breakthrough-btn--hidden';
                btn.innerHTML = `<span class="bt-glow"></span>⚡ 冲击境界`;
                btn.onclick = () => this.openBreakthroughModal();
                btn.title = '你已达到境界极限，可以尝试突破！';

                // 找合适的插入位置
                const parent = levelEl.closest('.player-stats, .profile-card, .player-info') || levelEl.parentElement;
                parent.appendChild(btn);
                return true;
            }
            return false;
        };

        if (!tryInject()) {
            const observer = new MutationObserver(() => {
                if (tryInject()) observer.disconnect();
            });
            observer.observe(document.body, { childList: true, subtree: true });
        }
    }

    updateBreakthroughButton() {
        const btn = document.getElementById('breakthroughBtn');
        if (!btn) return;
        if (this.breakthroughInProgress) {
            btn.classList.remove('breakthrough-btn--active');
            btn.classList.remove('breakthrough-btn--hidden');
            btn.disabled = true;
            btn.innerHTML = `<span class="bt-glow"></span>⌛ 突破进行中`;
            return;
        }
        btn.disabled = false;
        btn.innerHTML = `<span class="bt-glow"></span>⚡ 冲击境界`;
        if (this.canBreakthrough) {
            btn.classList.remove('breakthrough-btn--hidden');
            btn.classList.add('breakthrough-btn--active');
        } else {
            btn.classList.remove('breakthrough-btn--active');
            btn.classList.add('breakthrough-btn--hidden');
        }
    }

    showBreakthroughAlert() {
        const existed = document.getElementById('breakthroughAlert');
        if (existed) existed.remove();
        const alert = document.createElement('div');
        alert.id = 'breakthroughAlert';
        alert.className = 'breakthrough-alert';
        alert.innerHTML = `
            <span class="bt-alert-icon">⚡</span>
            <div>
                <div class="bt-alert-title">境界极限已至！</div>
                <div class="bt-alert-sub">点击"冲击境界"按钮进行突破</div>
            </div>
            <button class="bt-alert-go" onclick="window.breakthroughSystem.openBreakthroughModal()">前往突破</button>
            <button onclick="this.parentElement.remove()">✕</button>
        `;
        document.body.appendChild(alert);
        setTimeout(() => alert.remove(), 6000);
    }

    createBreakthroughModal() {
        if (document.getElementById('breakthroughModal')) return;
        const modal = document.createElement('div');
        modal.id = 'breakthroughModal';
        modal.innerHTML = `
            <div class="bt-overlay" onclick="window.breakthroughSystem.closeModal()"></div>
            <div class="bt-modal-content">
                <div class="bt-modal-header">
                    <div class="bt-realm-icon">⚡</div>
                    <h2 class="bt-modal-title">冲击境界</h2>
                    <p class="bt-modal-subtitle">挑战内心的心魔，突破修为壁垒</p>
                </div>

                <div class="bt-lore-box">
                    <p>「每一次境界突破，都是一场与自我内心的较量。</p>
                    <p>心魔不灭，道路不通。」</p>
                    <span class="bt-lore-attr">—— 苏玄清</span>
                </div>

                <div class="bt-info-grid">
                    <div class="bt-info-item">
                        <div class="bt-info-label">突破成功率</div>
                        <div class="bt-info-value bt-success-rate">70%</div>
                    </div>
                    <div class="bt-info-item">
                        <div class="bt-info-label">消耗</div>
                        <div class="bt-info-value">破境丹 ×1 或 5000灵石</div>
                    </div>
                    <div class="bt-info-item">
                        <div class="bt-info-label">成功奖励</div>
                        <div class="bt-info-value bt-reward">全属性大幅提升</div>
                    </div>
                    <div class="bt-info-item">
                        <div class="bt-info-label">失败代价</div>
                        <div class="bt-info-value bt-penalty">1小时冷却期</div>
                    </div>
                </div>

                <div class="bt-warning">
                    ⚠️ 失败后需等待1小时才能重试，请确认自身状态。
                </div>

                <div class="bt-btn-row">
                    <button class="bt-confirm-btn" id="btConfirmBtn" onclick="window.breakthroughSystem.attemptBreakthrough()">
                        <span class="bt-btn-glow"></span>
                        踏入心魔之境 →
                    </button>
                    <button class="bt-cancel-btn" onclick="window.breakthroughSystem.closeModal()">
                        暂不突破
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }

    createResultModal() {
        if (document.getElementById('breakthroughResultModal')) return;
        const modal = document.createElement('div');
        modal.id = 'breakthroughResultModal';
        modal.innerHTML = `
            <div class="bt-overlay"></div>
            <div class="bt-result-content" id="btResultContent">
                <!-- 动态填充 -->
            </div>
        `;
        document.body.appendChild(modal);
    }

    openBreakthroughModal() {
        if (this.breakthroughInProgress) {
            this.showToast('正在突破中，请稍候', 'warning');
            return;
        }
        if (!this.canBreakthrough) {
            this.showToast('当前境界还未达到突破条件', 'warning');
            return;
        }
        document.getElementById('breakthroughModal')?.classList.add('show');
    }

    closeModal() {
        if (this.breakthroughInProgress) {
            return;
        }
        document.getElementById('breakthroughModal')?.classList.remove('show');
    }

    async attemptBreakthrough() {
        if (this.breakthroughInProgress) {
            return;
        }
        this.breakthroughInProgress = true;
        this.updateBreakthroughButton();

        const btn = document.getElementById('btConfirmBtn');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="bt-btn-glow"></span>⌛ 心魔交战中...';
        }

        const cancelBtn = document.querySelector('#breakthroughModal .bt-cancel-btn');
        if (cancelBtn) {
            cancelBtn.disabled = true;
        }

        try {
            // 显示战斗动画
            this.showBreakthroughAnimation();

            // 调用后端API
            const res = await gameAPI.attemptBreakthrough();

            // 关闭突破弹窗
            this.closeModal();

            const normalizedResult = this.normalizeBreakthroughResult(res);
            setTimeout(() => {
                this.showResult(normalizedResult.success, normalizedResult);
            }, 2000);
        } catch (e) {
            this.closeModal();
            this.showToast('突破请求失败: ' + e.message, 'error');
        } finally {
            this.breakthroughInProgress = false;
            this.updateBreakthroughButton();
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = '<span class="bt-btn-glow"></span>踏入心魔之境 →';
            }
            if (cancelBtn) {
                cancelBtn.disabled = false;
            }
        }
    }

    normalizeBreakthroughResult(res) {
        if (!res || !res.success) {
            return {
                success: false,
                message: res?.message || '突破失败'
            };
        }

        const payload = res.data;

        // 兼容后端返回对象结构
        if (payload && typeof payload === 'object') {
            const success = payload.success === true || payload.breakthroughSuccess === true;
            return {
                ...payload,
                success,
                message: payload.message || res.message || (success ? '突破成功！' : '突破失败')
            };
        }

        // 兼容后端仅返回字符串描述
        const text = String(payload || res.message || '突破失败');
        const success = text.includes('突破成功');
        const realmMatch = text.match(/→\s*(.+)$/);

        return {
            success,
            message: text,
            newRealm: realmMatch ? realmMatch[1] : undefined
        };
    }

    showBreakthroughAnimation() {
        const overlay = document.createElement('div');
        overlay.id = 'btAnimOverlay';
        overlay.innerHTML = `
            <div class="bt-anim-center">
                <div class="bt-anim-ring ring1"></div>
                <div class="bt-anim-ring ring2"></div>
                <div class="bt-anim-ring ring3"></div>
                <div class="bt-anim-text">⚡</div>
                <div class="bt-anim-sub">心魔交战中...</div>
            </div>
        `;
        document.body.appendChild(overlay);
        setTimeout(() => overlay.remove(), 2200);
    }

    showResult(success, data) {
        const modal = document.getElementById('breakthroughResultModal');
        const content = document.getElementById('btResultContent');
        if (!modal || !content) return;

        if (success) {
            content.innerHTML = `
                <div class="bt-result-success">
                    <div class="bt-result-firework">🌟</div>
                    <h2 class="bt-result-title bt-result-title--success">境界突破成功！</h2>
                    <p class="bt-result-realm">${data.newRealm || '新境界'}</p>
                    <p class="bt-result-desc">心魔已斩，道路已通。你的修为已踏入新境界！</p>
                    <div class="bt-result-attrs" id="btResultAttrs"></div>
                    <div class="bt-result-npc">
                        <span>👨‍🦳</span>
                        <p>"恭喜师弟突破！此后的路，更需谨慎。" —— 苏玄清</p>
                    </div>
                    <button class="bt-result-btn bt-result-btn--success" onclick="window.breakthroughSystem.closeResult()">
                        踏入新境界 →
                    </button>
                </div>
            `;
            // 填充属性变化
            const attrsEl = document.getElementById('btResultAttrs');
            if (attrsEl && data) {
                const attrs = [];
                if (data.hpIncrease) attrs.push(`❤️ HP +${data.hpIncrease}`);
                if (data.attackIncrease) attrs.push(`⚔️ 攻击 +${data.attackIncrease}`);
                if (data.defenseIncrease) attrs.push(`🛡️ 防御 +${data.defenseIncrease}`);
                if (data.expGained) attrs.push(`✨ 经验 +${data.expGained}`);
                attrsEl.innerHTML = attrs.map(a => '<span class="bt-attr-chip">' + escapeHtml(String(a)) + '</span>').join('');
            }
        } else {
            content.innerHTML = `
                <div class="bt-result-fail">
                    <div class="bt-result-firework">💔</div>
                    <h2 class="bt-result-title bt-result-title--fail">突破失败</h2>
                    <p class="bt-result-desc">${data.message || '心魔过强，此次突破失败。积蓄力量，1小时后可再次尝试。'}</p>
                    <div class="bt-result-npc">
                        <span>👨‍🦳</span>
                        <p>"失败乃成功之母，调整心态，下次必成。" —— 苏玄清</p>
                    </div>
                    <button class="bt-result-btn bt-result-btn--fail" onclick="window.breakthroughSystem.closeResult()">
                        明白，下次再战
                    </button>
                </div>
            `;
        }

        modal.classList.add('show');
        this.canBreakthrough = false;
        this.updateBreakthroughButton();
    }

    async closeResult() {
        const resultContent = document.getElementById('btResultContent');
        const resultTitle = resultContent?.querySelector('.bt-result-title')?.textContent || '';
        const resultRealm = resultContent?.querySelector('.bt-result-realm')?.textContent || '';

        document.getElementById('breakthroughResultModal')?.classList.remove('show');
        // 刷新玩家数据
        if (window.authManager?.loadPlayerProfile) {
            await window.authManager.loadPlayerProfile();
        }
        // 重新检查突破状态
        await this.checkBreakthroughStatus();

        if (resultTitle.includes('成功')) {
            this.showToast(
                this.formatOutcomeToast('突破成功', `新境界：${resultRealm || '未知境界'}`, '道心更进一步'),
                'success'
            );
        }
    }

    showToast(msg, type) {
        if (window.gameManager?.showToast) window.gameManager.showToast(msg, type);
    }

    formatOutcomeToast(title, core, extra = '') {
        return extra ? `${title} | ${core} | ${extra}` : `${title} | ${core}`;
    }

    injectStyles() {
        if (document.getElementById('breakthroughStyles')) return;
        const style = document.createElement('style');
        style.id = 'breakthroughStyles';
        style.textContent = `
            /* 突破按钮 */
            .breakthrough-btn {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                position: relative;
                background: linear-gradient(135deg, #4a1080, #6a1090);
                color: #e0aaff;
                border: 1px solid rgba(180,120,255,0.5);
                border-radius: 8px;
                padding: 8px 16px;
                font-size: 13px;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.3s;
                margin-top: 8px;
                font-family: 'Microsoft YaHei', sans-serif;
                overflow: hidden;
            }
            .breakthrough-btn--hidden { display: none !important; }
            .breakthrough-btn--active {
                display: inline-flex !important;
                animation: btPulse 2s ease infinite;
            }
            @keyframes btPulse {
                0%, 100% { box-shadow: 0 0 8px rgba(180,120,255,0.4); }
                50% { box-shadow: 0 0 20px rgba(180,120,255,0.8), 0 0 40px rgba(180,120,255,0.3); }
            }
            .breakthrough-btn:hover {
                transform: translateY(-2px);
                background: linear-gradient(135deg, #5a1595, #7a20a0);
            }
            .bt-glow {
                position: absolute;
                inset: 0;
                background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
                animation: btGlowSlide 2s linear infinite;
            }
            @keyframes btGlowSlide {
                0% { transform: translateX(-100%); }
                100% { transform: translateX(100%); }
            }

            /* 突破警报 */
            .breakthrough-alert {
                position: fixed;
                top: 20px;
                right: 20px;
                background: linear-gradient(135deg, #4a1080, #6a1090);
                border: 1px solid rgba(180,120,255,0.6);
                border-radius: 12px;
                padding: 14px 20px;
                color: #e0aaff;
                display: flex;
                align-items: center;
                gap: 12px;
                z-index: 9500;
                box-shadow: 0 8px 30px rgba(0,0,0,0.5);
                animation: btAlertIn 0.4s ease;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            @keyframes btAlertIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            .bt-alert-icon { font-size: 24px; }
            .bt-alert-title { font-size: 14px; font-weight: bold; margin-bottom: 2px; }
            .bt-alert-sub { font-size: 12px; opacity: 0.8; }
            .breakthrough-alert button {
                background: none; border: none; color: #e0aaff; cursor: pointer; font-size: 16px; margin-left: 8px;
            }

            /* 突破弹窗 */
            #breakthroughModal, #breakthroughResultModal {
                display: none;
                position: fixed;
                inset: 0;
                z-index: 10050;
                align-items: center;
                justify-content: center;
            }
            #breakthroughModal.show, #breakthroughResultModal.show { display: flex; }
            .bt-overlay {
                position: absolute;
                inset: 0;
                background: rgba(0,0,0,0.8);
                backdrop-filter: blur(6px);
            }
            .bt-modal-content {
                position: relative;
                background: linear-gradient(135deg, #0a0020 0%, #1a0040 100%);
                border: 1px solid rgba(180,120,255,0.5);
                border-radius: 20px;
                padding: 36px;
                max-width: 480px;
                width: 92%;
                box-shadow: 0 0 60px rgba(120,60,200,0.3), 0 20px 60px rgba(0,0,0,0.8);
                animation: btModalIn 0.4s ease;
            }
            @keyframes btModalIn {
                from { opacity: 0; transform: scale(0.9) translateY(-20px); }
                to { opacity: 1; transform: scale(1) translateY(0); }
            }
            .bt-modal-header { text-align: center; margin-bottom: 24px; }
            .bt-realm-icon { font-size: 52px; margin-bottom: 12px; }
            .bt-modal-title {
                font-size: 26px;
                color: #c084fc;
                margin-bottom: 8px;
                text-shadow: 0 0 20px rgba(192,132,252,0.5);
            }
            .bt-modal-subtitle { font-size: 14px; color: rgba(255,255,255,0.5); }
            
            .bt-lore-box {
                background: rgba(120,60,200,0.08);
                border: 1px solid rgba(180,120,255,0.2);
                border-radius: 10px;
                padding: 14px 16px;
                margin-bottom: 20px;
                font-size: 13px;
                color: #aaaacc;
                font-style: italic;
                line-height: 1.8;
            }
            .bt-lore-attr { display: block; text-align: right; color: #c084fc; margin-top: 6px; font-style: normal; }

            .bt-info-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 10px;
                margin-bottom: 16px;
            }
            .bt-info-item {
                background: rgba(255,255,255,0.04);
                border: 1px solid rgba(255,255,255,0.08);
                border-radius: 8px;
                padding: 10px 12px;
            }
            .bt-info-label { font-size: 11px; color: rgba(255,255,255,0.4); margin-bottom: 4px; }
            .bt-info-value { font-size: 13px; color: #e8e8e8; font-weight: bold; }
            .bt-success-rate { color: #2ecc71 !important; font-size: 18px !important; }
            .bt-reward { color: #d4af37 !important; }
            .bt-penalty { color: #e74c3c !important; }

            .bt-warning {
                font-size: 12px;
                color: #f39c12;
                background: rgba(243,156,18,0.08);
                border: 1px solid rgba(243,156,18,0.2);
                border-radius: 8px;
                padding: 8px 12px;
                margin-bottom: 20px;
            }

            .bt-btn-row { display: flex; flex-direction: column; gap: 10px; }
            .bt-confirm-btn {
                position: relative;
                background: linear-gradient(135deg, #6020a0, #8030c0);
                color: #fff;
                border: none;
                border-radius: 10px;
                padding: 14px;
                font-size: 15px;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.2s;
                font-family: 'Microsoft YaHei', sans-serif;
                overflow: hidden;
            }
            .bt-confirm-btn:hover:not(:disabled) {
                background: linear-gradient(135deg, #7030b0, #9040d0);
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(120,60,200,0.5);
            }
            .bt-confirm-btn:disabled { opacity: 0.6; cursor: not-allowed; }
            .bt-btn-glow {
                position: absolute;
                inset: 0;
                background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
                animation: btGlowSlide 1.5s linear infinite;
            }
            .bt-cancel-btn {
                background: none;
                border: 1px solid rgba(255,255,255,0.15);
                color: rgba(255,255,255,0.4);
                border-radius: 10px;
                padding: 12px;
                font-size: 13px;
                cursor: pointer;
                font-family: 'Microsoft YaHei', sans-serif;
            }

            /* 突破动画遮罩 */
            #btAnimOverlay {
                position: fixed;
                inset: 0;
                background: rgba(0,0,0,0.9);
                z-index: 10100;
                display: flex;
                align-items: center;
                justify-content: center;
            }
            .bt-anim-center { position: relative; display: flex; align-items: center; justify-content: center; }
            .bt-anim-ring {
                position: absolute;
                border-radius: 50%;
                border: 2px solid rgba(192,132,252,0.6);
                animation: btRingExpand 2s ease infinite;
            }
            .bt-anim-ring.ring1 { width: 80px; height: 80px; animation-delay: 0s; }
            .bt-anim-ring.ring2 { width: 160px; height: 160px; animation-delay: 0.3s; }
            .bt-anim-ring.ring3 { width: 240px; height: 240px; animation-delay: 0.6s; }
            @keyframes btRingExpand {
                0% { transform: scale(0.5); opacity: 1; }
                100% { transform: scale(1.5); opacity: 0; }
            }
            .bt-anim-text { font-size: 64px; z-index: 1; animation: btAnim 0.5s ease infinite alternate; }
            @keyframes btAnim {
                from { transform: scale(1); }
                to { transform: scale(1.2); }
            }
            .bt-anim-sub {
                position: absolute;
                bottom: -50px;
                font-size: 14px;
                color: #c084fc;
                white-space: nowrap;
                font-family: 'Microsoft YaHei', sans-serif;
            }

            /* 结果弹窗 */
            .bt-result-content {
                position: relative;
                max-width: 420px;
                width: 92%;
                animation: btModalIn 0.4s ease;
            }
            .bt-result-success, .bt-result-fail {
                background: linear-gradient(135deg, #0a0020 0%, #1a0040 100%);
                border-radius: 20px;
                padding: 36px;
                text-align: center;
                box-shadow: 0 20px 60px rgba(0,0,0,0.8);
            }
            .bt-result-success { border: 2px solid rgba(212,175,55,0.6); }
            .bt-result-fail { border: 1px solid rgba(231,76,60,0.4); }
            .bt-result-firework { font-size: 56px; margin-bottom: 12px; }
            .bt-result-title { font-size: 24px; margin-bottom: 8px; }
            .bt-result-title--success { color: #d4af37; }
            .bt-result-title--fail { color: #e74c3c; }
            .bt-result-realm { font-size: 16px; color: #c084fc; margin-bottom: 10px; font-weight: bold; }
            .bt-result-desc { font-size: 14px; color: #e8e8e8; line-height: 1.7; margin-bottom: 16px; }
            .bt-result-attrs {
                display: flex; flex-wrap: wrap; gap: 8px;
                justify-content: center; margin-bottom: 16px;
            }
            .bt-attr-chip {
                background: rgba(212,175,55,0.1);
                border: 1px solid rgba(212,175,55,0.3);
                color: #d4af37;
                border-radius: 6px;
                padding: 4px 12px;
                font-size: 13px;
            }
            .bt-result-npc {
                display: flex; align-items: center; gap: 10px;
                background: rgba(255,255,255,0.03);
                border: 1px solid rgba(255,255,255,0.08);
                border-radius: 10px;
                padding: 10px 14px;
                margin-bottom: 20px;
                text-align: left;
            }
            .bt-result-npc span { font-size: 24px; flex-shrink: 0; }
            .bt-result-npc p { font-size: 12px; color: #aaaacc; font-style: italic; }
            .bt-result-btn {
                width: 100%; border: none; border-radius: 10px; padding: 14px;
                font-size: 14px; font-weight: bold; cursor: pointer;
                font-family: 'Microsoft YaHei', sans-serif;
            }
            .bt-result-btn--success {
                background: linear-gradient(135deg, #d4af37, #b8972e);
                color: #1a1a2e;
            }
            .bt-result-btn--fail {
                background: rgba(231,76,60,0.2);
                border: 1px solid rgba(231,76,60,0.4);
                color: #e74c3c;
            }
        `;
        document.head.appendChild(style);
    }
}

// =====================================================
// 宠物进化前端UI系统
// GDD 7.3: 灵兽蜕变机制
// 对接后端: /api/pets/evolution/*
// =====================================================

class PetEvolutionSystem {
    constructor() {
        this.createUI();
        this.injectEvolutionButton();
    }

    // 在宠物详情弹窗中注入进化按钮
    injectEvolutionButton() {
        // 监听宠物模态框打开
        const observer = new MutationObserver((mutations) => {
            mutations.forEach(m => {
                m.addedNodes.forEach(node => {
                    if (node.nodeType === 1) {
                        const actions = node.querySelector?.('#modalActions') || 
                                       (node.id === 'modalActions' ? node : null);
                        if (actions) {
                            this.addEvolutionButton(actions);
                        }
                    }
                });
            });

            // 也检查现有DOM变更
            const actions = document.getElementById('modalActions');
            if (actions && !actions.querySelector('.pet-evolve-btn')) {
                this.addEvolutionButton(actions);
            }
        });
        observer.observe(document.body, { childList: true, subtree: true, characterData: false });
    }

    addEvolutionButton(actionsEl) {
        if (actionsEl.querySelector('.pet-evolve-btn')) return;

        // 获取当前宠物ID（从已有按钮中解析）
        const existingBtn = actionsEl.querySelector('button[onclick*="feedPet"], button[onclick*="setActivePet"]');
        const match = existingBtn?.getAttribute('onclick')?.match(/\((\d+)\)/);
        if (!match) return;

        const playerPetId = match[1];
        const btn = document.createElement('button');
        btn.className = 'btn btn-warning pet-evolve-btn';
        btn.innerHTML = '🌟 进化';
        btn.onclick = () => this.checkAndShowEvolution(playerPetId);
        actionsEl.appendChild(btn);
    }

    async checkAndShowEvolution(playerPetId) {
        try {
            const res = await gameAPI.checkPetEvolution(playerPetId);
            if (!res || !res.success) {
                this.showToast('检查进化条件失败', 'error');
                return;
            }
            this.showEvolutionModal(playerPetId, res.data);
        } catch (e) {
            this.showToast('无法检查进化条件', 'error');
        }
    }

    createUI() {
        if (document.getElementById('petEvolutionModal')) return;
        const modal = document.createElement('div');
        modal.id = 'petEvolutionModal';
        modal.innerHTML = `
            <div class="pe-overlay" onclick="window.petEvolutionSystem.closeModal()"></div>
            <div class="pe-modal-content">
                <div class="pe-header">
                    <div class="pe-title-icon">🌟</div>
                    <h2 class="pe-title">灵兽蜕变</h2>
                </div>
                <div id="peModalBody">
                    <!-- 动态填充 -->
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        this.injectStyles();
    }

    showEvolutionModal(playerPetId, data) {
        const modal = document.getElementById('petEvolutionModal');
        const body = document.getElementById('peModalBody');
        if (!modal || !body) return;

        const canEvolve = data.canEvolve;
        const pet = {
            playerPetId,
            nickname: data.currentPetNickname || data.currentPetName,
            level: data.currentLevel,
            loyalty: data.currentLoyalty
        };
        const evolution = {
            targetName: data.targetPetName,
            attackBonus: data.attackBonus,
            defenseBonus: data.defenseBonus,
            healthBonus: data.healthBonus,
            speedBonus: data.speedBonus
        };
        const conditions = {
            levelMet: (data.currentLevel || 0) >= (data.requiredLevel || 1),
            loyaltyMet: (data.currentLoyalty || 0) >= (data.requiredLoyalty || 0),
            itemMet: data.hasRequiredItem === true
        };

        body.innerHTML = canEvolve ? `
            <div class="pe-pet-preview">
                <div class="pe-from">
                    <div class="pe-pet-emoji">${this.getPetEmoji(pet.playerPetId)}</div>
                    <div class="pe-pet-name">${pet.nickname || '灵兽'}</div>
                    <div class="pe-pet-level">Lv.${pet.level}</div>
                </div>
                <div class="pe-arrow">→</div>
                <div class="pe-to">
                    <div class="pe-pet-emoji pe-glow">${evolution.targetEmoji || '✨'}</div>
                    <div class="pe-pet-name pe-gold">${evolution.targetName || '进化形态'}</div>
                    <div class="pe-pet-level">全新形态</div>
                </div>
            </div>
            <div class="pe-attr-preview">
                <div class="pe-attr-title">进化后属性加成</div>
                <div class="pe-attr-grid">
                    <span class="pe-attr-item">⚔️ 攻击 +${evolution.attackBonus || '50'}%</span>
                    <span class="pe-attr-item">🛡️ 防御 +${evolution.defenseBonus || '30'}%</span>
                    <span class="pe-attr-item">❤️ HP +${evolution.healthBonus || '40'}%</span>
                    <span class="pe-attr-item">⚡ 速度 +${evolution.speedBonus || '20'}%</span>
                </div>
            </div>
            <div class="pe-new-skill">
                ✨ 解锁新技能：<strong>${evolution.newSkill || '蜕变技能'}</strong>
            </div>
            <div class="pe-cost">
                消耗：<strong>进化丹 ×1</strong>
            </div>
            <div class="pe-btn-row">
                <button class="pe-confirm-btn" onclick="window.petEvolutionSystem.confirmEvolve(${playerPetId})">
                    🌟 开始蜕变
                </button>
                <button class="pe-cancel-btn" onclick="window.petEvolutionSystem.closeModal()">取消</button>
            </div>
        ` : `
            <div class="pe-conditions">
                <div class="pe-conditions-title">进化条件</div>
                ${this.renderCondition(`等级达到 ${data.requiredLevel || 1}`, conditions.levelMet, `当前 Lv.${pet.level || 1}`)}
                ${this.renderCondition(`忠诚度 ≥ ${data.requiredLoyalty || 0}`, conditions.loyaltyMet, `当前 ${pet.loyalty || 0}`)}
                ${this.renderCondition(`持有进化丹 ×${data.requiredItemQuantity || 1}`, conditions.itemMet, conditions.itemMet ? '已满足' : '背包不足')}
            </div>
            <div class="pe-conditions-tip">
                满足以上条件后即可进化，进化道具可从每周任务/宗门活动获得
            </div>
            <button class="pe-cancel-btn" style="width:100%" onclick="window.petEvolutionSystem.closeModal()">我知道了</button>
        `;

        modal.classList.add('show');
    }

    renderCondition(label, met, current) {
        return `
            <div class="pe-condition-item ${met ? 'met' : 'unmet'}">
                <span class="pe-cond-icon">${met ? '✅' : '❌'}</span>
                <span class="pe-cond-label">${label}</span>
                <span class="pe-cond-current">${current}</span>
            </div>
        `;
    }

    async confirmEvolve(playerPetId) {
        const btn = document.querySelector('.pe-confirm-btn');
        if (btn) { btn.disabled = true; btn.textContent = '⌛ 蜕变中...'; }

        try {
            const res = await gameAPI.evolvePet(playerPetId);
            this.closeModal();
            if (res && res.success) {
                this.showEvolutionSuccessAnimation(res.data);
                if (window.loadMyPets) setTimeout(loadMyPets, 1500);
            } else {
                this.showToast(res?.message || '进化失败', 'error');
            }
        } catch (e) {
            this.showToast('进化失败: ' + e.message, 'error');
        }
    }

    showEvolutionSuccessAnimation(data) {
        const overlay = document.createElement('div');
        overlay.className = 'pe-success-overlay';
        overlay.innerHTML = `
            <div class="pe-success-center">
                <div class="pe-success-rings">
                    <div class="pe-ring r1"></div>
                    <div class="pe-ring r2"></div>
                    <div class="pe-ring r3"></div>
                </div>
                <div class="pe-success-emoji">🌟</div>
                <div class="pe-success-text">灵兽蜕变成功！</div>
                <div class="pe-success-name">${data?.newPetName || '新形态'}</div>
            </div>
        `;
        document.body.appendChild(overlay);
        setTimeout(() => overlay.remove(), 3000);
    }

    closeModal() {
        document.getElementById('petEvolutionModal')?.classList.remove('show');
    }

    // 加载宠物列表并显示进化状态
    async loadMyPetsEvolution() {
        const container = document.getElementById('petEvolutionList');
        if (!container) return;

        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载宠物数据...</p></div>';

        try {
            // 获取玩家宠物列表
            const res = await gameAPI.getMyPets();
            if (!res || !res.success || !res.data?.length) {
                container.innerHTML = '<div class="empty-state">你还没有宠物</div>';
                return;
            }

            // 构建宠物卡片列表
            let html = '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:15px;">';
            for (const pet of res.data) {
                const playerPetId = pet.id;
                // 检查进化状态
                let evolutionInfo = '';
                try {
                    const evoRes = await gameAPI.checkPetEvolution(playerPetId);
                    if (evoRes?.success) {
                        const evo = evoRes.data;
                        evolutionInfo = evo.canEvolve
                            ? `<div style="color:#4ade80;margin-top:5px;">✅ ${evo.message}</div>`
                            : `<div style="color:#fbbf24;margin-top:5px;">📋 ${evo.message}</div>`;
                    }
                } catch (e) {}

                html += `
                    <div class="pet-evolution-card">
                        <div style="display:flex;align-items:center;gap:12px;">
                            <div style="font-size:48px;">${this.getPetEmoji(pet.petId)}</div>
                            <div>
                                <div style="font-size:18px;font-weight:bold;color:#d4af37;">${escapeHtml(pet.nickname || '灵兽')}</div>
                                <div style="color:#a0a0a0;">Lv.${pet.level} | 忠诚:${pet.loyalty}</div>
                                <div style="color:#888;font-size:12px;">生命:${pet.health}/${pet.maxHealth} 攻击:${pet.attack} 防御:${pet.defense}</div>
                            </div>
                        </div>
                        ${evolutionInfo}
                        <div style="margin-top:10px;">
                            <button class="btn btn-warning btn-sm" onclick="window.petEvolutionSystem?.checkAndShowEvolution(${playerPetId})">
                                🌟 检查进化
                            </button>
                        </div>
                    </div>
                `;
            }
            html += '</div>';
            container.innerHTML = html;

            // 注入样式（如果尚未注入）
            this.injectStyles();
        } catch (error) {
            container.innerHTML = '<div class="error-state">加载宠物数据失败</div>';
        }
    }

    getPetEmoji(petId) {
        const emojis = { 1: '🦊', 2: '🦄', 3: '🐉', 4: '🐯', 5: '🐢', 6: '🦅', 7: '🐱', 9: '🐺' };
        return emojis[petId] || '🐾';
    }

    showToast(msg, type) {
        if (window.gameManager?.showToast) window.gameManager.showToast(msg, type);
    }

    injectStyles() {
        if (document.getElementById('petEvolutionStyles')) return;
        const style = document.createElement('style');
        style.id = 'petEvolutionStyles';
        style.textContent = `
            #petEvolutionModal {
                display: none; position: fixed; inset: 0; z-index: 10060;
                align-items: center; justify-content: center;
            }
            #petEvolutionModal.show { display: flex; }
            .pe-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.75); backdrop-filter: blur(4px); }
            .pe-modal-content {
                position: relative;
                background: linear-gradient(135deg, #0a1a0a 0%, #1a2a1a 100%);
                border: 1px solid rgba(46,204,113,0.4);
                border-radius: 20px; padding: 32px; max-width: 440px; width: 92%;
                box-shadow: 0 0 40px rgba(46,204,113,0.15), 0 20px 60px rgba(0,0,0,0.8);
                animation: peIn 0.4s ease;
            }
            @keyframes peIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }
            .pe-header { text-align: center; margin-bottom: 24px; }
            .pe-title-icon { font-size: 48px; margin-bottom: 8px; }
            .pe-title { font-size: 24px; color: #2ecc71; }
            
            .pe-pet-preview {
                display: flex; align-items: center; justify-content: center; gap: 20px;
                background: rgba(46,204,113,0.05); border: 1px solid rgba(46,204,113,0.2);
                border-radius: 12px; padding: 20px; margin-bottom: 16px;
            }
            .pe-from, .pe-to { text-align: center; }
            .pe-pet-emoji { font-size: 40px; margin-bottom: 6px; }
            .pe-glow { animation: peGlow 1.5s ease infinite alternate; }
            @keyframes peGlow { from { filter: drop-shadow(0 0 4px #2ecc71); } to { filter: drop-shadow(0 0 12px #2ecc71); } }
            .pe-pet-name { font-size: 14px; color: #e8e8e8; }
            .pe-gold { color: #d4af37 !important; font-weight: bold; }
            .pe-pet-level { font-size: 12px; color: rgba(255,255,255,0.4); }
            .pe-arrow { font-size: 24px; color: #2ecc71; }

            .pe-attr-preview { margin-bottom: 14px; }
            .pe-attr-title { font-size: 12px; color: rgba(255,255,255,0.5); margin-bottom: 8px; }
            .pe-attr-grid { display: flex; flex-wrap: wrap; gap: 6px; }
            .pe-attr-item {
                background: rgba(46,204,113,0.1); border: 1px solid rgba(46,204,113,0.3);
                border-radius: 6px; padding: 4px 10px; font-size: 12px; color: #2ecc71;
            }
            .pe-new-skill {
                background: rgba(127,255,212,0.08); border: 1px solid rgba(127,255,212,0.2);
                border-radius: 8px; padding: 10px 14px; font-size: 13px; color: #7fffd4;
                margin-bottom: 10px;
            }
            .pe-cost { font-size: 13px; color: #f39c12; margin-bottom: 16px; }
            
            .pe-btn-row { display: flex; gap: 10px; }
            .pe-confirm-btn {
                flex: 1; background: linear-gradient(135deg, #27ae60, #2ecc71);
                color: #fff; border: none; border-radius: 10px; padding: 12px;
                font-size: 14px; font-weight: bold; cursor: pointer;
                font-family: 'Microsoft YaHei', sans-serif;
                transition: all 0.2s;
            }
            .pe-confirm-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(46,204,113,0.4); }
            .pe-cancel-btn {
                background: none; border: 1px solid rgba(255,255,255,0.15);
                color: rgba(255,255,255,0.4); border-radius: 10px; padding: 12px 16px;
                cursor: pointer; font-family: 'Microsoft YaHei', sans-serif;
            }
            
            .pe-conditions { margin-bottom: 14px; }
            .pe-conditions-title { font-size: 13px; color: rgba(255,255,255,0.6); margin-bottom: 10px; }
            .pe-condition-item {
                display: flex; align-items: center; gap: 10px;
                padding: 8px 12px; border-radius: 8px; margin-bottom: 6px;
                background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08);
            }
            .pe-condition-item.met { border-color: rgba(46,204,113,0.3); background: rgba(46,204,113,0.05); }
            .pe-condition-item.unmet { border-color: rgba(231,76,60,0.3); background: rgba(231,76,60,0.05); }
            .pe-cond-label { flex: 1; font-size: 13px; color: #e8e8e8; }
            .pe-cond-current { font-size: 12px; color: rgba(255,255,255,0.4); }
            .pe-conditions-tip { font-size: 12px; color: rgba(255,255,255,0.35); margin-bottom: 16px; line-height: 1.6; }

            /* 进化成功动画 */
            .pe-success-overlay {
                position: fixed; inset: 0; background: rgba(0,0,0,0.9); z-index: 10200;
                display: flex; align-items: center; justify-content: center;
            }
            .pe-success-center { position: relative; text-align: center; }
            .pe-success-rings { position: absolute; inset: -100px; display: flex; align-items: center; justify-content: center; }
            .pe-ring {
                position: absolute; border-radius: 50%; border: 2px solid rgba(46,204,113,0.6);
                animation: peRing 2s ease infinite;
            }
            .pe-ring.r1 { width: 100px; height: 100px; animation-delay: 0s; }
            .pe-ring.r2 { width: 200px; height: 200px; animation-delay: 0.3s; }
            .pe-ring.r3 { width: 300px; height: 300px; animation-delay: 0.6s; }
            @keyframes peRing { 0% { transform: scale(0.5); opacity: 1; } 100% { transform: scale(1.5); opacity: 0; } }
            .pe-success-emoji { font-size: 80px; z-index: 1; position: relative; }
            .pe-success-text { font-size: 22px; color: #2ecc71; margin-top: 16px; font-family: 'Microsoft YaHei', sans-serif; }
            .pe-success-name { font-size: 28px; color: #d4af37; font-weight: bold; font-family: 'Microsoft YaHei', sans-serif; }

            /* 宠物进化卡片样式 */
            .pet-evolution-card {
                background: linear-gradient(135deg, rgba(30,30,60,0.9) 0%, rgba(20,20,40,0.95) 100%);
                border: 1px solid rgba(212,175,55,0.3);
                border-radius: 16px;
                padding: 16px;
                margin-bottom: 12px;
                transition: all 0.3s ease;
            }
            .pet-evolution-card:hover {
                border-color: rgba(212,175,55,0.6);
                box-shadow: 0 4px 20px rgba(212,175,55,0.15);
            }
            .pet-evolution-card .btn-sm {
                padding: 6px 16px;
                font-size: 13px;
            }
        `;
        document.head.appendChild(style);
    }
}

// =====================================================
// 初始化所有新系统，并扩展 gameAPI
// =====================================================

// 扩展 gameAPI 添加新接口
document.addEventListener('DOMContentLoaded', () => {
    const tryExtend = () => {
        if (!window.gameAPI) { setTimeout(tryExtend, 300); return; }

        // 境界突破相关
        if (!window.gameAPI.canBreakthrough) {
            window.gameAPI.canBreakthrough = () => window.api.get('/player/breakthrough/can');
        }
        if (!window.gameAPI.attemptBreakthrough) {
            window.gameAPI.attemptBreakthrough = () => window.api.post('/player/breakthrough');
        }

        // 宠物进化相关
        if (!window.gameAPI.checkPetEvolution) {
            window.gameAPI.checkPetEvolution = (playerPetId) => window.api.get(`/pets/evolution/check/${playerPetId}`);
        }
        if (!window.gameAPI.evolvePet) {
            window.gameAPI.evolvePet = (playerPetId) => window.api.post(`/pets/evolution/evolve/${playerPetId}`);
        }
        if (!window.gameAPI.getPetEvolutionInfo) {
            window.gameAPI.getPetEvolutionInfo = (playerPetId) => window.api.get(`/pets/evolution/info/${playerPetId}`);
        }

        // 初始化系统
        if (localStorage.getItem('authToken')) {
            window.breakthroughSystem = new BreakthroughSystem();
            window.petEvolutionSystem = new PetEvolutionSystem();
        }
    };
    tryExtend();
});
