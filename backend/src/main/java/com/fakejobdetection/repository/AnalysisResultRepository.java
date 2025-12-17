package com.fakejobdetection.repository;

import com.fakejobdetection.entity.AnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnalysisResultRepository
        extends MongoRepository<AnalysisResult, String> {
}
