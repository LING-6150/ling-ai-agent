package com.ling.lingaiagent;

import com.ling.lingaiagent.agent.LingManus;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)

public class LingManusTest {

    @Resource
    private LingManus lingManus;

    @Test
    void run() {
        String userPrompt = """
            帮我规划一次约会，
            我还没决定去哪个城市，也不确定预算，
            请帮我制定一个完整的约会计划。
            """;
        String answer = lingManus.run(userPrompt);
        System.out.println(answer);
    }
}