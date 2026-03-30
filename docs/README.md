# 修仙挂机游戏 — 开发文档中心

> 本目录是项目唯一的技术文档入口。所有开发规范、架构说明、API参考均在此维护。

**维护者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-27

---

## 📂 文档目录

### 🚀 快速入门
| 文档 | 说明 | 适合谁 |
|------|------|--------|
| [快速上手指南](./guides/GETTING-STARTED.md) | 30分钟从零跑通项目 | 所有新成员 |
| [前端开发指南](./guides/FRONTEND-GUIDE.md) | 前端模块结构与开发规范 | 前端开发 |
| [前端重构指南](./frontend-refactoring-guide.md) | 模块化架构、分层设计、迁移方案 | 前端开发 |
| [前端架构对比](./frontend-architecture-comparison.md) | 新旧架构详细对比 | 前端开发、架构师 |
| [前端迁移快速指南](./frontend-migration-quickstart.md) | 5分钟学会迁移到新架构 | 前端开发 |
| [前端文件清单](./frontend-new-files-list.md) | 新架构完整文件清单 | 前端开发 |
| [部署检查清单](./guides/DEPLOYMENT-CHECKLIST.md) | 部署前质量检查、配置验证、问题排查 | 运维、部署人员 |

### 🏗️ 架构参考
| 文档 | 说明 | 适合谁 |
|------|------|--------|
| [后端架构总览](./architecture/BACKEND-ARCHITECTURE.md) | 四包模块化结构、22个业务模块、设计决策 | 后端开发、架构师 |
| [数据库设计](./architecture/DATABASE-DESIGN.md) | 完整表结构、ER关系、索引说明 | 全员 |
| [缓存架构](./architecture/CACHE-ARCHITECTURE.md) | Redis 双层缓存、缓存空间、降级策略 | 后端开发 |
| [架构演进路线](./architecture/BACKEND-ARCHITECTURE-EVOLUTION.md) | 渐进式升级方案：缓存→模块化→服务化 | 架构师 |

### 📡 API 参考
| 文档 | 说明 |
|------|------|
| [API 总览与通用规范](./api/API-OVERVIEW.md) | 认证方式、响应格式、错误码 |
| [游戏核心 API](./api/GAME-CORE-API.md) | 玩家、修炼、战斗、技能 |
| [宠物与叙事 API](./api/PET-NARRATIVE-API.md) | 宠物、进化、NPC、对话、传说 |
| [社交与经济 API](./api/SOCIAL-ECONOMY-API.md) | 宗门、拍卖行、排行榜、成就 |

### 📐 开发规范
| 文档 | 说明 |
|------|------|
| **[代码审查标准](./standards/CODE-REVIEW-STANDARDS.md)** | **检查清单、优先级定义、审查规范** |
| **[代码审查流程](./standards/CODE-REVIEW-PROCESS.md)** | **PR流程、角色职责、工具使用** |
| **[代码审查模板](./standards/CODE-REVIEW-TEMPLATES.md)** | **标准化审查评论模板、示例** |
| **[代码审查快速检查清单](./standards/CODE-REVIEW-CHECKLIST.md)** | **日常审查、自查的快速参考工具** |
| **[代码审查快速上手指南](./standards/CODE-REVIEW-QUICKSTART.md)** | **5分钟了解代码审查,10分钟开始第一次审查** |
| **[代码审查改进方案](./standards/CODE-REVIEW-IMPROVEMENT-PLAN.md)** | **系统化改进计划、实施方案、ROI分析(2026-03-26)** |
| [后端编码规范](./standards/BACKEND-CODING-STANDARDS.md) | 异常处理、事务、日志、并发规范 |
| [ErrorCode 手册](./standards/ERROR-CODE-REFERENCE.md) | 全部错误码分段说明 |
| [性能优化指南](./standards/PERFORMANCE-GUIDE.md) | 数据库索引、N+1优化、前端懒加载、缓存策略 |
| [数值调优记录](./standards/OPTIMIZATION-NOTES.md) | 核心数值表、配置参考、历史Bug修复记录 |
| [美术管线标准](./standards/ART-PIPELINE-STANDARDS.md) | 资产预算、命名规范、导入流程 |
| [VFX 特效优化指南](./standards/VFX-OPTIMIZATION-GUIDE.md) | 特效分级、对象池、过度绘制控制 |
| [颜色与无障碍标准](./standards/COLOR-AND-ACCESSIBILITY-STANDARDS.md) | WCAG AA 合规、色盲友好、CSS 变量 |

### 🎮 游戏设计
| 文档 | 说明 |
|------|------|
| [游戏设计文档 (GDD)](./design/GDD-修仙挂机游戏设计文档.md) | 玩法机制、数值公式、5大设计支柱 |
| [叙事设计文档 (NDD)](./design/NARRATIVE-DESIGN-DOCUMENT.md) | 世界观、NPC阵容、对话系统 |
| [关卡设计文档 (LDD)](./design/LEVEL-DESIGN-DOCUMENT.md) | 地图、关卡、进度曲线 |
| [音频设计文档 (ADD)](./design/AUDIO-DESIGN-DOCUMENT.md) | 音效规格、Web Audio API、自适应音乐 |

---

## 🗓️ 文档维护说明

- **谁维护**：功能开发者负责随代码同步更新对应文档
- **何时更新**：API变更、新增系统、架构调整时，PR必须包含文档修改
- **文档优先级**：`docs/` > 根目录 `.md` 文件（根目录文件为对外展示用）
- **发现过期内容**：直接提PR修正，或在对应文档末尾标注 `⚠️ 待更新 [日期]`

---

## 📦 项目状态速览

| 系统模块 | 状态 | 后端 | 前端 | 文档 |
|---------|------|------|------|------|
| 认证系统 | ✅ 完成 | ✅ | ✅ | ✅ |
| 玩家/修炼 | ✅ 完成 | ✅ | ✅ | ✅ |
| 战斗系统 | ✅ 完成 | ✅ | ✅ | ✅ |
| 技能系统 | ✅ 完成 | ✅ | ✅ | ✅ |
| 宠物系统 | ✅ 完成 | ✅ | ✅ | ✅ |
| 装备系统 | ✅ 完成 | ✅ | ✅ | 部分 |
| 宗门系统 | ✅ 完成 | ✅ | ✅ | 部分 |
| 叙事/NPC | ✅ 完成 | ✅ | ✅ | ✅ |
| 排行榜 | ✅ 完成 | ✅ | ✅ | 部分 |
| 成就系统 | ✅ 完成 | ✅ | ✅ | 部分 |
| 签到系统 | ✅ 完成 | ✅ | ✅ | 部分 |
| 宗门BOSS | ✅ 完成 | ✅ | ✅ | 部分 |
| 地图系统 | ✅ 完成 | ✅ | ✅ | 待完善 |
| 音频系统 | ✅ 完成 | — | ✅ | ✅ |

*最后更新：2026-03-27（代码大规模重构：CombatService拆分优化、新增Java8Compatibility兼容工具类、新增DTO（ListAuctionRequest/SystemMailRequest）、前端utils.js重构整合；共 334 个 Java 文件）*
