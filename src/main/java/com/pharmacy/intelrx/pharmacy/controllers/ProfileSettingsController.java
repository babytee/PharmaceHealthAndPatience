package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.SecurityQuestionsAnswerRequest;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.pharmacy.services.ProfileSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("PharmacyProfileSettingsController")
public class ProfileSettingsController {
    private final ProfileSettingsService profileSettingsService;

    @GetMapping({"get_personal_details"})
    public ResponseEntity<?> getPersonalDetails() {
        return profileSettingsService.getPersonalDetails();
    }

    @PostMapping({"manage_personal_details"})
    public ResponseEntity<?> managePersonalDetails(@RequestBody UserRequest request) throws IOException {
        return profileSettingsService.managePersonalDetails(request);
    }

    @GetMapping({"get_my_answers"})
    public ResponseEntity<?> getMyAnswers() {
        return profileSettingsService.getMyAnswers();
    }

    @PostMapping({"manage_my_answers"})
    public ResponseEntity<?> manageMyAnswers(@RequestBody SecurityQuestionsAnswerRequest request) throws IOException {
        return profileSettingsService.manageMyAnswers(request);
    }

    @PostMapping({"change_password"})
    public ResponseEntity<?> changePassword(@RequestBody UserRequest request) {
        return profileSettingsService.changePassword(request);
    }

}
