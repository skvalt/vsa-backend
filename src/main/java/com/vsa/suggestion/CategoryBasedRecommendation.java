package com.vsa.suggestion;

import com.vsa.model.Product;
import com.vsa.repository.ProductRepository;

import java.util.List;

//Suggest items by category popularity.

public class CategoryBasedRecommendation implements RecommendationStrategy {

    private final ProductRepository repo;

    public CategoryBasedRecommendation(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Product> recommend(String category) {
        return repo.findByCategoryIgnoreCase(category);
    }
}
