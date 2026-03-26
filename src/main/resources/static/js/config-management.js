/**
 * 配置管理页面JavaScript
 */

class ConfigManagement {
    constructor() {
        this.configs = [];
        this.currentCategory = 'ALL';
        this.editingConfig = null;
        
        this.init();
    }
    
    init() {
        this.bindEvents();
        this.loadConfigs();
        this.loadQuickStatus();
    }
    
    bindEvents() {
        // 刷新缓存按钮
        document.getElementById('refreshCacheBtn').addEventListener('click', () => {
            this.refreshCache();
        });
        
        // 添加配置按钮
        document.getElementById('addConfigBtn').addEventListener('click', () => {
            this.showAddConfigModal();
        });
        
        // 快捷操作按钮
        document.getElementById('toggleDoubleExpBtn').addEventListener('click', () => {
            this.toggleDoubleExp();
        });
        
        document.getElementById('toggleDoubleDropBtn').addEventListener('click', () => {
            this.toggleDoubleDrop();
        });
        
        document.getElementById('toggleMaintenanceBtn').addEventListener('click', () => {
            this.toggleMaintenance();
        });
        
        // 分类筛选按钮
        document.querySelectorAll('.category-filter').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.filterByCategory(e.target.dataset.category);
            });
        });
        
        // 模态框按钮
        document.getElementById('cancelBtn').addEventListener('click', () => {
            this.hideConfigModal();
        });
        
        document.getElementById('saveBtn').addEventListener('click', () => {
            this.saveConfig();
        });
        
        // 点击模态框外部关闭
        document.getElementById('configModal').addEventListener('click', (e) => {
            if (e.target.id === 'configModal') {
                this.hideConfigModal();
            }
        });
    }
    
    async loadConfigs() {
        try {
            const response = await api.get('/admin/config/list');
            if (response.success) {
                this.configs = response.data;
                this.renderConfigs();
            } else {
                this.showError('加载配置失败: ' + response.message);
            }
        } catch (error) {
            console.error('加载配置失败:', error);
            this.showError('加载配置失败');
        }
    }
    
    async loadQuickStatus() {
        try {
            const response = await api.get('/admin/config/list');
            if (response.success) {
                const configs = response.data;
                
                // 更新快捷操作状态
                const doubleExpConfig = configs.find(c => c.configKey === 'activity.double.exp.enabled');
                const doubleDropConfig = configs.find(c => c.configKey === 'activity.double.drop.enabled');
                const maintenanceConfig = configs.find(c => c.configKey === 'system.maintenance.mode');
                
                this.updateQuickStatus('doubleExpStatus', doubleExpConfig?.configValue === 'true');
                this.updateQuickStatus('doubleDropStatus', doubleDropConfig?.configValue === 'true');
                this.updateQuickStatus('maintenanceStatus', maintenanceConfig?.configValue === 'true');
            }
        } catch (error) {
            console.error('加载快捷状态失败:', error);
        }
    }
    
    updateQuickStatus(elementId, enabled) {
        const element = document.getElementById(elementId);
        element.textContent = `状态: ${enabled ? '开启' : '关闭'}`;
        element.className = `text-sm ${enabled ? 'text-green-600' : 'text-gray-600'}`;
    }
    
    renderConfigs() {
        const tbody = document.getElementById('configTableBody');
        tbody.innerHTML = '';
        
        const filteredConfigs = this.currentCategory === 'ALL' 
            ? this.configs 
            : this.configs.filter(config => config.category === this.currentCategory);
        
        if (filteredConfigs.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="px-6 py-4 text-center text-gray-500">暂无数据</td>
                </tr>
            `;
            return;
        }
        
        filteredConfigs.forEach(config => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${escapeHtml(config.configKey)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <span class="max-w-xs truncate block" title="${escapeHtml(config.configValue)}">${escapeHtml(config.configValue)}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <span class="px-2 py-1 text-xs rounded-full ${this.getTypeColor(config.configType)}">${escapeHtml(config.configType)}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <span class="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">${escapeHtml(config.category)}</span>
                </td>
                <td class="px-6 py-4 text-sm text-gray-900">
                    <span class="max-w-xs truncate block" title="${escapeHtml(config.description || '-')}">${escapeHtml(config.description || '-')}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${this.formatDateTime(config.updatedAt)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <button onclick="configManagement.editConfig('${config.configKey}')" class="text-blue-600 hover:text-blue-900 mr-2">编辑</button>
                    <button onclick="configManagement.deleteConfig('${config.configKey}')" class="text-red-600 hover:text-red-900">删除</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }
    
    getTypeColor(type) {
        const colors = {
            'STRING': 'bg-blue-100 text-blue-800',
            'INTEGER': 'bg-green-100 text-green-800',
            'DOUBLE': 'bg-yellow-100 text-yellow-800',
            'BOOLEAN': 'bg-purple-100 text-purple-800'
        };
        return colors[type] || 'bg-gray-100 text-gray-800';
    }
    
    filterByCategory(category) {
        this.currentCategory = category;
        
        // 更新按钮状态
        document.querySelectorAll('.category-filter').forEach(btn => {
            btn.classList.remove('active', 'bg-blue-500', 'text-white');
            btn.classList.add('bg-gray-200', 'text-gray-700');
        });
        
        const activeBtn = document.querySelector(`[data-category="${category}"]`);
        activeBtn.classList.add('active', 'bg-blue-500', 'text-white');
        activeBtn.classList.remove('bg-gray-200', 'text-gray-700');
        
        this.renderConfigs();
    }
    
    showAddConfigModal() {
        this.editingConfig = null;
        document.getElementById('modalTitle').textContent = '添加配置';
        document.getElementById('configKey').disabled = false;
        document.getElementById('configForm').reset();
        document.getElementById('configModal').classList.remove('hidden');
    }
    
    editConfig(configKey) {
        this.editingConfig = this.configs.find(c => c.configKey === configKey);
        if (!this.editingConfig) return;
        
        document.getElementById('modalTitle').textContent = '编辑配置';
        document.getElementById('configKey').value = this.editingConfig.configKey;
        document.getElementById('configKey').disabled = true;
        document.getElementById('configValue').value = this.editingConfig.configValue;
        document.getElementById('configCategory').value = this.editingConfig.category;
        document.getElementById('configDescription').value = this.editingConfig.description || '';
        
        document.getElementById('configModal').classList.remove('hidden');
    }
    
    hideConfigModal() {
        document.getElementById('configModal').classList.add('hidden');
        this.editingConfig = null;
    }
    
    async saveConfig() {
        const key = document.getElementById('configKey').value.trim();
        const value = document.getElementById('configValue').value.trim();
        const category = document.getElementById('configCategory').value;
        const description = document.getElementById('configDescription').value.trim();
        
        if (!key || !value) {
            this.showError('配置键和配置值不能为空');
            return;
        }
        
        try {
            let response;
            if (this.editingConfig) {
                // 更新配置
                response = await api.put('/admin/config/update', null, {
                    key: key,
                    value: value,
                    category: category,
                    description: description
                });
            } else {
                // 创建配置
                response = await api.post('/admin/config/create', {
                    key: key,
                    value: value,
                    category: category,
                    description: description
                });
            }
            
            if (response.success) {
                this.showSuccess(this.editingConfig ? '更新成功' : '创建成功');
                this.hideConfigModal();
                this.loadConfigs();
                this.loadQuickStatus();
            } else {
                this.showError(response.message);
            }
        } catch (error) {
            console.error('保存配置失败:', error);
            this.showError('保存失败');
        }
    }
    
    async deleteConfig(configKey) {
        if (!confirm('确定要删除这个配置吗？')) {
            return;
        }
        
        try {
            const response = await api.delete('/admin/config/delete', {
                key: configKey
            });
            
            if (response.success) {
                this.showSuccess('删除成功');
                this.loadConfigs();
                this.loadQuickStatus();
            } else {
                this.showError(response.message);
            }
        } catch (error) {
            console.error('删除配置失败:', error);
            this.showError('删除失败');
        }
    }
    
    async refreshCache() {
        try {
            const response = await api.post('/admin/config/refresh');
            if (response.success) {
                this.showSuccess('缓存刷新成功');
            } else {
                this.showError(response.message);
            }
        } catch (error) {
            console.error('刷新缓存失败:', error);
            this.showError('刷新缓存失败');
        }
    }
    
    async toggleDoubleExp() {
        try {
            const currentConfig = this.configs.find(c => c.configKey === 'activity.double.exp.enabled');
            const currentEnabled = currentConfig?.configValue === 'true';
            
            const response = await api.post('/admin/config/toggle-double-exp', {
                enabled: !currentEnabled
            });
            
            if (response.success) {
                this.showSuccess(response.message);
                this.loadQuickStatus();
                this.loadConfigs();
            } else {
                this.showError(response.message);
            }
        } catch (error) {
            console.error('切换双倍经验失败:', error);
            this.showError('操作失败');
        }
    }
    
    async toggleDoubleDrop() {
        try {
            const currentConfig = this.configs.find(c => c.configKey === 'activity.double.drop.enabled');
            const currentEnabled = currentConfig?.configValue === 'true';
            
            const response = await api.post('/admin/config/toggle-double-drop', {
                enabled: !currentEnabled
            });
            
            if (response.success) {
                this.showSuccess(response.message);
                this.loadQuickStatus();
                this.loadConfigs();
            } else {
                this.showError(response.message);
            }
        } catch (error) {
            console.error('切换双倍掉落失败:', error);
            this.showError('操作失败');
        }
    }
    
    async toggleMaintenance() {
        try {
            const currentConfig = this.configs.find(c => c.configKey === 'system.maintenance.mode');
            const currentEnabled = currentConfig?.configValue === 'true';
            
            if (!currentEnabled && !confirm('确定要开启维护模式吗？这将影响所有玩家的游戏体验。')) {
                return;
            }
            
            const response = await api.post('/admin/config/toggle-maintenance', {
                enabled: !currentEnabled
            });
            
            if (response.success) {
                this.showSuccess(response.message);
                this.loadQuickStatus();
                this.loadConfigs();
            } else {
                this.showError(response.message);
            }
        } catch (error) {
            console.error('切换维护模式失败:', error);
            this.showError('操作失败');
        }
    }
    
    formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '-';
        const date = new Date(dateTimeStr);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    showSuccess(message) {
        // 简单的成功提示，可以替换为更好的UI组件
        alert('✓ ' + message);
    }
    
    showError(message) {
        // 简单的错误提示，可以替换为更好的UI组件
        alert('✗ ' + message);
    }
}

// 初始化
const configManagement = new ConfigManagement();