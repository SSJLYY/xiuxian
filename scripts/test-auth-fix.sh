#!/bin/bash
# 认证修复测试脚本

echo "=== xiuxian 认证修复测试 ==="
echo ""

# 1. 检查 git 提交
echo "1. 检查最近的 git 提交..."
git log --oneline -5
echo ""

# 2. 检查 auth.js 语法
echo "2. 检查 auth.js 语法..."
if [ -f src/main/resources/static/js/auth.js ]; then
    echo "✓ auth.js 文件存在"
    # 检查是否有明显的语法错误
    if grep -q "DOMContentLoaded" src/main/resources/static/js/auth.js; then
        echo "✓ DOMContentLoaded 事件已添加"
    fi
    if grep -q "bindEvents" src/main/resources/static/js/auth.js; then
        echo "✓ bindEvents 方法存在"
    fi
else
    echo "✗ auth.js 文件不存在"
fi
echo ""

# 3. 检查 login.html 表单结构
echo "3. 检查 login.html 表单结构..."
if grep -q '<form id="loginForm"' src/main/resources/static/login.html; then
    echo "✓ loginForm 表单标签存在"
fi
if grep -q 'type="submit"' src/main/resources/static/login.html; then
    echo "✓ submit 类型按钮存在"
fi
echo ""

# 4. 检查 index.html 表单结构
echo "4. 检查 index.html 表单结构..."
if grep -q '<form id="loginForm"' src/main/resources/static/index.html; then
    echo "✓ loginForm 表单标签存在"
fi
if grep -q 'type="submit"' src/main/resources/static/index.html; then
    echo "✓ submit 类型按钮存在"
fi
echo ""

# 5. 显示测试步骤
echo "=== 手动测试步骤 ==="
echo ""
echo "1. 在浏览器中打开 http://localhost:8082/login.html"
echo "2. 按 F12 打开开发者工具"
echo "3. 按 Ctrl+Shift+R 强制刷新"
echo "4. 查看控制台输出:"
echo "   - DOMContentLoaded - 初始化 AuthManager"
echo "   - AuthManager bindEvents 开始"
echo "   - 登录表单事件绑定成功"
echo "   - 注册表单事件绑定成功"
echo ""
echo "5. 点击'注册'标签页"
echo "6. 填写注册表单:"
echo "   - 用户名：testuser"
echo "   - 昵称：测试玩家"
echo "   - 邮箱：test@example.com"
echo "   - 密码：test123456"
echo "   - 确认密码：test123456"
echo "7. 点击'注册'按钮"
echo "8. 查看 Network 面板，应该有 POST /api/auth/register 请求"
echo ""
echo "=== 测试完成 ==="
