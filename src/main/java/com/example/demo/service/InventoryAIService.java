package com.example.demo.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class InventoryAIService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public InventoryAIService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    public String askQuestion(String question) {
        // 1. Retrieve relevant data from pgvector database
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(question).withTopK(3)
        );

        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        // 2. Build a prompt injecting the local database context
        String promptMessage = """
                You are a helpful AI assistant for a Car Inventory System.
                Use the following context from the inventory database to answer the user's question.
                If the answer is not in the context, just say you don't know based on the current system info.

                Context:
                {context}

                Question:
                {question}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptMessage);
        Prompt prompt = promptTemplate.create(Map.of(
                "context", context,
                "question", question
        ));

        // 3. Send to AWS Bedrock LLM and return the response
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }
}
