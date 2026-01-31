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

    @Value("${app.chunk-size:500}")
    private int chunkSize;

    @Override
    public void run(String... args) {
        System.out.println("🚀 Starting knowledge base indexing from: " + knowledgeBasePath);

        File baseDir = new File(knowledgeBasePath);
        if (!baseDir.exists()) {
            System.err.println("❌ Knowledge base path does not exist: " + knowledgeBasePath);
            return;
        }

        List<File> pdfFiles = pdfReaderService.getAllPDFs(knowledgeBasePath);
        System.out.println("📁 Found " + pdfFiles.size() + " PDF files");

        int totalChunks = 0;
        int newFiles = 0;

        for (File pdfFile : pdfFiles) {
            if (documentRepository.existsByFilePath(pdfFile.getPath())) {
                System.out.println("⏭️ Skipping (already indexed): " + pdfFile.getName());
                continue;
            }

            System.out.println("📄 Processing: " + pdfFile.getName());
            newFiles++;

            try {
                String text = pdfReaderService.extractTextFromPDF(pdfFile);
                if (text == null || text.trim().isEmpty()) {
                    System.err.println("⚠️ Empty content in: " + pdfFile.getName());
                    continue;
                }

                List<String> chunks = pdfReaderService.chunkText(text, chunkSize);
                System.out.println("  → Split into " + chunks.size() + " chunks");

                for (int i = 0; i < chunks.size(); i++) {
                    String chunkContent = chunks.get(i).trim();
                    if (chunkContent.isEmpty()) continue;

                    float[] embedding = openAIService.createEmbedding(chunkContent);

                    DocumentChunk chunk = new DocumentChunk();
                    chunk.setFilePath(pdfFile.getPath());
                    chunk.setFolderName(pdfFile.getParentFile().getName());
                    chunk.setFileName(pdfFile.getName());
                    chunk.setContent(chunkContent);
                    chunk.setChunkIndex(i);
                    chunk.setTotalChunks(chunks.size());
                    chunk.setEmbeddingVector(embedding); // CRITICAL: Uses helper with proper formatting
                    chunk.setCreatedAt(LocalDateTime.now());

                    documentRepository.save(chunk); // Now works with @SQLInsert casting
                    totalChunks++;
                }

                System.out.println("✅ Completed: " + pdfFile.getName() + " (" + chunks.size() + " chunks)");

            } catch (Exception e) {
                System.err.println("❌ Error processing " + pdfFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\n✨ Indexing complete!");
        System.out.println("   New files processed: " + newFiles);
        System.out.println("   Total new chunks: " + totalChunks);
        System.out.println("   Total chunks in DB: " + documentRepository.count());
    }
}