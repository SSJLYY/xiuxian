# Trace Checklist

## 页面到接口

1. 找页面文件
2. 找按钮或初始化入口
3. 找事件绑定
4. 找请求 URL、方法、参数构造

## 接口到服务

1. 对应 controller 方法
2. 顺到 service
3. 看 DTO 和 `ApiResponse<T>` 包装
4. 看是否有字段重命名、列表包装、分页包装

## 服务回到页面

1. 页面依赖哪些字段
2. 是否做了空值兼容
3. 是否把 `data`、`records`、`guilds`、`list` 等层级用错
4. 是否把模板 id 和实例 id 混用

## 高频错位点

- `GameApi.js`
- 各 `js/modules/*Service.js`
- 技能、背包、宗门相关页面
