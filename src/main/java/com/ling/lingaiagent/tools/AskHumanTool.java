package com.ling.lingaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class AskHumanTool {

    @Tool(description = """
            Use this tool to ask human for help when you encounter a problem
            that you cannot solve by yourself, or when you need more information
            from the user to complete the task.
            """)
    public String askHuman(String question) {
        System.out.println("\n🤖 LingManus需要你的帮助: " + question);
        System.out.print("你的回答: ");
        Scanner scanner = new Scanner(System.in);
        return "用户回答: " + scanner.nextLine();
    }
}