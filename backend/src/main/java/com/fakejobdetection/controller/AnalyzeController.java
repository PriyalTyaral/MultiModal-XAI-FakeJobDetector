package com.fakejobdetection.controller;

import com.fakejobdetection.dto.AnalyzeRequest;
import com.fakejobdetection.dto.AnalyzeResponse;
import com.fakejobdetection.entity.AnalysisResult;
import com.fakejobdetection.service.TextAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalyzeController {

    private final TextAnalysisService service;

    public AnalyzeController(TextAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {

        AnalysisResult result =
                // service.analyzeAndSave(request.getText());
                service.analyze(request.getText());


        return new AnalyzeResponse(
                result.getPrediction().label,
                result.getPrediction().confidence,
                result.getExplanation().reasons,
                result.getExplanation().weights
        );
    }

    @GetMapping("/health")
    public String health() {
        return "Backend is running 🚀";
    }
}
