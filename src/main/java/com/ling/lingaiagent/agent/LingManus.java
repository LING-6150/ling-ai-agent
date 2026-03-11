package com.ling.lingaiagent.agent;

import com.ling.lingaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * LingManus - 基于 Manus 架构的 AI 超级智能体
 * 继承 ToolCallAgent，集成所有工具，是最终可用的智能体实例
 */
@Component
@Scope("prototype")  // ← 加这一行，每次注入都是新实例
public class LingManus extends ToolCallAgent {

    public LingManus(ToolCallback[] allTools, ChatModel openAiChatModel) {
        super(allTools);
        this.setName("LingManus");

        String SYSTEM_PROMPT = """
                You are LingManus, an all-capable AI assistant, aimed at solving
                any task presented by the user.
                You have various tools at your disposal that you can call upon
                to efficiently complete complex requests.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool
                or combination of tools.
                For complex tasks, you can break down the problem and use different
                tools step by step to solve it.
                After using each tool, clearly explain the execution results and
                suggest the next steps.
                If you want to stop the interaction at any point, use the terminate
                tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);

        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}