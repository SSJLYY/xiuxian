# 修仙挂机游戏 — 音频设计文档 (ADD)

**版本**: v1.0  
**日期**: 2026-03-24  
**作者**: shaun.sheng  
**状态**: 已实现 — 三个核心文件交付完毕

---

## 目录

1. [音频身份定义](#1-音频身份定义)
2. [架构概览](#2-架构概览)
3. [混音器总线设计](#3-混音器总线设计)
4. [事件命名规范](#4-事件命名规范)
5. [自适应音乐系统](#5-自适应音乐系统)
6. [程序化音效库](#6-程序化音效库)
7. [空间音频配置](#7-空间音频配置)
8. [音频预算规格](#8-音频预算规格)
9. [游戏状态 → 音频映射](#9-游戏状态--音频映射)
10. [性能规格](#10-性能规格)
11. [开发者工具](#11-开发者工具)
12. [集成 API 参考](#12-集成-api-参考)

---

## 1. 音频身份定义

### 1.1 三个形容词

> **古朴 · 空灵 · 张弛有度**

- **古朴**：东方五声音阶、钟磬泛音、低频颤鸣——听觉上感受到千年修炼传承的厚重
- **空灵**：修炼状态采用极简长尾混响与柔和泛音，画面外的"灵气流动"感
- **张弛有度**：探索时平静如水，战斗时紧张剧烈，突破时庄严肃穆——情绪对比强烈但过渡自然

### 1.2 音频声音特征矩阵

| 游戏场景      | 频率重心   | 节拍感   | 空间感   | 情绪目标       |
|-------------|-----------|---------|---------|---------------|
| 探索/修炼    | 低频为主   | 无明显节拍 | 宽广、深远 | 平静、专注     |
| 战斗初期     | 中低频     | 弱节拍   | 中等     | 警觉、兴奋     |
| 激烈战斗     | 全频段     | 强节拍   | 收窄     | 紧张、热血     |
| Boss/危急   | 低频轰鸣   | 强烈节拍  | 压迫     | 绝境感、肾上腺素 |
| 境界突破     | 钟磬高频   | 庄重慢节拍 | 极宽混响 | 仪式感、蜕变   |
| UI/菜单     | 中高频     | 无       | 干声     | 清脆、即时     |

### 1.3 参考声音档案

> （以下为风格参考，不包含版权内容）

- **探索/修炼**：类似《仙剑奇侠传》古风弦乐 + 自然环境层
- **战斗**：类似《鬼泣》节奏驱动 + 东方打击乐融合
- **突破仪式**：类似《尼尔：机械纪元》仪式感人声 + 钟鸣
- **程序化实现**：全部使用 Web Audio API 合成，零外部依赖

---

## 2. 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                      游戏系统层（业务代码）                        │
│  game.js / enhanced_combat.js / breakthrough-evolution.js / ...  │
└────────────────────────┬────────────────────────────────────────┘
                         │  语义化事件 API（见§12）
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                   音频集成层（audio-integration.js）              │
│  CombatAudioController  CultivationAudioController               │
│  BreakthroughAudioController  PetAudioController                 │
│  UIAudioController  EconomyAudioController  PlayerStateSync      │
└────────────────────────┬────────────────────────────────────────┘
                         │  play(eventPath) / updateGameState()
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                   音频引擎层（audio-engine.js）                   │
│  ┌──────────────┐ ┌──────────────────┐ ┌──────────────────────┐ │
│  │ VoiceManager │ │ AdaptiveMusicSys │ │  ProceduralSFX       │ │
│  │ 语音预算管理  │ │ 参数驱动音乐层    │ │  15种程序化音效       │ │
│  └──────────────┘ └──────────────────┘ └──────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────────┤
│  │              Web Audio API 混音器总线架构                      │
│  │  Master ─┬─ Music Bus   ← 自适应音乐                          │
│  │           ├─ SFX Bus     ← 战斗/修炼/宠物音效                  │
│  │           ├─ UI Bus      ← UI音效（零延迟）                    │
│  │           ├─ Ambient Bus ← 灵气环境音                         │
│  │           └─ Voice Bus   ← NPC语音（预留）                    │
│  └──────────────────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│               音频设置 UI（audio-settings.js）                    │
│  五路 VCA 滑块 / 静音按钮 / 音效预览 / 开发者调试 HUD             │
└─────────────────────────────────────────────────────────────────┘
```

### 技术选型

| 技术       | 选择         | 理由                                       |
|-----------|-------------|-------------------------------------------|
| 音频引擎   | Web Audio API 原生 | 零依赖、全平台支持、低延迟、精确时间控制    |
| 音乐中间件 | 自实现 FMOD 风格事件系统 | 无需外部 CDN，适合挂机游戏流量              |
| 音效生成   | 程序化合成（振荡器+滤波器） | 零资源文件，包体不增加，动态变化无重复感    |
| 空间音频   | ConvolverNode IR + BiquadFilter | 模拟 FMOD 混响区域，遮挡效果              |

---

## 3. 混音器总线设计

### 3.1 总线架构

```
AudioContext.destination
└── Master Bus (masterGain)  VCA: masterVolume
      ├── Music Bus           VCA: musicVolume    DSP预算: 0.8ms
      ├── SFX Bus             VCA: sfxVolume      DSP预算: 0.6ms
      ├── UI Bus              VCA: uiVolume       DSP预算: 0.1ms（零延迟）
      ├── Ambient Bus         VCA: ambientVolume  DSP预算: 0.3ms
      └── Voice Bus           VCA: voiceVolume    DSP预算: 0.2ms（预留）
```

### 3.2 默认 VCA 值

| VCA           | 默认值 | 范围  | 说明                         |
|--------------|--------|-------|------------------------------|
| masterVolume | 0.8    | 0-1   | 主音量                       |
| musicVolume  | 0.7    | 0-1   | 背景音乐                     |
| sfxVolume    | 0.9    | 0-1   | 游戏音效（战斗优先级高）       |
| uiVolume     | 0.8    | 0-1   | UI交互音效                   |
| ambientVolume| 0.5    | 0-1   | 环境音（灵气氛围声）           |

### 3.3 低生命值滤波器

- 触发条件：playerHealth < 0.2
- 效果：全局低通滤波器，截止频率从 1200Hz 线性降至 400Hz
- 目的：模拟玩家虚弱/濒死的晕眩感，强化危机紧迫感

---

## 4. 事件命名规范

所有音频事件通过事件路径字符串触发，结构映射自 FMOD Studio 路径：

```
[category]/[subcategory]/[event_name]
```

### 4.1 已实现事件完整列表

```
# 战斗音效
sfx/combat/attack_normal       普通攻击     ADPCM 等效  优先级 1
sfx/combat/attack_critical     暴击攻击     ADPCM 等效  优先级 1
sfx/combat/hit_taken           玩家受击     ADPCM 等效  优先级 1
sfx/combat/monster_die         怪物死亡     ADPCM 等效  优先级 2
sfx/combat/skill_cast          技能释放     程序化合成  优先级 1  参数: tier(1-5)
sfx/combat/combo_trigger       连招触发     程序化合成  优先级 1  参数: comboCount

# 玩家音效
sfx/player/footstep_stone      脚步(石板)   程序化合成  优先级 2
sfx/player/footstep_grass      脚步(草地)   程序化合成  优先级 2
sfx/player/level_up            升级         程序化合成  优先级 0
sfx/player/cultivation_pulse   修炼脉冲     程序化合成  优先级 3
sfx/player/breakthrough_start  突破开始     程序化合成  优先级 0  持续2.0s
sfx/player/breakthrough_success 突破成功    程序化合成  优先级 0  持续2.5s
sfx/player/breakthrough_fail   突破失败     程序化合成  优先级 0  持续1.0s

# 宠物音效
sfx/pet/happy                  宠物喜悦     程序化合成  优先级 2
sfx/pet/evolve                 宠物进化     程序化合成  优先级 0

# 经济音效
sfx/economy/coin               灵石获得     程序化合成  优先级 3

# UI音效（通过 ui/ 前缀路由到 UI Bus）
ui/ui/click                    按钮点击     程序化合成  优先级 0
ui/ui/open                     弹窗打开     程序化合成  优先级 0
ui/ui/close                    弹窗关闭     程序化合成  优先级 0
ui/ui/error                    错误提示     程序化合成  优先级 0
ui/ui/reward                   奖励提示     程序化合成  优先级 0

# 音乐控制事件（转发到 AdaptiveMusicSystem）
music/combat/intensity_low     轻度战斗
music/combat/intensity_high    激烈战斗
music/combat/boss              Boss战
music/exploration/start        进入探索
music/breakthrough/start       突破开始
music/breakthrough/end         突破结束
```

### 4.2 事件优先级层级

| 优先级 | 类型            | 抢占策略    | 说明                    |
|-------|----------------|------------|------------------------|
| 0 (高) | UI、升级、突破 | 从不抢占   | 重要反馈，必须播放        |
| 1      | 玩家战斗音效   | 抢占最旧   | 攻击/受击               |
| 2      | 宠物、脚步声   | 抢占最安静 | 可被战斗音效抢占          |
| 3 (低) | 环境、经济     | 抢占最旧   | 非关键，语音紧张时丢弃    |
| 4      | 音乐           | 独立总线   | 不参与语音竞争            |

---

## 5. 自适应音乐系统

### 5.1 参数集

```
combatIntensity  [0.0 - 1.0]  战斗紧张度
  0.0  = 探索/修炼 — 仅 Drone + Melody 层
  0.3  = 敌人警戒  — Rhythm 层渐入
  0.6  = 激烈战斗  — Combat 层全入
  0.9+ = Boss/危急 — Danger 层压顶

realmLevel       [0 - 3]      修炼境界（影响 Drone 基音）
  0 = 练气  55 Hz (A1)
  1 = 筑基  73.4 Hz (D2)
  2 = 金丹  82.4 Hz (E2)
  3 = 元婴  110 Hz (A2)

playerHealth     [0.0 - 1.0]  玩家生命值
  < 0.2 → Danger 层激活 + 全局低通滤波器

inBreakthrough   boolean      境界突破进行中
  true → 所有层静音，Breakthrough 层独占

timeOfDay        [0.0 - 1.0]  游戏时间（预留，当前未完全实现）
```

### 5.2 音乐层架构

| 层名        | 触发条件              | 音量范围     | 内容描述                    |
|------------|----------------------|------------|----------------------------|
| Drone      | 始终播放（突破时除外）  | 0.18        | 三层正弦叠加垫音，境界基音   |
| Melody     | intensity < 0.6 时为主 | 0.0-0.70   | 五声音阶程序化即兴旋律       |
| Rhythm     | intensity > 0.25      | 0.0-0.60   | 程序化打击乐（Kick+HiHat）  |
| Combat     | intensity > 0.55      | 0.0-0.70   | 低频行进音型，dim 和弦       |
| Danger     | intensity > 0.88 或 health < 0.2 | 0.0-0.65 | 低频颤音威胁音效 |
| Breakthrough | inBreakthrough=true  | 0.75        | 钟鸣泛音序列，庄重仪式感     |

### 5.3 过渡规则

- **节拍同步**：所有层音量变化使用 `setTargetAtTime(target, nextBeatTime, tau)` — 在节拍边界开始，3倍时间常数过渡
- **平滑参数**：combatIntensity 使用 α=0.3 的指数平滑，防止跳变
- **BPM 联动**：intensity < 0.3 时 BPM=76，intensity ≥ 0.6 时 BPM=132，中间线性插值
- **禁止硬切**：除场景重置外，任何时间都不得使用 `gain.value = X` 直接赋值

### 5.4 东方调式配置

```
主音阶：C 大调五声音阶（宫调式）
  宫 = C (261.63 Hz)
  商 = D (293.66 Hz)
  角 = E (329.63 Hz)
  徵 = G (392.00 Hz)
  羽 = A (440.00 Hz)

Drone 基音（随境界）：
  练气 = A1 (55 Hz)     — 低沉稳重
  筑基 = D2 (73.4 Hz)   — 趋于宽厚
  金丹 = E2 (82.4 Hz)   — 和声丰满
  元婴 = A2 (110 Hz)    — 空灵通透

Combat 动机（减七和弦）：
  D2 → C#2 → C2 → D2   (紧张行进)
```

---

## 6. 程序化音效库

所有音效 100% 程序化合成，不依赖任何外部音频文件。

### 6.1 合成技术映射

| 音效         | 合成技术                    | 随机化参数                    |
|-------------|---------------------------|------------------------------|
| 脚步(石板)   | 噪声 burst + 高通           | 音量±20%                     |
| 脚步(草地)   | 软噪声 + 低通               | 音量±30%，时长±20%            |
| 普通攻击     | 锯齿波瞬态 + 噪声层          | 固定                          |
| 暴击攻击     | 方波 + 金属谐振叠加          | 固定                          |
| 受击         | 低频正弦下行                 | 固定                          |
| 技能释放     | 正弦 + 三角波上扫频          | 基频随 tier(1-5) 缩放         |
| 连招触发     | 三连音上行方波               | 基频随 comboCount 提升        |
| 宠物喜悦     | C5-E5-G5 三音琶音           | 固定                          |
| 宠物进化     | C4→C6 七音上行阶梯           | 固定                          |
| 修炼脉冲     | 80Hz 柔和正弦                | 固定                          |
| 升级         | 三音上行三角波               | 固定                          |
| 突破开始     | 低频锯波共鸣 + 方波撞击       | 固定                          |
| 突破成功     | 五音 C 大调和弦琶音           | 固定                          |
| 突破失败     | 锯波下行 + 过载失真           | 固定                          |
| 灵石音效     | 双正弦高频碰撞               | 频率随机±200Hz                |

### 6.2 音效随机化策略

每类音效都启用变体随机化，确保**同一音效不会连续两次听起来完全相同**：

- 脚步声：音量 ±20-30%
- 灵石：基频 ±200Hz
- 技能释放：基频随 tier 参数线性缩放

---

## 7. 空间音频配置

### 7.1 混响区域规格

| 区域      | 预延迟  | 衰减时间 | 湿声比  | 使用场景               |
|----------|--------|---------|--------|----------------------|
| outdoor  | 20ms   | 0.8s    | 15%    | 野外战斗、探索地图       |
| indoor   | 30ms   | 1.5s    | 35%    | 宗门大厅、商店、主界面   |
| cave     | 50ms   | 3.5s    | 60%    | 叙事/传说区域、剧情场景 |
| metal    | 15ms   | 1.0s    | 45%    | 装备强化、铸造系统       |
| void     | 80ms   | 5.0s    | 70%    | 修炼空间、境界突破      |

### 7.2 模块→混响映射

| 游戏模块      | 混响区域  |
|-------------|---------|
| dashboard   | indoor  |
| combat      | outdoor |
| pets        | outdoor |
| skills      | void    |
| guild       | indoor  |
| narrative   | cave    |
| lore        | cave    |
| inventory   | indoor  |
| 修炼进行中   | void    |

### 7.3 遮挡实现规范

> **当前版本（浏览器 2D 游戏）**：
> 由于游戏无 3D 世界空间，遮挡通过 BiquadFilter 低通滤波模拟：
> - 完全遮挡：截止频率 800Hz（等效通过障碍物）
> - 半遮挡：截止频率 2000Hz
> - 无遮挡：无滤波器

---

## 8. 音频预算规格

### 8.1 语音预算（浏览器平台）

| 类型     | 数量  | 说明                          |
|---------|-------|-------------------------------|
| 实体语音 | 32   | 同时播放的最大音频节点数        |
| 虚拟语音 | 64   | 超出限制时跳过播放（不报错）    |
| 当前利用率警告阈值 | 85% | 调试 HUD 以黄色高亮   |
| 当前利用率危险阈值 | 100% | 调试 HUD 以红色高亮，触发抢占  |

### 8.2 音效时长与语音槽占用

| 音效类别     | 近似时长   | 语音占用时间 |
|------------|----------|------------|
| UI 点击     | 60ms     | 60ms       |
| 脚步声      | 100-150ms | 150ms     |
| 攻击/受击   | 150-250ms | 250ms     |
| 技能/连招   | 400-500ms | 500ms     |
| 升级        | 800ms    | 800ms      |
| 突破系列    | 1-2.5s   | 实际时长   |
| 宠物音效    | 400-700ms | 700ms     |
| 环境音      | 持续      | 持续占用1槽 |

### 8.3 CPU 预算目标

| 子系统          | 目标预算    |
|----------------|-----------|
| 自适应音乐层     | ≤ 0.8ms/frame |
| 程序化音效合成   | ≤ 0.6ms/frame |
| 混响卷积         | ≤ 0.4ms/frame |
| 参数更新循环     | ≤ 0.1ms/frame |
| **总计**        | **≤ 2.0ms/frame** |

---

## 9. 游戏状态 → 音频映射

### 9.1 战斗系统

```
战斗开始          → combatIntensity = 0.6
                   → 混响切换为 outdoor
普通攻击回合      → play('sfx/combat/attack_normal')
暴击回合          → play('sfx/combat/attack_critical')
玩家受击          → play('sfx/combat/hit_taken')
技能释放          → play('sfx/combat/skill_cast', { tier })
连续3击以上       → play('sfx/combat/combo_trigger', { comboCount })
怪物死亡          → play('sfx/combat/monster_die')
战斗胜利          → combatIntensity → 0.1（500ms后）
升级              → play('sfx/player/level_up')
战斗失败          → combatIntensity → 0.0（300ms后）
```

### 9.2 修炼系统

```
开始修炼          → combatIntensity = 0.0
                   → setReverbZone('void')
                   → play('sfx/player/cultivation_pulse')
                   → 启动4秒脉冲定时器
停止修炼          → setReverbZone('outdoor')
                   → 停止脉冲定时器
```

### 9.3 境界突破

```
检测到可突破      → play('sfx/combat/skill_cast', { tier: 3 })（提示音）
点击突破按钮      → inBreakthrough = true
                   → setReverbZone('void')
                   → play('sfx/player/breakthrough_start')
突破成功          → play('sfx/player/breakthrough_success')（600ms后）
                   → inBreakthrough = false
                   → realmLevel += 1
                   → setReverbZone('outdoor')
突破失败          → play('sfx/player/breakthrough_fail')（600ms后）
                   → inBreakthrough = false
                   → setReverbZone('outdoor')
```

### 9.4 宠物系统

```
喂食宠物          → play('sfx/pet/happy')
宠物进化开始      → play('sfx/combat/skill_cast', { tier: 4 })
宠物进化成功      → play('sfx/pet/evolve')（400ms后）
```

### 9.5 经济系统

```
灵石增加 1-99    → play('sfx/economy/coin') × 1
灵石增加 100-299 → play('sfx/economy/coin') × 2（70ms间隔）
灵石增加 300+    → play('sfx/economy/coin') × 3-4
```

### 9.6 玩家状态

```
playerHealth < 0.2  → Danger 层音乐激活
                      → 全局低通滤波器激活（截止 400-1200Hz）
playerHealth ≥ 0.2  → 低通滤波器移除
                      → Danger 层音量归零
```

---

## 10. 性能规格

### 10.1 浏览器自动播放策略处理

- **解锁方式**：监听 `click`、`touchstart`、`keydown` 事件（`passive: true`）
- **触发时机**：首次用户交互后调用 `audioEngine.ensureInit()`
- **失败回退**：AudioContext 不可用时静默禁用，不影响游戏逻辑

### 10.2 后台标签页优化

- 页面 hidden 时：`AudioContext.suspend()`（停止 DSP 计算）
- 页面可见时：`AudioContext.resume()`（恢复）
- 实现：`document.addEventListener('visibilitychange', ...)`

### 10.3 内存管理

- 所有程序化音效节点在播放完成后自动 GC（`stop()` 后解除引用）
- 混响 IR 缓冲区一次性生成，全生命周期复用
- 自适应音乐振荡器持续运行（通过增益节点控制音量），不重复创建/销毁

### 10.4 已知限制

| 限制                        | 原因                              | 缓解措施                      |
|---------------------------|----------------------------------|------------------------------|
| 首次加载有 ~50ms 延迟        | AudioContext 初始化开销            | 首次交互后异步初始化            |
| Safari 不支持 AudioContext  | 兼容性问题                         | 静默降级，不影响游戏            |
| 多标签页多实例               | 每个页面独立 AudioContext          | localStorage 同步音量设置      |

---

## 11. 开发者工具

### 11.1 调试 HUD（Alt+D 激活）

实时显示：
- AudioContext 状态（running/suspended）
- 活跃语音数 / 最大语音数 / 利用率进度条
- 总计播放次数 / 抢占次数 / 虚拟化次数
- 当前音乐参数（combatIntensity 原始值 + 平滑值）
- 境界等级、生命值、突破状态
- 当前混响区域

颜色编码：
- 白色 → 正常
- 黄色 → 警告（利用率 > 65%，或健康值 < 0.5）
- 红色 → 危险（利用率 > 85%，或健康值 < 0.2）

### 11.2 快捷键

| 快捷键  | 功能                    |
|--------|------------------------|
| M      | 切换全局静音             |
| Alt+A  | 打开/关闭音频设置面板     |
| Alt+D  | 切换调试 HUD（开发者模式）|

### 11.3 控制台 API

```javascript
// 获取调试信息
window.gameAudio.getDebugInfo()

// 手动触发任意音效
window.gameAudio.play('sfx/player/level_up')
window.gameAudio.play('sfx/combat/attack_critical', { volume: 1.2 })

// 更新游戏状态
window.gameAudio.updateGameState({ combatIntensity: 0.8 })
window.gameAudio.updateGameState({ inBreakthrough: true })

// 设置音量
window.gameAudio.setVolume('music', 0.5)

// 切换混响区域
window.gameAudio.setReverbZone('cave')

// 语音预算统计
window.gameAudio.voices.getStats()

// 触发语义化游戏事件
window.gameAudioEvent('breakthrough:success')
window.gameAudioEvent('player:health', { ratio: 0.1 })
```

---

## 12. 集成 API 参考

### 12.1 直接调用（推荐）

```javascript
// 播放音效事件
gameAudio.play(eventPath, options)
// options: { volume, priority, stealMode, tier, comboCount }

// 更新游戏状态（驱动自适应音乐）
gameAudio.updateGameState({ combatIntensity, realmLevel, playerHealth, inBreakthrough })

// 设置混响区域
gameAudio.setReverbZone(zoneName)
// zoneName: 'outdoor'|'indoor'|'cave'|'metal'|'void'

// 音量控制
gameAudio.setVolume(bus, value)
// bus: 'master'|'music'|'sfx'|'ui'|'ambient'
```

### 12.2 语义化事件 API（可选，更简洁）

```javascript
// 触发游戏语义事件（音频集成层处理所有细节）
gameAudioEvent('combat:start')
gameAudioEvent('combat:end')
gameAudioEvent('combat:intensity', { value: 0.7 })
gameAudioEvent('cultivation:start')
gameAudioEvent('cultivation:stop')
gameAudioEvent('breakthrough:attempt')
gameAudioEvent('breakthrough:success')
gameAudioEvent('breakthrough:fail')
gameAudioEvent('pet:feed')
gameAudioEvent('pet:evolve')
gameAudioEvent('player:level_up')
gameAudioEvent('player:health', { ratio: 0.15 })
gameAudioEvent('reward:offline')
```

### 12.3 自动触发（零配置）

以下行为由音频集成层自动检测，**无需任何手动调用**：

- 所有 `<button>` 点击 → `ui/ui/click`
- 弹窗 DOM 插入/移除 → `ui/ui/open` / `ui/ui/close`
- `showToast('msg', 'error')` → `ui/ui/error`
- `showToast('msg', 'success')` → `ui/ui/reward`
- 模块切换 (`showModule`) → 自动更新混响区域
- 灵石数量 DOM 变化 → `sfx/economy/coin`
- 战斗结果 (`displayCombatResult`) → 完整战斗音效链
- 玩家血量 DOM 变化 → 同步到音乐系统

---

## 附录 A：文件清单

| 文件                                          | 说明             | 大小约  |
|---------------------------------------------|-----------------|--------|
| `src/main/resources/static/js/audio-engine.js` | 音频引擎核心     | ~900行  |
| `src/main/resources/static/js/audio-integration.js` | 游戏集成层  | ~500行  |
| `src/main/resources/static/js/audio-settings.js` | 设置UI系统  | ~400行  |
| `AUDIO-DESIGN-DOCUMENT.md`                  | 本文档           | —       |

**总计新增代码**: ~1800行，无外部依赖，无额外网络请求

---

## 附录 B：后续扩展路线

### Phase 2（可选，需真实音频资源）
- 接入真实音频文件（推荐格式：OGG Vorbis）
- 实现真实的 3D 距离衰减（适合地图探索模式）
- NPC 语音系统（Voice Bus 已预留）
- 音频事件的 A/B 测试框架

### Phase 3（平台扩展）
- Mobile 优化：语音预算降至 24，关闭混响卷积
- PWA 音频离线支持：Service Worker 缓存音频配置

---

*本文档由 GameAudioEngineer 智能体生成，与代码实现严格同步。*  
*每次音频系统重大修改后，请同步更新本文档的对应章节。*
