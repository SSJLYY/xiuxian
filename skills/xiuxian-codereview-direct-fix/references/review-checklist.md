# Review Checklist

## 先扫哪里

- `service`
- `controller`
- `dto`
- `GameApi.js`
- `js/modules/*`
- `static/pages/*`

## 先看哪类问题

- 空值和历史数据兼容
- 重复状态写入
- 奖励、收益、掉落、成长数值错误
- 接口返回结构和页面预期不一致
- 业务错误被包装丢失
- 中文乱码或可见文本回归

## 高价值验证

- `compile`
- `test -DskipITs`
- `spotbugs:spotbugs`
- `node --check`

## 继续挖掘规则

- 编译过后继续看 SpotBugs 和手工高风险路径
- 前端不编译时至少做脚本语法检查和契约核对
- 一轮修复后继续搜索邻近模块的相似模式
