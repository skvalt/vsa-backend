package com.vsa.service;

import com.vsa.exceptions.ItemNotFoundException;
import com.vsa.model.Item;
import com.vsa.model.ShoppingList;
import com.vsa.repository.ItemRepository;
import com.vsa.repository.ListRepository;
import com.vsa.repository.UserRepository;
import com.vsa.model.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ListService {

    private final ListRepository listRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ListService(ListRepository listRepository,
                       ItemRepository itemRepository,
                       UserRepository userRepository) {
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public ShoppingList createList(String name, Optional<String> authUsername) {
        if (authUsername.isEmpty()) {
            throw new IllegalArgumentException("User must be authenticated");
        }

        User user = userRepository.findByUsername(authUsername.get())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ShoppingList list = ShoppingList.builder()
                .userId(user.getId())
                .name(name)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return listRepository.save(list);
    }

    public List<ShoppingList> getUserLists(Optional<String> authUsername) {
        if (authUsername.isEmpty()) {
            throw new IllegalArgumentException("Not authorized");
        }
        User user = userRepository.findByUsername(authUsername.get())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return listRepository.findByUserId(user.getId());
    }

    public ShoppingList getList(String id, Optional<String> authUsername) {
        ShoppingList list = listRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("List not found"));

        checkOwnership(list, authUsername);
        return list;
    }

    public ShoppingList renameList(String id, String newName, Optional<String> authUsername) {
        ShoppingList list = getList(id, authUsername);
        list.setName(newName);
        list.setUpdatedAt(Instant.now());
        return listRepository.save(list);
    }

    public void deleteList(String id, Optional<String> authUsername) {
        ShoppingList list = getList(id, authUsername);
        listRepository.deleteById(id);
    }

    public ShoppingList addItemToList(String listId, String itemId, Optional<String> authUsername) {
        ShoppingList list = getList(listId, authUsername);

        // ensure item exists
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        list.getItemIds().add(itemId);
        list.setUpdatedAt(Instant.now());
        return listRepository.save(list);
    }

    public ShoppingList removeItemFromList(String listId, String itemId, Optional<String> authUsername) {
        ShoppingList list = getList(listId, authUsername);

        list.getItemIds().remove(itemId);
        list.setUpdatedAt(Instant.now());
        return listRepository.save(list);
    }

    private void checkOwnership(ShoppingList list, Optional<String> authUsername) {
        if (authUsername.isPresent()) {
            User user = userRepository.findByUsername(authUsername.get())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!list.getUserId().equals(user.getId())) {
                throw new IllegalArgumentException("Not authorized to access this list");
            }
        }
    }
}
