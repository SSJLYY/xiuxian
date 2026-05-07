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

// 暴露�?modules.js 调用
window.loadMySkills = loadMySkills;
window.loadAvailableSkills = loadAvailableSkills;
window.loadEquippedSkills = loadEquippedSkills;

function normalizePlayerSkill(skill) {
    const summary = skill?.skill || {};
    const experience = Number(skill?.experience || 0);
    const expToNext = Number(skill?.expToNext || 0);
    return {
        ...skill,
        playerSkillId: skill?.playerSkillId ?? skill?.id,
        skillId: summary?.id ?? skill?.skillId ?? null,
        skillName: summary?.name || skill?.skillName || skill?.name || 'Unknown Skill',
        description: summary?.description || skill?.description || '',
        skillType: summary?.type || skill?.skillType || skill?.type || 'SPECIAL',
        isEquipped: skill?.isEquipped ?? skill?.equipped ?? false,
        rarity: skill?.rarity || 1,
        damage: skill?.damage ?? skill?.baseDamage ?? 0,
        manaCost: skill?.manaCost ?? skill?.baseManaCost ?? 0,
        cooldown: skill?.cooldown ?? skill?.baseCooldown ?? 0,
        experience,
        expToNext: expToNext > 0 ? expToNext : Math.max(experience, 1)
    };
}

function normalizeAvailableSkill(skill) {
    return {
        ...skill,
        skillName: skill?.skillName || skill?.name || 'Unknown Skill',
        skillType: skill?.skillType || skill?.type || 'SPECIAL',
        rarity: skill?.rarity || 1,
        baseDamage: skill?.baseDamage ?? skill?.damage ?? 0,
        baseManaCost: skill?.baseManaCost ?? skill?.manaCost ?? 0,
        baseCooldown: skill?.baseCooldown ?? skill?.cooldown ?? 0,
        cost: skill?.cost ?? skill?.requiredSpiritStones ?? 0
    };
}

// 加载玩家信息
async function loadPlayerInfo() {
    try {
        const response = await gameAPI.getCurrentPlayerProfile();
        if (response.success) {
            const player = response.data;
            document.getElementById('playerName').textContent = player.nickname || 'xiuxian player';
            document.getElementById('playerLevel').textContent = player.level || 1;
            document.getElementById('playerSpiritStones').textContent = player.spiritStones || 0;
            document.getElementById('playerSkillPoints').textContent = player.skillPoints || 0;
        }
    } catch (error) {
        console.error('加载玩家信息失败:', error);
    }
}

// �л���ǩҳ
function showSkillTab(tabName, triggerElement = null) {
    currentTab = tabName;
    
    // ���°�ť״̬
    const buttons = document.querySelectorAll('.skill-tab-btn');
    buttons.forEach(btn => btn.classList.remove('active'));
    const activeButton = triggerElement
        || (typeof event !== 'undefined' ? event.target?.closest?.('.skill-tab-btn') : null)
        || Array.from(buttons).find(btn => btn.dataset.tab === tabName || btn.getAttribute('onclick')?.includes(`'${tabName}'`));
    activeButton?.classList.add('active');
    
    // 隐藏所有标签页
    document.querySelectorAll('.skill-tab-content').forEach(tab => tab.style.display = 'none');
    
    // ��ʾ��Ӧ��ǩҳ
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

// �����ҵļ����б�
async function loadMySkills() {
    showLoading();
    try {
        const response = await gameAPI.getPlayerSkills();
        hideLoading();
        
        if (response.success) {
            mySkills = (response.data || []).map(normalizePlayerSkill);
            renderMySkills();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('�����ҵļ���ʧ��', 'error');
        console.error(error);
    }
}

// ��Ⱦ�ҵļ����б�
function renderMySkills() {
    const container = document.getElementById('mySkillsList');
    
    if (mySkills.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">你还没有技能，去学习一些吧�?/p>';
        return;
    }
    
    container.innerHTML = mySkills.map(skill => `
        <div class="skill-card rarity-${skill.rarity || 1}">
            ${skill.isEquipped ? '<div class="equipped-badge">已装�?/div>' : ''}
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">等级 ${skill.level}</p>
            
            <div class="skill-stats">
                <div class="stat-item">⚔️ 伤害: ${skill.damage || 0}</div>
                <div class="stat-item">�?消�? ${skill.manaCost || 0}</div>
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
                ${!skill.isEquipped ? `<button class="btn btn-primary btn-sm" onclick="equipSkill(${skill.playerSkillId})">装备</button>` : ''}
                <button class="btn btn-success btn-sm" onclick="upgradeSkill(${skill.playerSkillId})">升级</button>
            </div>
        </div>
    `).join('');
}

// ���ؿ�ѧϰ�ļ���
async function loadAvailableSkills() {
    showLoading();
    try {
        const response = await gameAPI.getAvailableSkills();
        hideLoading();
        
        if (response.success) {
            availableSkills = (response.data || []).map(normalizeAvailableSkill);
            renderAvailableSkills();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('���ؿ�ѧϰ����ʧ��', 'error');
        console.error(error);
    }
}

// ��Ⱦ��ѧϰ�ļ���
function renderAvailableSkills() {
    const container = document.getElementById('availableSkillsList');
    
    if (availableSkills.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">暂无可学习的技�?/p>';
        return;
    }
    
    container.innerHTML = availableSkills.map(skill => `
        <div class="skill-card rarity-${skill.rarity}">
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">${escapeHtml(getRarityName(skill.rarity))}</p>
            <p style="text-align: center; font-size: 12px; color: #666; margin: 5px 0;">${escapeHtml(skill.type)}</p>
            
            <div class="skill-stats">
                <div class="stat-item">⚔️ 伤害: ${skill.baseDamage}</div>
                <div class="stat-item">�?消�? ${skill.baseManaCost}</div>
                <div class="stat-item">⏱️ 冷却: ${skill.baseCooldown}s</div>
            </div>
            
            <p style="text-align: center; margin-top: 10px; font-size: 12px; color: #666;">
                解锁等级: ${skill.unlockLevel}
            </p>
            
            <p style="text-align: center; margin-top: 5px; font-size: 12px; color: #666;">
                消耗灵�? ${skill.cost || 0}
            </p>
            
            <button class="btn btn-primary" style="width: 100%; margin-top: 10px;" onclick="learnSkill(${skill.id})">
                📚 学习
            </button>
        </div>
    `).join('');
}

// ������װ���ļ���
async function loadEquippedSkills() {
    showLoading();
    try {
        const response = await gameAPI.getEquippedSkills();
        hideLoading();
        
        if (response.success) {
            equippedSkills = (response.data || []).map(normalizePlayerSkill);
            renderEquippedSkills();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('������װ������ʧ��', 'error');
        console.error(error);
    }
}

// ��Ⱦ��װ���ļ���
function renderEquippedSkills() {
    const container = document.getElementById('equippedSkillsList');
    
    if (equippedSkills.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">还没有装备任何技�?/p>';
        return;
    }
    
    container.innerHTML = equippedSkills.map(skill => `
        <div class="skill-card equipped rarity-${skill.rarity || 1}">
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">等级 ${skill.level}</p>
            
            <div class="skill-stats">
                <div class="stat-item">⚔️ 伤害: ${skill.damage || 0}</div>
                <div class="stat-item">�?消�? ${skill.manaCost || 0}</div>
                <div class="stat-item">⏱️ 冷却: ${skill.cooldown || 0}s</div>
            </div>
            
            <div class="action-buttons" style="margin-top: 10px;">
                <button class="btn btn-secondary btn-sm" onclick="unequipSkill(${skill.playerSkillId})">卸下</button>
            </div>
        </div>
    `).join('');
}

// ѧϰ����
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

// װ������
async function equipSkill(playerSkillId) {
    showLoading();
    try {
        // 获取装备槽位（默认第1个空闲槽位）
        const slotNumber = 0;
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

// ж�¼���
async function unequipSkill(playerSkillId) {
    showLoading();
    try {
        const response = await gameAPI.unequipSkill(playerSkillId);
        hideLoading();
        
        if (response.success) {
            showToast('��ж�¼���', 'success');
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

// ��������
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

// ���ߺ�������ȡ����ͼ��
function getSkillIcon(skillType) {
    const icons = {
        'ATTACK': '⚔️',
        'DEFENSE': '[DEF]',
        'HEAL': '❤️',
        'BUFF': '[BUFF]',
        'DEBUFF': '💀',
        'SPECIAL': '🌟'
    };
    return icons[skillType] || '[SKILL]';
}

// ���ߺ�������ȡϡ�ж�����
function getRarityName(rarity) {
    const names = {
        1: '��ͨ',
        2: 'ϡ��',
        3: '史诗',
        4: '��˵',
        5: '��'
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







