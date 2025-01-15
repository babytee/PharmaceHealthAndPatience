package com.pharmacy.intelrx.openAI;

import com.pharmacy.intelrx.common.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RequestMapping("/api/v1/intelrx/ai")
@RestController("OpenAiSController")
@RequiredArgsConstructor

public class OpenAiSController {

    private final OpenAiService openAiService;

    @GetMapping("/about_medication")
    public ResponseEntity<?> aboutMedication(@RequestParam String prompt) {
        try {
            String response = String.valueOf(openAiService.aboutMedication(prompt));
            return ResponseEntity.ok(StandardResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(StandardResponse.error("Runtime error: " + e.getMessage()));
        }
    }

    @GetMapping("/check_medication_interactions")
    public ResponseEntity<?> checkMedicationInteractions(@RequestParam String prompt) {
        try {
            String response = String.valueOf(openAiService.checkMedicationInteractions(prompt));
            return ResponseEntity.ok(StandardResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(StandardResponse.error("Runtime error: " + e.getMessage()));
        }
    }

//    @GetMapping("/chat")
//    public String getChatResponse(@RequestParam String prompt) {
//        try {
//            String model = "gpt-3.5-turbo";
//            return openAiService.getChatCompletion(model, prompt);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error: " + e.getMessage();
//        }
//    }

}
