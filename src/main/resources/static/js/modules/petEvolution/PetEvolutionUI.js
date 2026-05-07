import { petEvolutionService } from './PetEvolutionService.js';

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

function getTargetPetName(info) {
    return info?.evolutionPetName || info?.targetPetName || '进化形态';
}

export class PetEvolutionUI {
    async init() {
        return this.loadEvolutionInfo();
    }

    async loadEvolutionInfo() {
        const select = document.getElementById('evolution-pet-select');
        const container = document.getElementById('petEvolutionList');
        if (!select || !container) return;

        const playerPetId = select.value;
        if (!playerPetId) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">请选择要进化的宠物</div>';
            return;
        }

        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>检查进化条件中...</p></div>';
        try {
            const info = await petEvolutionService.getEvolutionInfo(playerPetId);
            const currentPetName = info.currentPetName || info.currentPetNickname || '宠物';
            const targetPetName = getTargetPetName(info);

            container.innerHTML = info.canEvolve
                ? `
                    <div class="evolution-ready p-4 rounded" style="grid-column:1/-1;background:rgba(46,204,113,0.05);border:1px solid rgba(46,204,113,0.3);">
                        <div class="text-center mb-4">
                            <span style="font-size:3rem;">${info.currentIcon || '🐥'}</span>
                            <span style="font-size:2rem;margin:0 10px;color:#2ecc71;">→</span>
                            <span style="font-size:3rem;">${info.evolutionIcon || '✨'}</span>
                        </div>
                        <h4 class="text-center font-bold mb-2" style="color:#2ecc71;">${escapeText(currentPetName)} → ${escapeText(targetPetName)}</h4>
                        <div class="text-sm text-muted text-center mb-4">${escapeText(info.currentQuality || '普通')} → ${escapeText(info.evolutionQuality || '优秀')}</div>
                        <button class="btn btn-lg w-full" style="background:#2ecc71;color:#fff;" onclick="doEvolution(${playerPetId})">开始进化</button>
                    </div>
                `
                : `
                    <div class="evolution-info p-4 rounded" style="grid-column:1/-1;background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                        <h4 class="text-center font-bold mb-2">${escapeText(currentPetName)} → ${escapeText(targetPetName)}</h4>
                        <div class="text-sm text-red-400 text-center">${escapeText(info.reason || '暂不满足进化条件')}</div>
                    </div>
                `;
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    async doEvolution(playerPetId) {
        if (!confirm('确定要进化这只宠物吗？进化成功后宠物将获得新的形态。')) return;

        try {
            const result = await petEvolutionService.evolvePet(playerPetId);
            if (result?.isSuccess || result?.success) {
                const petName = result.newPetName || result.newName || result.targetPetName || '新形态';
                showToast(`进化成功，恭喜获得 ${petName}`, 'success');
            } else {
                showToast(`进化失败: ${result?.message || '材料不足'}`, 'error');
            }

            await this.loadEvolutionInfo();
            if (window.loadMyPets) await window.loadMyPets();
        } catch (error) {
            showToast(`进化失败: ${error.message}`, 'error');
        }
    }
}

export const petEvolutionUI = new PetEvolutionUI();
