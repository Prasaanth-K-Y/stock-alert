# --- !Ups
CREATE TABLE IF NOT EXISTS order_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL
);

# --- !Down
DROP TABLE IF EXISTS order_alerts;
