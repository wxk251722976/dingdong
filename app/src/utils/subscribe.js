/**
 * 微信小程序订阅消息工具
 * 用于请求用户订阅消息授权
 */

// 订阅消息模板ID - 需要与后端配置保持一致
// 在微信公众平台获取实际的模板ID后替换这里的值
const TEMPLATE_IDS = {
    // 叮咚提醒模板 - 发送给被叮咚者
    REMIND_CHECKIN: 'YOUR_REMIND_TEMPLATE_ID',
    // 打卡完成通知模板 - 发送给监督者
    CHECKIN_COMPLETE: 'YOUR_COMPLETE_TEMPLATE_ID',
    // 漏打卡通知模板 - 发送给监督者
    MISSED_CHECKIN: 'YOUR_MISSED_TEMPLATE_ID'
};

/**
 * 请求订阅消息授权
 * @param {Array<string>} tmplIds - 需要订阅的模板ID数组
 * @returns {Promise<Object>} - 返回订阅结果
 */
export function requestSubscribeMessage(tmplIds) {
    return new Promise((resolve, reject) => {
        // #ifdef MP-WEIXIN
        uni.requestSubscribeMessage({
            tmplIds: tmplIds,
            success(res) {
                console.log('订阅消息授权结果:', res);
                resolve(res);
            },
            fail(err) {
                console.error('订阅消息授权失败:', err);
                // 如果用户拒绝或关闭，不视为错误
                if (err.errCode === 20004) {
                    // 用户关闭了订阅消息弹窗
                    resolve({ cancelled: true });
                } else {
                    reject(err);
                }
            }
        });
        // #endif

        // 非微信小程序环境直接resolve
        // #ifndef MP-WEIXIN
        resolve({ notSupported: true });
        // #endif
    });
}

/**
 * 请求监督者需要的订阅消息授权
 * 包括：打卡完成通知、漏打卡通知
 */
export function requestSupervisorSubscribe() {
    const tmplIds = [
        TEMPLATE_IDS.CHECKIN_COMPLETE,
        TEMPLATE_IDS.MISSED_CHECKIN
    ].filter(id => id && !id.startsWith('YOUR_'));

    if (tmplIds.length === 0) {
        console.warn('监督者订阅模板ID未配置');
        return Promise.resolve({ notConfigured: true });
    }

    return requestSubscribeMessage(tmplIds);
}

/**
 * 请求被监督者需要的订阅消息授权
 * 包括：叮咚提醒通知
 */
export function requestSupervisedSubscribe() {
    const tmplIds = [
        TEMPLATE_IDS.REMIND_CHECKIN
    ].filter(id => id && !id.startsWith('YOUR_'));

    if (tmplIds.length === 0) {
        console.warn('被监督者订阅模板ID未配置');
        return Promise.resolve({ notConfigured: true });
    }

    return requestSubscribeMessage(tmplIds);
}

/**
 * 请求所有订阅消息授权
 * 一次性请求所有模板（最多3个）
 */
export function requestAllSubscribe() {
    const tmplIds = Object.values(TEMPLATE_IDS)
        .filter(id => id && !id.startsWith('YOUR_'));

    if (tmplIds.length === 0) {
        console.warn('订阅模板ID未配置');
        return Promise.resolve({ notConfigured: true });
    }

    return requestSubscribeMessage(tmplIds);
}

/**
 * 检查是否需要引导用户开启订阅消息
 * 在设置页检查用户的订阅状态
 */
export function checkSubscriptionSetting() {
    return new Promise((resolve) => {
        // #ifdef MP-WEIXIN
        uni.getSetting({
            withSubscriptions: true,
            success(res) {
                console.log('订阅消息设置:', res.subscriptionsSetting);
                resolve(res.subscriptionsSetting || {});
            },
            fail() {
                resolve({});
            }
        });
        // #endif

        // #ifndef MP-WEIXIN
        resolve({});
        // #endif
    });
}

/**
 * 显示订阅引导提示
 * 当用户拒绝订阅后，引导用户去设置页开启
 */
export function showSubscribeGuide() {
    uni.showModal({
        title: '开启消息通知',
        content: '为了及时收到叮咚提醒和打卡通知，请在设置中开启订阅消息',
        confirmText: '去设置',
        success(res) {
            if (res.confirm) {
                uni.openSetting({
                    success(settingRes) {
                        console.log('设置结果:', settingRes);
                    }
                });
            }
        }
    });
}

export default {
    TEMPLATE_IDS,
    requestSubscribeMessage,
    requestSupervisorSubscribe,
    requestSupervisedSubscribe,
    requestAllSubscribe,
    checkSubscriptionSetting,
    showSubscribeGuide
};
