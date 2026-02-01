package com.ling.lingaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * 自定义 Re2 Advisor
 * 可提高大型语言模型的推理能力
 * | 重读 | Re-read |
 * | 增强理解 | Enhance comprehension |
 * | 修改提示词 | Modify prompt |
 * | 重复两次 | Repeat twice |
 * | 提高准确性 | Improve accuracy |
 * | 委托 | Delegate |
 */
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * 执行请求前，改写 Prompt
     *
     * @param chatClientRequest
     * @return
     */
    private ChatClientRequest before(ChatClientRequest chatClientRequest) {
        //Extract user's original message
        String userText = chatClientRequest.prompt().getUserMessage().getText();
        //Store original query in context for reference
        chatClientRequest.context().put("re2_input_query", userText);
        // rewrite prompt : repeat the question twice
        String newUserText = """
                %s
                Read the question again: %s
                """.formatted(userText, userText);
        // Create new prompt with modified message
        Prompt newPrompt = chatClientRequest.prompt().augmentUserMessage(newUserText);
        return new ChatClientRequest(newPrompt, chatClientRequest.context());
    }

    //handle non-streaming calls: apple before() and delegate
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        return chain.nextCall(this.before(chatClientRequest));
    }

    //handle streaming calls: apply before() then delegate
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        return chain.nextStream(this.before(chatClientRequest));
    }

    //execute first (highest priority)
    @Override
    public int getOrder() {
        return 0;
    }

    //return advisor name for identification
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
