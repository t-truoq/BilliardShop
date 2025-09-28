package swd.billiardshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.dto.request.*;
import swd.billiardshop.dto.response.*;
import swd.billiardshop.entity.*;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import swd.billiardshop.enums.OrderStatus;


@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductService promotionService; // optional - use reflection

    @Autowired
    private ShipmentService shippingService; // optional - use reflection

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryLogService inventoryService; // optional - use reflection

    public OrderPreviewResponse previewOrder(Integer userId, OrderPreviewRequest request) {
        userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        CartResponse cart;
        if (request.getSelectedItems() != null && !request.getSelectedItems().isEmpty()) {
            cart = cartService.getCartSelection(userId, request.getSelectedItems());
        } else {
            cart = cartService.validateCartForCheckout(userId);
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_ADDRESS_INVALID));
        if (!userId.equals(address.getUser().getUserId())) throw new AppException(ErrorCode.FORBIDDEN);

        BigDecimal subtotal = cart.getTotalAmount();

        BigDecimal shippingCost = BigDecimal.ZERO;
        if (shippingService != null) {
            // let AppException (e.g. LOCATION_NOT_MAPPED) propagate so client sees mapping issues during preview
            try {
                shippingCost = (BigDecimal) shippingService.getClass()
                        .getMethod("calculateShippingFee", String.class, Address.class, List.class)
                        .invoke(shippingService, request.getShippingMethod(), address, cart.getItems());
            } catch (java.lang.reflect.InvocationTargetException ite) {
                // unwrap and rethrow underlying AppException if present
                Throwable cause = ite.getCause();
                if (cause instanceof AppException) throw (AppException) cause;
                throw new AppException(ErrorCode.GHN_API_ERROR, "Lỗi khi tính phí vận chuyển");
            } catch (Exception e) {
                throw new AppException(ErrorCode.GHN_API_ERROR, "Lỗi khi tính phí vận chuyển");
            }
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        PromotionResponse appliedPromotion = null;
        if (request.getPromotionCode() != null && !request.getPromotionCode().trim().isEmpty() && promotionService != null) {
            try {
                appliedPromotion = (PromotionResponse) promotionService.getClass()
                        .getMethod("validatePromotion", String.class, Integer.class, BigDecimal.class, BigDecimal.class)
                        .invoke(promotionService, request.getPromotionCode(), userId, subtotal, shippingCost);
                discountAmount = (BigDecimal) promotionService.getClass()
                        .getMethod("calculateDiscount", PromotionResponse.class, BigDecimal.class, BigDecimal.class)
                        .invoke(promotionService, appliedPromotion, subtotal, shippingCost);
            } catch (Exception e) {
                appliedPromotion = null;
                discountAmount = BigDecimal.ZERO;
            }
        }

        BigDecimal totalAmount = subtotal.add(shippingCost).subtract(discountAmount);

        OrderPreviewResponse resp = new OrderPreviewResponse();
        resp.setItems(buildOrderItemResponses(cart.getItems()));
        resp.setShippingAddress(buildAddressResponse(address));
        resp.setShippingMethod(request.getShippingMethod());
        resp.setShippingCost(shippingCost);
        resp.setSubtotal(subtotal);
        resp.setAppliedPromotion(appliedPromotion);
        resp.setDiscountAmount(discountAmount);
        resp.setTotalAmount(totalAmount);
        if (shippingService != null) {
            try {
                Object est = shippingService.getClass().getMethod("getEstimatedDelivery", String.class).invoke(shippingService, request.getShippingMethod());
                resp.setEstimatedDelivery(est != null ? est.toString() : null);
            } catch (Exception ignored) {
                resp.setEstimatedDelivery(null);
            }
        } else {
            resp.setEstimatedDelivery(null);
        }

        return resp;
    }

    public OrderResponse createOrder(Integer userId, CreateOrderRequest request) {
    OrderPreviewRequest previewRequest = new OrderPreviewRequest();
    previewRequest.setAddressId(request.getAddressId());
    previewRequest.setPromotionCode(request.getPromotionCode());
    previewRequest.setShippingMethod(request.getShippingMethod());
    previewRequest.setSelectedItems(request.getSelectedItems());

    OrderPreviewResponse preview = previewOrder(userId, previewRequest);

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Address address = addressRepository.findById(request.getAddressId()).orElseThrow(() -> new AppException(ErrorCode.SHIPPING_ADDRESS_INVALID));

        Order order = new Order();
    order.setOrderNumber(generateOrderNumber());
    order.setUser(user);
    order.setStatus(swd.billiardshop.enums.OrderStatus.PENDING);
    order.setPaymentStatus(swd.billiardshop.enums.PaymentStatus.PENDING);

        order.setCustomerName(user.getFullName());
        order.setCustomerEmail(user.getEmail());
        order.setCustomerPhone(user.getPhone());

        order.setShippingAddress(formatAddress(address));
    order.setShippingMethod(request.getShippingMethod());
        order.setShippingCost(preview.getShippingCost());

        order.setSubtotal(preview.getSubtotal());
        order.setDiscountAmount(preview.getDiscountAmount());
        order.setTotalAmount(preview.getTotalAmount());

        order.setNotes(request.getNotes());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

    order = orderRepository.save(order);

        CartResponse selectedCart;
        if (request.getSelectedItems() != null && !request.getSelectedItems().isEmpty()) {
            selectedCart = cartService.getCartSelection(userId, request.getSelectedItems());
        } else {
            selectedCart = cartService.getCartByUserId(userId);
        }
        for (var cartItem : selectedCart.getItems()) {
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(productRepository.findById(cartItem.getProductId()).orElse(null))
                    .productName(cartItem.getProductName())
                    .productSku(cartItem.getProductSku())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .totalPrice(cartItem.getTotalPrice())
                    .createdAt(LocalDateTime.now())
                    .build();
            orderItemRepository.save(oi);
        }

        if (inventoryService != null) {
            try {
                inventoryService.getClass().getMethod("reserveStock", Integer.class, List.class)
                        .invoke(inventoryService, order.getOrderId(), selectedCart.getItems());
            } catch (Exception ignored) {}
        }

        if (preview.getAppliedPromotion() != null && promotionService != null) {
            try {
                promotionService.getClass().getMethod("usePromotion", Integer.class, Integer.class, Integer.class, BigDecimal.class)
                        .invoke(promotionService, preview.getAppliedPromotion().getPromotionId(), userId, order.getOrderId(), preview.getDiscountAmount());
            } catch (Exception ignored) {}
        }

        if (request.getSelectedItems() != null && !request.getSelectedItems().isEmpty()) {
            java.util.List<Integer> ids = request.getSelectedItems().stream().map(s -> s.getCartItemId()).collect(java.util.stream.Collectors.toList());
            cartService.removeItemsFromCart(userId, ids);
        } else {
            cartService.clearCart(userId);
        }

        return getOrderById(userId, order.getOrderId());
    }

    public OrderResponse confirmOrder(Integer userId, Integer orderId, ConfirmOrderRequest request) {
        Order order = getOrderAndValidateOwnership(userId, orderId);

    if (order.getStatus() != swd.billiardshop.enums.OrderStatus.PENDING) throw new AppException(ErrorCode.ORDER_ALREADY_CONFIRMED);

    order.setStatus(swd.billiardshop.enums.OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // create payment record (not persisted here) and trigger shipment if necessary
    createPaymentRecord(order, request.getPaymentMethod());

        if (!"cash".equalsIgnoreCase(request.getPaymentMethod()) && shippingService != null) {
            try {
                shippingService.getClass().getMethod("createShipment", Order.class).invoke(shippingService, order);
            } catch (Exception ignored) {}
        }

        return getOrderById(userId, orderId);
    }

    public OrderResponse getOrderById(Integer userId, Integer orderId) {
        Order order = getOrderAndValidateOwnership(userId, orderId);
        return buildOrderResponse(order);
    }

    public java.util.List<OrderResponse> getOrdersByUserId(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        java.util.List<Order> orders = orderRepository.findByUser(user);
        return orders.stream().map(this::buildOrderResponse).collect(Collectors.toList());
    }

    public java.util.List<OrderResponse> getOrdersByStatus(Integer userId, String status) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // status is an enum in Order entity, so filter in-memory for now
        java.util.List<Order> orders = orderRepository.findByUser(user);
        return orders.stream().filter(o -> o.getStatus() != null && o.getStatus().name().equalsIgnoreCase(status))
                .map(this::buildOrderResponse).collect(Collectors.toList());
    }

    public java.util.List<OrderResponse> getAllOrders() {
        java.util.List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::buildOrderResponse).collect(Collectors.toList());
    }

    public OrderResponse cancelOrder(Integer userId, Integer orderId, String reason) {
        Order order = getOrderAndValidateOwnership(userId, orderId);
        if (!canCancelOrder(order.getStatus())) throw new AppException(ErrorCode.ORDER_CANNOT_CANCEL);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setAdminNotes(reason);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        if (inventoryService != null) {
            try {
                inventoryService.getClass().getMethod("releaseReservedStock", Integer.class).invoke(inventoryService, orderId);
            } catch (Exception ignored) {}
        }
        if (shippingService != null) {
            try {
                shippingService.getClass().getMethod("cancelShipment", Integer.class).invoke(shippingService, orderId);
            } catch (Exception ignored) {}
        }
        if (promotionService != null) {
            try {
                promotionService.getClass().getMethod("refundPromotionUsage", Integer.class).invoke(promotionService, orderId);
            } catch (Exception ignored) {}
        }

        return buildOrderResponse(order);
    }

    // helpers
    private Order getOrderAndValidateOwnership(Integer userId, Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getUser() == null || !userId.equals(order.getUser().getUserId())) throw new AppException(ErrorCode.FORBIDDEN);
        return order;
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 1000));
        return "ORD" + timestamp + random;
    }

    private String formatAddress(Address address) {
        return String.format("%s, %s, %s, %s, %s",
                address.getAddressLine(),
                address.getWard(),
                address.getDistrict(),
                address.getCity(),
                address.getProvince());
    }

    private boolean canCancelOrder(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    private OrderResponse buildOrderResponse(Order order) {
    List<OrderItem> items = orderItemRepository.findByOrder(order);
        OrderResponse resp = new OrderResponse();
        resp.setOrderId(order.getOrderId());
        resp.setOrderNumber(order.getOrderNumber());
    resp.setUserId(order.getUser() != null ? order.getUser().getUserId() : null);
    resp.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
    resp.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        resp.setCustomerName(order.getCustomerName());
        resp.setCustomerEmail(order.getCustomerEmail());
        resp.setCustomerPhone(order.getCustomerPhone());
        resp.setShippingAddress(order.getShippingAddress());
        resp.setSubtotal(order.getSubtotal());
        resp.setShippingCost(order.getShippingCost());
        resp.setDiscountAmount(order.getDiscountAmount());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setShippingMethod(order.getShippingMethod());
        resp.setNotes(order.getNotes());
        resp.setAdminNotes(order.getAdminNotes());
        resp.setCreatedAt(order.getCreatedAt());
        resp.setConfirmedAt(order.getConfirmedAt());
        resp.setShippedAt(order.getShippedAt());
        resp.setDeliveredAt(order.getDeliveredAt());
        resp.setCancelledAt(order.getCancelledAt());

        resp.setItems(items.stream().map(this::buildOrderItemResponse).collect(Collectors.toList()));
        resp.setTotalItems(items.stream().mapToInt(OrderItem::getQuantity).sum());
        return resp;
    }

    private OrderItemResponse buildOrderItemResponse(OrderItem item) {
        OrderItemResponse r = new OrderItemResponse();
        r.setItemId(item.getItemId());
        r.setProductId(item.getProduct() != null ? item.getProduct().getProductId() : null);
        r.setProductName(item.getProductName());
        r.setProductSku(item.getProductSku());
        r.setQuantity(item.getQuantity());
        r.setUnitPrice(item.getUnitPrice());
        r.setTotalPrice(item.getTotalPrice());
        return r;
    }

    private List<OrderItemResponse> buildOrderItemResponses(List<CartItemResponse> cartItems) {
        return cartItems.stream().map(ci -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setProductId(ci.getProductId());
            r.setProductName(ci.getProductName());
            r.setProductSku(ci.getProductSku());
            r.setProductImage(ci.getProductImage());
            r.setQuantity(ci.getQuantity());
            r.setUnitPrice(ci.getUnitPrice());
            r.setTotalPrice(ci.getTotalPrice());
            return r;
        }).collect(Collectors.toList());
    }

    private AddressResponse buildAddressResponse(Address address) {
        AddressResponse r = new AddressResponse();
        r.setAddressId(address.getAddressId());
        r.setRecipientName(address.getRecipientName());
        r.setPhone(address.getPhone());
        r.setAddressLine(address.getAddressLine());
        r.setWard(address.getWard());
        r.setDistrict(address.getDistrict());
        r.setCity(address.getCity());
        r.setProvince(address.getProvince());
        r.setPostalCode(address.getPostalCode());
        r.setIsDefault(address.getIsDefault());
        return r;
    }

    private PaymentResponse createPaymentRecord(Order order, String paymentMethod) {
        PaymentResponse p = new PaymentResponse();
        p.setPaymentCode("PAY" + order.getOrderNumber());
        p.setOrderId(order.getOrderId());
        p.setAmount(order.getTotalAmount());
        p.setMethod(paymentMethod);
        p.setStatus("pending");
        return p;
    }
}
