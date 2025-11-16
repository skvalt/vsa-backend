package com.vsa.util;

import java.util.Map;
import java.util.Set;

//Simple category mapping from product name keywords to categories.

public final class ItemCategoryMapper {

    private static final Map<String, String> MAP = Map.ofEntries(
            Map.entry("milk", "Dairy"),
            Map.entry("bread", "Bakery"),
            Map.entry("apple", "Produce"),
            Map.entry("banana", "Produce"),
            Map.entry("toothpaste", "Personal Care"),
            Map.entry("shampoo", "Personal Care"),
            Map.entry("rice", "Grains")
    );

    public static String mapToCategory(String productName) {
        if (productName == null) return "Misc";
        String lower = productName.toLowerCase();
        for (var k : MAP.keySet()) {
            if (lower.contains(k)) return MAP.get(k);
        }
        return "Misc";
    }

    public static Set<String> knownKeywords() { return MAP.keySet(); }
}
