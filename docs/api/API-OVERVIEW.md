# API 总览与通用规范

> 本文档描述所有 API 的通用规则：认证方式、请求格式、响应格式、错误处理。  
> 具体接口见各子文档。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-24

---

## 基础信息

| 项 | 值 |
|----|----|
| 基础 URL | `http://localhost:8082` |
| 协议 | HTTP/HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |

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
