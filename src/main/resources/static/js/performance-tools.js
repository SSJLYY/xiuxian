/**
 * 性能监控与资产验证工具集
 * 技术美术 P4 文档 - 自动化工具
 * 版本: 1.0
 * 日期: 2026-03-23
 * 
 * 功能:
 * 1. 实时性能监控 (FPS, 内存, GPU)
 * 2. 资产质量自动检查
 * 3. 性能预算告警
 * 4. 性能指标导出
 */

// ============================================================
// 1. 性能监控系统
// ============================================================

class PerformanceMetrics {
    constructor() {
        this.metrics = {
            fps: 60,
            memory: 0,
            frameTime: 0,
            lastFrameTime: performance.now(),
            frameCount: 0,
            fpsHistory: [],
            memoryHistory: [],
            maxFpsHistory: 100
        };
        
        this.thresholds = {
            fpsWarning: 45,      // FPS低于45触发警告
            fpsError: 30,        // FPS低于30触发错误
            memoryWarning: 200,  // 内存超过200MB警告
            memoryError: 300     // 内存超过300MB错误
        };
        
        this.isMonitoring = false;
        this.listeners = [];
    }
    
    /**
     * 启动性能监控
     */
    start() {
        if (this.isMonitoring) return;
        this.isMonitoring = true;
        
        // FPS监控
        this.startFPSMonitoring();
        
        // 内存监控 (如果支持)
        if (performance.memory) {
            this.startMemoryMonitoring();
        }
        
        console.log('✓ 性能监控已启动');
    }
    
    /**
     * 启动FPS监控
     */
    startFPSMonitoring() {
        const measureFrame = () => {
            const now = performance.now();
            const deltaTime = now - this.metrics.lastFrameTime;
            
            this.metrics.frameTime = deltaTime;
            this.metrics.frameCount++;
            
            // 每1000ms计算一次FPS
            if (this.metrics.frameCount % 60 === 0) {
                this.metrics.fps = Math.round(1000 / (deltaTime));
                
                this.metrics.fpsHistory.push(this.metrics.fps);
                if (this.metrics.fpsHistory.length > this.metrics.maxFpsHistory) {
                    this.metrics.fpsHistory.shift();
                }
                
                // 检查阈值
                this.checkThresholds();
                
                // 通知监听器
                this.notify('fps', this.metrics.fps);
            }
            
            this.metrics.lastFrameTime = now;
            requestAnimationFrame(measureFrame);
        };
        
        requestAnimationFrame(measureFrame);
    }
    
    /**
     * 启动内存监控
     */
    startMemoryMonitoring() {
        setInterval(() => {
            if (performance.memory) {
                const usedMB = (performance.memory.usedJSHeapSize / 1048576).toFixed(2);
                this.metrics.memory = usedMB;
                
                this.metrics.memoryHistory.push(usedMB);
                if (this.metrics.memoryHistory.length > this.metrics.maxFpsHistory) {
                    this.metrics.memoryHistory.shift();
                }
                
                this.checkThresholds();
                this.notify('memory', usedMB);
            }
        }, 1000);
    }
    
    /**
     * 检查性能阈值
     */
    checkThresholds() {
        // FPS检查
        if (this.metrics.fps < this.thresholds.fpsError) {
            this.notify('error', `🚨 FPS严重下降: ${this.metrics.fps}`);
        } else if (this.metrics.fps < this.thresholds.fpsWarning) {
            this.notify('warning', `⚠️ FPS下降: ${this.metrics.fps}`);
        }
        
        // 内存检查
        if (performance.memory) {
            if (this.metrics.memory > this.thresholds.memoryError) {
                this.notify('error', `🚨 内存占用过高: ${this.metrics.memory}MB`);
            } else if (this.metrics.memory > this.thresholds.memoryWarning) {
                this.notify('warning', `⚠️ 内存占用较高: ${this.metrics.memory}MB`);
            }
        }
    }
    
    /**
     * 注册监听器
     */
    on(metric, callback) {
        this.listeners.push({ metric, callback });
    }
    
    /**
     * 通知监听器
     */
    notify(metric, value) {
        this.listeners.forEach(listener => {
            if (listener.metric === metric || listener.metric === 'all') {
                listener.callback(value);
            }
        });
    }
    
    /**
     * 获取平均FPS
     */
    getAverageFPS() {
        if (this.metrics.fpsHistory.length === 0) return 0;
        const sum = this.metrics.fpsHistory.reduce((a, b) => a + b, 0);
        return Math.round(sum / this.metrics.fpsHistory.length);
    }
    
    /**
     * 获取最小FPS
     */
    getMinFPS() {
        return Math.min(...this.metrics.fpsHistory);
    }
    
    /**
     * 获取最大FPS
     */
    getMaxFPS() {
        return Math.max(...this.metrics.fpsHistory);
    }
    
    /**
     * 获取性能报告
     */
    getReport() {
        return {
            currentFPS: this.metrics.fps,
            averageFPS: this.getAverageFPS(),
            minFPS: this.getMinFPS(),
            maxFPS: this.getMaxFPS(),
            memory: this.metrics.memory,
            timestamp: new Date().toISOString(),
            platformInfo: {
                userAgent: navigator.userAgent,
                cores: navigator.hardwareConcurrency,
                deviceMemory: navigator.deviceMemory
            }
        };
    }
}

// ============================================================
// 2. 资产质量检查系统
// ============================================================

class AssetValidator {
    constructor() {
        this.issues = [];
        this.warnings = [];
    }
    
    /**
     * 检查所有图片
     */
    validateAllImages() {
        const images = document.querySelectorAll('img');
        console.log(`🔍 检查 ${images.length} 张图片...`);
        
        images.forEach(img => {
            this.validateImage(img);
        });
        
        return {
            issues: this.issues,
            warnings: this.warnings
        };
    }
    
    /**
     * 检查单个图片
     */
    validateImage(img) {
        const checks = [
            this.checkImageSize(img),
            this.checkImageFormat(img),
            this.checkImageAlt(img),
            this.checkImageDimensions(img)
        ];
        
        checks.forEach(result => {
            if (result.type === 'error') {
                this.issues.push(result.message);
            } else if (result.type === 'warning') {
                this.warnings.push(result.message);
            }
        });
    }
    
    /**
     * 检查图片大小
     */
    checkImageSize(img) {
        const src = img.src;
        
        // 模拟检查 (实际需要从服务器获取)
        if (src.includes('png') && !src.includes('webp')) {
            return {
                type: 'warning',
                message: `⚠️ ${src} 使用PNG格式，建议转换为WebP`
            };
        }
        
        return { type: 'ok' };
    }
    
    /**
     * 检查图片格式
     */
    checkImageFormat(img) {
        const src = img.src;
        const validFormats = ['webp', 'jpg', 'png', 'svg'];
        const format = src.split('.').pop().toLowerCase();
        
        if (!validFormats.includes(format)) {
            return {
                type: 'error',
                message: `❌ ${src} 使用不支持的格式: ${format}`
            };
        }
        
        return { type: 'ok' };
    }
    
    /**
     * 检查alt属性
     */
    checkImageAlt(img) {
        if (!img.alt || img.alt.trim() === '') {
            return {
                type: 'warning',
                message: `⚠️ ${img.src} 缺少alt属性 (无障碍问题)`
            };
        }
        
        return { type: 'ok' };
    }
    
    /**
     * 检查图片尺寸
     */
    checkImageDimensions(img) {
        if (!img.width || !img.height) {
            return {
                type: 'warning',
                message: `⚠️ ${img.src} 缺少width/height属性 (可能引起布局移位)`
            };
        }
        
        return { type: 'ok' };
    }
    
    /**
     * 检查CSS规则数
     */
    validateCSS() {
        let totalRules = 0;
        
        for (let i = 0; i < document.styleSheets.length; i++) {
            try {
                const sheet = document.styleSheets[i];
                if (sheet.cssRules) {
                    totalRules += sheet.cssRules.length;
                }
            } catch (e) {
                // 跨域样式表会报错，跳过
            }
        }
        
        const result = {
            totalRules: totalRules,
            status: 'ok'
        };
        
        if (totalRules > 2000) {
            result.status = 'error';
            result.message = `❌ CSS规则过多: ${totalRules} (推荐 < 2000)`;
        } else if (totalRules > 1500) {
            result.status = 'warning';
            result.message = `⚠️ CSS规则较多: ${totalRules} (推荐 < 1500)`;
        }
        
        return result;
    }
    
    /**
     * 检查动画效果并发数
     */
    validateAnimationConcurrency() {
        const floatingTexts = document.querySelectorAll('.floating-damage').length;
        const environmentEffects = document.querySelectorAll('.environment-effect').length;
        const combatEffects = document.querySelectorAll('.combat-effect').length;
        
        const result = {
            floatingTexts: floatingTexts,
            environmentEffects: environmentEffects,
            combatEffects: combatEffects,
            issues: []
        };
        
        if (floatingTexts > 20) {
            result.issues.push(`⚠️ 飘字过多: ${floatingTexts}/20`);
        }
        
        if (environmentEffects > 10) {
            result.issues.push(`⚠️ 环境特效过多: ${environmentEffects}/10`);
        }
        
        if (combatEffects > 5) {
            result.issues.push(`🚨 战斗特效过多: ${combatEffects}/5`);
        }
        
        return result;
    }
    
    /**
     * 完整资产检查报告
     */
    generateFullReport() {
        console.group('📊 资产质量检查报告');
        
        // 图片检查
        console.group('🖼️ 图片检查');
        const imageValidation = this.validateAllImages();
        console.log(`✓ 问题数: ${imageValidation.issues.length}`);
        console.log(`⚠️ 警告数: ${imageValidation.warnings.length}`);
        if (imageValidation.warnings.length > 0) {
            console.warn(imageValidation.warnings);
        }
        console.groupEnd();
        
        // CSS检查
        console.group('🎨 CSS检查');
        const cssValidation = this.validateCSS();
        console.log(cssValidation.message || `✓ ${cssValidation.totalRules} 条规则`);
        console.groupEnd();
        
        // 动画检查
        console.group('✨ 动画并发检查');
        const animationValidation = this.validateAnimationConcurrency();
        console.log(`飘字: ${animationValidation.floatingTexts}`);
        console.log(`环境特效: ${animationValidation.environmentEffects}`);
        console.log(`战斗特效: ${animationValidation.combatEffects}`);
        if (animationValidation.issues.length > 0) {
            console.warn(animationValidation.issues);
        }
        console.groupEnd();
        
        console.groupEnd();
        
        return {
            images: imageValidation,
            css: cssValidation,
            animations: animationValidation
        };
    }
}

// ============================================================
// 3. 性能预算管理器
// ============================================================

class BudgetManager {
    constructor() {
        this.budgets = {
            // UI元素预算
            uiElements: {
                maxDOM: 150,
                maxCSS: 500,
                maxImages: 15,
                maxLoadTime: 1500
            },
            
            // 动画预算
            animations: {
                maxFPS: 60,
                minFPS: 45,
                maxConcurrent: 20
            },
            
            // 资源预算
            resources: {
                maxImageSize: 50,  // MB
                maxCSSSize: 100,   // KB
                maxJSSize: 500     // KB
            }
        };
        
        this.violations = [];
    }
    
    /**
     * 检查DOM节点预算
     */
    checkDOMBudget() {
        const domCount = document.querySelectorAll('*').length;
        const budget = this.budgets.uiElements.maxDOM;
        
        if (domCount > budget) {
            const violation = {
                type: 'DOM',
                current: domCount,
                budget: budget,
                severity: 'warning'
            };
            this.violations.push(violation);
            console.warn(`⚠️ DOM节点超预算: ${domCount}/${budget}`);
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查CSS预算
     */
    checkCSSBudget() {
        let totalCSS = 0;
        for (let i = 0; i < document.styleSheets.length; i++) {
            try {
                if (document.styleSheets[i].cssRules) {
                    totalCSS += document.styleSheets[i].cssRules.length;
                }
            } catch (e) {}
        }
        
        const budget = this.budgets.uiElements.maxCSS;
        
        if (totalCSS > budget) {
            const violation = {
                type: 'CSS',
                current: totalCSS,
                budget: budget,
                severity: 'warning'
            };
            this.violations.push(violation);
            console.warn(`⚠️ CSS规则超预算: ${totalCSS}/${budget}`);
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查动画预算
     */
    checkAnimationBudget() {
        const floatingTexts = document.querySelectorAll('.floating-damage').length;
        const budget = this.budgets.animations.maxConcurrent;
        
        if (floatingTexts > budget) {
            const violation = {
                type: 'Animation',
                current: floatingTexts,
                budget: budget,
                severity: 'error'
            };
            this.violations.push(violation);
            console.error(`❌ 动画超预算: ${floatingTexts}/${budget}`);
            return false;
        }
        
        return true;
    }
    
    /**
     * 运行所有预算检查
     */
    runAllChecks() {
        console.group('📋 性能预算检查');
        
        const domOk = this.checkDOMBudget();
        const cssOk = this.checkCSSBudget();
        const animOk = this.checkAnimationBudget();
        
        const allOk = domOk && cssOk && animOk;
        
        if (allOk) {
            console.log('✓ 所有预算检查通过！');
        } else {
            console.log(`❌ 共有 ${this.violations.length} 项预算违规`);
            console.table(this.violations);
        }
        
        console.groupEnd();
        
        return {
            allOk: allOk,
            violations: this.violations
        };
    }
}

// ============================================================
// 4. 全局工具栏 (开发者工具)
// ============================================================

class PerformanceToolbar {
    constructor() {
        this.metrics = new PerformanceMetrics();
        this.validator = new AssetValidator();
        this.budget = new BudgetManager();
        
        this.createToolbar();
    }
    
    createToolbar() {
        const toolbar = document.createElement('div');
        toolbar.id = 'performance-toolbar';
        toolbar.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            z-index: 10000;
            background: rgba(26, 26, 46, 0.95);
            border: 1px solid rgba(212, 175, 55, 0.3);
            border-radius: 8px;
            padding: 12px;
            font-family: monospace;
            font-size: 12px;
            color: #d4af37;
            max-width: 300px;
            max-height: 400px;
            overflow-y: auto;
        `;
        
        toolbar.innerHTML = `
            <div style="margin-bottom: 10px; font-weight: bold;">⚙️ 性能工具</div>
            <div style="font-size: 11px; line-height: 1.6;">
                <div>FPS: <span id="toolbar-fps">--</span></div>
                <div>内存: <span id="toolbar-memory">--</span>MB</div>
                <div>帧时间: <span id="toolbar-frametime">--</span>ms</div>
                <div style="border-top: 1px solid rgba(212, 175, 55, 0.2); margin: 8px 0; padding-top: 8px;">
                    <button onclick="window.performanceToolbar.showReport()" 
                            style="background: #d4af37; color: #1a1a2e; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; margin: 4px 0; width: 100%;">
                        📊 性能报告
                    </button>
                    <button onclick="window.performanceToolbar.validateAssets()" 
                            style="background: #d4af37; color: #1a1a2e; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; margin: 4px 0; width: 100%;">
                        🔍 资产检查
                    </button>
                    <button onclick="window.performanceToolbar.checkBudget()" 
                            style="background: #d4af37; color: #1a1a2e; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; margin: 4px 0; width: 100%;">
                        📋 预算检查
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(toolbar);
        
        // 启动监控
        this.metrics.start();
        
        // 更新显示
        this.metrics.on('all', () => this.updateDisplay());
    }
    
    updateDisplay() {
        document.getElementById('toolbar-fps').textContent = this.metrics.metrics.fps;
        document.getElementById('toolbar-memory').textContent = this.metrics.metrics.memory;
        document.getElementById('toolbar-frametime').textContent = 
            this.metrics.metrics.frameTime.toFixed(2);
    }
    
    showReport() {
        const report = this.metrics.getReport();
        console.log('📊 性能报告:', report);
        alert(`性能报告已输出到控制台\n平均FPS: ${report.averageFPS}`);
    }
    
    validateAssets() {
        const report = this.validator.generateFullReport();
        alert('资产检查已完成，详见控制台');
    }
    
    checkBudget() {
        const result = this.budget.runAllChecks();
        if (!result.allOk) {
            alert(`发现 ${result.violations.length} 项预算违规，详见控制台`);
        } else {
            alert('✓ 所有预算检查通过！');
        }
    }
}

// ============================================================
// 5. 初始化
// ============================================================

// 在页面加载后初始化工具
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.performanceToolbar = new PerformanceToolbar();
    });
} else {
    window.performanceToolbar = new PerformanceToolbar();
}

// 导出到全局作用域供脚本访问
window.PerformanceMetrics = PerformanceMetrics;
window.AssetValidator = AssetValidator;
window.BudgetManager = BudgetManager;

console.log('✓ 性能监控工具已加载');
console.log('💡 使用 window.performanceToolbar 访问工具');
