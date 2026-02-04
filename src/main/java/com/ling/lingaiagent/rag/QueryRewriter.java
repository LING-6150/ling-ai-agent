package com.ling.lingaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 * purpose : optimizes colloquial and non-standard user queries
 * Examples:
 *  * Examples:
 *  * - "How do I get a girlfriend?" → "What are effective strategies for starting a relationship?"
 *  * - Casual question → Standardized query
 *
 *  Mechanism:
 *  use LLM to rewrite user questions into more standard expressions
 *  suitable for retrieval
 *
 *  Known Issue:
 *  -Default prompt converts Chinese to English (needs optimization)
 *
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        // create query rewrite transformer
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 执行查询重写
     *
     * @param prompt
     * @return
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // call LLM to rewrite query
        Query transformedQuery = queryTransformer.transform(query);
        // 输出重写后的查询
        return transformedQuery.text();
    }
}
