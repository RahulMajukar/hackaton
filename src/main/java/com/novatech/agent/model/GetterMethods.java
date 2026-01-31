package com.novatech.agent.model;

import com.novatech.agent.service.SearchService;
import lombok.Getter;

public class GetterMethods {
    // Add getters for SearchResult and SourceInfo
    @Getter
    public static class SearchResult {
        private final String answer;
        private final java.util.List<SourceInfo> sources;
        
        public SearchResult(String answer, java.util.List<SourceInfo> sources) {
            this.answer = answer;
            this.sources = sources;
        }
    }
    
    @Getter
    public static class SourceInfo {
        private final String fileName;
        private final String folder;
        private final int chunkNumber;
        
        public SourceInfo(String fileName, String folder, int chunkNumber) {
            this.fileName = fileName;
            this.folder = folder;
            this.chunkNumber = chunkNumber;
        }
    }
}