package com.vsa.controller;

import com.vsa.model.Cart;
import com.vsa.model.CartItem;
import com.vsa.model.Product;
import com.vsa.repository.ProductRepository;
import com.vsa.service.CartService;
import com.vsa.service.NlpService;
import com.vsa.service.UndoService;
import com.vsa.util.ItemCategoryMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final NlpService nlpService;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final UndoService undoService;

    public VoiceController(NlpService nlpService,
                           CartService cartService,
                           ProductRepository productRepository,
                           UndoService undoService) {
        this.nlpService = nlpService;
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.undoService = undoService;
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parseVoice(@RequestBody Map<String, Object> body) {
        String text = body.getOrDefault("text", "").toString();
        return ResponseEntity.ok(nlpService.parse(text));
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyIntent(@RequestBody Map<String, Object> payload,
                                         Authentication auth) {

        String userId = (String) payload.getOrDefault("userId", null);
        String intent = String.valueOf(payload.getOrDefault("intent", "")).toLowerCase();

        Map<String, String> entities = extractEntities(payload);
        List<Map<String, Object>> matches = extractMatches(payload);

        return switch (intent) {
            case "add_item" -> handleAddItem(userId, entities, matches);
            case "remove_item" -> handleRemoveItem(userId, entities, matches);
            case "update_quantity" -> handleUpdateQty(userId, entities, matches);
            default -> ResponseEntity.badRequest().body(Map.of("error", "Unsupported intent"));
        };
    }

    private Map<String, String> extractEntities(Map<String, Object> payload) {
        Map<String, String> map = new HashMap<>();
        if (payload.get("entities") instanceof Map<?, ?> raw) {
            raw.forEach((k, v) -> map.put(k.toString(), v == null ? null : v.toString()));
        }
        return map;
    }

    private List<Map<String, Object>> extractMatches(Map<String, Object> payload) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (payload.get("matches") instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> map) out.add((Map<String, Object>) map);
            }
        }
        return out;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        if (t.endsWith("s") && t.length() > 1) t = t.substring(0, t.length() - 1);
        return t.replace("-", " ").replace("_", " ").replaceAll("\\s+", " ");
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            return s == null ? fallback : Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    // add item
    private ResponseEntity<?> handleAddItem(String userId,
                                            Map<String, String> entities,
                                            List<Map<String, Object>> matches) {

        String nameRaw = entities.get("product");
        String nameNorm = normalize(nameRaw);
        int qty = parseIntSafe(entities.get("quantity"), 1);

        Product p = null;

        if (!matches.isEmpty()) {
            String id = String.valueOf(matches.get(0).get("id"));
            p = productRepository.findById(id).orElse(null);
        }

        if (p == null && nameNorm != null) {
            p = productRepository.findByNameIgnoreCase(nameNorm).orElse(null);
        }

        String finalName = (p != null ? p.getName() : nameNorm);

        CartItem ci = CartItem.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .productId(p != null ? p.getId() : null)
                .name(finalName)
                .category(p != null ? p.getCategory() : ItemCategoryMapper.mapToCategory(finalName))
                .unit(p != null ? p.getUnit() : null)
                .price(p != null ? p.getPrice() : null)
                .quantity(qty)
                .build();

        Cart updated = cartService.addToCart(ci);
        return ResponseEntity.ok(updated);
    }

    // remove item
    private ResponseEntity<?> handleRemoveItem(String userId,
                                               Map<String, String> entities,
                                               List<Map<String, Object>> matches) {

        String nameNorm = normalize(entities.get("product"));
        int qty = parseIntSafe(entities.get("quantity"), 1);

        Cart cart = cartService.getCart(userId);

        CartItem found = findItem(cart, matches, nameNorm);
        if (found == null) return ResponseEntity.status(404).body(Map.of("error", "not found"));

        int newQty = found.getQuantity() - qty;

        if (newQty > 0) {
            Cart updated = cartService.updateQuantityByItemId(found.getId(), newQty);
            return ResponseEntity.ok(updated);
        } else {
            Cart updated = cartService.removeItemByItemId(found.getId());
            return ResponseEntity.ok(Map.of("removed", found.getName(), "cart", updated));
        }
    }

    // update quantity
    private ResponseEntity<?> handleUpdateQty(String userId,
                                              Map<String, String> entities,
                                              List<Map<String, Object>> matches) {

        String nameNorm = normalize(entities.get("product"));
        int qty = parseIntSafe(entities.get("quantity"), -1);
        if (qty < 0) return ResponseEntity.badRequest().body(Map.of("error", "invalid qty"));

        Cart cart = cartService.getCart(userId);

        CartItem found = findItem(cart, matches, nameNorm);
        if (found == null) return ResponseEntity.status(404).body(Map.of("error", "not found"));

        Cart updated = cartService.updateQuantityByItemId(found.getId(), qty);
        return ResponseEntity.ok(updated);
    }

    private CartItem findItem(Cart cart,
                              List<Map<String, Object>> matches,
                              String nameNorm) {

        String matchId = null;
        if (!matches.isEmpty() && matches.get(0).get("id") != null) {
            matchId = String.valueOf(matches.get(0).get("id"));
        }

        for (CartItem i : cart.getItems()) {
            if (matchId != null && matchId.equals(i.getProductId())) return i;
        }

        for (CartItem i : cart.getItems()) {
            if (normalize(i.getName()).equals(nameNorm)) return i;
        }

        return null;
    }
}
