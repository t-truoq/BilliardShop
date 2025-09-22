package swd.billiardshop.mapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import swd.billiardshop.dto.request.CategoryCreateRequest;
import swd.billiardshop.dto.response.CategoryResponse;
import swd.billiardshop.dto.request.CategoryUpdateRequest;
import swd.billiardshop.entity.Category;

import java.time.LocalDateTime;

@Component
@ConditionalOnMissingBean(CategoryMapper.class)
public class CategoryMapperFallback implements CategoryMapper {

    @Override
    public CategoryResponse toDto(Category category) {
        if (category == null) return null;
        CategoryResponse dto = new CategoryResponse();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setImagePublicId(category.getImagePublicId());
        dto.setParentId(category.getParent() == null ? null : category.getParent().getCategoryId());
        dto.setSortOrder(category.getSortOrder());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

    @Override
    public Category toEntity(CategoryCreateRequest dto) {
        if (dto == null) return null;
        Category c = new Category();
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) c.setSortOrder(dto.getSortOrder());
        if (dto.getIsActive() != null) c.setIsActive(dto.getIsActive());
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    @Override
    public void updateFromDto(CategoryUpdateRequest dto, Category entity) {
        if (dto == null || entity == null) return;
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
