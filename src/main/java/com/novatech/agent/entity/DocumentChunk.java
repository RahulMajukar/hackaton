package com.novatech.agent.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "document_chunks")
@Data
public class DocumentChunk {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "folder_name")
    private String folderName;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "chunk_index")
    private Integer chunkIndex;
    
    @Column(name = "total_chunks")
    private Integer totalChunks;
    
    // PGvector column - stores 1536-dimensional embeddings
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
}