ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '000000';
CREATE USER IF NOT EXISTS 'notiuser'@'%' IDENTIFIED BY 'notipass';
GRANT ALL PRIVILEGES ON testdb.* TO 'notiuser'@'%';
FLUSH PRIVILEGES;
