package com.vsa.suggestion;

import com.vsa.model.Product;

import java.util.List;

public interface RecommendationStrategy {
    List<Product> recommend(String category);
}
