package com.ling.lingaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;
    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }
    @Tool(description = "Search for information from the Internet")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        try {
            String url = SEARCH_API_URL + "?engine=google&api_key=" + apiKey + "&q=" + URLEncoder.encode(query, "UTF-8");
            String response = HttpUtil.get(url);
            System.out.println("API原始返回: " + response);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            List<Object> objects = organicResults.subList(0, 5);
            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));
            return result;
        } catch (Exception e) {
            return "Error searching: " + e.getMessage();
        }
    }
}
