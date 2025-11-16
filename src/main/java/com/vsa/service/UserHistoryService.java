package com.vsa.service;

import com.vsa.model.UserHistory;
import com.vsa.repository.UserHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserHistoryService {

    private final UserHistoryRepository historyRepository;

    public UserHistoryService(UserHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    //Record that a user interacted with a product (add/update/remove). This increments count or creates entry.

    public void record(String userId, String productId, String name) {
        if (userId == null || productId == null && (name == null || name.isBlank())) return;

        
        UserHistory h = UserHistory.builder()
                .userId(userId)
                .productId(productId)
                .name(name)
                .count(1)
                .lastSeen(Instant.now())
                .build();

        historyRepository.save(h);
    }

    public List<UserHistory> getRecent(String userId) {
        if (userId == null) return List.of();
        return historyRepository.findByUserIdOrderByLastSeenDesc(userId);
    }
}
