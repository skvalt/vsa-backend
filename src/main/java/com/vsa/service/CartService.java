package com.vsa.service;

import com.vsa.model.Cart;
import com.vsa.model.CartItem;
import com.vsa.model.Product;
import com.vsa.repository.CartRepository;
import com.vsa.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getCart(String userId) {
        return getOrCreateCart(userId);
    }

    public Cart getOrCreateCart(String userId) {
        if (userId == null)
            throw new RuntimeException("userId required for cart");

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = Cart.builder()
                            .userId(userId)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return cartRepository.save(c);
                });
    }


    public Cart addToCart(CartItem item) {

        Cart cart = getOrCreateCart(item.getUserId());

        if (item.getProductId() != null) {
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                if (item.getName() == null) item.setName(p.getName());
                if (item.getPrice() == null) item.setPrice(p.getPrice());
                if (item.getUnit() == null) item.setUnit(p.getUnit());
                if (item.getCategory() == null) item.setCategory(p.getCategory());
            });
        }

        // Normalize matching: prefer productId match, else name 
        CartItem existing = cart.getItems().stream()
                .filter(ci ->
                        (item.getProductId() != null && item.getProductId().equals(ci.getProductId())) ||
                        (item.getName() != null && ci.getName() != null && ci.getName().equalsIgnoreCase(item.getName()))
                )
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
            existing.setPrice(item.getPrice() != null ? item.getPrice() : existing.getPrice());
            existing.setUpdatedAt(Instant.now());
        } else {
            // ensure item has an id for later updates/removes
            if (item.getId() == null || item.getId().isBlank()) {
                item.setId(UUID.randomUUID().toString());
            }
            item.setAddedAt(Instant.now());
            item.setUpdatedAt(Instant.now());
            cart.getItems().add(0, item);
        }

        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }

    //Update quantity of an item identified by embedded item id (itemId).

    public Cart updateQuantityByItemId(String itemId, int qty) {
        if (itemId == null) throw new IllegalArgumentException("itemId required");

        Cart cart = cartRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Cart not found for item"));

        boolean changed = false;
        for (CartItem ci : cart.getItems()) {
            if (itemId.equals(ci.getId())) {
                ci.setQuantity(qty);
                ci.setUpdatedAt(Instant.now());
                changed = true;
                break;
            }
        }

        if (!changed) throw new RuntimeException("Item not found in cart");

        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }

    //Remove an item by its embedded item id and return the updated Cart.

    public Cart removeItemByItemId(String itemId) {
        if (itemId == null) throw new IllegalArgumentException("itemId required");

        Cart cart = cartRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Cart not found for item"));

        boolean removed = cart.getItems().removeIf(ci -> itemId.equals(ci.getId()));

        if (!removed) throw new RuntimeException("Item not found in cart");

        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }

    @Deprecated
    public CartItem updateQuantity(String id, int qty) {
        throw new UnsupportedOperationException("Use updateQuantityByItemId(itemId, qty) instead");
    }

    @Deprecated
    public void removeItem(String id) {
        throw new UnsupportedOperationException("Use removeItemByItemId(itemId) instead");
    }
}
