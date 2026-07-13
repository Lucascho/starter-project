INSERT INTO users (username, password, role) VALUES ('admin', '$2y$10$pjexo7YXOFj.p.8rFQ9YI.UrxuqmxDeBckA4gJ1YMBWjHe5Jjh4Oy', 'ADMIN')
  ON CONFLICT DO NOTHING;
INSERT INTO users (username, password, role) VALUES ('alice', '$2y$10$JmxlqULiBUZ.K6R8mVZmm.xB8VxOhUVcwvBR6ifPgRlKtyMN1DwVi', 'USER')
  ON CONFLICT DO NOTHING;

INSERT INTO products (name, price, stock) VALUES ('機械鍵盤', 2999.99, 10);
INSERT INTO products (name, price, stock) VALUES ('人體工學椅', 8800.00, 5);
INSERT INTO products (name, price, stock) VALUES ('4K 螢幕', 12000.00, 3);
