package com.fakejobdetection.util;

import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class OpenNLPProcessor {

    private static Tokenizer tokenizer;
    private static SentenceDetectorME sentenceDetector;

    static {
        try {
            InputStream tokenModel =
                    OpenNLPProcessor.class.getResourceAsStream("/models/en-token.bin");
            InputStream sentModel =
                    OpenNLPProcessor.class.getResourceAsStream("/models/en-sent.bin");

            tokenizer = new TokenizerME(new TokenizerModel(tokenModel));
            sentenceDetector = new SentenceDetectorME(new SentenceModel(sentModel));

        } catch (Exception e) {
            throw new RuntimeException("Failed to load OpenNLP models", e);
        }
    }

    public static List<String> tokenize(String text) {
        return Arrays.asList(tokenizer.tokenize(text));
    }

    public static int sentenceCount(String text) {
        return sentenceDetector.sentDetect(text).length;
    }
}
