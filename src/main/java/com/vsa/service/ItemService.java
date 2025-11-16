package com.vsa.service;

import com.vsa.exceptions.ItemNotFoundException;
import com.vsa.model.Item;
import com.vsa.model.User;
import com.vsa.model.dto.AddItemRequest;
import com.vsa.repository.ItemRepository;
import com.vsa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    // ADD ITEM

    public Item addItem(AddItemRequest req, Optional<String> authUsername) {
        String resolvedUserId = resolveUserId(req.getUserId(), authUsername);

        Item item = Item.builder()
                .userId(resolvedUserId)
                .name(req.getName().trim())
                .quantity(Math.max(1, req.getQuantity()))
                .unit(req.getUnit())
                .category(req.getCategory())
                .price(req.getPrice())
                .checked(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return itemRepository.save(item);
    }

    // Generic save (used by controllers that manipulate Item objects directly)

    public Item save(Item item) {
        item.setUpdatedAt(Instant.now());
        return itemRepository.save(item);
    }


    // FIND by name helper (tries per-user first, then global)

    public Optional<Item> findByName(String productName, String userId) {

        if (userId != null) {
            Optional<Item> perUser = itemRepository.findFirstByUserIdAndNameIgnoreCase(userId, productName);
            if (perUser.isPresent()) return perUser;
        }


        List<Item> byName = itemRepository.findByNameIgnoreCase(productName);
        if (!byName.isEmpty()) return Optional.of(byName.get(0));

        return Optional.empty();
    }

    // GET ITEMS FOR USER

    public List<Item> getItemsForUser(String userId) {
        if (userId == null) {
            return itemRepository.findAll(); // if null, return all (you can change later)
        }
        return itemRepository.findByUserId(userId);
    }

    // REMOVE BY NAME

    public Optional<Item> removeItemByName(String productName,
                                           String userId,
                                           Optional<String> authUsername) {

        String resolvedUserId = resolveUserId(userId, authUsername);

        // match user's item first
        if (resolvedUserId != null) {
            Optional<Item> found = itemRepository.findFirstByUserIdAndNameIgnoreCase(resolvedUserId, productName);
            if (found.isPresent()) {
                itemRepository.deleteById(found.get().getId());
                return found;
            }
        }

        // fallback global
        List<Item> list = itemRepository.findByNameIgnoreCase(productName);
        if (!list.isEmpty()) {
            Item it = list.get(0);
            itemRepository.deleteById(it.getId());
            return Optional.of(it);
        }

        return Optional.empty();
    }

    // UPDATE QUANTITY BY NAME

    public Optional<Item> updateQuantityByName(String productName,
                                               int quantity,
                                               String userId,
                                               Optional<String> authUsername) {

        String resolvedUserId = resolveUserId(userId, authUsername);
        Optional<Item> found = Optional.empty();

        if (resolvedUserId != null) {
            found = itemRepository.findFirstByUserIdAndNameIgnoreCase(resolvedUserId, productName);
        }

        if (found.isEmpty()) {
            List<Item> list = itemRepository.findByNameIgnoreCase(productName);
            if (!list.isEmpty()) found = Optional.of(list.get(0));
        }

        if (found.isPresent()) {
            Item it = found.get();
            it.setQuantity(quantity);
            it.setUpdatedAt(Instant.now());
            return Optional.of(itemRepository.save(it));
        }

        return Optional.empty();
    }

    // CLEAR LIST FOR USER

    public List<Item> clearItemsForUser(String userId) {
        if (userId == null) return List.of();

        List<Item> items = itemRepository.findByUserId(userId);
        List<String> ids = new ArrayList<>();
        items.forEach(i -> ids.add(i.getId()));

        itemRepository.deleteAllById(ids);
        return items;
    }

    // UPDATE FULL ITEM

    public Item updateItem(String id, AddItemRequest req, Optional<String> authUsername) {
        Item existing = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        // auth check
        if (authUsername.isPresent()) {
            User u = userRepository.findByUsername(authUsername.get()).orElse(null);
            if (u != null && existing.getUserId() != null && !existing.getUserId().equals(u.getId())) {
                throw new IllegalArgumentException("Not authorized to modify this item");
            }
        }

        if (req.getName() != null) existing.setName(req.getName().trim());
        if (req.getQuantity() > 0) existing.setQuantity(req.getQuantity());
        if (req.getUnit() != null) existing.setUnit(req.getUnit());
        if (req.getCategory() != null) existing.setCategory(req.getCategory());
        if (req.getPrice() != null) existing.setPrice(req.getPrice());

        existing.setUpdatedAt(Instant.now());
        return itemRepository.save(existing);
    }

    // DELETE

    public void deleteItem(String id, Optional<String> authUsername) {
        Item existing = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        if (authUsername.isPresent()) {
            User u = userRepository.findByUsername(authUsername.get()).orElse(null);
            if (u != null && existing.getUserId() != null && !existing.getUserId().equals(u.getId())) {
                throw new IllegalArgumentException("Not authorized");
            }
        }

        itemRepository.deleteById(id);
    }

    // CHECK / UNCHECK

    public Item toggleCheck(String id, boolean checked, Optional<String> authUsername) {
        Item existing = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        if (authUsername.isPresent()) {
            User u = userRepository.findByUsername(authUsername.get()).orElse(null);
            if (u != null && existing.getUserId() != null && !existing.getUserId().equals(u.getId())) {
                throw new IllegalArgumentException("Not authorized");
            }
        }

        existing.setChecked(checked);
        existing.setUpdatedAt(Instant.now());
        return itemRepository.save(existing);
    }

    // RESOLVE USER ID

    private String resolveUserId(String userId, Optional<String> authUsername) {
        if (authUsername.isPresent()) {
            User u = userRepository.findByUsername(authUsername.get()).orElse(null);
            if (u != null) return u.getId();
        }
        return userId;
    }
}
