package com.ling.lingaiagent.app;

import com.ling.lingaiagent.tools.WebSearchTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Assertions;

import java.util.UUID;

@SpringBootTest
@Slf4j  // 添加日志注解
class LoveAppTest {

        @Resource
        private LoveApp loveApp;

        @Test
        void testChat() {
            String chatId = UUID.randomUUID().toString();
            // 第一轮
            String message = "你好，我是Ling";
            String answer = loveApp.doChat(message, chatId);
            Assertions.assertNotNull(answer);
            // 第二轮
            message = "我想让另一半(JIJI）更爱我";
            answer = loveApp.doChat(message, chatId);
            Assertions.assertNotNull(answer);
            // 第三轮
            message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
            answer = loveApp.doChat(message, chatId);
            Assertions.assertNotNull(answer);
        }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是Ling，我想让另一半（JiJi）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }


    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId,"已婚");
        Assertions.assertNotNull(answer);
    }

    @Test
    void testDoChatWithTools() {
        String chatId = UUID.randomUUID().toString();
        String result = loveApp.doChatWithTools(
                "请立即调用writeFile工具，将以下内容保存为文件，文件名：love_record.txt，内容：姓名=小明，状态=单身，喜欢=小红，问题=不知道如何表白。不要问我任何问题，直接保存！",
                chatId
        );
        System.out.println(result);
    }

    @Test
    void testWebSearchTool() {
        WebSearchTool tool = new WebSearchTool("pVEekXeWWe72tguRwE6ziXGY");
        String result = tool.searchWeb("上海情侣约会地点");
        System.out.println(result);
    }

    @Test
    void testWebScraping(){
            String chatId = UUID.randomUUID().toString();
            String result= loveApp.doChatWithTools(
                    "最近和对象吵架了， 看看Ling 有什么方法（https://github.com/LING-6150）上面其他情侣怎么解决矛盾的？",
                    chatId
            );
            System.out.println(result);
    }

    @Test
    void testResourceDownload() {
        String chatId = UUID.randomUUID().toString();
        String result = loveApp.doChatWithTools(
                "直接下载一张适合做手机壁纸的星空情侣图片，文件名保存为couple_wallpaper.jpg",
                chatId
        );
        System.out.println(result);
    }

    @Test
    void testTerminalOperation() {
        String chatId = UUID.randomUUID().toString();
        String result = loveApp.doChatWithTools(
                "执行 ls 命令，查看当前目录下的文件列表",
                chatId
        );
        System.out.println(result);
    }
    @Test
    void testPDFGeneration() {
        String chatId = UUID.randomUUID().toString();
        String result = loveApp.doChatWithTools(
                "生成一份《七夕约会计划》PDF，包含餐厅预订、活动流程和礼物清单，文件名为qixi_plan.pdf",
                chatId
        );
        System.out.println(result);
    }

    @Test
    void testDoChatWithDocument() {
        String chatId = UUID.randomUUID().toString();
        // 用昨天生成的七夕计划PDF
        String filePath = System.getProperty("user.dir") + "/tmp/pdf/qixi_plan.pdf";
        String result = loveApp.doChatWithDocument(
                "这份计划里有哪些礼物推荐？",
                chatId,
                filePath
        );
        System.out.println(result);
    }


    @Test
    void testRagAfterIngest() {
        String chatId = UUID.randomUUID().toString();
        String result = loveApp.doChatWithRag(
                "七夕约会计划里有哪些礼物推荐？",
                chatId,
                "通用"
        );
        System.out.println("RAG查询结果: " + result);
    }

    @Test
    void testIngestDocumentV2() {
        String filePath = System.getProperty("user.dir") + "/tmp/pdf/qixi_plan.pdf";

        // 第一次入库
        IngestResult result1 = loveApp.ingestDocument(filePath, "qixi_plan.pdf", "通用");
        System.out.println("第一次入库: " + result1);

        // 第二次入库（测试幂等/去重）
        IngestResult result2 = loveApp.ingestDocument(filePath, "qixi_plan.pdf", "通用");
        System.out.println("第二次入库: " + result2);

        // 验证去重生效
        Assertions.assertTrue(result2.skippedAsDuplicate(), "第二次应该被跳过！");
        Assertions.assertEquals(result1.docId(), result2.docId());
        System.out.println("✅ 去重验证通过！docId=" + result1.docId());
    }



    @Test
    void testDoChatWithRagAndSourcesWithDocId() {
        String chatId = UUID.randomUUID().toString();

        // 先入库拿到docId
        String filePath = System.getProperty("user.dir") + "/tmp/pdf/qixi_plan.pdf";
        IngestResult ingestResult = loveApp.ingestDocument(filePath, "qixi_plan.pdf", "通用");
        System.out.println("docId: " + ingestResult.docId());
        System.out.println("skipped: " + ingestResult.skippedAsDuplicate());

        // 用docId filter检索，只在这份文档里问
        String result = loveApp.doChatWithRagAndSources(
                "七夕有哪些礼物推荐？",
                chatId,
                "通用",
                ingestResult.docId()  // 传入docId
        );
        System.out.println(result);
    }


    @Test
    void testDoChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "我的另一半居住在上海静安寺地铁站附近，请帮我找到3公里内适合情侣的餐厅";
        String answer = loveApp.doChatWithMcp(message, chatId);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

    @Test
    void testDoChatWithMcpImage() {
        String chatId = UUID.randomUUID().toString();
        String message = "帮我搜索一些适合情侣约会的浪漫图片";
        String answer = loveApp.doChatWithMcp(message, chatId);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }
}

