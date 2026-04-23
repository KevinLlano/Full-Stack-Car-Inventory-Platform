package com.example.demo.controllers;

import com.example.demo.service.InventoryAIService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class InventoryAIController {

    private final InventoryAIService aiService;

    public InventoryAIController(InventoryAIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public String askAi(@RequestBody Map<String, String> payload) {
        String question = payload.getOrDefault("question", "");
        return aiService.askQuestion(question);
    }
}
