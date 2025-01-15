package com.pharmacy.intelrx.openAI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIChatRequest {
    private String model;
    private List<AIMessage> messages;
    private int max_tokens;
    private double temperature;

    public AIChatRequest(String model, String prompt,double temperature,int max_tokens) {
        this.model = model;

        this.messages = new ArrayList<>();
        this.messages.add(new AIMessage("user", prompt));
        this.temperature = temperature;
        this.max_tokens = max_tokens;
    }
}
