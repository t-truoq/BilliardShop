package swd.billiardshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.Cart;
import swd.billiardshop.service.CartService;
import java.util.List;

@RestController
@RequestMapping("/carts")
public class CartController {
    @Autowired
    private CartService cartService;



    @GetMapping
    public List<Cart> getAllCarts() {
        return cartService.getAllCarts();
    }
}
