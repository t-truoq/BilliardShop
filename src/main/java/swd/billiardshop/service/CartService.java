package swd.billiardshop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.dto.request.AddToCartRequest;
import swd.billiardshop.dto.request.UpdateCartItemRequest;
import swd.billiardshop.dto.response.CartItemResponse;
import swd.billiardshop.dto.response.CartResponse;
import swd.billiardshop.entity.Cart;
import swd.billiardshop.entity.CartItem;
import swd.billiardshop.entity.Product;
import swd.billiardshop.entity.User;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.repository.CartItemRepository;
import swd.billiardshop.repository.CartRepository;
import swd.billiardshop.repository.ProductRepository;
import swd.billiardshop.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse addToCart(Integer userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == null || !product.getStatus().name().equalsIgnoreCase("ACTIVE")) {
            throw new AppException(ErrorCode.PRODUCT_INACTIVE);
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Chỉ còn " + product.getStockQuantity() + " sản phẩm trong kho");
        }

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> existing = cartItemRepository.findByCartCartIdAndProductProductId(cart.getCartId(), request.getProductId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQty) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Tổng số lượng vượt quá hàng tồn kho. Còn lại: " + product.getStockQuantity());
            }
            item.setQuantity(newQty);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(newQty)));
            item.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                    .addedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            cartItemRepository.save(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartByUserId(userId);
    }

    public CartResponse updateCartItem(Integer userId, Integer itemId, UpdateCartItemRequest request) {
        userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        Cart cart = cartRepository.findById(item.getCart().getCartId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (!userId.equals(cart.getUser().getUserId())) throw new AppException(ErrorCode.FORBIDDEN);

        Product product = productRepository.findById(item.getProduct().getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Chỉ còn " + product.getStockQuantity() + " sản phẩm trong kho");
        }

        item.setQuantity(request.getQuantity());
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        item.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(item);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartByUserId(userId);
    }

    public CartResponse removeCartItem(Integer userId, Integer itemId) {
        userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        Cart cart = cartRepository.findById(item.getCart().getCartId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (!userId.equals(cart.getUser().getUserId())) throw new AppException(ErrorCode.FORBIDDEN);

        cartItemRepository.delete(item);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartByUserId(userId);
    }

    public CartResponse getCartByUserId(Integer userId) {
        userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Optional<Cart> cartOpt = cartRepository.findByUserUserId(userId);
        if (cartOpt.isEmpty()) {
            CartResponse resp = new CartResponse();
            resp.setUserId(userId);
            resp.setItems(List.of());
            resp.setTotalItems(0);
            resp.setTotalAmount(BigDecimal.ZERO);
            return resp;
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cartItemRepository.findByCartCartIdOrderByAddedAtDesc(cart.getCartId());
        return buildCartResponse(cart, items);
    }

    public void clearCart(Integer userId) {
        userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Optional<Cart> cartOpt = cartRepository.findByUserUserId(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCartCartId(cart.getCartId());
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    public CartResponse validateCartForCheckout(Integer userId) {
        CartResponse cr = getCartByUserId(userId);
        if (cr.getItems() == null || cr.getItems().isEmpty()) throw new AppException(ErrorCode.CART_EMPTY);

        for (var item : cr.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            if (product.getStatus() == null || !product.getStatus().name().equalsIgnoreCase("ACTIVE")) {
                throw new AppException(ErrorCode.PRODUCT_INACTIVE, "Sản phẩm " + product.getName() + " không còn bán");
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Sản phẩm " + product.getName() + " chỉ còn " + product.getStockQuantity() + " trong kho");
            }

            if (!product.getPrice().equals(item.getUnitPrice())) {
                updateCartItemPrice(userId, item.getItemId(), product.getPrice());
            }
        }

        return getCartByUserId(userId);
    }

    /**
     * Return a CartResponse that contains only the selected cart items (with requested quantities).
     * Throws AppException if any cart item is invalid or not owned by user.
     */
    public CartResponse getCartSelection(Integer userId, java.util.List<swd.billiardshop.dto.request.SelectedCartItem> selected) {
        userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (selected == null || selected.isEmpty()) return getCartByUserId(userId);

        CartResponse full = getCartByUserId(userId);
        java.util.Map<Integer, CartItemResponse> map = full.getItems().stream().collect(Collectors.toMap(CartItemResponse::getItemId, c -> c));

        java.util.List<CartItemResponse> resultItems = new java.util.ArrayList<>();
        for (swd.billiardshop.dto.request.SelectedCartItem sel : selected) {
            if (sel == null || sel.getCartItemId() == null) continue;
            CartItemResponse ex = map.get(sel.getCartItemId());
            if (ex == null) throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found: " + sel.getCartItemId());
            int qty = sel.getQuantity() == null ? ex.getQuantity() : sel.getQuantity();
            if (qty <= 0) qty = 1;
            if (qty > ex.getQuantity()) qty = ex.getQuantity(); // clamp

            CartItemResponse copy = new CartItemResponse();
            copy.setItemId(ex.getItemId());
            copy.setProductId(ex.getProductId());
            copy.setProductName(ex.getProductName());
            copy.setProductSlug(ex.getProductSlug());
            copy.setProductImage(ex.getProductImage());
            copy.setProductSku(ex.getProductSku());
            copy.setUnitPrice(ex.getUnitPrice());
            copy.setQuantity(qty);
            copy.setTotalPrice(ex.getUnitPrice().multiply(new BigDecimal(qty)));
            copy.setAvailableStock(ex.getAvailableStock());
            resultItems.add(copy);
        }

        CartResponse resp = new CartResponse();
        resp.setUserId(userId);
        resp.setItems(resultItems);
        resp.setTotalItems(resultItems.stream().mapToInt(CartItemResponse::getQuantity).sum());
        resp.setTotalAmount(resultItems.stream().map(CartItemResponse::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        return resp;
    }

    /**
     * Remove the specified cart item ids from the user's cart (ownership checked).
     */
    public void removeItemsFromCart(Integer userId, java.util.List<Integer> itemIds) {
        userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (itemIds == null || itemIds.isEmpty()) return;
        for (Integer id : itemIds) {
            CartItem it = cartItemRepository.findById(id).orElse(null);
            if (it == null) continue;
            if (it.getCart() == null || it.getCart().getUser() == null || !userId.equals(it.getCart().getUser().getUserId())) continue;
            cartItemRepository.delete(it);
        }
        Optional<Cart> cOpt = cartRepository.findByUserUserId(userId);
        if (cOpt.isPresent()) { Cart c = cOpt.get(); c.setUpdatedAt(LocalDateTime.now()); cartRepository.save(c); }
    }

    // Helpers
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserUserId(user.getUserId()).orElseGet(() -> {
            Cart c = Cart.builder()
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return cartRepository.save(c);
        });
    }

    private CartResponse buildCartResponse(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(it -> {
            CartItemResponse r = new CartItemResponse();
            r.setItemId(it.getItemId());
            r.setProductId(it.getProduct().getProductId());
            r.setProductName(it.getProduct().getName());
            r.setProductSlug(it.getProduct().getSlug());
            r.setProductSku(it.getProduct().getSku());
            r.setUnitPrice(it.getUnitPrice());
            r.setQuantity(it.getQuantity());
            r.setTotalPrice(it.getTotalPrice());
            r.setAvailableStock(it.getProduct().getStockQuantity());
            r.setAddedAt(it.getAddedAt());
            r.setUpdatedAt(it.getUpdatedAt());
            return r;
        }).collect(Collectors.toList());

        CartResponse resp = new CartResponse();
        resp.setCartId(cart.getCartId());
        resp.setUserId(cart.getUser().getUserId());
        resp.setItems(itemResponses);
        resp.setTotalItems(items.stream().mapToInt(CartItem::getQuantity).sum());
        resp.setTotalAmount(items.stream().map(CartItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        resp.setUpdatedAt(cart.getUpdatedAt());
        return resp;
    }

    private void updateCartItemPrice(Integer userId, Integer itemId, BigDecimal newPrice) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        item.setUnitPrice(newPrice);
        item.setTotalPrice(newPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        item.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(item);
    }
}
