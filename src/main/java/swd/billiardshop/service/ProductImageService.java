package swd.billiardshop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import swd.billiardshop.entity.ProductImage;
import swd.billiardshop.entity.Product;
import swd.billiardshop.repository.ProductImageRepository;
import swd.billiardshop.repository.ProductRepository;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;

import java.io.IOException;
import java.util.List;

@Service
public class ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public ProductImageService(ProductImageRepository productImageRepository,
                               ProductRepository productRepository,
                               CloudinaryService cloudinaryService) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public ProductImage uploadImage(Integer productId, MultipartFile file) throws IOException {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        // Enforce max 5MB per requirement
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "File size too large. Maximum 5MB allowed");
        }

        Integer count = productImageRepository.countByProductProductId(productId);
        if (count != null && count >= 10) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Maximum 10 images allowed per product");
        }

        String url = cloudinaryService.uploadRawImage(file, "products");
    ProductImage img = ProductImage.builder()
        .product(product)
                .imageUrl(url)
                .altText(file.getOriginalFilename())
                .isPrimary(false)
                .build();
        return productImageRepository.save(img);
    }

    @Transactional
    public List<ProductImage> uploadImages(Integer productId, MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) return List.of();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));

        Integer existing = productImageRepository.countByProductProductId(productId);
        int existingCount = existing == null ? 0 : existing;
        if (existingCount + files.length > 10) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Uploading these files would exceed the 10 images per product limit");
        }

        // pre-validate sizes
        for (MultipartFile f : files) {
            if (f.getSize() > 5 * 1024 * 1024) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "One of the files is too large. Maximum 5MB allowed");
            }
        }

        List<ProductImage> saved = new java.util.ArrayList<>();
        for (MultipartFile f : files) {
            String url = cloudinaryService.uploadRawImage(f, "products");
            ProductImage img = ProductImage.builder()
                    .product(product)
                    .imageUrl(url)
                    .altText(f.getOriginalFilename())
                    .isPrimary(false)
                    .build();
            saved.add(productImageRepository.save(img));
        }
        return saved;
    }

    @Transactional
    public void setPrimary(Integer productId, Integer imageId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        ProductImage img = productImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Image not found"));
        if (!img.getProduct().getProductId().equals(productId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Image does not belong to product");
        }

        List<ProductImage> images = productImageRepository.findByProductProductIdOrderBySortOrderAsc(productId);
        for (ProductImage i : images) {
            i.setIsPrimary(i.getImageId().equals(imageId));
        }
        productImageRepository.saveAll(images);
    }
}
