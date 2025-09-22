package swd.billiardshop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.entity.Product;
import swd.billiardshop.entity.Category;
import swd.billiardshop.entity.User;
import swd.billiardshop.repository.ProductRepository;
import swd.billiardshop.repository.CategoryRepository;
import swd.billiardshop.dto.request.ProductRequest;
import swd.billiardshop.dto.response.ProductResponse;
import swd.billiardshop.dto.response.ProductImageResponse;
import swd.billiardshop.repository.ProductImageRepository;
import swd.billiardshop.entity.ProductImage;
import swd.billiardshop.enums.ProductStatus;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import swd.billiardshop.repository.spec.ProductSpecifications;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Page<ProductResponse> searchProducts(String q, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                                String brand, String material, Integer minRating, Boolean inStockOnly,
                                                String sortBy, Integer page, Integer size) {
        Specification<Product> spec = ProductSpecifications.search(q, categoryId, minPrice, maxPrice, brand, material, minRating, inStockOnly);
        Sort sort = Sort.by(Sort.Direction.DESC, "productId");
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc" -> sort = Sort.by(Sort.Direction.ASC, "price");
                case "price_desc" -> sort = Sort.by(Sort.Direction.DESC, "price");
                case "rating" -> sort = Sort.by(Sort.Direction.DESC, "averageRating");
                case "sales" -> sort = Sort.by(Sort.Direction.DESC, "salesCount");
                case "newest" -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size, sort);
        Page<Product> results = productRepository.findAll(spec, pageable);
        return results.map(this::toResponse);
    }

    public ProductResponse getProduct(Integer id) {
        return productRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest req, User creator) {
        validateRequestForCreate(req);
        if (productRepository.existsBySku(req.getSku()))
            throw new AppException(ErrorCode.INVALID_REQUEST, "SKU already exists");
        if (productRepository.existsBySlug(req.getSlug()))
            throw new AppException(ErrorCode.INVALID_REQUEST, "Slug already exists");

        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Category not found"));
        }

        Product p = Product.builder()
                .category(category)
                .name(req.getName())
                .slug(req.getSlug())
                .description(req.getDescription())
                .shortDescription(req.getShortDescription())
                .originCountry(req.getOriginCountry())
                .warrantyPeriod(req.getWarrantyPeriod())
                .weightRange(req.getWeightRange())
                .tipSize(req.getTipSize())
                .material(req.getMaterial())
                .brand(req.getBrand())
                .sku(req.getSku())
                .price(req.getPrice())
                .comparePrice(req.getComparePrice())
                .stockQuantity(req.getStockQuantity() == null ? 0 : req.getStockQuantity())
                .minStockLevel(req.getMinStockLevel() == null ? 0 : req.getMinStockLevel())
                .weight(req.getWeight())
                .dimensions(req.getDimensions())
                .isFeatured(req.getIsFeatured() == null ? Boolean.FALSE : req.getIsFeatured())
                .createdBy(creator)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(ProductStatus.ACTIVE)
                .build();

        if (p.getMinStockLevel() != null && p.getStockQuantity() != null && p.getMinStockLevel() > p.getStockQuantity())
            throw new AppException(ErrorCode.INVALID_REQUEST, "Min stock level cannot be greater than stock quantity");

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));

        if (req.getName() != null) p.setName(req.getName());
        if (req.getSlug() != null && !req.getSlug().equals(p.getSlug())) {
            if (productRepository.existsBySlug(req.getSlug()))
                throw new AppException(ErrorCode.INVALID_REQUEST, "Slug already exists");
            p.setSlug(req.getSlug());
        }
        if (req.getSku() != null && !req.getSku().equals(p.getSku())) {
            if (productRepository.existsBySku(req.getSku()))
                throw new AppException(ErrorCode.INVALID_REQUEST, "SKU already exists");
            p.setSku(req.getSku());
        }

        if (req.getPrice() != null) p.setPrice(req.getPrice());
        if (req.getComparePrice() != null) p.setComparePrice(req.getComparePrice());

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Category not found"));
            p.setCategory(category);
        }

        p.setDescription(req.getDescription());
        p.setShortDescription(req.getShortDescription());
        p.setOriginCountry(req.getOriginCountry());
        p.setWarrantyPeriod(req.getWarrantyPeriod());
        p.setWeightRange(req.getWeightRange());
        p.setTipSize(req.getTipSize());
        p.setMaterial(req.getMaterial());
        p.setBrand(req.getBrand());
        p.setStockQuantity(req.getStockQuantity() == null ? p.getStockQuantity() : req.getStockQuantity());
        p.setMinStockLevel(req.getMinStockLevel() == null ? p.getMinStockLevel() : req.getMinStockLevel());
        p.setWeight(req.getWeight());
        p.setDimensions(req.getDimensions());
        p.setIsFeatured(req.getIsFeatured() == null ? p.getIsFeatured() : req.getIsFeatured());
        p.setUpdatedAt(LocalDateTime.now());

        // business validations
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Price must be > 0");
        if (p.getComparePrice() != null && p.getComparePrice().compareTo(p.getPrice()) < 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Compare price must be >= price");

        if (p.getMinStockLevel() != null && p.getStockQuantity() != null && p.getMinStockLevel() > p.getStockQuantity())
            throw new AppException(ErrorCode.INVALID_REQUEST, "Min stock level cannot be greater than stock quantity");

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        productRepository.delete(p);
    }

    @Transactional
    public void recalculateRating(Integer productId, BigDecimal average, Integer count) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        p.setAverageRating(average == null ? BigDecimal.ZERO : average);
        p.setReviewCount(count == null ? 0 : count);
        productRepository.save(p);
    }

    private void validateRequestForCreate(ProductRequest req) {
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new AppException(ErrorCode.INVALID_REQUEST, "Product name is required");
        if (req.getName().length() > 200)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Product name too long");
        if (req.getSku() == null || req.getSku().trim().isEmpty())
            throw new AppException(ErrorCode.INVALID_REQUEST, "SKU is required");
        if (req.getSlug() == null || req.getSlug().trim().isEmpty())
            throw new AppException(ErrorCode.INVALID_REQUEST, "Slug is required");
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Price must be > 0");
        if (req.getComparePrice() != null && req.getComparePrice().compareTo(req.getPrice()) < 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Compare price must be >= price");
        if (req.getStockQuantity() != null && req.getStockQuantity() < 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Stock quantity must be >= 0");
        if (req.getMinStockLevel() != null && req.getMinStockLevel() < 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Min stock level must be >= 0");
        if (req.getWeight() != null && req.getWeight().compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Weight must be > 0");
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.setProductId(p.getProductId());
        r.setCategoryId(p.getCategory() == null ? null : p.getCategory().getCategoryId());
        r.setName(p.getName());
        r.setSlug(p.getSlug());
        r.setDescription(p.getDescription());
        r.setShortDescription(p.getShortDescription());
        r.setOriginCountry(p.getOriginCountry());
        r.setWarrantyPeriod(p.getWarrantyPeriod());
        r.setWeightRange(p.getWeightRange());
        r.setTipSize(p.getTipSize());
        r.setMaterial(p.getMaterial());
        r.setBrand(p.getBrand());
        r.setSku(p.getSku());
        r.setPrice(p.getPrice());
        r.setComparePrice(p.getComparePrice());
        r.setStockQuantity(p.getStockQuantity());
        r.setMinStockLevel(p.getMinStockLevel());
        r.setWeight(p.getWeight());
        r.setDimensions(p.getDimensions());
        r.setStatus(p.getStatus() == null ? null : p.getStatus().name());
        r.setIsFeatured(p.getIsFeatured());
        r.setAverageRating(p.getAverageRating());
        r.setReviewCount(p.getReviewCount());
        r.setViewCount(p.getViewCount());
        r.setSalesCount(p.getSalesCount());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());

        List<ProductImage> images = productImageRepository.findByProductProductIdOrderBySortOrderAsc(p.getProductId());
        List<ProductImageResponse> imgs = images.stream().map(img -> {
            ProductImageResponse ir = new ProductImageResponse();
            ir.setImageId(img.getImageId());
            ir.setImageUrl(img.getImageUrl());
            ir.setAltText(img.getAltText());
            ir.setIsPrimary(img.getIsPrimary());
            return ir;
        }).collect(Collectors.toList());
        r.setImages(imgs);

        return r;
    }
}
