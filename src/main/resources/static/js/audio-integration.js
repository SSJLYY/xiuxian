/**
 * 修仙挂机游戏 — 音频集成层 (Audio Integration Layer)
 * ======================================================
 * 职责：将游戏各模块的关键状态变化钩子对接到 GameAudioEngine
 *
 * 架构原则（FMOD 集成规范）：
 *   - 所有音效通过 audioEngine.play(eventPath) 触发，禁止直接调用原生 API
 *   - 所有音乐状态通过 audioEngine.updateGameState() 驱动，不硬编码
 *   - 音频逻辑与游戏逻辑分离：集成层只做映射，不含业务判断
 *   - 零轮询：通过函数劫持（monkey-patch）和 MutationObserver 实现事件驱动
 *
 * 覆盖的触发场景：
 *   战斗系统：攻击/暴击/受击/技能/连招/怪物死亡/胜利/失败
 *   修炼系统：开始/停止/脉冲节拍/升级/境界进度
 *   境界突破：开始心魔战斗/成功/失败
 *   宠物系统：喂食/进化/宠物参战技能触发
 *   经济系统：获得灵石/购买/奖励
 *   UI 系统：所有按钮点击/弹窗开关/错误提示
 *   导航系统：模块切换混响区域
 */

'use strict';

// ─────────────────────────────────────────────────────────────────────────────
// 战斗音频控制器
// ─────────────────────────────────────────────────────────────────────────────

class CombatAudioController {
    constructor() {
        // 战斗状态追踪
        this._inCombat = false;
        this._currentEnemyCount = 0;
        this._combatStartTime = 0;
        this._consecutiveHits = 0;
        this._lastAttackTime = 0;

        // 连击计数器重置窗口（3秒）
        this._comboResetMs = 3000;

        this._patchCombatFunctions();
        this._observeCombatLog();
        console.log('[AudioIntegration] CombatAudioController 已初始化');
    }

    /**
     * 劫持战斗相关函数，注入音效
     */
    _patchCombatFunctions() {
        // 劫持 enhanced_combat.js 中的 displayCombatResult
        const self = this;
        const origDisplay = window.displayCombatResult;
        if (typeof origDisplay === 'function') {
            window.displayCombatResult = function(result) {
                self._onCombatResult(result);
                return origDisplay.apply(this, arguments);
            };
        }

        // 劫持生成怪物
        const origGenerate = window.generateMonster;
        if (typeof origGenerate === 'function') {
            window.generateMonster = function(...args) {
                self._onMonsterGenerated();
                return origGenerate.apply(this, args);
            };
        }
    }

    /**
     * 处理战斗结果（核心触发点）
     */
    _onCombatResult(result) {
        if (!result) return;
        const audio = window.gameAudio;
        if (!audio) return;

        const now = Date.now();

        // 进入/维持战斗状态
        if (!this._inCombat) {
            this._inCombat = true;
            this._combatStartTime = now;
            audio.updateGameState({ combatIntensity: 0.65 });
        }

        // 解析战斗日志
        const rounds = result.battleLog || result.combatLog || [];
        rounds.forEach((log, i) => {
            const delay = i * 120; // 错开音效，避免同帧叠加

            setTimeout(() => {
                if (!window.gameAudio) return;

                // 玩家攻击
                if (log.playerDamage > 0) {
                    const isCrit = log.isCritical || (log.playerDamage > (log.expectedDamage || 0) * 1.5);
                    if (isCrit) {
                        audio.play('sfx/combat/attack_critical', { priority: 1, volume: 0.9 });
                    } else {
                        audio.play('sfx/combat/attack_normal', { priority: 1, volume: 0.7 });
                    }

                    // 连击检测
                    const timeSinceLast = now - this._lastAttackTime;
                    if (timeSinceLast < this._comboResetMs) {
                        this._consecutiveHits++;
                        if (this._consecutiveHits >= 3) {
                            setTimeout(() => {
                                audio.play('sfx/combat/combo_trigger', {
                                    priority: 1,
                                    volume: 0.8,
                                    comboCount: Math.min(this._consecutiveHits, 10)
                                });
                            }, 80);
                        }
                    } else {
                        this._consecutiveHits = 1;
                    }
                    this._lastAttackTime = now;
                }

                // 玩家受击
                if (log.monsterDamage > 0) {
                    audio.play('sfx/combat/hit_taken', { priority: 1, volume: 0.6 });
                }

                // 技能使用
                if (log.skillUsed) {
                    const tier = log.skillTier || 1;
                    audio.play('sfx/combat/skill_cast', { priority: 1, volume: 0.8, tier });
                }

            }, delay);
        });

        // 战斗结束处理
        if (result.playerWon === true || result.victory === true) {
            this._onCombatVictory(result);
        } else if (result.playerWon === false || result.defeat === true) {
            this._onCombatDefeat(result);
        }

        // 怪物死亡
        if (result.monsterDefeated || result.playerWon) {
            const delay = (rounds.length * 120) + 100;
            setTimeout(() => {
                if (window.gameAudio) {
                    window.gameAudio.play('sfx/combat/monster_die', { priority: 2, volume: 0.8 });
                }
            }, delay);
        }

        // 灵石奖励音效
        if (result.spiritStones > 0 || result.rewards?.spiritStones > 0) {
            const coins = result.spiritStones || result.rewards?.spiritStones || 0;
            const coinCount = Math.min(Math.floor(coins / 50) + 1, 5);
            for (let c = 0; c < coinCount; c++) {
                setTimeout(() => {
                    if (window.gameAudio) {
                        window.gameAudio.play('sfx/economy/coin', { priority: 3, volume: 0.5 });
                    }
                }, (rounds.length * 120) + 200 + c * 80);
            }
        }
    }

    _onCombatVictory(result) {
        const audio = window.gameAudio;
        if (!audio) return;

        // 战斗结束，降低紧张度
        setTimeout(() => {
            audio.updateGameState({ combatIntensity: 0.1 });
            this._inCombat = false;
            this._consecutiveHits = 0;
        }, 500);

        // 升级音效
        if (result.levelUp || result.playerLeveledUp) {
            setTimeout(() => {
                audio.play('sfx/player/level_up', { priority: 0, volume: 1.0 });
            }, 800);
        }
    }

    _onCombatDefeat(result) {
        const audio = window.gameAudio;
        if (!audio) return;
        setTimeout(() => {
            audio.updateGameState({ combatIntensity: 0.0 });
            this._inCombat = false;
        }, 300);
    }

    _onMonsterGenerated() {
        const audio = window.gameAudio;
        if (!audio) return;
        // 检测到新怪物：短暂提升紧张度
        audio.updateGameState({ combatIntensity: 0.4 });
    }

    /**
     * 观察战斗日志 DOM，处理无回调的异步场景
     */
    _observeCombatLog() {
        const target = document.getElementById('battleLog') || document.getElementById('combatLog');
        if (!target) {
            // 延迟重试
            setTimeout(() => this._observeCombatLog(), 2000);
            return;
        }

        const observer = new MutationObserver((mutations) => {
            mutations.forEach(m => {
                m.addedNodes.forEach(node => {
                    if (node.nodeType !== 1) return;
                    const text = node.textContent || '';
                    // 从日志文本启发式触发音效（作为回调触发的补充）
                    if (text.includes('暴击') || text.includes('会心')) {
                        window.gameAudio?.play('sfx/combat/attack_critical', { priority: 1, volume: 0.75 });
                    } else if (text.includes('闪避') || text.includes('未命中')) {
                        // 闪避不触发音效，只有视觉反馈
                    }
                });
            });
        });

        observer.observe(target, { childList: true, subtree: true });
    }

    /** 外部调用：手动设置战斗强度 */
    setCombatIntensity(value) {
        window.gameAudio?.updateGameState({ combatIntensity: Math.max(0, Math.min(1, value)) });
    }

    /** 外部调用：离开战斗 */
    exitCombat() {
        this._inCombat = false;
        this._consecutiveHits = 0;
        window.gameAudio?.updateGameState({ combatIntensity: 0.0 });
        window.gameAudio?.play('music/exploration/start');
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 修炼音频控制器
// ─────────────────────────────────────────────────────────────────────────────

class CultivationAudioController {
    constructor() {
        this._isCultivating = false;
        this._pulseTimer = null;
        this._pulseInterval = 4000; // 4秒一次呼吸脉冲

        this._patchCultivationFunctions();
        console.log('[AudioIntegration] CultivationAudioController 已初始化');
    }

    _patchCultivationFunctions() {
        // 劫持 GameManager 的修炼方法
        const self = this;

        // 等待 GameManager 实例化
        const tryPatch = () => {
            const gm = window.gameManager;
            if (!gm) {
                setTimeout(tryPatch, 500);
                return;
            }

            // 修炼开始
            const origStart = gm.startCultivation?.bind(gm);
            if (origStart) {
                gm.startCultivation = async function(...args) {
                    self._onCultivationStart();
                    return origStart(...args);
                };
            }

            // 修炼停止
            const origStop = gm.stopCultivation?.bind(gm);
            if (origStop) {
                gm.stopCultivation = async function(...args) {
                    self._onCultivationStop();
                    return origStop(...args);
                };
            }

            // 修炼周期完成（每10秒）
            const origUpdate = gm.updateCultivationStatus?.bind(gm);
            if (origUpdate) {
                gm.updateCultivationStatus = function(msg, ...args) {
                    // 每周期一次脉冲音效（通过状态消息触发）
                    if (msg && msg !== '点击开始修炼' && self._isCultivating) {
                        // 不在这里触发，由定时器统一触发
                    }
                    return origUpdate(msg, ...args);
                };
            }
        };
        tryPatch();
    }

    _onCultivationStart() {
        if (this._isCultivating) return;
        this._isCultivating = true;

        const audio = window.gameAudio;
        if (!audio) return;

        // 修炼状态：降低战斗紧张度，切换到冥想模式
        audio.updateGameState({ combatIntensity: 0.0 });
        audio.setReverbZone('void'); // 虚空混响

        // 播放修炼开始音效（轻柔上行音）
        audio.play('sfx/player/cultivation_pulse', { priority: 2, volume: 0.35 });

        // 启动周期性脉冲
        this._startPulseTimer();

        console.log('[AudioIntegration] 修炼开始，音频状态切换为冥想模式');
    }

    _onCultivationStop() {
        if (!this._isCultivating) return;
        this._isCultivating = false;

        this._stopPulseTimer();

        const audio = window.gameAudio;
        if (!audio) return;

        // 恢复室外混响
        audio.setReverbZone('outdoor');

        console.log('[AudioIntegration] 修炼停止，恢复默认音频状态');
    }

    _startPulseTimer() {
        this._stopPulseTimer();
        this._pulseTimer = setInterval(() => {
            if (!this._isCultivating) return;
            window.gameAudio?.play('sfx/player/cultivation_pulse', {
                priority: 3,
                volume: 0.2 + Math.random() * 0.1,
            });
        }, this._pulseInterval);
    }

    _stopPulseTimer() {
        if (this._pulseTimer) {
            clearInterval(this._pulseTimer);
            this._pulseTimer = null;
        }
    }

    /** 外部调用：同步修炼状态 */
    syncState(isCultivating) {
        if (isCultivating && !this._isCultivating) this._onCultivationStart();
        else if (!isCultivating && this._isCultivating) this._onCultivationStop();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 境界突破音频控制器
// ─────────────────────────────────────────────────────────────────────────────

class BreakthroughAudioController {
    constructor() {
        this._patchBreakthroughSystem();
        console.log('[AudioIntegration] BreakthroughAudioController 已初始化');
    }

    _patchBreakthroughSystem() {
        const self = this;
        const tryPatch = () => {
            const bs = window.breakthroughSystem;
            if (!bs) {
                setTimeout(tryPatch, 1000);
                return;
            }

            // 劫持突破尝试方法
            const origAttempt = bs.attemptBreakthrough?.bind(bs);
            if (origAttempt) {
                bs.attemptBreakthrough = async function(...args) {
                    self._onBreakthroughAttempt();
                    const result = await origAttempt(...args);
                    return result;
                };
            }

            // 劫持结果显示
            const origShowResult = bs.showResult?.bind(bs);
            if (origShowResult) {
                bs.showResult = function(success, ...args) {
                    self._onBreakthroughResult(success);
                    return origShowResult(success, ...args);
                };
            }

            // 劫持可突破提示
            const origAlert = bs.showBreakthroughAlert?.bind(bs);
            if (origAlert) {
                bs.showBreakthroughAlert = function(...args) {
                    self._onBreakthroughReady();
                    return origAlert(...args);
                };
            }
        };
        tryPatch();
    }

    _onBreakthroughReady() {
        const audio = window.gameAudio;
        if (!audio) return;
        // 可突破提示：神秘提示音
        audio.play('sfx/combat/skill_cast', { priority: 0, volume: 0.6, tier: 3 });
    }

    _onBreakthroughAttempt() {
        const audio = window.gameAudio;
        if (!audio) return;

        // 切换到突破音乐
        audio.updateGameState({ inBreakthrough: true });
        audio.setReverbZone('void');
        audio.play('sfx/player/breakthrough_start', { priority: 0, volume: 1.0 });

        console.log('[AudioIntegration] 境界突破开始，音乐切换为突破轨道');
    }

    _onBreakthroughResult(success) {
        const audio = window.gameAudio;
        if (!audio) return;

        // 延迟600ms等待视觉效果
        setTimeout(() => {
            if (success) {
                audio.play('sfx/player/breakthrough_success', { priority: 0, volume: 1.0 });
                // 突破成功：升级音乐境界
                const newRealm = Math.min((audio._gameState.realmLevel || 0) + 1, 3);
                audio.updateGameState({
                    inBreakthrough: false,
                    realmLevel: newRealm,
                    combatIntensity: 0.0,
                });
                console.log('[AudioIntegration] 境界突破成功！新境界等级:', newRealm);
            } else {
                audio.play('sfx/player/breakthrough_fail', { priority: 0, volume: 0.9 });
                audio.updateGameState({
                    inBreakthrough: false,
                    combatIntensity: 0.0,
                });
                console.log('[AudioIntegration] 境界突破失败');
            }
            audio.setReverbZone('outdoor');
        }, 600);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 宠物音频控制器
// ─────────────────────────────────────────────────────────────────────────────

class PetAudioController {
    constructor() {
        this._patchPetFunctions();
        console.log('[AudioIntegration] PetAudioController 已初始化');
    }

    _patchPetFunctions() {
        const self = this;

        // 劫持 PetEvolutionSystem
        const tryPatchEvolution = () => {
            const pes = window.petEvolutionSystem;
            if (!pes) {
                setTimeout(tryPatchEvolution, 1000);
                return;
            }

            const origEvolve = pes.triggerEvolution?.bind(pes);
            if (origEvolve) {
                pes.triggerEvolution = async function(petId, ...args) {
                    self._onPetEvolutionStart();
                    const result = await origEvolve(petId, ...args);
                    if (result?.success) self._onPetEvolutionSuccess();
                    return result;
                };
            }
        };
        tryPatchEvolution();

        // 劫持全局喂食函数
        const origFeed = window.feedPet;
        if (typeof origFeed === 'function') {
            window.feedPet = function(...args) {
                self._onPetFeed();
                return origFeed.apply(this, args);
            };
        }

        // API 层喂食拦截（通过 MutationObserver 观察宠物饱食度变化）
        this._observePetHunger();
    }

    _observePetHunger() {
        const tryObserve = () => {
            const hungerEl = document.getElementById('petHunger') || document.querySelector('[data-pet-hunger]');
            if (!hungerEl) {
                setTimeout(tryObserve, 3000);
                return;
            }

            let lastHunger = parseFloat(hungerEl.textContent || '0');
            const observer = new MutationObserver(() => {
                const newHunger = parseFloat(hungerEl.textContent || '0');
                if (newHunger > lastHunger + 5) {
                    // 饱食度上升：喂食成功
                    this._onPetFeed();
                }
                lastHunger = newHunger;
            });
            observer.observe(hungerEl, { characterData: true, subtree: true, childList: true });
        };
        tryObserve();
    }

    _onPetFeed() {
        window.gameAudio?.play('sfx/pet/happy', { priority: 2, volume: 0.6 });
    }

    _onPetEvolutionStart() {
        // 进化开始：播放技能合成音
        window.gameAudio?.play('sfx/combat/skill_cast', { priority: 0, volume: 0.9, tier: 4 });
    }

    _onPetEvolutionSuccess() {
        setTimeout(() => {
            window.gameAudio?.play('sfx/pet/evolve', { priority: 0, volume: 1.0 });
        }, 400);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UI 音频控制器
// ─────────────────────────────────────────────────────────────────────────────

class UIAudioController {
    constructor() {
        this._bindGlobalClick();
        this._bindModalEvents();
        this._bindToastEvents();
        this._patchShowModule();
        console.log('[AudioIntegration] UIAudioController 已初始化');
    }

    /**
     * 全局点击音效（基于 CSS 类名启发式判断）
     */
    _bindGlobalClick() {
        document.addEventListener('click', (e) => {
            const audio = window.gameAudio;
            if (!audio) return;

            const target = e.target.closest('button, .btn, [role="button"], .nav-item, .tab-btn');
            if (!target) return;

            // 跳过音频设置面板自身的按钮（避免递归）
            if (target.closest('#audioSettingsPanel')) return;

            const cls = target.className || '';
            const text = (target.textContent || '').trim();

            if (cls.includes('btn-danger') || text === '关闭' || text === '取消') {
                audio.play('ui/ui/close', { priority: 0 });
            } else if (cls.includes('btn-success') || cls.includes('btn-primary')) {
                audio.play('ui/ui/click', { priority: 0, volume: 0.4 });
            } else if (target.closest('.nav-item') || target.closest('.tab-btn')) {
                audio.play('ui/ui/open', { priority: 0, volume: 0.3 });
            } else {
                audio.play('ui/ui/click', { priority: 0, volume: 0.25 });
            }
        }, { passive: true });
    }

    /**
     * 弹窗打开/关闭音效
     */
    _bindModalEvents() {
        const observer = new MutationObserver((mutations) => {
            mutations.forEach(m => {
                m.addedNodes.forEach(node => {
                    if (node.nodeType !== 1) return;
                    if (node.classList?.contains('modal') ||
                        node.classList?.contains('popup') ||
                        node.id?.toLowerCase().includes('modal')) {
                        window.gameAudio?.play('ui/ui/open', { priority: 0 });
                    }
                });
                m.removedNodes.forEach(node => {
                    if (node.nodeType !== 1) return;
                    if (node.classList?.contains('modal') || node.classList?.contains('popup')) {
                        window.gameAudio?.play('ui/ui/close', { priority: 0 });
                    }
                });
            });
        });
        observer.observe(document.body, { childList: true, subtree: false });
    }

    /**
     * Toast 提示音效
     */
    _bindToastEvents() {
        const origShowToast = window.showToast;
        if (typeof origShowToast === 'function') {
            window.showToast = function(message, type, ...args) {
                const audio = window.gameAudio;
                if (audio) {
                    if (type === 'error') {
                        audio.play('ui/ui/error', { priority: 0 });
                    } else if (type === 'success') {
                        audio.play('ui/ui/reward', { priority: 0, volume: 0.5 });
                    }
                    // info/warning 使用默认 click，不额外播放
                }
                return origShowToast.apply(this, [message, type, ...args]);
            };
        }
    }

    /**
     * 模块切换时更新混响区域
     */
    _patchShowModule() {
        const origShowModule = window.showModule;
        if (typeof origShowModule !== 'function') {
            setTimeout(() => this._patchShowModule(), 1000);
            return;
        }

        // 模块→混响区域映射
        const moduleReverbMap = {
            'dashboard':    'indoor',
            'combat':       'outdoor',
            'pets':         'outdoor',
            'guild':        'indoor',
            'inventory':    'indoor',
            'shop':         'indoor',
            'skills':       'void',
            'quests':       'indoor',
            'narrative':    'cave',
            'lore':         'cave',
            'ranking':      'indoor',
            'achievements': 'indoor',
            'map':          'outdoor',
        };

        window.showModule = function(moduleName, ...args) {
            const audio = window.gameAudio;
            if (audio) {
                const zone = moduleReverbMap[moduleName] || 'indoor';
                audio.setReverbZone(zone);
                // 非战斗模块：重置战斗紧张度
                if (moduleName !== 'combat') {
                    // 不直接重置，让战斗控制器管理
                }
            }
            return origShowModule.apply(this, [moduleName, ...args]);
        };
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 经济/奖励音频控制器
// ─────────────────────────────────────────────────────────────────────────────

class EconomyAudioController {
    constructor() {
        this._patchEconomyAPIs();
        this._observeRewardPanels();
        console.log('[AudioIntegration] EconomyAudioController 已初始化');
    }

    _patchEconomyAPIs() {
        // 观察灵石数量变化
        const tryObserveStones = () => {
            const stonesEl = document.getElementById('spiritStones') ||
                             document.querySelector('[data-spirit-stones]') ||
                             document.getElementById('playerSpiritStones');
            if (!stonesEl) {
                setTimeout(tryObserveStones, 2000);
                return;
            }

            let lastStones = parseInt(stonesEl.textContent || '0');
            const observer = new MutationObserver(() => {
                const newStones = parseInt(stonesEl.textContent || '0');
                const diff = newStones - lastStones;
                if (diff > 0) {
                    // 灵石增加
                    const coinCount = Math.min(Math.ceil(diff / 100), 4);
                    for (let i = 0; i < coinCount; i++) {
                        setTimeout(() => {
                            window.gameAudio?.play('sfx/economy/coin', { priority: 3, volume: 0.45 });
                        }, i * 70);
                    }
                }
                lastStones = newStones;
            });
            observer.observe(stonesEl, { characterData: true, subtree: true, childList: true });
        };
        tryObserveStones();
    }

    _observeRewardPanels() {
        // 监听离线奖励面板、每日签到面板出现
        const observer = new MutationObserver((mutations) => {
            mutations.forEach(m => {
                m.addedNodes.forEach(node => {
                    if (node.nodeType !== 1) return;
                    const id = node.id || '';
                    const cls = (node.className || '').toString();
                    if (id.includes('reward') || id.includes('offline') ||
                        cls.includes('reward') || cls.includes('offline-reward')) {
                        window.gameAudio?.play('sfx/player/level_up', { priority: 1, volume: 0.8 });
                    }
                });
            });
        });
        observer.observe(document.body, { childList: true, subtree: true });
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 玩家状态同步控制器
// ─────────────────────────────────────────────────────────────────────────────

class PlayerStateAudioSync {
    /**
     * 定期从 DOM 同步玩家状态到音频引擎
     * 同步内容：生命值、境界等级
     */
    constructor() {
        this._syncInterval = null;
        this._lastHealth = 1.0;
        this._lastRealm = 0;
        this._startSync();
        console.log('[AudioIntegration] PlayerStateAudioSync 已初始化');
    }

    _startSync() {
        // 每2秒同步一次（低开销）
        this._syncInterval = setInterval(() => this._sync(), 2000);
    }

    _sync() {
        const audio = window.gameAudio;
        if (!audio || !audio._initialized) return;

        const stateUpdate = {};

        // 同步生命值
        const hpEl = document.getElementById('playerHp') ||
                     document.getElementById('playerHealth') ||
                     document.querySelector('[data-player-hp]');
        const maxHpEl = document.getElementById('playerMaxHp') ||
                        document.querySelector('[data-player-max-hp]');
        if (hpEl && maxHpEl) {
            const hp = parseFloat(hpEl.textContent || '100');
            const maxHp = parseFloat(maxHpEl.textContent || '100');
            if (maxHp > 0) {
                const healthRatio = Math.max(0, Math.min(1, hp / maxHp));
                if (Math.abs(healthRatio - this._lastHealth) > 0.05) {
                    stateUpdate.playerHealth = healthRatio;
                    this._lastHealth = healthRatio;
                }
            }
        }

        // 同步境界
        const realmEl = document.getElementById('playerRealm') ||
                        document.querySelector('[data-realm]');
        if (realmEl) {
            const realmText = realmEl.textContent || '';
            const realmMap = { '练气': 0, '筑基': 1, '金丹': 2, '元婴': 3 };
            let realmLevel = 0;
            for (const [key, val] of Object.entries(realmMap)) {
                if (realmText.includes(key)) { realmLevel = val; break; }
            }
            if (realmLevel !== this._lastRealm) {
                stateUpdate.realmLevel = realmLevel;
                this._lastRealm = realmLevel;
            }
        }

        if (Object.keys(stateUpdate).length > 0) {
            audio.updateGameState(stateUpdate);
        }
    }

    stop() {
        if (this._syncInterval) {
            clearInterval(this._syncInterval);
            this._syncInterval = null;
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 主集成协调器（入口）
// ─────────────────────────────────────────────────────────────────────────────

class AudioIntegrationManager {
    /**
     * 统一管理所有音频控制器的生命周期
     */
    constructor() {
        this.controllers = {};
        this._initialized = false;
    }

    /**
     * 初始化所有控制器
     * 在 DOMContentLoaded 后调用
     */
    init() {
        if (this._initialized) return;
        this._initialized = true;

        try {
            this.controllers.combat       = new CombatAudioController();
            this.controllers.cultivation  = new CultivationAudioController();
            this.controllers.breakthrough = new BreakthroughAudioController();
            this.controllers.pet          = new PetAudioController();
            this.controllers.ui           = new UIAudioController();
            this.controllers.economy      = new EconomyAudioController();
            this.controllers.playerSync   = new PlayerStateAudioSync();

            console.log('[AudioIntegration] 音频集成层初始化完成，共', Object.keys(this.controllers).length, '个控制器');
        } catch (err) {
            console.error('[AudioIntegration] 初始化失败:', err);
        }
    }

    /**
     * 获取指定控制器
     */
    getController(name) {
        return this.controllers[name];
    }

    /**
     * 触发游戏事件（供游戏系统直接调用的语义化 API）
     *
     * 事件类型：
     *   'combat:start'           — 战斗开始
     *   'combat:end'             — 战斗结束
     *   'combat:intensity'       — 设置战斗强度 { value: 0-1 }
     *   'cultivation:start'      — 开始修炼
     *   'cultivation:stop'       — 停止修炼
     *   'breakthrough:attempt'   — 尝试突破
     *   'breakthrough:success'   — 突破成功
     *   'breakthrough:fail'      — 突破失败
     *   'pet:feed'               — 喂食宠物
     *   'pet:evolve'             — 宠物进化
     *   'player:level_up'        — 升级
     *   'player:health'          — 生命值变化 { ratio: 0-1 }
     *   'reward:offline'         — 领取离线奖励
     */
    emit(eventType, data = {}) {
        const audio = window.gameAudio;
        if (!audio) return;

        switch (eventType) {
            case 'combat:start':
                audio.updateGameState({ combatIntensity: 0.6 });
                break;
            case 'combat:end':
                this.controllers.combat?.exitCombat();
                break;
            case 'combat:intensity':
                this.controllers.combat?.setCombatIntensity(data.value || 0);
                break;
            case 'cultivation:start':
                this.controllers.cultivation?.syncState(true);
                break;
            case 'cultivation:stop':
                this.controllers.cultivation?.syncState(false);
                break;
            case 'breakthrough:attempt':
                this.controllers.breakthrough?._onBreakthroughAttempt();
                break;
            case 'breakthrough:success':
                this.controllers.breakthrough?._onBreakthroughResult(true);
                break;
            case 'breakthrough:fail':
                this.controllers.breakthrough?._onBreakthroughResult(false);
                break;
            case 'pet:feed':
                this.controllers.pet?._onPetFeed();
                break;
            case 'pet:evolve':
                this.controllers.pet?._onPetEvolutionSuccess();
                break;
            case 'player:level_up':
                audio.play('sfx/player/level_up', { priority: 0, volume: 1.0 });
                break;
            case 'player:health':
                audio.updateGameState({ playerHealth: data.ratio || 1.0 });
                break;
            case 'reward:offline':
                audio.play('sfx/player/level_up', { priority: 1, volume: 0.85 });
                setTimeout(() => {
                    for (let i = 0; i < 3; i++) {
                        setTimeout(() => audio.play('sfx/economy/coin', { priority: 3, volume: 0.4 }), i * 90);
                    }
                }, 300);
                break;
            default:
                console.warn('[AudioIntegration] 未知事件类型:', eventType);
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 全局单例 & 自动初始化
// ─────────────────────────────────────────────────────────────────────────────

/** 全局音频集成管理器单例 */
const audioIntegration = new AudioIntegrationManager();

// DOM 就绪后初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => audioIntegration.init());
} else {
    // 已就绪（脚本后加载）
    setTimeout(() => audioIntegration.init(), 0);
}

/** 全局暴露（供游戏其他模块直接调用） */
window.audioIntegration = audioIntegration;

/**
 * 语义化游戏事件触发接口
 * 用法：gameAudioEvent('combat:start')
 *       gameAudioEvent('player:health', { ratio: 0.15 })
 */
window.gameAudioEvent = (eventType, data) => audioIntegration.emit(eventType, data);

console.log('[AudioIntegration] audio-integration.js 加载完成');
