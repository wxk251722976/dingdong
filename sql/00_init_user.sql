-- 授权 root 用户远程访问
-- 使用 mysql_native_password 认证方式以兼容各种客户端
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123456';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- 更新 localhost 的 root 用户为 mysql_native_password
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root123456';

FLUSH PRIVILEGES;
