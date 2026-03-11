package com.ling.lingaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HybridSearchService {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 混合检索：向量检索 + BM25关键词检索，用 RRF 融合排序
     *
     * RRF (Reciprocal Rank Fusion)：
     * score = Σ 1 / (k + rank)
     * k=60 是标准常数，防止排名靠后的结果分数过低
     */
    public List<Document> hybridSearch(String query, String status, int topK) {

        // 1. 向量检索
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK * 2)  // 多取一些，给 RRF 融合用
                .similarityThreshold(0.2)
                .filterExpression(expression)
                .build();
        List<Document> vectorDocs = pgVectorVectorStore.similaritySearch(searchRequest);
        log.info("Vector search returned {} docs", vectorDocs.size());

        // 2. BM25 关键词检索（PostgreSQL 全文搜索）
        List<Document> bm25Docs = bm25Search(query, status, topK * 2);
        log.info("BM25 search returned {} docs", bm25Docs.size());

        // 3. RRF 融合排序
        List<Document> fusedDocs = rrfFusion(vectorDocs, bm25Docs, topK);
        log.info("RRF fusion returned {} docs", fusedDocs.size());

        return fusedDocs;
    }

    /**
     * PostgreSQL 全文搜索（BM25近似）
     */
    private List<Document> bm25Search(String query, String status, int topK) {
        try {
            // 把查询词转成 tsquery 格式（用 & 连接多个词）
            String tsQuery = Arrays.stream(query.trim().split("\\s+"))
                    .filter(w -> w.length() > 2)
                    .map(w -> w.replaceAll("[^a-zA-Z0-9]", ""))
                    .filter(w -> !w.isEmpty())
                    .collect(Collectors.joining(" & "));

            if (tsQuery.isEmpty()) {
                log.warn("BM25 query is empty after processing, skipping");
                return new ArrayList<>();
            }

            String sql = """
                    SELECT id, content, metadata,
                           ts_rank(content_tsv, to_tsquery('english', ?)) AS rank
                    FROM vector_store
                    WHERE content_tsv @@ to_tsquery('english', ?)
                    AND metadata->>'status' = ?
                    ORDER BY rank DESC
                    LIMIT ?
                    """;

            List<Document> results = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {
                        String content = rs.getString("content");
                        String metadataJson = rs.getString("metadata");
                        Map<String, Object> metadata = parseMetadata(metadataJson);
                        Document doc = new Document(content, metadata);
                        return doc;
                    },
                    tsQuery, tsQuery, status, topK
            );

            return results;
        } catch (Exception e) {
            log.warn("BM25 search failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * RRF (Reciprocal Rank Fusion) 融合算法
     * 把两个排序列表合并成一个更好的排序
     */
    private List<Document> rrfFusion(List<Document> vectorDocs,
                                     List<Document> bm25Docs,
                                     int topK) {
        final int K = 60; // RRF 标准常数
        Map<String, Double> scoreMap = new HashMap<>();
        Map<String, Document> docMap = new HashMap<>();

        // 向量检索结果打分
        for (int i = 0; i < vectorDocs.size(); i++) {
            Document doc = vectorDocs.get(i);
            String key = getDocKey(doc);
            scoreMap.merge(key, 1.0 / (K + i + 1), Double::sum);
            docMap.put(key, doc);
        }

        // BM25 结果打分
        for (int i = 0; i < bm25Docs.size(); i++) {
            Document doc = bm25Docs.get(i);
            String key = getDocKey(doc);
            scoreMap.merge(key, 1.0 / (K + i + 1), Double::sum);
            docMap.putIfAbsent(key, doc);
        }

        // 按 RRF 分数排序，取 topK
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(e -> docMap.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 用 content 前50个字符作为文档唯一标识
     */
    private String getDocKey(Document doc) {
        String content = doc.getText();
        return content != null ? content.substring(0, Math.min(50, content.length())) : UUID.randomUUID().toString();
    }

    /**
     * 解析 metadata JSON 字符串
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(metadataJson, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}