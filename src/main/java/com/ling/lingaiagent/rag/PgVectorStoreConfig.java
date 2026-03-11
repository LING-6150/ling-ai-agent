package com.ling.lingaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

// 改用这个 import
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;
import lombok.extern.slf4j.Slf4j;
@Slf4j
/**
 * PGVector Vector Database Configuration (Advanced Version)
 *
 * Integrated Features:
 * 1. Document Splitting (TokenTextSplitter) - Better retrieval precision
 * 2. Keyword Enrichment (KeywordEnricher) - AI-extracted keywords
 * 3. Status Metadata (for personalized filtering)
 * 4. Data Persistence (PostgreSQL)
 * 5. High-Performance Indexing (HNSW)
 *
 * Tech Stack:
 * - PostgreSQL 16 + PGVector Extension
 * - Docker Containerization
 * - HNSW Indexing Algorithm
 *
 * vs SimpleVectorStore:
 * - SimpleVectorStore: In-memory, lost on restart
 * - PgVectorStore: Database storage, enterprise-grade
 */

@Configuration
public class PgVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;
    /**
     * Creates PGVector VectorStore with full document preprocessing pipeline
     *
     * Pipeline:
     * 1. Load documents (with status metadata from filenames)
     * 2. Split long documents into smaller chunks (200 tokens each)
     * 3. Enrich with AI-extracted keywords
     * 4. Embed vectors using DashScope
     * 5. Store in PostgreSQL with HNSW indexing
     *
     * @param jdbcTemplate Spring JDBC Template (auto-injected by Spring)
     * @param openAiEmbeddingModel AI  Embedding Model (text-embedding-v3, 1536-dim)
     * @return PgVectorStore instance ready for production use
     */

    @Bean
    public VectorStore pgVectorVectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel openAiEmbeddingModel) {

        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, openAiEmbeddingModel)
                .dimensions(1536)  // Vector dimensions (matches text-embedding-v3)
                .distanceType(COSINE_DISTANCE)  // Cosine distance for similarity calculation
                .indexType(HNSW) // HNSW index (Hierarchical Navigable Small World)
                             // - High performance approximate nearest neighbor search
                .initializeSchema(true) // Auto-create table and vector extension
                .schemaName("public")  // // PostgreSQL schema name
                .vectorTableName("vector_store")    //Table name for storing vectors
                .maxDocumentBatchSize(10000) //Batch insert size for performance
                .build();

        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        log.info("✅ Loaded {} raw documents", documents.size());

        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        log.info("✅ Split into {} chunks", splitDocuments.size());

        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);
        log.info("✅ Enriched {} documents with keywords", enrichedDocuments.size());

        // ✅ 加这段，给每个 chunk 加 chunkIndex 和 docId
        for (int i = 0; i < enrichedDocuments.size(); i++) {
            Document doc = enrichedDocuments.get(i);
            String filename = (String) doc.getMetadata().getOrDefault("filename", "unknown");
            String docId = filename.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().substring(0, Math.min(8, filename.length()));
            doc.getMetadata().put("chunkIndex", i);
            doc.getMetadata().put("docId", docId);
        }


        batchAddDocuments(vectorStore, enrichedDocuments);

        return vectorStore;
    }
    /**
     * Batch-insert documents to respect DashScope API limit
     *
     * DashScope embedding API limit: 25 documents per request
     * Using batch size of 20 for safety margin
     *
     * @param vectorStore Target vector store
     * @param documents Documents to insert
     */
    private void batchAddDocuments(VectorStore vectorStore, List<Document> documents) {
        int batchSize= 20;
        int batchCount = (documents.size() + batchSize -1) / batchSize;

        log.info("📦 Batch insertion: {} documents in {} batches (size={})",
                documents.size(), batchCount, batchSize);

        for(int i=0; i< documents.size(); i+=batchSize) {
            int batchNum= (i/batchSize) +1;
            int end= Math.min(i+batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);

            try{
                vectorStore.add(batch);
                log.info("✅ Batch {}/{} completed ({} documents)",
                        batchNum, batchCount, batch.size());
            }catch (Exception e){
                log.error("❌ Batch {} failed", batchNum, e);
                throw new RuntimeException("Failed to store batch " + batchNum, e);
            }
        }
        log.info("🎉 All {} documents stored successfully", documents.size());
    }
}