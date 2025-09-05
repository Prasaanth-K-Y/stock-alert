CREATE USER 'stockuser'@'%' IDENTIFIED BY 'stockpass';
GRANT ALL PRIVILEGES ON testdb.* TO 'stockuser'@'%';
FLUSH PRIVILEGES;
