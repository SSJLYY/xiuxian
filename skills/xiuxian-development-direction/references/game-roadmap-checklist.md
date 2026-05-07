# Game Roadmap Checklist

## 四条主线

- 成长
- 战斗
- 资源
- 养成

## 阶段优先级

1. 主循环可玩
2. 成长反馈清晰
3. 中期追求成立
4. 扩展内容补强
5. 平衡与性能优化

## 规划时要映射到仓库

- 后端模块：`player`、`combat`、`skill`、`pet`、`equipment`、`quest`、`shop`、`auction`、`guild`
- 前端模块：`src/main/resources/static/js/modules/*`
- 页面：`src/main/resources/static/pages/game/*`

## 常见失误

- 先做大系统，主循环还不闭环
- 只讲玩法，不讲落地模块
- 没写验证和回归
- 本轮范围没有明确裁剪
