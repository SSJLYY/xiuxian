/**
 * 宠物模块 - UI渲染层
 */
import { petsService } from './PetsService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';

export class PetsUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadPets();
    }

    setupElements() {
        this.elements = {
            myPetsContainer: document.getElementById('myPetsContainer'),
            availablePetsContainer: document.getElementById('availablePetsContainer')
        };
    }

    bindEvents() {
        // 标签页切换
        document.querySelectorAll('[data-tab="pet"]').forEach(tab => {
            tab.addEventListener('click', (e) => this.switchTab(e.target.dataset.petTab));
        });
    }

    switchTab(tabName) {
        document.querySelectorAll('[data-tab="pet"]').forEach(tab => {
            tab.classList.toggle('active', tab.dataset.petTab === tabName);
        });

        if (tabName === 'my') {
            this.elements.myPetsContainer.style.display = 'block';
            this.elements.availablePetsContainer.style.display = 'none';
        } else {
            this.elements.myPetsContainer.style.display = 'none';
            this.elements.availablePetsContainer.style.display = 'block';
        }
    }

    async loadPets() {
        loading.show();
        try {
            await Promise.all([
                petsService.loadMyPets(),
                petsService.loadAvailablePets()
            ]);
            this.renderMyPets();
            this.renderAvailablePets();
        } catch (error) {
            toast.error('加载宠物失败');
        } finally {
            loading.hide();
        }
    }

    renderMyPets() {
        const container = this.elements.myPetsContainer;
        if (!container) return;

        if (petsService.myPets.length === 0) {
            container.innerHTML = '<p>暂无宠物</p>';
            return;
        }

        container.innerHTML = petsService.myPets.map(pet => `
            <div class="pet-card">
                <div class="pet-image">
                    <img src="${pet.image}" alt="${pet.name}">
                </div>
                <div class="pet-info">
                    <h4>${pet.name}</h4>
                    <p>等级: ${pet.level}</p>
                    <p>饱食度: ${pet.hunger}%</p>
                </div>
            </div>
        `).join('');
    }

    renderAvailablePets() {
        const container = this.elements.availablePetsContainer;
        if (!container) return;

        container.innerHTML = petsService.availablePets.map(pet => `
            <div class="pet-card">
                <div class="pet-image">
                    <img src="${pet.image}" alt="${pet.name}">
                </div>
                <div class="pet-info">
                    <h4>${pet.name}</h4>
                    <p>${pet.description}</p>
                </div>
            </div>
        `).join('');
    }
}

export const petsUI = new PetsUI();
