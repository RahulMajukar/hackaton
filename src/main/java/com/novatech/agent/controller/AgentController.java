package com.novatech.agent.controller;

import com.novatech.agent.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AgentController {
    
    @Autowired
    private SearchService searchService;
    
    @PostMapping("/ask")
    public Map<String, Object> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        
        if (question == null || question.trim().isEmpty()) {
            return Map.of(
                "error", "Question is required",
                "success", false
            );
        }
        
        try {
            SearchService.SearchResult result = searchService.search(question);
            
            // Convert sources to list of maps for JSON
            Map<String, Object> response = new HashMap<>();
            response.put("question", question);
            response.put("answer", result.getAnswer());
            response.put("sources", result.getSources().stream()
                .map(source -> Map.of(
                    "fileName", source.getFileName(),
                    "folder", source.getFolder(),
                    "chunkNumber", source.getChunkNumber()
                ))
                .toList());
            response.put("success", true);
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                "error", "Failed to process question: " + e.getMessage(),
                "success", false
            );
        }
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "OK", 
            "service", "NovaTech Knowledge Agent",
            "time", java.time.LocalDateTime.now().toString()
        );
    }
}