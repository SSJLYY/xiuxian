(() => {
  const loginBtn = document.getElementById('adminLoginBtn');
  const changeBtn = document.getElementById('changeButton');
  const loginBox = document.getElementById('adminLogin');
  const forceBox = document.getElementById('forceChange');
  const consoleBox = document.getElementById('adminConsole');

  // 检查用户权限，必须是管理员才能访问
  async function checkAdminPermission() {
    try {
      // 检查token是否存在
      const token = localStorage.getItem('authToken');
      if (!token) {
        // 如果没有token，显示登录表单
        if (loginBox) loginBox.style.display = 'block';
        if (forceBox) forceBox.style.display = 'none';
        if (consoleBox) consoleBox.style.display = 'none';
        return false;
      }
      
      // 验证用户权限
      const me = await gameAPI.getCurrentUser();
      if (!me || !me.success) {
        window.location.href = 'login.html';
        return false;
      }
      
      if (me.data.role !== 'ADMIN') {
        alert('权限不足，需要管理员权限才能访问此页面。您将被重定向到修炼页面。');
        window.location.href = 'cultivate.html';
        return false;
      }
      
      return true;
    } catch (error) {
      console.error('权限检查失败:', error);
      // 清除token并跳转到登录页
      localStorage.removeItem('authToken');
      window.location.href = 'login.html';
      return false;
    }
  }

  async function login() {
    const u = document.getElementById('adminUsername')?.value.trim();
    const p = document.getElementById('adminPassword')?.value;
    
    if (!u || !p) {
      alert('请输入用户名和密码');
      return;
    }
    
    try {
      const res = await gameAPI.login(u, p);
      if (!res || !res.success) { 
        alert(res?.message || '登录失败'); 
        return; 
      }
      
      const me = await gameAPI.getCurrentUser();
      if (!me?.success) { 
        alert('获取用户失败'); 
        return; 
      }
      
      if (me.data.role !== 'ADMIN') { 
        alert('需要管理员权限，您将跳转到修炼页面'); 
        window.location.href = 'cultivate.html';
        return; 
      }
      
      if (me.data.mustChangePassword) {
        if (loginBox) loginBox.style.display = 'none';
        if (forceBox) forceBox.style.display = 'block';
        if (consoleBox) consoleBox.style.display = 'none';
      } else {
        if (loginBox) loginBox.style.display = 'none';
        if (forceBox) forceBox.style.display = 'none';
        if (consoleBox) consoleBox.style.display = 'block';
        await loadData();
      }
    } catch (error) {
      console.error('登录失败:', error);
      alert('登录失败: ' + error.message);
    }
  }

  async function changePassword() {
    const np = document.getElementById('newPassword').value;
    const r = await gameAPI.adminChangePassword(np);
    if (!r?.success) { alert(r?.message || '修改失败'); return; }
    forceBox.style.display = 'none';
    consoleBox.style.display = 'block';
    await loadData();
  }

  async function loadData() {
    const users = await gameAPI.adminListUsers();
    const userList = document.getElementById('userList');
    userList.innerHTML = '';
    (users?.data || []).forEach(u => {
      const row = document.createElement('div');
      row.innerHTML = `${escapeHtml(String(u.id))} ${escapeHtml(u.username)} ${escapeHtml(u.role)} <button data-id="${escapeHtml(String(u.id))}" data-role="ADMIN">设为ADMIN</button> <button data-id="${escapeHtml(String(u.id))}" data-role="USER">设为USER</button>`;
      userList.appendChild(row);
    });
    userList.querySelectorAll('button[data-id]').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        const id = e.currentTarget.getAttribute('data-id');
        const role = e.currentTarget.getAttribute('data-role');
        const r = await gameAPI.adminSetUserRole(id, role);
        if (r?.success) await loadData();
      });
    });

    const shop = await gameAPI.adminListShopItems();
    const shopBox = document.getElementById('shopItems');
    shopBox.innerHTML = '';
    (shop?.data || []).forEach(it => {
      const row = document.createElement('div');
      row.innerHTML = 'ID:' + escapeHtml(String(it.id)) + ' item:' + escapeHtml(String(it.itemId)) + ' 价:' + escapeHtml(String(it.priceSpiritStones)) + ' 存:' + escapeHtml(String(it.stock));
      shopBox.appendChild(row);
    });

    const skill = await gameAPI.adminListSkillShop();
    const skillBox = document.getElementById('skillShop');
    skillBox.innerHTML = '';
    (skill?.data || []).forEach(it => {
      const row = document.createElement('div');
      row.innerHTML = 'ID:' + escapeHtml(String(it.id)) + ' skill:' + escapeHtml(String(it.skillId)) + ' 价:' + escapeHtml(String(it.price)) + ' 等:' + escapeHtml(String(it.requiredLevel));
      skillBox.appendChild(row);
    });
  }

  // 页面加载时检查权限
  window.addEventListener('DOMContentLoaded', async () => {
    // 如果没有登录表单，说明用户可能是通过链接直接访问的
    if (!loginBox || loginBox.style.display === 'none') {
      const hasAdminPermission = await checkAdminPermission();
      if (hasAdminPermission) {
        // 如果是管理员且已经登录，显示控制台
        if (loginBox) loginBox.style.display = 'none';
        if (forceBox) forceBox.style.display = 'none';
        if (consoleBox) consoleBox.style.display = 'block';
        await loadData();
      }
    }
  });

  if (loginBtn) loginBtn.addEventListener('click', login);
  if (changeBtn) changeBtn.addEventListener('click', changePassword);
})();