package com.pharmacy.intelrx.openAI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIChatResponse {
    private List<Choice> choices;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {

        private int index;
        private AIMessage message;

    }
}
