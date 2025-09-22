package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.service.ProductService;
import swd.billiardshop.service.UserService;
import swd.billiardshop.dto.response.ProductResponse;
import swd.billiardshop.dto.request.ProductRequest;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/user/products")
public class ProductController {
    private final ProductService productService;
    private final UserService userService;

    public ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/search")
    public Object searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return productService.searchProducts(q, categoryId, minPrice, maxPrice, brand, material, minRating, inStockOnly, sortBy, page, size);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Integer id) {
        return productService.getProduct(id);
    }

    @PostMapping
    public ProductResponse createProduct(@jakarta.validation.Valid @RequestBody ProductRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        swd.billiardshop.entity.User creator = null;
        if (auth != null && auth.getName() != null) creator = userService.getUserEntityByUsername(auth.getName());
        return productService.createProduct(req, creator);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Integer id, @jakarta.validation.Valid @RequestBody ProductRequest req) {
        return productService.updateProduct(id, req);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
    }
}
