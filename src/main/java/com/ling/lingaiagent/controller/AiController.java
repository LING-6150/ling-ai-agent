package com.ling.lingaiagent.controller;

import com.ling.lingaiagent.agent.LingManus;
import com.ling.lingaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ApplicationContext applicationContext;  // ← 用这个获取prototype bean

    // ========== LoveApp 接口 ==========

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    @GetMapping("/love_app/chat/mcp")
    public String doChatWithMcp(String message, String chatId) {
        return loveApp.doChatWithMcp(message, chatId);
    }

    // ========== LingManus 智能体接口 ==========

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        // 每次从容器获取新实例（因为是prototype scope）
        LingManus lingManus = applicationContext.getBean(LingManus.class);
        return lingManus.runStream(message);
    }
    // ========== RAG 接口 ==========

    @GetMapping("/love_app/chat/rag")
    public String doChatWithRag(String message, String chatId, String status) {
        return loveApp.doChatWithRag(message, chatId, status);
    }

    @GetMapping("/love_app/chat/rag/rerank")
    public String doChatWithRagAndRerank(String message, String chatId, String status) {
        return loveApp.doChatWithRagAndRerank(message, chatId, status);
    }
}
