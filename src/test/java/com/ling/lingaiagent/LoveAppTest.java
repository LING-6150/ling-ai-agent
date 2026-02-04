package com.ling.lingaiagent;

import com.ling.lingaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
    public class LoveAppTest {

        @Resource
        private LoveApp loveApp;

        @Test
        void testPgVectorRag() {
            System.out.println("======== 测试 PGVector RAG ========");

            String question = "如何提升自身魅力？";
            System.out.println("问题: " + question);

            String answer = loveApp.doChatWithRag(question, "test-001","单身");
            System.out.println("\n回答: " + answer);

            System.out.println("\n======== 测试完成 ========");
        }
    }
