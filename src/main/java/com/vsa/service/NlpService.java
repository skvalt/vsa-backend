package com.vsa.service;

import com.vsa.model.Product;
import com.vsa.model.response.ParsedIntentResponse;
import com.vsa.model.response.ParsedIntentResponse.ProductMatch;
import com.vsa.repository.ProductRepository;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
public class NlpService {

    private final ProductRepository productRepository;

    private static final Set<String> NUMBER_WORDS = Set.of(
            "one","two","three","four","five",
            "six","seven","eight","nine","ten",
            "1","2","3","4","5","6","7","8","9","10"
    );

    private static final Map<String, Integer> WORD_NUMBERS = Map.ofEntries(
            Map.entry("one",1), Map.entry("two",2), Map.entry("three",3),
            Map.entry("four",4), Map.entry("five",5), Map.entry("six",6),
            Map.entry("seven",7), Map.entry("eight",8), Map.entry("nine",9),
            Map.entry("ten",10)
    );

    // Minimal English-only keyword intent map 
    private final Map<String, String> keywordIntentMap = createKeywordIntentMap();

    private Map<String, String> createKeywordIntentMap() {
    Map<String, String> m = new HashMap<>();

    m.put("add", "add_item");
    m.put("put", "add_item");
    m.put("insert", "add_item");

    m.put("remove", "remove_item");
    m.put("delete", "remove_item");
    m.put("discard", "remove_item");

    m.put("update", "update_quantity");
    m.put("change", "update_quantity");
    m.put("set", "update_quantity");

    m.put("find", "search_item");
    m.put("search", "search_item");
    m.put("show", "search_item");

    m.put("suggest", "get_suggestions");
    m.put("filter", "filter_price");

    return Collections.unmodifiableMap(m);
}


    public NlpService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ParsedIntentResponse parse(String rawText) {
        String original = normalize(rawText);

        ParsedIntentResponse out = new ParsedIntentResponse();
        out.setRawText(original);

        // Intent
        String intent = detectIntentByKeywords(original);
        double intentScore = intent != null ? 1.0 : 0.0;
        if (intent == null) intent = "unknown";

        out.setIntent(intent);
        out.setIntentScore(intentScore);

        // Entities
        Map<String, String> entities = new HashMap<>();

        String qty = parseQuantity(original);
        if (qty != null) entities.put("quantity", qty);

        String price = parsePrice(original);
        if (price != null) entities.put("price_range", price);

        // Product detection (fixed)
        String productGuess = guessProduct(original);
        if (productGuess != null) entities.put("product", productGuess);

        out.setEntities(entities);

        // Matches
        List<ProductMatch> matches = resolveMatches(entities, original);
        out.setMatches(matches);

        return out;
    }

    private String detectIntentByKeywords(String text) {
        if (text == null || text.isBlank()) return null;
        String lower = text.toLowerCase();
        for (Map.Entry<String, String> e : keywordIntentMap.entrySet()) {
            if (lower.contains(e.getKey())) return e.getValue();
        }
        return null;
    }

    private String guessProduct(String text) {
        if (text == null || text.isBlank()) return null;

        List<String> tokens = Arrays.stream(text.split("\\s+"))
                .map(String::trim)
                .filter(t -> t.length() > 1)
                .filter(t -> !isGarbageToken(t))
                .filter(t -> !NUMBER_WORDS.contains(t.toLowerCase()))   // NEW: ignore number words
                .collect(Collectors.toList());

        if (tokens.isEmpty()) return null;

        List<Product> all = productRepository.findAll();
        LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();

        double bestScore = 0.0;
        String bestToken = null;

        for (String token : tokens) {
            String lowerToken = token.toLowerCase();

            for (Product p : all) {

                if (p.getName() != null && p.getName().toLowerCase().contains(lowerToken)) {
                    return token;
                }

                if (p.getTags() != null) {
                    for (String tag : p.getTags()) {
                        if (tag != null && tag.toLowerCase().contains(lowerToken)) {
                            return token;
                        }
                    }
                }

                if (p.getName() != null) {
                    double s = similarity(lowerToken, p.getName().toLowerCase(), ld);
                    if (s > bestScore) {
                        bestScore = s;
                        bestToken = token;
                    }
                }
            }
        }

        return bestScore >= 0.45 ? bestToken : null;
    }

    private boolean isGarbageToken(String t) {
        Set<String> junk = Set.of(
                "add", "put", "insert",
                "remove", "delete", "discard",
                "find", "search", "show",
                "suggest", "filter",
                "in", "on", "to", "my", "the", "a",
                "for", "of", "and", "list"
        );

        return junk.contains(t.toLowerCase());
    }

    private double similarity(String a, String b, LevenshteinDistance ld) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;
        return 1.0 - (double) ld.apply(a, b) / Math.max(a.length(), b.length());
    }

    private List<ProductMatch> resolveMatches(Map<String, String> entities, String text) {
        if (entities.containsKey("product")) {
            List<ProductMatch> found = resolveProduct(entities.get("product"));
            if (!found.isEmpty()) return found;
        }

        List<ProductMatch> fb = fallbackMatch(text);
        if (!fb.isEmpty()) return fb;

        if (entities.containsKey("price_range")) {
            return searchByPriceRange(entities.get("price_range"));
        }

        return Collections.emptyList();
    }

    private List<ProductMatch> resolveProduct(String token) {
        String q = token.toLowerCase();
        List<ProductMatch> out = new ArrayList<>();

        List<Product> nameHits = productRepository.findByNameRegex("(?i).*" + Pattern.quote(q) + ".*");

        for (Product p : nameHits) {
            out.add(new ProductMatch(p.getId(), p.getName(), p.getBrand(), p.getCategory(), p.getPrice(), 0.95));
        }

        if (!out.isEmpty()) return out;

        return approxMatches(q);
    }

    private List<ProductMatch> approxMatches(String token) {
        LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();
        return productRepository.findAll().stream()
                .map(p -> new ProductMatch(
                        p.getId(), p.getName(), p.getBrand(), p.getCategory(),
                        p.getPrice(), similarity(token, p.getName().toLowerCase(), ld)
                ))
                .filter(m -> m.getScore() >= 0.40)
                .sorted(Comparator.comparing(ProductMatch::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<ProductMatch> fallbackMatch(String text) {
        if (text == null) return Collections.emptyList();
        String lower = text.toLowerCase();
        return productRepository.findAll().stream()
                .filter(p -> p.getName() != null && lower.contains(p.getName().toLowerCase()))
                .map(p -> new ProductMatch(p.getId(), p.getName(), p.getBrand(), p.getCategory(), p.getPrice(), 1.0))
                .collect(Collectors.toList());
    }

    private List<ProductMatch> searchByPriceRange(String r) {
        String[] parts = r.split("-");
        double low = Double.parseDouble(parts[0]);
        double high = Double.parseDouble(parts[1]);

        return productRepository.findByPriceBetween(low, high).stream()
                .map(p -> new ProductMatch(p.getId(), p.getName(), p.getBrand(), p.getCategory(), p.getPrice(), 0.8))
                .collect(Collectors.toList());
    }

    private String parseQuantity(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        Matcher m = Pattern.compile("(\\d+)\\s*(kg|g|l|ml|liters|liter|ltr|litre)?").matcher(lower);
        if (m.find()) return m.group(1);

        // word numbers
        for (var e : WORD_NUMBERS.entrySet()) {
            if (lower.contains(e.getKey())) {
                return String.valueOf(e.getValue());
            }
        }

        return null;
    }

    private String parsePrice(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        Matcher m1 = Pattern.compile("between (\\d+) and (\\d+)").matcher(lower);
        if (m1.find()) return m1.group(1) + "-" + m1.group(2);

        Matcher m2 = Pattern.compile("(under|below|less than) (\\d+)").matcher(lower);
        if (m2.find()) return "0-" + m2.group(2);

        return null;
    }

    private String normalize(String t) {
        if (t == null) return "";
        String s = t.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replaceAll("[\\r\\n]+", " ").trim();
    }
}
