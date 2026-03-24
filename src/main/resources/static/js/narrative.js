/**
 * 叙事系统 - NPC对话与传说图鉴
 * narrative.js
 */

const narrativeAPI = {
    // 获取NPC列表
    async getNpcList() {
        return api.get('/npc/list');
    },

    // 获取NPC详情
    async getNpcDetail(npcId) {
        return api.get(`/npc/${npcId}`);
    },

    // 获取NPC关系列表
    async getNpcRelations() {
        return api.get('/npc/relations');
    },

    // 获取NPC可用对话列表
    async getAvailableDialogues(npcId) {
        return api.get(`/dialogue/available/${npcId}`);
    },

    // 开始/继续对话
    async startDialogue(dialogueKey) {
        return api.post('/dialogue/start', { dialogueKey });
    },

    // 做出选择
    async makeChoice(dialogueKey, choiceNodeKey) {
        return api.post('/dialogue/choice', { dialogueKey, choiceNodeKey });
    },

    // 获取传说进度
    async getLoreProgress() {
        return api.get('/lore/progress');
    },

    // 获取所有传说条目
    async getLoreEntries() {
        return api.get('/lore/entries');
    },

    // 获取已发现传说
    async getDiscoveredLore() {
        return api.get('/lore/discovered');
    },

    // 检查离线事件
    async checkOfflineEvents() {
        return api.get('/narrative/offline-events');
    },

    // 获取玩家flags
    async getPlayerFlags() {
        return api.get('/narrative/flags');
    }
};

// ==================== 对话UI组件 ====================

class DialogueUI {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.currentDialogueKey = null;
        this.isTyping = false;
        this.typingTimeout = null;
    }

    // 渲染NPC列表
    renderNpcList(npcs) {
        if (!this.container) return;

        let html = `
            <div class="narrative-panel">
                <div class="narrative-header">
                    <h2><i class="fas fa-users"></i> 仙界人物</h2>
                    <p class="narrative-subtitle">与苍玄界的修士们交流</p>
                </div>
                <div class="npc-grid">
        `;

        const factionIcons = {
            '天剑宗': 'fa-bolt',
            '万法阁': 'fa-book',
            '幽冥殿': 'fa-skull',
            '灵兽山': 'fa-paw',
            '散修联盟': 'fa-hat-wizard'
        };

        const factionColors = {
            '天剑宗': '#d4af37',
            '万法阁': '#7fffd4',
            '幽冥殿': '#8b5cf6',
            '灵兽山': '#10b981',
            '散修联盟': '#f59e0b'
        };

        npcs.forEach(npc => {
            const icon = factionIcons[npc.faction] || 'fa-user';
            const color = factionColors[npc.faction] || '#e8e8e8';
            html += `
                <div class="npc-card" onclick="dialogueUI.openNpc(${npc.id})" style="--npc-color: ${color}">
                    <div class="npc-avatar" style="border-color: ${color}">
                        <i class="fas ${icon}" style="color: ${color}"></i>
                    </div>
                    <div class="npc-info">
                        <div class="npc-name">${npc.name}</div>
                        <div class="npc-title">${npc.title || ''}</div>
                        <div class="npc-faction" style="color: ${color}">${npc.faction || ''}</div>
                    </div>
                    <i class="fas fa-chevron-right npc-arrow"></i>
                </div>
            `;
        });

        html += `
                </div>
            </div>
        `;

        this.container.innerHTML = html;
    }

    // 打开NPC详情
    async openNpc(npcId) {
        const result = await narrativeAPI.getNpcDetail(npcId);
        if (!result.success) {
            showNotification(result.message, 'error');
            return;
        }

        const detail = result.data;
        const relationBar = this.buildRelationBar(detail.affinity || 0);

        let html = `
            <div class="narrative-panel">
                <button class="back-btn" onclick="dialogueUI.showNpcList()">
                    <i class="fas fa-arrow-left"></i> 返回人物列表
                </button>
                
                <div class="npc-detail-header">
                    <div class="npc-detail-avatar">
                        <i class="fas fa-user-circle" style="font-size: 4rem; color: var(--accent-gold)"></i>
                    </div>
                    <div class="npc-detail-info">
                        <h2>${detail.name}</h2>
                        <p class="npc-detail-title">${detail.title || ''}</p>
                        <p class="npc-detail-faction">${detail.faction || ''}</p>
                    </div>
                </div>

                ${relationBar}

                <div class="npc-description">
                    <p>${detail.description || '暂无描述'}</p>
                </div>

                ${detail.dailyDialogue ? `
                <div class="npc-daily-dialogue">
                    <div class="dialogue-bubble npc-bubble">
                        <span class="dialogue-speaker">${detail.name}</span>
                        <p>${detail.dailyDialogue}</p>
                    </div>
                </div>
                ` : ''}

                <div class="npc-dialogue-list">
                    <h3><i class="fas fa-comments"></i> 对话选项</h3>
                    <div id="npcDialogueList" class="dialogue-options">
                        <div class="loading-spinner"><div class="spinner"></div><p>加载中...</p></div>
                    </div>
                </div>
            </div>
        `;

        this.container.innerHTML = html;

        // 加载可用对话
        await this.loadDialogues(npcId);
    }

    // 构建好感度条
    buildRelationBar(affinity) {
        const level = this.getRelationLevel(affinity);
        const percent = Math.max(0, Math.min(100, affinity + 100)) / 2; // -100~100 -> 0~100%
        return `
            <div class="relation-bar-container">
                <div class="relation-info">
                    <span class="relation-level">${level}</span>
                    <span class="relation-value">${affinity}</span>
                </div>
                <div class="relation-bar">
                    <div class="relation-fill" style="width: ${percent}%"></div>
                </div>
            </div>
        `;
    }

    getRelationLevel(affinity) {
        if (affinity >= 81) return '至交';
        if (affinity >= 61) return '信任';
        if (affinity >= 41) return '熟悉';
        if (affinity >= 21) return '认识';
        return '陌生';
    }

    // 加载NPC可用对话
    async loadDialogues(npcId) {
        const result = await narrativeAPI.getAvailableDialogues(npcId);
        const listContainer = document.getElementById('npcDialogueList');
        if (!listContainer) return;

        if (!result.success || !result.data || result.data.length === 0) {
            listContainer.innerHTML = '<p class="no-dialogue">暂无可用对话</p>';
            return;
        }

        let html = '';
        result.data.forEach(dialogue => {
            html += `
                <button class="dialogue-start-btn" onclick="dialogueUI.startDialogue('${dialogue.dialogueKey}')">
                    <i class="fas fa-comment-dots"></i>
                    <span>${dialogue.title || dialogue.dialogueKey}</span>
                    <span class="dialogue-scene">${dialogue.scene || ''}</span>
                </button>
            `;
        });

        listContainer.innerHTML = html;
    }

    // 返回NPC列表
    async showNpcList() {
        const result = await narrativeAPI.getNpcList();
        if (result.success && result.data) {
            this.renderNpcList(result.data);
        }
    }

    // 开始对话
    async startDialogue(dialogueKey) {
        const result = await narrativeAPI.startDialogue(dialogueKey);
        if (!result.success) {
            showNotification(result.message, 'error');
            return;
        }

        this.currentDialogueKey = dialogueKey;
        this.renderDialogueScene(result.data);
    }

    // 渲染对话场景
    renderDialogueScene(scene) {
        if (!this.container) return;

        if (scene.completed) {
            let html = `
                <div class="narrative-panel dialogue-scene">
                    <div class="dialogue-complete">
                        <i class="fas fa-check-circle" style="font-size: 3rem; color: var(--accent-gold)"></i>
                        <h3>对话结束</h3>
                        <button class="btn btn-primary" onclick="dialogueUI.closeDialogue('${scene.dialogueKey}')">
                            <i class="fas fa-arrow-left"></i> 返回
                        </button>
                    </div>
                </div>
            `;
            this.container.innerHTML = html;
            return;
        }

        const line = scene.currentLine;
        const isNpc = line.speaker && line.speaker !== '玩家' && line.speaker !== '旁白';

        let choicesHtml = '';
        if (scene.choices && scene.choices.length > 0) {
            choicesHtml = '<div class="dialogue-choices">';
            scene.choices.forEach((choice, idx) => {
                choicesHtml += `
                    <button class="choice-btn" onclick="dialogueUI.makeChoice('${choice.nodeKey}')">
                        <span class="choice-number">${idx + 1}.</span>
                        ${choice.text}
                    </button>
                `;
            });
            choicesHtml += '</div>';
        } else if (line.nextNodeKey) {
            choicesHtml = `
                <div class="dialogue-choices">
                    <button class="choice-btn" onclick="dialogueUI.makeChoice('continue')">
                        <i class="fas fa-arrow-right"></i> 继续
                    </button>
                </div>
            `;
        }

        let html = `
            <div class="narrative-panel dialogue-scene">
                <div class="dialogue-scene-header">
                    <span class="scene-location"><i class="fas fa-map-marker-alt"></i> ${scene.scene || ''}</span>
                    <span class="scene-npc"><i class="fas fa-user"></i> ${scene.npcName || ''}</span>
                </div>
                
                <div class="dialogue-area">
                    <div class="dialogue-bubble ${isNpc ? 'npc-bubble' : 'player-bubble'}">
                        <span class="dialogue-speaker">${line.speaker || ''}</span>
                        <p class="dialogue-text" id="dialogueText">${line.text}</p>
                    </div>
                </div>

                ${choicesHtml}
            </div>
        `;

        this.container.innerHTML = html;

        // 打字机效果
        this.typeText(line.text, 'dialogueText');
    }

    // 打字机效果
    typeText(text, elementId) {
        const el = document.getElementById(elementId);
        if (!el) return;

        this.isTyping = true;
        el.textContent = '';
        let i = 0;

        const type = () => {
            if (i < text.length && this.isTyping) {
                el.textContent += text.charAt(i);
                i++;
                this.typingTimeout = setTimeout(type, 30);
            } else {
                this.isTyping = false;
            }
        };
        type();

        // 点击跳过打字
        el.onclick = () => {
            if (this.isTyping) {
                this.isTyping = false;
                clearTimeout(this.typingTimeout);
                el.textContent = text;
            }
        };
    }

    // 做出选择
    async makeChoice(choiceNodeKey) {
        if (this.isTyping) return;
        if (!this.currentDialogueKey) return;

        const result = await narrativeAPI.makeChoice(this.currentDialogueKey, choiceNodeKey);
        if (!result.success) {
            showNotification(result.message, 'error');
            return;
        }

        this.renderDialogueScene(result.data);
    }

    // 关闭对话
    async closeDialogue(dialogueKey) {
        this.currentDialogueKey = null;
        this.showNpcList();
    }
}

// ==================== 传说图鉴UI ====================

class LoreUI {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
    }

    async init() {
        const progressResult = await narrativeAPI.getLoreProgress();
        const entriesResult = await narrativeAPI.getLoreEntries();

        if (!progressResult.success || !entriesResult.success) {
            this.container.innerHTML = '<p>加载传说数据失败</p>';
            return;
        }

        this.renderLoreBook(progressResult.data, entriesResult.data);
    }

    renderLoreBook(progress, entries) {
        const totalPercent = progress.totalCount > 0 
            ? Math.round(progress.discoveredCount / progress.totalCount * 100) : 0;

        let html = `
            <div class="narrative-panel">
                <div class="narrative-header">
                    <h2><i class="fas fa-book-open"></i> 苍玄志异</h2>
                    <p class="narrative-subtitle">传说图鉴 — 记录苍玄界的故事与秘密</p>
                </div>

                <!-- 进度总览 -->
                <div class="lore-progress-overview">
                    <div class="lore-progress-ring">
                        <div class="progress-circle" style="--percent: ${totalPercent}">
                            <span class="progress-text">${totalPercent}%</span>
                        </div>
                    </div>
                    <div class="lore-stats">
                        <div class="lore-stat">
                            <span class="stat-label">已发现</span>
                            <span class="stat-value">${progress.discoveredCount}/${progress.totalCount}</span>
                        </div>
                    </div>
                </div>

                <!-- 分层进度 -->
                <div class="lore-layer-progress">
                    <div class="lore-layer">
                        <div class="layer-header">
                            <span class="layer-name surface">表面传说</span>
                            <span class="layer-count">${progress.surfaceDiscovered}/${progress.surfaceTotal}</span>
                        </div>
                        <div class="layer-bar"><div class="layer-fill surface-fill" style="width: ${progress.surfaceTotal > 0 ? progress.surfaceDiscovered / progress.surfaceTotal * 100 : 0}%"></div></div>
                    </div>
                    <div class="lore-layer">
                        <div class="layer-header">
                            <span class="layer-name engaged">探索传说</span>
                            <span class="layer-count">${progress.engagedDiscovered}/${progress.engagedTotal}</span>
                        </div>
                        <div class="layer-bar"><div class="layer-fill engaged-fill" style="width: ${progress.engagedTotal > 0 ? progress.engagedDiscovered / progress.engagedTotal * 100 : 0}%"></div></div>
                    </div>
                    <div class="lore-layer">
                        <div class="layer-header">
                            <span class="layer-name deep">深层传说</span>
                            <span class="layer-count">${progress.deepDiscovered}/${progress.deepTotal}</span>
                        </div>
                        <div class="layer-bar"><div class="layer-fill deep-fill" style="width: ${progress.deepTotal > 0 ? progress.deepDiscovered / progress.deepTotal * 100 : 0}%"></div></div>
                    </div>
                </div>

                <!-- 传说条目列表 -->
                <div class="lore-entries">
                    <h3>传说条目</h3>
                    <div class="lore-list">
        `;

        entries.forEach(entry => {
            const layerClass = entry.discovered ? `lore-${entry.loreLayer}` : 'lore-locked';
            html += `
                <div class="lore-entry ${layerClass}" onclick="loreUI.toggleLoreEntry(this)">
                    <div class="lore-entry-header">
                        <span class="lore-icon">${entry.discovered ? '<i class="fas fa-scroll"></i>' : '<i class="fas fa-question"></i>'}</span>
                        <div class="lore-entry-info">
                            <span class="lore-title">${entry.title}</span>
                            <span class="lore-meta">${entry.category || ''} · ${entry.loreLayer}</span>
                        </div>
                        <i class="fas fa-chevron-down lore-toggle"></i>
                    </div>
                    <div class="lore-entry-content" style="display: none;">
                        <p>${entry.content}</p>
                        ${entry.discovered ? `<span class="lore-source">${entry.discoverCondition || ''}</span>` : ''}
                    </div>
                </div>
            `;
        });

        html += `
                    </div>
                </div>
            </div>
        `;

        this.container.innerHTML = html;
    }

    toggleLoreEntry(el) {
        const content = el.querySelector('.lore-entry-content');
        const toggle = el.querySelector('.lore-toggle');
        if (content.style.display === 'none') {
            content.style.display = 'block';
            toggle.style.transform = 'rotate(180deg)';
        } else {
            content.style.display = 'none';
            toggle.style.transform = 'rotate(0deg)';
        }
    }
}

// 创建全局实例
const dialogueUI = new DialogueUI('narrativeContent');
const loreUI = new LoreUI('loreContent');

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', () => {
    // 如果页面有对应容器，自动加载
    const narrativeModule = document.getElementById('narrativeContent');
    if (narrativeModule) {
        dialogueUI.showNpcList();
    }

    const loreModule = document.getElementById('loreContent');
    if (loreModule) {
        loreUI.init();
    }

    // 检查离线事件
    const token = localStorage.getItem('authToken');
    if (token) {
        checkOfflineEvents();
    }
});

// 离线事件检查
async function checkOfflineEvents() {
    const result = await narrativeAPI.checkOfflineEvents();
    if (!result.success || !result.data || result.data.length === 0) return;

    result.data.forEach(event => {
        showOfflineEventModal(event);
    });
}

// 离线事件弹窗
function showOfflineEventModal(event) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
        <div class="offline-event-modal">
            <div class="event-header">
                <i class="fas fa-star" style="color: var(--accent-gold)"></i>
                <h3>${event.title || '奇遇'}</h3>
            </div>
            <div class="event-narrative">
                <p>${event.narrative}</p>
            </div>
            ${event.rewards && event.rewards.length > 0 ? `
                <div class="event-rewards">
                    ${event.rewards.map(r => `<span class="reward-tag"><i class="fas fa-gift"></i> ${r}</span>`).join('')}
                </div>
            ` : ''}
            <button class="btn btn-primary" onclick="this.closest('.modal-overlay').remove()">
                <i class="fas fa-check"></i> 知道了
            </button>
        </div>
    `;
    document.body.appendChild(overlay);
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) overlay.remove();
    });
}
