CREATE USER 'notiuser'@'%' IDENTIFIED BY 'notipass';
GRANT ALL PRIVILEGES ON testdb.* TO 'notiuser'@'%';
FLUSH PRIVILEGES;
