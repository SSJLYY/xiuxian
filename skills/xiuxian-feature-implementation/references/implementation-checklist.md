# Implementation Checklist

## 先确认

- 成功标准
- 本轮不做项
- 是否前后端联动
- 是否涉及历史数据兼容

## 后端落点

- `controller`
- `service`
- `mapper`
- `entity`
- `dto`

## 前端落点

- `pages/game` 或 `pages/admin`
- `js/modules/<domain>`
- `js/core/api`
- `js/pages`

## 必查契约

- `ApiResponse<T>` 包装结构
- 字段名是否与页面消费一致
- 列表、分页、嵌套对象层级
- `skillId` / `playerSkillId`

## 必做验证

- `mvn.cmd compile`
- `mvn.cmd test -DskipITs`
- `mvn.cmd spotbugs:spotbugs`
- `node --check`

## 必做回归

- 直接影响页面
- 邻近模块
- 权限边界
- 缓存与降级分支
- 中文文案与编码
