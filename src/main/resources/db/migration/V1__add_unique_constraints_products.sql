-- Add unique constraints for products and categories
-- Ensure existing duplicates are handled externally before running migration

ALTER TABLE products
  ADD CONSTRAINT uq_products_sku UNIQUE (sku);

ALTER TABLE products
  ADD CONSTRAINT uq_products_slug UNIQUE (slug);

ALTER TABLE categories
  ADD CONSTRAINT uq_categories_slug UNIQUE (slug);
