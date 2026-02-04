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
        String answer = loveApp.doChatWithRag("如何提升魅力？", "test-001", "单身");
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
        String answer = loveApp.doChatWithRag("如何提升魅力？", "test-001", "单身");
        System.out.println("回答: " + answer);
        // 应该能检索到知识库内容并回答
    }

    @Test
    void testOutOfScopeQuestion() {
        System.out.println("======== 测试知识库外问题 ========");
        String answer = loveApp.doChatWithRag("今天天气怎么样？", "test-002", null);
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
        String answer = loveApp.doChatWithRag(original, "test-003", "单身");
        System.out.println("回答: " + answer);
    }

    @Test
    void testDocumentMetadata() {
        System.out.println("======== 验证 status 元数据 ========");

        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        System.out.println("✅ 加载文档数量: " + documents.size());

        // 查看前3个文档
        for (int i = 0; i < Math.min(3, documents.size()); i++) {
            Document doc = documents.get(i);
            System.out.println("\n文档 " + (i+1) + ":");
            System.out.println("  文件名: " + doc.getMetadata().get("filename"));
            System.out.println("  状态: " + doc.getMetadata().get("status"));  // ⭐ 看这里
            System.out.println("  标题: " + doc.getMetadata().get("title"));
        }
    }

    @Test
    void testFilterBySingleStatus() {
        System.out.println("======== 测试单身状态过滤 ========");

        // 调用带 status 参数的方法
        String answer = loveApp.doChatWithRag("如何脱单？", "test-001", "单身");
        System.out.println("回答: " + answer);
    }
    @Test
    void testFilterByMarriedStatus() {
        System.out.println("======== 测试已婚状态过滤 ========");

        String answer = loveApp.doChatWithRag("如何经营婚姻关系？", "test-002", "已婚");
        System.out.println("回答: " + answer);
    }

    @Test
    void testFilterImpact() {
        System.out.println("======== 对比不同状态的过滤效果 ========");

        String question = "如何提升个人魅力？";

        String answer1 = loveApp.doChatWithRag(question, "test-001", "单身");
        System.out.println("\n【单身】回答: " + answer1.substring(0, 200) + "...");

        String answer2 = loveApp.doChatWithRag(question, "test-002", "恋爱");
        System.out.println("\n【恋爱】回答: " + answer2.substring(0, 200) + "...");

        String answer3 = loveApp.doChatWithRag(question, "test-003", "已婚");
        System.out.println("\n【已婚】回答: " + answer3.substring(0, 200) + "...");
    }

}
