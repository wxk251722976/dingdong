<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
			// Check login status
			const user = uni.getStorageSync('user');
			if (user) {
				if (user.role === 'ELDER') {
					uni.reLaunch({ url: '/pages/elder/index' });
				} else if (user.role === 'CHILD') {
					uni.reLaunch({ url: '/pages/supervisor/index' });
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
