# SSL 证书目录

请将 SSL 证书文件放置在此目录：

## 文件命名
- `fullchain.pem` - 完整证书链（包含域名证书和中间证书）
- `privkey.pem` - 私钥文件

## Let's Encrypt 获取方式
```bash
# 在服务器上执行
certbot certonly --standalone -d api.doudou.asia

# 证书会生成在 /etc/letsencrypt/live/api.doudou.asia/
# 复制到此目录：
cp /etc/letsencrypt/live/api.doudou.asia/fullchain.pem ./
cp /etc/letsencrypt/live/api.doudou.asia/privkey.pem ./
```

## 云服务商证书
下载 Nginx 格式证书后，重命名为上述文件名。
