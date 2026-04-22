import { petsService } from './PetsService.js';

function getPetEmoji(type) {
    const emojis = { NORMAL: '🐾', FIRE: '🔥', WATER: '💧', GRASS: '🌿', THUNDER: '⚡', ICE: '❄️', DARK: '🌙', LIGHT: '☀️', DRAGON: '🐉' };
    return emojis[type] || '🐾';
}

function escapeText(value) {
    return window.escapeHtml ? window.escapeHtml(value) : String(value ?? '');
}

export class PetsUI {
    async init() {
        return this.switchTab('my');
    }

    switchTab(tab) {
        document.querySelectorAll('#pets-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.petTab === tab);
        });
        const myPanel = document.getElementById('pets-my-panel');
        const availablePanel = document.getElementById('pets-available-panel');
        if (myPanel) myPanel.style.display = tab === 'my' ? '' : 'none';
        if (availablePanel) availablePanel.style.display = tab === 'available' ? '' : 'none';
        return tab === 'my' ? this.loadMyPets() : this.loadAvailablePets();
    }

    async loadMyPets() {
        const container = document.getElementById('myPetsList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载宠物...</p></div>';
        try {
            const pets = await petsService.getMyPets();
            this.renderMyPets(pets);
            await this.loadActivePetInfo();
        } catch (e) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(e.message)}</div>`;
        }
    }

    renderMyPets(pets) {
        const container = document.getElementById('myPetsList');
        const select = document.getElementById('evolution-pet-select');
        if (!container) return;

        if (select) {
            const defaultOpt = select.querySelector('option[value=""]');
            select.innerHTML = defaultOpt ? defaultOpt.outerHTML : '<option value="">-- 请选择宠物 --</option>';
            pets.forEach(pet => {
                if ((pet.level || 1) >= 10) {
                    const opt = document.createElement('option');
                    opt.value = pet.id;
                    opt.textContent = `${pet.nickname || pet.name || pet.petName} (Lv.${pet.level || 1})`;
                    select.appendChild(opt);
                }
            });
        }

        if (!pets.length) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">您还没有宠物，快去捕捉吧！</div>';
            return;
        }

        container.innerHTML = pets.map(pet => {
            const rarityNames = { 1: '普通', 2: '稀有', 3: '史诗', 4: '传说', 5: '神话' };
            const qualityColors = { 1: '#aaa', 2: '#5ba85b', 3: '#4a90d9', 4: '#9b59b6', 5: '#f39c12' };
            const rarity = Number(pet.rarity || 1);
            const qualityColor = qualityColors[rarity] || '#aaa';
            const isActive = !!pet.isActive;
            const isLocked = !!pet.isLocked;
            const expToNext = pet.expToNext || 100;
            const trainingCooldownUntil = pet.trainCooldownUntil ? new Date(pet.trainCooldownUntil) : null;
            const trainingCoolingDown = trainingCooldownUntil && trainingCooldownUntil > new Date();
            return `
                <div class="pet-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isActive ? qualityColor : 'rgba(255,255,255,0.1)'};">
                    <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center gap-2">
                            <span class="pet-icon" style="font-size:1.5rem;">${getPetEmoji(pet.type || 'NORMAL')}</span>
                            <div>
                                <h4 class="font-semibold">${escapeText(pet.nickname || pet.name || pet.petName || '宠物')}</h4>
                                <span class="text-xs" style="color:${qualityColor};">${rarityNames[rarity] || '普通'}</span>
                            </div>
                        </div>
                        ${isActive ? '<span class="text-xs px-2 py-1 rounded" style="background:rgba(46,204,113,0.2);color:#2ecc71;">出战中</span>' : ''}
                        ${isLocked ? '<i class="fa-solid fa-lock text-muted"></i>' : ''}
                    </div>
                    <div class="text-xs text-muted mb-2">等级 ${pet.level || 1} | 经验 ${pet.exp || 0}/${expToNext}</div>
                    <div class="text-xs text-muted mb-2">攻击 ${pet.attack || 0} | 防御 ${pet.defense || 0} | 生命 ${pet.health || 0}/${pet.maxHealth || 0}</div>
                    <div class="text-xs text-muted mb-2">忠诚 ${pet.loyalty || 0} | 饱食 ${pet.hunger || 0}</div>
                    <div class="flex gap-2 flex-wrap mt-3">
                        ${!isActive ? `<button class="btn btn-sm btn-primary" onclick="activatePet(${pet.id})"><i class="fa-solid fa-play"></i> 出战</button>` : ''}
                        <button class="btn btn-sm" onclick="feedPet(${pet.id})"><i class="fa-solid fa-utensils"></i> 喂食</button>
                        <button class="btn btn-sm" ${trainingCoolingDown ? 'disabled' : ''} onclick="trainPet(${pet.id})"><i class="fa-solid fa-dumbbell"></i> ${trainingCoolingDown ? '训练冷却中' : '训练'}</button>
                        <button class="btn btn-sm" onclick="togglePetLock(${pet.id})"><i class="fa-solid fa-${isLocked ? 'unlock' : 'lock'}"></i></button>
                        ${!isLocked ? `<button class="btn btn-sm btn-danger" onclick="releasePet(${pet.id})"><i class="fa-solid fa-trash"></i></button>` : ''}
                    </div>
                </div>
            `;
        }).join('');
    }

    async loadActivePetInfo() {
        const infoEl = document.getElementById('active-pet-info');
        if (!infoEl) return;
        try {
            const pet = await petsService.getActivePet();
            if (pet) {
                infoEl.style.display = '';
                infoEl.innerHTML = `
                    <div class="flex items-center gap-4">
                        <span style="font-size:2rem;">${getPetEmoji(pet.type || 'NORMAL')}</span>
                        <div class="flex-1">
                            <div class="font-bold" style="color:var(--accent-gold);">${escapeText(pet.nickname || pet.name || pet.petName || '出战宠物')}</div>
                            <div class="text-sm text-muted">等级 ${pet.level || 1} | 战力评估中...</div>
                        </div>
                        <span class="text-sm text-green-400">战斗中...</span>
                    </div>
                `;
            } else {
                infoEl.style.display = 'none';
            }
        } catch {
            infoEl.style.display = 'none';
        }
    }

    async loadAvailablePets() {
        const container = document.getElementById('availablePetsList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载可捕获宠物...</p></div>';
        try {
            const pets = await petsService.getAvailablePets();
            if (!pets.length) {
                container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">当前没有可捕获的宠物</div>';
                return;
            }
            container.innerHTML = pets.map(pet => {
                const rarityNames = { 1: '普通', 2: '稀有', 3: '史诗', 4: '传说', 5: '神话' };
                const qualityColors = { 1: '#aaa', 2: '#5ba85b', 3: '#4a90d9', 4: '#9b59b6', 5: '#f39c12' };
                const rarity = Number(pet.rarity || 1);
                const qualityColor = qualityColors[rarity] || '#aaa';
                return `
                    <div class="pet-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${qualityColor};">
                        <div class="flex items-center gap-2 mb-2">
                            <span style="font-size:1.5rem;">${getPetEmoji(pet.type || 'NORMAL')}</span>
                            <div>
                                <h4 class="font-semibold">${escapeText(pet.name || '野生宠物')}</h4>
                                <span class="text-xs" style="color:${qualityColor};">${rarityNames[rarity] || '普通'}</span>
                            </div>
                        </div>
                        <div class="text-xs text-muted mb-2">解锁等级 ${pet.unlockLevel || 1} | 捕获率 ${pet.captureRate || 0}%</div>
                        <div class="text-xs text-muted mb-3">${escapeText(pet.description || '野生宠物，出没于野外')}</div>
                        <button class="btn btn-sm w-full btn-primary" onclick="capturePet(${pet.id})"><i class="fa-solid fa-hand-sparkles"></i> 捕捉</button>
                    </div>
                `;
            }).join('');
        } catch (e) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(e.message)}</div>`;
        }
    }

    async activatePet(playerPetId) {
        await petsService.activatePet(playerPetId);
        if (window.moduleManager) window.moduleManager.showToast('设置出战成功！', 'success');
        await this.loadMyPets();
    }

    async feedPet(playerPetId) {
        await petsService.feedPet(playerPetId);
        sessionStorage.setItem('tutorial_pet_fed_once', 'true');
        if (window.moduleManager) window.moduleManager.showToast('喂食成功！宠物很开心！', 'success');
        await this.loadMyPets();
        if (window.tutorialSystem?.checkProgress) {
            window.tutorialSystem.checkProgress();
        }
    }

    async trainPet(playerPetId, trainingType) {
        await petsService.trainPet(playerPetId, trainingType);
        if (window.moduleManager) window.moduleManager.showToast('训练成功！成长有所提升。', 'success');
        await this.loadMyPets();
    }

    async togglePetLock(playerPetId) {
        await petsService.toggleLockPet(playerPetId);
        if (window.moduleManager) window.moduleManager.showToast('锁定状态已切换', 'success');
        await this.loadMyPets();
    }

    async releasePet(playerPetId) {
        await petsService.releasePet(playerPetId);
        if (window.moduleManager) window.moduleManager.showToast('宠物已放生', 'info');
        await this.loadMyPets();
    }

    async capturePet(petId) {
        await petsService.capturePet(petId);
        if (window.moduleManager) window.moduleManager.showToast('捕捉成功！获得新宠物！', 'success');
        await this.loadMyPets();
        await this.loadAvailablePets();
    }
}

export const petsUI = new PetsUI();
