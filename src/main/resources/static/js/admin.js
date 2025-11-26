(() => {
  const loginBtn = document.getElementById('adminLoginBtn');
  const changeBtn = document.getElementById('changeBtn');
  const loginBox = document.getElementById('adminLogin');
  const forceBox = document.getElementById('forceChange');
  const consoleBox = document.getElementById('adminConsole');

  async function login() {
    const u = document.getElementById('adminUsername').value.trim();
    const p = document.getElementById('adminPassword').value;
    const res = await gameAPI.login(u, p);
    if (!res || !res.success) { alert(res?.message || '登录失败'); return; }
    const me = await gameAPI.getCurrentUser();
    if (!me?.success) { alert('获取用户失败'); return; }
    if (me.data.role !== 'ADMIN') { alert('需要管理员权限'); return; }
    if (me.data.mustChangePassword) {
      loginBox.style.display = 'none';
      forceBox.style.display = 'block';
    } else {
      loginBox.style.display = 'none';
      consoleBox.style.display = 'block';
      await loadData();
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
      row.innerHTML = `${u.id} ${u.username} ${u.role} <button data-id="${u.id}" data-role="ADMIN">设为ADMIN</button> <button data-id="${u.id}" data-role="USER">设为USER</button>`;
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
      row.innerHTML = `ID:${it.id} item:${it.itemId} 价:${it.priceSpiritStones} 存:${it.stock}`;
      shopBox.appendChild(row);
    });

    const skill = await gameAPI.adminListSkillShop();
    const skillBox = document.getElementById('skillShop');
    skillBox.innerHTML = '';
    (skill?.data || []).forEach(it => {
      const row = document.createElement('div');
      row.innerHTML = `ID:${it.id} skill:${it.skillId} 价:${it.price} 等:${it.requiredLevel}`;
      skillBox.appendChild(row);
    });
  }

  if (loginBtn) loginBtn.addEventListener('click', login);
  if (changeBtn) changeBtn.addEventListener('click', changePassword);
})();