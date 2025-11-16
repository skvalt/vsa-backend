package com.vsa.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    private String id;         

    private String userId;
    private String productId;
    private String name;
    private int quantity = 1;
    private String unit;
    private String category;
    private Double price;

    private Instant addedAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
