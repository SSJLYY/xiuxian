/**
 * 玩家模块入口文件
 * 导出模块的所有公共接口
 */

import { playerService } from './PlayerService.js';
import { playerUI } from './PlayerUI.js';

// 模块导出
export {
    playerService,
    playerUI
};

// 默认导出
export default {
    service: playerService,
    ui: playerUI
};

// 如果使用传统script标签加载,则挂载到全局对象
if (typeof window !== 'undefined') {
    window.playerModule = {
        service: playerService,
        ui: playerUI
    };
}
