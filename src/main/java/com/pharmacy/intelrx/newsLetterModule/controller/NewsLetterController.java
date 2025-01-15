package com.pharmacy.intelrx.newsLetterModule.controller;

import com.pharmacy.intelrx.newsLetterModule.dto.NewsLetterRequest;
import com.pharmacy.intelrx.newsLetterModule.services.NewsLetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/newsletter"})
@RestController
public class NewsLetterController {
    private final NewsLetterService newsLetterService;

    @PostMapping("create")
    public ResponseEntity<?> createNewsLetter(@RequestBody @Valid NewsLetterRequest request) {
        return newsLetterService.createNewsLetter(request);
    }
}
