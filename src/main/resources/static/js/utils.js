// ==================== 全局工具函数 ====================

/**
 * HTML 转义工具函数 — 防止 XSS
 * 所有 innerHTML 拼接用户可控数据时必须使用此函数
 */
function escapeHtml(text) {
    if (text == null) return '';
    const str = String(text);
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return str.replace(/[&<>"']/g, c => map[c]);
}
