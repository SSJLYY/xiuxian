/**
 * 宗门模块 - UI渲染层
 */
import { guildService } from './GuildService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { modal } from '../../components/Modal.js';

export class GuildUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadGuildData();
    }

    setupElements() {
        this.elements = {
            guildListContainer: document.getElementById('guildListContainer'),
            myGuildContainer: document.getElementById('myGuildContainer'),
            createGuildBtn: document.getElementById('createGuildBtn')
        };
    }

    bindEvents() {
        if (this.elements.createGuildBtn) {
            this.elements.createGuildBtn.addEventListener('click', () => this.showCreateModal());
        }
    }

    async loadGuildData() {
        loading.show();
        try {
            await Promise.all([
                guildService.loadGuildList(),
                guildService.loadMyGuild()
            ]);
            this.renderGuildList();
            this.renderMyGuild();
        } catch (error) {
            toast.error('加载数据失败');
        } finally {
            loading.hide();
        }
    }

    renderGuildList() {
        const container = this.elements.guildListContainer;
        if (!container) return;

        if (guildService.guildList.length === 0) {
            container.innerHTML = '<p>暂无宗门</p>';
            return;
        }

        container.innerHTML = guildService.guildList.map(guild => `
            <div class="guild-card">
                <div class="guild-info">
                    <h4>${guild.name}</h4>
                    <p>${guild.description}</p>
                    <p>成员数: ${guild.memberCount}</p>
                </div>
                <button class="btn btn-primary" data-action="join" data-guild-id="${guild.id}">加入</button>
            </div>
        `).join('');

        container.querySelectorAll('[data-action="join"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleJoin(e.target.dataset.guildId));
        });
    }

    renderMyGuild() {
        const container = this.elements.myGuildContainer;
        if (!container) return;

        if (!guildService.myGuild) {
            container.innerHTML = '<p>您还未加入宗门</p>';
            return;
        }

        container.innerHTML = `
            <div class="my-guild">
                <h3>${guildService.myGuild.name}</h3>
                <p>${guildService.myGuild.description}</p>
                <p>成员数: ${guildService.myGuild.memberCount}</p>
                <button class="btn btn-danger" id="leaveGuildBtn">退出宗门</button>
            </div>
        `;

        document.getElementById('leaveGuildBtn')?.addEventListener('click', () => this.handleLeave());
    }

    showCreateModal() {
        const modalHtml = `
            <form id="createGuildForm">
                <div class="form-group">
                    <label>宗门名称</label>
                    <input type="text" name="name" required>
                </div>
                <div class="form-group">
                    <label>宗门描述</label>
                    <textarea name="description"></textarea>
                </div>
                <button type="submit" class="btn btn-primary">创建</button>
            </form>
        `;

        modal.show({
            title: '创建宗门',
            content: modalHtml,
            onConfirm: () => {
                document.getElementById('createGuildForm').dispatchEvent(new Event('submit'));
            }
        });

        document.getElementById('createGuildForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(e.target);
            await this.handleCreate(
                formData.get('name'),
                formData.get('description')
            );
            modal.hide();
        });
    }

    async handleCreate(name, description) {
        loading.show();
        try {
            await guildService.createGuild(name, description);
            await this.loadGuildData();
        } catch (error) {
            toast.error('创建失败');
        } finally {
            loading.hide();
        }
    }

    async handleJoin(guildId) {
        loading.show();
        try {
            await guildService.joinGuild(guildId);
        } catch (error) {
            toast.error('申请失败');
        } finally {
            loading.hide();
        }
    }

    async handleLeave() {
        if (!confirm('确定要退出宗门吗?')) return;

        loading.show();
        try {
            await guildService.leaveGuild();
            await this.loadGuildData();
        } catch (error) {
            toast.error('退出失败');
        } finally {
            loading.hide();
        }
    }
}

export const guildUI = new GuildUI();
