package com.vsa.repository;

import com.vsa.model.UserHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserHistoryRepository extends MongoRepository<UserHistory, String> {
    List<UserHistory> findByUserIdOrderByLastSeenDesc(String userId);
}
