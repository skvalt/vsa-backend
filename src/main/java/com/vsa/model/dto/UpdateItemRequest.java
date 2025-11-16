package com.vsa.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemRequest {
    private String userId;
    private String productName;
    private Integer quantity;
    private String category;

    private String unit;
    private Double price;
}
