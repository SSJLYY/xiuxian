---
name: xiuxian-codereview-direct-fix
description: 以 codereview 模式在 xiuxian 仓库里做整仓或大范围缺陷排查，并直接实施修复，覆盖后端、前端契约、编码乱码、缓存降级、历史数据兼容和回归验证。用于用户要求“codereview 模式找出所有 bug 并直接修复”、“继续”、“整仓范围继续排查修复”或在这个仓库里持续多轮 bughunt 时。
---

# Xiuxian Codereview Direct Fix

## 目标

按 codereview 模式持续找 bug、直接修、立即验证，不把工作停在“列发现”。

## 适用场景

- 用户明确说 `codereview 模式`
- 用户要求找出所有 bug 并直接修复
- 用户在多轮修复后继续说 `继续`
- 用户要求整仓范围继续排查，不只看单文件

## 默认工作方式

1. 先扫高风险目录和高频问题模式。
2. 优先修真实缺陷，不优先做纯风格调整。
3. 每修一批就验证一批。
4. 一轮通过后继续深挖，不把首次绿编译当结束。
5. 同时关注后端逻辑、前后端契约、中文显示和历史数据兼容。

## 高风险区域

- `src/main/java/com/xiuxian/game/modules/*/service`
- `src/main/java/com/xiuxian/game/modules/*/controller`
- `src/main/resources/static/js/core/api/GameApi.js`
- `src/main/resources/static/js/modules/*`
- `src/main/resources/static/pages/*`
- `src/main/resources/application.properties`

## 优先缺陷类型

- 空指针和旧数据兼容
- 重复写状态或重复记账
- DTO 与页面字段错位
- `skillId` / `playerSkillId` 混用
- Redis 缓存与本地降级导致的不一致
- 业务失败包装后前端处理失真
- 中文文案和 UTF-8 污染

## 先读

- `references/review-checklist.md`
- `references/verification.md`
- `references/hotspots.md`

## 验证要求

- 后端优先使用 `D:\soft\apache-maven-3.9.12\bin\mvn.cmd`
- 前端脚本优先用 `node --check`
- SpotBugs 用来筛高信号缺陷，不把它当完整结论

## 输出要求

先给发现和修复结果，再给验证结论。发现按风险排序。

```markdown
## 已修复问题
1. ...
   - 原因：...
   - 修复：...
   - 验证：...

## 本轮验证
- ...

## 剩余风险
- ...

## 下一轮建议
- ...
```

## 约束

- 不要只停留在列问题。
- 不要第一次编译通过就结束。
- 不要把中文文案误改成英文。
- 不要忽略前后端契约与页面渲染层。
