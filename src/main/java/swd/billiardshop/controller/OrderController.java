package swd.billiardshop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.billiardshop.configuration.JwtUtil;
import swd.billiardshop.dto.request.ConfirmOrderRequest;
import swd.billiardshop.dto.request.CreateOrderRequest;
import swd.billiardshop.dto.request.OrderPreviewRequest;
import swd.billiardshop.dto.response.ApiResponse;
import swd.billiardshop.service.OrderService;

@RestController
@RequestMapping("/api/user/orders") // Changed from /api/public/orders to /api/user/orders
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Preview an order (calculate totals, shipping, promotion)
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<?>> previewOrder(
            @Valid @RequestBody OrderPreviewRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(orderService.previewOrder(userId, request)));
    }

    // Create an order from preview
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(userId, request)));
    }

    // Confirm (place) the order and optionally trigger payment/shipment
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<?>> confirmOrder(
            @PathVariable Integer orderId,
            @Valid @RequestBody ConfirmOrderRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(orderService.confirmOrder(userId, orderId, request)));
    }

    // Get order by id
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrder(
            @PathVariable Integer orderId,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(userId, orderId)));
    }

    // List orders for the authenticated user
    @GetMapping
    public ResponseEntity<ApiResponse<?>> listOrders(
            @RequestParam(value = "status", required = false) String status,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        if (status == null) {
            return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByUserId(userId)));
        }
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByStatus(userId, status)));
    }

    // Cancel order
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelOrder(
            @PathVariable Integer orderId,
            @RequestParam(value = "reason", required = false) String reason,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(userId, orderId, reason)));
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