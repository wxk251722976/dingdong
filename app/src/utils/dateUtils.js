/**
 * 时间格式化工具
 * 统一处理后端返回的时间戳（毫秒）
 */

/**
 * 格式化时间戳为日期字符串
 * @param {number} timestamp - 毫秒时间戳
 * @param {string} format - 格式化类型
 *   - 'date': 2026-01-15
 *   - 'time': 14:30
 *   - 'datetime': 2026-01-15 14:30
 *   - 'full': 2026-01-15 14:30:00
 *   - 'relative': 今天/昨天/2026-01-15
 *   - 'chinese': 1月15日 14:30
 * @returns {string}
 */
export function formatTimestamp(timestamp, format = 'datetime') {
    if (!timestamp) return '';

    const date = new Date(timestamp);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    switch (format) {
        case 'date':
            return `${year}-${month}-${day}`;
        case 'time':
            return `${hours}:${minutes}`;
        case 'datetime':
            return `${year}-${month}-${day} ${hours}:${minutes}`;
        case 'full':
            return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        case 'relative':
            return formatRelativeDate(date);
        case 'chinese':
            return `${parseInt(month)}月${parseInt(day)}日 ${hours}:${minutes}`;
        default:
            return `${year}-${month}-${day} ${hours}:${minutes}`;
    }
}

/**
 * 格式化为相对日期（今天/昨天/具体日期）
 */
function formatRelativeDate(date) {
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    const dateStr = date.toDateString();

    if (dateStr === today.toDateString()) {
        return '今天';
    } else if (dateStr === yesterday.toDateString()) {
        return '昨天';
    } else {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        // 如果是今年，不显示年份
        if (year === today.getFullYear()) {
            return `${month}-${day}`;
        }
        return `${year}-${month}-${day}`;
    }
}

/**
 * 格式化为相对时间（几分钟前/几小时前/几天前）
 * @param {number} timestamp - 毫秒时间戳
 * @returns {string}
 */
export function formatTimeAgo(timestamp) {
    if (!timestamp) return '';

    const now = Date.now();
    const diff = now - timestamp;

    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (minutes < 1) {
        return '刚刚';
    } else if (minutes < 60) {
        return `${minutes}分钟前`;
    } else if (hours < 24) {
        return `${hours}小时前`;
    } else if (days < 7) {
        return `${days}天前`;
    } else {
        return formatTimestamp(timestamp, 'date');
    }
}

/**
 * 获取今天的日期字符串
 * @returns {string} 2026-01-15
 */
export function getTodayStr() {
    return formatTimestamp(Date.now(), 'date');
}

/**
 * 判断时间戳是否是今天
 * @param {number} timestamp - 毫秒时间戳
 * @returns {boolean}
 */
export function isToday(timestamp) {
    if (!timestamp) return false;
    return new Date(timestamp).toDateString() === new Date().toDateString();
}

/**
 * 判断时间戳是否已过期（早于当前时间）
 * @param {number} timestamp - 毫秒时间戳
 * @returns {boolean}
 */
export function isExpired(timestamp) {
    if (!timestamp) return false;
    return timestamp < Date.now();
}

export default {
    formatTimestamp,
    formatTimeAgo,
    getTodayStr,
    isToday,
    isExpired
};
