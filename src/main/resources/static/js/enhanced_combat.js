// 增强战斗JavaScript逻辑

// 当前战斗状态
let currentMonster = null;
let playerSkills = [];
let playerItems = [];
let battleInProgress = false;

// 生成怪物
async function generateMonster(mapId = 1) {
    showLoading(true);
    
    try {
        const response = await api.get(`/combat/generate-monster?mapId=${mapId}`);
        if (response.success) {
            currentMonster = response.data;
            updateMonsterDisplay();
            addToBattleLog(`出现了一只${currentMonster.name}！`);
        } else {
            showToast('生成怪物失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('生成怪物失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 更新怪物显示
function updateMonsterDisplay() {
    if (!currentMonster) return;
    
    document.getElementById('monsterName').textContent = currentMonster.name;
    document.getElementById('monsterLevel').textContent = currentMonster.level;
    document.getElementById('monsterType').textContent = currentMonster.type;
    document.getElementById('monsterHealth').textContent = currentMonster.health;
    document.getElementById('monsterMaxHealth').textContent = currentMonster.health;
    
    // 更新血条
    updateHealthBar('monster', currentMonster.health, currentMonster.health);
}

// 普通攻击
async function performNormalAttack() {
    if (!currentMonster || battleInProgress) return;
    
    battleInProgress = true;
    
    try {
        const response = await api.post('/combat/enhanced', {
            mapId: 1
        });
        
        if (response.success) {
            displayCombatResult(response.data);
        } else {
            showToast('战斗失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('战斗失败: ' + error.message, 'error');
    } finally {
        battleInProgress = false;
    }
}

// 显示技能选择
async function showSkills() {
    if (battleInProgress) return;
    
    // 加载玩家技能
    await loadPlayerSkills();
    
    const skillsList = document.getElementById('skillsList');
    if (!skillsList) return;
    
    if (playerSkills.length === 0) {
        skillsList.innerHTML = '<p class="text-center py-4 text-gray-500">暂无可用技能</p>';
    } else {
        skillsList.innerHTML = playerSkills.map(skill => `
            <div class="skill-card p-3 border rounded hover:bg-yellow-50 cursor-pointer" 
                 onclick="useSkill(${skill.skillId})">
                <h4 class="font-bold">${skill.skill.name}</h4>
                <p class="text-sm text-gray-600">${skill.skill.description}</p>
                <div class="flex justify-between mt-2 text-sm">
                    <span>等级: ${skill.level}</span>
                    <span>消耗: ${skill.skill.manaCost}法力</span>
                </div>
            </div>
        `).join('');
    }
    
    document.getElementById('skillsModal').style.display = 'block';
}

// 关闭技能模态框
function closeSkillsModal() {
    document.getElementById('skillsModal').style.display = 'none';
}

// 使用技能
async function useSkill(skillId) {
    if (!currentMonster || battleInProgress) return;
    
    closeSkillsModal();
    battleInProgress = true;
    
    try {
        const response = await api.post('/combat/enhanced', {
            mapId: 1,
            skillId: skillId
        });
        
        if (response.success) {
            displayCombatResult(response.data);
        } else {
            showToast('战斗失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('战斗失败: ' + error.message, 'error');
    } finally {
        battleInProgress = false;
    }
}

// 显示道具选择
async function showItems() {
    if (battleInProgress) return;
    
    // 加载玩家道具
    await loadPlayerItems();
    
    const itemsList = document.getElementById('itemsList');
    if (!itemsList) return;
    
    if (playerItems.length === 0) {
        itemsList.innerHTML = '<p class="text-center py-4 text-gray-500">暂无可用道具</p>';
    } else {
        itemsList.innerHTML = playerItems.map(item => {
            const itemTemplate = getItemTemplate(item.itemId);
            return `
                <div class="item-card p-3 border rounded hover:bg-green-50 cursor-pointer" 
                     onclick="useItem(${item.id})">
                    <h4 class="font-bold">${escapeHtml(itemTemplate?.name || '未知道具')}</h4>
                    <p class="text-sm text-gray-600">${escapeHtml(itemTemplate?.description || '')}</p>
                    <div class="flex justify-between mt-2 text-sm">
                        <span>数量: ${item.quantity}</span>
                    </div>
                </div>
            `;
        }).join('');
    }
    
    document.getElementById('itemsModal').style.display = 'block';
}

// 关闭道具模态框
function closeItemsModal() {
    document.getElementById('itemsModal').style.display = 'none';
}

// 使用道具
async function useItem(itemId) {
    if (!currentMonster || battleInProgress) return;
    
    closeItemsModal();
    battleInProgress = true;
    
    try {
        const response = await api.post('/combat/enhanced', {
            mapId: 1,
            itemId: itemId
        });
        
        if (response.success) {
            displayCombatResult(response.data);
        } else {
            showToast('战斗失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('战斗失败: ' + error.message, 'error');
    } finally {
        battleInProgress = false;
    }
}

// 显示战斗结果 - 集成视觉反馈系统
function displayCombatResult(result) {
    // 获取位置信息用于飘字
    const monsterElement = document.getElementById('monsterStats');
    const playerElement = document.getElementById('playerStats');
    
    const monsterPos = monsterElement ? {
        x: monsterElement.getBoundingClientRect().left + monsterElement.getBoundingClientRect().width / 2,
        y: monsterElement.getBoundingClientRect().top + monsterElement.getBoundingClientRect().height / 2
    } : { x: window.innerWidth * 0.7, y: window.innerHeight * 0.3 };
    
    const playerPos = playerElement ? {
        x: playerElement.getBoundingClientRect().left + playerElement.getBoundingClientRect().width / 2,
        y: playerElement.getBoundingClientRect().top + playerElement.getBoundingClientRect().height / 2
    } : { x: window.innerWidth * 0.3, y: window.innerHeight * 0.3 };
    
    // 更新血条
    updateHealthBar('player', result.playerCurrentHealth, result.playerMaxHealth);
    updateHealthBar('monster', result.monsterCurrentHealth, result.monsterMaxHealth);
    
    // 更新数值显示
    document.getElementById('playerHealth').textContent = result.playerCurrentHealth;
    document.getElementById('playerMaxHealth').textContent = result.playerMaxHealth;
    document.getElementById('monsterHealth').textContent = result.monsterCurrentHealth;
    document.getElementById('monsterMaxHealth').textContent = result.monsterMaxHealth;
    
    // 处理战斗日志 - 生成视觉反馈
    result.battleLog.forEach((logEntry, index) => {
        // 添加到日志显示
        addToBattleLog(logEntry);
        
        // 延迟生成视觉反馈，营造逐步效果
        setTimeout(() => {
            processLogAndVisualize(logEntry, result, playerPos, monsterPos);
        }, index * 150);
    });
    
    // 检查战斗结果
    if (result.result === 'WIN') {
        showToast('战斗胜利！', 'success');
        // 重新生成怪物
        setTimeout(() => {
            generateMonster();
        }, 2000);
    } else if (result.result === 'LOSE') {
        showToast('战斗失败！', 'error');
    }
}

// 解析日志并生成视觉效果
function processLogAndVisualize(logEntry, result, playerPos, monsterPos) {
    if (!window.combatVisualFeedback) return;
    
    // 暴击检测
    if (logEntry.includes('暴击')) {
        const damageMatch = logEntry.match(/(\d+)/);
        const damage = damageMatch ? parseInt(damageMatch[1]) : 0;
        const isPlayerAttack = logEntry.includes('玩家造成暴击伤害');
        
        const pos = isPlayerAttack ? monsterPos : playerPos;
        window.combatVisualFeedback.showDamageFloat(damage, pos, {
            type: 'damage',
            isCritical: true,
            source: isPlayerAttack ? 'player' : 'monster'
        });
        window.combatVisualFeedback.showCriticalIndicator(pos);
        
    } else if (logEntry.includes('造成伤害') || logEntry.includes('伤害')) {
        const damageMatch = logEntry.match(/(\d+)/);
        const damage = damageMatch ? parseInt(damageMatch[1]) : 0;
        const isPlayerAttack = logEntry.includes('玩家');
        
        const pos = isPlayerAttack ? monsterPos : playerPos;
        window.combatVisualFeedback.showDamageFloat(damage, pos, {
            type: 'damage',
            isCritical: false,
            source: isPlayerAttack ? 'player' : 'monster'
        });
        
        // 触发血条伤害闪烁
        const healthBar = document.getElementById(isPlayerAttack ? 'monsterHealthBar' : 'playerHealthBar');
        if (healthBar) {
            window.combatVisualFeedback.showHealthBarDamage(healthBar);
        }
        
    } else if (logEntry.includes('恢复') || logEntry.includes('治疗')) {
        const healMatch = logEntry.match(/(\d+)/);
        const heal = healMatch ? parseInt(healMatch[1]) : 0;
        const isPlayerHeal = logEntry.includes('玩家');
        
        const pos = isPlayerHeal ? playerPos : monsterPos;
        window.combatVisualFeedback.showDamageFloat(heal, pos, {
            type: 'heal'
        });
        
        // 触发治疗脉冲
        const healthBar = document.getElementById(isPlayerHeal ? 'playerHealthBar' : 'monsterHealthBar');
        if (healthBar) {
            window.combatVisualFeedback.showHealPulse(healthBar);
        }
        
    } else if (logEntry.includes('闪避')) {
        const isPlayerDodge = logEntry.includes('玩家');
        window.combatVisualFeedback.showDamageFloat(0, isPlayerDodge ? playerPos : monsterPos, {
            type: 'dodge'
        });
    }
}

// 更新血条
function updateHealthBar(character, current, max) {
    const percentage = (current / max) * 100;
    const healthBar = document.getElementById(`${character}HealthBar`);
    if (healthBar) {
        healthBar.style.width = `${percentage}%`;
        
        // 根据血量改变颜色
        if (percentage > 60) {
            healthBar.style.background = character === 'player' 
                ? 'linear-gradient(90deg, #ef4444, #f87171)' 
                : 'linear-gradient(90deg, #3b82f6, #60a5fa)';
        } else if (percentage > 30) {
            healthBar.style.background = character === 'player'
                ? 'linear-gradient(90deg, #f59e0b, #fbbf24)'
                : 'linear-gradient(90deg, #f59e0b, #fbbf24)';
        } else {
            healthBar.style.background = character === 'player'
                ? 'linear-gradient(90deg, #ef4444, #fecaca)'
                : 'linear-gradient(90deg, #ef4444, #fecaca)';
        }
    }
}

// 添加到战斗日志
function addToBattleLog(message) {
    const battleLog = document.getElementById('battleLog');
    if (!battleLog) return;
    
    const logEntry = document.createElement('div');
    logEntry.className = 'battle-log-entry';
    
    // 检查是否是暴击消息
    if (message.includes('暴击')) {
        logEntry.classList.add('critical');
    }
    
    logEntry.textContent = message;
    battleLog.appendChild(logEntry);
    
    // 滚动到底部
    battleLog.scrollTop = battleLog.scrollHeight;
}

// 加载玩家技能
async function loadPlayerSkills() {
    try {
        const response = await api.get('/skills/my');
        if (response.success) {
            playerSkills = response.data.filter(skill => skill.equipped);
        }
    } catch (error) {
        console.error('加载玩家技能失败:', error);
    }
}

// 加载玩家道具
async function loadPlayerItems() {
    try {
        const response = await api.get('/inventory/items');
        if (response.success) {
            playerItems = response.data;
        }
    } catch (error) {
        console.error('加载玩家道具失败:', error);
    }
}

// 获取道具模板（简化实现）
function getItemTemplate(itemId) {
    // 实际应该从服务器获取道具模板信息
    // 这里简化处理
    return {
        name: `道具${itemId}`,
        description: '战斗中使用的道具'
    };
}

// 显示加载动画
function showLoading(show) {
    const loading = document.getElementById('loading');
    if (loading) {
        if (show) {
            loading.classList.remove('hidden');
        } else {
            loading.classList.add('hidden');
        }
    }
}

// 显示消息提示
function showToast(message, type) {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    toastMessage.textContent = message;
    toast.className = 'toast ' + type;
    
    // 3秒后隐藏
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}