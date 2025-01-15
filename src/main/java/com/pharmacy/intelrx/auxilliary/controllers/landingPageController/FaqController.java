package com.pharmacy.intelrx.auxilliary.controllers.landingPageController;

import com.pharmacy.intelrx.auxilliary.LandingPages.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController("faqCategory")
@RequestMapping({"/api/v1/faq"})
public class FaqController {

    private final FaqService faqService;

    @GetMapping({"all_pharmacy_owners"})
    public ResponseEntity<?> allPharmacyOwnersFaqs() {
        return faqService.allPharmacyOwnersFaqs();
    }

    @GetMapping({"all_pharmacists"})
    public ResponseEntity<?> allPharmacistsFaqs() {
        return faqService.allPharmacistsFaqs();
    }
}
