package com.vsa.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    private String id;

    private String userId;

    private String name;

    private int quantity = 1;

    private String unit;

    private String category;

    private Double price;

    private boolean checked = false;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
