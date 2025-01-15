package com.pharmacy.intelrx.SmsByTermii;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@RequiredArgsConstructor
@Service
public class TermiiSMSService {

    private static final Logger logger = LoggerFactory.getLogger(TermiiSMSService.class);

    private final RestTemplate restTemplate;

    @Value("${spring.termii.api.key}")
    private String apiKey;

    @Value("${spring.termii.api.url}")
    private String smsUrl;

    @Value("${spring.termii.sms.from}")
    private String smsFrom;

    public String sendSms(String to, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        TermiiSmsRequest termiiSmsRequest = new TermiiSmsRequest(to, smsFrom, message, "plain", "generic",apiKey);

        HttpEntity<TermiiSmsRequest> request = new HttpEntity<>(termiiSmsRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(smsUrl, HttpMethod.POST, request, String.class);

        logger.info("smsUrl: {}, smsFrom: {}, apiKey: {}", smsUrl, smsFrom, apiKey);
        logger.info("Response Status Code: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());

        return extractMessage(response.getBody());
    }

    private String extractMessage(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("message").asText();
            String code = root.path("code").asText();
            logger.info("Extracted message: {}, code: {}", message, code);
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing response";
        }
    }

}
