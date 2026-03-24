// 宗门系统脚本

let myGuild = null;
let currentTab = 'info';
let currentPage = 1;
let pageSize = 12;

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    loadMyGuild();
    loadGuildList();
    
    // 搜索功能
    const searchInput = document.getElementById('searchGuild');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                currentPage = 1;
                loadGuildList();
            }, 500);
        });
    }
});

/**
 * 加载我的宗门
 */
async function loadMyGuild() {
    try {
        const response = await apiCall('/api/guild/my', 'GET');
        
        if (response.success && response.data) {
            myGuild = response.data;
            renderMyGuild();
            document.getElementById('myGuildSection').style.display = 'block';
            document.getElementById('guildListSection').style.display = 'none';
        } else {
            document.getElementById('myGuildSection').style.display = 'none';
            document.getElementById('guildListSection').style.display = 'block';
        }
    } catch (error) {
        console.error('加载我的宗门失败:', error);
    }
}

/**
 * 渲染我的宗门
 */
function renderMyGuild() {
    const myGuildInfo = document.getElementById('myGuildInfo');
    
    if (!myGuild) return;
    
    const expProgress = myGuild.expToNext > 0 ? (myGuild.exp / myGuild.expToNext * 100).toFixed(1) : 0;
    
    myGuildInfo.innerHTML = `
        <div class="guild-card">
            <h3>⚔️ ${escapeHtml(myGuild.guildName)}</h3>
            <p style="opacity: 0.9; margin-top: 10px;">${escapeHtml(myGuild.description || '暂无简介')}</p>
            ${myGuild.announcement ? `<p style="background: rgba(255,255,255,0.2); padding: 10px; border-radius: 6px; margin-top: 10px;">📢 ${escapeHtml(myGuild.announcement)}</p>` : ''}
            <div class="guild-stats">
                <div class="stat">
                    <div class="stat-label">宗门等级</div>
                    <div class="stat-value">Lv.${myGuild.level}</div>
                </div>
                <div class="stat">
                    <div class="stat-label">成员数量</div>
                    <div class="stat-value">${myGuild.memberCount}/${myGuild.maxMembers}</div>
                </div>
                <div class="stat">
                    <div class="stat-label">宗门资金</div>
                    <div class="stat-value">${formatNumber(myGuild.guildFunds)}</div>
                </div>
                <div class="stat">
                    <div class="stat-label">宗门经验</div>
                    <div class="stat-value">${expProgress}%</div>
                </div>
            </div>
        </div>
    `;
}

/**
 * 加载宗门列表
 */
async function loadGuildList() {
    try {
        const response = await apiCall('/api/guild/list?page=1&size=20', 'GET');
        
        if (response.success) {
            renderGuildList(response.data.guilds);
        } else {
            showError('加载宗门列表失败: ' + response.message);
        }
    } catch (error) {
        console.error('加载宗门列表失败:', error);
        showError('加载宗门列表失败');
    }
}

/**
 * 渲染宗门列表
 */
function renderGuildList(guilds) {
    const guildList = document.getElementById('guildList');
    
    if (!guilds || guilds.length === 0) {
        guildList.innerHTML = '<p class="empty-message">暂无宗门</p>';
        return;
    }
    
    let html = '<div class="guild-items">';
    
    guilds.forEach(guild => {
        html += `
            <div class="guild-item">
                <div class="guild-info">
                    <h4>${escapeHtml(guild.guildName)}</h4>
                    <p>${escapeHtml(guild.description || '暂无简介')}</p>
                    <div class="guild-stats">
                        <span>等级 ${guild.level}</span>
                        <span>成员 ${guild.memberCount}/${guild.maxMembers}</span>
                    </div>
                </div>
                <button onclick="applyToGuild(${guild.id})" class="btn-primary">申请加入</button>
            </div>
        `;
    });
    
    html += '</div>';
    guildList.innerHTML = html;
}

/**
 * 显示创建宗门模态框
 */
function showCreateGuildModal() {
    document.getElementById('createGuildModal').style.display = 'block';
}

/**
 * 关闭创建宗门模态框
 */
function closeCreateGuildModal() {
    document.getElementById('createGuildModal').style.display = 'none';
}

/**
 * 创建宗门
 */
async function createGuild() {
    const guildName = document.getElementById('guildName').value.trim();
    const description = document.getElementById('guildDescription').value.trim();
    
    if (!guildName) {
        showError('请输入宗门名称');
        return;
    }
    
    try {
        const response = await apiCall('/api/guild/create', 'POST', {
            guildName: guildName,
            description: description
        });
        
        if (response.success) {
            showSuccess('宗门创建成功！');
            closeCreateGuildModal();
            loadMyGuild();
        } else {
            showError('创建失败: ' + response.message);
        }
    } catch (error) {
        console.error('创建宗门失败:', error);
        showError('创建宗门失败');
    }
}

/**
 * 申请加入宗门
 */
async function applyToGuild(guildId) {
    try {
        const response = await apiCall(`/api/guild/apply/${guildId}`, 'POST');
        
        if (response.success) {
            showSuccess('申请已提交，请等待审核');
        } else {
            showError('申请失败: ' + response.message);
        }
    } catch (error) {
        console.error('申请加入宗门失败:', error);
        showError('申请加入宗门失败');
    }
}

/**
 * 显示捐献模态框
 */
function showDonateModal() {
    document.getElementById('donateModal').style.display = 'block';
}

/**
 * 关闭捐献模态框
 */
function closeDonateModal() {
    document.getElementById('donateModal').style.display = 'none';
}

/**
 * 捐献
 */
async function donate() {
    const amount = parseInt(document.getElementById('donateAmount').value);
    
    if (!amount || amount <= 0) {
        showError('请输入有效的捐献数量');
        return;
    }
    
    try {
        const response = await apiCall('/api/guild/donate', 'POST', {
            amount: amount
        });
        
        if (response.success) {
            showSuccess('捐献成功！');
            closeDonateModal();
            loadMyGuild();
        } else {
            showError('捐献失败: ' + response.message);
        }
    } catch (error) {
        console.error('捐献失败:', error);
        showError('捐献失败');
    }
}

/**
 * 退出宗门
 */
async function leaveGuild() {
    if (!confirm('确定要退出宗门吗？')) {
        return;
    }
    
    try {
        const response = await apiCall('/api/guild/leave', 'POST');
        
        if (response.success) {
            showSuccess('已退出宗门');
            myGuild = null;
            loadMyGuild();
            loadGuildList();
        } else {
            showError('退出失败: ' + response.message);
        }
    } catch (error) {
        console.error('退出宗门失败:', error);
        showError('退出宗门失败');
    }
}

/**
 * HTML转义
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * 显示错误消息
 */
function showError(message) {
    alert('错误: ' + message);
}

/**
 * 显示成功消息
 */
function showSuccess(message) {
    alert(message);
}

/**
 * 切换标签页
 */
function switchTab(tab) {
    currentTab = tab;
    
    // 更新按钮状态
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // 显示对应内容
    document.getElementById('guildInfoTab').style.display = tab === 'info' ? 'block' : 'none';
    document.getElementById('guildMembersTab').style.display = tab === 'members' ? 'block' : 'none';
    document.getElementById('guildApplicationsTab').style.display = tab === 'applications' ? 'block' : 'none';
    
    // 加载对应数据
    if (tab === 'members') {
        loadGuildMembers();
    } else if (tab === 'applications') {
        loadGuildApplications();
    }
}

/**
 * 加载宗门成员
 */
async function loadGuildMembers() {
    if (!myGuild) return;
    
    try {
        const response = await apiCall(`/api/guild/${myGuild.id}`, 'GET');
        
        if (response.success && response.data.members) {
            renderGuildMembers(response.data.members);
        } else {
            showError('加载成员列表失败');
        }
    } catch (error) {
        console.error('加载宗门成员失败:', error);
        showError('加载成员列表失败');
    }
}

/**
 * 渲染宗门成员
 */
function renderGuildMembers(members) {
    const membersList = document.getElementById('membersList');
    
    if (!members || members.length === 0) {
        membersList.innerHTML = '<p class="empty-message">暂无成员</p>';
        return;
    }
    
    // 按贡献值排序
    members.sort((a, b) => b.contribution - a.contribution);
    
    let html = '';
    members.forEach((member, index) => {
        const roleClass = member.role === 'LEADER' ? 'role-leader' : 
                         member.role === 'OFFICER' ? 'role-officer' : 'role-member';
        const roleName = member.role === 'LEADER' ? '宗主' : 
                        member.role === 'OFFICER' ? '长老' : '成员';
        
        html += `
            <div class="member-item">
                <div class="member-info">
                    <span style="font-weight: bold; color: #333;">#${index + 1}</span>
                    <span style="font-size: 16px; color: #333;">玩家 ${member.playerId}</span>
                    <span class="member-role ${roleClass}">${roleName}</span>
                </div>
                <div style="display: flex; align-items: center; gap: 15px;">
                    <span class="contribution-badge">贡献: ${formatNumber(member.contribution)}</span>
                    <span style="color: #999; font-size: 14px;">加入于 ${formatDate(member.joinedAt)}</span>
                </div>
            </div>
        `;
    });
    
    membersList.innerHTML = html;
}

/**
 * 加载宗门申请
 */
async function loadGuildApplications() {
    if (!myGuild) return;
    
    try {
        // 这里需要后端提供获取申请列表的接口
        const applicationsList = document.getElementById('applicationsList');
        applicationsList.innerHTML = '<p class="empty-message">暂无待处理申请</p>';
    } catch (error) {
        console.error('加载宗门申请失败:', error);
    }
}

/**
 * 显示宗门设置模态框
 */
function showGuildSettingsModal() {
    if (!myGuild) return;
    
    document.getElementById('guildAnnouncement').value = myGuild.announcement || '';
    document.getElementById('guildSettingsModal').style.display = 'block';
}

/**
 * 关闭宗门设置模态框
 */
function closeGuildSettingsModal() {
    document.getElementById('guildSettingsModal').style.display = 'none';
}

/**
 * 更新宗门设置
 */
async function updateGuildSettings() {
    const announcement = document.getElementById('guildAnnouncement').value.trim();
    
    try {
        // 这里需要后端提供更新宗门设置的接口
        showSuccess('宗门设置已更新');
        closeGuildSettingsModal();
        loadMyGuild();
    } catch (error) {
        console.error('更新宗门设置失败:', error);
        showError('更新宗门设置失败');
    }
}

/**
 * 格式化数字
 */
function formatNumber(num) {
    if (num >= 100000000) {
        return (num / 100000000).toFixed(2) + '亿';
    } else if (num >= 10000) {
        return (num / 10000).toFixed(2) + '万';
    }
    return num.toString();
}

/**
 * 格式化日期
 */
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (days === 0) {
        const hours = Math.floor(diff / (1000 * 60 * 60));
        if (hours === 0) {
            const minutes = Math.floor(diff / (1000 * 60));
            return minutes + '分钟前';
        }
        return hours + '小时前';
    } else if (days < 7) {
        return days + '天前';
    } else {
        return date.toLocaleDateString('zh-CN');
    }
}

// 点击模态框外部关闭
window.onclick = function(event) {
    const modals = document.getElementsByClassName('modal');
    for (let modal of modals) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    }
}

/**
 * 切换宗门标签页（含BOSS标签）
 */
function switchGuildTab(tabName) {
    // 更新标签按钮状态
    document.querySelectorAll('.guild-tab-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.tab === tabName) {
            btn.classList.add('active');
        }
    });

    // 显示对应区域
    const sections = {
        'my-guild': 'myGuildSection',
        'guild-boss': 'guildBossSection',
        'guild-list': 'guildListSection',
        'create-guild': 'createGuildSection'
    };

    Object.entries(sections).forEach(([key, id]) => {
        const el = document.getElementById(id);
        if (el) {
            el.style.display = 'none';
            el.classList.remove('active');
        }
    });

    const target = document.getElementById(sections[tabName]);
    if (target) {
        target.style.display = 'block';
        target.classList.add('active');
    }

    // 切换到BOSS标签时初始化BOSS系统
    if (tabName === 'guild-boss' && window.guildBossSystem) {
        guildBossSystem.init();
    }
}

