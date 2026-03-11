package com.ling.lingaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
@Slf4j
public class RerankService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String RERANK_URL = "http://localhost:8000/rerank";

    public List<String> rerank(String query, List<String> documents, int topK) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("query", query);
            request.put("documents", documents);
            request.put("top_k", topK);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    RERANK_URL, entity, Map.class);

            return (List<String>) response.getBody().get("reranked_documents");
        } catch (Exception e) {
            log.warn("Reranking failed, fallback to original order: {}", e.getMessage());
            return documents;
        }
    }
}