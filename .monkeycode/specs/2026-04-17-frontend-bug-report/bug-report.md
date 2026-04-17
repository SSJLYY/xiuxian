# 玩家前台 UI 和业务 Bug 检查报告

**检查日期**: 2026-04-17  
**检查范围**: 注册、登录、修炼核心流程  
**检查人**: shaun.sheng

---

## 🔴 严重 Bug（必须修复）

### Bug #1: 修炼信息接口缺失 ⚠️

**问题描述**:  
前端 `CultivateService.js` 调用了 `gameAPI.player.getCultivateInfo()`，但后端 **不存在** 这个接口！

**影响范围**:
- 修炼页面无法加载玩家修炼状态
- 无法显示当前境界、等级、经验等信息
- 修炼功能完全不可用

**代码位置**:
- 前端调用：`src/main/resources/static/js/modules/cultivate/CultivateService.js:14`
- 后端缺失：`PlayerController.java` 中没有 `getCultivateInfo` 接口

**复现步骤**:
1. 玩家登录游戏
2. 进入修炼页面
3. 控制台报错：`GET /api/player/cultivate/info 404`
4. 页面显示"加载修炼信息失败"

**修复方案**:
```java
// PlayerController.java 新增接口
@GetMapping("/cultivate/info")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<ApiResponse<PlayerProfile>> getCultivateInfo() {
    try {
        PlayerProfile profile = playerService.getCurrentPlayerProfile();
        return ResponseEntity.ok(ApiResponse.success("获取修炼信息成功", profile));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
```

**优先级**: 🔥 P0 - 立即修复

---

### Bug #2: 开始修炼接口参数不匹配 ⚠️

**问题描述**:  
前端 `startCultivate(type)` 传递修炼类型参数（normal/intensive/meditation），但后端 `cultivate()` 方法 **不接受任何参数**，导致修炼类型选择失效。

**影响范围**:
- 所有修炼类型都按普通修炼处理
- 闭关修炼（x1.5）和冥想修炼（x2.0）无效
- 灵石消耗计算错误

**代码位置**:
- 前端：`CultivateService.js:24` - `startCultivate(type = 'normal')`
- 后端：`PlayerController.java:38` - `public ResponseEntity<Void> cultivate()`

**修复方案**:
```java
// PlayerController.java
@PostMapping("/cultivate")
public ResponseEntity<ApiResponse<Void>> cultivate(
        @RequestBody Map<String, String> params) {
    String type = params != null ? params.getOrDefault("type", "normal") : "normal";
    playerService.cultivate(type);
    // ...
}

// PlayerService.java
public void cultivate(String type) {
    // ...
    double speedMultiplier = getSpeedMultiplier(type);
    profile.setCultivationSpeed(new BigDecimal(speedMultiplier));
    // ...
}

private double getSpeedMultiplier(String type) {
    switch (type) {
        case "intensive": return 1.5;
        case "meditation": return 2.0;
        default: return 1.0;
    }
}
```

**优先级**: 🔥 P0 - 立即修复

---

### Bug #3: 修炼速度字段未使用 ⚠️

**问题描述**:  
`PlayerProfile` 实体有 `cultivationSpeed` 字段，但 `stopCultivate()` 方法中使用了硬编码的修炼速度，未从数据库读取。

**代码位置**:
- `PlayerService.java:335`
```java
double cultivationSpeedMultiplier = profile.getCultivationSpeed().doubleValue();
```
这里读取了，但问题是 `cultivate()` 方法没有设置这个值！

**影响**:
- 修炼速度永远为 1.0
- 修炼类型选择毫无意义

**优先级**: ⚠️ P1 - 高优先级

---

### Bug #4: 批量修炼导致超时 ⚠️

**问题描述**:  
`stopCultivate()` 方法计算修炼收益时，如果玩家离线超过 24 小时，会触发大量经验计算和多次升级检查，可能导致：
- 数据库事务超时
- 玩家连续升级 100+ 次，服务器卡顿
- 任务进度更新失败

**代码位置**:
- `PlayerService.java:330-370`

**当前逻辑**:
```java
long cultivationTimeSeconds = java.time.Duration.between(startTime, now).getSeconds();
long maxCultivationTime = 24 * 60 * 60; // 24 小时
long actualCultivationTime = Math.min(cultivationTimeSeconds, maxCultivationTime);
```

虽然限制了 24 小时，但 24 小时的修炼收益仍然很大：
- 经验：24 * 3600 * 1.0 * cultivationSpeed = 86400 经验
- 可能导致连续升级 10+ 次

**修复建议**:
1. 限制单次最多升级 5 次
2. 剩余经验存储在缓冲区
3. 分批次处理升级逻辑

**优先级**: ⚠️ P1 - 高优先级

---

## 🟡 中等问题（建议修复）

### Bug #5: 密码强度验证不足

**问题描述**:  
AuthService 中有 `isValidPassword()` 方法，但在 `register()` 方法中 **未调用**，导致密码强度验证失效。

**代码位置**:
- `AuthService.java:47-209` - 有验证方法但未使用
- 当前只要求密码长度 >= 6 位

**影响**:
- 玩家可以设置弱密码（如 "123456"）
- 账户安全性降低

**修复方案**:
在 `AuthService.register()` 中添加：
```java
if (!isValidPassword(request.getPassword())) {
    throw new BusinessException(ErrorCode.PARAM_ERROR, 
        "密码必须至少 8 位，包含字母和数字");
}
```

**优先级**: 🟡 P2 - 中优先级

---

### Bug #6: 注册未检查昵称重复

**问题描述**:  
注册时检查了用户名和邮箱唯一性，但 **未检查昵称是否重复**。

**代码位置**:
- `AuthService.java:47-70`

**影响**:
- 多个玩家可以同名（昵称）
- 排行榜、社交系统可能出现重复名称

**修复方案**:
```java
if (playerProfileMapper.selectByNickname(request.getNickname()) != null) {
    throw new BusinessException(ErrorCode.PARAM_ERROR, "昵称已被使用");
}
```

**优先级**: 🟡 P2 - 中优先级

---

### Bug #7: 登录页面硬编码 userType

**问题描述**:  
`auth.js:175` 中硬编码 `userType = 'player'`，与后端 `handleAdminLogin()` 的逻辑冲突。

**代码位置**:
- `auth.js:175` - `const userType = 'player';`
- `AuthController.java:77-79` - 根据 userType 区分管理员/玩家

**影响**:
- 管理员无法通过普通登录页面登录
- 只能通过 `adminLogin.html` 登录

**当前状态**: 这是设计如此，但代码注释不清晰

**建议**: 添加注释说明设计意图

**优先级**: ℹ️ P3 - 低优先级

---

### Bug #8: 修炼页面初始化时序问题

**问题描述**:  
`cultivate.html` 使用 ES6 模块导入，但 `App.js.init()` 和 `cultivateUI.init()` 的执行时序不确定，可能导致：
- 玩家数据未加载完成就渲染 UI
- 显示空数据或旧数据

**代码位置**:
- `cultivate.html:47-51`

**当前代码**:
```html
<script type="module">
    import { App } from '/js/App.js';
    const app = new App();
    app.init();
    import('/js/modules/cultivate/index.js').then(module => {
        module.cultivateUI.init();
    });
</script>
```

**问题**: `app.init()` 是异步的，但没有等待完成就调用 `cultivateUI.init()`

**修复方案**:
```javascript
(async () => {
    import { App } from '/js/App.js';
    const app = new App();
    await app.init();
    import('/js/modules/cultivate/index.js').then(module => {
        module.cultivateUI.init();
    });
})();
```

**优先级**: 🟡 P2 - 中优先级

---

## 🟢 轻微问题（可优化）

### Bug #9: Token 验证失败后未清除 localStorage

**问题描述**:  
`auth.js:85-99` 中，自动登录失败时调用了 `clearAuthData()`，但如果 `isGamePage` 为 false 且 isLoginPage 也为 false（其他页面），不会跳转到登录页。

**影响**:
- 无效的 token 残留在 localStorage
- 下次访问时重复尝试验证

**优先级**: ℹ️ P3 - 低优先级

---

### Bug #10: Toast 消息堆叠

**问题描述**:  
`auth.js:510-544` 中，Toast 消息的定位计算 `bottom = 10 + count * 36`，但如果快速触发多个 Toast，可能导致：
- Toast 堆叠到屏幕外
- 遮挡重要操作按钮

**优先级**: ℹ️ P3 - 低优先级

---

### Bug #11: 修炼类型对话框无取消按钮

**问题描述**:  
`CultivateUI.js:149-174` 中，修炼类型选择对话框只有 confirmText: '取消'，但没有明确的取消按钮。

**影响**:
- 用户只能点击遮罩层关闭
- 移动端体验较差

**优先级**: ℹ️ P3 - 体验优化

---

## ✅ 正常功能

经过检查，以下功能 **工作正常**：

### 1. 注册流程 ✅
- 用户名/邮箱唯一性检查 ✅
- 密码加密存储 ✅
- 玩家档案自动创建 ✅
- 新手物品发放 ✅
- JWT Token 生成 ✅
- 自动登录 ✅

**但需要注意**:
- ⚠️ 密码强度验证未启用（Bug #5）
- ⚠️ 昵称重复检查缺失（Bug #6）

### 2. 登录流程 ✅
- 密码验证 ✅
- JWT Token 生成和存储 ✅
- 玩家资料加载 ✅
- 自动登录（token 验证） ✅
- 401/403 错误处理 ✅

**但需要注意**:
- ℹ️ 管理员登录需要专用页面（设计如此）

### 3. 修炼基础流程 ⚠️
- 开始修炼接口 ✅（但没有类型参数）
- 停止修炼接口 ✅（经验计算正确）
- 突破接口 ✅
- 任务进度更新 ✅

**但需要注意**:
- ⚠️ 缺少修炼信息查询接口（Bug #1）
- ⚠️ 修炼类型参数不匹配（Bug #2）
- ⚠️ 修炼速度字段未使用（Bug #3）

---

## 🔧 修复优先级

### P0 - 立即修复（阻塞性功能）
1. **Bug #1**: 新增 `/api/player/cultivate/info` 接口
2. **Bug #2**: 修炼类型参数传递
3. **Bug #3**: 设置修炼速度字段

### P1 - 高优先级（影响体验）
4. **Bug #4**: 批量修炼优化，防止超时
5. **Bug #8**: 修炼页面初始化时序

### P2 - 中优先级（业务逻辑）
6. **Bug #5**: 启用密码强度验证
7. **Bug #6**: 昵称唯一性检查

### P3 - 低优先级（体验优化）
8. **Bug #7**: 添加管理员登录说明注释
9. **Bug #9**: Token 验证失败处理优化
10. **Bug #10**: Toast 消息堆叠优化
11. **Bug #11**: 修炼类型对话框优化

---

## 📊 功能完整性评估

| 功能模块 | 状态 | 评分 | 说明 |
|---------|------|------|------|
| 注册 | ⚠️ 部分可用 | 7/10 | 基础功能正常，缺少密码强度和昵称检查 |
| 登录 | ✅ 可用 | 9/10 | 功能完整，管理员登录设计清晰 |
| 修炼 | ❌ 不可用 | 2/10 | 缺少关键接口，类型选择失效 |
| 突破 | ✅ 可用 | 8/10 | 接口完整，UI 正常 |

**综合评分**: **6.5/10** - 存在严重 blocker，需要立即修复 P0 问题

---

## 📝 后续建议

### 短期（本周）
1. 修复所有 P0 和 P1 级别 Bug
2. 添加修炼系统测试用例
3. 优化修炼页面加载体验

### 中期（本月）
1. 修复 P2 级别业务逻辑问题
2. 添加修炼系统监控（修炼时长、突破成功率）
3. 优化批量修炼处理逻辑

### 长期（优化）
1. 修复 P3 级别体验问题
2. 添加修炼类型特效动画
3. 优化 Toast、Modal 等 UI 组件

---

**结论**: 
- ✅ **注册、登录功能基本可用**，但有一些安全性和体验上的小问题
- ❌ **修炼功能目前不可用**，缺少关键 API 接口，需要立即修复 P0 级别的 3 个 Bug
- ⚠️ 建议 **暂停其他开发任务**，优先修复 P0 问题，确保核心玩法可用性

*报告生成时间：2026-04-17*  
*作者：shaun.sheng*
