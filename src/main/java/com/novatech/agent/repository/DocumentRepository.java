package com.novatech.agent.repository;

import com.novatech.agent.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentChunk, Long> {
    
    // pgvector similarity search using cosine distance
    @Query(value = """
        SELECT * FROM document_chunks 
        ORDER BY embedding <=> CAST(:embedding AS vector) 
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarDocuments(
        @Param("embedding") String embedding,
        @Param("limit") int limit
    );
    
    // Search with folder filter
    @Query(value = """
        SELECT * FROM document_chunks 
        WHERE folder_name = :folder
        ORDER BY embedding <=> CAST(:embedding AS vector) 
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarInFolder(
        @Param("embedding") String embedding,
        @Param("folder") String folder,
        @Param("limit") int limit
    );
}