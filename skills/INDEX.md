# Skills Index

## 项目技能

### `xiuxian-project-context`
- 作用：建立项目整体上下文，快速找入口、模块边界、运行方式和文档起点。
- 适用：通读项目、接手仓库、找某个功能在哪、判断改动落点、先理解再动手。
- 路径：`skills/xiuxian-project-context/SKILL.md`

### `xiuxian-bug-locate`
- 作用：定位 bug，收敛根因、验证步骤和影响范围。
- 适用：先分析代码和日志、解释为什么功能不生效、排查接口报错、页面异常、数据不一致、乱码问题。
- 路径：`skills/xiuxian-bug-locate/SKILL.md`

### `xiuxian-api-trace`
- 作用：追踪页面动作到接口到服务到返回字段的完整链路。
- 适用：分析按钮点击、页面加载、接口调用链、字段对不上、前后端契约错位。
- 路径：`skills/xiuxian-api-trace/SKILL.md`

### `xiuxian-feature-implementation`
- 作用：拆解并落地功能改动，明确后端、前端、DTO、配置、文档和验证。
- 适用：新增功能、改造现有逻辑、需要先给方案再改代码、做前后端联动实现。
- 路径：`skills/xiuxian-feature-implementation/SKILL.md`

### `xiuxian-development-direction`
- 作用：按修仙挂机玩法的成长、战斗、资源、养成主线规划版本方向。
- 适用：修仙玩法路线图、MVP 范围、版本优先级、系统先后顺序。
- 路径：`skills/xiuxian-development-direction/SKILL.md`

### `project-development-direction`
- 作用：做更通用的项目阶段规划、范围控制、里程碑和近期迭代拆分。
- 适用：路线规划、里程碑设计、优先级排序、决定下一阶段做什么。
- 路径：`skills/project-development-direction/SKILL.md`

### `xiuxian-codereview-direct-fix`
- 作用：按 codereview 模式整仓排查并直接修复 xiuxian 项目的缺陷。
- 适用：找所有 bug 并直接修、整仓继续排查、重复多轮 bughunt、用户只说“继续”但上下文已经是 codereview 直修。
- 路径：`skills/xiuxian-codereview-direct-fix/SKILL.md`

## 使用顺序建议

1. 先用 `xiuxian-project-context` 建立仓库上下文。
2. 需要定位故障时切到 `xiuxian-bug-locate`。
3. 需要展开调用链时切到 `xiuxian-api-trace`。
4. 需要设计并落地改动时切到 `xiuxian-feature-implementation`。

## 仓库特定约束

- 后端验证优先使用 `D:\soft\apache-maven-3.9.12\bin\mvn.cmd`。
- 前端语法验证优先使用 `node --check`。
- 看到中文异常时，把 UTF-8/乱码污染当成真实问题处理。
- 玩家端和管理端接口、登录流、权限边界必须分开看。
- DTO 字段形状、`GameApi.js` 归一化和页面渲染字段是高频失配点。
