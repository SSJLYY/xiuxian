// 鎶€鑳界郴缁烰avaScript

var currentTab = 'my-skills';
var mySkills = [];
var availableSkills = [];
var equippedSkills = [];

// 椤甸潰鍔犺浇鏃跺垵濮嬪寲
document.addEventListener('DOMContentLoaded', function() {
    loadPlayerInfo();
    loadMySkills();
    loadAvailableSkills();
    loadEquippedSkills();
});

// 鏆撮湶缁?modules.js 璋冪敤
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

// 鍔犺浇鐜╁淇℃伅
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
        console.error('鍔犺浇鐜╁淇℃伅澶辫触:', error);
    }
}

// 切换标签页
function showSkillTab(tabName, triggerElement = null) {
    currentTab = tabName;
    
    // 更新按钮状态
    const buttons = document.querySelectorAll('.skill-tab-btn');
    buttons.forEach(btn => btn.classList.remove('active'));
    const activeButton = triggerElement
        || (typeof event !== 'undefined' ? event.target?.closest?.('.skill-tab-btn') : null)
        || Array.from(buttons).find(btn => btn.dataset.tab === tabName || btn.getAttribute('onclick')?.includes(`'${tabName}'`));
    activeButton?.classList.add('active');
    
    // 闅愯棌鎵€鏈夋爣绛鹃〉
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
            mySkills = (response.data || []).map(normalizePlayerSkill);
            renderMySkills();
        } else {
            showToast('鍔犺浇澶辫触: ' + response.message, 'error');
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
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">浣犺繕娌℃湁鎶€鑳斤紝鍘诲涔犱竴浜涘惂锛?/p>';
        return;
    }
    
    container.innerHTML = mySkills.map(skill => `
        <div class="skill-card rarity-${skill.rarity || 1}">
            ${skill.isEquipped ? '<div class="equipped-badge">宸茶澶?/div>' : ''}
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">绛夌骇 ${skill.level}</p>
            <p style="text-align: center; font-size: 12px; color: #666;">槽位 ${Number(skill.slotNumber || 0) + 1}</p>
            
            <div class="skill-stats">
                <div class="stat-item">鈿旓笍 浼ゅ: ${skill.damage || 0}</div>
                <div class="stat-item">鈿?娑堣€? ${skill.manaCost || 0}</div>
                <div class="stat-item">鈴憋笍 鍐峰嵈: ${skill.cooldown || 0}s</div>
            </div>
            
            <div style="margin-top: 10px;">
                <div style="display: flex; justify-content: space-between; font-size: 12px;">
                    <span>缁忛獙</span>
                    <span>${skill.experience}/${skill.expToNext}</span>
                </div>
                <div class="exp-bar">
                    <div class="exp-fill" style="width: ${(skill.experience / skill.expToNext * 100)}%"></div>
                </div>
            </div>
            
            <div style="margin-top: 8px; font-size: 12px; color: #666;">
                <p>${escapeHtml(skill.description || '鏆傛棤鎻忚堪')}</p>
            </div>
            
            <div class="action-buttons" style="margin-top: 10px;">
                ${!skill.isEquipped ? `<button class="btn btn-primary btn-sm" onclick="equipSkill(${skill.playerSkillId})">瑁呭</button>` : ''}
                <button class="btn btn-success btn-sm" onclick="upgradeSkill(${skill.playerSkillId})">鍗囩骇</button>
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
            availableSkills = (response.data || []).map(normalizeAvailableSkill);
            renderAvailableSkills();
        } else {
            showToast('鍔犺浇澶辫触: ' + response.message, 'error');
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
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">鏆傛棤鍙涔犵殑鎶€鑳?/p>';
        return;
    }
    
    container.innerHTML = availableSkills.map(skill => `
        <div class="skill-card rarity-${skill.rarity}">
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">${escapeHtml(getRarityName(skill.rarity))}</p>
            <p style="text-align: center; font-size: 12px; color: #666; margin: 5px 0;">${escapeHtml(skill.type)}</p>
            
            <div class="skill-stats">
                <div class="stat-item">鈿旓笍 浼ゅ: ${skill.baseDamage}</div>
                <div class="stat-item">鈿?娑堣€? ${skill.baseManaCost}</div>
                <div class="stat-item">鈴憋笍 鍐峰嵈: ${skill.baseCooldown}s</div>
            </div>
            
            <p style="text-align: center; margin-top: 10px; font-size: 12px; color: #666;">
                瑙ｉ攣绛夌骇: ${skill.unlockLevel}
            </p>
            
            <p style="text-align: center; margin-top: 5px; font-size: 12px; color: #666;">
                娑堣€楃伒鐭? ${skill.cost || 0}
            </p>
            
            <button class="btn btn-primary" style="width: 100%; margin-top: 10px;" onclick="learnSkill(${skill.id})">
                馃摎 瀛︿範
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
            equippedSkills = (response.data || []).map(normalizePlayerSkill);
            renderEquippedSkills();
        } else {
            showToast('鍔犺浇澶辫触: ' + response.message, 'error');
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
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">杩樻病鏈夎澶囦换浣曟妧鑳?/p>';
        return;
    }
    
    container.innerHTML = equippedSkills.map(skill => `
        <div class="skill-card equipped rarity-${skill.rarity || 1}">
            <div class="skill-icon">${getSkillIcon(skill.skillType)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(skill.skillName)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">绛夌骇 ${skill.level}</p>
            <p style="text-align: center; font-size: 12px; color: #666;">槽位 ${Number(skill.slotNumber || 0) + 1}</p>
            
            <div class="skill-stats">
                <div class="stat-item">鈿旓笍 浼ゅ: ${skill.damage || 0}</div>
                <div class="stat-item">鈿?娑堣€? ${skill.manaCost || 0}</div>
                <div class="stat-item">鈴憋笍 鍐峰嵈: ${skill.cooldown || 0}s</div>
            </div>
            
            <div class="action-buttons" style="margin-top: 10px;">
                <button class="btn btn-secondary btn-sm" onclick="unequipSkill(${skill.playerSkillId})">鍗镐笅</button>
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
            showToast('馃摎 鎶€鑳藉涔犳垚鍔燂紒', 'success');
            loadMySkills();
            loadAvailableSkills();
            loadPlayerInfo();
        } else {
            showToast(response.message || '瀛︿範澶辫触', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('瀛︿範澶辫触: ' + (error.message || '鏈煡閿欒'), 'error');
    }
}

// 装备技能
async function equipSkill(playerSkillId) {
    showLoading();
    try {
        const equippedResponse = await gameAPI.getEquippedSkills();
        if (!equippedResponse?.success) {
            hideLoading();
            showToast(equippedResponse?.message || '无法获取当前装备技能', 'error');
            return;
        }

        equippedSkills = (equippedResponse.data || []).map(normalizePlayerSkill);
        const usedSlots = new Set(
            equippedSkills
                .map(skill => Number(skill.slotNumber))
                .filter(slot => Number.isInteger(slot) && slot >= 0)
        );
        const slotNumber = [0, 1, 2].find(slot => !usedSlots.has(slot));
        if (slotNumber === undefined) {
            hideLoading();
            showToast('已达到3个装备上限，请先卸下一个技能', 'error');
            return;
        }

        const response = await gameAPI.equipSkill(playerSkillId, slotNumber);
        hideLoading();
        
        if (response.success) {
            showToast('鈿旓笍 鎶€鑳借澶囨垚鍔燂紒', 'success');
            loadMySkills();
            loadEquippedSkills();
        } else {
            showToast(response.message || '瑁呭澶辫触', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('瑁呭澶辫触', 'error');
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
            showToast(response.message || '鍗镐笅澶辫触', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('鍗镐笅澶辫触', 'error');
    }
}

// 升级技能
async function upgradeSkill(playerSkillId) {
    showLoading();
    try {
        const response = await gameAPI.upgradeSkill(playerSkillId);
        hideLoading();
        
        if (response.success) {
            showToast('馃挭 鎶€鑳藉崌绾ф垚鍔燂紒', 'success');
            loadMySkills();
            loadEquippedSkills();
            loadPlayerInfo();
        } else {
            showToast(response.message || '鍗囩骇澶辫触', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('鍗囩骇澶辫触', 'error');
    }
}

// 工具函数：获取技能图标
function getSkillIcon(skillType) {
    const icons = {
        'ATTACK': '鈿旓笍',
        'DEFENSE': '[DEF]',
        'HEAL': '鉂わ笍',
        'BUFF': '[BUFF]',
        'DEBUFF': '馃拃',
        'SPECIAL': '馃専'
    };
    return icons[skillType] || '[SKILL]';
}

// 工具函数：获取稀有度名称
function getRarityName(rarity) {
    const names = {
        1: '普通',
        2: '稀有',
        3: '鍙茶瘲',
        4: '传说',
        5: '神话'
    };
    return names[rarity] || '鏈煡';
}

// 鏄剧ず鍔犺浇鍔ㄧ敾
function showLoading() {
    document.getElementById('loading').classList.remove('hidden');
}

// 闅愯棌鍔犺浇鍔ㄧ敾
function hideLoading() {
    document.getElementById('loading').classList.add('hidden');
}

// 鏄剧ず鎻愮ず娑堟伅
function showToast(message, type = 'info') {
    if (window.authManager && window.authManager.showToast) {
        window.authManager.showToast(message, type);
    } else {
        alert(message);
    }
}









