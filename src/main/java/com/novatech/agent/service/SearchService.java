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

    @Value("${app.max-results}")
    private int maxResults;

    public SearchResult search(String question) {
        System.out.println("Searching for: " + question);

        // 1. Create embedding for question
        float[] queryEmbedding = openAIService.createEmbedding(question);
        String embeddingStr = arrayToString(queryEmbedding);

        // 2. Search similar documents in database
        List<DocumentChunk> similarChunks = documentRepository.findSimilarDocuments(
                embeddingStr, maxResults);

        System.out.println("Found " + similarChunks.size() + " similar chunks");

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

    // private String arrayToString(float[] array) {
    // // Convert float array to PostgreSQL vector format: {0.1, 0.2, ...}
    // StringBuilder sb = new StringBuilder();
    // sb.append("{");
    // for (int i = 0; i < array.length; i++) {
    // sb.append(array[i]);
    // if (i < array.length - 1) {
    // sb.append(",");
    // }
    // }
    // sb.append("}");
    // return sb.toString();
    // }
    private String arrayToString(float[] array) {
        // pgvector expects [0.1,0.2,...] format, not {0.1,0.2,...}
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // Helper classes with getters
    public static class SearchResult {
        private final String answer;
        private final List<SourceInfo> sources;

        public SearchResult(String answer, List<SourceInfo> sources) {
            this.answer = answer;
            this.sources = sources;
        }

        public String getAnswer() {
            return answer;
        }

        public List<SourceInfo> getSources() {
            return sources;
        }
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

        public String getFileName() {
            return fileName;
        }

        public String getFolder() {
            return folder;
        }

        public int getChunkNumber() {
            return chunkNumber;
        }
    }
}