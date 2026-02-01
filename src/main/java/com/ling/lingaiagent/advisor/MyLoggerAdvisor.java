package com.ling.lingaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	//Return unique advisor name
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	// Execution order: lower == higher priority
	@Override
	public int getOrder() {
		return 0;
	} //0 means execute first

	// pre-procession: log request
	private ChatClientRequest before(ChatClientRequest request) {
		log.info("AI Request: {}", request.prompt());
		return request;
	}
    //post- processing: log response
	private void observeAfter(ChatClientResponse chatClientResponse) {
		log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
	}

	//handle non-streaming calls
	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
		chatClientRequest = before(chatClientRequest);  //log request
		ChatClientResponse chatClientResponse = chain.nextCall(chatClientRequest); // Call next
		observeAfter(chatClientResponse); // log response
		return chatClientResponse;
	}

	//handle streaming calls
	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
		chatClientRequest = before(chatClientRequest); // log request 前置处理
		//call next advisor and get streaming response  (调用下一个并获取流式响应)
		Flux<ChatClientResponse> chatClientResponseFlux = chain.nextStream(chatClientRequest);
		// aggregate chunks and log complete response （聚合所有的片段， 然后记录完整的相应）
		return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponseFlux, this::observeAfter);
	}
}
