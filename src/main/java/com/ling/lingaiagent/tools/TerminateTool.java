package com.ling.lingaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request is met OR
            if the assistant cannot proceed further with the task.
            When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate(String status) {
        return "The interaction has been completed with status: " + status;
    }
}