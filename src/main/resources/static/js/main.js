/**
 * 主入口文件
 * 负责启动应用
 */

// 等待DOM加载完成
document.addEventListener('DOMContentLoaded', async () => {
    console.log('DOM已加载完成,开始启动应用...');

    try {
        // 导入应用类
        const { app } = await import('./App.js');

        // 初始化应用
        await app.init();

        // 将app挂载到全局,方便调试
        window.app = app;

        console.log('应用启动成功');

    } catch (error) {
        console.error('应用启动失败:', error);

        // 显示错误提示
        const errorDiv = document.createElement('div');
        errorDiv.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: #1a1a2e;
            color: #e8e8e8;
            padding: 24px;
            border-radius: 12px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
            text-align: center;
            z-index: 99999;
        `;
        errorDiv.innerHTML = `
            <h3 style="color: #f44336; margin-top: 0;">应用启动失败</h3>
            <p>${error.message}</p>
            <button onclick="location.reload()" style="
                background: #4caf50;
                color: white;
                border: none;
                padding: 12px 24px;
                border-radius: 6px;
                cursor: pointer;
                font-size: 14px;
            ">重新加载</button>
        `;
        document.body.appendChild(errorDiv);
    }
});

// 导出主模块
export { app };
