package com.vsa.repository;

import com.vsa.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategoryIgnoreCase(String category);

    Optional<Product> findByNameIgnoreCase(String name);

    List<Product> findByPriceBetween(double min, double max);

    // regex search for product name
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Product> findByNameRegex(String regex);

    // tag-based lookup
    @Query("{ 'tags': { $in: [?0] } }")
    List<Product> findByTagsIgnoreCase(String tag);
}
