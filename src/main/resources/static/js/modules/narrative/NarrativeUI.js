/**
 * 叙事模块 - UI渲染层
 */
import { narrativeService } from './NarrativeService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { modal } from '../../components/Modal.js';

export class NarrativeUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadNarrativeData();
    }

    setupElements() {
        this.elements = {
            npcListContainer: document.getElementById('npcListContainer'),
            relationContainer: document.getElementById('relationContainer'),
            narrativeTabs: document.querySelectorAll('[data-tab="narrative"]')
        };
    }

    bindEvents() {
        this.elements.narrativeTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchTab(e.target.dataset.narrativeTab);
            });
        });
    }

    switchTab(tabName) {
        this.elements.narrativeTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.narrativeTab === tabName);
        });

        if (tabName === 'npcs') {
            this.elements.npcListContainer.style.display = 'block';
            this.elements.relationContainer.style.display = 'none';
        } else {
            this.elements.npcListContainer.style.display = 'none';
            this.elements.relationContainer.style.display = 'block';
        }
    }

    async loadNarrativeData() {
        loading.show();
        try {
            await Promise.all([
                narrativeService.getNpcList(),
                narrativeService.getNpcRelations()
            ]);
            this.renderNpcList();
            this.renderRelations();
        } catch (error) {
            toast.error('加载叙事数据失败');
        } finally {
            loading.hide();
        }
    }

    renderNpcList() {
        const container = this.elements.npcListContainer;
        if (!container) return;

        if (narrativeService.npcs.length === 0) {
            container.innerHTML = '<p>暂无NPC</p>';
            return;
        }

        container.innerHTML = `
            <div class="npc-list">
                ${narrativeService.npcs.map(npc => {
                    const relation = narrativeService.getRelationByNpcId(npc.id);
                    const relationLevel = relation?.relationLevel || 0;
                    const relationName = relation?.relationName || '陌生人';

                    return `
                        <div class="npc-card">
                            <div class="npc-avatar">
                                <img src="${npc.avatar || '/images/npcs/default.png'}" alt="${npc.name}">
                            </div>
                            <div class="npc-info">
                                <h4>${npc.name}</h4>
                                <p class="npc-location">位置: ${npc.location}</p>
                                <p class="npc-desc">${npc.description}</p>
                                <div class="npc-relation">
                                    <span class="relation-level">关系: ${relationName}</span>
                                    <div class="relation-bar">
                                        <div class="relation-fill" style="width: ${relationLevel * 10}%"></div>
                                    </div>
                                </div>
                            </div>
                            <div class="npc-actions">
                                <button class="btn btn-primary" data-action="interact" data-npc-id="${npc.id}">对话</button>
                                ${npc.availableQuests && npc.availableQuests.length > 0 ?
                                    `<button class="btn btn-info" data-action="quest" data-npc-id="${npc.id}">任务(${npc.availableQuests.length})</button>` : ''}
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;

        // 绑定事件
        container.querySelectorAll('[data-action="interact"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleInteract(e.target.dataset.npcId));
        });

        container.querySelectorAll('[data-action="quest"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.showQuestDialog(e.target.dataset.npcId));
        });
    }

    renderRelations() {
        const container = this.elements.relationContainer;
        if (!container) return;

        if (narrativeService.myRelations.length === 0) {
            container.innerHTML = '<p>暂无NPC关系</p>';
            return;
        }

        container.innerHTML = `
            <div class="relations-list">
                ${narrativeService.myRelations.map(rel => {
                    const npc = narrativeService.getNpcById(rel.npcId);
                    return `
                        <div class="relation-card">
                            <div class="relation-avatar">
                                <img src="${npc?.avatar || '/images/npcs/default.png'}" alt="${npc?.name}">
                            </div>
                            <div class="relation-info">
                                <h4>${npc?.name || '未知NPC'}</h4>
                                <div class="relation-level">
                                    <span>${rel.relationName}</span>
                                    <span>等级 ${rel.relationLevel}</span>
                                </div>
                                <div class="relation-progress">
                                    <span>亲密度: ${rel.intimacy}</span>
                                    <div class="intimacy-bar">
                                        <div class="intimacy-fill" style="width: ${rel.intimacy}%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    }

    async handleInteract(npcId) {
        loading.show();
        try {
            const result = await narrativeService.interactWithNpc(npcId);

            if (result.dialogue) {
                this.showDialogue(result.dialogue, result.npc);
            }
        } catch (error) {
            toast.error('交互失败');
        } finally {
            loading.hide();
        }
    }

    showDialogue(dialogue, npc) {
        const dialogueHtml = `
            <div class="dialogue-container">
                <div class="dialogue-avatar">
                    <img src="${npc.avatar}" alt="${npc.name}">
                </div>
                <div class="dialogue-content">
                    <div class="dialogue-name">${npc.name}</div>
                    <div class="dialogue-text">${dialogue.text}</div>
                    ${dialogue.options ? `
                        <div class="dialogue-options">
                            ${dialogue.options.map((option, index) => `
                                <button class="btn btn-option" data-option-index="${index}">${option.text}</button>
                            `).join('')}
                        </div>
                    ` : ''}
                </div>
            </div>
        `;

        modal.show({
            title: '对话',
            content: dialogueHtml,
            showCancel: false,
            confirmText: '关闭'
        });

        if (dialogue.options) {
            document.querySelectorAll('.btn-option').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const optionIndex = parseInt(e.target.dataset.optionIndex);
                    const option = dialogue.options[optionIndex];
                    // 处理选项
                    modal.hide();
                    if (option.nextDialogue) {
                        this.showDialogue(option.nextDialogue, npc);
                    }
                });
            });
        }
    }

    showQuestDialog(npcId) {
        const npc = narrativeService.getNpcById(npcId);
        if (!npc || !npc.availableQuests) return;

        const questHtml = `
            <div class="quest-dialog">
                <h3>可用任务</h3>
                <div class="quest-list">
                    ${npc.availableQuests.map(quest => `
                        <div class="quest-item">
                            <div class="quest-info">
                                <h4>${quest.name}</h4>
                                <p>${quest.description}</p>
                                <div class="quest-reward">奖励: ${quest.rewardDescription}</div>
                            </div>
                            <button class="btn btn-success" data-action="start-quest" data-quest-id="${quest.id}">接受</button>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;

        modal.show({
            title: `${npc.name} - 任务列表`,
            content: questHtml,
            showCancel: false,
            confirmText: '关闭'
        });

        document.querySelectorAll('[data-action="start-quest"]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.handleStartQuest(npcId, e.target.dataset.questId);
                modal.hide();
            });
        });
    }

    async handleStartQuest(npcId, questId) {
        loading.show();
        try {
            await narrativeService.startQuest(npcId, questId);
            await this.loadNarrativeData();
        } catch (error) {
            toast.error('接受任务失败');
        } finally {
            loading.hide();
        }
    }
}

export const narrativeUI = new NarrativeUI();
