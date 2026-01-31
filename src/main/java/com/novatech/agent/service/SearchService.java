package com.novatech.agent.service;

import com.novatech.agent.entity.DocumentChunk;
import com.novatech.agent.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private OpenAIService openAIService;

    @Value("${app.max-results:5}")
    private int maxResults;

    public SearchResult search(String question) {
        // System.out.println("🔍 Searching for: " + question);

        // 1. Create embedding for question
        float[] queryEmbedding = openAIService.createEmbedding(question);
        String embeddingStr = floatArrayToVectorString(queryEmbedding);

        // 2. Search similar documents in database
        List<DocumentChunk> similarChunks = documentRepository.findSimilarDocuments(embeddingStr);

        // System.out.println("✅ Found " + similarChunks.size() + " similar chunks");

        // 3. Extract content from chunks
        List<String> contexts = similarChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());

        // 4. Generate answer using OpenAI
        String answer = openAIService.generateAnswer(question, contexts);

        // 5. Prepare source information
        List<SourceInfo> sources = similarChunks.stream()
                .map(chunk -> new SourceInfo(
                        chunk.getFileName(),
                        chunk.getFolderName(),
                        chunk.getChunkIndex() + 1))
                .collect(Collectors.toList());

        return new SearchResult(answer, sources);
    }

    // CRITICAL: pgvector requires EXACT format: [0.123,0.456,...] WITHOUT spaces
    private String floatArrayToVectorString(float[] array) {
        if (array == null || array.length == 0) {
            return "[" + "0.0,".repeat(1535) + "0.0]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < array.length; i++) {
            // Format to avoid scientific notation and extra spaces
            sb.append(String.format("%.7f", array[i]).replaceAll("0+?$", "").replaceAll("\\.$", ".0"));
            if (i < array.length - 1) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    // Helper classes
    public static class SearchResult {
        private final String answer;
        private final List<SourceInfo> sources;

        public SearchResult(String answer, List<SourceInfo> sources) {
            this.answer = answer;
            this.sources = sources;
        }

        public String getAnswer() { return answer; }
        public List<SourceInfo> getSources() { return sources; }
    }

    public static class SourceInfo {
        private final String fileName;
        private final String folder;
        private final int chunkNumber;

        public SourceInfo(String fileName, String folder, int chunkNumber) {
            this.fileName = fileName;
            this.folder = folder;
            this.chunkNumber = chunkNumber;
        }

        public String getFileName() { return fileName; }
        public String getFolder() { return folder; }
        public int getChunkNumber() { return chunkNumber; }
    }
}