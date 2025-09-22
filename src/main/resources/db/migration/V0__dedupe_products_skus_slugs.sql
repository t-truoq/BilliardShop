-- Dedupe duplicate SKUs and slugs for products before applying unique constraints
-- Strategy: for rows with duplicate sku or slug, keep the lowest product_id unchanged
-- and append a suffix to other duplicates so the unique constraint migration can run.

-- Make updates transactional in SQL clients as needed.

-- Dedupe SKU
UPDATE products p
JOIN (
  SELECT sku, MIN(product_id) AS keep_id
  FROM products
  WHERE sku IS NOT NULL AND sku <> ''
  GROUP BY sku
  HAVING COUNT(*) > 1
) dup ON IFNULL(p.sku,'') = IFNULL(dup.sku,'')
SET p.sku = CONCAT(p.sku, '-dup-', p.product_id)
WHERE p.product_id <> dup.keep_id;

-- Dedupe slug
UPDATE products p
JOIN (
  SELECT slug, MIN(product_id) AS keep_id
  FROM products
  WHERE slug IS NOT NULL AND slug <> ''
  GROUP BY slug
  HAVING COUNT(*) > 1
) dup ON IFNULL(p.slug,'') = IFNULL(dup.slug,'')
SET p.slug = CONCAT(p.slug, '-dup-', p.product_id)
WHERE p.product_id <> dup.keep_id;

-- Note: Review these changes before running in production. This script appends '-dup-{id}'
-- to duplicate values to allow unique constraints to be applied. Consider manually
-- resolving semantic duplicates after migration.
