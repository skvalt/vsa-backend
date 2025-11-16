package com.vsa.controller;

import com.vsa.model.Item;
import com.vsa.model.dto.AddItemRequest;
import com.vsa.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }


    // CREATE ITEM
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody AddItemRequest req,
                                           Authentication authentication) {

        Optional<String> username = Optional.ofNullable(authentication)
                .map(Authentication::getName);

        Item saved = itemService.addItem(req, username);
        return ResponseEntity.status(201).body(saved);
    }


    // GET ITEMS FOR USER
    @GetMapping
    public ResponseEntity<List<Item>> listItems(
            @RequestParam(required = false) String userId,
            Authentication authentication) {

        Optional<String> username = Optional.ofNullable(authentication)
                .map(Authentication::getName);

        String resolvedUserId = username.isPresent() ? null : userId;

        List<Item> items = itemService.getItemsForUser(resolvedUserId);
        return ResponseEntity.ok(items);
    }

    // UPDATE FULL ITEM
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable String id,
            @Valid @RequestBody AddItemRequest req,
            Authentication authentication) {

        Optional<String> username = Optional.ofNullable(authentication)
                .map(Authentication::getName);

        Item updated = itemService.updateItem(id, req, username);
        return ResponseEntity.ok(updated);
    }


    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id,
                                           Authentication authentication) {

        Optional<String> username = Optional.ofNullable(authentication)
                .map(Authentication::getName);

        itemService.deleteItem(id, username);
        return ResponseEntity.noContent().build();
    }

    // CHECK/UNCHECK
    @PostMapping("/{id}/check")
    public ResponseEntity<Item> checkItem(
            @PathVariable String id,
            @RequestParam boolean checked,
            Authentication authentication) {

        Optional<String> username = Optional.ofNullable(authentication)
                .map(Authentication::getName);

        Item updated = itemService.toggleCheck(id, checked, username);
        return ResponseEntity.ok(updated);
    }
}
