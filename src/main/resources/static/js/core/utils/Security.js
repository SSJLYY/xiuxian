/**
 * XSS防护工具函数
 * 提供HTML转义功能,防止XSS攻击
 */

/**
 * 转义HTML特殊字符
 * @param {string} text - 需要转义的文本
 * @returns {string} 转义后的安全文本
 */
function escapeHtml(text) {
    if (text == null) return '';
    const str = String(text);
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return str.replace(/[&<>"']/g, c => map[c]);
}

/**
 * 转义URL参数
 * @param {string} text - 需要转义的文本
 * @returns {string} 转义后的安全URL参数
 */
function escapeUrl(text) {
    if (text == null) return '';
    return encodeURIComponent(String(text));
}

/**
 * 安全设置元素文本内容
 * @param {HTMLElement} element - 目标元素
 * @param {string} text - 要设置的文本
 */
function safeSetText(element, text) {
    element.textContent = text || '';
}

/**
 * 安全设置元素HTML内容(需要确认安全)
 * @param {HTMLElement} element - 目标元素
 * @param {string} html - 要设置的HTML
 * @param {boolean} isTrusted - 是否信任该HTML内容
 */
function safeSetHtml(element, html, isTrusted = false) {
    if (isTrusted) {
        element.innerHTML = html;
    } else {
        console.warn('尝试设置未受信任的HTML内容,使用textContent代替');
        element.textContent = html;
    }
}

export {
    escapeHtml,
    escapeUrl,
    safeSetText,
    safeSetHtml
};

// 导出工具函数(如果使用模块化)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        escapeHtml,
        escapeUrl,
        safeSetText,
        safeSetHtml
    };
}
