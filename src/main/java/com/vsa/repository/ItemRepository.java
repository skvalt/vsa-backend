package com.vsa.repository;

import com.vsa.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item, String> {
    List<Item> findByUserId(String userId);
    List<Item> findByUserIdAndCategory(String userId, String category);

    Optional<Item> findFirstByUserIdAndNameIgnoreCase(String userId, String name);
    List<Item> findByNameIgnoreCase(String name);

    Optional<Item> findById(String id);
}
