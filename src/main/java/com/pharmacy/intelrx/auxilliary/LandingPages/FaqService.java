package com.pharmacy.intelrx.auxilliary.LandingPages;

import com.pharmacy.intelrx.auxilliary.dto.CustomSingleResponse;
import com.pharmacy.intelrx.auxilliary.dto.FaqResponse;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.models.Faq;
import com.pharmacy.intelrx.auxilliary.repositories.FaqRepository;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("FaqService")
public class FaqService {
    private final FaqRepository faqRepository;
    private final Utility utility;

    public ResponseEntity<?> allPharmacyOwnersFaqs() {

        List<Faq> faqList = faqRepository.findAllByUserType("Pharmacy Owners");

        if (faqList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "faq is empty"));
        }

        List<FaqResponse> faqResponseList = (List) faqList.stream().map((address) -> {
            return this.mapToFaqResponse(address);
        }).collect(Collectors.toList());

        CustomSingleResponse<FaqResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(faqResponseList);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allPharmacistsFaqs() {

        List<Faq> faqList = faqRepository.findAllByUserType("Pharmacists");

        if (faqList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "faq is empty"));
        }

        List<FaqResponse> faqResponseList = (List) faqList.stream().map((address) -> {
            return this.mapToFaqResponse(address);
        }).collect(Collectors.toList());

        CustomSingleResponse<FaqResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(faqResponseList);
        return ResponseEntity.ok(response);
    }

    private FaqResponse mapToFaqResponse(Faq faq) {

        FaqResponse faqResponse = new FaqResponse();
        faqResponse.setId(faq.getId());
        faqResponse.setUserType(faq.getUserType());
        faqResponse.setQuestion(faq.getQuestion());
        faqResponse.setAnswer(faq.getAnswer());
        return faqResponse;

    }
}
