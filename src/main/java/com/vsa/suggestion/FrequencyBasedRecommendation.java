package com.vsa.suggestion;

import com.vsa.model.Item;
import com.vsa.model.Product;
import com.vsa.repository.ItemRepository;
import com.vsa.repository.ProductRepository;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FrequencyBasedRecommendation implements RecommendationStrategy {

    private final ProductRepository repo;
    private final ItemRepository itemRepo;

    public FrequencyBasedRecommendation(ProductRepository repo, ItemRepository itemRepo) {
        this.repo = repo;
        this.itemRepo = itemRepo;
    }

    @Override
    public List<Product> recommend(String category) {
        // Count item name frequency in shopping history
        Map<String, Long> counts = itemRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        i -> i.getName().toLowerCase().trim(),
                        Collectors.counting()
                ));

        if (counts.isEmpty()) return Collections.emptyList();

        // top 10 frequent names
        List<String> topNames = counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // fetch matching products
        List<Product> results = new ArrayList<>();
        for (String nm : topNames) {
            results.addAll(
                    repo.findByNameRegex("(?i).*" + Pattern.quote(nm) + ".*")
            );
        }

        LinkedHashMap<String, Product> dedup = new LinkedHashMap<>();
        for (Product p : results) dedup.putIfAbsent(p.getId(), p);

        return new ArrayList<>(dedup.values());
    }
}
