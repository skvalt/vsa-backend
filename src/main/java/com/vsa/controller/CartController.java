package com.vsa.controller;

import com.vsa.model.Cart;
import com.vsa.model.CartItem;
import com.vsa.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getCart(@RequestParam String userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody CartItem item) {
        return ResponseEntity.ok(cartService.addToCart(item));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> updateQty(
            @PathVariable String itemId,
            @RequestBody Map<String, Object> body) {

        int qty = Integer.parseInt(String.valueOf(body.get("quantity")));
        Cart updated = cartService.updateQuantityByItemId(itemId, qty);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> delete(@PathVariable String itemId) {
        cartService.removeItemByItemId(itemId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
