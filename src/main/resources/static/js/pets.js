// 宠物系统JavaScript

let currentTab = 'my-pets';
let myPets = [];
let availablePets = [];
let activePet = null;
let currentPetDetail = null;

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    loadPlayerInfo();
    loadMyPets();
});

// 暴露给 modules.js 调用
window.loadMyPets = loadMyPets;

// 检查登录状态
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }
}

// 加载玩家信息
async function loadPlayerInfo() {
    try {
        const response = await apiRequest('/api/player/profile', 'GET');
        if (response.success) {
            const player = response.data;
            document.getElementById('playerName').textContent = player.nickname || 'xiuxian者';
            document.getElementById('playerLevel').textContent = player.level || 1;
            document.getElementById('playerSpiritStones').textContent = player.spiritStones || 0;
        }
    } catch (error) {
        console.error('加载玩家信息失败:', error);
    }
}

// 切换标签页
function showTab(tabName) {
    currentTab = tabName;
    
    // 更新按钮状态
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    
    // 隐藏所有标签页
    document.querySelectorAll('.tab-content').forEach(tab => tab.style.display = 'none');
    
    // 显示对应标签页
    if (tabName === 'my-pets') {
        document.getElementById('my-pets-tab').style.display = 'block';
        loadMyPets();
    } else if (tabName === 'capture') {
        document.getElementById('capture-tab').style.display = 'block';
        loadAvailablePets();
    } else if (tabName === 'active-pet') {
        document.getElementById('active-pet-tab').style.display = 'block';
        loadActivePet();
    }
}

// 加载我的宠物列表
async function loadMyPets() {
    showLoading();
    try {
        const response = await apiRequest('/api/pets/my', 'GET');
        hideLoading();
        
        if (response.success) {
            myPets = response.data || [];
            renderMyPets();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('加载我的宠物失败', 'error');
        console.error(error);
    }
}

// 渲染我的宠物列表
function renderMyPets() {
    const container = document.getElementById('myPetsList');
    
    if (myPets.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">你还没有宠物，去捕获一只吧！</p>';
        return;
    }
    
    container.innerHTML = myPets.map(pet => `
        <div class="pet-card rarity-${pet.rarity || 1}" onclick="showMyPetDetail(${pet.id})">
            ${pet.isActive ? '<div class="active-badge">出战中</div>' : ''}
            ${pet.isLocked ? '<div class="locked-badge">🔒</div>' : ''}
            <div class="pet-avatar">${getPetEmoji(pet.petId)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(pet.nickname || '未命名')}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">等级 ${pet.level}</p>
            
            <div class="pet-stats">
                <div class="stat-item">⚔️ 攻击: ${pet.attack}</div>
                <div class="stat-item">🛡️ 防御: ${pet.defense}</div>
                <div class="stat-item">❤️ 生命: ${pet.health}/${pet.maxHealth}</div>
                <div class="stat-item">⚡ 速度: ${pet.speed}</div>
            </div>
            
            <div style="margin-top: 10px;">
                <div style="display: flex; justify-content: space-between; font-size: 12px;">
                    <span>忠诚度</span>
                    <span>${pet.loyalty}/100</span>
                </div>
                <div class="loyalty-bar">
                    <div class="loyalty-fill" style="width: ${pet.loyalty}%"></div>
                </div>
            </div>
            
            <div style="margin-top: 8px;">
                <div style="display: flex; justify-content: space-between; font-size: 12px;">
                    <span>饱食度</span>
                    <span>${pet.hunger}/100</span>
                </div>
                <div class="hunger-bar">
                    <div class="hunger-fill" style="width: ${pet.hunger}%"></div>
                </div>
            </div>
            
            <p style="text-align: center; margin-top: 10px; font-size: 12px; color: #666;">
                战绩: ${pet.totalWins}胜/${pet.totalBattles}战
            </p>
        </div>
    `).join('');
}

// 加载可捕获的宠物
async function loadAvailablePets() {
    showLoading();
    try {
        const response = await apiRequest('/api/pets/available', 'GET');
        hideLoading();
        
        if (response.success) {
            availablePets = response.data || [];
            renderAvailablePets();
        } else {
            showToast('加载失败: ' + response.message, 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('加载可捕获宠物失败', 'error');
        console.error(error);
    }
}

// 渲染可捕获的宠物
function renderAvailablePets() {
    const container = document.getElementById('availablePetsList');
    
    if (availablePets.length === 0) {
        container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 40px;">暂无可捕获的宠物</p>';
        return;
    }
    
    container.innerHTML = availablePets.map(pet => `
        <div class="pet-card rarity-${pet.rarity}" onclick="showCaptureDetail(${pet.id})">
            <div class="pet-avatar">${getPetEmoji(pet.id)}</div>
            <h4 style="text-align: center; margin: 10px 0;">${escapeHtml(pet.name)}</h4>
            <p style="text-align: center; font-size: 12px; color: #666;">${escapeHtml(getRarityName(pet.rarity))}</p>
            <p style="text-align: center; font-size: 12px; color: #666; margin: 5px 0;">${escapeHtml(pet.type)}</p>
            
            <div class="pet-stats">
                <div class="stat-item">⚔️ 攻击: ${pet.baseAttack}</div>
                <div class="stat-item">🛡️ 防御: ${pet.baseDefense}</div>
                <div class="stat-item">❤️ 生命: ${pet.baseHealth}</div>
                <div class="stat-item">⚡ 速度: ${pet.baseSpeed}</div>
            </div>
            
            <p style="text-align: center; margin-top: 10px; font-size: 12px;">
                成长率: ${pet.growthRate}
            </p>
            <p style="text-align: center; margin-top: 5px; font-size: 12px; color: #CD5C5C;">
                捕获率: ${pet.captureRate}%
            </p>
            <p style="text-align: center; margin-top: 5px; font-size: 12px; color: #666;">
                解锁等级: ${pet.unlockLevel}
            </p>
            
            <button class="btn btn-primary" style="width: 100%; margin-top: 10px;" onclick="event.stopPropagation(); capturePet(${pet.id})">
                🎯 捕获
            </button>
        </div>
    `).join('');
}

// 加载出战宠物
async function loadActivePet() {
    showLoading();
    try {
        const response = await apiRequest('/api/pets/active', 'GET');
        hideLoading();
        
        if (response.success && response.data) {
            activePet = response.data;
            renderActivePet();
        } else {
            document.getElementById('activePetDetail').innerHTML = '<p style="text-align: center; padding: 40px;">暂无出战宠物</p>';
        }
    } catch (error) {
        hideLoading();
        document.getElementById('activePetDetail').innerHTML = '<p style="text-align: center; padding: 40px;">暂无出战宠物</p>';
    }
}

// 渲染出战宠物
function renderActivePet() {
    const container = document.getElementById('activePetDetail');
    
    container.innerHTML = `
        <div class="pet-card rarity-${activePet.rarity || 1}" style="max-width: 100%;">
            <div class="active-badge">出战中</div>
            <div class="pet-avatar" style="width: 120px; height: 120px; font-size: 60px;">${getPetEmoji(activePet.petId)}</div>
            <h3 style="text-align: center; margin: 15px 0;">${escapeHtml(activePet.nickname || '未命名')}</h3>
            <p style="text-align: center; font-size: 16px; color: #666;">等级 ${activePet.level}</p>
            
            <div style="margin: 20px 0;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <span>经验值</span>
                    <span>${activePet.exp}/${activePet.expToNext}</span>
                </div>
                <div class="loyalty-bar">
                    <div class="loyalty-fill" style="width: ${(activePet.exp / activePet.expToNext * 100)}%"></div>
                </div>
            </div>
            
            <div class="pet-stats" style="grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="stat-item" style="padding: 10px;">⚔️ 攻击: ${activePet.attack}</div>
                <div class="stat-item" style="padding: 10px;">🛡️ 防御: ${activePet.defense}</div>
                <div class="stat-item" style="padding: 10px;">❤️ 生命: ${activePet.health}/${activePet.maxHealth}</div>
                <div class="stat-item" style="padding: 10px;">⚡ 速度: ${activePet.speed}</div>
            </div>
            
            <div style="margin-top: 15px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <span>忠诚度 (${getLoyaltyStatus(activePet.loyalty)})</span>
                    <span>${activePet.loyalty}/100</span>
                </div>
                <div class="loyalty-bar">
                    <div class="loyalty-fill" style="width: ${activePet.loyalty}%"></div>
                </div>
            </div>
            
            <div style="margin-top: 10px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <span>饱食度 ${activePet.hunger < 30 ? '(需要喂食)' : ''}</span>
                    <span>${activePet.hunger}/100</span>
                </div>
                <div class="hunger-bar">
                    <div class="hunger-fill" style="width: ${activePet.hunger}%"></div>
                </div>
            </div>
            
            <div style="margin-top: 15px; text-align: center;">
                <p style="font-size: 14px; color: #666;">战绩: ${activePet.totalWins}胜 / ${activePet.totalBattles}战</p>
                <p style="font-size: 14px; color: #666;">胜率: ${activePet.totalBattles > 0 ? ((activePet.totalWins / activePet.totalBattles * 100).toFixed(1)) : 0}%</p>
            </div>
            
            <div class="action-buttons" style="margin-top: 20px;">
                <button class="btn btn-success" onclick="feedPet(${activePet.id})">🍖 喂食</button>
                <button class="btn btn-primary" onclick="showTrainModal(${activePet.id})">💪 训练</button>
            </div>
        </div>
    `;
}

// 显示我的宠物详情
function showMyPetDetail(petId) {
    const pet = myPets.find(p => p.id === petId);
    if (!pet) return;
    
    currentPetDetail = pet;
    
    const modalContent = document.getElementById('modalPetContent');
    modalContent.innerHTML = `
        <div style="text-align: center;">
            <div class="pet-avatar" style="width: 100px; height: 100px; font-size: 50px; margin: 0 auto;">${getPetEmoji(pet.petId)}</div>
            <h4 style="margin: 15px 0;">等级 ${pet.level}</h4>
        </div>
        
        <div class="pet-stats" style="margin: 15px 0;">
            <div class="stat-item">⚔️ 攻击: ${pet.attack}</div>
            <div class="stat-item">🛡️ 防御: ${pet.defense}</div>
            <div class="stat-item">❤️ 生命: ${pet.health}/${pet.maxHealth}</div>
            <div class="stat-item">⚡ 速度: ${pet.speed}</div>
        </div>
        
        <div style="margin: 15px 0;">
            <p style="font-size: 14px;">忠诚度: ${pet.loyalty}/100 (${getLoyaltyStatus(pet.loyalty)})</p>
            <div class="loyalty-bar"><div class="loyalty-fill" style="width: ${pet.loyalty}%"></div></div>
        </div>
        
        <div style="margin: 15px 0;">
            <p style="font-size: 14px;">饱食度: ${pet.hunger}/100 ${pet.hunger < 30 ? '⚠️ 需要喂食' : ''}</p>
            <div class="hunger-bar"><div class="hunger-fill" style="width: ${pet.hunger}%"></div></div>
        </div>
        
        <p style="text-align: center; margin: 10px 0;">战绩: ${pet.totalWins}胜/${pet.totalBattles}战</p>
    `;
    
    const modalActions = document.getElementById('modalActions');
    modalActions.innerHTML = `
        ${!pet.isActive ? `<button class="btn btn-primary" onclick="setActivePet(${pet.id})">⚔️ 设为出战</button>` : '<button class="btn btn-secondary" disabled>已出战</button>'}
        <button class="btn btn-success" onclick="feedPet(${pet.id})">🍖 喂食</button>
        <button class="btn btn-primary" onclick="showTrainModal(${pet.id})">💪 训练</button>
        <button class="btn btn-secondary" onclick="renamePet(${pet.id})">✏️ 重命名</button>
        <button class="btn btn-secondary" onclick="toggleLockPet(${pet.id})">${pet.isLocked ? '🔓 解锁' : '🔒 锁定'}</button>
        ${!pet.isActive && !pet.isLocked ? `<button class="btn btn-danger" onclick="releasePet(${pet.id})">🗑️ 释放</button>` : ''}
    `;
    
    document.getElementById('modalPetName').textContent = pet.nickname || '宠物详情';
    document.getElementById('petModal').classList.add('show');
}

// 显示捕获详情
function showCaptureDetail(petId) {
    const pet = availablePets.find(p => p.id === petId);
    if (!pet) return;
    
    const modalContent = document.getElementById('modalPetContent');
    modalContent.innerHTML = `
        <div style="text-align: center;">
            <div class="pet-avatar" style="width: 100px; height: 100px; font-size: 50px; margin: 0 auto;">${getPetEmoji(pet.id)}</div>
            <p style="margin: 10px 0; color: #666;">${escapeHtml(getRarityName(pet.rarity))} - ${escapeHtml(pet.type)}</p>
        </div>
        
        <div style="margin: 15px 0;">
            <p style="font-size: 14px; line-height: 1.6;">${escapeHtml(pet.description || '暂无描述')}</p>
        </div>
        
        <div class="pet-stats" style="margin: 15px 0;">
            <div class="stat-item">⚔️ 基础攻击: ${pet.baseAttack}</div>
            <div class="stat-item">🛡️ 基础防御: ${pet.baseDefense}</div>
            <div class="stat-item">❤️ 基础生命: ${pet.baseHealth}</div>
            <div class="stat-item">⚡ 基础速度: ${pet.baseSpeed}</div>
        </div>
        
        <div style="margin: 15px 0; text-align: center;">
            <p style="font-size: 14px;">成长率: <strong>${pet.growthRate}</strong></p>
            <p style="font-size: 14px; color: #CD5C5C;">捕获率: <strong>${pet.captureRate}%</strong></p>
            <p style="font-size: 14px; color: #666;">解锁等级: <strong>${pet.unlockLevel}</strong></p>
        </div>
    `;
    
    const modalActions = document.getElementById('modalActions');
    modalActions.innerHTML = `
        <button class="btn btn-primary" onclick="capturePet(${pet.id})" style="grid-column: 1/-1;">🎯 捕获此宠物</button>
    `;
    
    document.getElementById('modalPetName').textContent = pet.name;
    document.getElementById('petModal').classList.add('show');
}

// 关闭模态框
function closePetModal() {
    document.getElementById('petModal').classList.remove('show');
    currentPetDetail = null;
}

// 捕获宠物
async function capturePet(petId) {
    showLoading();
    try {
        const response = await apiRequest(`/api/pets/capture/${petId}`, 'POST');
        hideLoading();
        
        if (response.success) {
            showToast('🎉 捕获成功！', 'success');
            closePetModal();
            loadMyPets();
            loadPlayerInfo();
        } else {
            showToast(response.message || '捕获失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('捕获失败: ' + (error.message || '未知错误'), 'error');
    }
}

// 设置出战宠物
async function setActivePet(petId) {
    showLoading();
    try {
        const response = await apiRequest(`/api/pets/activate/${petId}`, 'POST');
        hideLoading();
        
        if (response.success) {
            showToast('设置成功！', 'success');
            closePetModal();
            loadMyPets();
        } else {
            showToast(response.message || '设置失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('设置失败', 'error');
    }
}

// 喂食宠物
async function feedPet(petId) {
    showLoading();
    try {
        const response = await apiRequest(`/api/pets/feed/${petId}`, 'POST');
        hideLoading();
        
        if (response.success) {
            showToast('🍖 喂食成功！', 'success');
            closePetModal();
            loadMyPets();
            if (currentTab === 'active-pet') {
                loadActivePet();
            }
        } else {
            showToast(response.message || '喂食失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('喂食失败', 'error');
    }
}

// 显示训练模态框
function showTrainModal(petId) {
    closePetModal();
    
    const modal = document.getElementById('petModal');
    const modalContent = document.getElementById('modalPetContent');
    
    modalContent.innerHTML = `
        <p style="text-align: center; margin-bottom: 20px;">选择训练类型：</p>
    `;
    
    const modalActions = document.getElementById('modalActions');
    modalActions.innerHTML = `
        <button class="btn btn-primary" onclick="trainPet(${petId}, '攻击')">⚔️ 训练攻击</button>
        <button class="btn btn-primary" onclick="trainPet(${petId}, '防御')">🛡️ 训练防御</button>
        <button class="btn btn-primary" onclick="trainPet(${petId}, '速度')" style="grid-column: 1/-1;">⚡ 训练速度</button>
    `;
    
    document.getElementById('modalPetName').textContent = '训练宠物';
    modal.classList.add('show');
}

// 训练宠物
async function trainPet(petId, trainingType) {
    showLoading();
    try {
        const response = await apiRequest(`/api/pets/train/${petId}`, 'POST', { trainingType });
        hideLoading();
        
        if (response.success) {
            showToast(`💪 ${trainingType}训练成功！`, 'success');
            closePetModal();
            loadMyPets();
            if (currentTab === 'active-pet') {
                loadActivePet();
            }
        } else {
            showToast(response.message || '训练失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('训练失败', 'error');
    }
}

// 重命名宠物
async function renamePet(petId) {
    const newName = prompt('请输入新的昵称（最多20个字符）：');
    if (!newName || newName.trim() === '') return;
    
    showLoading();
    try {
        const response = await apiRequest(`/api/pets/rename/${petId}`, 'POST', { nickname: newName.trim() });
        hideLoading();
        
        if (response.success) {
            showToast('✏️ 重命名成功！', 'success');
            closePetModal();
            loadMyPets();
        } else {
            showToast(response.message || '重命名失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('重命名失败', 'error');
    }
}

// 锁定/解锁宠物
async function toggleLockPet(petId) {
    showLoading();
    try {
        const response = await apiRequest(`/api/pets/toggle-lock/${petId}`, 'POST');
        hideLoading();
        
        if (response.success) {
            showToast('操作成功！', 'success');
            closePetModal();
            loadMyPets();
        } else {
            showToast(response.message || '操作失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('操作失败', 'error');
    }
}

// 释放宠物
async function releasePet(petId) {
    if (!confirm('确定要释放这只宠物吗？此操作不可恢复！')) return;
    
    showLoading();
    try {
        const response = await apiRequest(`/api/pets/release/${petId}`, 'DELETE');
        hideLoading();
        
        if (response.success) {
            showToast('🗑️ 释放成功', 'success');
            closePetModal();
            loadMyPets();
        } else {
            showToast(response.message || '释放失败', 'error');
        }
    } catch (error) {
        hideLoading();
        showToast('释放失败', 'error');
    }
}

// 工具函数：获取宠物表情
function getPetEmoji(petId) {
    const emojis = {
        1: '🦊', 2: '🦄', 3: '🐉', 4: '🐯', 5: '🐢', 
        6: '🦅', 7: '🐱', 8: '🦅', 9: '🐺', 10: '🦅'
    };
    return emojis[petId] || '🐾';
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

// 工具函数：获取忠诚度状态
function getLoyaltyStatus(loyalty) {
    if (loyalty >= 80) return '忠诚';
    if (loyalty >= 50) return '友好';
    if (loyalty >= 20) return '一般';
    return '疏远';
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
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    toastMessage.textContent = message;
    toast.className = 'toast ' + type;
    toast.classList.remove('hidden');
    
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}

// API请求函数
async function apiRequest(url, method = 'GET', data = null) {
    const token = localStorage.getItem('token');
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        }
    };
    
    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }
    
    const response = await fetch(url, options);
    
    if (response.status === 401) {
        localStorage.removeItem('token');
        window.location.href = 'login.html';
        throw new Error('未授权，请重新登录');
    }
    
    return await response.json();
}
