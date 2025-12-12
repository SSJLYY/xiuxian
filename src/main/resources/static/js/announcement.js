// 公告系统脚本

let currentAnnouncements = [];
let currentAnnouncementIndex = 0;
let announcementInterval = null;

/**
 * 加载公告
 */
async function loadAnnouncements() {
    try {
        const response = await apiCall('/api/announcements/active', 'GET');
        
        if (response.success && response.data && response.data.length > 0) {
            currentAnnouncements = response.data;
            currentAnnouncementIndex = 0;
            showAnnouncement();
            
            // 如果有多个公告，每30秒轮换一次
            if (currentAnnouncements.length > 1) {
                if (announcementInterval) {
                    clearInterval(announcementInterval);
                }
                announcementInterval = setInterval(() => {
                    currentAnnouncementIndex = (currentAnnouncementIndex + 1) % currentAnnouncements.length;
                    showAnnouncement();
                }, 30000);
            }
        }
    } catch (error) {
        console.error('加载公告失败:', error);
    }
}

/**
 * 显示当前公告
 */
function showAnnouncement() {
    if (currentAnnouncements.length === 0) return;
    
    const announcement = currentAnnouncements[currentAnnouncementIndex];
    const banner = document.getElementById('announcementBanner');
    const text = document.getElementById('announcementText');
    
    if (banner && text) {
        text.innerHTML = `<strong>${escapeHtml(announcement.title)}</strong>: ${escapeHtml(announcement.content)}`;
        banner.style.display = 'block';
    }
}

/**
 * 关闭公告
 */
function closeAnnouncement() {
    const banner = document.getElementById('announcementBanner');
    if (banner) {
        banner.style.display = 'none';
    }
    
    if (announcementInterval) {
        clearInterval(announcementInterval);
        announcementInterval = null;
    }
}

/**
 * HTML转义
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 页面加载时加载公告
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadAnnouncements);
} else {
    loadAnnouncements();
}
