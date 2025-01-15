package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.auxilliary.dto.FaqRequest;
import com.pharmacy.intelrx.admin.services.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController("adminFaqCategory")
@RequestMapping({"/api/v1/admin/faq"})
public class FaqController {

    private final FaqService faqService;

    @PostMapping({"store"})
    public ResponseEntity<?> store(@RequestBody FaqRequest request) {
        return faqService.store(request);
    }

    @PatchMapping({"update/{id}"})
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody FaqRequest request) {
        return faqService.update(id,request);
    }

    @GetMapping({"{id}"})
    public ResponseEntity<?> fetchSingleFaq(@PathVariable long id) {
        return faqService.fetchSingleFaq(id);
    }

    @DeleteMapping({"delete/{id}"})
    public ResponseEntity<?> deleteFaq(@PathVariable long id) {
        return faqService.deleteFaq(id);
    }

    @GetMapping({"status/{userType}"})
    public ResponseEntity<?> allFaqs(@PathVariable String userType) {
        return faqService.allFaqs(userType);
    }
}
