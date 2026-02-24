package com.ling.lingaiagent.app;

/** 结构化返回
 *
 * @param docId
 * @param filename
 * @param status
 * @param chunks
 * @param contentHash
 * @param skippedAsDuplicate
 */
public record IngestResult(
        String docId, // 文档唯一ID （SHA-256前16位）
        String filename, // 文件名
        String status, //状态（单身、恋爱、已婚、通用）
        int chunks, // 实际入库的chunk 数量
        String contentHash, // 完整SHA-256 hash
        boolean skippedAsDuplicate // 是否因为重复而跳过
) {}