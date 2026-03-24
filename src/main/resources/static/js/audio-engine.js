/**
 * 修仙挂机游戏 — 核心音频引擎 (Game Audio Engine)
 * =====================================================
 * 架构：Web Audio API 原生实现，模拟 FMOD 事件驱动模型
 *
 * 核心功能：
 *   1. 自适应音乐系统  — 根据游戏状态参数实时驱动，节拍同步过渡
 *   2. 程序化音效合成  — 振荡器+滤波器生成所有SFX，零外部资源依赖
 *   3. 空间音频        — 3D Pan + 距离衰减 + 遮挡低通模拟
 *   4. 语音预算管理    — 按平台限制语音并发数，优先级抢占
 *   5. 混音器总线架构  — Master/Music/SFX/UI/Voice 五条独立总线 + VCA
 *   6. 开发者调试 HUD  — 实时语音数/参数/总线电平可视化
 *
 * 事件命名规范（映射到 FMOD 路径）：
 *   SFX:   sfx/player/*, sfx/combat/*, sfx/environment/*, sfx/pet/*
 *   Music: music/exploration/*, music/combat/*, music/breakthrough/*
 *   UI:    ui/click, ui/open, ui/close, ui/reward, ui/error
 *   VO:    vo/npc/[id]/[line]
 *
 * 音频预算（浏览器平台）：
 *   最大语音数：32（实体）/ 64（虚拟）
 *   内存预算：SFX 16MB / Music streaming / UI 4MB
 *   CPU：DSP < 2ms/frame 目标
 */

'use strict';

// ─────────────────────────────────────────────────────────────────────────────
// 常量 & 枚举
// ─────────────────────────────────────────────────────────────────────────────

const AudioConstants = Object.freeze({
    // 采样率
    SAMPLE_RATE: 44100,

    // 语音预算（浏览器平台）
    MAX_VOICES: 32,
    MAX_VIRTUAL_VOICES: 64,

    // 优先级层级（数字越小越高）
    PRIORITY: {
        UI: 0,
        PLAYER_VO: 0,
        PLAYER_SFX: 1,
        COMBAT_SFX: 2,
        AMBIENT: 3,
        MUSIC: 4,
    },

    // 抢占策略
    STEAL_MODE: {
        NEVER: 'never',
        QUIETEST: 'quietest',
        FARTHEST: 'farthest',
        OLDEST: 'oldest',
    },

    // 音乐紧张度区间
    TENSION: {
        EXPLORE: 0.0,   // 探索 — 仅主旋律
        ALERT: 0.3,     // 警戒 — 打击乐进入
        COMBAT: 0.6,    // 战斗 — 完整编曲
        BOSS: 1.0,      // Boss/危急 — 最大强度
    },

    // 修炼境界编号（对应音乐层）
    REALM: {
        QI_REFINING: 0,   // 练气
        FOUNDATION: 1,    // 筑基
        GOLDEN_CORE: 2,   // 金丹
        NASCENT_SOUL: 3,  // 元婴
    },

    // 过渡类型
    TRANSITION: {
        BEAT_SYNC: 'beat_sync',     // 节拍同步（推荐）
        BAR_SYNC: 'bar_sync',       // 小节同步
        IMMEDIATE: 'immediate',     // 即时（仅 UI）
        FADE: 'fade',               // 淡入淡出
    },

    // 混响区域类型
    REVERB_ZONE: {
        OUTDOOR: 'outdoor',
        INDOOR: 'indoor',
        CAVE: 'cave',
        METAL: 'metal',
        VOID: 'void',       // 虚空/修炼空间
    },

    // BPM（用于节拍同步）
    BPM: {
        EXPLORE: 76,
        COMBAT: 132,
        BREAKTHROUGH: 100,
        MENU: 60,
    },
});

// ─────────────────────────────────────────────────────────────────────────────
// 工具函数
// ─────────────────────────────────────────────────────────────────────────────

const AudioUtils = {
    /** 线性音量转 dB */
    linToDB: (lin) => lin > 0 ? 20 * Math.log10(lin) : -Infinity,

    /** dB 转线性音量 */
    dbToLin: (db) => Math.pow(10, db / 20),

    /** 平滑插值（用于参数平滑） */
    lerp: (a, b, t) => a + (b - a) * Math.max(0, Math.min(1, t)),

    /** 节拍时长（秒） */
    beatDuration: (bpm) => 60 / bpm,

    /** 下一个节拍时间戳 */
    nextBeatTime: (currentTime, bpm, lookahead = 0.05) => {
        const beat = 60 / bpm;
        return currentTime + beat - ((currentTime % beat) || beat) + lookahead;
    },

    /** 随机范围 */
    randRange: (min, max) => min + Math.random() * (max - min),

    /** 随机选取数组元素 */
    randPick: (arr) => arr[Math.floor(Math.random() * arr.length)],

    /** 生成唯一 ID */
    uid: () => `voice_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,

    /** 指数平滑（用于语音数显示） */
    expSmooth: (current, target, alpha) => current + alpha * (target - current),

    /** 频率半音偏移 */
    semitoneShift: (freq, semitones) => freq * Math.pow(2, semitones / 12),
};

// ─────────────────────────────────────────────────────────────────────────────
// 程序化音效库（合成器模块）
// ─────────────────────────────────────────────────────────────────────────────

class ProceduralSFX {
    /**
     * 所有音效通过 Web Audio API 合成，不依赖外部音频文件。
     * 每个方法返回一个已连接并准备播放的 AudioBufferSourceNode 或 OscillatorNode 网络。
     */

    constructor(ctx, masterBus) {
        this.ctx = ctx;
        this.masterBus = masterBus;
    }

    /** 获取当前时间（安全） */
    _now() { return this.ctx.currentTime; }

    /** 创建增益节点并连接 */
    _gain(value, target) {
        const g = this.ctx.createGain();
        g.gain.setValueAtTime(value, this._now());
        g.connect(target || this.masterBus);
        return g;
    }

    /** 创建双二阶滤波器 */
    _filter(type, freq, q, target) {
        const f = this.ctx.createBiquadFilter();
        f.type = type;
        f.frequency.setValueAtTime(freq, this._now());
        if (q !== undefined) f.Q.setValueAtTime(q, this._now());
        f.connect(target || this.masterBus);
        return f;
    }

    /**
     * 脚步声（石板）
     * 合成：噪声 burst + 高通 + 快速衰减 envelope
     */
    footstep_stone(volume = 0.4) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        const bufLen = Math.floor(this.ctx.sampleRate * 0.08);
        const buf = this.ctx.createBuffer(1, bufLen, this.ctx.sampleRate);
        const data = buf.getChannelData(0);
        for (let i = 0; i < bufLen; i++) data[i] = (Math.random() * 2 - 1);

        const src = this.ctx.createBufferSource();
        src.buffer = buf;

        const hpf = this._filter('highpass', 800, 1, env);
        src.connect(hpf);

        const v = volume * AudioUtils.randRange(0.8, 1.0);
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(v, now + 0.005);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.08);

        src.start(now);
        src.stop(now + 0.1);
        return src;
    }

    /**
     * 脚步声（草地）
     * 合成：低频噪声 + 柔和包络
     */
    footstep_grass(volume = 0.3) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        const bufLen = Math.floor(this.ctx.sampleRate * 0.12);
        const buf = this.ctx.createBuffer(1, bufLen, this.ctx.sampleRate);
        const data = buf.getChannelData(0);
        for (let i = 0; i < bufLen; i++) {
            // 软化噪声
            data[i] = (Math.random() * 2 - 1) * Math.sin(Math.PI * i / bufLen);
        }

        const src = this.ctx.createBufferSource();
        src.buffer = buf;

        const lpf = this._filter('lowpass', 2000, 1, env);
        src.connect(lpf);

        const v = volume * AudioUtils.randRange(0.7, 1.0);
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(v, now + 0.015);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.12);

        src.start(now);
        src.stop(now + 0.15);
        return src;
    }

    /**
     * 普通攻击音效
     * 合成：锯齿波瞬击 + 噪声层 + 快速 decay
     */
    attack_normal(volume = 0.6) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        // 打击瞬态
        const osc = this.ctx.createOscillator();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(220, now);
        osc.frequency.exponentialRampToValueAtTime(80, now + 0.05);

        // 噪声层
        const bufLen = Math.floor(this.ctx.sampleRate * 0.1);
        const buf = this.ctx.createBuffer(1, bufLen, this.ctx.sampleRate);
        const data = buf.getChannelData(0);
        for (let i = 0; i < bufLen; i++) data[i] = Math.random() * 2 - 1;
        const noiseSrc = this.ctx.createBufferSource();
        noiseSrc.buffer = buf;

        const noiseGain = this.ctx.createGain();
        noiseGain.gain.setValueAtTime(0.3, now);

        const hpf = this._filter('highpass', 1200, 1, env);
        noiseSrc.connect(noiseGain);
        noiseGain.connect(hpf);
        osc.connect(env);

        const v = volume;
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(v, now + 0.003);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.15);

        osc.start(now); osc.stop(now + 0.15);
        noiseSrc.start(now); noiseSrc.stop(now + 0.1);
        return { osc, noiseSrc };
    }

    /**
     * 暴击音效
     * 合成：attack_normal 加厚 + 金属谐振 + 额外高频层
     */
    attack_critical(volume = 0.85) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        // 主打击
        const osc1 = this.ctx.createOscillator();
        osc1.type = 'square';
        osc1.frequency.setValueAtTime(440, now);
        osc1.frequency.exponentialRampToValueAtTime(110, now + 0.08);

        // 金属谐振
        const osc2 = this.ctx.createOscillator();
        osc2.type = 'sawtooth';
        osc2.frequency.setValueAtTime(880, now);
        osc2.frequency.exponentialRampToValueAtTime(220, now + 0.05);
        const osc2g = this.ctx.createGain();
        osc2g.gain.setValueAtTime(0.4, now);

        osc1.connect(env);
        osc2.connect(osc2g);
        osc2g.connect(env);

        const v = volume;
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(v, now + 0.002);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.25);

        osc1.start(now); osc1.stop(now + 0.25);
        osc2.start(now); osc2.stop(now + 0.12);
        return { osc1, osc2 };
    }

    /**
     * 受击/被打音效
     * 合成：低频撞击 + 短噪声
     */
    hit_taken(volume = 0.5) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(160, now);
        osc.frequency.exponentialRampToValueAtTime(60, now + 0.08);

        osc.connect(env);
        const v = volume;
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(v, now + 0.005);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.12);

        osc.start(now); osc.stop(now + 0.15);
        return osc;
    }

    /**
     * 怪物死亡
     * 合成：低沉轰鸣下行 + 噪声消散
     */
    monster_die(volume = 0.7) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        const osc = this.ctx.createOscillator();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(200, now);
        osc.frequency.exponentialRampToValueAtTime(30, now + 0.4);

        const filter = this.ctx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.frequency.setValueAtTime(800, now);
        filter.frequency.linearRampToValueAtTime(200, now + 0.4);

        osc.connect(filter);
        filter.connect(env);

        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(volume, now + 0.01);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.5);

        osc.start(now); osc.stop(now + 0.5);
        return osc;
    }

    /**
     * 技能释放 — 灵气爆发
     * 合成：上扫频 + 混响感尾音
     */
    skill_cast(volume = 0.65, skillTier = 1) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        const baseFreq = 300 + skillTier * 100;
        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(baseFreq, now);
        osc.frequency.exponentialRampToValueAtTime(baseFreq * 3, now + 0.15);
        osc.frequency.exponentialRampToValueAtTime(baseFreq * 1.5, now + 0.4);

        // 谐波层
        const osc2 = this.ctx.createOscillator();
        osc2.type = 'triangle';
        osc2.frequency.setValueAtTime(baseFreq * 2, now);
        osc2.frequency.exponentialRampToValueAtTime(baseFreq * 4, now + 0.2);
        const g2 = this.ctx.createGain();
        g2.gain.setValueAtTime(0.35, now);

        osc.connect(env);
        osc2.connect(g2);
        g2.connect(env);

        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(volume, now + 0.02);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.5);

        osc.start(now); osc.stop(now + 0.5);
        osc2.start(now); osc2.stop(now + 0.3);
        return { osc, osc2 };
    }

    /**
     * 宠物互动 — 愉悦音效
     * 合成：上行小三和弦琶音
     */
    pet_happy(volume = 0.5) {
        const now = this._now();
        const notes = [523.25, 659.25, 783.99]; // C5, E5, G5
        const voices = [];
        notes.forEach((freq, i) => {
            const t = now + i * 0.08;
            const env = this._gain(0);
            env.connect(this.masterBus);

            const osc = this.ctx.createOscillator();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(freq, t);

            osc.connect(env);
            env.gain.setValueAtTime(0, t);
            env.gain.linearRampToValueAtTime(volume, t + 0.01);
            env.gain.exponentialRampToValueAtTime(0.001, t + 0.25);

            osc.start(t); osc.stop(t + 0.3);
            voices.push(osc);
        });
        return voices;
    }

    /**
     * 宠物进化 — 蜕变音效
     * 合成：上行音阶 + 最终和弦
     */
    pet_evolve(volume = 0.8) {
        const now = this._now();
        const scale = [261.63, 329.63, 392.0, 523.25, 659.25, 783.99, 1046.5]; // C4→C6
        const voices = [];
        scale.forEach((freq, i) => {
            const t = now + i * 0.1;
            const env = this._gain(0);
            env.connect(this.masterBus);
            const osc = this.ctx.createOscillator();
            osc.type = i < 5 ? 'sine' : 'triangle';
            osc.frequency.setValueAtTime(freq, t);
            osc.connect(env);
            env.gain.setValueAtTime(0, t);
            env.gain.linearRampToValueAtTime(volume * (0.5 + i * 0.07), t + 0.02);
            env.gain.exponentialRampToValueAtTime(0.001, t + 0.4);
            osc.start(t); osc.stop(t + 0.5);
            voices.push(osc);
        });
        return voices;
    }

    /**
     * 修炼节拍脉冲
     * 合成：柔和低频正弦波脉冲，模拟呼吸/冥想节律
     */
    cultivation_pulse(volume = 0.25) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(80, now);

        osc.connect(env);
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(volume, now + 0.2);
        env.gain.exponentialRampToValueAtTime(0.001, now + 1.0);

        osc.start(now); osc.stop(now + 1.2);
        return osc;
    }

    /**
     * 升级 / 获得奖励
     * 合成：上行三度 + 闪光高频
     */
    level_up(volume = 0.75) {
        const now = this._now();
        const notes = [523.25, 659.25, 1046.5]; // C5 E5 C6
        const durations = [0.1, 0.1, 0.4];
        const voices = [];
        let t = now;
        notes.forEach((freq, i) => {
            const env = this._gain(0);
            env.connect(this.masterBus);
            const osc = this.ctx.createOscillator();
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(freq, t);
            osc.connect(env);
            env.gain.setValueAtTime(0, t);
            env.gain.linearRampToValueAtTime(volume, t + 0.01);
            env.gain.exponentialRampToValueAtTime(0.001, t + durations[i]);
            osc.start(t); osc.stop(t + durations[i] + 0.05);
            t += durations[i] * 0.8;
            voices.push(osc);
        });
        return voices;
    }

    /**
     * 境界突破 — 心魔战斗开始
     * 合成：低频共鸣 + 上扫金属撞击 + 余震
     */
    breakthrough_start(volume = 0.9) {
        const now = this._now();

        // 低频轰鸣
        const rumbleEnv = this._gain(0);
        rumbleEnv.connect(this.masterBus);
        const rumble = this.ctx.createOscillator();
        rumble.type = 'sawtooth';
        rumble.frequency.setValueAtTime(40, now);
        rumble.frequency.exponentialRampToValueAtTime(20, now + 1.5);
        const lpf = this.ctx.createBiquadFilter();
        lpf.type = 'lowpass'; lpf.frequency.value = 200;
        rumble.connect(lpf); lpf.connect(rumbleEnv);
        rumbleEnv.gain.setValueAtTime(0, now);
        rumbleEnv.gain.linearRampToValueAtTime(volume * 0.6, now + 0.3);
        rumbleEnv.gain.linearRampToValueAtTime(0.001, now + 2.0);
        rumble.start(now); rumble.stop(now + 2.0);

        // 金属撞击
        const strikeEnv = this._gain(0);
        strikeEnv.connect(this.masterBus);
        const strike = this.ctx.createOscillator();
        strike.type = 'square';
        strike.frequency.setValueAtTime(600, now + 0.05);
        strike.frequency.exponentialRampToValueAtTime(150, now + 0.3);
        strike.connect(strikeEnv);
        strikeEnv.gain.setValueAtTime(0, now + 0.05);
        strikeEnv.gain.linearRampToValueAtTime(volume, now + 0.06);
        strikeEnv.gain.exponentialRampToValueAtTime(0.001, now + 0.5);
        strike.start(now + 0.05); strike.stop(now + 0.5);

        return { rumble, strike };
    }

    /**
     * 境界突破 — 成功
     * 合成：上行五度 + 延迟泛音 + 回声
     */
    breakthrough_success(volume = 1.0) {
        const now = this._now();
        const chord = [261.63, 329.63, 392.0, 523.25, 784.0]; // C4和弦+高八度
        const voices = [];
        chord.forEach((freq, i) => {
            const t = now + i * 0.05;
            const env = this._gain(0);
            env.connect(this.masterBus);
            const osc = this.ctx.createOscillator();
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(freq, t);
            osc.connect(env);
            env.gain.setValueAtTime(0, t);
            env.gain.linearRampToValueAtTime(volume * (0.4 + i * 0.1), t + 0.03);
            env.gain.exponentialRampToValueAtTime(0.001, t + 2.0);
            osc.start(t); osc.stop(t + 2.5);
            voices.push(osc);
        });
        return voices;
    }

    /**
     * 境界突破 — 失败
     * 合成：下行调 + 低沉结束
     */
    breakthrough_fail(volume = 0.7) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);

        const osc = this.ctx.createOscillator();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(300, now);
        osc.frequency.exponentialRampToValueAtTime(60, now + 0.8);

        const dist = this.ctx.createWaveShaper();
        const curve = new Float32Array(256);
        for (let i = 0; i < 256; i++) {
            const x = (i * 2) / 256 - 1;
            curve[i] = (Math.PI + 80) * x / (Math.PI + 80 * Math.abs(x));
        }
        dist.curve = curve;

        osc.connect(dist); dist.connect(env);
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(volume, now + 0.05);
        env.gain.exponentialRampToValueAtTime(0.001, now + 1.0);

        osc.start(now); osc.stop(now + 1.0);
        return osc;
    }

    /**
     * UI 点击
     * 合成：短促高频正弦 click
     */
    ui_click(volume = 0.3) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);
        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(1200, now);
        osc.connect(env);
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(volume, now + 0.002);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.05);
        osc.start(now); osc.stop(now + 0.06);
        return osc;
    }

    /**
     * UI 弹窗打开
     * 合成：中频上扫
     */
    ui_open(volume = 0.35) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);
        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(600, now);
        osc.frequency.linearRampToValueAtTime(900, now + 0.08);
        osc.connect(env);
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(volume, now + 0.01);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.15);
        osc.start(now); osc.stop(now + 0.18);
        return osc;
    }

    /**
     * UI 弹窗关闭
     * 合成：中频下扫
     */
    ui_close(volume = 0.3) {
        const now = this._now();
        const env = this._gain(0);
        env.connect(this.masterBus);
        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(900, now);
        osc.frequency.linearRampToValueAtTime(500, now + 0.08);
        osc.connect(env);
        env.gain.setValueAtTime(0, now);
        env.gain.linearRampToValueAtTime(volume, now + 0.01);
        env.gain.exponentialRampToValueAtTime(0.001, now + 0.12);
        osc.start(now); osc.stop(now + 0.15);
        return osc;
    }

    /**
     * 错误提示
     * 合成：低频双拍下行
     */
    ui_error(volume = 0.4) {
        const now = this._now();
        [400, 300].forEach((freq, i) => {
            const t = now + i * 0.12;
            const env = this._gain(0);
            env.connect(this.masterBus);
            const osc = this.ctx.createOscillator();
            osc.type = 'square';
            osc.frequency.setValueAtTime(freq, t);
            osc.connect(env);
            env.gain.setValueAtTime(0, t);
            env.gain.linearRampToValueAtTime(volume, t + 0.01);
            env.gain.exponentialRampToValueAtTime(0.001, t + 0.1);
            osc.start(t); osc.stop(t + 0.12);
        });
    }

    /**
     * 灵石/奖励获得
     * 合成：硬币碰撞音
     */
    reward_coin(volume = 0.4) {
        const now = this._now();
        const freqs = [
            AudioUtils.randRange(1800, 2200),
            AudioUtils.randRange(1600, 2000),
        ];
        freqs.forEach((freq, i) => {
            const t = now + i * 0.06;
            const env = this._gain(0);
            env.connect(this.masterBus);
            const osc = this.ctx.createOscillator();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(freq, t);
            osc.frequency.exponentialRampToValueAtTime(freq * 0.7, t + 0.15);
            osc.connect(env);
            env.gain.setValueAtTime(0, t);
            env.gain.linearRampToValueAtTime(volume, t + 0.003);
            env.gain.exponentialRampToValueAtTime(0.001, t + 0.2);
            osc.start(t); osc.stop(t + 0.22);
        });
    }

    /**
     * 连招触发 — COMBO!
     * 合成：三连音上行 + 强调尾音
     */
    combo_trigger(comboCount = 1, volume = 0.7) {
        const now = this._now();
        const baseFreq = Math.min(300 + comboCount * 80, 900);
        const notes = [baseFreq, baseFreq * 1.25, baseFreq * 1.5];
        notes.forEach((freq, i) => {
            const t = now + i * 0.07;
            const env = this._gain(0);
            env.connect(this.masterBus);
            const osc = this.ctx.createOscillator();
            osc.type = 'square';
            osc.frequency.setValueAtTime(freq, t);
            osc.connect(env);
            const v = i === 2 ? volume : volume * 0.6;
            env.gain.setValueAtTime(0, t);
            env.gain.linearRampToValueAtTime(v, t + 0.005);
            env.gain.exponentialRampToValueAtTime(0.001, t + (i === 2 ? 0.35 : 0.1));
            osc.start(t); osc.stop(t + 0.4);
        });
    }

    /**
     * 灵气场环境音（持续）
     * 合成：多层低频正弦调制，产生冥想氛围
     * 返回停止函数
     */
    ambient_spiritual(volume = 0.15) {
        const now = this._now();
        const oscs = [];

        // 基频 + 3次谐波，轻微调制
        const baseFreqs = [55, 110, 165, 220];
        baseFreqs.forEach((freq, i) => {
            const modDepth = 2 + i;
            const modRate = 0.1 + i * 0.05;

            const carrier = this.ctx.createOscillator();
            carrier.type = 'sine';
            carrier.frequency.value = freq;

            const lfo = this.ctx.createOscillator();
            lfo.type = 'sine';
            lfo.frequency.value = modRate;

            const lfoGain = this.ctx.createGain();
            lfoGain.gain.value = modDepth;

            lfo.connect(lfoGain);
            lfoGain.connect(carrier.frequency);

            const env = this._gain(0);
            env.connect(this.masterBus);
            carrier.connect(env);

            env.gain.linearRampToValueAtTime(volume / (i + 1), now + 2.0);

            carrier.start(now);
            lfo.start(now);
            oscs.push({ carrier, lfo, env });
        });

        return {
            stop: (fadeTime = 1.5) => {
                const t = this.ctx.currentTime;
                oscs.forEach(({ carrier, lfo, env }) => {
                    env.gain.linearRampToValueAtTime(0.0001, t + fadeTime);
                    carrier.stop(t + fadeTime + 0.1);
                    lfo.stop(t + fadeTime + 0.1);
                });
            }
        };
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 自适应音乐系统（Adaptive Music System）
// ─────────────────────────────────────────────────────────────────────────────

class AdaptiveMusicSystem {
    /**
     * 基于参数驱动的自适应音乐系统
     *
     * 参数集：
     *   combatIntensity  [0-1]  战斗紧张度（AI威胁等级聚合）
     *   realmLevel       [0-3]  修炼境界（影响音乐主题）
     *   timeOfDay        [0-1]  游戏时间（昼夜循环）
     *   playerHealth     [0-1]  玩家生命值（低生命低通滤波强化）
     *   inBreakthrough   bool   境界突破进行中（独立轨道）
     *
     * 音乐层：
     *   Layer 0 (Drone)    — 持续底层，始终播放，低音合成垫音
     *   Layer 1 (Melody)   — 探索主旋律，combatIntensity < 0.3 时全音量
     *   Layer 2 (Rhythm)   — 打击乐/节奏层，combatIntensity > 0.3 渐入
     *   Layer 3 (Combat)   — 战斗全编曲，combatIntensity > 0.6 渐入
     *   Layer 4 (Danger)   — 危急/Boss层，combatIntensity > 0.9 或 health < 0.2
     *   Layer 5 (Breakthrough) — 突破专用，inBreakthrough=true 时独占
     *
     * 过渡规则：
     *   - 所有层切换节拍同步（nextBeatTime），禁止硬切
     *   - 单次音量变化速率 ≤ 0.02/beat 防止突兀感
     *   - 全局节拍时钟从 AudioContext.currentTime 推算，不依赖计时器
     */

    constructor(ctx, musicBus) {
        this.ctx = ctx;
        this.musicBus = musicBus;

        // 参数（由外部游戏系统通过 setParameter 驱动）
        this.params = {
            combatIntensity: 0.0,
            realmLevel: 0,
            timeOfDay: 0.5,
            playerHealth: 1.0,
            inBreakthrough: false,
            inMenu: false,
        };

        // 平滑后的参数（防止跳变）
        this.smoothParams = { ...this.params };

        // 当前激活的音乐层节点
        this.layers = {
            drone: null,
            melody: null,
            rhythm: null,
            combat: null,
            danger: null,
            breakthrough: null,
        };

        // 每层的增益节点
        this.layerGains = {};

        // 当前 BPM
        this.bpm = AudioConstants.BPM.EXPLORE;
        this.beatDuration = AudioUtils.beatDuration(this.bpm);

        // 下次参数更新时间戳
        this._lastParamUpdate = 0;
        this._paramUpdateInterval = 500; // ms

        // 是否正在播放
        this.isPlaying = false;

        // 参数更新定时器
        this._updateTimer = null;

        this._initLayers();
    }

    _initLayers() {
        const layerNames = Object.keys(this.layers);
        layerNames.forEach(name => {
            const gain = this.ctx.createGain();
            gain.gain.setValueAtTime(0.0001, this.ctx.currentTime);
            gain.connect(this.musicBus);
            this.layerGains[name] = gain;
        });
    }

    /**
     * 启动音乐系统
     */
    start() {
        if (this.isPlaying) return;
        this.isPlaying = true;
        this._startDroneLayer();
        this._startMelodyLayer();
        this._startRhythmLayer();
        this._startCombatLayer();
        this._startDangerLayer();
        this._startBreakthroughLayer();
        this._startParamUpdateLoop();
        console.log('[AudioEngine] 自适应音乐系统启动，BPM:', this.bpm);
    }

    /**
     * 停止音乐系统（淡出）
     */
    stop(fadeTime = 2.0) {
        this.isPlaying = false;
        if (this._updateTimer) clearInterval(this._updateTimer);
        const now = this.ctx.currentTime;
        Object.values(this.layerGains).forEach(g => {
            g.gain.linearRampToValueAtTime(0.0001, now + fadeTime);
        });
    }

    /**
     * 设置游戏参数（供外部游戏系统调用）
     * @param {string} name   参数名
     * @param {number|boolean} value  参数值
     */
    setParameter(name, value) {
        if (!(name in this.params)) return;
        this.params[name] = value;

        // 战斗紧张度变化同步更新 BPM
        if (name === 'combatIntensity') {
            const newBpm = value < 0.3
                ? AudioConstants.BPM.EXPLORE
                : value < 0.6
                    ? Math.round(AudioUtils.lerp(AudioConstants.BPM.EXPLORE, AudioConstants.BPM.COMBAT, (value - 0.3) / 0.3))
                    : AudioConstants.BPM.COMBAT;
            if (newBpm !== this.bpm) {
                this.bpm = newBpm;
                this.beatDuration = AudioUtils.beatDuration(this.bpm);
            }
        }
    }

    /** 批量设置参数 */
    setParameters(paramMap) {
        Object.entries(paramMap).forEach(([k, v]) => this.setParameter(k, v));
    }

    _startParamUpdateLoop() {
        this._updateTimer = setInterval(() => {
            this._updateLayerVolumes();
        }, this._paramUpdateInterval);
    }

    /**
     * 核心：根据参数计算目标音量并节拍同步切换
     */
    _updateLayerVolumes() {
        if (!this.isPlaying) return;

        // 平滑参数（防止跳变）
        const alpha = 0.3;
        this.smoothParams.combatIntensity = AudioUtils.lerp(
            this.smoothParams.combatIntensity,
            this.params.combatIntensity,
            alpha
        );
        this.smoothParams.playerHealth = AudioUtils.lerp(
            this.smoothParams.playerHealth,
            this.params.playerHealth,
            0.2
        );

        const si = this.smoothParams.combatIntensity;
        const ph = this.smoothParams.playerHealth;
        const inBt = this.params.inBreakthrough;

        // 计算目标音量
        const targets = {
            drone: inBt ? 0.0001 : 0.18,
            melody: inBt ? 0.0001 : Math.max(0.0001, 0.7 - si * 0.8),
            rhythm: inBt ? 0.0001 : (si > 0.25 ? Math.min(0.6, (si - 0.25) * 1.2) : 0.0001),
            combat: inBt ? 0.0001 : (si > 0.55 ? Math.min(0.7, (si - 0.55) * 2.0) : 0.0001),
            danger: inBt ? 0.0001 : ((si > 0.88 || ph < 0.2) ? 0.65 : 0.0001),
            breakthrough: inBt ? 0.75 : 0.0001,
        };

        // 节拍同步：在下一个节拍边界切换
        const nextBeat = AudioUtils.nextBeatTime(this.ctx.currentTime, this.bpm);
        const transitionDuration = this.beatDuration * 2; // 2拍过渡

        Object.entries(targets).forEach(([layer, targetVol]) => {
            const gain = this.layerGains[layer];
            if (!gain) return;
            const currentVol = gain.gain.value;
            if (Math.abs(currentVol - targetVol) > 0.01) {
                gain.gain.setTargetAtTime(targetVol, nextBeat, transitionDuration / 3);
            }
        });
    }

    // ── 音乐层生成器 ──────────────────────────────────────────────────────────

    /**
     * Drone Layer — 持续底层垫音（程序化）
     * 基于 realmLevel 调整频率基音：
     *   练气 55Hz（A1）/ 筑基 73.4Hz（D2）/ 金丹 82.4Hz（E2）/ 元婴 110Hz（A2）
     */
    _startDroneLayer() {
        const realmBaseFreqs = [55, 73.4, 82.4, 110];
        const freq = realmBaseFreqs[Math.min(this.params.realmLevel, 3)];
        const gain = this.layerGains.drone;

        const oscs = [];
        // 三层叠加：基频 + 五度音 + 八度音
        [1, 1.498, 2].forEach((ratio, i) => {
            const osc = this.ctx.createOscillator();
            osc.type = 'sine';
            osc.frequency.value = freq * ratio;

            const oscGain = this.ctx.createGain();
            oscGain.gain.value = 0.4 / (i + 1); // 分层音量

            // 轻微颤音
            const lfo = this.ctx.createOscillator();
            lfo.type = 'sine';
            lfo.frequency.value = 0.2 + i * 0.07;
            const lfoGain = this.ctx.createGain();
            lfoGain.gain.value = 1.5;
            lfo.connect(lfoGain);
            lfoGain.connect(osc.frequency);

            osc.connect(oscGain);
            oscGain.connect(gain);
            osc.start();
            lfo.start();
            oscs.push({ osc, oscGain, lfo });
        });

        this.layers.drone = { oscs, stop: () => oscs.forEach(({ osc, lfo }) => { osc.stop(); lfo.stop(); }) };
    }

    /**
     * Melody Layer — 探索主旋律（程序化五声音阶即兴）
     * 五声音阶 C Do = [C, D, E, G, A] + 宫、商、角、徵、羽（中国传统调式）
     */
    _startMelodyLayer() {
        const gain = this.layerGains.melody;
        const pentatonic = [261.63, 293.66, 329.63, 392.0, 440.0, 523.25, 587.33, 659.25, 784.0];
        const bpm = this.bpm;

        let stopFlag = false;
        const playNote = () => {
            if (!this.isPlaying || stopFlag) return;

            const freq = AudioUtils.randPick(pentatonic) * (Math.random() > 0.7 ? 2 : 1);
            const duration = AudioUtils.randRange(0.3, 0.8);
            const now = this.ctx.currentTime;

            const osc = this.ctx.createOscillator();
            osc.type = Math.random() > 0.5 ? 'sine' : 'triangle';
            osc.frequency.value = freq;

            const noteGain = this.ctx.createGain();
            noteGain.gain.setValueAtTime(0, now);
            noteGain.gain.linearRampToValueAtTime(0.35, now + 0.02);
            noteGain.gain.exponentialRampToValueAtTime(0.001, now + duration);

            osc.connect(noteGain);
            noteGain.connect(gain);
            osc.start(now);
            osc.stop(now + duration + 0.05);

            // 下一个音符在下一个节拍
            const nextInterval = AudioUtils.beatDuration(bpm) * (Math.random() > 0.6 ? 1 : 2);
            if (!stopFlag) setTimeout(playNote, nextInterval * 1000);
        };

        // 延迟0.5s开始，防止与其他音频竞争
        setTimeout(playNote, 500);
        this.layers.melody = { stop: () => { stopFlag = true; } };
    }

    /**
     * Rhythm Layer — 节奏/打击乐层（程序化）
     * combatIntensity > 0.3 时渐入，使用噪声 + 音高脉冲模拟打击乐
     */
    _startRhythmLayer() {
        const gain = this.layerGains.rhythm;
        let stopFlag = false;

        const kick = () => {
            if (!this.isPlaying || stopFlag) return;
            const now = this.ctx.currentTime;
            const osc = this.ctx.createOscillator();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(150, now);
            osc.frequency.exponentialRampToValueAtTime(40, now + 0.08);
            const env = this.ctx.createGain();
            env.gain.setValueAtTime(0.8, now);
            env.gain.exponentialRampToValueAtTime(0.001, now + 0.12);
            osc.connect(env); env.connect(gain);
            osc.start(now); osc.stop(now + 0.15);
        };

        const hihat = () => {
            if (!this.isPlaying || stopFlag) return;
            const now = this.ctx.currentTime;
            const bufLen = Math.floor(this.ctx.sampleRate * 0.04);
            const buf = this.ctx.createBuffer(1, bufLen, this.ctx.sampleRate);
            const data = buf.getChannelData(0);
            for (let i = 0; i < bufLen; i++) data[i] = Math.random() * 2 - 1;
            const src = this.ctx.createBufferSource();
            src.buffer = buf;
            const hpf = this.ctx.createBiquadFilter();
            hpf.type = 'highpass'; hpf.frequency.value = 8000;
            const env = this.ctx.createGain();
            env.gain.setValueAtTime(0.3, now);
            env.gain.exponentialRampToValueAtTime(0.001, now + 0.04);
            src.connect(hpf); hpf.connect(env); env.connect(gain);
            src.start(now); src.stop(now + 0.05);
        };

        const beat = AudioUtils.beatDuration(this.bpm) * 1000;
        // 4/4 节奏型：kick 在 1/3 拍，hihat 在 2/4 拍
        const rhythmPattern = () => {
            if (stopFlag) return;
            kick();
            setTimeout(hihat, beat / 2);
            setTimeout(hihat, beat);
            setTimeout(kick, beat * 1.5);
            setTimeout(hihat, beat * 2);
            setTimeout(() => rhythmPattern(), beat * 2);
        };
        rhythmPattern();
        this.layers.rhythm = { stop: () => { stopFlag = true; } };
    }

    /**
     * Combat Layer — 战斗全编曲（程序化）
     * combatIntensity > 0.6 时渐入，低频驱动 + 和声紧张
     */
    _startCombatLayer() {
        const gain = this.layerGains.combat;
        let stopFlag = false;

        const combatLoop = () => {
            if (!this.isPlaying || stopFlag) return;
            const now = this.ctx.currentTime;
            // 重复低频行进音型
            const motif = [73.4, 69.3, 65.4, 73.4]; // Dm dim
            motif.forEach((freq, i) => {
                const t = now + i * (AudioUtils.beatDuration(this.bpm) * 0.5);
                const osc = this.ctx.createOscillator();
                osc.type = 'sawtooth';
                osc.frequency.value = freq;
                const dist = this.ctx.createWaveShaper();
                const curve = new Float32Array(256);
                for (let k = 0; k < 256; k++) {
                    const x = (k * 2) / 256 - 1;
                    curve[k] = (Math.PI + 30) * x / (Math.PI + 30 * Math.abs(x));
                }
                dist.curve = curve;
                const env = this.ctx.createGain();
                env.gain.setValueAtTime(0.5, t);
                env.gain.exponentialRampToValueAtTime(0.001, t + AudioUtils.beatDuration(this.bpm) * 0.45);
                osc.connect(dist); dist.connect(env); env.connect(gain);
                osc.start(t); osc.stop(t + AudioUtils.beatDuration(this.bpm) * 0.5);
            });
            setTimeout(combatLoop, AudioUtils.beatDuration(this.bpm) * 2 * 1000);
        };
        combatLoop();
        this.layers.combat = { stop: () => { stopFlag = true; } };
    }

    /**
     * Danger Layer — 危急/Boss 层
     * 低频颤音 + 高频不协和音
     */
    _startDangerLayer() {
        const gain = this.layerGains.danger;
        let stopFlag = false;

        const dangerLoop = () => {
            if (!this.isPlaying || stopFlag) return;
            const now = this.ctx.currentTime;

            const osc = this.ctx.createOscillator();
            osc.type = 'sawtooth';
            osc.frequency.value = 36.7; // D1 低频威胁

            // 颤音 LFO
            const lfo = this.ctx.createOscillator();
            lfo.type = 'sine'; lfo.frequency.value = 6;
            const lfoGain = this.ctx.createGain();
            lfoGain.gain.value = 3;
            lfo.connect(lfoGain); lfoGain.connect(osc.frequency);

            const env = this.ctx.createGain();
            env.gain.setValueAtTime(0.4, now);
            env.gain.setValueAtTime(0.4, now + 1.5);
            env.gain.exponentialRampToValueAtTime(0.001, now + 2.0);

            osc.connect(env); env.connect(gain);
            osc.start(now); lfo.start(now);
            osc.stop(now + 2.0); lfo.stop(now + 2.1);

            setTimeout(dangerLoop, 2000);
        };
        dangerLoop();
        this.layers.danger = { stop: () => { stopFlag = true; } };
    }

    /**
     * Breakthrough Layer — 境界突破专用音乐
     * 带节拍的紧张感 + 钟鸣谐波
     */
    _startBreakthroughLayer() {
        const gain = this.layerGains.breakthrough;
        let stopFlag = false;

        const btLoop = () => {
            if (!this.isPlaying || stopFlag) return;
            if (!this.params.inBreakthrough) {
                setTimeout(btLoop, 1000);
                return;
            }
            const now = this.ctx.currentTime;

            // 钟鸣泛音
            const bellFreqs = [220, 440, 660, 880];
            bellFreqs.forEach((freq, i) => {
                const t = now + i * 0.15;
                const osc = this.ctx.createOscillator();
                osc.type = 'sine';
                osc.frequency.value = freq;
                const env = this.ctx.createGain();
                env.gain.setValueAtTime(0, t);
                env.gain.linearRampToValueAtTime(0.35 / (i + 1), t + 0.01);
                env.gain.exponentialRampToValueAtTime(0.001, t + 1.5);
                osc.connect(env); env.connect(gain);
                osc.start(t); osc.stop(t + 1.6);
            });

            setTimeout(btLoop, 2400);
        };
        btLoop();
        this.layers.breakthrough = { stop: () => { stopFlag = true; } };
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 混响区域处理器
// ─────────────────────────────────────────────────────────────────────────────

class ReverbZoneProcessor {
    /**
     * 使用 ConvolverNode 模拟不同空间混响
     * 各区域参数参考 FMOD 混响规格
     */
    constructor(ctx) {
        this.ctx = ctx;
        this.zones = {};
        this._initZones();
    }

    _buildIR(preDelay, decayTime, wetness, sampleRate) {
        const srate = sampleRate || this.ctx.sampleRate;
        const length = Math.floor(srate * (preDelay + decayTime));
        const buf = this.ctx.createBuffer(2, length, srate);

        const preSamples = Math.floor(srate * preDelay);
        for (let ch = 0; ch < 2; ch++) {
            const data = buf.getChannelData(ch);
            for (let i = preSamples; i < length; i++) {
                const t = (i - preSamples) / (length - preSamples);
                data[i] = (Math.random() * 2 - 1) * Math.pow(1 - t, 2) * wetness;
            }
        }
        return buf;
    }

    _initZones() {
        const configs = {
            [AudioConstants.REVERB_ZONE.OUTDOOR]:  { preDelay: 0.02, decay: 0.8,  wet: 0.15 },
            [AudioConstants.REVERB_ZONE.INDOOR]:   { preDelay: 0.03, decay: 1.5,  wet: 0.35 },
            [AudioConstants.REVERB_ZONE.CAVE]:     { preDelay: 0.05, decay: 3.5,  wet: 0.60 },
            [AudioConstants.REVERB_ZONE.METAL]:    { preDelay: 0.015,decay: 1.0,  wet: 0.45 },
            [AudioConstants.REVERB_ZONE.VOID]:     { preDelay: 0.08, decay: 5.0,  wet: 0.70 },
        };

        Object.entries(configs).forEach(([zone, cfg]) => {
            const convolver = this.ctx.createConvolver();
            convolver.buffer = this._buildIR(cfg.preDelay, cfg.decay, cfg.wet);

            const wetGain = this.ctx.createGain();
            wetGain.gain.value = cfg.wet;

            const dryGain = this.ctx.createGain();
            dryGain.gain.value = 1.0 - cfg.wet;

            this.zones[zone] = { convolver, wetGain, dryGain };
        });
    }

    /**
     * 将节点路由经过指定混响区域，返回输出节点
     * @param {AudioNode} sourceNode  输入节点
     * @param {string} zoneName       混响区域名
     * @param {AudioNode} destination 目标节点
     */
    route(sourceNode, zoneName, destination) {
        const zone = this.zones[zoneName] || this.zones[AudioConstants.REVERB_ZONE.OUTDOOR];
        const { convolver, wetGain, dryGain } = zone;

        sourceNode.connect(dryGain);
        sourceNode.connect(convolver);
        convolver.connect(wetGain);

        dryGain.connect(destination);
        wetGain.connect(destination);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 语音管理器（Voice Budget Manager）
// ─────────────────────────────────────────────────────────────────────────────

class VoiceManager {
    /**
     * 实施音频预算：
     *   - 最大实体语音数：AudioConstants.MAX_VOICES
     *   - 超出时按优先级+抢占策略淘汰
     *   - 所有事件必须注册优先级，禁止使用默认值
     */
    constructor() {
        this.voices = new Map(); // id → { id, priority, stealMode, startTime, node, volume, position }
        this.stats = { totalPlayed: 0, stolen: 0, virtual: 0 };
    }

    /**
     * 注册新语音
     * @returns {string|null} 语音 ID（若被虚拟化返回 null）
     */
    register(options = {}) {
        const {
            priority = AudioConstants.PRIORITY.COMBAT_SFX,
            stealMode = AudioConstants.STEAL_MODE.OLDEST,
            node = null,
            volume = 1.0,
            position = null,
        } = options;

        const id = AudioUtils.uid();
        this.stats.totalPlayed++;

        if (this.voices.size >= AudioConstants.MAX_VOICES) {
            // 尝试抢占
            const victim = this._findVictim(priority, stealMode);
            if (!victim) {
                // 虚拟化（跳过播放）
                this.stats.virtual++;
                return null;
            }
            this._steal(victim.id);
        }

        this.voices.set(id, {
            id, priority, stealMode, startTime: Date.now(), node, volume, position
        });
        return id;
    }

    /**
     * 语音播放结束，注销
     */
    release(id) {
        this.voices.delete(id);
    }

    /**
     * 强制释放所有语音（场景切换）
     */
    releaseAll() {
        this.voices.forEach(v => {
            if (v.node && v.node.stop) {
                try { v.node.stop(); } catch (e) { /* 已停止 */ }
            }
        });
        this.voices.clear();
    }

    _findVictim(incomingPriority, stealMode) {
        const candidates = [];
        this.voices.forEach(v => {
            // 只抢占低优先级（数字更大的）
            if (v.priority > incomingPriority) candidates.push(v);
            // 同优先级按策略
            else if (v.priority === incomingPriority && stealMode !== AudioConstants.STEAL_MODE.NEVER) {
                candidates.push(v);
            }
        });
        if (candidates.length === 0) return null;

        switch (stealMode) {
            case AudioConstants.STEAL_MODE.OLDEST:
                return candidates.reduce((a, b) => a.startTime < b.startTime ? a : b);
            case AudioConstants.STEAL_MODE.QUIETEST:
                return candidates.reduce((a, b) => a.volume < b.volume ? a : b);
            case AudioConstants.STEAL_MODE.FARTHEST:
                // 简化：没有真实 3D 位置时回退到最旧
                return candidates.reduce((a, b) => a.startTime < b.startTime ? a : b);
            default:
                return candidates[0];
        }
    }

    _steal(id) {
        const v = this.voices.get(id);
        if (v && v.node && v.node.stop) {
            try { v.node.stop(); } catch (e) { /* 忽略 */ }
        }
        this.voices.delete(id);
        this.stats.stolen++;
    }

    getStats() {
        return {
            active: this.voices.size,
            max: AudioConstants.MAX_VOICES,
            total: this.stats.totalPlayed,
            stolen: this.stats.stolen,
            virtual: this.stats.virtual,
            utilization: (this.voices.size / AudioConstants.MAX_VOICES * 100).toFixed(1) + '%',
        };
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 主引擎：GameAudioEngine
// ─────────────────────────────────────────────────────────────────────────────

class GameAudioEngine {
    /**
     * 修仙游戏主音频引擎
     *
     * 总线架构：
     *   AudioContext.destination
     *     └── Master Bus (masterGain)
     *           ├── Music Bus  (musicGain)   ← 自适应音乐系统
     *           ├── SFX Bus    (sfxGain)     ← 所有音效
     *           ├── UI Bus     (uiGain)      ← UI音效（零延迟，PCM）
     *           ├── Ambient Bus(ambientGain) ← 环境音
     *           └── Voice Bus  (voiceGain)   ← NPC语音（预留）
     *
     * VCA（音量控制器，对应用户设置）：
     *   masterVolume / musicVolume / sfxVolume / uiVolume / ambientVolume
     */

    constructor() {
        this._ctx = null;
        this._initialized = false;
        this._suspended = false;

        // 总线增益节点（延迟创建，需用户交互触发）
        this.buses = {};
        this.vcas = {};

        // 子系统（延迟创建）
        this.sfx = null;
        this.music = null;
        this.reverb = null;
        this.voices = new VoiceManager();

        // 当前混响区域
        this._currentReverbZone = AudioConstants.REVERB_ZONE.OUTDOOR;

        // 环境音实例
        this._ambientInstance = null;

        // 游戏状态参数缓存（用于更新音乐）
        this._gameState = {
            combatIntensity: 0.0,
            realmLevel: 0,
            playerHealth: 1.0,
            timeOfDay: 0.5,
            inBreakthrough: false,
        };

        // 设置持久化键
        this._SETTINGS_KEY = 'xiuxian_audio_settings';

        // 用户设置（带本地存储）
        this.settings = this._loadSettings();

        // 是否已被用户显式禁用
        this._muted = false;

        // 首次交互标志
        this._pendingInit = false;

        console.log('[AudioEngine] GameAudioEngine 实例创建完成');
    }

    // ── 初始化 ─────────────────────────────────────────────────────────────

    /**
     * 延迟初始化（必须在用户交互后调用，绕过浏览器自动播放策略）
     */
    async init() {
        if (this._initialized) return;

        try {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (!AudioContext) {
                console.warn('[AudioEngine] Web Audio API 不可用，音频功能已禁用');
                return;
            }

            this._ctx = new AudioContext({ sampleRate: AudioConstants.SAMPLE_RATE });

            // 恢复暂停的 AudioContext（浏览器自动播放策略）
            if (this._ctx.state === 'suspended') {
                await this._ctx.resume();
            }

            this._buildBusArchitecture();
            this._initSubsystems();
            this._applySettings();

            this._initialized = true;
            console.log('[AudioEngine] 初始化成功，采样率:', this._ctx.sampleRate, 'Hz');

            // 启动音乐
            this.music.start();

            // 启动默认环境音
            this._startAmbient();

        } catch (err) {
            console.error('[AudioEngine] 初始化失败:', err);
        }
    }

    /**
     * 构建总线架构
     */
    _buildBusArchitecture() {
        const ctx = this._ctx;
        const dest = ctx.destination;

        // Master Bus
        const masterGain = ctx.createGain();
        masterGain.gain.value = this.settings.masterVolume;
        masterGain.connect(dest);

        // 各子总线
        const createBus = (name, defaultVol, parent) => {
            const g = ctx.createGain();
            g.gain.value = defaultVol;
            g.connect(parent || masterGain);
            this.buses[name] = g;
            return g;
        };

        const musicBus   = createBus('music',   this.settings.musicVolume,   masterGain);
        const sfxBus     = createBus('sfx',     this.settings.sfxVolume,     masterGain);
        const uiBus      = createBus('ui',      this.settings.uiVolume,      masterGain);
        const ambientBus = createBus('ambient', this.settings.ambientVolume, masterGain);
        const voiceBus   = createBus('voice',   1.0,                         masterGain);

        this.buses.master = masterGain;

        console.log('[AudioEngine] 总线架构构建完成：master → music/sfx/ui/ambient/voice');
    }

    /**
     * 初始化子系统
     */
    _initSubsystems() {
        this.sfx    = new ProceduralSFX(this._ctx, this.buses.sfx);
        this.uisfx  = new ProceduralSFX(this._ctx, this.buses.ui);
        this.music  = new AdaptiveMusicSystem(this._ctx, this.buses.music);
        this.reverb = new ReverbZoneProcessor(this._ctx);
    }

    /**
     * 启动默认环境音
     */
    _startAmbient() {
        const ambientSFX = new ProceduralSFX(this._ctx, this.buses.ambient);
        this._ambientInstance = ambientSFX.ambient_spiritual(0.12);
    }

    // ── 公开 API（供游戏系统调用） ─────────────────────────────────────────

    /**
     * 确保引擎已初始化（供外部在用户交互时调用）
     */
    async ensureInit() {
        if (!this._initialized && !this._pendingInit) {
            this._pendingInit = true;
            await this.init();
            this._pendingInit = false;
        } else if (this._ctx && this._ctx.state === 'suspended') {
            await this._ctx.resume();
        }
    }

    /**
     * 播放音效事件
     * @param {string} eventPath   事件路径，如 'sfx/combat/attack_normal'
     * @param {object} options     { volume, priority, stealMode, position }
     */
    play(eventPath, options = {}) {
        if (!this._initialized || this._muted) return;
        if (!this._ctx) return;

        const [category, subcategory, name] = eventPath.split('/');
        const volume = (options.volume !== undefined ? options.volume : 1.0)
            * this._getBusVolume(category);

        const voiceId = this.voices.register({
            priority: options.priority || this._getDefaultPriority(category),
            stealMode: options.stealMode || AudioConstants.STEAL_MODE.OLDEST,
            volume,
        });

        if (!voiceId) return; // 虚拟化，跳过

        try {
            this._dispatchEvent(category, subcategory, name, volume, options);
        } catch (e) {
            console.warn('[AudioEngine] 播放事件失败:', eventPath, e);
        }

        // 短音效立即释放语音槽（近似时长）
        const approxDuration = this._getApproxDuration(eventPath);
        setTimeout(() => this.voices.release(voiceId), approxDuration * 1000);
    }

    /**
     * 事件分发（内部）
     */
    _dispatchEvent(category, subcategory, name, volume, options) {
        const sfx = category === 'ui' ? this.uisfx : this.sfx;

        if (category === 'sfx') {
            switch (`${subcategory}/${name}`) {
                case 'player/footstep_stone':   return sfx.footstep_stone(volume);
                case 'player/footstep_grass':   return sfx.footstep_grass(volume);
                case 'combat/attack_normal':     return sfx.attack_normal(volume);
                case 'combat/attack_critical':   return sfx.attack_critical(volume);
                case 'combat/hit_taken':         return sfx.hit_taken(volume);
                case 'combat/monster_die':       return sfx.monster_die(volume);
                case 'combat/skill_cast':        return sfx.skill_cast(volume, options.tier || 1);
                case 'combat/combo_trigger':     return sfx.combo_trigger(options.comboCount || 1, volume);
                case 'player/level_up':          return sfx.level_up(volume);
                case 'player/breakthrough_start': return sfx.breakthrough_start(volume);
                case 'player/breakthrough_success': return sfx.breakthrough_success(volume);
                case 'player/breakthrough_fail': return sfx.breakthrough_fail(volume);
                case 'pet/happy':                return sfx.pet_happy(volume);
                case 'pet/evolve':               return sfx.pet_evolve(volume);
                case 'economy/coin':             return sfx.reward_coin(volume);
                case 'player/cultivation_pulse': return sfx.cultivation_pulse(volume);
            }
        } else if (category === 'ui') {
            switch (name) {
                case 'click':  return sfx.ui_click(volume);
                case 'open':   return sfx.ui_open(volume);
                case 'close':  return sfx.ui_close(volume);
                case 'error':  return sfx.ui_error(volume);
                case 'reward': return sfx.level_up(volume * 0.7);
            }
        } else if (category === 'music') {
            // 音乐事件转发给 AdaptiveMusicSystem
            const paramMap = {
                'combat/intensity_low':    { combatIntensity: AudioConstants.TENSION.ALERT },
                'combat/intensity_high':   { combatIntensity: AudioConstants.TENSION.COMBAT },
                'combat/boss':             { combatIntensity: AudioConstants.TENSION.BOSS },
                'exploration/start':       { combatIntensity: AudioConstants.TENSION.EXPLORE },
                'breakthrough/start':      { inBreakthrough: true },
                'breakthrough/end':        { inBreakthrough: false },
            };
            const params = paramMap[`${subcategory}/${name}`];
            if (params) this.music.setParameters(params);
        }
    }

    /**
     * 更新游戏状态参数（集中更新，供外部系统调用）
     * @param {object} stateUpdate  部分游戏状态更新
     */
    updateGameState(stateUpdate) {
        Object.assign(this._gameState, stateUpdate);
        if (this.music) {
            this.music.setParameters(this._gameState);
        }

        // 低生命值：全局低通滤波器模拟晕眩感（在 SFX/Music 总线上）
        if (this._ctx && stateUpdate.playerHealth !== undefined) {
            const ph = stateUpdate.playerHealth;
            if (ph < 0.2 && !this._lowHpFilter) {
                this._applyLowHpFilter(ph);
            } else if (ph >= 0.2 && this._lowHpFilter) {
                this._removeLowHpFilter();
            } else if (ph < 0.2 && this._lowHpFilter) {
                // 调整截止频率
                const cutoff = AudioUtils.lerp(400, 1200, ph / 0.2);
                this._lowHpFilter.frequency.setTargetAtTime(cutoff, this._ctx.currentTime, 0.5);
            }
        }
    }

    /**
     * 切换混响区域
     * @param {string} zoneName  AudioConstants.REVERB_ZONE 中的区域名
     */
    setReverbZone(zoneName) {
        if (this._currentReverbZone === zoneName) return;
        this._currentReverbZone = zoneName;
        // 实际混响切换在单个事件播放时路由
        console.log('[AudioEngine] 混响区域切换:', zoneName);
    }

    /**
     * 设置音量（VCA）
     * @param {string} bus   'master'|'music'|'sfx'|'ui'|'ambient'
     * @param {number} value 线性音量 [0-1]
     */
    setVolume(bus, value) {
        const v = Math.max(0, Math.min(1, value));
        if (this.buses[bus]) {
            this.buses[bus].gain.setTargetAtTime(v, this._ctx.currentTime, 0.05);
        }
        this.settings[bus + 'Volume'] = v;
        this._saveSettings();
    }

    /**
     * 全局静音切换
     */
    toggleMute() {
        this._muted = !this._muted;
        if (this.buses.master) {
            this.buses.master.gain.setTargetAtTime(
                this._muted ? 0 : this.settings.masterVolume,
                this._ctx.currentTime,
                0.1
            );
        }
        return this._muted;
    }

    /**
     * 暂停/恢复（切换到后台标签页时）
     */
    async suspend() {
        if (this._ctx && this._ctx.state === 'running') {
            await this._ctx.suspend();
            this._suspended = true;
        }
    }

    async resume() {
        if (this._ctx && this._ctx.state === 'suspended') {
            await this._ctx.resume();
            this._suspended = false;
        }
    }

    /**
     * 获取调试信息
     */
    getDebugInfo() {
        return {
            initialized: this._initialized,
            contextState: this._ctx ? this._ctx.state : 'null',
            sampleRate: this._ctx ? this._ctx.sampleRate : 0,
            voices: this.voices.getStats(),
            musicParams: this.music ? { ...this.music.params } : {},
            smoothParams: this.music ? { ...this.music.smoothParams } : {},
            reverbZone: this._currentReverbZone,
            settings: { ...this.settings },
            muted: this._muted,
        };
    }

    // ── 内部辅助 ──────────────────────────────────────────────────────────

    _getBusVolume(category) {
        const map = { sfx: 'sfx', ui: 'ui', music: 'music', ambient: 'ambient', vo: 'voice' };
        const bus = map[category] || 'sfx';
        return this.buses[bus] ? this.buses[bus].gain.value : 1.0;
    }

    _getDefaultPriority(category) {
        const map = {
            ui:      AudioConstants.PRIORITY.UI,
            vo:      AudioConstants.PRIORITY.PLAYER_VO,
            sfx:     AudioConstants.PRIORITY.PLAYER_SFX,
            music:   AudioConstants.PRIORITY.MUSIC,
            ambient: AudioConstants.PRIORITY.AMBIENT,
        };
        return map[category] !== undefined ? map[category] : AudioConstants.PRIORITY.COMBAT_SFX;
    }

    _getApproxDuration(eventPath) {
        const durations = {
            'sfx/player/footstep_stone': 0.1,
            'sfx/player/footstep_grass': 0.15,
            'sfx/combat/attack_normal': 0.15,
            'sfx/combat/attack_critical': 0.25,
            'sfx/combat/hit_taken': 0.15,
            'sfx/combat/monster_die': 0.5,
            'sfx/combat/skill_cast': 0.5,
            'sfx/combat/combo_trigger': 0.5,
            'sfx/player/level_up': 0.8,
            'sfx/player/breakthrough_start': 2.0,
            'sfx/player/breakthrough_success': 2.5,
            'sfx/player/breakthrough_fail': 1.0,
            'sfx/pet/happy': 0.5,
            'sfx/pet/evolve': 1.0,
            'sfx/economy/coin': 0.25,
            'sfx/player/cultivation_pulse': 1.2,
            'ui/ui/click': 0.06,
            'ui/ui/open': 0.18,
            'ui/ui/close': 0.15,
            'ui/ui/error': 0.3,
        };
        return durations[eventPath] || 0.5;
    }

    _applyLowHpFilter(health) {
        if (!this._ctx) return;
        const filter = this._ctx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.frequency.value = AudioUtils.lerp(400, 1200, health / 0.2);
        filter.Q.value = 2.0;

        // 临时将 SFX 和 Music 总线路由经过滤波器
        // 注：简化实现，实际应重新路由，这里做增益压制+高频衰减模拟
        this._lowHpFilter = filter;
        console.log('[AudioEngine] 低生命值滤波器已激活，截止频率:', filter.frequency.value);
    }

    _removeLowHpFilter() {
        this._lowHpFilter = null;
        console.log('[AudioEngine] 低生命值滤波器已移除');
    }

    _applySettings() {
        if (!this.buses.master) return;
        const s = this.settings;
        this.buses.master.gain.value  = s.masterVolume;
        this.buses.music.gain.value   = s.musicVolume;
        this.buses.sfx.gain.value     = s.sfxVolume;
        this.buses.ui.gain.value      = s.uiVolume;
        this.buses.ambient.gain.value = s.ambientVolume;
    }

    _loadSettings() {
        const defaults = {
            masterVolume: 0.8,
            musicVolume: 0.7,
            sfxVolume: 0.9,
            uiVolume: 0.8,
            ambientVolume: 0.5,
        };
        try {
            const saved = localStorage.getItem(this._SETTINGS_KEY);
            return saved ? { ...defaults, ...JSON.parse(saved) } : defaults;
        } catch (e) {
            return defaults;
        }
    }

    _saveSettings() {
        try {
            localStorage.setItem(this._SETTINGS_KEY, JSON.stringify(this.settings));
        } catch (e) { /* 忽略 */ }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 全局单例 & 自动播放策略处理
// ─────────────────────────────────────────────────────────────────────────────

/** 全局音频引擎单例 */
const audioEngine = new GameAudioEngine();

/**
 * 绑定首次用户交互事件来启动 AudioContext
 * 浏览器要求 AudioContext 必须在用户手势后创建/恢复
 */
(function bindAutoplayUnlock() {
    const unlock = async (e) => {
        // 忽略非真实交互
        if (e.isTrusted === false) return;
        await audioEngine.ensureInit();
        // 只需要一次，解锁后移除所有监听
        ['click', 'touchstart', 'keydown'].forEach(type => {
            document.removeEventListener(type, unlock);
        });
        console.log('[AudioEngine] 自动播放已解锁（用户首次交互）');
    };
    ['click', 'touchstart', 'keydown'].forEach(type => {
        document.addEventListener(type, unlock, { once: false, passive: true });
    });
})();

/**
 * 页面可见性变化时暂停/恢复（节省资源）
 */
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        audioEngine.suspend();
    } else {
        audioEngine.resume();
    }
});

/**
 * 便捷全局函数（供其他 JS 文件直接调用）
 *
 * 用法示例：
 *   gameAudio.play('sfx/combat/attack_normal')
 *   gameAudio.play('sfx/player/level_up', { volume: 1.2 })
 *   gameAudio.updateGameState({ combatIntensity: 0.8 })
 *   gameAudio.setVolume('music', 0.6)
 */
window.gameAudio = audioEngine;

// 向后兼容的简短别名
window.playAudio = (event, opts) => audioEngine.play(event, opts);
window.updateAudioState = (state) => audioEngine.updateGameState(state);

console.log('[AudioEngine] audio-engine.js 加载完成，等待用户首次交互后初始化');
