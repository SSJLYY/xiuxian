/**
 * 地图模块 - UI渲染层
 */
import { mapService } from './MapService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { modal } from '../../components/Modal.js';

export class MapUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadMapData();
    }

    setupElements() {
        this.elements = {
            currentMapContainer: document.getElementById('currentMapContainer'),
            mapListContainer: document.getElementById('mapListContainer'),
            mapTabs: document.querySelectorAll('[data-tab="map"]')
        };
    }

    bindEvents() {
        this.elements.mapTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchTab(e.target.dataset.mapTab);
            });
        });
    }

    switchTab(tabName) {
        this.elements.mapTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.mapTab === tabName);
        });

        if (tabName === 'current') {
            this.elements.currentMapContainer.style.display = 'block';
            this.elements.mapListContainer.style.display = 'none';
        } else {
            this.elements.currentMapContainer.style.display = 'none';
            this.elements.mapListContainer.style.display = 'block';
        }
    }

    async loadMapData() {
        loading.show();
        try {
            await Promise.all([
                mapService.getCurrentMap(),
                mapService.getMapList(),
                mapService.getExploredMaps()
            ]);
            this.renderCurrentMap();
            this.renderMapList();
        } catch (error) {
            toast.error('加载地图数据失败');
        } finally {
            loading.hide();
        }
    }

    renderCurrentMap() {
        const container = this.elements.currentMapContainer;
        if (!container) return;

        const map = mapService.currentMap;
        if (!map) {
            container.innerHTML = '<p>暂无地图信息</p>';
            return;
        }

        container.innerHTML = `
            <div class="current-map">
                <div class="map-image">
                    <img src="${map.image || '/images/maps/default.png'}" alt="${map.name}">
                </div>
                <div class="map-info">
                    <h3>${map.name}</h3>
                    <p class="map-desc">${map.description}</p>
                    <div class="map-details">
                        <div class="detail-item">
                            <span class="label">等级要求:</span>
                            <span class="value">${map.requiredLevel}级</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">怪物等级:</span>
                            <span class="value">${map.monsterLevel}级</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">经验加成:</span>
                            <span class="value">x${map.expMultiplier}</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">灵石加成:</span>
                            <span class="value">x${map.spiritStoneMultiplier}</span>
                        </div>
                    </div>
                </div>
                <div class="map-actions">
                    <button class="btn btn-primary" id="exploreBtn">探索</button>
                    <button class="btn btn-info" id="teleportBtn">传送</button>
                </div>
            </div>
        `;

        // 绑定事件
        document.getElementById('exploreBtn')?.addEventListener('click', () => {
            this.handleExplore(map.id);
        });

        document.getElementById('teleportBtn')?.addEventListener('click', () => {
            this.showTeleportDialog();
        });
    }

    renderMapList() {
        const container = this.elements.mapListContainer;
        if (!container) return;

        if (mapService.availableMaps.length === 0) {
            container.innerHTML = '<p>暂无可用地图</p>';
            return;
        }

        container.innerHTML = `
            <div class="map-list">
                ${mapService.availableMaps.map(map => {
                    const isExplored = mapService.exploredMaps.includes(map.id);
                    const isCurrent = mapService.currentMap?.id === map.id;

                    return `
                        <div class="map-card ${isCurrent ? 'current' : ''} ${isExplored ? 'explored' : ''}">
                            <div class="map-image">
                                <img src="${map.image || '/images/maps/default.png'}" alt="${map.name}">
                            </div>
                            <div class="map-info">
                                <h4>${map.name}</h4>
                                <p>${map.description}</p>
                                <div class="map-stats">
                                    <span>要求等级: ${map.requiredLevel}</span>
                                    <span>怪物等级: ${map.monsterLevel}</span>
                                </div>
                            </div>
                            <div class="map-status">
                                ${isCurrent ? '<span class="status current">当前地图</span>' : ''}
                                ${isExplored ? '<span class="status explored">已探索</span>' : '<span class="status locked">未探索</span>'}
                            </div>
                            <div class="map-actions">
                                ${!isCurrent ? `
                                    <button class="btn btn-primary" data-action="teleport" data-map-id="${map.id}">传送</button>
                                    ${!isExplored ? `
                                        <button class="btn btn-info" data-action="explore" data-map-id="${map.id}">探索</button>
                                    ` : ''}
                                ` : ''}
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;

        // 绑定事件
        container.querySelectorAll('[data-action="teleport"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTeleport(e.target.dataset.mapId));
        });

        container.querySelectorAll('[data-action="explore"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleExplore(e.target.dataset.mapId));
        });
    }

    showTeleportDialog() {
        const currentMap = mapService.currentMap;
        const availableMaps = mapService.availableMaps.filter(m => m.id !== currentMap?.id);

        if (availableMaps.length === 0) {
            toast.info('没有可传送的地图');
            return;
        }

        const teleportHtml = `
            <div class="teleport-list">
                ${availableMaps.map(map => `
                    <div class="teleport-option" data-map-id="${map.id}">
                        <div class="map-name">${map.name}</div>
                        <div class="map-level">要求等级: ${map.requiredLevel}</div>
                    </div>
                `).join('')}
            </div>
        `;

        modal.show({
            title: '选择传送地图',
            content: teleportHtml,
            showCancel: true,
            confirmText: '取消'
        });

        document.querySelectorAll('.teleport-option').forEach(option => {
            option.addEventListener('click', (e) => {
                const mapId = e.currentTarget.dataset.mapId;
                this.handleTeleport(mapId);
                modal.hide();
            });
        });
    }

    async handleTeleport(mapId) {
        loading.show();
        try {
            await mapService.teleportToMap(mapId);
            await this.loadMapData();
        } catch (error) {
            toast.error('传送失败');
        } finally {
            loading.hide();
        }
    }

    async handleExplore(mapId) {
        loading.show();
        try {
            await mapService.exploreMap(mapId);
            await this.loadMapData();
        } catch (error) {
            toast.error('探索失败');
        } finally {
            loading.hide();
        }
    }
}

export const mapUI = new MapUI();
