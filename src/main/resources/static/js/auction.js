// 拍卖行JavaScript逻辑

// 当前页码和总页数
let currentPage = 1;
let totalPages = 1;

// 显示标签页
function showTab(tabName) {
    // 隐藏所有标签页内容
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.style.display = 'none';
    });
    
    // 移除所有标签页的激活状态
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 显示选中的标签页
    document.getElementById(`${tabName}-tab`).style.display = 'block';
    
    // 激活选中的标签页按钮
    document.querySelector(`.nav-tab[data-module="${tabName}"]`).classList.add('active');
    
    // 根据标签页加载相应数据
    switch(tabName) {
        case 'browse':
            loadAuctionItems(1);
            break;
        case 'my-auctions':
            loadMyAuctions('');
            break;
        case 'list-item':
            // 不需要加载数据
            break;
    }
}

// 加载拍卖物品列表
async function loadAuctionItems(page = 1) {
    showLoading(true);
    
    try {
        const itemType = document.getElementById('itemTypeFilter').value;
        const minPrice = document.getElementById('minPriceFilter').value || null;
        const maxPrice = document.getElementById('maxPriceFilter').value || null;
        
        let url = `/auction/items?page=${page}&size=12`;
        if (itemType) url += `&itemType=${itemType}`;
        if (minPrice) url += `&minPrice=${minPrice}`;
        if (maxPrice) url += `&maxPrice=${maxPrice}`;
        
        const response = await api.get(url);
        if (response.success) {
            displayAuctionItems(response.data.records);
            currentPage = response.data.current;
            totalPages = response.data.pages;
            updatePagination();
        } else {
            showToast('获取拍卖物品失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取拍卖物品失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示拍卖物品列表
function displayAuctionItems(items) {
    const container = document.getElementById('auctionItemsContainer');
    if (!container) return;
    
    if (items.length === 0) {
        container.innerHTML = '<p class="text-center col-span-3 py-8 text-gray-500">暂无拍卖物品</p>';
        return;
    }
    
    container.innerHTML = items.map(item => `
        <div class="auction-item-card">
            <div class="p-4 border-b border-gray-200">
                <h3 class="font-bold text-lg">${getItemTypeName(item.itemType)} - ${item.id}</h3>
                <p class="text-gray-600 text-sm">ID: ${item.itemId}</p>
            </div>
            <div class="p-4">
                <div class="flex justify-between items-center mb-2">
                    <span class="text-gray-600">数量:</span>
                    <span class="font-semibold">${item.quantity}</span>
                </div>
                <div class="flex justify-between items-center mb-2">
                    <span class="text-gray-600">价格:</span>
                    <span class="font-semibold text-green-600">${item.price} 灵石</span>
                </div>
                <div class="flex justify-between items-center mb-4">
                    <span class="text-gray-600">剩余时间:</span>
                    <span class="font-semibold">${getTimeRemaining(item.expireAt)}</span>
                </div>
                <button class="btn btn-primary w-full" onclick="buyItem(${item.id})">
                    <i class="fas fa-coins"></i> 立即购买
                </button>
            </div>
        </div>
    `).join('');
}

// 加载玩家的拍卖物品
async function loadMyAuctions(status = '') {
    showLoading(true);
    
    try {
        const response = await api.get(`/auction/my-items?status=${status}`);
        if (response.success) {
            displayMyAuctions(response.data);
        } else {
            showToast('获取我的拍卖失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取我的拍卖失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示我的拍卖物品
function displayMyAuctions(items) {
    const container = document.getElementById('myAuctionsContainer');
    if (!container) return;
    
    if (items.length === 0) {
        container.innerHTML = '<p class="text-center col-span-3 py-8 text-gray-500">暂无拍卖物品</p>';
        return;
    }
    
    container.innerHTML = items.map(item => {
        let statusText = '';
        let statusClass = '';
        switch (item.status) {
            case 'ON_SALE':
                statusText = '拍卖中';
                statusClass = 'bg-blue-100 text-blue-800';
                break;
            case 'SOLD':
                statusText = '已售出';
                statusClass = 'bg-green-100 text-green-800';
                break;
            case 'CANCELLED':
                statusText = '已取消';
                statusClass = 'bg-yellow-100 text-yellow-800';
                break;
            case 'EXPIRED':
                statusText = '已过期';
                statusClass = 'bg-gray-100 text-gray-800';
                break;
        }
        
        return `
            <div class="auction-item-card">
                <div class="p-4 border-b border-gray-200">
                    <div class="flex justify-between items-start">
                        <div>
                            <h3 class="font-bold text-lg">${getItemTypeName(item.itemType)} - ${item.id}</h3>
                            <p class="text-gray-600 text-sm">ID: ${item.itemId}</p>
                        </div>
                        <span class="px-2 py-1 rounded-full text-xs font-semibold ${statusClass}">
                            ${statusText}
                        </span>
                    </div>
                </div>
                <div class="p-4">
                    <div class="flex justify-between items-center mb-2">
                        <span class="text-gray-600">数量:</span>
                        <span class="font-semibold">${item.quantity}</span>
                    </div>
                    <div class="flex justify-between items-center mb-2">
                        <span class="text-gray-600">价格:</span>
                        <span class="font-semibold text-green-600">${item.price} 灵石</span>
                    </div>
                    ${item.status === 'ON_SALE' ? `
                        <button class="btn btn-secondary w-full mt-2" onclick="cancelAuction(${item.id})">
                            <i class="fas fa-times"></i> 取消拍卖
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
    }).join('');
}

// 加载玩家物品（用于上架）
async function loadPlayerItems() {
    const itemType = document.getElementById('itemType').value;
    const select = document.getElementById('playerItemId');
    
    if (!itemType) {
        select.innerHTML = '<option value="">请先选择物品类型</option>';
        return;
    }
    
    try {
        let url = '';
        switch (itemType) {
            case 'ITEM':
                url = '/inventory/items';
                break;
            case 'EQUIPMENT':
                url = '/equipment/list';
                break;
            case 'PET':
                url = '/pets/list';
                break;
        }
        
        const response = await api.get(url);
        if (response.success) {
            displayPlayerItems(response.data, itemType);
        } else {
            showToast('获取物品失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取物品失败: ' + error.message, 'error');
    }
}

// 显示玩家物品（用于上架）
function displayPlayerItems(items, itemType) {
    const select = document.getElementById('playerItemId');
    if (!select) return;
    
    if (items.length === 0) {
        select.innerHTML = '<option value="">该类型下无可用物品</option>';
        return;
    }
    
    select.innerHTML = items.map(item => {
        let itemName = '';
        let itemDetails = '';
        
        switch (itemType) {
            case 'ITEM':
                itemName = item.item ? item.item.name : `物品-${item.itemId}`;
                itemDetails = `${item.quantity}个`;
                break;
            case 'EQUIPMENT':
                itemName = item.equipment ? item.equipment.name : `装备-${item.equipmentId}`;
                itemDetails = `攻击+${item.attackBonus} 防御+${item.defenseBonus}`;
                break;
            case 'PET':
                itemName = item.pet ? item.pet.name : `宠物-${item.petId}`;
                itemDetails = `等级${item.level}`;
                break;
        }
        
        return `<option value="${item.id}">${itemName} (${itemDetails})</option>`;
    }).join('');
}

// 上架物品
async function listItem() {
    const form = document.getElementById('listItemForm');
    const formData = new FormData(form);
    
    const itemType = formData.get('itemType');
    const playerItemId = formData.get('playerItemId');
    const quantity = parseInt(formData.get('quantity'));
    const price = parseInt(formData.get('price'));
    const duration = parseInt(formData.get('duration'));
    
    // 验证表单
    if (!itemType || !playerItemId || !quantity || !price || !duration) {
        showToast('请填写所有必填字段', 'error');
        return;
    }
    
    if (quantity <= 0) {
        showToast('数量必须大于0', 'error');
        return;
    }
    
    if (price <= 0) {
        showToast('价格必须大于0', 'error');
        return;
    }
    
    if (duration <= 0) {
        showToast('持续时间必须大于0', 'error');
        return;
    }
    
    showLoading(true);
    
    try {
        // 获取物品ID
        let itemId = 0;
        switch (itemType) {
            case 'ITEM':
                const itemResponse = await api.get(`/inventory/items`);
                if (itemResponse.success) {
                    const selectedItem = itemResponse.data.find(item => item.id == playerItemId);
                    if (selectedItem) {
                        itemId = selectedItem.itemId;
                    }
                }
                break;
            case 'EQUIPMENT':
                const equipResponse = await api.get(`/equipment/list`);
                if (equipResponse.success) {
                    const selectedEquip = equipResponse.data.find(equip => equip.id == playerItemId);
                    if (selectedEquip) {
                        itemId = selectedEquip.equipmentId;
                    }
                }
                break;
            case 'PET':
                const petResponse = await api.get(`/pets/list`);
                if (petResponse.success) {
                    const selectedPet = petResponse.data.find(pet => pet.id == playerItemId);
                    if (selectedPet) {
                        itemId = selectedPet.petId;
                    }
                }
                break;
        }
        
        if (itemId === 0) {
            showToast('无法获取物品信息', 'error');
            return;
        }
        
        const response = await api.post('/auction/list', {
            itemType: itemType,
            itemId: itemId,
            playerItemId: parseInt(playerItemId),
            quantity: quantity,
            price: price,
            duration: duration
        });
        
        if (response.success) {
            showToast('物品上架成功', 'success');
            // 重置表单
            form.reset();
            document.getElementById('playerItemId').innerHTML = '<option value="">请先选择物品类型</option>';
            document.getElementById('feeAmount').textContent = '0';
            
            // 切换到我的拍卖标签页
            showTab('my-auctions');
            loadMyAuctions('');
        } else {
            showToast('物品上架失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('物品上架失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 购买拍卖物品
async function buyItem(auctionItemId) {
    if (!confirm('确定要购买这个物品吗？')) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await api.post(`/auction/buy/${auctionItemId}`);
        if (response.success) {
            showToast('购买成功', 'success');
            loadAuctionItems(currentPage);
            loadPlayerData(); // 更新玩家灵石等信息
        } else {
            showToast('购买失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('购买失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 取消拍卖
async function cancelAuction(auctionItemId) {
    if (!confirm('确定要取消这个拍卖吗？将会收取一定的手续费')) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await api.post(`/auction/cancel/${auctionItemId}`);
        if (response.success) {
            showToast('拍卖已取消', 'success');
            loadMyAuctions('');
            loadPlayerData(); // 更新玩家灵石等信息
        } else {
            showToast('取消失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('取消失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 更新手续费显示
function updateFee() {
    const price = document.getElementById('price').value;
    const feeElement = document.getElementById('feeAmount');
    if (price && !isNaN(price) && price > 0) {
        const fee = Math.max(1, Math.floor(price / 20));
        feeElement.textContent = fee;
    } else {
        feeElement.textContent = '0';
    }
}

// 更新分页控件
function updatePagination() {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let paginationHtml = '';
    
    // 上一页按钮
    if (currentPage > 1) {
        paginationHtml += `<button class="btn btn-secondary mx-1" onclick="loadAuctionItems(${currentPage - 1})">上一页</button>`;
    }
    
    // 页码按钮
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        if (i === currentPage) {
            paginationHtml += `<button class="btn btn-primary mx-1" disabled>${i}</button>`;
        } else {
            paginationHtml += `<button class="btn btn-secondary mx-1" onclick="loadAuctionItems(${i})">${i}</button>`;
        }
    }
    
    // 下一页按钮
    if (currentPage < totalPages) {
        paginationHtml += `<button class="btn btn-secondary mx-1" onclick="loadAuctionItems(${currentPage + 1})">下一页</button>`;
    }
    
    pagination.innerHTML = paginationHtml;
}

// 获取物品类型名称
function getItemTypeName(itemType) {
    switch (itemType) {
        case 'ITEM':
            return '普通物品';
        case 'EQUIPMENT':
            return '装备';
        case 'PET':
            return '宠物';
        default:
            return itemType;
    }
}

// 计算剩余时间
function getTimeRemaining(expireAt) {
    const expireDate = new Date(expireAt);
    const now = new Date();
    const diff = expireDate - now;
    
    if (diff <= 0) {
        return '已过期';
    }
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    
    if (days > 0) {
        return `${days}天${hours}小时`;
    } else if (hours > 0) {
        return `${hours}小时${minutes}分钟`;
    } else {
        return `${minutes}分钟`;
    }
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