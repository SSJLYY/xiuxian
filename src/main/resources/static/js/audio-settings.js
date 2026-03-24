/**
 * 修仙挂机游戏 — 音频设置 UI 系统 (Audio Settings UI)
 * =======================================================
 * 功能：
 *   1. 音量控制面板   — 五路 VCA（总/音乐/音效/UI/环境）滑块
 *   2. 静音切换       — 快捷键 M / 浮动按钮
 *   3. 音频测试       — 点击预览各类音效
 *   4. 开发者调试 HUD — 实时语音数/参数/总线电平可视化
 *   5. 设置持久化      — localStorage 跨会话保存
 *
 * 调试 HUD 指标：
 *   - 活跃语音数 / 最大语音数
 *   - 语音利用率进度条
 *   - 当前音乐参数（战斗强度/境界/健康值）
 *   - 当前混响区域
 *   - AudioContext 状态
 *
 * 快捷键：
 *   M       — 切换静音
 *   Alt+A   — 打开/关闭音频设置面板
 *   Alt+D   — 切换调试 HUD（仅开发模式）
 */

'use strict';

class AudioSettingsUI {
    constructor() {
        this._panelVisible = false;
        this._hudVisible = false;
        this._hudUpdateTimer = null;
        this._devMode = localStorage.getItem('xiuxian_dev_mode') === 'true';

        this._createStyles();
        this._createMuteButton();
        this._createSettingsPanel();
        if (this._devMode) this._createDebugHUD();
        this._bindKeyboardShortcuts();
        this._restoreUIState();

        console.log('[AudioSettingsUI] 音频设置UI初始化完成');
    }

    // ── 样式注入 ─────────────────────────────────────────────────────────────

    _createStyles() {
        if (document.getElementById('audio-settings-styles')) return;
        const style = document.createElement('style');
        style.id = 'audio-settings-styles';
        style.textContent = `
/* ── 静音浮动按钮 ── */
#audioMuteBtn {
    position: fixed;
    bottom: 80px;
    right: 20px;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: rgba(26, 26, 46, 0.92);
    border: 1px solid rgba(212, 175, 55, 0.4);
    color: #d4af37;
    font-size: 18px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9990;
    box-shadow: 0 2px 12px rgba(0,0,0,0.4);
    transition: all 0.2s ease;
    backdrop-filter: blur(8px);
}
#audioMuteBtn:hover {
    background: rgba(212, 175, 55, 0.2);
    border-color: rgba(212, 175, 55, 0.8);
    transform: scale(1.08);
}
#audioMuteBtn.muted {
    color: #ef4444;
    border-color: rgba(239, 68, 68, 0.5);
}

/* ── 音频设置按钮 ── */
#audioSettingsBtn {
    position: fixed;
    bottom: 132px;
    right: 20px;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: rgba(26, 26, 46, 0.92);
    border: 1px solid rgba(127, 255, 212, 0.3);
    color: #7fffd4;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9990;
    box-shadow: 0 2px 12px rgba(0,0,0,0.4);
    transition: all 0.2s ease;
    backdrop-filter: blur(8px);
}
#audioSettingsBtn:hover {
    background: rgba(127, 255, 212, 0.1);
    border-color: rgba(127, 255, 212, 0.7);
    transform: scale(1.08);
}

/* ── 音频设置面板 ── */
#audioSettingsPanel {
    position: fixed;
    bottom: 185px;
    right: 20px;
    width: 280px;
    background: rgba(16, 16, 36, 0.97);
    border: 1px solid rgba(212, 175, 55, 0.35);
    border-radius: 12px;
    padding: 16px;
    z-index: 9989;
    box-shadow: 0 8px 32px rgba(0,0,0,0.6);
    backdrop-filter: blur(12px);
    transform: translateY(10px);
    opacity: 0;
    pointer-events: none;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
#audioSettingsPanel.visible {
    transform: translateY(0);
    opacity: 1;
    pointer-events: all;
}
#audioSettingsPanel .panel-title {
    font-size: 13px;
    font-weight: 600;
    color: #d4af37;
    letter-spacing: 0.5px;
    margin-bottom: 14px;
    display: flex;
    align-items: center;
    gap: 6px;
}
#audioSettingsPanel .panel-title::before {
    content: '🎵';
    font-size: 14px;
}
.audio-volume-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
}
.audio-volume-row .vol-label {
    width: 48px;
    font-size: 11px;
    color: #a0a0c0;
    flex-shrink: 0;
    text-align: right;
}
.audio-volume-row .vol-icon {
    width: 18px;
    text-align: center;
    font-size: 13px;
    flex-shrink: 0;
}
.audio-volume-row input[type="range"] {
    flex: 1;
    height: 4px;
    -webkit-appearance: none;
    appearance: none;
    background: rgba(255,255,255,0.1);
    border-radius: 2px;
    outline: none;
    cursor: pointer;
}
.audio-volume-row input[type="range"]::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: #d4af37;
    cursor: pointer;
    box-shadow: 0 1px 4px rgba(0,0,0,0.3);
    transition: transform 0.1s;
}
.audio-volume-row input[type="range"]::-webkit-slider-thumb:hover {
    transform: scale(1.2);
}
.audio-volume-row .vol-value {
    width: 28px;
    font-size: 11px;
    color: #7fffd4;
    text-align: right;
    flex-shrink: 0;
}
.audio-divider {
    height: 1px;
    background: rgba(255,255,255,0.06);
    margin: 10px 0;
}
.audio-test-row {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    margin-top: 2px;
}
.audio-test-btn {
    padding: 4px 8px;
    background: rgba(127, 255, 212, 0.08);
    border: 1px solid rgba(127, 255, 212, 0.2);
    border-radius: 6px;
    color: #7fffd4;
    font-size: 10px;
    cursor: pointer;
    transition: all 0.15s;
}
.audio-test-btn:hover {
    background: rgba(127, 255, 212, 0.18);
    border-color: rgba(127, 255, 212, 0.5);
}
.audio-test-label {
    font-size: 10px;
    color: #666;
    margin-bottom: 5px;
    margin-top: 2px;
}
.audio-status-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 10px;
    color: #666;
    margin-top: 6px;
}
.audio-status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #ef4444;
    display: inline-block;
    margin-right: 4px;
}
.audio-status-dot.active { background: #10b981; }
.audio-shortcut-hint {
    font-size: 9px;
    color: #444;
    text-align: center;
    margin-top: 8px;
}

/* ── 调试 HUD ── */
#audioDebugHUD {
    position: fixed;
    top: 60px;
    right: 20px;
    width: 220px;
    background: rgba(8, 8, 20, 0.93);
    border: 1px solid rgba(127, 255, 212, 0.25);
    border-radius: 8px;
    padding: 10px 12px;
    z-index: 9991;
    font-family: 'Courier New', monospace;
    font-size: 10px;
    color: #7fffd4;
    line-height: 1.6;
    pointer-events: none;
    box-shadow: 0 4px 16px rgba(0,0,0,0.5);
}
#audioDebugHUD .hud-title {
    color: #d4af37;
    font-weight: bold;
    font-size: 10px;
    margin-bottom: 6px;
    letter-spacing: 1px;
}
#audioDebugHUD .hud-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 2px;
}
#audioDebugHUD .hud-key { color: #a0a0c0; }
#audioDebugHUD .hud-val { color: #7fffd4; }
#audioDebugHUD .hud-val.warn { color: #f59e0b; }
#audioDebugHUD .hud-val.crit { color: #ef4444; }
#audioDebugHUD .hud-bar {
    height: 4px;
    background: rgba(255,255,255,0.1);
    border-radius: 2px;
    margin: 4px 0;
}
#audioDebugHUD .hud-bar-fill {
    height: 100%;
    border-radius: 2px;
    background: #7fffd4;
    transition: width 0.3s;
}
#audioDebugHUD .hud-bar-fill.warn { background: #f59e0b; }
#audioDebugHUD .hud-bar-fill.crit { background: #ef4444; }
#audioDebugHUD .hud-section {
    border-top: 1px solid rgba(255,255,255,0.05);
    margin-top: 5px;
    padding-top: 5px;
}
        `;
        document.head.appendChild(style);
    }

    // ── 静音按钮 ──────────────────────────────────────────────────────────────

    _createMuteButton() {
        const btn = document.createElement('button');
        btn.id = 'audioMuteBtn';
        btn.title = '切换静音 (M)';
        btn.innerHTML = '🔊';
        btn.addEventListener('click', () => this._toggleMute());
        document.body.appendChild(btn);
        this._muteBtn = btn;
    }

    _toggleMute() {
        const audio = window.gameAudio;
        if (!audio) return;
        const muted = audio.toggleMute();
        this._muteBtn.innerHTML = muted ? '🔇' : '🔊';
        this._muteBtn.classList.toggle('muted', muted);
        this._muteBtn.title = muted ? '取消静音 (M)' : '切换静音 (M)';
    }

    // ── 设置按钮 ──────────────────────────────────────────────────────────────

    _createSettingsPanel() {
        // 设置按钮
        const btn = document.createElement('button');
        btn.id = 'audioSettingsBtn';
        btn.title = '音频设置 (Alt+A)';
        btn.innerHTML = '🎚️';
        btn.addEventListener('click', () => this._togglePanel());
        document.body.appendChild(btn);

        // 面板
        const panel = document.createElement('div');
        panel.id = 'audioSettingsPanel';
        panel.innerHTML = this._buildPanelHTML();
        document.body.appendChild(panel);
        this._panel = panel;

        // 绑定滑块事件
        this._bindSliders();
        this._bindTestButtons();
    }

    _buildPanelHTML() {
        const buses = [
            { key: 'master',  label: '总音量', icon: '🎵', settingKey: 'masterVolume' },
            { key: 'music',   label: '音乐',   icon: '🎼', settingKey: 'musicVolume' },
            { key: 'sfx',     label: '音效',   icon: '⚔️', settingKey: 'sfxVolume' },
            { key: 'ui',      label: 'UI',     icon: '🖱️', settingKey: 'uiVolume' },
            { key: 'ambient', label: '环境',   icon: '🌿', settingKey: 'ambientVolume' },
        ];

        const settings = window.gameAudio?.settings || {
            masterVolume: 0.8, musicVolume: 0.7, sfxVolume: 0.9, uiVolume: 0.8, ambientVolume: 0.5
        };

        const sliders = buses.map(b => {
            const val = settings[b.settingKey] !== undefined ? settings[b.settingKey] : 0.8;
            const pct = Math.round(val * 100);
            return `
            <div class="audio-volume-row">
                <span class="vol-icon">${b.icon}</span>
                <span class="vol-label">${b.label}</span>
                <input type="range" min="0" max="100" value="${pct}"
                       data-bus="${b.key}" class="audio-slider">
                <span class="vol-value" data-bus-val="${b.key}">${pct}%</span>
            </div>`;
        }).join('');

        const testButtons = [
            { event: 'sfx/combat/attack_normal',   label: '普通攻击' },
            { event: 'sfx/combat/attack_critical', label: '暴击' },
            { event: 'sfx/player/level_up',        label: '升级' },
            { event: 'sfx/pet/happy',              label: '宠物' },
            { event: 'sfx/economy/coin',           label: '灵石' },
            { event: 'sfx/player/breakthrough_start', label: '突破' },
        ].map(t => `<button class="audio-test-btn" data-test="${t.event}">${t.label}</button>`).join('');

        return `
        <div class="panel-title">音频设置</div>
        ${sliders}
        <div class="audio-divider"></div>
        <div class="audio-test-label">🔈 音效预览</div>
        <div class="audio-test-row">${testButtons}</div>
        <div class="audio-status-row">
            <span>
                <span class="audio-status-dot" id="audioContextStatusDot"></span>
                <span id="audioContextStatusText">未初始化</span>
            </span>
            <span id="audioVoiceCount">语音: 0/32</span>
        </div>
        <div class="audio-shortcut-hint">M 静音 &nbsp;|&nbsp; Alt+A 面板 &nbsp;|&nbsp; Alt+D 调试</div>
        `;
    }

    _bindSliders() {
        const panel = this._panel;
        panel.querySelectorAll('.audio-slider').forEach(slider => {
            slider.addEventListener('input', () => {
                const bus = slider.dataset.bus;
                const value = parseInt(slider.value) / 100;
                window.gameAudio?.setVolume(bus, value);

                const valEl = panel.querySelector(`[data-bus-val="${bus}"]`);
                if (valEl) valEl.textContent = slider.value + '%';
            });
        });
    }

    _bindTestButtons() {
        this._panel.querySelectorAll('.audio-test-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const audio = window.gameAudio;
                if (!audio) return;
                await audio.ensureInit();
                audio.play(btn.dataset.test, { volume: 0.8 });
                // 短暂动画反馈
                btn.style.background = 'rgba(212, 175, 55, 0.3)';
                setTimeout(() => { btn.style.background = ''; }, 200);
            });
        });
    }

    _togglePanel() {
        this._panelVisible = !this._panelVisible;
        this._panel.classList.toggle('visible', this._panelVisible);
        if (this._panelVisible) {
            this._updateStatusBar();
            // 面板打开时启动状态更新
            this._statusUpdateTimer = setInterval(() => this._updateStatusBar(), 1000);
        } else {
            clearInterval(this._statusUpdateTimer);
        }
    }

    _updateStatusBar() {
        const audio = window.gameAudio;
        if (!audio) return;

        const dotEl = document.getElementById('audioContextStatusDot');
        const textEl = document.getElementById('audioContextStatusText');
        const voiceEl = document.getElementById('audioVoiceCount');

        if (!dotEl || !textEl || !voiceEl) return;

        const state = audio._ctx?.state || 'null';
        const initialized = audio._initialized;

        if (initialized && state === 'running') {
            dotEl.className = 'audio-status-dot active';
            textEl.textContent = '运行中';
        } else if (state === 'suspended') {
            dotEl.className = 'audio-status-dot warn';
            textEl.textContent = '已暂停';
        } else {
            dotEl.className = 'audio-status-dot';
            textEl.textContent = '等待交互';
        }

        if (audio.voices) {
            const stats = audio.voices.getStats();
            voiceEl.textContent = `语音: ${stats.active}/${stats.max}`;
        }
    }

    // ── 调试 HUD ──────────────────────────────────────────────────────────────

    _createDebugHUD() {
        if (document.getElementById('audioDebugHUD')) return;

        const hud = document.createElement('div');
        hud.id = 'audioDebugHUD';
        hud.style.display = 'none';
        hud.innerHTML = `
            <div class="hud-title">🎵 AUDIO DEBUG</div>
            <div id="hud-content">等待初始化...</div>
        `;
        document.body.appendChild(hud);
        this._hud = hud;
    }

    _toggleDebugHUD() {
        if (!this._devMode) return;
        this._hudVisible = !this._hudVisible;
        if (!this._hud) this._createDebugHUD();
        this._hud.style.display = this._hudVisible ? 'block' : 'none';

        if (this._hudVisible) {
            this._hudUpdateTimer = setInterval(() => this._updateDebugHUD(), 250);
        } else {
            clearInterval(this._hudUpdateTimer);
        }
    }

    _updateDebugHUD() {
        const audio = window.gameAudio;
        if (!audio || !this._hud) return;

        const info = audio.getDebugInfo();
        const v = info.voices;
        const p = info.musicParams;
        const sp = info.smoothParams;

        const utilNum = parseFloat(v.utilization);
        const utilClass = utilNum > 85 ? 'crit' : utilNum > 65 ? 'warn' : '';
        const barFillClass = utilClass || '';

        const hpClass = p.playerHealth < 0.2 ? 'crit' : p.playerHealth < 0.5 ? 'warn' : '';

        const formatParam = (val, decimals = 2) => {
            if (typeof val === 'boolean') return val ? '<span style="color:#10b981">TRUE</span>' : '<span style="color:#666">FALSE</span>';
            if (typeof val === 'number') return val.toFixed(decimals);
            return val ?? '—';
        };

        document.getElementById('hud-content').innerHTML = `
        <div class="hud-row">
            <span class="hud-key">状态</span>
            <span class="hud-val ${info.contextState !== 'running' ? 'warn' : ''}">${info.contextState.toUpperCase()}</span>
        </div>
        <div class="hud-row">
            <span class="hud-key">采样率</span>
            <span class="hud-val">${info.sampleRate} Hz</span>
        </div>

        <div class="hud-section">
            <div class="hud-row">
                <span class="hud-key">语音 (实体)</span>
                <span class="hud-val ${utilClass}">${v.active} / ${v.max}</span>
            </div>
            <div class="hud-bar">
                <div class="hud-bar-fill ${barFillClass}" style="width:${v.utilization}"></div>
            </div>
            <div class="hud-row">
                <span class="hud-key">总计 / 抢占</span>
                <span class="hud-val">${v.total} / <span class="${v.stolen > 0 ? 'warn' : ''}">${v.stolen}</span></span>
            </div>
            <div class="hud-row">
                <span class="hud-key">虚拟化</span>
                <span class="hud-val ${v.virtual > 5 ? 'warn' : ''}">${v.virtual}</span>
            </div>
        </div>

        <div class="hud-section">
            <div class="hud-row">
                <span class="hud-key">战斗强度</span>
                <span class="hud-val">${formatParam(p.combatIntensity)} <span style="color:#555">(sm:${formatParam(sp.combatIntensity)})</span></span>
            </div>
            <div class="hud-bar">
                <div class="hud-bar-fill ${p.combatIntensity > 0.7 ? 'warn' : ''}" style="width:${(p.combatIntensity * 100).toFixed(0)}%"></div>
            </div>
            <div class="hud-row">
                <span class="hud-key">境界</span>
                <span class="hud-val">${['练气','筑基','金丹','元婴'][p.realmLevel] || p.realmLevel}</span>
            </div>
            <div class="hud-row">
                <span class="hud-key">生命值</span>
                <span class="hud-val ${hpClass}">${formatParam(p.playerHealth)}</span>
            </div>
            <div class="hud-row">
                <span class="hud-key">突破中</span>
                <span class="hud-val">${formatParam(p.inBreakthrough)}</span>
            </div>
        </div>

        <div class="hud-section">
            <div class="hud-row">
                <span class="hud-key">混响区域</span>
                <span class="hud-val">${info.reverbZone}</span>
            </div>
            <div class="hud-row">
                <span class="hud-key">静音</span>
                <span class="hud-val">${formatParam(info.muted)}</span>
            </div>
        </div>
        `;
    }

    // ── 键盘快捷键 ────────────────────────────────────────────────────────────

    _bindKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // M — 静音切换
            if (e.key === 'm' || e.key === 'M') {
                // 避免在输入框中触发
                if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
                this._toggleMute();
            }

            // Alt+A — 音频设置面板
            if (e.altKey && (e.key === 'a' || e.key === 'A')) {
                e.preventDefault();
                this._togglePanel();
            }

            // Alt+D — 调试 HUD（开发者模式）
            if (e.altKey && (e.key === 'd' || e.key === 'D')) {
                e.preventDefault();
                if (!this._devMode) {
                    // 首次 Alt+D 激活开发者模式
                    this._devMode = true;
                    localStorage.setItem('xiuxian_dev_mode', 'true');
                    this._createDebugHUD();
                    console.log('[AudioSettingsUI] 开发者模式已激活');
                }
                this._toggleDebugHUD();
            }
        });
    }

    // ── 状态恢复 ──────────────────────────────────────────────────────────────

    _restoreUIState() {
        // 延迟初始化，等待 DOM 和 AudioEngine 完成
        setTimeout(() => {
            this._updateStatusBar();
            this._syncSlidersFromEngine();
        }, 1500);
    }

    _syncSlidersFromEngine() {
        const settings = window.gameAudio?.settings;
        if (!settings || !this._panel) return;

        const busList = ['master', 'music', 'sfx', 'ui', 'ambient'];
        busList.forEach(bus => {
            const slider = this._panel.querySelector(`[data-bus="${bus}"]`);
            const valEl  = this._panel.querySelector(`[data-bus-val="${bus}"]`);
            const settingKey = bus + 'Volume';
            if (slider && settings[settingKey] !== undefined) {
                const pct = Math.round(settings[settingKey] * 100);
                slider.value = pct;
                if (valEl) valEl.textContent = pct + '%';
            }
        });
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 全局单例 & 自动初始化
// ─────────────────────────────────────────────────────────────────────────────

let audioSettingsUI = null;

const initAudioSettingsUI = () => {
    if (audioSettingsUI) return;
    audioSettingsUI = new AudioSettingsUI();
    window.audioSettingsUI = audioSettingsUI;
};

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAudioSettingsUI);
} else {
    setTimeout(initAudioSettingsUI, 0);
}

console.log('[AudioSettingsUI] audio-settings.js 加载完成');
