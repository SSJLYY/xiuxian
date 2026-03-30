/**
 * 技能模块 - UI渲染层
 */
import { skillsService } from './SkillsService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';

export class SkillsUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadSkills();
    }

    setupElements() {
        this.elements = {
            mySkillsContainer: document.getElementById('mySkillsContainer'),
            availableSkillsContainer: document.getElementById('availableSkillsContainer'),
            skillTabs: document.querySelectorAll('[data-tab="skill"]')
        };
    }

    bindEvents() {
        this.elements.skillTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchTab(e.target.dataset.skillTab);
            });
        });
    }

    switchTab(tabName) {
        // 更新标签页状态
        this.elements.skillTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.skillTab === tabName);
        });

        // 显示对应内容
        if (tabName === 'my') {
            this.elements.mySkillsContainer.style.display = 'block';
            this.elements.availableSkillsContainer.style.display = 'none';
        } else {
            this.elements.mySkillsContainer.style.display = 'none';
            this.elements.availableSkillsContainer.style.display = 'block';
        }
    }

    async loadSkills() {
        loading.show();
        try {
            await Promise.all([
                skillsService.loadMySkills(),
                skillsService.loadAvailableSkills()
            ]);
            this.renderMySkills();
            this.renderAvailableSkills();
        } catch (error) {
            toast.error('加载技能失败');
        } finally {
            loading.hide();
        }
    }

    renderMySkills() {
        const container = this.elements.mySkillsContainer;
        if (!container) return;

        if (skillsService.mySkills.length === 0) {
            container.innerHTML = '<p>暂无技能</p>';
            return;
        }

        container.innerHTML = skillsService.mySkills.map(skill => `
            <div class="skill-card ${skill.rarity}">
                <div class="skill-icon">
                    <img src="${skill.icon || '/images/skills/default.png'}" alt="${skill.name}">
                </div>
                <div class="skill-info">
                    <h4>${skill.name}</h4>
                    <p>等级: ${skill.level}</p>
                    <p>${skill.description}</p>
                </div>
                <button class="btn btn-primary" data-action="upgrade" data-skill-id="${skill.id}">升级</button>
            </div>
        `).join('');

        // 绑定升级按钮
        container.querySelectorAll('[data-action="upgrade"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleUpgrade(e.target.dataset.skillId));
        });
    }

    renderAvailableSkills() {
        const container = this.elements.availableSkillsContainer;
        if (!container) return;

        if (skillsService.availableSkills.length === 0) {
            container.innerHTML = '<p>暂无可学习的技能</p>';
            return;
        }

        container.innerHTML = skillsService.availableSkills.map(skill => `
            <div class="skill-card ${skill.rarity}">
                <div class="skill-icon">
                    <img src="${skill.icon || '/images/skills/default.png'}" alt="${skill.name}">
                </div>
                <div class="skill-info">
                    <h4>${skill.name}</h4>
                    <p>需求等级: ${skill.requiredLevel}</p>
                    <p>消耗: ${skill.cost} 灵石</p>
                    <p>${skill.description}</p>
                </div>
                <button class="btn btn-success" data-action="learn" data-skill-id="${skill.id}">学习</button>
            </div>
        `).join('');

        // 绑定学习按钮
        container.querySelectorAll('[data-action="learn"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleLearn(e.target.dataset.skillId));
        });
    }

    async handleLearn(skillId) {
        loading.show();
        try {
            await skillsService.learnSkill(skillId);
            await this.loadSkills();
        } catch (error) {
            toast.error('学习失败');
        } finally {
            loading.hide();
        }
    }

    async handleUpgrade(skillId) {
        loading.show();
        try {
            await skillsService.upgradeSkill(skillId);
            await this.loadSkills();
        } catch (error) {
            toast.error('升级失败');
        } finally {
            loading.hide();
        }
    }
}

export const skillsUI = new SkillsUI();
