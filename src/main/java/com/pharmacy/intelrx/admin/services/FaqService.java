package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.auxilliary.dto.CustomSingleResponse;
import com.pharmacy.intelrx.auxilliary.dto.FaqRequest;
import com.pharmacy.intelrx.auxilliary.dto.FaqResponse;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.models.Faq;
import com.pharmacy.intelrx.auxilliary.repositories.FaqRepository;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminFaqService")
public class FaqService {
    private final FaqRepository faqRepository;
    private final Utility utility;

    public ResponseEntity<?> store(FaqRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getUserType())) {
            return ResponseEntity.ok().body(new Response("failed", "userType is required"));
        } else if (utility.isNullOrEmpty(request.getQuestion())) {
            return ResponseEntity.ok().body(new Response("failed", "question is required"));
        } else if (utility.isNullOrEmpty(request.getAnswer())) {
            return ResponseEntity.ok().body(new Response("failed", "answer is required"));
        }else {

            if (!request.getUserType().equals("Pharmacy Owners") && !request.getUserType().equals("Pharmacists")) {
                return ResponseEntity.ok().body(new Response("failed", "userType has to be Pharmacy Owners or Pharmacists"));
            }

            Faq faq = Faq.builder()
                    .userType(request.getUserType())
                    .question(request.getQuestion())
                    .answer(request.getAnswer())
                    .build();
            faqRepository.save(faq);
            return ResponseEntity.ok().body(new Response("success", "Faq Added Successfully"));
        }
    }

    public ResponseEntity<?> update(long id, FaqRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }  else if (utility.isNullOrEmpty(request.getUserType())) {
            return ResponseEntity.ok().body(new Response("failed", "userType is required"));
        } else if (utility.isNullOrEmpty(request.getQuestion())) {
            return ResponseEntity.ok().body(new Response("failed", "question is required"));
        } else if (utility.isNullOrEmpty(request.getAnswer())) {
            return ResponseEntity.ok().body(new Response("failed", "answer is required"));
        } else {

            Optional<Faq> optionalFaq = faqRepository.findById(id);
            if (!optionalFaq.isPresent()) {
                return ResponseEntity.ok().body(new Response("failed", "Faq does not exist or the id Not Found"));
            }

            if (!request.getUserType().equals("Pharmacy Owners") && !request.getUserType().equals("Pharmacists")) {
                return ResponseEntity.ok().body(new Response("failed", "userType has to be Pharmacy Owners or Pharmacists"));
            }


            Faq faq = optionalFaq.get();
            faq.setUserType(request.getUserType());
            faq.setQuestion(request.getQuestion());
            faq.setAnswer(request.getAnswer());
            faqRepository.save(faq);
            return ResponseEntity.ok().body(new Response("failed", "Faq Is Updated Successfully"));
        }
    }

    public ResponseEntity<?> allFaqs(String userType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }

        if (utility.isNullOrEmpty(userType)) {
            return ResponseEntity.ok().body(new Response("failed", "userType is required"));
        }

        if (!userType.equals("Pharmacy Owners") && !userType.equals("Pharmacists")) {
            return ResponseEntity.ok().body(new Response("failed", "userType has to be Pharmacy Owners or Pharmacists"));
        }


        List<Faq> faqList = faqRepository.findAllByUserType(userType);

        if (faqList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "faq does not exist or the id Not Found"));
        }

        List<FaqResponse> faqResponseList = (List) faqList.stream().map((address) -> {
            return this.mapToFaqResponse(address);
        }).collect(Collectors.toList());

        CustomSingleResponse<FaqResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(faqResponseList);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> fetchSingleFaq(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "is is required"));
        }

        Optional<Faq> optionalFaq = faqRepository.findById(id);
        if (!optionalFaq.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "category does not exist or the id Not Found"));
        }

        Faq faq = optionalFaq.get();

        FaqResponse faqResponse = mapToFaqResponse(faq);

        CustomSingleResponse<FaqResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(faqResponse);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> deleteFaq(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "id is required"));
        }

        Optional<Faq> optionalFaq = faqRepository.findById(id);
        if (!optionalFaq.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "faq does not exist or the id Not Found"));
        }

        Faq faq = optionalFaq.get();
        faqRepository.delete(faq);
        return ResponseEntity.ok().body(new Response("failed", "deleted successfully"));
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
