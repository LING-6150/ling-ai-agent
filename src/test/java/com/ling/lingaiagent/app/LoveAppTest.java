package com.ling.lingaiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
@Slf4j  // 添加日志注解
class LoveAppTest {

        @Resource
        private LoveApp loveApp;

        @Test
        void testChat() {
            String chatId = UUID.randomUUID().toString();
            // 第一轮
            String message = "你好，我是Ling";
            String answer = loveApp.doChat(message, chatId);
            Assertions.assertNotNull(answer);
            // 第二轮
            message = "我想让另一半(JIJI）更爱我";
            answer = loveApp.doChat(message, chatId);
            Assertions.assertNotNull(answer);
            // 第三轮
            message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
            answer = loveApp.doChat(message, chatId);
            Assertions.assertNotNull(answer);
        }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是Ling，我想让另一半（JiJi）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }


    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

}

