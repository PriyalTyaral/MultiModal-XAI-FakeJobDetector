package com.example.random_major.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

@Service
public class TextExtractService {

    public String extractText(File file) throws IOException {

        String name = file.getName().toLowerCase();

        if (name.endsWith(".pdf")) {
            return extractFromPDF(file);
        }
        else if (name.endsWith(".docx")) {
            return extractFromDOCX(file);
        }
        else if (name.endsWith(".txt")) {
            return extractFromTXT(file);
        }

        return null;
    }

    private String extractFromPDF(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return cleanText(text);
    }

    private String extractFromDOCX(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        XWPFDocument doc = new XWPFDocument(fis);
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        extractor.close();
        return cleanText(text);
    }

    private String extractFromTXT(File file) throws IOException {
        return cleanText(
            new String(java.nio.file.Files.readAllBytes(file.toPath()))
        );
    }

    private String cleanText(String text) {
        return text
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}