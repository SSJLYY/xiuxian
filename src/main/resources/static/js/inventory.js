// 背包系统 JavaScript 功能

let currentItems = [];
let categorizedItems = {};

// 加载背包物品
function loadInventoryItems() {
    showLoading();
    api.get('/inventory/categorized')
        .then(response => {
            hideLoading();
            if (response.success) {
                categorizedItems = response.data;
                renderInventoryItems();
                updateInventoryStats();
            } else {
                showToast('获取背包物品失败: ' + response.message, 'error');
            }
        })
        .catch(error => {
            hideLoading();
            showToast('获取背包物品失败: ' + error.message, 'error');
        });
}

// 渲染背包物品
function renderInventoryItems() {
    const container = document.getElementById('itemsContainer');
    container.innerHTML = '';
    
    const typeFilter = document.getElementById('typeFilter').value;
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const sortBy = document.getElementById('sortSelect').value;
    const order = document.getElementById('orderSelect').value;
    
    // 合并所有物品到一个数�?
    let allItems = [];
    for (const type in categorizedItems) {
        allItems = allItems.concat(categorizedItems[type]);
    }
    
    // 应用过滤�?
    if (typeFilter) {
        allItems = allItems.filter(item => item.itemType === typeFilter);
    }
    
    if (searchTerm) {
        allItems = allItems.filter(item => 
            item.itemName.toLowerCase().includes(searchTerm) || 
            item.itemDescription.toLowerCase().includes(searchTerm)
        );
    }
    
    // 应用排序
    allItems.sort((a, b) => {
        let comparison = 0;
        
        switch (sortBy) {
            case 'quality':
                comparison = a.itemQuality - b.itemQuality;
                break;
            case 'quantity':
                comparison = a.quantity - b.quantity;
                break;
            case 'created':
                comparison = new Date(a.createdAt) - new Date(b.createdAt);
                break;
            default:
                // 默认按类型和品质排序
                const typeOrder = { '装备': 1, '消耗品': 2, '材料': 3, '任务物品': 4 };
                const aTypeOrder = typeOrder[a.itemType] || 5;
                const bTypeOrder = typeOrder[b.itemType] || 5;
                comparison = aTypeOrder - bTypeOrder || a.itemQuality - b.itemQuality;
        }
        
        return order === 'desc' ? -comparison : comparison;
    });
    
    currentItems = allItems;
    
    // 渲染物品
    if (allItems.length === 0) {
        container.innerHTML = '<div class="col-span-full text-center py-8 text-gray-500">暂无物品</div>';
        return;
    }
    
    allItems.forEach(item => {
        const itemElement = createItemCard(item);
        container.appendChild(itemElement);
    });
}

// 创建物品卡片
function createItemCard(item) {
    const card = document.createElement('div');
    card.className = `item-card bg-white rounded-lg shadow-md overflow-hidden border ${item.locked ? 'locked-item' : ''} item-quality-${item.itemQuality}`;
    
    // 获取品质颜色�?
    const qualityClasses = {
        1: 'text-gray-600',
        2: 'text-green-600',
        3: 'text-blue-600',
        4: 'text-purple-600',
        5: 'text-yellow-600'
    };
    
    const qualityText = {
        1: '��ͨ',
        2: '精良',
        3: 'ϡ��',
        4: 'ʷʫ',
        5: '��˵'
    };
    
    card.innerHTML = `
        <div class="p-4">
            <div class="flex justify-between items-start">
                <h3 class="font-bold text-lg ${qualityClasses[item.itemQuality] || ''}">${escapeHtml(item.itemName)}</h3>
                <span class="text-xs px-2 py-1 rounded ${qualityClasses[item.itemQuality] || ''} bg-opacity-20">
                    ${qualityText[item.itemQuality] || '未知'}
                </span>
            </div>
            <p class="text-gray-600 text-sm mt-1 truncate">${escapeHtml(item.itemDescription)}</p>
            <div class="mt-3 flex justify-between items-center">
                <span class="text-sm font-medium">数量: ${item.quantity}</span>
                <span class="text-xs px-2 py-1 rounded bg-gray-100">${item.itemType}</span>
            </div>
            <div class="mt-3 flex space-x-2">
                ${item.usable ? `<button class="btn btn-sm btn-primary" onclick="useItem(${item.id}, 1)">使用</button>` : ''}
                ${item.sellable ? `<button class="btn btn-sm btn-success" onclick="sellItem(${item.id}, 1)">出售</button>` : ''}
                <button class="btn btn-sm btn-info" onclick="showItemDetails(${item.itemId})">详情</button>
                <button class="btn btn-sm ${item.locked ? 'btn-warning' : 'btn-secondary'}" onclick="toggleItemLock(${item.id})">
                    ${item.locked ? '解锁' : '锁定'}
                </button>
            </div>
        </div>
    `;
    
    return card;
}

// 使用物品
function useItem(playerItemId, quantity = 1) {
    if (confirm(`确定要使用这个物品吗？`)) {
        showLoading();
        api.post(`/inventory/use/${playerItemId}?quantity=${quantity}`)
            .then(response => {
                hideLoading();
                if (response.success) {
                    showToast('使用物品成功', 'success');
                    loadInventoryItems(); // 重新加载物品列表
                } else {
                    showToast('使用物品失败: ' + response.message, 'error');
                }
            })
            .catch(error => {
                hideLoading();
                showToast('使用物品失败: ' + error.message, 'error');
            });
    }
}

// 出售物品
function sellItem(playerItemId, quantity = 1) {
    if (confirm(`确定要出售这个物品吗？`)) {
        showLoading();
        api.post(`/inventory/sell/${playerItemId}?quantity=${quantity}`)
            .then(response => {
                hideLoading();
                if (response.success) {
                    showToast('出售物品成功', 'success');
                    loadInventoryItems(); // 重新加载物品列表
                } else {
                    showToast('出售物品失败: ' + response.message, 'error');
                }
            })
            .catch(error => {
                hideLoading();
                showToast('出售物品失败: ' + error.message, 'error');
            });
    }
}

// 过滤物品
function filterItems() {
    renderInventoryItems();
}

// 排序物品
function sortItems() {
    renderInventoryItems();
}

// 整理背包
function organizeInventory() {
    if (confirm('ȷ��Ҫ�����������⽫�Զ��ѵ���ͬ��Ʒ��')) {
        showLoading();
        api.post('/inventory/organize')
            .then(response => {
                hideLoading();
                if (response.success) {
                    showToast('背包整理完成', 'success');
                    loadInventoryItems(); // 重新加载物品列表
                } else {
                    showToast('整理背包失败: ' + response.message, 'error');
                }
            })
            .catch(error => {
                hideLoading();
                showToast('整理背包失败: ' + error.message, 'error');
            });
    }
}

// 按类型排�?
function sortByType() {
    document.getElementById('sortSelect').value = '';
    document.getElementById('typeFilter').value = '';
    document.getElementById('orderSelect').value = 'asc';
    renderInventoryItems();
}

// 按品质排�?
function sortByQuality() {
    document.getElementById('sortSelect').value = 'quality';
    document.getElementById('typeFilter').value = '';
    document.getElementById('orderSelect').value = 'desc';
    renderInventoryItems();
}

// 显示物品详情
function showItemDetails(itemId) {
    showLoading();
    api.get(`/inventory/details/${itemId}`)
        .then(response => {
            hideLoading();
            if (!response.success) {
                showToast('获取物品详情失败: ' + response.message, 'error');
                return;
            }

            const item = response.data || {};
            const quality = Number(item.quality || item.itemQuality || 1);
            const detailHtml = `
                <div class="item-detail">
                    <h2 class="text-2xl font-bold mb-4">${escapeHtml(item.name || item.itemName || 'Unknown Item')}</h2>
                    <div class="mb-4">
                        <span class="inline-block px-2 py-1 rounded text-xs font-semibold mr-2">
                            Ʒ��: ${escapeHtml(getQualityText(quality))}
                        </span>
                        <span class="inline-block px-2 py-1 rounded text-xs font-semibold bg-gray-200 text-gray-800">
                            ����: ${escapeHtml(item.type || item.itemType || 'Unknown')}
                        </span>
                    </div>
                    <div class="mb-4">
                        <p class="text-gray-700">${escapeHtml(item.description || item.itemDescription || '')}</p>
                    </div>
                    <div class="mb-4">
                        <h3 class="font-bold mb-2">����</h3>
                        <ul class="list-disc pl-5">
                            <li>�۸�: ${item.price ?? 0} ��ʯ</li>
                            <li>�ɶѵ�: ${item.stackable ? '��' : '��'}</li>
                            <li>�ɳ���: ${item.sellable ? '��' : '��'}</li>
                            <li>��ʹ��: ${item.usable ? '��' : '��'}</li>
                            ${item.maxStack ? `<li>���ѵ�: ${item.maxStack}</li>` : ''}
                        </ul>
                    </div>
                    <div class="flex justify-end space-x-2">
                        <button class="btn btn-secondary" onclick="closeItemDetailModal()">�ر�</button>
                    </div>
                </div>
            `;
            document.getElementById('itemDetailContent').innerHTML = detailHtml;
            document.getElementById('itemDetailModal').style.display = 'block';
        })
        .catch(error => {
            hideLoading();
            showToast('获取物品详情失败: ' + error.message, 'error');
        });
}

// 切换物品锁定状�?
function toggleItemLock(playerItemId) {
    showLoading();
    api.post(`/inventory/lock/${playerItemId}`)
        .then(response => {
            hideLoading();
            if (response.success) {
                showToast('物品锁定状态已切换', 'success');
                loadInventoryItems();
            } else {
                showToast('切换锁定状态失�? ' + response.message, 'error');
            }
        })
        .catch(error => {
            hideLoading();
            showToast('切换锁定状态失�? ' + error.message, 'error');
        });
}

function getQualityText(quality) {
    const qualityMap = {
        1: '��ͨ',
        2: '����',
        3: 'ϡ��',
        4: 'ʷʫ',
        5: '��˵'
    };
    return qualityMap[quality] || 'δ֪';
}
// 更新背包统计
function updateInventoryStats() {
    let totalCount = 0;
    let stackableCount = 0;
    let uniqueCount = 0;
    let lockedCount = 0;
    
    // 合并所有物品到一个数�?
    let allItems = [];
    for (const type in categorizedItems) {
        allItems = allItems.concat(categorizedItems[type]);
    }
    
    totalCount = allItems.length;
    
    allItems.forEach(item => {
        if (item.stackable) {
            stackableCount++;
        }
        if (item.locked) {
            lockedCount++;
        }
    });
    
    uniqueCount = [...new Set(allItems.map(item => item.itemId))].length;
    
    document.getElementById('totalCount').textContent = totalCount;
    document.getElementById('stackableCount').textContent = stackableCount;
    document.getElementById('uniqueCount').textContent = uniqueCount;
    document.getElementById('lockedCount').textContent = lockedCount;
}

// 显示标签�?
function showTab(tabName) {
    // 隐藏所有标签页内容
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.style.display = 'none';
    });
    
    // 移除所有标签的激活状�?
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 显示选中的标签页
    document.getElementById(`${tabName}-tab`).style.display = 'block';
    
    // 激活选中的标�?
    document.querySelector(`[data-module="${tabName}"]`).classList.add('active');
    
    // 如果是整理标签页，更新统�?
    if (tabName === 'organize') {
        updateInventoryStats();
    }
}

// 关闭物品详情模态框
function closeItemDetailModal() {
    document.getElementById('itemDetailModal').style.display = 'none';
}

// 关闭批量操作模态框
function closeBatchActionModal() {
    document.getElementById('batchActionModal').style.display = 'none';
}

// 显示加载动画
function showLoading() {
    document.getElementById('loading').classList.remove('hidden');
}

// 隐藏加载动画
function hideLoading() {
    document.getElementById('loading').classList.add('hidden');
}

// 显示消息提示
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    toastMessage.textContent = message;
    toast.classList.remove('hidden');
    
    // 根据类型设置样式
    toast.className = 'toast';
    if (type === 'error') {
        toast.classList.add('toast-error');
    } else if (type === 'success') {
        toast.classList.add('toast-success');
    }
    
    // 3秒后自动隐藏
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}

// 点击模态框外部关闭
window.onclick = function(event) {
    const itemDetailModal = document.getElementById('itemDetailModal');
    const batchActionModal = document.getElementById('batchActionModal');
    
    if (event.target === itemDetailModal) {
        itemDetailModal.style.display = 'none';
    }
    
    if (event.target === batchActionModal) {
        batchActionModal.style.display = 'none';
    }
};

