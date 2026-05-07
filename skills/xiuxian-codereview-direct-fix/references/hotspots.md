# Hotspots

## 后端热点

### `modules/*/service`

优先看这些模式：

- 旧数据空值参与算术
- 状态写两次或重复记账
- 事务边界不完整
- 缓存刷新缺失
- 重复调用记录方法

特别高价值模块：

- `combat`
- `map`
- `giftcode`
- `guild`
- `activity`
- `offline`
- `player`

### `modules/*/controller`

优先看这些模式：

- 返回 DTO 与页面字段不一致
- 业务失败包装后信息丢失
- 玩家端和管理端接口混线
- 请求参数 id 类型混用

## 前端热点

### `js/core/api/GameApi.js`

高频问题：

- 包装层级与后端 `ApiResponse<T>` 不一致
- 对业务失败做了错误归一化
- 老页面依赖的方法漂移

### `js/modules/*`

高频问题：

- 页面依赖字段名和 DTO 不一致
- `skillId` 与 `playerSkillId` 混用
- 列表字段误用 `records`
- 空值没有兜底导致页面不渲染

特别高价值模块：

- `skills`
- `inventory`
- `guild`
- `pets`
- `mail`
- `map`

## 编码与文案热点

- `src/main/resources/static/js/*`
- `src/main/resources/static/pages/*`
- `src/main/java/com/xiuxian/game/modules/*`

看到这些信号要优先考虑 UTF-8 污染：

- 中文页面文案突然变英文
- 文件局部中文乱码但逻辑正常
- 注释和字符串字面量同时异常
