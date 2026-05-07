# Bug Patterns

## 高频问题类型

- DTO 字段名与页面渲染字段不一致
- `GameApi.js` 或模块 service 归一化逻辑错误
- 玩家技能接口误用 `skillId`，实际应使用 `playerSkillId`
- 业务失败走 HTTP 200，但页面只看 HTTP 状态码
- 历史数据空值导致空指针或数值异常
- Redis 缓存、本地降级、数据库三者结果不一致
- 中文文本或注释发生 UTF-8 污染

## 高价值接口契约提醒

- `SkillController /api/skills/player` 的可操作 id 通常是 `playerSkillId`
- `GuildController /api/guild/list` 返回的是 `guilds`，不是常见的 `records`
- `InventoryController /api/inventory/categorized` 返回的是 `Map<String, List<PlayerItemResponse>>`
- 玩家端和管理端认证、接口前缀、登录流完全分离

## 高价值前端落点

- `src/main/resources/static/js/core/api/GameApi.js`
- `src/main/resources/static/js/modules/*`
- `src/main/resources/static/js/pages/*`

## 高价值后端落点

- `src/main/java/com/xiuxian/game/modules/*/controller`
- `src/main/java/com/xiuxian/game/modules/*/service`
- `src/main/java/com/xiuxian/game/dto`
