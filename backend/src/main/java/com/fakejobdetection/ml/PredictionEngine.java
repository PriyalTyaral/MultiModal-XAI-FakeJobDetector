package com.fakejobdetection.ml;

import org.jpmml.evaluator.*;

import java.util.HashMap;
import java.util.Map;

public class PredictionEngine {

    public static double predictProbability(Map<String, Object> features) {

        Evaluator evaluator = PMMLModelLoader.getEvaluator();

        // Prepare input without FieldName
        Map<String, FieldValue> arguments = new HashMap<>();
        for (InputField inputField : evaluator.getInputFields()) {
            String name = inputField.getName();
            Object rawValue = features.get(name);
            arguments.put(name, inputField.prepare(rawValue));
        }

        // Evaluate
        Map<String, ?> results =
                EvaluatorUtil.decodeAll(evaluator.evaluate(arguments));

        // Target field (binary classifier)
        Object prediction = results.values().iterator().next();

        if (prediction instanceof Number number) {
            return number.doubleValue();
        }

        // fallback
        return 0.0;
    }
}
