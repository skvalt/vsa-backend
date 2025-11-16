package com.vsa.repository;

import com.vsa.model.ShoppingList;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ListRepository extends MongoRepository<ShoppingList, String> {
    List<ShoppingList> findByUserId(String userId);
}
