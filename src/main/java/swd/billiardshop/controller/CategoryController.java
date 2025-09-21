package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.Category;
import swd.billiardshop.service.CategoryService;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
