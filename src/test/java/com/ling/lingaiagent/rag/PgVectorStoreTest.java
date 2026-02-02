package com.ling.lingaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PgVectorStoreTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void testLoadDocuments() {
        // 1. 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        System.out.println("✅ 加载文档数量: " + documents.size());

        // 2. 查看文档内容
        if (!documents.isEmpty()) {
            System.out.println("第一个文档内容预览: " + documents.get(0).getText().substring(0, Math.min(100, documents.get(0).getText().length())));
        }

        // 3. 添加到向量库
        if (!documents.isEmpty()) {
            pgVectorVectorStore.add(documents);
            System.out.println("✅ 文档已添加到 PGVector");
        } else {
            System.err.println("❌ 没有加载到任何文档！");
        }
    }
}