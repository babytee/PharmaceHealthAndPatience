package com.pharmacy.intelrx.newsLetterModule.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.newsLetterModule.dto.NewsLetterRequest;
import com.pharmacy.intelrx.newsLetterModule.models.NewsLetter;
import com.pharmacy.intelrx.newsLetterModule.repository.NewsLetterRepository;
import com.pharmacy.intelrx.utility.AdminDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class NewsLetterService {
    private final NewsLetterRepository newsLetterRepository;
    private final AdminDetailsService userDetailsService;
    public ResponseEntity<?> createNewsLetter(NewsLetterRequest request) {
       var admin = userDetailsService.getAuthenticatedUser(); // Ensure the user is authenticated.

        if(request.getScheduleTimeType().equals("Later") &
                request.getScheduleTime() == null){
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("scheduleTime is required"));
        }
        // Build the NewsLetter object after validation
        NewsLetter newsLetter = NewsLetter.builder()
                .newsType(request.getNewsType())
                .sendTo(request.getSendTo())
                .scheduleTimeType(request.getScheduleTimeType())
                .scheduleTime(request.getScheduleTime())
                .newsLetterTitle(request.getNewsLetterTitle())
                .adminId(admin.getId())
                .content(request.getContent())
                .newsStatus(request.getNewsStatus())
                .createdAt(LocalDateTime.now())
                .build();

        newsLetterRepository.save(newsLetter);
        return ResponseEntity.ok(StandardResponse.success("Newsletter Created Successfully"));
    }
}
