package com.fakejobdetection.ml;

import org.jpmml.evaluator.*;
import org.jpmml.model.PMMLUtil;
import org.dmg.pmml.PMML;

import java.io.InputStream;

public class PMMLModelLoader {

    private static final Evaluator EVALUATOR = loadModel();

    @SuppressWarnings("UseSpecificCatch")
    private static Evaluator loadModel() {
        try {
            InputStream is =
                PMMLModelLoader.class.getResourceAsStream(
                    "/models/fake_job_model.pmml");

            PMML pmml = PMMLUtil.unmarshal(is);
            Evaluator evaluator = new ModelEvaluatorBuilder(pmml).build();
            evaluator.verify();

            return evaluator;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PMML model", e);
        }
    }

    public static Evaluator getEvaluator() {
        return EVALUATOR;
    }
}
