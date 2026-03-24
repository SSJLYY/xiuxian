/**
 * 宠物饱食度倒计时监控系统
 * GDD 8.3: 在主界面显示宠物饱食度倒计时，"6小时后需要喂食"
 * GDD 7.2: 每小时衰减2点饱食度
 */

class PetHungerMonitor {
    constructor() {
        this.pet = null;
        this.updateTimer = null;
        this.pollInterval = 60000; // 每分钟从服务器刷新一次真实数据
        this.localDecayRate = 2 / 60; // 每分钟本地预测衰减量（2点/小时）
        this.lastFetchTime = null;
        this.localHunger = null;

        this.init();
    }

    async init() {
        this.createWidget();
        await this.fetchPetData();
        this.startLocalDecay();
        this.startRemotePolling();
    }

    // 创建悬浮小组件
    createWidget() {
        if (document.getElementById('petHungerWidget')) return;

        const widget = document.createElement('div');
        widget.id = 'petHungerWidget';
        widget.innerHTML = `
            <div class="phw-inner" id="phwInner" onclick="window.petHungerMonitor.openPetPage()">
                <div class="phw-pet-emoji" id="phwEmoji">🦊</div>
                <div class="phw-info">
                    <div class="phw-name" id="phwName">灵兽</div>
                    <div class="phw-bar-wrap">
                        <div class="phw-bar" id="phwBar" style="width:100%"></div>
                    </div>
                    <div class="phw-countdown" id="phwCountdown">状态加载中...</div>
                </div>
                <div class="phw-feed-btn" id="phwFeedBtn" onclick="event.stopPropagation(); window.petHungerMonitor.quickFeed()">
                    🍖
                </div>
            </div>
            <div class="phw-alert" id="phwAlert" style="display:none">
                <span id="phwAlertText">⚠️ 灵兽饥饿！</span>
            </div>
        `;
        document.body.appendChild(widget);
        this.injectStyles();
    }

    injectStyles() {
        if (document.getElementById('petHungerStyles')) return;
        const style = document.createElement('style');
        style.id = 'petHungerStyles';
        style.textContent = `
            /* 宠物饱食度悬浮小组件 */
            #petHungerWidget {
                position: fixed;
                top: 50%;
                right: 0;
                transform: translateY(-50%);
                z-index: 8000;
                transition: all 0.3s ease;
            }

            .phw-inner {
                display: flex;
                align-items: center;
                gap: 8px;
                background: linear-gradient(135deg, rgba(10,10,30,0.95) 0%, rgba(20,20,50,0.95) 100%);
                border: 1px solid rgba(212,175,55,0.4);
                border-right: none;
                border-radius: 12px 0 0 12px;
                padding: 8px 10px 8px 12px;
                cursor: pointer;
                transition: all 0.2s ease;
                box-shadow: -4px 0 20px rgba(0,0,0,0.4);
                min-width: 160px;
            }

            .phw-inner:hover {
                background: linear-gradient(135deg, rgba(15,15,40,0.98) 0%, rgba(25,25,60,0.98) 100%);
                border-color: rgba(212,175,55,0.7);
                transform: translateX(-4px);
                box-shadow: -6px 0 24px rgba(0,0,0,0.5);
            }

            .phw-pet-emoji {
                font-size: 24px;
                flex-shrink: 0;
                transition: transform 0.3s;
            }

            .phw-inner:hover .phw-pet-emoji {
                transform: scale(1.15);
            }

            /* 饥饿时摇摆 */
            @keyframes phwShake {
                0%, 100% { transform: rotate(0deg); }
                25% { transform: rotate(-8deg); }
                75% { transform: rotate(8deg); }
            }
            .phw-pet-emoji.hungry {
                animation: phwShake 1s ease infinite;
            }

            .phw-info {
                flex: 1;
                min-width: 0;
            }

            .phw-name {
                font-size: 12px;
                color: #d4af37;
                font-weight: bold;
                margin-bottom: 4px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                font-family: 'Microsoft YaHei', sans-serif;
            }

            .phw-bar-wrap {
                height: 4px;
                background: rgba(255,255,255,0.1);
                border-radius: 2px;
                overflow: hidden;
                margin-bottom: 4px;
            }

            .phw-bar {
                height: 100%;
                border-radius: 2px;
                transition: width 0.5s ease, background 0.5s ease;
                background: #2ecc71;
            }

            .phw-bar.warn { background: #f39c12; }
            .phw-bar.danger { background: #e74c3c; }

            .phw-countdown {
                font-size: 11px;
                color: rgba(255,255,255,0.55);
                white-space: nowrap;
                font-family: 'Microsoft YaHei', sans-serif;
            }

            .phw-countdown.warn { color: #f39c12; }
            .phw-countdown.danger {
                color: #e74c3c;
                font-weight: bold;
                animation: phwBlink 1s ease infinite;
            }

            @keyframes phwBlink {
                0%, 100% { opacity: 1; }
                50% { opacity: 0.5; }
            }

            .phw-feed-btn {
                font-size: 16px;
                cursor: pointer;
                padding: 4px;
                border-radius: 6px;
                transition: all 0.2s;
                flex-shrink: 0;
                background: rgba(255,255,255,0.05);
                border: 1px solid rgba(255,255,255,0.1);
                width: 28px;
                height: 28px;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .phw-feed-btn:hover {
                background: rgba(46,204,113,0.2);
                border-color: #2ecc71;
                transform: scale(1.1);
            }

            /* 饥饿警报 */
            .phw-alert {
                background: rgba(231,76,60,0.9);
                color: #fff;
                font-size: 12px;
                font-weight: bold;
                padding: 4px 12px;
                border-radius: 0 0 0 8px;
                text-align: center;
                font-family: 'Microsoft YaHei', sans-serif;
                animation: phwBlink 1s ease infinite;
            }

            /* 无宠物状态隐藏 */
            #petHungerWidget.no-pet { display: none; }

            /* 快速喂食动画 */
            @keyframes phwFeedPop {
                0% { transform: scale(1); }
                50% { transform: scale(1.3); }
                100% { transform: scale(1); }
            }
            .phw-pet-emoji.fed {
                animation: phwFeedPop 0.4s ease;
            }

            /* 喂食成功浮字 */
            .phw-float-text {
                position: fixed;
                right: 180px;
                font-size: 14px;
                font-weight: bold;
                color: #2ecc71;
                z-index: 8001;
                pointer-events: none;
                animation: phwFloatUp 1.5s ease forwards;
            }
            @keyframes phwFloatUp {
                0% { opacity: 1; transform: translateY(0); }
                100% { opacity: 0; transform: translateY(-40px); }
            }
        `;
        document.head.appendChild(style);
    }

    // 从服务器获取宠物数据
    async fetchPetData() {
        try {
            const res = await gameAPI.getActivePet();
            if (res && res.success && res.data) {
                this.pet = res.data;
                this.localHunger = this.pet.hunger;
                this.lastFetchTime = Date.now();
                localStorage.setItem('hasPet', 'true');
                this.updateWidget();
                document.getElementById('petHungerWidget')?.classList.remove('no-pet');
            } else {
                // 无出战宠物
                this.pet = null;
                localStorage.setItem('hasPet', 'false');
                document.getElementById('petHungerWidget')?.classList.add('no-pet');
            }
        } catch (e) {
            // 静默失败
        }
    }

    // 启动本地预测衰减（不频繁请求服务器，本地估算）
    startLocalDecay() {
        this.updateTimer = setInterval(() => {
            if (!this.pet || this.localHunger === null) return;

            // 本地预测：每分钟减少 2/60 ≈ 0.033 点
            this.localHunger = Math.max(0, this.localHunger - this.localDecayRate);
            this.updateWidget();
        }, 60000); // 每分钟更新一次
    }

    // 启动服务器轮询（每分钟同步真实数据）
    startRemotePolling() {
        setInterval(() => {
            this.fetchPetData();
        }, this.pollInterval);
    }

    // 更新组件显示
    updateWidget() {
        if (!this.pet) return;

        const hunger = Math.max(0, Math.round(this.localHunger || 0));
        const percent = Math.min(100, hunger);

        // 更新表情
        const emoji = document.getElementById('phwEmoji');
        if (emoji) {
            const emojis = { 1: '🦊', 2: '🦄', 3: '🐉', 4: '🐯', 5: '🐢' };
            emoji.textContent = emojis[this.pet.petId] || '🦊';
            emoji.classList.toggle('hungry', hunger < 20);
        }

        // 更新名字
        const name = document.getElementById('phwName');
        if (name) name.textContent = this.pet.nickname || '灵兽';

        // 更新进度条
        const bar = document.getElementById('phwBar');
        if (bar) {
            bar.style.width = `${percent}%`;
            bar.className = 'phw-bar';
            if (hunger < 20) bar.classList.add('danger');
            else if (hunger < 50) bar.classList.add('warn');
        }

        // 更新倒计时文字
        const countdown = document.getElementById('phwCountdown');
        if (countdown) {
            const hoursLeft = hunger / 2; // 每小时衰减2点
            countdown.className = 'phw-countdown';
            if (hunger === 0) {
                countdown.textContent = '极度饥饿！';
                countdown.classList.add('danger');
            } else if (hunger < 20) {
                countdown.textContent = `饥饿 ${hunger}/100`;
                countdown.classList.add('danger');
            } else if (hunger < 50) {
                const h = Math.floor(hoursLeft);
                const m = Math.round((hoursLeft - h) * 60);
                countdown.textContent = `约${h}h${m > 0 ? m + 'm' : ''}后需喂食`;
                countdown.classList.add('warn');
            } else {
                const h = Math.floor(hoursLeft);
                countdown.textContent = `饱食 ${hunger}/100（约${h}h）`;
            }
        }

        // 警报
        const alert = document.getElementById('phwAlert');
        const alertText = document.getElementById('phwAlertText');
        if (alert && alertText) {
            if (hunger < 20) {
                alertText.textContent = hunger === 0 ? '⚠️ 灵兽断粮了！' : '⚠️ 快去喂食！';
                alert.style.display = 'block';
            } else {
                alert.style.display = 'none';
            }
        }
    }

    // 跳转到宠物页面
    openPetPage() {
        // 尝试SPA内导航
        const petNav = document.querySelector('[data-section="pets"], [href="#pets"], [data-tab="pets"]');
        if (petNav) {
            petNav.click();
            return;
        }
        // 如果是单独的宠物页面
        if (!window.location.pathname.includes('pets')) {
            window.location.href = 'pets.html';
        }
    }

    // 快速喂食
    async quickFeed() {
        if (!this.pet) return;

        const feedBtn = document.getElementById('phwFeedBtn');
        const emoji = document.getElementById('phwEmoji');

        if (feedBtn) feedBtn.textContent = '⌛';
        try {
            const res = await gameAPI.feedPet(this.pet.id);
            if (res && res.success) {
                // 喂食成功动画
                if (emoji) {
                    emoji.classList.add('fed');
                    setTimeout(() => emoji.classList.remove('fed'), 400);
                }

                // 浮字效果
                const widget = document.getElementById('petHungerWidget');
                if (widget) {
                    const float = document.createElement('div');
                    float.className = 'phw-float-text';
                    float.style.top = `${widget.getBoundingClientRect().top}px`;
                    float.textContent = '+20 饱食度';
                    document.body.appendChild(float);
                    setTimeout(() => float.remove(), 1600);
                }

                // 立即刷新数据
                await this.fetchPetData();
                this.showToast('🍖 喂食成功！', 'success');
            } else {
                this.showToast(res?.message || '喂食失败', 'error');
            }
        } catch (e) {
            this.showToast('喂食失败: ' + e.message, 'error');
        } finally {
            if (feedBtn) feedBtn.textContent = '🍖';
        }
    }

    // 轻量级Toast
    showToast(msg, type) {
        if (window.gameManager?.showToast) {
            window.gameManager.showToast(msg, type);
        }
    }

    // 外部调用：宠物数据更新后刷新
    refresh() {
        this.fetchPetData();
    }
}

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    const tryInit = () => {
        if (window.gameAPI && localStorage.getItem('authToken')) {
            window.petHungerMonitor = new PetHungerMonitor();
        } else {
            setTimeout(tryInit, 800);
        }
    };
    tryInit();
});
