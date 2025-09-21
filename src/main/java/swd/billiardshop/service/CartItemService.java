package swd.billiardshop.service;

import lombok.Builder;
import org.springframework.stereotype.Service;
import swd.billiardshop.entity.CartItem;
import java.util.List;
@Builder
@Service
public class CartItemService {
    public List<CartItem> getAllCartItems() {
        return null;
    }
}
