package com.vsa.nlp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityExtractor {

    private static final Pattern QTY_PATTERN =
            Pattern.compile("(\\d+)\\s*(bottles|pieces|pcs|packs|kg|g|liters|l)?",
                    Pattern.CASE_INSENSITIVE);

    private static final Map<String,Integer> WORD_NUM =
            Map.of("one",1,"two",2,"three",3,"four",4,"five",5,"six",6,"seven",7,"eight",8,"nine",9,"ten",10);

    private static final Pattern PRODUCT_PATTERN =
            Pattern.compile("(add|buy|need|get|order|bring)\\s+([a-zA-Z0-9\\s]+)",
                    Pattern.CASE_INSENSITIVE);

    public Map<String, String> extract(String text) {
        Map<String, String> map = new HashMap<>();
        String lower = text.toLowerCase();

        // digit quantities
        Matcher qtyMatcher = QTY_PATTERN.matcher(text);
        if (qtyMatcher.find()) {
            map.put("quantity", qtyMatcher.group(1));
        }

        // word quantities
        for (var e : WORD_NUM.entrySet()) {
            if (lower.contains(e.getKey())) {
                map.put("quantity", String.valueOf(e.getValue()));
            }
        }

        Matcher productMatcher = PRODUCT_PATTERN.matcher(text);
        if (productMatcher.find()) {
            map.put("product", productMatcher.group(2).trim());
        } else {
            String[] tokens = text.split("\\s+");
            if (tokens.length > 0) {
                String guess = tokens[tokens.length - 1]
                        .replaceAll("[^a-zA-Z0-9 ]", "");
                map.put("product", guess.trim());
            }
        }

        return map;
    }
}
