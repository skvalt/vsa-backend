package com.vsa.controller;

import com.vsa.model.ShoppingList;
import com.vsa.service.ListService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lists")
public class ListController {

    private final ListService listService;

    public ListController(ListService listService) {
        this.listService = listService;
    }

    @PostMapping
    public ResponseEntity<ShoppingList> createList(
            @RequestParam @NotBlank String name,
            Authentication auth
    ) {
        Optional<String> username = Optional.ofNullable(auth).map(Authentication::getName);
        return ResponseEntity.status(201).body(listService.createList(name, username));
    }

    @GetMapping
    public ResponseEntity<List<ShoppingList>> getMyLists(Authentication auth) {
        Optional<String> username = Optional.ofNullable(auth).map(Authentication::getName);
        return ResponseEntity.ok(listService.getUserLists(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingList> getList(@PathVariable String id, Authentication auth) {
        Optional<String> username = Optional.ofNullable(auth).map(Authentication::getName);
        return ResponseEntity.ok(listService.getList(id, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingList> renameList(
            @PathVariable String id,
            @RequestParam String name,
            Authentication auth
    ) {
        Optional<String> username = Optional.ofNullable(auth).map(Authentication::getName);
        return ResponseEntity.ok(listService.renameList(id, name, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteList(@PathVariable String id, Authentication auth) {
        Optional<String> username = Optional.ofNullable(auth).map(Authentication::getName);
        listService.deleteList(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{listId}/items/{itemId}")
    public ResponseEntity<ShoppingList> addItem(
            @PathVariable String listId,
            @PathVariable String itemId,
            Authentication auth
    ) {
        Optional<String> username = Optional.ofNullable(auth).map(Authentication::getName);
        return ResponseEntity.ok(listService.addItemToList(listId, itemId, username));
    }

    @DeleteMapping("/{listId}/items/{itemId}")
    public ResponseEntity<ShoppingList> removeItem(
            @PathVariable String listId,
            @PathVariable String itemId,
            Authentication auth
    ) {
        Optional<String> username = Optional.ofNullable(auth).map(Authentication::getName);
        return ResponseEntity.ok(listService.removeItemFromList(listId, itemId, username));
    }
}
