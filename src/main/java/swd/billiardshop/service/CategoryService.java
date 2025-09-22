package swd.billiardshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import swd.billiardshop.dto.request.CategoryCreateRequest;
import swd.billiardshop.dto.response.CategoryResponse;
import swd.billiardshop.dto.response.CategoryTreeResponse;
import swd.billiardshop.dto.request.CategoryUpdateRequest;
import swd.billiardshop.entity.Category;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.mapper.CategoryMapper;
import swd.billiardshop.repository.CategoryRepository;
import swd.billiardshop.repository.ProductRepository;
import swd.billiardshop.util.SlugUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final Cloudinary cloudinary;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CategoryMapper categoryMapper,
                           Cloudinary cloudinary) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
        this.cloudinary = cloudinary;
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> all = categoryRepository.findAll();
        List<CategoryResponse> result = new ArrayList<>();
        for (Category c : all) result.add(categoryMapper.toDto(c));
        return result;
    }

    public CategoryResponse createCategory(CategoryCreateRequest dto, swd.billiardshop.entity.User creator) {
        if (dto.getName() == null || dto.getName().trim().isEmpty())
            throw new AppException(ErrorCode.INVALID_REQUEST, "Name is required");

        validateNameParentUnique(dto.getName(), dto.getParentId());
        Category entity = categoryMapper.toEntity(dto);

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Parent category not found"));
            checkMaxDepth(parent);
            entity.setParent(parent);
        }

        String slug = SlugUtil.toSlug(dto.getName());
        slug = ensureUniqueSlug(slug);
        entity.setSlug(slug);

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            Map<String, String> upload = uploadToCloudinary(dto.getImage());
            entity.setImageUrl(upload.get("url"));
            entity.setImagePublicId(upload.get("public_id"));
        }

    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());
    if (creator != null) entity.setCreatedBy(creator);

        Category saved = categoryRepository.save(entity);
        return categoryMapper.toDto(saved);
    }

    public CategoryResponse updateCategory(Integer id, CategoryUpdateRequest dto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Category not found"));

        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(existing.getName())) {
            Integer parentId = dto.getParentId() != null ? dto.getParentId() : (existing.getParent() == null ? null : existing.getParent().getCategoryId());
            validateNameParentUnique(dto.getName(), parentId);
            String newSlug = SlugUtil.toSlug(dto.getName());
            if (!newSlug.equals(existing.getSlug())) newSlug = ensureUniqueSlug(newSlug);
            existing.setSlug(newSlug);
            existing.setName(dto.getName());
        }

        if (dto.getParentId() != null && (existing.getParent() == null || !Objects.equals(existing.getParent().getCategoryId(), dto.getParentId()))) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Parent category not found"));
            checkMaxDepth(parent);
            existing.setParent(parent);
        }

        if (dto.getIsActive() != null && !dto.getIsActive() && hasActiveChildren(existing)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Parent category cannot be set inactive while child is active");
        }

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            Map<String, String> upload = uploadToCloudinary(dto.getImage());
            existing.setImageUrl(upload.get("url"));
            String newPublicId = upload.get("public_id");
            String oldPublicId = existing.getImagePublicId();
            existing.setImagePublicId(newPublicId);
            // delete old image from Cloudinary if present
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                try {
                    cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
                } catch (IOException ignored) {
                }
            }
        }

        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) existing.setSortOrder(dto.getSortOrder());
        if (dto.getIsActive() != null) existing.setIsActive(dto.getIsActive());

        existing.setUpdatedAt(LocalDateTime.now());
        Category saved = categoryRepository.save(existing);
        return categoryMapper.toDto(saved);
    }

    public void deleteCategory(Integer id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Category not found"));
        Integer count = productRepository.countByCategoryId(id);
        if (count != null && count > 0) throw new AppException(ErrorCode.INVALID_REQUEST, "Cannot delete category with products");
        categoryRepository.delete(existing);
    }

    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> all = categoryRepository.findAll();
        Map<Integer, CategoryTreeResponse> map = new HashMap<>();
        for (Category c : all) {
        CategoryTreeResponse dto = CategoryTreeResponse.builder()
                    .categoryId(c.getCategoryId())
                    .name(c.getName())
                    .slug(c.getSlug())
                    .description(c.getDescription())
                    .imageUrl(c.getImageUrl())
            .imagePublicId(c.getImagePublicId())
                    .parentId(c.getParent() == null ? null : c.getParent().getCategoryId())
                    .sortOrder(c.getSortOrder())
                    .isActive(c.getIsActive())
                    .createdAt(c.getCreatedAt())
                    .updatedAt(c.getUpdatedAt())
                    .build();
            map.put(c.getCategoryId(), dto);
        }
        List<CategoryTreeResponse> roots = new ArrayList<>();
        for (Category c : all) {
            CategoryTreeResponse dto = map.get(c.getCategoryId());
            if (c.getParent() != null) {
                CategoryTreeResponse parent = map.get(c.getParent().getCategoryId());
                if (parent != null) parent.getChildren().add(dto);
            } else {
                roots.add(dto);
            }
        }
        return roots;
    }

    public List<CategoryResponse> getBreadcrumb(Integer categoryId) {
        Category c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Category not found"));
        LinkedList<CategoryResponse> list = new LinkedList<>();
        while (c != null) {
            list.addFirst(categoryMapper.toDto(c));
            c = c.getParent();
        }
        return list;
    }

    private void validateNameParentUnique(String name, Integer parentId) {
        List<Category> all = categoryRepository.findAll();
        for (Category c : all) {
            Integer pid = c.getParent() == null ? null : c.getParent().getCategoryId();
            if (Objects.equals(pid, parentId) && c.getName().equalsIgnoreCase(name)) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Category name must be unique within the same parent");
            }
        }
    }

    private String ensureUniqueSlug(String base) {
        String slug = base;
        int i = 1;
        while (categoryRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    private void checkMaxDepth(Category parent) {
        int depth = 1;
        Category p = parent.getParent();
        while (p != null) {
            depth++;
            p = p.getParent();
        }
        if (depth >= 3) throw new AppException(ErrorCode.INVALID_REQUEST, "Maximum category depth (3) exceeded");
    }

    private boolean hasActiveChildren(Category parent) {
        List<Category> children = categoryRepository.findByParentId(parent.getCategoryId());
        for (Category c : children) if (Boolean.TRUE.equals(c.getIsActive())) return true;
        return false;
    }

    private Map<String, String> uploadToCloudinary(MultipartFile file) {
        if (file == null || file.isEmpty()) return Collections.emptyMap();
        if (file.getSize() > 2L * 1024 * 1024) throw new AppException(ErrorCode.FILE_TOO_LARGE, "Image size must be <= 2MB");
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg") && !contentType.equalsIgnoreCase("image/png"))) {
            throw new AppException(ErrorCode.FILE_INVALID_TYPE, "Invalid image type. Only JPG/PNG allowed");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = uploadResult.get("secure_url") != null ? uploadResult.get("secure_url").toString() : String.valueOf(uploadResult.get("url"));
            String publicId = uploadResult.get("public_id") != null ? uploadResult.get("public_id").toString() : null;
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("public_id", publicId);
            return result;
        } catch (IOException e) {
            throw new AppException(ErrorCode.AVATAR_UPLOAD_FAILED, "Failed to upload image: " + e.getMessage());
        }
    }
}
