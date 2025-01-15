package com.pharmacy.intelrx.openAI;

import com.pharmacy.intelrx.common.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service("OpenAiService")
public class OpenAiService {

    @Qualifier("openaiRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${spring.openai.model}")
    private String model;

    @Value("${spring.openai.api.url}")
    private String apiUrl;

    @Value("${spring.openai.api.key}")
    private String apiKey;


    public String aboutMedication(String prompt) {
        String promptMsg = "About the following medications not more than 20 words each.\n" +
                "Medications: ";
        promptMsg += prompt;
        AIChatRequest request = new AIChatRequest(
                model,
                promptMsg, 1, 4000);

        // Log API details for debugging
        System.out.println("API URL: " + apiUrl);
        System.out.println("Request Model: " + model);
        System.out.println("Prompt: " + prompt);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Create HttpEntity with the request and headers
        HttpEntity<AIChatRequest> entity = new HttpEntity<>(request, headers);

        // Send the request using exchange method
        ResponseEntity<AIChatResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, AIChatResponse.class);

        // Check response and return result
        AIChatResponse response = responseEntity.getBody();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "No response";
        }

        String jsonResponse = response.getChoices().get(0).getMessage().getContent();

        // Convert the message to HTML
        String htmlResponse = convertToHtml(jsonResponse);

        return htmlResponse;
    }

    public String checkMedicationInteractions(String prompt) {
        String promptMsg = "As a Clinical Pharmacist, review the following medications:\n" +
                "\n" +
                "1. Drug Indications (≤100 words)\n" +
                "2. Interactions: Describe severity and evidence level numerically (≤200 words). Note any interactions among these medications.\n" +
                "3. Pharmacokinetics (≤100 words)\n" +
                "4. Dosage Suggestion (≤100 words)\n" +
                "5. Usage Considerations (≤100 words)\n" +
                "\n" +
                "Audience: Fellow Pharmacists\n" +
                "\n" +
                "Medications: " +
                "Remove the ### sign from the response and make each heading comes in " +
                "capital letter. Be precise and clear and state exact interaction among " +
                "or between the medications selected. " +
                "Let only the heading to be bold while the body of the text should be light." +
                "Also go straight without any statement like sure or anything of such";

        promptMsg += prompt;
        AIChatRequest request = new AIChatRequest(
                model,
                promptMsg, 1, 4000);

        // Log API details for debugging
        System.out.println("API URL: " + apiUrl);
        System.out.println("Request Model: " + model);
        System.out.println("Prompt: " + prompt);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Create HttpEntity with the request and headers
        HttpEntity<AIChatRequest> entity = new HttpEntity<>(request, headers);

        // Send the request using exchange method
        ResponseEntity<AIChatResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, AIChatResponse.class);

        // Check response and return result
        AIChatResponse response = responseEntity.getBody();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "No response";
        }

        String jsonResponse = response.getChoices().get(0).getMessage().getContent();


        // Convert the message to HTML
        String htmlResponse = convertToHtml(jsonResponse);

        return htmlResponse;
    }


    public String getChatCompletion(String model, String prompt) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + apiKey);

        String jsonPayload = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"system\", \"content\": \"%s\"}], \"temperature\": 1, \"max_tokens\": 256, \"top_p\": 1, \"frequency_penalty\": 0, \"presence_penalty\": 0}",
                model, prompt);

        httpPost.setEntity(new StringEntity(jsonPayload));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return responseBody;
        }
    }


    private String convertToHtml(String message) {
        // Check if the message looks like a list
        if (message.matches(".*\\d\\.\\s\\*\\*.*\\*\\*.*")) {
            // Convert markdown-like text to HTML list
            String[] lines = message.split("\\\\n");
            StringBuilder htmlBuilder = new StringBuilder("<ul>");
            for (String line : lines) {
                htmlBuilder.append("<li>")
                        .append(line.replace("**", "<strong>").replace("**", "</strong>"))
                        .append("</li>");
            }
            htmlBuilder.append("</ul>");
            return htmlBuilder.toString();
        } else {
            // Convert markdown-like text to HTML paragraph
            return "<p>" + message.replace("**", "<strong>").replace("**", "</strong>") + "</p>";
        }
    }
}
