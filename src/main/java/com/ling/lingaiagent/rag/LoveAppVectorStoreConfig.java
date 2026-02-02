package com.ling.lingaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 恋爱大师向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
/**
 * VectorStore 配置类
 *
 * 作用：
 * 1. 在 Spring Boot 启动时创建一个 VectorStore Bean
 * 2. 将本地 Markdown 文档加载并向量化
 * 3. 把文档向量存入 VectorStore，供 RAG 检索使用
 */
@Configuration // Configuration class: used to define Spring Beans
public class LoveAppVectorStoreConfig {

    @Resource
    // Document loader: reads Markdown files and converts them into Document objects
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    //Define a VectorStore Bean and inject the EmbeddingModel
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // Create an in-memory vector store using the embedding model
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        // 加载文档
        // Add documents to the vector store (documents are embedded automatically)
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        simpleVectorStore.add(documents);
        return simpleVectorStore; // Return the VectorStore for RAG retrieval
    }
}
