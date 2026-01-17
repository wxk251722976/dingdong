<script>
	export default {
		onLaunch: function(options) {
			console.log('App Launch')
			
			try {
				const token = uni.getStorageSync('token');
				const user = uni.getStorageSync('user');
				
				if (!token || !user) {
					// 未登录，强制跳转到登录页
					// 如果已经在登录页（例如通过分享进入），则不跳转以免丢失参数
					const path = options && options.path ? options.path : '';
					if (path && path.indexOf('pages/login/index') === -1) {
						// 延迟跳转，确保应用初始化完成
						setTimeout(() => {
							uni.reLaunch({ url: '/pages/login/index' });
						}, 100);
					}
				} else {
					// 已登录，根据角色跳转
					// 同样延迟跳转，避免初始化未完成
					setTimeout(() => {
						if (user.role === 'CHILD') {
							// 如果当前页面不是目标页面才跳转
							const path = options && options.path ? options.path : '';
							if (path.indexOf('pages/supervisor/index') === -1) {
								uni.reLaunch({ url: '/pages/supervisor/index' });
							}
						} else {
							// 默认是 home，如果是 home 则不需跳转（因为 home 是默认启动页）
							// 但如果 options.path 不是 home，且不在白名单，可能需要处理？
							// 这里简化逻辑，只在需要切换 Tab 或角色对应的首页不同时干预
							// Pages.json 中 home/index 是第一项，所以默认启动就是 home/index。
							// 如果角色是 CHILD，我们跳到了 supervisor。
							// 如果是普通用户，留在 home 即可。
						}
					}, 100);
				}
			} catch (e) {
				console.error('App Launch Error', e);
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
