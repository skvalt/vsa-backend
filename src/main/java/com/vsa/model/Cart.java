package com.vsa.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    private String id;

    private String userId;
    private List<CartItem> items = new ArrayList<>();

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public double getTotal() {
        return items.stream()
                .mapToDouble(i -> (i.getPrice() == null ? 0 : i.getPrice()) * i.getQuantity())
                .sum();
    }
}
