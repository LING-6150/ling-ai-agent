package com.ling.lingaiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义基于 Token 的切词器
 * Purpose: Splits long documents into smaller chunks for better retrieval
 * Why split documents:
 * 1. Long documents contain multiple topics → imprecise retrieval
 * 2. Documents too long → exceed LLM context window
 * 3. Smaller chunks → more accurate similarity matching
 * Strategy:
 * - Split by token count
 * - Maintain semantic integrity
 * - Avoid breaking sentences
 */
@Component
class MyTokenTextSplitter {
    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(200, 100, 10, 5000, true);
        return splitter.apply(documents);
    }
}
