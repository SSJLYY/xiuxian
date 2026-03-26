/**
 * 游戏地图系统 - 关卡设计实现
 * 提供世界地图探索、地图切换、离线挂机等功能
 */

class GameMapSystem {
    constructor() {
        this.currentMap = null;
        this.allMaps = [];
        this.playerProgress = [];
        this.mapUnlocks = {};
        this.isLoading = false;
    }

    /**
     * 初始化地图系统
     */
    async init() {
        console.log('[GameMap] 初始化地图系统...');
        await this.loadAllMaps();
        await this.loadPlayerProgress();
        this.renderMapModule();
    }

    /**
     * 加载所有地图数据
     */
    async loadAllMaps() {
        try {
            const response = await gameAPI.getAllMaps();
            if (response.success) {
                this.allMaps = response.data || [];
                console.log(`[GameMap] 加载了 ${this.allMaps.length} 个地图`);
            }
        } catch (error) {
            console.error('[GameMap] 加载地图失败:', error);
        }
    }

    /**
     * 加载玩家地图进度
     */
    async loadPlayerProgress() {
        try {
            const response = await gameAPI.getPlayerMapProgress();
            if (response.success) {
                this.playerProgress = response.data || [];
                this.currentMap = this.playerProgress.find(p => p.current);
                console.log('[GameMap] 当前地图:', this.currentMap?.map?.name);
            }
        } catch (error) {
            console.error('[GameMap] 加载进度失败:', error);
        }
    }

    /**
     * 渲染地图模块
     */
    renderMapModule() {
        const container = document.getElementById('map-module');
        if (!container) return;

        container.innerHTML = `
            <div class="map-container">
                <div class="map-header">
                    <h2><i class="fas fa-map-marked-alt"></i> 苍玄界地图</h2>
                    <div class="current-location">
                        <span class="location-label">当前位置：</span>
                        <span class="location-name" id="currentMapName">
                            ${escapeHtml(this.currentMap?.map?.name || '未知区域')}
                        </span>
                    </div>
                </div>
                
                <div class="map-world-view" id="mapWorldView">
                    ${this.renderWorldMap()}
                </div>
                
                <div class="map-details-panel" id="mapDetailsPanel">
                    ${this.renderMapDetails(this.currentMap?.map)}
                </div>
            </div>
        `;

        this.bindMapEvents();
    }

    /**
     * 渲染世界地图视图
     */
    renderWorldMap() {
        // 按区域分组地图
        const regions = this.groupMapsByRegion();
        
        return `
            <div class="world-map">
                ${Object.entries(regions).map(([region, maps]) => `
                    <div class="map-region" data-region="${region}">
                        <h3 class="region-name">${this.getRegionDisplayName(region)}</h3>
                        <div class="region-maps">
                            ${maps.map(map => this.renderMapNode(map)).join('')}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    /**
     * 渲染单个地图节点
     */
    renderMapNode(map) {
        const progress = this.playerProgress.find(p => p.mapId === map.id);
        const isUnlocked = progress?.unlocked || false;
        const isCurrent = progress?.current || false;
        const isDangerous = map.mapType === 'DANGEROUS' || map.mapType === 'BOSS';
        
        return `
            <div class="map-node ${isUnlocked ? 'unlocked' : 'locked'} ${isCurrent ? 'current' : ''} ${isDangerous ? 'dangerous' : ''}"
                 data-map-id="${map.id}"
                 onclick="gameMapSystem.selectMap(${map.id})">
                <div class="map-node-icon">
                    ${this.getMapIcon(map.mapType)}
                </div>
                <div class="map-node-info">
                    <div class="map-name">${map.name}</div>
                    <div class="map-meta">
                        <span class="danger-level">${'⚠️'.repeat(map.dangerLevel || 1)}</span>
                        <span class="map-type">${this.getMapTypeLabel(map.mapType)}</span>
                    </div>
                </div>
                ${isCurrent ? '<div class="current-indicator">📍</div>' : ''}
                ${!isUnlocked ? '<div class="lock-overlay">🔒</div>' : ''}
            </div>
        `;
    }

    /**
     * 渲染地图详情面板
     */
    renderMapDetails(map) {
        if (!map) {
            return `<div class="map-details-empty">选择一个地图查看详情</div>`;
        }

        const progress = this.playerProgress.find(p => p.mapId === map.id);
        const isUnlocked = progress?.unlocked || false;
        const isCurrent = progress?.current || false;
        const canEnter = isUnlocked && !isCurrent;

        return `
            <div class="map-details" style="--map-theme: ${map.themeColor || '#4a5568'}">
                <div class="map-details-header">
                    <h3>${map.name}</h3>
                    <span class="map-badge ${map.mapType}">${this.getMapTypeLabel(map.mapType)}</span>
                </div>
                
                <div class="map-ambience">
                    <div class="ambience-visual" style="background: ${map.themeColor || '#4a5568'}"></div>
                    <p class="ambience-text">${map.ambienceText || '一片神秘的区域...'}</p>
                </div>
                
                <div class="map-stats">
                    <div class="stat-item">
                        <span class="stat-label">危险等级</span>
                        <span class="stat-value danger">${'⚠️'.repeat(map.dangerLevel || 1)}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">推荐境界</span>
                        <span class="stat-value">${map.requiredRealm || '练气期'}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">灵石收益</span>
                        <span class="stat-value">${map.baseSpiritStones || 10}/小时</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">经验倍率</span>
                        <span class="stat-value">x${map.expModifier || 1.0}</span>
                    </div>
                </div>
                
                <div class="map-description">
                    <h4>区域描述</h4>
                    <p>${escapeHtml(map.description || '暂无描述')}</p>
                </div>
                
                ${map.unlockCondition ? `
                    <div class="unlock-condition">
                        <h4>解锁条件</h4>
                        <p>${escapeHtml(map.unlockCondition)}</p>
                    </div>
                ` : ''}
                
                <div class="map-actions">
                    ${isCurrent ? `
                        <button class="btn btn-primary" disabled>
                            <i class="fas fa-check"></i> 当前位置
                        </button>
                    ` : isUnlocked ? `
                        <button class="btn btn-primary" onclick="gameMapSystem.enterMap(${map.id})">
                            <i class="fas fa-walking"></i> 进入此区域
                        </button>
                    ` : `
                        <button class="btn btn-secondary" disabled>
                            <i class="fas fa-lock"></i> 未解锁
                        </button>
                    `}
                    
                    ${isCurrent && map.mapType !== 'SAFE' ? `
                        <button class="btn btn-warning" onclick="gameMapSystem.startOfflineHanging(${map.id})">
                            <i class="fas fa-moon"></i> 离线挂机
                        </button>
                    ` : ''}
                </div>
                
                ${progress ? `
                    <div class="map-progress">
                        <h4>探索进度</h4>
                        <div class="progress-stats">
                            <span>击杀数: ${progress.totalKills || 0}</span>
                            <span>累计停留: ${this.formatDuration(progress.totalTimeSpent || 0)}</span>
                        </div>
                    </div>
                ` : ''}
            </div>
        `;
    }

    /**
     * 选择地图
     */
    selectMap(mapId) {
        const map = this.allMaps.find(m => m.id === mapId);
        if (!map) return;

        const panel = document.getElementById('mapDetailsPanel');
        if (panel) {
            panel.innerHTML = this.renderMapDetails(map);
        }

        // 高亮选中的节点
        document.querySelectorAll('.map-node').forEach(node => {
            node.classList.remove('selected');
        });
        const selectedNode = document.querySelector(`[data-map-id="${mapId}"]`);
        if (selectedNode) {
            selectedNode.classList.add('selected');
        }
    }

    /**
     * 进入地图
     */
    async enterMap(mapId) {
        if (this.isLoading) return;
        this.isLoading = true;

        try {
            const response = await gameAPI.enterMap(mapId);
            if (response.success) {
                this.showToast(`已进入 ${response.data?.map?.name || '新区域'}`, 'success');
                await this.loadPlayerProgress();
                this.renderMapModule();
                
                // 触发进入地图事件
                this.onMapEntered(response.data);
            } else {
                this.showToast(response.message || '进入地图失败', 'error');
            }
        } catch (error) {
            console.error('[GameMap] 进入地图失败:', error);
            this.showToast('进入地图失败', 'error');
        } finally {
            this.isLoading = false;
        }
    }

    /**
     * 开始离线挂机
     */
    async startOfflineHanging(mapId) {
        const map = this.allMaps.find(m => m.id === mapId);
        if (!map) return;

        if (map.offlineRisk) {
            const confirmed = confirm(
                `⚠️ 危险区域离线挂机警告\n\n` +
                `在${map.name}离线挂机存在风险：\n` +
                `• 超过12小时会自动返回安全区\n` +
                `• 宠物饱食度<20时无法挂机\n` +
                `• 可能遭遇随机事件\n\n` +
                `是否确认在此区域离线挂机？`
            );
            if (!confirmed) return;
        }

        this.showToast(`已设置在${map.name}离线挂机`, 'info');
    }

    /**
     * 地图进入后的处理
     */
    onMapEntered(mapData) {
        // 更新游戏状态
        if (window.gameManager) {
            window.gameManager.addCultivationLog(`进入新区域: ${mapData.map?.name}`);
        }
        
        // 刷新玩家数据
        if (window.authManager) {
            window.authManager.loadPlayerProfile();
        }
    }

    /**
     * 按区域分组地图
     */
    groupMapsByRegion() {
        const groups = {};
        this.allMaps.forEach(map => {
            const region = map.region || '其他';
            if (!groups[region]) {
                groups[region] = [];
            }
            groups[region].push(map);
        });
        return groups;
    }

    /**
     * 获取区域显示名称
     */
    getRegionDisplayName(region) {
        const names = {
            'qingyun': '青云镇区域',
            'tianjian': '天剑宗区域',
            'wilderness': '荒野区域',
            'yaoshou': '妖兽林区域',
            'wanfa': '万法阁区域',
            'secret': '上古秘境'
        };
        return names[region] || region;
    }

    /**
     * 获取地图图标
     */
    getMapIcon(mapType) {
        const icons = {
            'SAFE': '🏠',
            'NORMAL': '🌲',
            'DANGEROUS': '⚠️',
            'BOSS': '👹',
            'DUNGEON': '🏰'
        };
        return icons[mapType] || '📍';
    }

    /**
     * 获取地图类型标签
     */
    getMapTypeLabel(mapType) {
        const labels = {
            'SAFE': '安全区',
            'NORMAL': '普通区域',
            'DANGEROUS': '危险区域',
            'BOSS': 'BOSS区域',
            'DUNGEON': '副本'
        };
        return labels[mapType] || '未知';
    }

    /**
     * 格式化时长
     */
    formatDuration(minutes) {
        if (minutes < 60) return `${minutes}分钟`;
        if (minutes < 1440) return `${Math.floor(minutes / 60)}小时`;
        return `${Math.floor(minutes / 1440)}天`;
    }

    /**
     * 显示提示
     */
    showToast(message, type = 'info') {
        if (window.authManager && window.authManager.showToast) {
            window.authManager.showToast(message, type);
        } else {
            console.log(`[${type}] ${message}`);
        }
    }

    /**
     * 绑定地图事件
     */
    bindMapEvents() {
        // 事件已在HTML中通过onclick绑定
    }
}

// 创建全局实例
const gameMapSystem = new GameMapSystem();

// 添加到游戏API
gameAPI.getAllMaps = async function() {
    return await api.get('/maps');
};

gameAPI.getPlayerMapProgress = async function() {
    return await api.get('/maps/progress');
};

gameAPI.enterMap = async function(mapId) {
    return await api.post(`/maps/enter/${mapId}`);
};

gameAPI.getMapDetail = async function(mapId) {
    return await api.get(`/maps/${mapId}`);
};

// 导出到全局
window.gameMapSystem = gameMapSystem;

console.log('[GameMap] 地图系统模块已加载');
