package com.vsa.service;

import com.vsa.model.Product;
import com.vsa.model.Item;
import com.vsa.model.response.ParsedIntentResponse;
import com.vsa.repository.ProductRepository;
import com.vsa.repository.ItemRepository;
import com.vsa.suggestion.SuggestionEngine;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SuggestionService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final SuggestionEngine suggestionEngine;

    public SuggestionService(ProductRepository productRepository,
                             ItemRepository itemRepository,
                             SuggestionEngine suggestionEngine) {
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
        this.suggestionEngine = suggestionEngine;
    }

    public List<Product> suggest(String userId) {

        if (userId != null && !userId.isBlank()) {
            List<Item> items = itemRepository.findByUserId(userId);
            if (items != null && !items.isEmpty()) {

                Map<String, Long> counts = items.stream()
                        .filter(i -> i.getName() != null)
                        .collect(Collectors.groupingBy(
                                i -> i.getName().toLowerCase().trim(),
                                Collectors.counting()
                        ));

                if (!counts.isEmpty()) {
                    List<String> keywords = counts.entrySet().stream()
                            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                            .limit(10)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    List<Product> rec = new ArrayList<>();
                    for (String nm : keywords) {
                        rec.addAll(
                                productRepository.findByNameRegex("(?i).*" + Pattern.quote(nm) + ".*")
                        );
                    }

                    LinkedHashMap<String, Product> dedup = new LinkedHashMap<>();
                    for (Product p : rec) {
                        if (p != null && p.getId() != null) dedup.put(p.getId(), p);
                    }

                    if (!dedup.isEmpty()) return new ArrayList<>(dedup.values());
                }
            }
        }

        List<Product> all = productRepository.findAll();
        if (all == null || all.isEmpty()) return Collections.emptyList();

        Collections.shuffle(all);
        return all.stream().limit(10).collect(Collectors.toList());
    }

    public List<Product> suggestByCategory(String category) {
        if (category == null || category.isBlank()) return Collections.emptyList();
        return productRepository.findByCategoryIgnoreCase(category);
    }

    public List<Product> suggestSubstitutes(ParsedIntentResponse parsed) {
        if (parsed == null) return Collections.emptyList();
        if (suggestionEngine == null) return Collections.emptyList();
        return suggestionEngine.suggestSubstitutes(parsed);
    }
}
