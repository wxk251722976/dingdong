<script>
	export default {
		onLaunch: function(options) {
			console.log('App Launch', options)
			
			const token = uni.getStorageSync('token');
			const user = uni.getStorageSync('user');
			
			if (!token || !user) {
				// 未登录，强制跳转到登录页
				// 如果已经在登录页（例如通过分享进入），则不跳转以免丢失参数
				const path = options && options.path ? options.path : '';
				if (path.indexOf('pages/login/index') === -1) {
					uni.reLaunch({ url: '/pages/login/index' });
				}
			} else {
				// 已登录，根据角色跳转
				if (user.role === 'CHILD') {
					uni.reLaunch({ url: '/pages/supervisor/index' });
				} else {
					uni.reLaunch({ url: '/pages/home/index' });
				}
			}
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/*每个页面公共css */
	page {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		box-sizing: border-box;
		background-color: #f5f5f5;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
	}
</style>
