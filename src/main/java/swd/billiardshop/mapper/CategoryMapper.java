package swd.billiardshop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import swd.billiardshop.dto.request.CategoryCreateRequest;
import swd.billiardshop.dto.response.CategoryResponse;
import swd.billiardshop.dto.request.CategoryUpdateRequest;
import swd.billiardshop.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
	@Mapping(target = "parentId", source = "parent.categoryId")
	@Mapping(target = "imagePublicId", source = "imagePublicId")
	CategoryResponse toDto(Category category);

	@Mapping(target = "categoryId", ignore = true)
	@Mapping(target = "parent", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "imageUrl", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Category toEntity(CategoryCreateRequest dto);

	@Mapping(target = "categoryId", ignore = true)
	@Mapping(target = "parent", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "imageUrl", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateFromDto(CategoryUpdateRequest dto, @MappingTarget Category entity);
}
