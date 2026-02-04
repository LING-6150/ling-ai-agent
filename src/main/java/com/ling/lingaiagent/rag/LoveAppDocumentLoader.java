package com.ling.lingaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.document.Document;


@Component
@Slf4j
/**
 * LoveApp 知识文档加载器
 *
 * 作用：
 * 1️⃣ 从 classpath 中读取 Markdown 知识文件
 * 2️⃣ 按配置规则将 Markdown 拆分为多个 Document
 * 3️⃣ 为每个 Document 补充元数据
 *
 * 该类是 RAG 流程的「入口」：
 * 文档 → Document → 向量化 → 检索 → 注入 Prompt
 */
class LoveAppDocumentLoader {

    /**
     * Spring 提供的资源解析器
     * 用于按路径模式（classpath / file / wildcard）加载资源
     */
    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 这里可以修改为你要加载的多个 Markdown 文件的路径模式
            // 加载document 目录下所有的markdown 文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                // 提取文档倒数第 3 和第 2 个字作为标签
                String status = fileName.substring(fileName.length() - 6, fileName.length() - 4);
                // 2️⃣ 配置 Markdown 解析规则
                // - 使用水平分割线（---）拆分文档
                // - 忽略代码块和引用内容
                // - 为每个 Document 添加 filename 元数据
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .withAdditionalMetadata("status", status) // add status
                        .build();
                // 3️⃣ 解析 Markdown 为 Document 列表
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
    //辅助方法： 根据文件名判断状态
    private String determineStatus(String filename) {
        if (filename.contains("单身")) return "单身";
        if (filename.contains("恋爱")) return "恋爱";
        if (filename.contains("已婚")) return "已婚";
        return "通用";
    }
}
