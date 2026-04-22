import { mapService } from './MapService.js';

function escapeText(value) {
    return window.escapeHtml ? window.escapeHtml(value) : String(value ?? '');
}

function showToast(message, type = 'info') {
    if (window.moduleManager?.showToast) {
        window.moduleManager.showToast(message, type);
        return;
    }
    if (window.authManager?.showToast) {
        window.authManager.showToast(message, type);
        return;
    }
    console.log(`[${type}] ${message}`);
}

function hasGameLayout() {
    return !!document.getElementById('map-module');
}

export class MapUI {
    async init() {
        return hasGameLayout() ? this.switchGameTab('explore') : this.loadStandaloneMaps();
    }

    async switchGameTab(tab) {
        document.querySelectorAll('#map-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.mapTab === tab);
        });
        const explorePanel = document.getElementById('map-explore-panel');
        const listPanel = document.getElementById('map-list-panel');
        if (explorePanel) explorePanel.style.display = tab === 'explore' ? '' : 'none';
        if (listPanel) listPanel.style.display = tab === 'list' ? '' : 'none';
        return tab === 'explore' ? this.loadCurrentMap() : this.loadMapList();
    }

    async loadCurrentMap() {
        const infoEl = document.getElementById('current-map-info');
        const exploreBtn = document.getElementById('explore-btn');
        if (!infoEl) return;
        try {
            const map = await mapService.getCurrentMap();
            if (!map) {
                infoEl.style.display = 'none';
                if (exploreBtn) {
                    exploreBtn.disabled = true;
                    exploreBtn.innerHTML = '<i class="fa-solid fa-map-location-dot"></i> 请先进入地图';
                }
                return;
            }
            infoEl.style.display = '';
            infoEl.innerHTML = `
                <div class="flex items-center gap-4">
                    <span style="font-size:2rem;">${map.icon || '🗺️'}</span>
                    <div class="flex-1">
                        <h3 class="font-bold">${escapeText(map.name || '未知地图')}</h3>
                        <div class="text-sm text-muted">${escapeText(map.description || '')}</div>
                    </div>
                    <span class="text-xs px-3 py-1 rounded" style="background:${map.mapType === 'SAFE' ? 'rgba(46,204,113,0.2)' : 'rgba(231,76,60,0.2)'};color:${map.mapType === 'SAFE' ? '#2ecc71' : '#e74c3c'};">
                        ${map.mapType === 'SAFE' ? '安全区' : '危险区'}
                    </span>
                </div>
                ${map.monsterLevel ? `<div class="text-xs text-muted mt-2">怪物等级: ${map.monsterLevel}</div>` : ''}
            `;
            if (exploreBtn) {
                exploreBtn.disabled = map.mapType === 'SAFE';
                exploreBtn.innerHTML = map.mapType === 'SAFE'
                    ? '<i class="fa-solid fa-shield-halved"></i> 安全区无法探索'
                    : '<i class="fa-solid fa-compass"></i> 开始探索';
            }
        } catch {
            infoEl.style.display = 'none';
        }
    }

    async loadMapList() {
        const container = document.getElementById('mapList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载地图...</p></div>';
        try {
            const maps = await mapService.getMapList();
            if (maps.length === 0) {
                container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无地图数据</div>';
                return;
            }
            container.innerHTML = maps.map(map => {
                const isCurrent = !!map.isCurrent;
                const isLocked = !!map.isLocked;
                return `
                    <div class="map-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isCurrent ? 'var(--accent-gold)' : 'rgba(255,255,255,0.1)'};">
                        <div class="flex items-center gap-2 mb-2">
                            <span style="font-size:1.5rem;">${map.icon || '🗺️'}</span>
                            <div class="flex-1">
                                <h4 class="font-semibold">${escapeText(map.name || '未知')}</h4>
                                <span class="text-xs text-muted">Lv.${map.requiredLevel || 1}+</span>
                            </div>
                            ${isCurrent ? '<span class="text-xs px-2 py-1 rounded" style="background:rgba(212,175,55,0.2);color:var(--accent-gold);">当前</span>' : ''}
                            ${isLocked ? '<i class="fa-solid fa-lock text-muted"></i>' : ''}
                        </div>
                        <div class="text-xs text-muted mb-3">${escapeText(map.description || '无描述')}</div>
                        <button class="btn btn-sm w-full ${isCurrent ? '' : 'btn-primary'}" onclick="enterMap(${map.id})" ${isCurrent || isLocked ? 'disabled' : ''}>
                            ${isCurrent ? '当前所在' : isLocked ? '等级不足' : '进入'}
                        </button>
                    </div>
                `;
            }).join('');
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    async enterMap(mapId) {
        try {
            await mapService.enterMap(mapId);
            showToast('已进入地图！', 'success');
            await this.loadCurrentMap();
            await this.loadMapList();
        } catch (error) {
            showToast('进入地图失败: ' + error.message, 'error');
        }
    }

    async exploreMap() {
        const btn = document.getElementById('explore-btn');
        if (btn) {
            btn.disabled = true;
            btn.textContent = '探索中...';
        }
        try {
            const encounter = await mapService.exploreMap();
            showToast(`遭遇 ${encounter?.monsterName || '怪物'}！`, 'info');
            if (typeof window.showModule === 'function' && confirm(`遭遇了 ${encounter?.monsterName || '怪物'}！开始战斗？`)) {
                window.showModule('combat');
            }
        } catch (error) {
            showToast('探索失败: ' + error.message, 'error');
        } finally {
            await this.loadCurrentMap();
        }
    }

    async loadStandaloneMaps() {
        const container = document.getElementById('mapRegions');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载地图中...</p></div>';
        try {
            const maps = await mapService.getMapList();
            container.innerHTML = maps.length === 0
                ? '<div class="empty-state">暂无地图</div>'
                : maps.map(map => `
                    <div class="map-region-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);margin-bottom:12px;">
                        <div class="font-semibold mb-1">${escapeText(map.name || '未知区域')}</div>
                        <div class="text-sm text-muted mb-2">${escapeText(map.description || '暂无描述')}</div>
                        <div class="text-xs text-muted">等级需求 ${map.requiredLevel || 1}</div>
                    </div>
                `).join('');
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }
}

export const mapUI = new MapUI();
