package com.ling.lingaiagent.app;

import com.ling.lingaiagent.advisor.MyLoggerAdvisor;
import com.ling.lingaiagent.rag.*;
import io.swagger.v3.core.filter.SpecFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@Slf4j

public class LoveApp {

    private final ChatClient chatClient; //spring AI 聊天客户端

    //系统提示词： define AI's role and actions
    private static final String SYSTEM_PROMPT = " You are an empathetic relationship coach with 10+ years of experience.\n" +
            "        Start by briefly introducing yourself and inviting the user to share their relationship concerns.\n" +
            "        \n" +
            "        Based on the user's situation, ask targeted questions:\n" +
            "        - Single: struggles with expanding social circle or pursuing someone they like\n" +
            "        - Dating: conflicts from communication issues or lifestyle differences\n" +
            "        - Married: challenges balancing family responsibilities and in-law relationships\n" +
            "        \n" +
            "        Guide the user to describe what happened, how their partner reacted, and how they felt.\n" +
            "        Keep responses concise and warm. Provide personalized, actionable advice.\n" +
            "        Always respond in English.";
    @Autowired
    private SpecFilter specFilter;

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
     * @param openAiChatModel
     */
    public LoveApp(ChatModel openAiChatModel) {
        //String fileDir = System.getProperty("user.dir")+"/tmp.chat-memory";

        //1. create chat memory (window-based, keeps the last 20 message)
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())//use in-memory storage (data lost on restart)
                .maxMessages(20) // limit message count to avoid exceeding token limits
                .build();

        // Build ChatClient and configure default behaviors
        chatClient = ChatClient.builder(openAiChatModel)
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

    /**  新加入的方法
     * doChatByStream
     */

    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
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
                .system(SYSTEM_PROMPT + " After each response, generate a love report in JSON format. \" +\n" +
                        "        \"The report title should be '{username}'s Love Report', \" +\n" +
                        "        \"and the content should be a list of personalized recommendations based on the conversation.")
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
    private  VectorStore loveAppVectorStore;


    @Resource
    private VectorStore pgVectorVectorStore;  // ← 改这里
    // AI 恋爱知识问答功能

    @Resource
    private QueryRewriter queryRewriter;

    public String doChatWithRag(String message, String chatId, String status) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用RAG知识库问答
                 //.advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 使用 PGVector RAG  这个是基础版
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))  // ← 改这里
                //应用RAG 检索增强服务（基于云知识库）
                //
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(pgVectorVectorStore, status))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private RerankService rerankService;

    public String doChatWithRagAndRerank(String message, String chatId, String status) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        // 1. 先检索 topK=6（比原来多）
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(rewrittenMessage)
                .topK(6)
                .similarityThreshold(0.3)
                .filterExpression(expression)
                .build();
        List<Document> retrievedDocs = pgVectorVectorStore.similaritySearch(searchRequest);
        log.info("Retrieved {} docs for status={}", retrievedDocs.size(), status); // ← 加这一行


        // 2. reranking，取 top 3
        List<String> docTexts = retrievedDocs.stream()
                .map(Document::getText)
                .collect(Collectors.toList());
        List<String> rerankedTexts = rerankService.rerank(rewrittenMessage, docTexts, 3);

        // 3. 构建 context
        String context = String.join("\n\n", rerankedTexts);

        // 4. 调用 AI
        ChatResponse chatResponse = chatClient
                .prompt()
                .user("Context:\n" + context + "\n\nQuestion: " + rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getText();
        // ✅ 拼接 Sources
        String sources = retrievedDocs.stream().map(doc -> {
            Map<String, Object> md = doc.getMetadata();
            String filename = (String) md.getOrDefault("filename", "unknown");
            Object chunkIndex = md.getOrDefault("chunkIndex", "?");
            String docId = (String) md.getOrDefault("docId", "unknown");
            return String.format("- %s (chunk %s, docId: %s)", filename, chunkIndex, docId);
        }).distinct().collect(Collectors.joining("\n"));

        log.info("content with rerank: {}", answer);
        return answer + "\n\n**Sources:**\n" + sources;
    }


    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 恋爱报告功能（支持调用工具）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }




  //用于pdf 解析的方法， 自己新增的

    /**
     * 用Apache PDFBox解析PDF文本内容，
     * 拼接到用户消息里作为上下文，让AI基于文档内容回答问题。不入库，适合一次性问答。
     * 新的依赖已经加入 pom.xml 中了
     * @param message
     * @param chatId
     * @param filePath
     * @return
     * 最重要的是这两个注释：
     *
     * 不用RAG，直接塞进prompt — 解释了这个方法和doChatWithRag的本质区别
     * try-with-resources — 解释了为什么用这个写法而不是普通try/finally
     */

    public String doChatWithDocument(String message, String chatId, String filePath) {
        // 1. 解析PDF内容
        String pdfContent = extractPdfContent(filePath);

        // 2. 把PDF内容拼接到用户消息里作为上下文
        // 注意： 这里不用RAG (不入库）， 适合一次性文档问答
        // 缺点： 如果pdf很长， 会超出模型的token 限制
        String messageWithContext = "The following is the content extracted from the user's uploaded document:\n\n"
                + pdfContent
                + "\n\nBased on the document above, please answer the user's question: " + message;

        // 3. 调用AI （和普通对话一样， 只是user 消息里多了文档内容
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(messageWithContext)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    private String extractPdfContent(String filePath) {
        // try-with-resouces 确保PDDocument用完之后自动关闭， 避免内存泄漏
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            // getText() 把pdf所有页面的文字提取成一个字符串
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to extract PDF content", e);
            // 返回错误信息而不是抛出异常， 让上层方法能够继续处理
            return "pdf extraction failed: " + e.getMessage();
        }
    }


    // ingestDocument 方法
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    public IngestResult ingestDocument(String filePath, String fileName, String status) {
        // 1. 解析pdf
        String pdfContent = extractPdfContent(filePath);
        // 解析失败直接返回， 不继续入库流程， 避免脏数据入库
        if (pdfContent.startsWith("pdf extraction failed")) {
            return new IngestResult(null, fileName, status, 0, null, false);
        }
        // 2. 计算hash, 生成docID
        String contentHash = sha256(pdfContent);
        // 只取前16位：16位hex = 64bit熵，碰撞概率极低，同时比64位完整hash更简洁
        //  关键设计：基于内容hash，而不是文件名 → 同一文件改名重传也能识别为重复
        String docId = contentHash.substring(0, 16);

        // 3. 去重检查（幂等！）
        // 幂等 = 同一操作执行多次，结果和执行一次一样
        // 这里的意义：用户重复上传同一文件，系统不会重复入库，节省embedding费用
        if (alreadyIngested(docId)) {
            log.info("⏭️ Already ingested, skipping: {}", fileName);
            return new IngestResult(docId, fileName, status, 0, contentHash, true);
        }

        //构建document + metadata
        //metadata 的作用： 存储文档的“身份信息” ， rag 检索时用来过滤和追踪来源
        Map<String, Object> baseMd = new HashMap<>();
        baseMd.put("docId", docId);
        baseMd.put("contentHash", contentHash);
        baseMd.put("filename", fileName);
        baseMd.put("source", "user_upload");
        baseMd.put("type", "pdf");
        baseMd.put("status", status); // ⚠️ 必须设置，RAG filter用这个！
        baseMd.put("uploadedAt", LocalDateTime.now().toString());

        // 分块（复用现有的TokenTextSplitter
        // 为什么要分块？ 因为整篇文档太长， 向量化效果差， 且检索时会引入无关内容
        Document document = new Document(pdfContent, baseMd);
        List<Document> splitDocs = myTokenTextSplitter.splitCustomized(List.of(document));

        //6. 给每个chunk 加追踪信息
        // chunkIndex：chunk在原文档中的位置（0, 1, 2...），用于Sources引用显示
        // chunkId：全局唯一标识，格式 docId:chunkIndex，方便定位具体chunk
        for (int i = 0; i < splitDocs.size(); i++) {
            splitDocs.get(i).getMetadata().put("chunkIndex", i);
            splitDocs.get(i).getMetadata().put("chunkId", docId + ":" + i);
        }

        // 7. 关键词增强（复用现有Enricher）
        // 为什么在分块后增强？因为每个chunk需要独立的关键词，分块前增强会混淆
        List<Document> enrichedDocs = myKeywordEnricher.enrichDocuments(splitDocs);
        batchAddDocuments(pgVectorVectorStore, enrichedDocs);

        // 8. 批量存入PGVector
        // 为什么批量？DashScope embedding API限制每次最多25条，批量控制避免报错
        log.info("✅ Ingested {} chunks, docId={}", enrichedDocs.size(), docId);
        return new IngestResult(docId, fileName, status, enrichedDocs.size(), contentHash, false);
    }


    private String sha256(String s) {
        try {
            // MessageDigest是Java内置的加密工具类，SHA-256是其中一种哈希算法
            // 哈希算法的特点：同样的输入永远得到同样的输出，且不可逆
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 先把字符串转成UTF-8字节数组，再计算hash
            // 为什么用UTF-8？确保中文字符也能正确处理
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));

            // digest是byte数组，需要转成十六进制字符串才能存储和比较
            // %02x：每个byte转成2位十六进制，不足2位补0（如 0x0A → "0a"）
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString(); // 最终得到64位十六进制字符串
        } catch (Exception e) {
            // SHA-256是Java标准算法，理论上不会找不到，但API要求必须处理异常
            // 用RuntimeException包装，让调用方不需要显式catch
            throw new RuntimeException(e);
        }
    }

    private boolean alreadyIngested(String docId) {
        // metadata是jsonb类型，->>'docId'表示取metadata字段里key为docId的值（返回文本）
        // ->> 和 -> 的区别：-> 返回json对象，->> 返回文本字符串
        // ? 是占位符，防止SQL注入，jdbcTemplate会自动替换
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM vector_store WHERE metadata->>'docId' = ?",
                Integer.class, docId);
        // count可能为null（queryForObject的边界情况），所以要判空
        return count != null && count > 0;
    }

    private void batchAddDocuments(VectorStore vectorStore, List<Document> documents) {
        int batchSize = 20;  // DashScope限制25条/次，用20留安全余量
        for (int i = 0; i < documents.size(); i += batchSize) {
            // Math.min防止最后一批越界
            // 比如共45条：第1批0-19，第2批20-39，第3批40-44（不是40-59）
            int end = Math.min(i + batchSize, documents.size());
            vectorStore.add(documents.subList(i, end));
        }
    }



    //我们新建一个方法 doChatWithRagAndSources，不破坏原有的 doChatWithRag。
    //关键是直接用 pgVectorVectorStore.similaritySearch() 自己控制检索，而不是通过Advisor。

    public String doChatWithRagAndSources(String message, String chatId, String status, String docId) {
        // 1. Query rewrite：把用户的口语化问题改写成更适合向量检索的表达
        // 例如："七夕送啥好" → "七夕节情侣礼物推荐"，提高检索命中率
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        // 2. 构建filter：同时过滤status和docId
        // 为什么要filter？避免检索到其他文档的内容，保证回答只基于指定文档
        String filterExpression;
        if (docId != null && !docId.isBlank()) {
            // docId不为空：只在这份文档里检索（精准模式）
            filterExpression = "status == '" + esc(status) + "' && docId == '" + esc(docId) + "'";
        } else {
            // docId为空：在所有同status的文档里检索（宽泛模式）
            filterExpression = "status == '" + esc(status) + "'";
        }

        // 自己做检索 拿到 chunks
        // 为什么不用Advisor？因为需要拿到原始Document对象，才能提取metadata做Sources
        // 用Advisor的话，检索结果被封装在内部，无法直接获取
        SearchRequest searchRequest = SearchRequest.builder()
                .query(rewrittenMessage)
                .topK(3)  // 最多返回3个最相关的chunks
                .similarityThreshold(0.3)  // 相似度低于0.3的chunks直接丢弃，避免不相关内容
                .filterExpression(filterExpression)
                .build();
        List<Document> retrievedDocs = pgVectorVectorStore.similaritySearch(searchRequest);

        // 拼接 context
        // 把多个chunk用空行分隔，让AI能清晰区分不同片段
        String context = retrievedDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        //构建带sources 的 prompt
        // -----分隔线-----是RAG的标准prompt格式，告诉AI哪部分是参考资料
        String userMessage = "Context information is below.\n\n"
                + "---------------------\n" + context + "\n---------------------\n\n"
                + "Given the context, answer the query.\nQuery: " + rewrittenMessage;

        //调用AI
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(userMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .call().chatResponse();
        String answer = chatResponse.getResult().getOutput().getText();

        //拼接 sources
        // getOrDefault：如果metadata里没有这个key，返回默认值，避免NullPointerException
        // distinct()：同一个文件的多个chunk只显示一次，避免重复
        String sources = retrievedDocs.stream().map(doc -> {
            Map<String, Object> md = doc.getMetadata();
            String filename = (String) md.getOrDefault("filename", "unknown");
            Object chunkIndex = md.getOrDefault("chunkIndex", "?");
            String dId = (String) md.getOrDefault("docId", "unknown");
            return String.format("- %s (chunk %s, docId: %s)", filename, chunkIndex, dId);
        }).distinct().collect(Collectors.joining("\n"));

        return answer + "\n\n**Sources:**\n" + sources;
    }

    // 防注入：转义单引号
    private String esc(String s) {
        // SQL中单引号是字符串的分隔符，如果值里含单引号会破坏SQL语句
        // 标准做法：把 ' 替换成 ''（两个单引号），数据库会自动识别为一个单引号
        // 例如：O'Brien → O''Brien
        // 为什么不用PreparedStatement？因为filterExpression是字符串拼接，不是SQL参数
        return s == null ? "" : s.replace("'", "''");
    }

    //加入MCP
    @Autowired(required = false)
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)  // ← 改这里，tools → toolCallbacks
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}

