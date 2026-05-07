// 妯″潡绠＄悊鍜屽鑸郴缁?
class ModuleManager {
    constructor() {
        this.currentModule = 'dashboard';
        this.navigationMode = 'drawer'; // 'drawer' 鎴?'tabs'
        this.modules = [
            'dashboard', 'combat', 'inventory', 'pets', 'quests', 'skills', 
            'shop', 'mail', 'guild', 'ranking', 'achievements', 
            'auction', 'vip', 'activity', 'narrative', 'lore', 'combos', 'petEvolution', 'map',
            'checkin'
        ];
        this.init();
    }

    init() {
        // 浠巐ocalStorage鎭㈠瀵艰埅妯″紡
        const savedMode = localStorage.getItem('navigationMode');
        if (savedMode) {
            this.navigationMode = savedMode;
            this.applyNavigationMode();
        }
        
        // 缁戝畾浜嬩欢
        this.bindEvents();
        
        // 鍒濆鍖栨ā鍧?
        this.showModule('dashboard');
    }

    bindEvents() {
        // 缁戝畾鎶藉眽鍒囨崲
        window.toggleDrawer = () => this.toggleDrawer();
        
        // 缁戝畾瀵艰埅妯″紡鍒囨崲
        window.toggleNavigationMode = () => this.toggleNavigationMode();
        
        // 缁戝畾妯″潡鍒囨崲
        window.showModule = (moduleName) => this.showModule(moduleName);
        
        // 缁戝畾鍚勭鏍囩椤靛垏鎹?
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
        
        // 闅愯棌鎵€鏈夋ā鍧?
        this.modules.forEach(module => {
            const element = document.getElementById(`${module}-module`);
            if (element) {
                element.style.display = 'none';
                element.classList.remove('active');
            }
        });

        // 鏄剧ず鐩爣妯″潡
        const targetModule = document.getElementById(`${moduleName}-module`);
        if (targetModule) {
            targetModule.style.display = 'block';
            targetModule.classList.add('active');
        }

        // 鏇存柊瀵艰埅鐘舵€?
        this.updateNavigationState(moduleName);
        
        // 鍔犺浇妯″潡鏁版嵁
        this.loadModuleData(moduleName);
        
        // 鍏抽棴鎶藉眽锛堢Щ鍔ㄧ锛?
        const drawer = document.getElementById('drawer');
        if (drawer) {
            drawer.classList.remove('open');
        }

        this.currentModule = moduleName;
    }

    updateNavigationState(moduleName) {
        // 鏇存柊鎶藉眽瀵艰埅鐘舵€?
        document.querySelectorAll('.drawer-item').forEach(item => {
            item.classList.remove('active');
            if (item.dataset.module === moduleName) {
                item.classList.add('active');
            }
        });

        // 鏇存柊鏍囩椤靛鑸姸鎬?
        document.querySelectorAll('.tab-navigation .tab-btn').forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.module === moduleName) {
                btn.classList.add('active');
            }
        });
    }

    showTab(moduleType, tabName) {
        // 闅愯棌鎵€鏈夋爣绛鹃〉鍐呭
        document.querySelectorAll(`#${moduleType}-module .tab-content, #${moduleType}-module [class$="-tab-content"]`).forEach(tab => {
            tab.style.display = 'none';
        });

        // 鏄剧ず鐩爣鏍囩椤?
        const targetTab = document.getElementById(`${tabName}-${moduleType}-tab`) || 
                         document.getElementById(`${tabName}-tab`);
        if (targetTab) {
            targetTab.style.display = 'block';
        }

        // 鏇存柊鏍囩鎸夐挳鐘舵€?
        const tabButtons = document.querySelectorAll(`#${moduleType}-module .tab-btn`);
        tabButtons.forEach(btn => {
            btn.classList.remove('active');
        });

        const activeButton = Array.from(tabButtons).find(btn =>
            btn.dataset.invTab === tabName ||
            btn.dataset.shopTab === tabName ||
            btn.dataset.rankTab === tabName ||
            btn.dataset.auctionTab === tabName ||
            btn.dataset.vipTab === tabName ||
            btn.dataset.activityTab === tabName
        );
        if (activeButton) {
            activeButton.classList.add('active');
        }
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
            console.error(`鍔犺浇${moduleName}妯″潡鏁版嵁澶辫触:`, error);
        }
    }

    async loadDashboardData() {
        // 淇偧妯″潡鏁版嵁宸插湪main.js涓鐞?
        console.log('鍔犺浇淇偧妯″潡鏁版嵁');
    }

    async loadMailData() {
        console.log('鍔犺浇閭欢鏁版嵁');
        if (window.mailUI?.init) {
            await window.mailUI.init();
        }
    }

    async loadAuctionData() {
        console.log('加载拍卖行数据');
        try {
            // 鏇存柊鐏电煶浣欓
            const ssEl = document.getElementById('auction-ss-balance');
            if (ssEl) {
                const ss = document.getElementById('playerSpiritStones');
                ssEl.textContent = ss ? ss.textContent : '0';
            }
            await window.loadAuctionItems();
        } catch (error) {
            console.error('鍔犺浇鎷嶅崠琛屽け璐?', error);
        }
    }

    async loadVipData() {
        console.log('鍔犺浇VIP鏁版嵁');
        // 瀹炵幇VIP鏁版嵁鍔犺浇
    }

    async loadActivityData() {
        console.log('鍔犺浇娲诲姩鏁版嵁');
        // 瀹炵幇娲诲姩鏁版嵁鍔犺浇
    }

    async loadInventoryData() {
        console.log('鍔犺浇鑳屽寘鏁版嵁');
        if (window.gameManager) {
            await window.gameManager.loadInventory();
        }
    }

    async loadQuestsData() {
        console.log('鍔犺浇浠诲姟鏁版嵁');
        if (window.questUI?.init) {
            await window.questUI.init();
        }
    }

    async loadPetsData() {
        console.log('鍔犺浇瀹犵墿鏁版嵁');
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
        console.log('鍔犺浇鍟嗗煄鏁版嵁');
        if (window.gameManager) {
            await window.gameManager.loadShopItems();
            await window.gameManager.loadSkillShop();
        }
        // 鏇存柊鐏电煶浣欓鏄剧ず
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
        console.log('鍔犺浇鎴樻枟鏁版嵁');
        if (window.combatUI?.init) {
            window.combatUI.init();
        }
    }

    async loadGuildData() {
        console.log('鍔犺浇瀹楅棬鏁版嵁');
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
        console.log('鍔犺浇鎴愬氨鏁版嵁');
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
            console.error('鍔犺浇杩炴嫑鏁版嵁澶辫触:', error);
        }
    }

    async loadPetEvolutionData() {
        console.log('鍔犺浇瀹犵墿杩涘寲鏁版嵁');
        if (window.petEvolutionUI?.init) {
            await window.petEvolutionUI.init();
        }
    }

    async loadNarrativeData() {
        console.log('鍔犺浇鍙欎簨鏁版嵁');
        if (window.narrativeUI?.init) {
            await window.narrativeUI.init();
        }
    }

    async loadLoreData() {
        console.log('鍔犺浇浼犺鏁版嵁');
        if (window.loreUI?.init) {
            await window.loreUI.init();
        }
    }

    async loadMapData() {
        console.log('鍔犺浇鍦板浘鏁版嵁');
        if (window.mapUI?.init) {
            await window.mapUI.init();
        }
    }

    async loadCheckInData() {
        console.log('鍔犺浇绛惧埌鏁版嵁');
        if (window.checkinUI?.init) {
            await window.checkinUI.init();
        }
    }

    async loadAchievementsData() {
        console.log('鍔犺浇鎴愬氨鏁版嵁');
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
        // 浣跨敤authManager鐨剆howToast鏂规硶
        if (window.authManager && window.authManager.showToast) {
            window.authManager.showToast(message, type);
        } else {
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
    }
}

// 鍒涘缓妯″潡绠＄悊鍣ㄥ疄渚?
const moduleManager = new ModuleManager();

// 鍏ㄥ眬鍑芥暟
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

// 瀵煎嚭鍒板叏灞€
window.moduleManager = moduleManager;

// ==================== 妯″潡鍒囨崲杈呭姪鍑芥暟 ====================

// 鑳屽寘鏍囩鍒囨崲
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

// 鍟嗗煄鏍囩鍒囨崲
window.switchShopTab = function(tab) {
    document.querySelectorAll('#shop-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.shopTab === tab);
    });
    document.getElementById('shop-general-panel').style.display = (tab === 'general') ? '' : 'none';
    document.getElementById('shop-skill-panel').style.display = (tab === 'skill') ? '' : 'none';
};

// 鍒锋柊鍟嗗煄鍟嗗搧
window.refreshShopItems = async function() {
    if (window.gameManager) {
        await window.gameManager.loadShopItems();
        moduleManager.showToast('商城已刷新', 'success');
    }
};

// 浠诲姟鏍囩鍒囨崲
window.switchQuestTab = function(tab) {
    return window.questUI?.switchTab(tab);
};

window.claimQuest = async function(questId) {
    return window.questUI?.claimQuest(questId);
};

// ==================== 瀹楅棬绯荤粺杈呭姪鍑芥暟锛堝鎵樺埌妯″潡鍖栧疄鐜帮級 ====================

// ==================== 鎷嶅崠琛岀郴缁熻緟鍔╁嚱鏁?====================

window.switchAuctionTab = function(tab) {
    return window.auctionUI?.switchTab(tab);
};

window.loadAuctionItems = async function() {
    return window.auctionUI?.loadAuctionItems();
};

window.loadMyAuctionItems = async function(status = '') {
    return window.auctionUI?.loadMyAuctionItems(status);
};

window.buyAuctionItem = async function(auctionId) {
    return window.auctionUI?.buyAuctionItem(auctionId);
};

window.cancelAuctionItem = async function(auctionId) {
    return window.auctionUI?.cancelAuctionItem(auctionId);
};

// ==================== 瀹犵墿绯荤粺杈呭姪鍑芥暟锛堝鎵樺埌妯″潡鍖栧疄鐜帮級 ====================

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

// ==================== 鎶€鑳界郴缁熻緟鍔╁嚱鏁帮紙濮旀墭鍒版ā鍧楀寲瀹炵幇锛?====================

function getSkillsModule() {
    if (!window.skillsUI) {
        throw new Error('鎶€鑳芥ā鍧楀皻鏈垵濮嬪寲');
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

// ==================== 淇偧/鎸傛満杈呭姪鍑芥暟锛堝鎵樺埌妯″潡鍖栧疄鐜帮級 ====================

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

// ==================== 浠欑晫浜虹墿锛圢PC锛夎緟鍔╁嚱鏁帮紙濮旀墭鍒版ā鍧楀寲瀹炵幇锛?====================

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

// ==================== 浼犺鍥鹃壌杈呭姪鍑芥暟 ====================

window.switchLoreTab = function(tab) {
    return window.loreUI?.switchTab(tab);
};

window.loadLoreEntries = async function(filter = 'all') {
    return window.loreUI?.loadEntries(filter);
};

// ==================== 鎶€鑳借繛鎷涜緟鍔╁嚱鏁帮紙濮旀墭鍒版ā鍧楀寲瀹炵幇锛?====================

window.switchComboTab = function(tab) {
    return getSkillsModule().switchComboTab(tab);
};

window.loadCombos = async function(availableOnly = true) {
    return getSkillsModule().loadCombos(availableOnly);
};

// ==================== 瀹犵墿杩涘寲杈呭姪鍑芥暟 ====================

window.loadEvolutionInfo = async function() {
    return window.petEvolutionUI?.loadEvolutionInfo();
};

window.doEvolution = async function(playerPetId) {
    return window.petEvolutionUI?.doEvolution(playerPetId);
};

// ==================== 涓栫晫鍦板浘杈呭姪鍑芥暟锛堝鎵樺埌妯″潡鍖栧疄鐜帮級 ====================

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

// ==================== 姣忔棩绛惧埌杈呭姪鍑芥暟 ====================

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


