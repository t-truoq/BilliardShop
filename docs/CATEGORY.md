Category Management

Endpoints:
- GET /categories : list all categories (flat list)
- POST /categories (multipart/form-data) : create category. Fields: name, description, parentId, sortOrder, isActive, image (jpg/png <= 2MB)
- PUT /categories/{id} (multipart/form-data) : update category
- DELETE /categories/{id} : delete category (fails if products exist)
- GET /categories/tree : get top-level categories (simple tree roots)
- GET /categories/{id}/breadcrumb : get breadcrumb (ancestors)

Business rules and constraints:
- Max depth: 3 levels
- Name unique within same parent
- Slug is auto-generated and unique global
- Cannot delete category with products
- Parent cannot be set inactive while a child is active
- Image: jpg/png, max 2MB
- Sort order: 0-999
