---
name: project-development-direction
description: Define and maintain development direction for this project, including milestones, scope boundaries, and iteration priorities. Use when the user asks for roadmap planning, phase goals, feature prioritization, or next-step execution sequencing.
---

# Project Development Direction

## 目标

为该项目提供可执行、可追踪的开发方向，避免“想到哪做哪”，确保每次迭代都有清晰目标、验收标准和代码落地路径。

## 适用场景

在以下情况优先使用本技能：
- 用户要求“做开发规划/路线图/阶段目标”
- 用户不确定先做哪个功能
- 需求较多，需要划分 MVP 与后续版本
- 需要把模糊想法拆成具体迭代任务

## 项目事实基线（规划前先对齐）

- 后端主栈：Java + Spring Boot + MyBatis-Plus（`src/main/java`）
- 前端主栈：静态页面 + 原生模块（`src/main/resources/static`）
- 当前是单仓双端协同模式：新增玩法通常需要后端接口与前端页面同步落地
- 核心业务域已存在：玩家/战斗/技能/任务/商店/拍卖/宗门

## 工作流程

按顺序执行并输出结果：

1. 明确当前阶段
   - 判断项目处于：启动期 / 可用版建设期 / 优化扩展期
   - 用一句话定义本阶段核心目标

2. 收敛范围
   - 列出候选需求
   - 标记 `MVP` / `Next` / `Later`
   - 明确不做项（本轮排除）

3. 确定里程碑
   - 规划 2-4 个里程碑
   - 每个里程碑包含：目标、关键任务、完成定义（DoD）

4. 设定优先级
   - 按“用户价值 + 实现成本 + 风险”排序
   - 默认先做高价值低耦合项

5. 形成最近迭代计划
   - 给出当前迭代（1-2 周）可落地任务清单
   - 每项任务要有明确产出和简单验收条件

6. 增加落地约束检查
   - 每个里程碑都要包含：代码位置、测试补齐、文档同步
   - 未满足约束不标记 Done

## 输出模板

使用下面结构返回，保持精简：

```markdown
## 开发方向（当前阶段）
[一句话阶段目标]

## 范围划分
- MVP: ...
- Next: ...
- Later: ...
- 本轮不做: ...

## 里程碑
1. 里程碑A
   - 目标: ...
   - 关键任务: ...
   - 完成定义: ...
2. 里程碑B
   - 目标: ...
   - 关键任务: ...
   - 完成定义: ...

## 当前迭代（1-2周）
- [ ] 任务1（产出 + 验收）
- [ ] 任务2（产出 + 验收）
- [ ] 任务3（产出 + 验收）

## 风险与应对
- 风险: ...
  - 应对: ...
```

## 约束

- 不输出空泛建议，必须可执行
- 不同时推进过多主线，避免上下文切换
- 每次规划都要有“本轮不做”清单
- 若信息不足，先补齐关键约束（时间、资源、目标用户）
- 新功能默认要求双端落地：后端 `controller + service`，前端 `js/modules + pages/game`
- 跨模块调用优先走 service 边界，避免直接跨模块访问 mapper
- 数值类改动（战斗/修炼/收益）优先配置化，避免硬编码散落
- 每个里程碑至少包含 1 条测试补齐任务（service 或接口层）
- 规划变更后同步更新相关文档的勾选状态，避免“代码已做但文档未更新”
