package com.ling.lingaiagent.agent;

import com.ling.lingaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类，用于管理状态和执行流程
 */
@Slf4j
@Data

public abstract class BaseAgent {
    // 核心属性
    private String name;
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory - 自主维护消息上下文
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (!StringUtils.hasText(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();


        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                // 这里修改了， 因为为了弄isStuck()， 为了防止循环
                String stepResult = step();

               // 检查是否陷入循环
                if (isStuck()) {
                    handleStuckState();
                }
                String result = "Step " + stepNumber + ": " + stepResult;

                results.add(result);

                // 检查是否超出步骤限制
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                }
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误: " + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    //isStuck()加在BaseAgent里，逻辑是检测最近的消息是否重复出现，如果AI一直输出同样内容就判定为卡死。
    /**
     * 检查是否陷入循环
     */
    protected boolean isStuck() {
        List<Message> messages = this.messageList;
        if (messages.size() < 2) {
            return false;
        }

        Message lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.getText() == null || lastMessage.getText().isEmpty()) {
            return false;
        }

        // 计算重复内容出现次数
        int duplicateCount = 0;
        for (int i = messages.size() - 2; i >= 0; i--) {
            Message msg = messages.get(i);
            if (lastMessage.getText().equals(msg.getText())) {
                duplicateCount++;
            }
        }
        return duplicateCount >= 2;
    }

    /**
     * 处理陷入循环的状态
     */
    protected void handleStuckState() {
        String stuckPrompt = "观察到重复响应，考虑新策略，避免重复已经尝试过的无效路径。";
        this.nextStepPrompt = stuckPrompt + "\n" +
                (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        log.info("Agent detected stuck state, added prompt: " + stuckPrompt);
    }

    /**
     * 执行单个步骤，必须由子类实现
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}
