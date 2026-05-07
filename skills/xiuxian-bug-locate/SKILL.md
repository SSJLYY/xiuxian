---
name: xiuxian-bug-locate
description: 定位 xiuxian 项目的页面、接口、登录、数据一致性、DTO 字段错配、数值逻辑、缓存回退和编码乱码问题，输出最短排查链路、候选根因和验证步骤。用于用户要求先分析代码和日志、定位 bug、解释为什么功能不生效、接口报错、页面行为异常或做 codereview 式缺陷排查时。
---

# Xiuxian Bug Locate

## 目标

把“这里有问题”收敛成可验证的定位结论，不要停留在猜测。

输出必须至少包含：

- 问题分类
- 首要排查链路
- 2 到 3 个高概率根因
- 每个根因对应的依据和验证方式
- 影响范围

## 优先分类

先判断属于哪类，再决定扫描路径：

- 启动与环境
- 登录与权限
- 接口行为
- 页面展示与交互
- DTO 或字段不匹配
- 持久化或事务一致性
- Redis 缓存与本地降级
- 数值、掉落、收益、成长
- UTF-8 或中文乱码

## 仓库内高频问题模式
读取 `references/bug-patterns.md` 获取这个仓库里最高频的问题模式和关键契约提醒。

## 最短排查链路

### 页面问题

`HTML -> js/pages 或 js/modules -> js/core/api -> controller -> service`

### 接口问题

`controller -> service -> mapper -> DB`

### 登录/权限问题

`登录页 -> 登录接口 -> token 存储 -> 鉴权过滤器 -> 对应 controller`

### 数值或收益问题

`页面/触发动作 -> service 核心分支 -> 配置来源 -> 历史数据兼容 -> 持久化`

### 编码问题

`用户可见文本 -> 文件实际编码状态 -> 同目录相邻文件 -> 运行后页面/日志显示`

## 必读位置

- `src/main/resources/application.properties`
- `src/main/java/com/xiuxian/game/common`
- `src/main/java/com/xiuxian/game/modules/*/controller`
- `src/main/java/com/xiuxian/game/modules/*/service`
- `src/main/resources/static/js/core/api`
- `src/main/resources/static/js/modules`

如果有日志，先对照这些目录，不要只读单个文件。

## 验证命令

```powershell
D:\soft\apache-maven-3.9.12\bin\mvn.cmd compile
D:\soft\apache-maven-3.9.12\bin\mvn.cmd test -DskipITs
D:\soft\apache-maven-3.9.12\bin\mvn.cmd spotbugs:spotbugs
node --check src/main/resources/static/js/xxx.js
```

必要时补充：

- 检查 `target/spotbugsXml.xml`
- 检查 `logs/`
- 对关键字段做 repo 内全文搜索

## 输出模板

```markdown
## 问题概述
- 现象：...
- 预期：...
- 类型：...

## 首要定位路径
1. `...`
2. `...`
3. `...`

## 高概率根因
1. ...
   - 依据：...
   - 验证：...
2. ...
   - 依据：...
   - 验证：...
3. ...
   - 依据：...
   - 验证：...

## 影响范围
- ...

## 下一步建议
- ...
```

## 约束

- 不要把推测当结论。
- 页面问题至少走完一次前后端链路。
- 登录问题先区分玩家端和管理端。
- 涉及缓存时同时考虑 Redis 和本地降级。
- 涉及数值逻辑时优先回看配置来源和旧数据兼容。
- 涉及乱码时优先判断是不是编码污染，不要直接翻译成英文糊过去。
