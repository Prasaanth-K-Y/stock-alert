# --- !Ups

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS restock;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS refes;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;


-- ─── USERS ─────────────────────────────────────────────────────────────
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(255),
  notifications VARCHAR(255),
  is_prime BOOLEAN DEFAULT FALSE,
  address VARCHAR(255) NOT NULL,
  role VARCHAR(50) DEFAULT 'customer',
  totp_secret VARCHAR(255)
);

-- ─── ITEMS ────────────────────────────────────────────────────────────
CREATE TABLE items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  stock BIGINT NOT NULL,
  minStock BIGINT NOT NULL
);

-- ─── REFES ────────────────────────────────────────────────────────────
CREATE TABLE refes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  ref VARCHAR(955) NOT NULL
);

-- ─── ORDERS ───────────────────────────────────────────────────────────
CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item BIGINT NOT NULL,
  qty BIGINT NOT NULL,
  CONSTRAINT fk_order_item FOREIGN KEY (item) REFERENCES items(id)
);

-- ─── RESTOCK ──────────────────────────────────────────────────────────
CREATE TABLE restock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  itemId BIGINT NOT NULL,
  customerId BIGINT NOT NULL,
  CONSTRAINT fk_restock_item FOREIGN KEY (itemId) REFERENCES items(id),
  CONSTRAINT fk_restock_customer FOREIGN KEY (customerId) REFERENCES users(id)
);

-- ─── DUMMY DATA ───────────────────────────────────────────────────────
INSERT INTO users (name, email, phone, address, role, is_prime)
VALUES
  ('Alice', 'alice@example.com', '9876543210', 'Chennai', 'Customer', FALSE),
  ('Bob', 'bob@example.com', '9123456780', 'Bangalore', 'Seller', TRUE),
  ('AdminUser', 'admin@example.com', '9000000000', 'Mumbai', 'Admin', TRUE);

INSERT INTO items (name, stock, minStock)
VALUES
  ('dress', 20, 1);



# --- !Downs

DROP TABLE IF EXISTS restock;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS refes;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;
