package com.ling.lingaiagent.rag;

import com.ling.lingaiagent.app.LoveApp;
import com.ling.lingaiagent.rag.LoveAppDocumentLoader;
import com.ling.lingaiagent.rag.MyKeywordEnricher;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;  // ← 添加这个
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;  // ← 添加这个

@SpringBootTest
public class LoveAppAdvancedTest {

    @Resource
    private LoveApp loveApp;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;  // ← 新增

    @Resource
    private MyKeywordEnricher myKeywordEnricher;  // ← 新增

    @Resource
    private QueryRewriter queryRewriter;  // ← 需要注入这个

    @Test
    void testEnrichedRag() {
        // 之前的测试
        String answer = loveApp.doChatWithRag("如何提升魅力？", "test-001");
        System.out.println(answer);
    }

    @Test  // ← 新增这个测试方法
    void testKeywordEnrichment() {
        // 直接测试 KeywordEnricher
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        System.out.println("原始文档数量: " + documents.size());

        List<Document> enriched = myKeywordEnricher.enrichDocuments(documents);

        // 查看第一个文档
        Document first = enriched.get(0);
        System.out.println("\n文档内容: " + first.getText().substring(0, 100));
        System.out.println("元数据: " + first.getMetadata());
        System.out.println("关键词: " + first.getMetadata().get("keywords"));
    }

    @Test
    void testNormalQuestion() {
        System.out.println("======== 测试正常问题 ========");
        String answer = loveApp.doChatWithRag("如何提升魅力？", "test-001");
        System.out.println("回答: " + answer);
        // 应该能检索到知识库内容并回答
    }

    @Test
    void testOutOfScopeQuestion() {
        System.out.println("======== 测试知识库外问题 ========");
        String answer = loveApp.doChatWithRag("今天天气怎么样？", "test-002");
        System.out.println("回答: " + answer);
    }


    @Test
    void testColloquialQuestion() {
        System.out.println("======== 测试口语化问题 ========");

        String original = "咋脱单啊？";
        System.out.println("原始问题: " + original);

        // 手动测试查询重写
        String rewritten = queryRewriter.doQueryRewrite(original);
        System.out.println("重写后: " + rewritten);

        // 再测试 RAG
        String answer = loveApp.doChatWithRag(original, "test-003");
        System.out.println("回答: " + answer);
    }

}
