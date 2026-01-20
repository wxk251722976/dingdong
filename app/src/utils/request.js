// 从环境变量读取 API 地址（自动区分开发/生产）
// 开发: .env.development -> VITE_API_BASE_URL
// 生产: .env.production -> VITE_API_BASE_URL
export const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://api.doudou.asia';

// 调试：查看环境变量
console.log('=== 环境变量调试 ===');
console.log('NODE_ENV:', import.meta.env.MODE);
console.log('VITE_API_BASE_URL:', import.meta.env.VITE_API_BASE_URL);
console.log('最终 BASE_URL:', BASE_URL);

let isRefreshing = false;
let requests = [];

const logout = () => {
    uni.removeStorageSync('token');
    uni.removeStorageSync('refreshToken');
    uni.removeStorageSync('user');
    uni.showToast({
        title: '登录已过期，请重新登录',
        icon: 'none'
    });
    setTimeout(() => {
        uni.reLaunch({ url: '/pages/login/index' });
    }, 1500);
};

const request = (options) => {
    return new Promise((resolve, reject) => {
        console.log('--- 发起请求 ---', options.url);
        const token = uni.getStorageSync('token');

        // 确保 header 存在
        options.header = options.header || {};
        options.header['Content-Type'] = 'application/json';
        // ngrok 免费版需要这个 header
        options.header['ngrok-skip-browser-warning'] = 'true';
        if (token) {
            options.header['Authorization'] = `Bearer ${token}`;
        }

        console.log('--- 调用 uni.request ---', BASE_URL + options.url);
        uni.request({
            url: BASE_URL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: options.header,
            success: (res) => {
                // 处理 401 未授权 (无感刷新)
                if (res.statusCode === 401) {
                    const refreshToken = uni.getStorageSync('refreshToken');
                    if (refreshToken) {
                        if (!isRefreshing) {
                            isRefreshing = true;
                            // 发起刷新请求
                            uni.request({
                                url: BASE_URL + '/auth/refresh',
                                method: 'POST',
                                data: { refreshToken },
                                header: { 'Content-Type': 'application/json' },
                                success: (refreshRes) => {
                                    if (refreshRes.statusCode === 200 && refreshRes.data.code === 200) {
                                        const newToken = refreshRes.data.data.token;
                                        uni.setStorageSync('token', newToken);

                                        // 执行队列中的请求
                                        requests.forEach(cb => cb(newToken));
                                        requests = [];

                                        // 重试当前请求
                                        options.header['Authorization'] = `Bearer ${newToken}`;
                                        request(options).then(resolve).catch(reject);
                                    } else {
                                        // 刷新失败
                                        requests = [];
                                        logout();
                                        reject('RefreshTokenExpired');
                                    }
                                },
                                fail: (err) => {
                                    logout();
                                    reject(err);
                                },
                                complete: () => {
                                    isRefreshing = false;
                                }
                            });
                        } else {
                            // 正在刷新中，将请求挂起
                            requests.push((newToken) => {
                                options.header['Authorization'] = `Bearer ${newToken}`;
                                request(options).then(resolve).catch(reject);
                            });
                        }
                        return;
                    } else {
                        // 无 Refresh Token，直接登出
                        logout();
                        reject('Unauthorized');
                        return;
                    }
                }

                if (res.statusCode === 200) {
                    if (res.data.code === 200) {
                        resolve(res.data.data);
                    } else {
                        uni.showToast({
                            title: res.data.msg || 'Error',
                            icon: 'none'
                        });
                        reject(res.data.msg);
                    }
                } else {
                    uni.showToast({
                        title: 'Network Error',
                        icon: 'none'
                    });
                    reject('Network Error');
                }
            },
            fail: (err) => {
                uni.showToast({
                    title: 'Request Failed',
                    icon: 'none'
                });
                reject(err);
            }
        });
    });
};

export default request;
