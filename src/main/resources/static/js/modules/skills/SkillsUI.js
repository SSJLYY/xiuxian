import { skillsService } from './SkillsService.js';

function escapeText(value) {
    return window.escapeHtml ? window.escapeHtml(value) : String(value ?? '');
}

function getSkillEmoji(element) {
    const emojis = { FIRE: '🔥', WATER: '💧', GRASS: '🌿', THUNDER: '⚡', ICE: '❄️', DARK: '🌙', LIGHT: '☀️', PHYSICAL: '⚔️' };
    return emojis[element] || '⚔️';
}

export class SkillsUI {
    async init() {
        return this.switchSkillTab('learned');
    }

    switchSkillTab(tab) {
        document.querySelectorAll('#skills-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.skillTab === tab);
        });
        const learnedPanel = document.getElementById('skills-learned-panel');
        const availablePanel = document.getElementById('skills-available-panel');
        if (learnedPanel) learnedPanel.style.display = tab === 'learned' ? '' : 'none';
        if (availablePanel) availablePanel.style.display = tab === 'available' ? '' : 'none';
        return tab === 'learned' ? this.loadMySkills() : this.loadAvailableSkills();
    }

    async loadMySkills() {
        const container = document.getElementById('mySkillsList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载技能...</p></div>';
        try {
            const skills = await skillsService.getMySkills();
            this.renderMySkills(skills);
            this.updateSkillStats(skills);
        } catch (e) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(e.message)}</div>`;
        }
    }

    renderMySkills(skills) {
        const container = document.getElementById('mySkillsList');
        if (!container) return;
        if (!skills.length) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">您还没有学会任何技能</div>';
            return;
        }

        container.innerHTML = skills.map(skill => {
            const elementColor = { FIRE: '#e74c3c', WATER: '#3498db', GRASS: '#27ae60', THUNDER: '#f1c40f', ICE: '#00bcd4', DARK: '#9b59b6', LIGHT: '#f39c12', PHYSICAL: '#95a5a6' }[skill.elementType] || '#aaa';
            const isEquipped = skill.equippedSlot != null;
            const playerSkillId = skill.playerSkillId || skill.id;
            return `
                <div class="skill-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isEquipped ? elementColor : 'rgba(255,255,255,0.1)'};">
                    <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center gap-2">
                            <span class="skill-icon" style="font-size:1.3rem;">${getSkillEmoji(skill.elementType)}</span>
                            <div>
                                <h4 class="font-semibold">${escapeText(skill.name)}</h4>
                                <span class="text-xs" style="color:${elementColor};">${skill.elementTypeName || skill.elementType || '物理'}</span>
                            </div>
                        </div>
                        ${isEquipped ? `<span class="text-xs px-2 py-1 rounded" style="background:rgba(46,204,113,0.2);color:#2ecc71;">已装备 #${skill.equippedSlot + 1}</span>` : ''}
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeText(skill.description || '无描述')}</div>
                    <div class="flex gap-2 text-xs text-muted mb-3">
                        <span>等级 ${skill.level || 1}</span>
                        <span>|</span>
                        <span>伤害 ${skill.damage || skill.baseDamage || 0}</span>
                        <span>|</span>
                        <span>冷却 ${skill.cooldown || 0}秒</span>
                    </div>
                    <div class="flex gap-2 flex-wrap mt-3">
                        ${!isEquipped ? `<button class="btn btn-sm btn-primary" onclick="equipSkill(${playerSkillId}, 0)"><i class="fa-solid fa-hand-sparkles"></i> 装备</button>` : ''}
                        ${isEquipped ? `<button class="btn btn-sm" onclick="unequipSkill(${playerSkillId})"><i class="fa-solid fa-hand"></i> 卸下</button>` : ''}
                        <button class="btn btn-sm" onclick="upgradeSkill(${playerSkillId})"><i class="fa-solid fa-arrow-up"></i> 升级</button>
                    </div>
                </div>
            `;
        }).join('');
    }

    updateSkillStats(skills) {
        const count = skills.filter(s => s.equippedSlot != null).length;
        const el1 = document.getElementById('skill-points-value');
        const el2 = document.getElementById('equipped-skills-count');
        if (el1) el1.textContent = count;
        if (el2) el2.textContent = count;
    }

    async loadAvailableSkills() {
        const container = document.getElementById('skillsAvailableList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载可学习技能...</p></div>';
        try {
            const skills = await skillsService.getAvailableSkills();
            this.renderAvailableSkills(skills);
        } catch (e) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(e.message)}</div>`;
        }
    }

    renderAvailableSkills(skills) {
        const container = document.getElementById('skillsAvailableList');
        if (!container) return;
        if (!skills.length) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">当前没有可学习的技能</div>';
            return;
        }
        container.innerHTML = skills.map(skill => {
            const elementColor = { FIRE: '#e74c3c', WATER: '#3498db', GRASS: '#27ae60', THUNDER: '#f1c40f', ICE: '#00bcd4', DARK: '#9b59b6', LIGHT: '#f39c12', PHYSICAL: '#95a5a6' }[skill.elementType] || '#aaa';
            return `
                <div class="skill-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${elementColor};">
                    <div class="flex items-center gap-2 mb-2">
                        <span style="font-size:1.3rem;">${getSkillEmoji(skill.elementType)}</span>
                        <div>
                            <h4 class="font-semibold">${escapeText(skill.name)}</h4>
                            <span class="text-xs" style="color:${elementColor};">${skill.elementTypeName || skill.elementType || '物理'}</span>
                        </div>
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeText(skill.description || '无描述')}</div>
                    <div class="text-xs text-muted mb-3">等级需求 ${skill.unlockLevel || skill.requiredLevel || 1} | 伤害 ${skill.damage || skill.baseDamage || 0} | 冷却 ${skill.cooldown || 0}秒</div>
                    <div class="text-sm mb-3"><span class="text-muted">学习费用:</span> <span class="font-bold" style="color:var(--accent-gold);"><i class="fa-solid fa-gem"></i> ${skill.requiredSpiritStones || skill.cost || 0}</span></div>
                    <button class="btn btn-sm w-full btn-primary" onclick="learnSkill(${skill.id})"><i class="fa-solid fa-graduation-cap"></i> 学习技能</button>
                </div>
            `;
        }).join('');
    }

    async learnSkill(skillId) {
        await skillsService.learnSkill(skillId);
        if (window.moduleManager) window.moduleManager.showToast('技能学习成功！', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        await this.loadMySkills();
        await this.loadAvailableSkills();
    }

    async equipSkill(playerSkillId, slotNumber) {
        await skillsService.equipSkill(playerSkillId, slotNumber);
        if (window.moduleManager) window.moduleManager.showToast('技能装备成功！', 'success');
        await this.loadMySkills();
    }

    async unequipSkill(playerSkillId) {
        await skillsService.unequipSkill(playerSkillId);
        if (window.moduleManager) window.moduleManager.showToast('技能已卸下', 'success');
        await this.loadMySkills();
    }

    async upgradeSkill(playerSkillId) {
        await skillsService.upgradeSkill(playerSkillId);
        if (window.moduleManager) window.moduleManager.showToast('技能升级成功！', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        await this.loadMySkills();
    }

    switchComboTab(tab) {
        document.querySelectorAll('#combos-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.comboTab === tab);
        });
        return this.loadCombos(tab === 'available');
    }

    async loadCombos(availableOnly = true) {
        const container = document.getElementById('comboContent');
        const totalEl = document.getElementById('combo-total-count');
        const masteredEl = document.getElementById('combo-mastered-count');
        const rateEl = document.getElementById('combo-use-rate');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载连招...</p></div>';
        try {
            const [combos, stats] = await Promise.all([
                skillsService.getCombos(availableOnly),
                skillsService.getComboStats()
            ]);

            if (totalEl) totalEl.textContent = stats.totalAvailable || combos.length;
            if (masteredEl) masteredEl.textContent = stats.masteredCount || 0;
            if (rateEl) rateEl.textContent = stats.usageRate || '0%';

            if (!combos.length) {
                container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无连招数据</div>';
                return;
            }

            container.innerHTML = combos.map(combo => {
                const isActive = combo.isAvailable || combo.isMastered;
                return `
                    <div class="combo-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isActive ? 'var(--accent-gold)' : 'rgba(255,255,255,0.1)'};">
                        <div class="flex items-center justify-between mb-2">
                            <h4 class="font-semibold">${escapeText(combo.name || '连招')}</h4>
                            <span class="text-xs px-2 py-1 rounded" style="background:${isActive ? 'rgba(212,175,55,0.2)' : 'rgba(255,255,255,0.1)'};color:${isActive ? 'var(--accent-gold)' : 'var(--text-muted)'};">
                                ${combo.isMastered ? '已掌握' : combo.isAvailable ? '可用' : '未解锁'}
                            </span>
                        </div>
                        <div class="text-sm text-muted mb-2">${escapeText(combo.description || '无描述')}</div>
                        <div class="text-xs text-muted mb-3">技能序列: ${(combo.skillSequence || []).map(s => escapeText(s)).join(' → ')}</div>
                        <div class="text-xs text-muted">伤害加成: ${combo.bonusPercent ? Number(combo.bonusPercent).toFixed(0) : 0}%</div>
                    </div>
                `;
            }).join('');
        } catch (e) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(e.message)}</div>`;
        }
    }
}

export const skillsUI = new SkillsUI();
