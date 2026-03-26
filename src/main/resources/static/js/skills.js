// 技能系统JavaScript

var currentTab = 'my-skills';
var mySkills = [];
var availableSkills = [];
var equippedSkills = [];

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    loadPlayerInfo();
    loadMySkills();
    loadAvailableSkills();
    loadEquippedSkills();
});

// 暴露给 modules.js 调用
window.loadMySkills = loadMySkills;
window.loadAvailableSkills = loadAvailableSkills;
window.loadEquippedSkills = loadEquippedSkills;

// 加载玩家信息
async function loadPlayerInfo() {
    try {
        const response = await gameAPI.getCurrentPlayerProfile();
        if (response.success) {
            const player = response.data;
            document.getElementById('playerName').textContent = player.nickname || 'xiuxian者';
            document.getElementById('playerLevel').textContent = player.level || 1;
            document.getElementById('playerSpiritStones').textContent = player.spiritStones || 0;
            document.getElementById('playerSkillPoints').textContent = player.skillPoints || 0;
        }
    } catch (error) {
        console.error('加载玩家信息失败:', error);
    }
}

// 切换标签页
function showSkillTab(tabName) {
    currentTab = tabName;
    
    // 更新按钮状态
    document.querySelectorAll('.skill-tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    
    // 隐藏所有标签页
    document.querySelectorAll('.skill-tab-content').forEach(tab => tab.style.display = 'none');
    
    // 显示对应标签页
    if (tabName === 'my-skills') {
        document.getElementById('my-skills-tab').style.display = 'block';
        loadMySkills();
    } else if (tabName === 'available-skills') {
        document.getElementById('available-skills-tab').style.display = 'block';
        loadAvailableSkills();
    } else if (tabName === 'equipped-skills') {
        document.getElementById('equipped-skills-tab').style.display = 'block';
        loadEquippedSkills();
    }
}

// 加载我的技能列表
async function loadMySkills() {
    showLoading();
    try {
        const response = await gameAPI.getPlayerSkills();
        hideLoading();
        
        if (response.success) {
            mySkills = response.data || [];
            renderMySkills();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('加载我的技能失败', 'error');
        console.error(error);
    }
}

// 渲染我的技能列表
function renderMySkills() {
    const container = document.getElementById('mySkillsList');
    
    if (mySkills.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">你还没有技能，去学习一些吧！</p>';
        return;
    }
    
    container.innerHTML = mySkills.map(skill => `
        <div class="skill-card rarity-${skill.rarity || 1}">
            ${skill.isEquipped ? '<div class="equipped-badge">已装备</div>' : ''}
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">等级 ${skill.level}</p>
            
            <div class="skill-stats">
                <div class="stat-item">⚔️ 伤害: ${skill.damage || 0}</div>
                <div class="stat-item">⚡ 消耗: ${skill.manaCost || 0}</div>
                <div class="stat-item">⏱️ 冷却: ${skill.cooldown || 0}s</div>
            </div>
            
            <div style="margin-top: 10px;">
                <div style="display: flex; justify-content: space-between; font-size: 12px;">
                    <span>经验</span>
                    <span>${skill.experience}/${skill.expToNext}</span>
                </div>
                <div class="exp-bar">
                    <div class="exp-fill" style="width: ${(skill.experience / skill.expToNext * 100)}%"></div>
                </div>
            </div>
            
            <div style="margin-top: 8px; font-size: 12px; color: #666;">
                <p>${escapeHtml(skill.description || '暂无描述')}</p>
            </div>
            
            <div class="action-buttons" style="margin-top: 10px;">
                ${!skill.isEquipped ? `<button class="btn btn-primary btn-sm" onclick="equipSkill(${skill.id})">装备</button>` : ''}
                <button class="btn btn-success btn-sm" onclick="upgradeSkill(${skill.id})">升级</button>
            </div>
        </div>
    `).join('');
}

// 加载可学习的技能
async function loadAvailableSkills() {
    showLoading();
    try {
        const response = await gameAPI.getAvailableSkills();
        hideLoading();
        
        if (response.success) {
            availableSkills = response.data || [];
            renderAvailableSkills();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('加载可学习技能失败', 'error');
        console.error(error);
    }
}

// 渲染可学习的技能
function renderAvailableSkills() {
    const container = document.getElementById('availableSkillsList');
    
    if (availableSkills.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">暂无可学习的技能</p>';
        return;
    }
    
    container.innerHTML = availableSkills.map(skill => `
        <div class="skill-card rarity-${skill.rarity}">
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.name)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">${escapeHtml(getRarityName(skill.rarity))}</p>
            <p style="text-align: center; font-size: 12px; color: #666; margin: 5px 0;">${escapeHtml(skill.type)}</p>
            
            <div class="skill-stats">
                <div class="stat-item">⚔️ 伤害: ${skill.baseDamage}</div>
                <div class="stat-item">⚡ 消耗: ${skill.baseManaCost}</div>
                <div class="stat-item">⏱️ 冷却: ${skill.baseCooldown}s</div>
            </div>
            
            <p style="text-align: center; margin-top: 10px; font-size: 12px; color: #666;">
                解锁等级: ${skill.unlockLevel}
            </p>
            
            <p style="text-align: center; margin-top: 5px; font-size: 12px; color: #666;">
                消耗灵石: ${skill.cost || 0}
            </p>
            
            <button class="btn btn-primary" style="width: 100%; margin-top: 10px;" onclick="learnSkill(${skill.id})">
                📚 学习
            </button>
        </div>
    `).join('');
}

// 加载已装备的技能
async function loadEquippedSkills() {
    showLoading();
    try {
        const response = await gameAPI.getEquippedSkills();
        hideLoading();
        
        if (response.success) {
            equippedSkills = response.data || [];
            renderEquippedSkills();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('加载已装备技能失败', 'error');
        console.error(error);
    }
}

// 渲染已装备的技能
function renderEquippedSkills() {
    const container = document.getElementById('equippedSkillsList');
    
    if (equippedSkills.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">还没有装备任何技能</p>';
        return;
    }
    
    container.innerHTML = equippedSkills.map(skill => `
        <div class="skill-card equipped rarity-${skill.rarity || 1}">
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">等级 ${skill.level}</p>
            
            <div class="skill-stats">
                <div class="stat-item">⚔️ 伤害: ${skill.damage || 0}</div>
                <div class="stat-item">⚡ 消耗: ${skill.manaCost || 0}</div>
                <div class="stat-item">⏱️ 冷却: ${skill.cooldown || 0}s</div>
            </div>
            
            <div class="action-buttons" style="margin-top: 10px;">
                <button class="btn btn-secondary btn-sm" onclick="unequipSkill(${skill.id})">卸下</button>
            </div>
        </div>
    `).join('');
}

// 学习技能
async function learnSkill(skillId) {
    showLoading();
    try {
        const response = await gameAPI.learnSkill(skillId);
        hideLoading();
        
        if (response.success) {
            showToast('📚 技能学习成功！', 'success');
            loadMySkills();
            loadAvailableSkills();
            loadPlayerInfo();
        } else {
            showToast(response.message || '学习失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('学习失败: ' + (error.message || '未知错误'), 'error');
    }
}

// 装备技能
async function equipSkill(playerSkillId) {
    showLoading();
    try {
        // 获取装备槽位（默认第1个空闲槽位）
        const slotNumber = 1;
        const response = await gameAPI.equipSkill(playerSkillId, slotNumber);
        hideLoading();
        
        if (response.success) {
            showToast('⚔️ 技能装备成功！', 'success');
            loadMySkills();
            loadEquippedSkills();
        } else {
            showToast(response.message || '装备失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('装备失败', 'error');
    }
}

// 卸下技能
async function unequipSkill(playerSkillId) {
    showLoading();
    try {
        const response = await gameAPI.unequipSkill(playerSkillId);
        hideLoading();
        
        if (response.success) {
            showToast('已卸下技能', 'success');
            loadMySkills();
            loadEquippedSkills();
        } else {
            showToast(response.message || '卸下失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('卸下失败', 'error');
    }
}

// 升级技能
async function upgradeSkill(playerSkillId) {
    showLoading();
    try {
        const response = await gameAPI.upgradeSkill(playerSkillId);
        hideLoading();
        
        if (response.success) {
            showToast('💪 技能升级成功！', 'success');
            loadMySkills();
            loadEquippedSkills();
            loadPlayerInfo();
        } else {
            showToast(response.message || '升级失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('升级失败', 'error');
    }
}

// 工具函数：获取技能图标
function getSkillIcon(skillType) {
    const icons = {
        'ATTACK': '⚔️',
        'DEFENSE': '🛡️',
        'HEAL': '❤️',
        'BUFF': '✨',
        'DEBUFF': '💀',
        'SPECIAL': '🌟'
    };
    return icons[skillType] || '🔮';
}

// 工具函数：获取稀有度名称
function getRarityName(rarity) {
    const names = {
        1: '普通',
        2: '稀有',
        3: '史诗',
        4: '传说',
        5: '神话'
    };
    return names[rarity] || '未知';
}

// 显示加载动画
function showLoading() {
    document.getElementById('loading').classList.remove('hidden');
}

// 隐藏加载动画
function hideLoading() {
    document.getElementById('loading').classList.add('hidden');
}

// 显示提示消息
function showToast(message, type = 'info') {
    if (window.authManager && window.authManager.showToast) {
        window.authManager.showToast(message, type);
    } else {
        alert(message);
    }
}
