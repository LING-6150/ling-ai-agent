package com.ling.lingaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 检索增强顾问的工厂
 * * Custom RAG Advisor Factory
 *  *
 *  * Purpose: Creates a RAG Advisor with advanced features integrated
 *  *
 *  * Features:
 *  * 1. Custom Document Retriever (DocumentRetriever)
 *  * 2. Empty Context Friendly Handling (ContextualQueryAugmenter)
 *  *
 *  * Use Cases:
 *  * - Need to control retrieval parameters (similarity threshold, number of results)
 *  * - Need to handle "no documents found" scenarios gracefully
 */

public class LoveAppRagCustomAdvisorFactory {

    /**
     * 创建自定义的 RAG 检索增强顾问
     *
     * @param vectorStore 向量存储
     *
     * @return 自定义的 RAG 检索增强顾问
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // 过滤特定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        // Configure document retriever
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) // 过滤条件
                .similarityThreshold(0.3) //similarity threshold (0-1, higher= stricter)
                .topK(3) // return top 3 most relevant documents
                .build();
        //combine into complete advisor
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever) // retriever
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();

    }
}
