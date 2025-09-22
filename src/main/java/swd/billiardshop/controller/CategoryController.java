package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.dto.response.CategoryResponse;
import swd.billiardshop.service.CategoryService;
import swd.billiardshop.service.UserService;
import java.util.List;
import swd.billiardshop.dto.request.CategoryCreateRequest;
import swd.billiardshop.dto.request.CategoryUpdateRequest;
import swd.billiardshop.dto.response.CategoryTreeResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/user/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }
    
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CategoryResponse> createCategory(@ModelAttribute CategoryCreateRequest dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        swd.billiardshop.entity.User creator = null;
        if (auth != null && auth.getName() != null) {
            creator = userService.getUserEntityByUsername(auth.getName());
        }
        CategoryResponse created = categoryService.createCategory(dto, creator);
        return ResponseEntity.ok(created);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Integer id, @ModelAttribute CategoryUpdateRequest dto) {
        CategoryResponse updated = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
}

    @GetMapping("/tree")
    public List<CategoryTreeResponse> getTree() {
        return categoryService.getCategoryTree();
    }

    @GetMapping("/{id}/breadcrumb")
    public List<CategoryResponse> breadcrumb(@PathVariable Integer id) {
        return categoryService.getBreadcrumb(id);
    }

}
