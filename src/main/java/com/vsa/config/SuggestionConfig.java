package com.vsa.config;

import com.vsa.repository.ItemRepository;
import com.vsa.repository.ProductRepository;
import com.vsa.suggestion.CategoryBasedRecommendation;
import com.vsa.suggestion.FrequencyBasedRecommendation;
import com.vsa.suggestion.RecommendationStrategy;
import com.vsa.suggestion.SuggestionEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Wires suggestion engine strategies
@Configuration
public class SuggestionConfig {

    @Bean
    public RecommendationStrategy categoryStrategy(ProductRepository repo) {
        return new CategoryBasedRecommendation(repo);
    }

    @Bean
    public RecommendationStrategy freqStrategy(ProductRepository repo, ItemRepository itemRepo) {
        return new FrequencyBasedRecommendation(repo, itemRepo);
    }

    @Bean
    public SuggestionEngine suggestionEngine(RecommendationStrategy categoryStrategy,
                                             RecommendationStrategy freqStrategy) {
        return new SuggestionEngine(categoryStrategy, freqStrategy);
    }
}
