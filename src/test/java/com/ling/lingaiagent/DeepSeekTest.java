package com.ling.lingaiagent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class DeepSeekTest {

    @Autowired
    ApplicationContext context;

    @Test
    void printBeans() {
        String[] beans = context.getBeanNamesForType(ChatModel.class);
        for (String bean : beans) {
            System.out.println("ChatModel Bean: " + bean);
        }
    }
}