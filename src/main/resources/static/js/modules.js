// 模块管理和导航系统
class ModuleManager {
    constructor() {
        this.currentModule = 'dashboard';
        this.navigationMode = 'drawer'; // 'drawer' 或 'tabs'
        this.modules = [
            'dashboard', 'combat', 'inventory', 'pets', 'quests', 'skills', 
            'shop', 'mail', 'guild', 'ranking', 'achievements', 
            'auction', 'vip', 'activity', 'narrative', 'lore', 'combos', 'petEvolution', 'map',
            'checkin'
        ];
        this.init();
    }

    init() {
        // 从localStorage恢复导航模式
        const savedMode = localStorage.getItem('navigationMode');
        if (savedMode) {
            this.navigationMode = savedMode;
            this.applyNavigationMode();
        }
        
        // 绑定事件
        this.bindEvents();
        
        // 初始化模块
        this.showModule('dashboard');
    }

    bindEvents() {
        // 绑定抽屉切换
        window.toggleDrawer = () => this.toggleDrawer();
        
        // 绑定导航模式切换
        window.toggleNavigationMode = () => this.toggleNavigationMode();
        
        // 绑定模块切换
        window.showModule = (moduleName) => this.showModule(moduleName);
        
        // 绑定各种标签页切换
        window.showInventoryTab = (tab) => this.showTab('inventory', tab);
        window.showShopTab = (tab) => this.showTab('shop', tab);
        window.showRankingTab = (tab) => this.showTab('ranking', tab);
        window.showAuctionTab = (tab) => this.showTab('auction', tab);
        window.showVipTab = (tab) => this.showTab('vip', tab);
        window.showActivityTab = (tab) => this.showTab('activity', tab);
    }

    toggleNavigationMode() {
        this.navigationMode = this.navigationMode === 'drawer' ? 'tabs' : 'drawer';
        localStorage.setItem('navigationMode', this.navigationMode);
        this.applyNavigationMode();
    }

    applyNavigationMode() {
        const body = document.body;
        const tabNavigation = document.getElementById('tabNavigation');
        const drawerToggle = document.getElementById('drawerToggle');
        const navModeText = document.getElementById('navModeText');

        if (this.navigationMode === 'tabs') {
            body.classList.add('tab-navigation-mode');
            if (tabNavigation) tabNavigation.style.display = 'block';
            if (drawerToggle) drawerToggle.style.display = 'none';
            if (navModeText) navModeText.textContent = '切换到抽屉';
        } else {
            body.classList.remove('tab-navigation-mode');
            if (tabNavigation) tabNavigation.style.display = 'none';
            if (drawerToggle) drawerToggle.style.display = 'block';
            if (navModeText) navModeText.textContent = '切换到标签页';
        }
    }

    toggleDrawer() {
        const drawer = document.getElementById('drawer');
        if (drawer) {
            drawer.classList.toggle('open');
        }
    }

    showModule(moduleName) {
        console.log('切换到模块:', moduleName);
        
        // 隐藏所有模块
        this.modules.forEach(module => {
            const element = document.getElementById(`${module}-module`);
            if (element) {
                element.style.display = 'none';
                element.classList.remove('active');
            }
        });

        // 显示目标模块
        const targetModule = document.getElementById(`${moduleName}-module`);
        if (targetModule) {
            targetModule.style.display = 'block';
            targetModule.classList.add('active');
        }

        // 更新导航状态
        this.updateNavigationState(moduleName);
        
        // 加载模块数据
        this.loadModuleData(moduleName);
        
        // 关闭抽屉（移动端）
        const drawer = document.getElementById('drawer');
        if (drawer) {
            drawer.classList.remove('open');
        }

        this.currentModule = moduleName;
    }

    updateNavigationState(moduleName) {
        // 更新抽屉导航状态
        document.querySelectorAll('.drawer-item').forEach(item => {
            item.classList.remove('active');
            if (item.dataset.module === moduleName) {
                item.classList.add('active');
            }
        });

        // 更新标签页导航状态
        document.querySelectorAll('.tab-navigation .tab-btn').forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.module === moduleName) {
                btn.classList.add('active');
            }
        });
    }

    showTab(moduleType, tabName) {
        // 隐藏所有标签页内容
        document.querySelectorAll(`#${moduleType}-module .tab-content, #${moduleType}-module [class$="-tab-content"]`).forEach(tab => {
            tab.style.display = 'none';
        });

        // 显示目标标签页
        const targetTab = document.getElementById(`${tabName}-${moduleType}-tab`) || 
                         document.getElementById(`${tabName}-tab`);
        if (targetTab) {
            targetTab.style.display = 'block';
        }

        // 更新标签按钮状态
        document.querySelectorAll(`#${moduleType}-module .tab-btn`).forEach(btn => {
            btn.classList.remove('active');
        });
        
        event.target.classList.add('active');
    }

    async loadModuleData(moduleName) {
        try {
            switch (moduleName) {
                case 'dashboard':
                    await this.loadDashboardData();
                    break;
                case 'mail':
                    await this.loadMailData();
                    break;
                case 'auction':
                    await this.loadAuctionData();
                    break;
                case 'vip':
                    await this.loadVipData();
                    break;
                case 'activity':
                    await this.loadActivityData();
                    break;
                case 'inventory':
                    await this.loadInventoryData();
                    break;
                case 'quests':
                    await this.loadQuestsData();
                    break;
                case 'pets':
                    await this.loadPetsData();
                    break;
                case 'skills':
                    await this.loadSkillsData();
                    break;
                case 'shop':
                    await this.loadShopData();
                    break;
                case 'combat':
                    await this.loadCombatData();
                    break;
                case 'guild':
                    await this.loadGuildData();
                    break;
                case 'ranking':
                    await this.loadRankingData();
                    break;
                case 'achievements':
                    await this.loadAchievementsData();
                    break;
                case 'combos':
                    await this.loadCombosData();
                    break;
                case 'petEvolution':
                    await this.loadPetEvolutionData();
                    break;
                case 'narrative':
                    await this.loadNarrativeData();
                    break;
                case 'lore':
                    await this.loadLoreData();
                    break;
                case 'map':
                    await this.loadMapData();
                    break;
                case 'checkin':
                    await this.loadCheckInData();
                    break;
                case 'achievements':
                    await this.loadAchievementsData();
                    break;
            }
        } catch (error) {
            console.error(`加载${moduleName}模块数据失败:`, error);
        }
    }

    async loadDashboardData() {
        // 修炼模块数据已在main.js中处理
        console.log('加载修炼模块数据');
    }

    async loadMailData() {
        console.log('加载邮件数据');
        if (window.mailUI?.init) {
            await window.mailUI.init();
        }
    }

    async loadAuctionData() {
        console.log('加载拍卖行数据');
        try {
            // 更新灵石余额
            const ssEl = document.getElementById('auction-ss-balance');
            if (ssEl) {
                const ss = document.getElementById('playerSpiritStones');
                ssEl.textContent = ss ? ss.textContent : '0';
            }
            await window.loadAuctionItems();
        } catch (error) {
            console.error('加载拍卖行失败:', error);
        }
    }

    async loadVipData() {
        console.log('加载VIP数据');
        // 实现VIP数据加载
    }

    async loadActivityData() {
        console.log('加载活动数据');
        // 实现活动数据加载
    }

    async loadInventoryData() {
        console.log('加载背包数据');
        if (window.gameManager) {
            await window.gameManager.loadInventory();
        }
    }

    async loadQuestsData() {
        console.log('加载任务数据');
        if (window.questUI?.init) {
            await window.questUI.init();
        }
    }

    async loadPetsData() {
        console.log('加载宠物数据');
        if (window.loadMyPets) {
            await window.loadMyPets();
        }
    }

    async loadSkillsData() {
        console.log('加载技能数据');
        if (window.loadMySkills) {
            await window.loadMySkills();
        }
    }

    async loadShopData() {
        console.log('加载商城数据');
        if (window.gameManager) {
            await window.gameManager.loadShopItems();
            await window.gameManager.loadSkillShop();
        }
        // 更新灵石余额显示
        this.updatePlayerSpiritStones();
    }

    updatePlayerSpiritStones() {
        const el = document.getElementById('shop-spirit-stones');
        if (el) {
            const ss = document.getElementById('playerSpiritStones');
            if (ss) el.textContent = ss.textContent;
        }
    }

    async loadCombatData() {
        console.log('加载战斗数据');
        if (window.combatUI?.init) {
            window.combatUI.init();
        }
    }

    async loadGuildData() {
        console.log('加载宗门数据');
        if (window.guildUI?.init) {
            await window.guildUI.init();
        }
    }

    async loadRankingData() {
        console.log('加载排行榜数据');
        if (window.rankingUI?.init) {
            await window.rankingUI.init();
        }
    }

    async loadAchievementsData() {
        console.log('加载成就数据');
        if (window.achievementUI?.init) {
            await window.achievementUI.init();
        }
    }

    async loadCombosData() {
        console.log('加载技能连招数据');
        try {
            if (window.skillComboSystem) {
                await window.skillComboSystem.loadCombos();
            }
        } catch (error) {
            console.error('加载连招数据失败:', error);
        }
    }

    async loadPetEvolutionData() {
        console.log('加载宠物进化数据');
        if (window.petEvolutionUI?.init) {
            await window.petEvolutionUI.init();
        }
    }

    async loadNarrativeData() {
        console.log('加载叙事数据');
        if (window.narrativeUI?.init) {
            await window.narrativeUI.init();
        }
    }

    async loadLoreData() {
        console.log('加载传说数据');
        if (window.loreUI?.init) {
            await window.loreUI.init();
        }
    }

    async loadMapData() {
        console.log('加载地图数据');
        if (window.mapUI?.init) {
            await window.mapUI.init();
        }
    }

    async loadCheckInData() {
        console.log('加载签到数据');
        if (window.checkinUI?.init) {
            await window.checkinUI.init();
        }
    }

    async loadAchievementsData() {
        console.log('加载成就数据');
        if (window.achievementUI?.init) {
            await window.achievementUI.init();
        }
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    }

    showToast(message, type = 'info') {
        // 使用authManager的showToast方法
        if (window.authManager && window.authManager.showToast) {
            window.authManager.showToast(message, type);
        } else {
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
    }
}

// 创建模块管理器实例
const moduleManager = new ModuleManager();

// 全局函数
window.refreshMails = async function() {
    return window.mailUI?.loadMails();
};

window.markAllAsRead = async function() {
    return window.mailUI?.markAllAsRead();
};

window.openMail = async function(mailId) {
    return window.mailUI?.openMail(mailId);
};

window.claimAttachment = async function(mailId) {
    return window.mailUI?.claimAttachment(mailId);
};

// 导出到全局
window.moduleManager = moduleManager;

// ==================== 模块切换辅助函数 ====================

// 背包标签切换
window.switchInventoryTab = function(tab) {
    document.querySelectorAll('#inventory-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.invTab === tab);
    });
    const grid = document.getElementById('inventoryGrid');
    if (!grid) return;
    const items = grid.querySelectorAll('.inventory-cell');
    items.forEach(cell => {
        const type = cell.dataset.itemType || 'all';
        if (tab === 'all' || type === tab) {
            cell.style.display = '';
        } else {
            cell.style.display = 'none';
        }
    });
};

// 商城标签切换
window.switchShopTab = function(tab) {
    document.querySelectorAll('#shop-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.shopTab === tab);
    });
    document.getElementById('shop-general-panel').style.display = (tab === 'general') ? '' : 'none';
    document.getElementById('shop-skill-panel').style.display = (tab === 'skill') ? '' : 'none';
};

// 刷新商城商品
window.refreshShopItems = async function() {
    if (window.gameManager) {
        await window.gameManager.loadShopItems();
        moduleManager.showToast('商城已刷新', 'success');
    }
};

// 任务标签切换
window.switchQuestTab = function(tab) {
    return window.questUI?.switchTab(tab);
};

window.claimQuest = async function(questId) {
    return window.questUI?.claimQuest(questId);
};

// ==================== 宗门系统辅助函数（委托到模块化实现） ====================

// ==================== 拍卖行系统辅助函数 ====================

window.switchAuctionTab = function(tab) {
    return window.auctionUI?.switchTab(tab);
};

window.loadAuctionItems = async function() {
    return window.auctionUI?.loadAuctionItems();
};

window.loadMyAuctionItems = async function() {
    return window.auctionUI?.loadMyAuctionItems();
};

window.buyAuctionItem = async function(auctionId) {
    return window.auctionUI?.buyAuctionItem(auctionId);
};

window.cancelAuctionItem = async function(auctionId) {
    return window.auctionUI?.cancelAuctionItem(auctionId);
};

// ==================== 宠物系统辅助函数（委托到模块化实现） ====================

function getPetsModule() {
    if (!window.petsUI) {
        throw new Error('宠物模块尚未初始化');
    }
    return window.petsUI;
}

window.switchPetTab = function(tab) {
    return getPetsModule().switchTab(tab);
};

window.loadMyPets = async function() {
    return getPetsModule().loadMyPets();
};

window.loadAvailablePets = async function() {
    return getPetsModule().loadAvailablePets();
};

window.activatePet = async function(playerPetId) {
    return getPetsModule().activatePet(playerPetId);
};

window.feedPet = async function(playerPetId) {
    return getPetsModule().feedPet(playerPetId);
};

window.trainPet = async function(playerPetId) {
    const types = ['普通训练', '强化训练', '特训'];
    const type = prompt('选择训练类型（普通训练/强化训练/特训）:');
    if (!types.includes(type?.trim())) {
        moduleManager.showToast('无效的训练类型', 'error');
        return;
    }
    return getPetsModule().trainPet(playerPetId, type.trim());
};

window.togglePetLock = async function(playerPetId) {
    return getPetsModule().togglePetLock(playerPetId);
};

window.releasePet = async function(playerPetId) {
    if (!confirm('确定要放生这只宠物吗？此操作不可撤销！')) return;
    return getPetsModule().releasePet(playerPetId);
};

window.capturePet = async function(petId) {
    return getPetsModule().capturePet(petId);
};

// ==================== 技能系统辅助函数（委托到模块化实现） ====================

function getSkillsModule() {
    if (!window.skillsUI) {
        throw new Error('技能模块尚未初始化');
    }
    return window.skillsUI;
}

window.switchSkillTab = function(tab) {
    return getSkillsModule().switchSkillTab(tab);
};

window.loadMySkills = async function() {
    return getSkillsModule().loadMySkills();
};

window.loadAvailableSkills = async function() {
    return getSkillsModule().loadAvailableSkills();
};

window.learnSkill = async function(skillId) {
    return getSkillsModule().learnSkill(skillId);
};

window.equipSkill = async function(playerSkillId, slotNumber) {
    return getSkillsModule().equipSkill(playerSkillId, slotNumber);
};

window.unequipSkill = async function(playerSkillId) {
    return getSkillsModule().unequipSkill(playerSkillId);
};

window.upgradeSkill = async function(playerSkillId) {
    return getSkillsModule().upgradeSkill(playerSkillId);
};

// ==================== 修炼/挂机辅助函数（委托到模块化实现） ====================

function getCultivateModule() {
    if (!window.cultivateUI) {
        throw new Error('修炼模块尚未初始化');
    }
    return window.cultivateUI;
}

window.startCultivation = async function() {
    return getCultivateModule().startCultivation();
};

window.stopCultivation = async function() {
    return getCultivateModule().stopCultivation();
};

window.toggleCultivation = async function() {
    return getCultivateModule().toggleCultivation();
};

window.claimOfflineRewards = async function() {
    return getCultivateModule().claimOfflineRewards();
};

window.resetCultivation = async function() {
    return getCultivateModule().resetCultivation();
};

// ==================== 仙界人物（NPC）辅助函数（委托到模块化实现） ====================

function getNarrativeModule() {
    if (!window.narrativeUI) {
        throw new Error('叙事模块尚未初始化');
    }
    return window.narrativeUI;
}

window.switchNarrativeTab = function(tab) {
    return getNarrativeModule().switchGameTab(tab);
};

window.loadNpcList = async function() {
    return getNarrativeModule().loadNpcList();
};

window.loadNpcRelations = async function() {
    return getNarrativeModule().loadNpcRelations();
};

window.showNpcDetail = async function(npcId) {
    return getNarrativeModule().showNpcDetail(npcId);
};

// ==================== 传说图鉴辅助函数 ====================

window.switchLoreTab = function(tab) {
    return window.loreUI?.switchTab(tab);
};

window.loadLoreEntries = async function(filter = 'all') {
    return window.loreUI?.loadEntries(filter);
};

// ==================== 技能连招辅助函数（委托到模块化实现） ====================

window.switchComboTab = function(tab) {
    return getSkillsModule().switchComboTab(tab);
};

window.loadCombos = async function(availableOnly = true) {
    return getSkillsModule().loadCombos(availableOnly);
};

// ==================== 宠物进化辅助函数 ====================

window.loadEvolutionInfo = async function() {
    return window.petEvolutionUI?.loadEvolutionInfo();
};

window.doEvolution = async function(petId) {
    return window.petEvolutionUI?.doEvolution(petId);
};

// ==================== 世界地图辅助函数（委托到模块化实现） ====================

function getMapModule() {
    if (!window.mapUI) {
        throw new Error('地图模块尚未初始化');
    }
    return window.mapUI;
}

window.switchMapTab = function(tab) {
    return getMapModule().switchGameTab(tab);
};

window.loadCurrentMap = async function() {
    return getMapModule().loadCurrentMap();
};

window.loadMapList = async function() {
    return getMapModule().loadMapList();
};

window.enterMap = async function(mapId) {
    return getMapModule().enterMap(mapId);
};

window.exploreMap = async function() {
    return getMapModule().exploreMap();
};

// ==================== 每日签到辅助函数 ====================

window.changeCheckinMonth = function(delta) {
    return window.checkinUI?.changeMonth(delta);
};

window.doCheckIn = async function() {
    return window.checkinUI?.doCheckIn();
};

window.switchAchievementTab = function(tab) {
    if (!window.achievementUI) {
        throw new Error('成就模块尚未初始化');
    }
    return window.achievementUI.switchTab(tab);
};

window.claimAchievement = async function(id) {
    if (!window.achievementUI) {
        throw new Error('成就模块尚未初始化');
    }
    return window.achievementUI.claimAchievement(id);
};

window.switchRankingTab = function(tab) {
    if (!window.rankingUI) {
        throw new Error('排行榜模块尚未初始化');
    }
    return window.rankingUI.switchTab(tab);
};
