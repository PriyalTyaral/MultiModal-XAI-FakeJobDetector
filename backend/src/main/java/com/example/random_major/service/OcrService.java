package com.example.random_major.service;

import java.io.File;

import org.springframework.stereotype.Service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OcrService {

   public String extractTextFromImage(File file) {

    try {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("eng");

        String extractedText = tesseract.doOCR(file);

        // ✅ CLEAN OCR TEXT HERE
        extractedText = extractedText
        .toLowerCase()
        .replaceAll("[^a-zA-Z0-9 ]", " ")
        .replaceAll("\\s+", " ")
        .trim();

        System.out.println("Cleaned OCR Text:");
        System.out.println(extractedText);

        return extractedText;

    } catch (TesseractException e) {
        e.printStackTrace();
        return null;
    }
}
}