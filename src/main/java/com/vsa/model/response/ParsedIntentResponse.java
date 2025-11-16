package com.vsa.model.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedIntentResponse {

    private String intent;
    private double intentScore;
    private Map<String, String> entities;
    private List<ProductMatch> matches;
    private String rawText;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductMatch {
        // IMPORTANT: must match frontend 'id' field
        private String id;
        private String name;
        private String brand;
        private String category;
        private Double price;
        private double score;
    }
}
