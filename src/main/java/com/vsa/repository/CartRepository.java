package com.vsa.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.vsa.model.Cart;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);

    @Query("{ 'items._id': ?0 }")
Optional<Cart> findByItemId(String itemId);

    Optional<Cart> findByItemsId(String itemId);
    
}
