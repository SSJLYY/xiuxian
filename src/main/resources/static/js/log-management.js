/**
 * 日志管理页面JavaScript
 */

class LogManagement {
    constructor() {
        this.currentLoginPage = 1;
        this.currentOperationPage = 1;
        this.pageSize = 20;
        this.operationTypes = [];
        
        this.init();
    }
    
    init() {
        this.bindEvents();
        this.loadOperationTypes();
        this.loadLoginLogs();
    }
    
    bindEvents() {
        // 标签页切换
        document.getElementById('loginLogTab').addEventListener('click', () => {
            this.switchTab('loginLog');
        });
        
        document.getElementById('operationLogTab').addEventListener('click', () => {
            this.switchTab('operationLog');
        });
        
        // 搜索按钮
        document.getElementById('searchLoginLogs').addEventListener('click', () => {
            this.currentLoginPage = 1;
            this.loadLoginLogs();
        });
        
        document.getElementById('searchOperationLogs').addEventListener('click', () => {
            this.currentOperationPage = 1;
            this.loadOperationLogs();
        });
        
        // 分页按钮 - 登录日志
        document.getElementById('loginLogPrevPage').addEventListener('click', () => {
            if (this.currentLoginPage > 1) {
                this.currentLoginPage--;
                this.loadLoginLogs();
            }
        });
        
        document.getElementById('loginLogNextPage').addEventListener('click', () => {
            this.currentLoginPage++;
            this.loadLoginLogs();
        });
        
        // 分页按钮 - 操作日志
        document.getElementById('operationLogPrevPage').addEventListener('click', () => {
            if (this.currentOperationPage > 1) {
                this.currentOperationPage--;
                this.loadOperationLogs();
            }
        });
        
        document.getElementById('operationLogNextPage').addEventListener('click', () => {
            this.currentOperationPage++;
            this.loadOperationLogs();
        });
        
        // 模态框关闭
        document.getElementById('closeOperationDetailModal').addEventListener('click', () => {
            this.hideOperationDetailModal();
        });
        
        // 点击模态框外部关闭
        document.getElementById('operationDetailModal').addEventListener('click', (e) => {
            if (e.target.id === 'operationDetailModal') {
                this.hideOperationDetailModal();
            }
        });
    }
    
    switchTab(tabName) {
        // 更新标签按钮状态
        document.querySelectorAll('.tab-button').forEach(btn => {
            btn.classList.remove('active', 'border-blue-500', 'text-blue-600');
            btn.classList.add('border-transparent', 'text-gray-500');
        });
        
        // 隐藏所有面板
        document.querySelectorAll('.tab-panel').forEach(panel => {
            panel.classList.add('hidden');
        });
        
        if (tabName === 'loginLog') {
            document.getElementById('loginLogTab').classList.add('active', 'border-blue-500', 'text-blue-600');
            document.getElementById('loginLogTab').classList.remove('border-transparent', 'text-gray-500');
            document.getElementById('loginLogPanel').classList.remove('hidden');
            this.loadLoginLogs();
        } else if (tabName === 'operationLog') {
            document.getElementById('operationLogTab').classList.add('active', 'border-blue-500', 'text-blue-600');
            document.getElementById('operationLogTab').classList.remove('border-transparent', 'text-gray-500');
            document.getElementById('operationLogPanel').classList.remove('hidden');
            this.loadOperationLogs();
        }
    }
    
    async loadOperationTypes() {
        try {
            const response = await api.get('/admin/logs/operation-types');
            if (response.success) {
                this.operationTypes = response.data;
                this.renderOperationTypeOptions();
            }
        } catch (error) {
            console.error('加载操作类型失败:', error);
        }
    }
    
    renderOperationTypeOptions() {
        const select = document.getElementById('operationType');
        select.innerHTML = '<option value="">全部</option>';
        
        this.operationTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type;
            option.textContent = this.getOperationTypeName(type);
            select.appendChild(option);
        });
    }
    
    getOperationTypeName(type) {
        const typeNames = {
            'PLAYER_BAN': '封禁玩家',
            'PLAYER_UNBAN': '解封玩家',
            'PLAYER_DELETE': '删除玩家',
            'PLAYER_REWARD': '发放奖励',
            'PLAYER_MODIFY': '修改玩家',
            'ANNOUNCEMENT_CREATE': '创建公告',
            'ANNOUNCEMENT_UPDATE': '更新公告',
            'ANNOUNCEMENT_DELETE': '删除公告',
            'MAIL_SEND': '发送邮件',
            'MAIL_BATCH_SEND': '批量发送邮件',
            'GIFT_CODE_CREATE': '创建礼包码',
            'GIFT_CODE_DISABLE': '禁用礼包码',
            'ACTIVITY_CREATE': '创建活动',
            'ACTIVITY_UPDATE': '更新活动',
            'CONFIG_UPDATE': '更新配置'
        };
        return typeNames[type] || type;
    }
    
    async loadLoginLogs() {
        try {
            const params = new URLSearchParams({
                page: this.currentLoginPage,
                size: this.pageSize
            });
            
            const playerId = document.getElementById('loginPlayerId').value;
            const startTime = document.getElementById('loginStartTime').value;
            const endTime = document.getElementById('loginEndTime').value;
            
            if (playerId) params.append('playerId', playerId);
            if (startTime) params.append('startTime', startTime);
            if (endTime) params.append('endTime', endTime);
            
            const response = await api.get(`/admin/logs/player-login?${params}`);
            if (response.success) {
                this.renderLoginLogs(response.data);
                this.updateLoginLogPagination(response.data);
            } else {
                this.showError('加载登录日志失败: ' + response.message);
            }
        } catch (error) {
            console.error('加载登录日志失败:', error);
            this.showError('加载登录日志失败');
        }
    }
    
    renderLoginLogs(pageData) {
        const tbody = document.getElementById('loginLogTableBody');
        tbody.innerHTML = '';
        
        if (pageData.records.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="px-6 py-4 text-center text-gray-500">暂无数据</td>
                </tr>
            `;
            return;
        }
        
        pageData.records.forEach(log => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.id}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.playerId}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.ipAddress || '-'}</td>
                <td class="px-6 py-4 text-sm text-gray-900" title="${log.deviceInfo || '-'}">
                    ${this.truncateText(log.deviceInfo || '-', 30)}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${this.formatDateTime(log.loginAt)}</td>
            `;
            tbody.appendChild(row);
        });
    }
    
    updateLoginLogPagination(pageData) {
        const start = (pageData.current - 1) * pageData.size + 1;
        const end = Math.min(pageData.current * pageData.size, pageData.total);
        
        document.getElementById('loginLogStart').textContent = start;
        document.getElementById('loginLogEnd').textContent = end;
        document.getElementById('loginLogTotal').textContent = pageData.total;
        document.getElementById('loginLogPageInfo').textContent = `第 ${pageData.current} 页，共 ${pageData.pages} 页`;
        
        document.getElementById('loginLogPrevPage').disabled = pageData.current <= 1;
        document.getElementById('loginLogNextPage').disabled = pageData.current >= pageData.pages;
    }
    
    async loadOperationLogs() {
        try {
            const params = new URLSearchParams({
                page: this.currentOperationPage,
                size: this.pageSize
            });
            
            const adminId = document.getElementById('operationAdminId').value;
            const operationType = document.getElementById('operationType').value;
            const startTime = document.getElementById('operationStartTime').value;
            const endTime = document.getElementById('operationEndTime').value;
            
            if (adminId) params.append('adminId', adminId);
            if (operationType) params.append('operationType', operationType);
            if (startTime) params.append('startTime', startTime);
            if (endTime) params.append('endTime', endTime);
            
            const response = await api.get(`/admin/logs/admin-operation?${params}`);
            if (response.success) {
                this.renderOperationLogs(response.data);
                this.updateOperationLogPagination(response.data);
            } else {
                this.showError('加载操作日志失败: ' + response.message);
            }
        } catch (error) {
            console.error('加载操作日志失败:', error);
            this.showError('加载操作日志失败');
        }
    }
    
    renderOperationLogs(pageData) {
        const tbody = document.getElementById('operationLogTableBody');
        tbody.innerHTML = '';
        
        if (pageData.records.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="px-6 py-4 text-center text-gray-500">暂无数据</td>
                </tr>
            `;
            return;
        }
        
        pageData.records.forEach(log => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.id}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.adminId}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${this.getOperationTypeName(log.operationType)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.targetType || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.targetId || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.ipAddress || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${this.formatDateTime(log.createdAt)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <button onclick="logManagement.showOperationDetail(${log.id})" class="text-blue-600 hover:text-blue-900">查看详情</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }
    
    updateOperationLogPagination(pageData) {
        const start = (pageData.current - 1) * pageData.size + 1;
        const end = Math.min(pageData.current * pageData.size, pageData.total);
        
        document.getElementById('operationLogStart').textContent = start;
        document.getElementById('operationLogEnd').textContent = end;
        document.getElementById('operationLogTotal').textContent = pageData.total;
        document.getElementById('operationLogPageInfo').textContent = `第 ${pageData.current} 页，共 ${pageData.pages} 页`;
        
        document.getElementById('operationLogPrevPage').disabled = pageData.current <= 1;
        document.getElementById('operationLogNextPage').disabled = pageData.current >= pageData.pages;
    }
    
    showOperationDetail(logId) {
        // 从当前页面数据中找到对应的日志记录
        const tbody = document.getElementById('operationLogTableBody');
        const rows = tbody.querySelectorAll('tr');
        
        for (let row of rows) {
            const firstCell = row.querySelector('td');
            if (firstCell && firstCell.textContent == logId) {
                const cells = row.querySelectorAll('td');
                const detail = {
                    id: cells[0].textContent,
                    adminId: cells[1].textContent,
                    operationType: cells[2].textContent,
                    targetType: cells[3].textContent,
                    targetId: cells[4].textContent,
                    ipAddress: cells[5].textContent,
                    createdAt: cells[6].textContent
                };
                
                this.renderOperationDetail(detail);
                this.showOperationDetailModal();
                break;
            }
        }
    }
    
    renderOperationDetail(detail) {
        const content = document.getElementById('operationDetailContent');
        content.innerHTML = `
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700">日志ID</label>
                    <p class="mt-1 text-sm text-gray-900">${detail.id}</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">管理员ID</label>
                    <p class="mt-1 text-sm text-gray-900">${detail.adminId}</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">操作类型</label>
                    <p class="mt-1 text-sm text-gray-900">${detail.operationType}</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">目标类型</label>
                    <p class="mt-1 text-sm text-gray-900">${detail.targetType}</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">目标ID</label>
                    <p class="mt-1 text-sm text-gray-900">${detail.targetId}</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">IP地址</label>
                    <p class="mt-1 text-sm text-gray-900">${detail.ipAddress}</p>
                </div>
                <div class="col-span-2">
                    <label class="block text-sm font-medium text-gray-700">操作时间</label>
                    <p class="mt-1 text-sm text-gray-900">${detail.createdAt}</p>
                </div>
            </div>
        `;
    }
    
    showOperationDetailModal() {
        document.getElementById('operationDetailModal').classList.remove('hidden');
    }
    
    hideOperationDetailModal() {
        document.getElementById('operationDetailModal').classList.add('hidden');
    }
    
    formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '-';
        const date = new Date(dateTimeStr);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }
    
    truncateText(text, maxLength) {
        if (!text || text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }
    
    showError(message) {
        alert(message); // 简单的错误提示，可以替换为更好的UI组件
    }
}

// 初始化
const logManagement = new LogManagement();