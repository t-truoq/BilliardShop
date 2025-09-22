package swd.billiardshop.service;

import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service upload ảnh cho BilliardShop (bàn, cơ, bi, banner, poster, avatar...)
 */
@Builder
@Service
public class CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    @Autowired
    private Cloudinary cloudinary;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // Preset cho các loại ảnh billiard
    private static final Map<String, String> PRESETS = Map.of(
            "table", "w_800,c_scale,q_auto,f_jpg",
            "cue", "w_400,c_scale,q_auto,f_jpg",
            "ball", "w_150,h_150,c_fit,q_auto,f_jpg",
            "banner", "w_1060,c_scale,q_auto,f_jpg",
            "poster", "w_800,c_scale,q_auto,f_jpg",
            "avatar", "w_300,h_300,c_fill,g_face,q_auto,f_jpg"
    );

    /**
     * Upload ảnh với preset cho sản phẩm billiard
     */
    public String uploadImage(MultipartFile file, String folder, String preset) throws IOException {
        validateFile(file);
        String transformation = PRESETS.getOrDefault(preset, "q_auto,f_auto");
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "transformation", transformation,
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String url = uploadResult.get("secure_url").toString();
            logger.info("Image uploaded successfully: {}", url);
            return url;
        } catch (Exception e) {
            logger.error("Failed to upload image: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Upload ảnh raw không preset (dùng cho avatar, product images...)
     */
    public String uploadRawImage(MultipartFile file, String folder) throws IOException {
        validateFile(file);
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "quality", "auto",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String url = uploadResult.get("secure_url").toString();
            logger.info("Raw image uploaded successfully: {}", url);
            return url;
        } catch (Exception e) {
            logger.error("Failed to upload raw image: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload raw image: " + e.getMessage());
        }
    }

    /**
     * Upload nhiều ảnh bất đồng bộ cho sản phẩm billiard
     */
    public List<String> uploadMultipleImagesAsync(MultipartFile[] files, String folder, String preset) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (MultipartFile file : files) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadImage(file, folder, preset);
                } catch (IOException e) {
                    logger.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                    return null;
                }
            }, executorService);
            futures.add(future);
        }
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Xóa ảnh từ Cloudinary
     */
    public boolean deleteResource(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            boolean success = "ok".equals(result.get("result"));
            if (success) {
                logger.info("Resource deleted successfully: {}", publicId);
            } else {
                logger.warn("Failed to delete resource: {}", publicId);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error deleting resource: {}", publicId, e);
            return false;
        }
    }

    /**
     * Xóa ảnh từ Cloudinary với resource type
     */
    public boolean deleteResource(String publicId, String resourceType) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
            boolean success = "ok".equals(result.get("result"));
            if (success) {
                logger.info("Resource deleted successfully: {} (type: {})", publicId, resourceType);
            } else {
                logger.warn("Failed to delete resource: {} (type: {})", publicId, resourceType);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error deleting resource: {} (type: {})", publicId, resourceType, e);
            return false;
        }
    }

    /**
     * Extract publicId từ Cloudinary URL
     */
    public String extractPublicIdFromUrl(String url) {
        try {
            if (url == null || !url.contains("cloudinary.com")) {
                return null;
            }
            
            // Tìm vị trí của "/upload/"
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;
            
            // Lấy phần sau "/upload/"
            String afterUpload = url.substring(uploadIndex + 8);
            
            // Bỏ version nếu có (vXXXX/)
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                int slashIndex = afterUpload.indexOf("/");
                afterUpload = afterUpload.substring(slashIndex + 1);
            }
            
            // Bỏ extension (.jpg, .png, etc.)
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            
            return afterUpload;
        } catch (Exception e) {
            logger.error("Error extracting public ID from URL: {}", url, e);
            return null;
        }
    }

    /**
     * Validate file trước khi upload
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "File cannot be null or empty");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "File size too large. Maximum 10MB allowed");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid file type. Only images are allowed");
        }
    }

    /**
     * Cleanup resources khi service shutdown
     */
    public void shutdown() {
        executorService.shutdown();
    }
}