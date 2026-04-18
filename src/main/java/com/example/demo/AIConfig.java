package com.example.demo;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    // Spring AI auto-configures ChatModel using AWS credentials from ~/.aws/credentials
    // No manual bean definition needed when using aws configure
}
