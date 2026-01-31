package com.novatech.agent.component;

import com.novatech.agent.entity.DocumentChunk;
import com.novatech.agent.repository.DocumentRepository;
import com.novatech.agent.service.OpenAIService;
import com.novatech.agent.service.PDFReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private PDFReaderService pdfReaderService;
    @Autowired
    private OpenAIService openAIService;
    @Autowired
    private DocumentRepository documentRepository;

    @Value("${app.knowledge-base-path}")
    private String knowledgeBasePath;

    @Value("${app.chunk-size}")
    private int chunkSize;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting data loading from: " + knowledgeBasePath);

        // Get all PDFs
        List<File> pdfFiles = pdfReaderService.getAllPDFs(knowledgeBasePath);
        System.out.println("Found " + pdfFiles.size() + " PDF files");

        int totalChunks = 0;

        // Process each PDF
        for (File pdfFile : pdfFiles) {
            System.out.println("Processing: " + pdfFile.getPath());

            try {
                // Extract text
                String text = pdfReaderService.extractTextFromPDF(pdfFile);

                // Chunk text
                List<String> chunks = pdfReaderService.chunkText(text, chunkSize);

                // Process each chunk
                for (int i = 0; i < chunks.size(); i++) {
                    String chunkContent = chunks.get(i);

                    // Create embedding
                    float[] embedding = openAIService.createEmbedding(chunkContent);

                    // Create document chunk entity
                    DocumentChunk chunk = new DocumentChunk();
                    chunk.setFilePath(pdfFile.getPath());
                    chunk.setFolderName(pdfFile.getParentFile().getName());
                    chunk.setFileName(pdfFile.getName());
                    chunk.setContent(chunkContent);
                    chunk.setChunkIndex(i);
                    chunk.setTotalChunks(chunks.size());
                    chunk.setEmbedding(embedding);
                    chunk.setCreatedAt(LocalDateTime.now());

                    // Save to database
                    documentRepository.save(chunk);
                    totalChunks++;

                    System.out.println("  Saved chunk " + (i + 1) + "/" + chunks.size());
                }

            } catch (Exception e) {
                System.err.println("Error processing " + pdfFile.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("✅ Data loading complete! Loaded " + totalChunks + " chunks");
    }
}