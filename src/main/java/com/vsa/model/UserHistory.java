package com.vsa.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHistory {
    @Id
    private String id;

    private String userId;
    private String productId;
    private String name;
    private int count = 1;
    private Instant lastSeen = Instant.now();
}
