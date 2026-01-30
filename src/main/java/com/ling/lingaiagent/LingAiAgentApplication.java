package com.ling.lingaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
public class LingAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingAiAgentApplication.class, args);
    }
}

