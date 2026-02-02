package com.ling.lingaiagent.app;

import com.ling.lingaiagent.advisor.MyLoggerAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;


import java.util.List;


@Component
@Slf4j

public class LoveApp {

    private final ChatClient chatClient; //spring AI 聊天客户端

    //系统提示词： define AI's role and actions
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    // 初始化客户端

    /**
     * | 中文 | 英文 | 说明 |
     * * |-----|------|------|
     * * | 聊天客户端 | Chat Client | 与 AI 交互的客户端 |
     * * | 系统提示词 | System Prompt | 定义 AI 角色的指令 |
     * * | 对话记忆 | Chat Memory | 存储历史对话 |
     * * | 窗口式 | Window-based | 保留最近 N 条消息 |
     * * | 内存存储 | In-memory Storage | 存在内存中 |
     * * | 拦截器 | Advisor / Interceptor | 拦截并处理请求 |
     * * | 对话历史 | Conversation History | 之前的聊天记录 |
     * * | 令牌限制 | Token Limit | AI 模型的输入限制 |
     * * | 构建请求 | Build Request | 组装 API 请求 |
     * * | 响应对象 | Response Object | AI 返回的结果 |
     * 初始化客户端
     *
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        //String fileDir = System.getProperty("user.dir")+"/tmp.chat-memory";

        //1. create chat memory (window-based, keeps the last 20 message)
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())//use in-memory storage (data lost on restart)
                .maxMessages(20) // limit message count to avoid exceeding token limits
                .build();

        // Build ChatClient and configure default behaviors
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT) // set default system prompt
                .defaultAdvisors(
                        //chat memoery advisor : manage conversation history
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // customer longer advisor: logs request and responses
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        // initiate a chat request
        ChatResponse chatResponse = chatClient
                //start building the request
                .prompt()
                .user(message) // set user message
                // configure : name : who- conversation identifier, value: user ID to separate different conversations
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // execute the AI call
                .call()
                //retrieve the complete response object
                .chatResponse();

        // Extract the text content from response
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI 恋爱报告功能（实战结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                //Add report generation instruction to system prompt
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                // Maintain conversation context via ChatId
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                //Convert JSON to loveReport objext
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;  // ← 改这里
    // AI 恋爱知识问答功能
//    @Resource
//    private VectorStore loveAppVectorStore;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用RAG知识库问答
                // .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 使用 PGVector RAG
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))  // ← 改这里
                //应用RAG 检索增强服务（基于晕知识库）
                //.advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}

