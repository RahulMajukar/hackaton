package com.novatech.agent.repository;

import com.novatech.agent.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentChunk, Long> {

    // FIXED: Added 'embedding' to SELECT clause so Hibernate can map the entity
    @Query(value = """
        SELECT
          id,
          file_path,
          folder_name,
          file_name,
          content,
          chunk_index,
          total_chunks,
          embedding,  -- CRITICAL: Must include for Hibernate mapping
          created_at
        FROM document_chunks
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT 5
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarDocuments(@Param("embedding") String embedding);

    // FIXED: Added 'embedding' to SELECT clause
    @Query(value = """
        SELECT 
          id,
          file_path,
          folder_name,
          file_name,
          content,
          chunk_index,
          total_chunks,
          embedding,  -- CRITICAL: Must include for Hibernate mapping
          created_at
        FROM document_chunks
        WHERE folder_name = :folder
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarInFolder(
        @Param("embedding") String embedding,
        @Param("folder") String folder,
        @Param("limit") int limit
    );

    boolean existsByFilePath(String filePath);
}