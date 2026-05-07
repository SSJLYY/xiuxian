---
name: xiuxian-feature-implementation
description: 为 xiuxian 项目拆解和实现功能改动，明确需求边界、后端和前端落点、DTO 与页面契约、配置与数据影响、验证命令和回归范围。用于用户要求新增功能、改造现有功能、先给实现方案再写代码、或在这个仓库内直接落地修复和联调时。
---

# Xiuxian Feature Implementation

## 目标

把需求拆成这个仓库里可直接执行的改动清单，避免空泛方案。

## 先判断改动类型

- 纯后端
- 纯前端
- 前后端联动
- 配置驱动
- 数据兼容或历史数据修复
- 文档同步

默认假设这个项目的大多数功能改动都不是纯单点，先检查是否涉及前后端契约同步。

## 常见落点

### 后端

- `src/main/java/com/xiuxian/game/modules/*/controller`
- `src/main/java/com/xiuxian/game/modules/*/service`
- `src/main/java/com/xiuxian/game/modules/*/mapper`
- `src/main/java/com/xiuxian/game/modules/*/entity`
- `src/main/java/com/xiuxian/game/dto`

### 前端

- `src/main/resources/static/pages/game`
- `src/main/resources/static/pages/admin`
- `src/main/resources/static/js/modules/<domain>`
- `src/main/resources/static/js/core/api`
- `src/main/resources/static/js/pages`

### 配置和文档

- `src/main/resources/application.properties`
- `docs/api`
- `docs/architecture`
- `docs/standards`
- `docs/design`

## 项目内实现原则

- 默认响应结构是 `ApiResponse<T>`。
- 跨模块调用优先走 service，不要直接跨模块依赖 mapper。
- 涉及页面显示时，检查字段名、空值形状、列表包装、分页包装是否与页面代码一致。
- 涉及玩家技能时，区分模板 `skillId` 和玩家实例 `playerSkillId`。
- 涉及数值、收益、掉落、成长时，回看设计文档和配置，不只改一处计算公式。
- 涉及中文文本时，保持 UTF-8，不要把中文可见文案误改成英文。

## 默认工作流

1. 明确成功标准和本轮不做项。
2. 识别受影响的后端模块、前端页面、API 契约、配置项、文档。
3. 确认是否有历史数据兼容风险。
4. 列出具体改动文件。
5. 执行改动。
6. 用仓库约定命令验证。
7. 总结回归范围和残余风险。

需要更细的落地和回归检查时，读取 `references/implementation-checklist.md`。

## 验证

后端优先：

```powershell
D:\soft\apache-maven-3.9.12\bin\mvn.cmd compile
D:\soft\apache-maven-3.9.12\bin\mvn.cmd test -DskipITs
D:\soft\apache-maven-3.9.12\bin\mvn.cmd spotbugs:spotbugs
```

前端脚本优先：

```powershell
node --check src/main/resources/static/js/xxx.js
```

## 输出模板

```markdown
## 需求目标
- 要实现：...
- 成功标准：...
- 本轮不做：...

## 落地映射
- 后端：`...`
- 前端：`...`
- DTO / 契约：`...`
- 配置：`...`
- 文档：`...`

## 实施步骤
1. ...
2. ...
3. ...

## 验证
- ...

## 风险与回归
- ...
```

## 约束

- 不要只说方案不落到文件路径。
- 不要默认前端不受影响。
- 不要遗漏文档和验证步骤。
- 改动大文件时优先分块验证，避免一次性引入新的乱码或回归。
