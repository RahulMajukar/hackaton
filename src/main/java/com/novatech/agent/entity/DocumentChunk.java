package com.novatech.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLUpdate;

@Entity
@Table(name = "document_chunks")
// CRITICAL: Override default INSERT/UPDATE to cast String → vector
@SQLInsert(sql = "INSERT INTO document_chunks (chunk_index, content, created_at, embedding, file_name, file_path, folder_name, total_chunks) " +
                 "VALUES (?, ?, ?, CAST(? AS vector(1536)), ?, ?, ?, ?)")
@SQLUpdate(sql = "UPDATE document_chunks SET chunk_index = ?, content = ?, created_at = ?, embedding = CAST(? AS vector(1536)), file_name = ?, file_path = ?, folder_name = ?, total_chunks = ? " +
                 "WHERE id = ?")
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

    // MUST be String type for pgvector compatibility with Hibernate
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private String embedding;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    // Helper: Convert float[] → pgvector string format [0.1,0.2,...]
    public void setEmbeddingVector(float[] embeddingArray) {
        if (embeddingArray == null || embeddingArray.length == 0) {
            // Create zero vector of correct dimension
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < 1536; i++) {
                sb.append("0.0");
                if (i < 1535) sb.append(",");
            }
            sb.append("]");
            this.embedding = sb.toString();
            return;
        }
        
        // Format: [0.1234567,0.2345678,...] - NO SPACES, 7 decimal precision
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embeddingArray.length; i++) {
            // Avoid scientific notation and trailing zeros issues
            String val = String.format("%.7f", embeddingArray[i])
                .replaceAll("0*$", "")  // Remove trailing zeros
                .replaceAll("\\.$", ".0"); // Keep at least one digit after decimal
            sb.append(val);
            if (i < embeddingArray.length - 1) sb.append(",");
        }
        sb.append("]");
        this.embedding = sb.toString();
    }
}