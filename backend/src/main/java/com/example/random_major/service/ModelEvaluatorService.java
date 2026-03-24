package com.example.random_major.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorBuilder;
import org.jpmml.evaluator.TargetField;
import org.jpmml.model.PMMLUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ModelEvaluatorService {

    private Evaluator evaluator;

    @PostConstruct
    public void init() {
        try {
            // Load PMML file from resources folder
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("model.pmml");

            if (is == null) {
                throw new RuntimeException("PMML file not found in resources folder");
            }

            PMML pmml = PMMLUtil.unmarshal(is);
            evaluator = new ModelEvaluatorBuilder(pmml).build();
            evaluator.verify();

            System.out.println("✅ PMML Model Loaded Successfully");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Failed to load PMML model", e);
        }
    }

    public Map<String, Object> predict(String text) {

    Map<String, Object> output = new HashMap<>();

    try {

        InputField inputField = evaluator.getInputFields().get(0);
        Object preparedValue = inputField.prepare(text);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put(inputField.getName(), preparedValue);

        Map<String, ?> results = evaluator.evaluate(arguments);

        TargetField targetField = evaluator.getTargetFields().get(0);
        Object prediction = results.get(targetField.getName());

        String labelValue = prediction.toString();

        double fakeProbability = 0.0;

        // 🔥 Get probability from probability distribution
        if (prediction instanceof org.jpmml.evaluator.ProbabilityDistribution) {
            org.jpmml.evaluator.ProbabilityDistribution dist =
                (org.jpmml.evaluator.ProbabilityDistribution) prediction;

            fakeProbability = dist.getProbability("1");
        }

        // Convert label
        if (labelValue.equals("1")) {
            output.put("label", "FAKE");
        } else {
            output.put("label", "REAL");
        }

        output.put("probability_fake", fakeProbability);

    } catch (Exception e) {
        e.printStackTrace();
        output.put("error", "Prediction failed");
    }

    return output;
}

}