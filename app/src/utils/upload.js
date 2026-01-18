/**
 * 头像上传工具函数
 */
import { BASE_URL } from './request';

/**
 * 上传头像到服务器
 * @param {string} tempFilePath 微信临时文件路径
 * @returns {Promise<string>} 返回永久访问URL
 */
export const uploadAvatar = (tempFilePath) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token');

        if (!tempFilePath) {
            reject(new Error('文件路径不能为空'));
            return;
        }

        uni.showLoading({ title: '上传中...' });

        uni.uploadFile({
            url: BASE_URL + '/file/upload/avatar',
            filePath: tempFilePath,
            name: 'file',
            header: {
                'Authorization': token ? `Bearer ${token}` : '',
                'ngrok-skip-browser-warning': 'true'
            },
            success: (res) => {
                uni.hideLoading();

                if (res.statusCode === 200) {
                    try {
                        const data = JSON.parse(res.data);
                        if (data.code === 200 && data.data && data.data.url) {
                            console.log('头像上传成功:', data.data.url);
                            resolve(data.data.url);
                        } else {
                            console.error('头像上传失败:', data.msg);
                            reject(new Error(data.msg || '上传失败'));
                        }
                    } catch (e) {
                        console.error('解析响应失败:', e);
                        reject(new Error('解析响应失败'));
                    }
                } else {
                    console.error('上传请求失败:', res.statusCode);
                    reject(new Error('上传失败，状态码: ' + res.statusCode));
                }
            },
            fail: (err) => {
                uni.hideLoading();
                console.error('上传请求错误:', err);
                reject(new Error('网络错误'));
            }
        });
    });
};

export default uploadAvatar;
