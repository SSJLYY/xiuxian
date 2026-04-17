# API 总览与通用规范

> 本文档描述所有 API 的通用规则：认证方式、请求格式、响应格式、错误处理。  
> 具体接口见各子文档。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-04-17

---

## 基础信息

| 项 | 值 |
|----|----|
| 基础 URL | `http://localhost:8082` |
| 协议 | HTTP/HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 跨域支持 | 已启用 CORS |

---

## 认证方式

### 游戏端认证

1. 调用 `POST /api/auth/login` 获取 Token
2. 后续请求在 Header 中携带：
   ```
   Authorization: Bearer <token>
   ```

### 管理端认证

1. 调用 `POST /api/admin/auth/login` 获取 Token
2. 后续请求在 Header 中携带：
   ```
   Authorization: Bearer <admin-token>
   ```

> ⚠️ 游戏 Token 无法访问管理端接口，反之亦然。两套 Token 完全独立。

---

## 响应格式

所有响应统一用 `ApiResponse<T>` 包装：

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 操作是否成功 |
| `code` | int | 200 表示成功，其他为业务错误码（见 [ErrorCode 手册](../standards/ERROR-CODE-REFERENCE.md)）|
| `message` | string | 可直接展示给用户的提示文案 |
| `data` | object/null | 成功时的业务数据 |

### 成功示例
```json
{
  "success": true,
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "nickname": "青云剑客"
  }
}
```

### 失败示例
```json
{
  "success": false,
  "code": 1103,
  "message": "密码错误",
  "data": null
}
```

---

## HTTP 状态码约定

| 状态码 | 含义 | 场景 |
|--------|------|------|
| 200 | 请求已处理 | 所有业务请求（包含业务失败） |
| 401 | 未认证 | Token 无效或缺失 |
| 403 | 无权限 | Token 有效但无访问权限 |
| 404 | 路径不存在 | 接口路径错误 |
| 500 | 服务器错误 | 未预期的系统错误 |

> **注意**：业务错误（如"灵石不足"、"等级不够"）HTTP 状态码仍为 `200`，通过 `code` 字段区分。

### 错误类型与处理策略

| 错误类型 | HTTP 状态码 | 业务 code 范围 | 处理策略 |
|---------|------------|--------------|---------|
| 认证失败 | 401 | 1001-1099 | 前端跳转登录页，清除本地 Token |
| 权限不足 | 403 | 1100-1199 | 提示"权限不足"，隐藏相关功能入口 |
| 业务错误 | 200 | 1200-1999 | 根据业务场景处理，通常弹窗提示用户 |
| 参数错误 | 400 | 2000-2099 | 前端表单验证不 through 时即处理，不应发到后端 |
| 系统错误 | 500 | 9000-9999 | 提示"服务器开小差了"，记录日志并告警 |

### 常见业务错误码速查

| Code | 错误标识 | 提示信息 | 解决方案 |
|------|---------|---------|---------|
| 1101 | USERNAME_EXISTS | 用户名已存在 | 更换用户名 |
| 1102 | EMAIL_EXISTS | 邮箱已被注册 | 更换邮箱或找回密码 |
| 1103 | INVALID_PASSWORD | 密码错误 | 重新输入或找回密码 |
| 1201 | LEVEL_INSUFFICIENT | 等级不足 | 提升等级后再试 |
| 1202 | SPIRIT_STONES_INSUFFICIENT | 灵石不足 | 获取足够灵石后再试 |
| 1203 | ALREADY_CULTIVATING | 已在修炼中 | 无需重复操作 |
| 1204 | NOT_CULTIVATING | 未在修炼中 | 先开始修炼 |
| 1205 | BREAKTHROUGH_NOT_READY | 未达到突破条件 | 检查等级和灵石是否足够 |
| 1206 | BREAKTHROUGH_COOLDOWN | 突破冷却中 | 等待冷却时间结束 |
| 1301 | ITEM_NOT_FOUND | 物品不存在 | 检查物品 ID 是否正确 |
| 1302 | ITEM_INSUFFICIENT | 物品数量不足 | 获取足够物品后再试 |

> 💡 **技术说明**：业务错误返回 HTTP 200 是为了避免浏览器误判为系统错误，同时便于前端统一处理业务逻辑。认证/权限错误返回 401/403 是为了利用 HTTP 协议本身的语义，便于中间件处理。

---

## 分页参数约定

支持分页的接口使用统一参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `page` | int | 1 | 页码（从 1 开始）|
| `size` | int | 10 | 每页条数 |
| `orderBy` | string | - | 排序字段 |
| `orderDir` | string | `desc` | 排序方向：`asc` / `desc` |

分页响应：
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [ ... ],
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10
  }
}
```

---

## 接口文档导航

| 文档 | 覆盖接口 |
|------|---------|
| [游戏核心 API](./GAME-CORE-API.md) | 认证、玩家、修炼、战斗、技能、装备、背包、商店 |
| [宠物与叙事 API](./PET-NARRATIVE-API.md) | 宠物、宠物进化、NPC、对话、传说、叙事标记 |
| [社交与经济 API](./SOCIAL-ECONOMY-API.md) | 宗门、宗门BOSS、排行榜、成就、拍卖行、签到、邮件、公告、活动、礼包码、VIP |
| [管理员 API](./ADMIN-API.md) | 管理员认证、玩家管理、内容管理、配置管理、安全管理 |

---

## 接口测试示例

```bash
# 1. 登录获取 Token
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'

# 返回中取出 data.token 值

# 2. 使用 Token 访问受保护接口
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

curl -X GET http://localhost:8082/api/player/profile \
  -H "Authorization: Bearer $TOKEN"

# 3. 开始修炼
curl -X POST http://localhost:8082/api/player/cultivate \
  -H "Authorization: Bearer $TOKEN"
```
