package com.novatech.agent.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PDFReaderService {
    
    public String extractTextFromPDF(File pdfFile) throws IOException {
        // Use Loader.loadPDF() instead of PDDocument.load()
        PDDocument document = Loader.loadPDF(pdfFile);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text;
    }
    
    public List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        
        // Simple chunking by paragraph
        String[] paragraphs = text.split("\n\n");
        
        StringBuilder currentChunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(paragraph).append("\n\n");
        }
        
        // Add last chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        // If no paragraphs found, split by sentences
        if (chunks.isEmpty() && text.length() > chunkSize) {
            int start = 0;
            while (start < text.length()) {
                int end = Math.min(start + chunkSize, text.length());
                chunks.add(text.substring(start, end).trim());
                start = end;
            }
        } else if (chunks.isEmpty()) {
            chunks.add(text.trim());
        }
        
        return chunks;
    }
    
    public List<File> getAllPDFs(String folderPath) {
        List<File> pdfFiles = new ArrayList<>();
        File folder = new File(folderPath);
        
        if (!folder.exists()) {
            System.err.println("Folder not found: " + folderPath);
            return pdfFiles;
        }
        
        scanForPDFs(folder, pdfFiles);
        return pdfFiles;
    }
    
    private void scanForPDFs(File folder, List<File> pdfFiles) {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanForPDFs(file, pdfFiles);
            } else if (file.getName().toLowerCase().endsWith(".pdf")) {
                pdfFiles.add(file);
            }
        }
    }
}