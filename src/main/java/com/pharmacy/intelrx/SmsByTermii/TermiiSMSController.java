package com.pharmacy.intelrx.SmsByTermii;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/intelrx/termiisms")
@RestController("TermiiSMSController")
@RequiredArgsConstructor
public class TermiiSMSController {
    private final TermiiSMSService smsService;
    @GetMapping("/send-sms")
    public String sendSms(@RequestParam String to, @RequestParam String message) {
        return smsService.sendSms(to, message);
    }

}
