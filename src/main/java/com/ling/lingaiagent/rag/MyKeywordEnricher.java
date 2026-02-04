package com.ling.lingaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 AI 的文档元信息增强器（为文档补充元信息）
 * keyword Enricher
 * Purpose: Automatically extracts Keywords from documents using AI and adds them to metabata
 *
 * Benefits:
 * 1.Improve retrieval accuracy (keyword matching)
 * 2. Supports filtering documents by keywords
 * 3. Facilitates document categorization and management
 *
 * Output Metadata Example:
 *  * {
 *  *   "excerpt_keywords": "personal grooming, social skills, confidence",
 *  *   "filename": "xxx.md"
 *  * }
 *  Note:
 *  - Extracted keywords maybe in English
 *  -Extract 5 keywords per document
 */
@Component
public class MyKeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        //Spring Ai built-in keyword extractor
        //Parameters: chatModel, number of keywords per document
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        return  keywordMetadataEnricher.apply(documents);
    }
}
