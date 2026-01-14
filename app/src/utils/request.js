const BASE_URL = 'http://localhost:8080';

const request = (options) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token');
        uni.request({
            url: BASE_URL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    // Assuming backend returns { code: 200, data: ..., msg: ... }
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
