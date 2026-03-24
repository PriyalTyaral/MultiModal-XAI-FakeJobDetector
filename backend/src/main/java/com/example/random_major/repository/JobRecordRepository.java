package com.example.random_major.repository;

import com.example.random_major.entity.JobRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface JobRecordRepository extends MongoRepository<JobRecord, String> {
    List<JobRecord> findByUserIdOrderByCreatedAtDesc(String userId);
}