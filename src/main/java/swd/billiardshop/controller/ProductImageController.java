package swd.billiardshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.billiardshop.service.ProductImageService;
import swd.billiardshop.dto.response.ProductImageResponse;
import swd.billiardshop.entity.ProductImage;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products/{productId}/images")
@Tag(name = "Product Image Management", description = "APIs for managing product images")
public class ProductImageController {
    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload single product image",
        description = "Upload a single image for a product. Maximum file size: 5MB. Supported formats: JPG, PNG, GIF"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully", 
                    content = @Content(schema = @Schema(implementation = ProductImageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<ProductImageResponse> uploadImage(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Integer productId,
            
            @Parameter(description = "Image file to upload (max 5MB)", required = true)
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "File cannot be empty");
        }
        
        ProductImage img = imageService.uploadImage(productId, file);
        ProductImageResponse response = mapToResponse(img);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload multiple product images",
        description = "Upload multiple images for a product. Maximum 10 images total per product. Each file max 5MB."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Images uploaded successfully", 
                    content = @Content(schema = @Schema(implementation = ProductImageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid files or request - check file count/size limits"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<List<ProductImageResponse>> uploadImages(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Integer productId,
            
            @Parameter(description = "Multiple image files to upload (max 5MB each)", required = true)
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No files provided");
        }
        
        // Validate each file
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "One or more files are empty");
            }
        }
        
        // Convert List to Array for service method
        MultipartFile[] filesArray = files.toArray(new MultipartFile[0]);
        List<ProductImage> imgs = imageService.uploadImages(productId, filesArray);
        List<ProductImageResponse> responses = imgs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{imageId}/primary")
    @Operation(
        summary = "Set primary image", 
        description = "Set an image as the primary image for a product"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Primary image set successfully"),
        @ApiResponse(responseCode = "400", description = "Image does not belong to this product"),
        @ApiResponse(responseCode = "404", description = "Product or image not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<Void> setPrimary(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Integer productId,
            
            @Parameter(description = "Image ID to set as primary", required = true)
            @PathVariable Integer imageId) {
        imageService.setPrimary(productId, imageId);
        return ResponseEntity.ok().build();
    }
    
    private ProductImageResponse mapToResponse(ProductImage img) {
        ProductImageResponse response = new ProductImageResponse();
        response.setImageId(img.getImageId());
        response.setImageUrl(img.getImageUrl());
        response.setAltText(img.getAltText());
        response.setIsPrimary(img.getIsPrimary());
        return response;
    }
}