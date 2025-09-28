package swd.billiardshop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.billiardshop.configuration.JwtUtil;
import swd.billiardshop.dto.request.AddToCartRequest;
import swd.billiardshop.dto.request.UpdateCartItemRequest;
import swd.billiardshop.dto.response.ApiResponse;
import swd.billiardshop.dto.response.CartResponse;
import swd.billiardshop.service.CartService;

@RestController
@RequestMapping("/api/user/cart") // Changed to /api/user/cart since it requires authentication
@CrossOrigin(origins = "*")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {

        Integer userId = getCurrentUserId(httpRequest);
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Đã thêm sản phẩm vào giỏ hàng"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Integer itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest httpRequest) {

        Integer userId = getCurrentUserId(httpRequest);
        CartResponse cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Đã cập nhật giỏ hàng"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @PathVariable Integer itemId,
            HttpServletRequest httpRequest) {

        Integer userId = getCurrentUserId(httpRequest);
        CartResponse cart = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Đã xóa sản phẩm khỏi giỏ hàng"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        CartResponse cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa toàn bộ giỏ hàng"));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<CartResponse>> validateCart(HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        CartResponse cart = cartService.validateCartForCheckout(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Giỏ hàng hợp lệ"));
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Invalid or expired JWT token");
            }

            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new RuntimeException("User ID not found in JWT token");
            }

            return userId;
        } catch (Exception e) {
            throw new RuntimeException("Error extracting user ID from JWT token: " + e.getMessage());
        }
    }
}