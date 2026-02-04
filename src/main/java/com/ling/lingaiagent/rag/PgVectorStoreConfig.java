package com.ling.lingaiagent.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

// 改用这个 import
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * PGVector Vector Database Configuration
 *
 * Tech Stack:
 * - PostgreSQL 16 + PGVector Extension
 * - Docker Containerization
 * - HNSW High-Performance Indexing
 *
 * Advantages over SimpleVectorStore:
 * 1. Data Persistence (survives application restarts)
 * 2. Distributed Support (multiple instance can share data)
 * 3. HIgh- performance retrieval (HNSW indexing algorithm)
 * 4. Production-Ready (enterprise- grade solution)

 * Database Setup:
 * - Container: postgres-pgvector (Docker)
 * - Database: ling_ai_agent
 * - Table: vector_store
 * - Port: 5432
 */


@Configuration
public class PgVectorStoreConfig {

    @Bean
    public VectorStore pgVectorVectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel dashscopeEmbeddingModel) {

        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)  // Vector dimensions (matches text-embedding-v3)
                .distanceType(COSINE_DISTANCE)  // Cosine distance for similarity calculation
                .indexType(HNSW) // HNSW index (Hierarchical Navigable Small World)
                             // - High performance approximate nearest neighbor search
                .initializeSchema(true) // Auto-create table and vector extension
                .schemaName("public")  // // PostgreSQL schema name
                .vectorTableName("vector_store")    //Table name for storing vectors
                .maxDocumentBatchSize(10000) //Batch insert size for performance
                .build();

        return vectorStore;
    }
}