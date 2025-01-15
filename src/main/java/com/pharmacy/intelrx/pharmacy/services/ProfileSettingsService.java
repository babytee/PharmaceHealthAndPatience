package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.SecurityQuestionsAnswerRequest;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.pharmacy.models.SecurityQuestionsAnswer;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.SecurityQuestion;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.SecurityQuestionsAnswerRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.SecurityQuestionRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service("PharmacyProfileSettingsService")
public class ProfileSettingsService {
    private final UserDetailsService userDetailsService;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final Utility utility;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final SecurityQuestionsAnswerRepository securityQuestionsAnswerRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> getPersonalDetails() {
        var user = userDetailsService.getAuthenticatedUser();
        return ResponseEntity.ok(StandardResponse.success(userDetailsService.mapToUserResponse(user)));
    }

    public ResponseEntity<?> managePersonalDetails(UserRequest request) throws IOException {
        try {
            var user = userDetailsService.getAuthenticatedUser();

            String logo = null;
            if (!utility.isNullOrEmpty(request.getProfilePic())) {
                logo = s3Service.uploadFileDoc(request.getProfilePic(), "pharmacy");
            } else {
                logo = user.getProfilePic();
            }

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            //user.setEmail(request.getEmail());
            user.setProfilePic(logo);
            userRepository.save(user);

            return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    public ResponseEntity<?> getMyAnswers() {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<SecurityQuestionsAnswer> optional = securityQuestionsAnswerRepository.findByIntelRxIdAndUser(
                intelRxId, user
        );
        return ResponseEntity.ok(StandardResponse.success(optional.get()));
    }

    public ResponseEntity<?> manageMyAnswers(SecurityQuestionsAnswerRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("Un-Authorized"));
        } else if (request.getSecurityQuestionIdOne() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("securityQuestionIdOne can not be empty"));
        } else if (utility.isNullOrEmpty(request.getAnswerOne())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("answerOne can not be empty"));
        } else if (request.getSecurityQuestionIdTwo() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("securityQuestionIdTwo can not be empty"));
        } else if (utility.isNullOrEmpty(request.getAnswerTwo())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("answerTwo can not be empty"));
        }
        var intelRxId = userDetailsService.getIntelRxId();

        Optional<SecurityQuestion> optionalSecurityQuestionOne = securityQuestionRepository.findById(request.getSecurityQuestionIdOne());
        if (!optionalSecurityQuestionOne.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("securityQuestion One Not Found"));
        }
        SecurityQuestion securityQuestionOne = optionalSecurityQuestionOne.get();

        Optional<SecurityQuestion> optionalSecurityQuestionTwo = securityQuestionRepository.findById(request.getSecurityQuestionIdTwo());
        if (!optionalSecurityQuestionTwo.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("securityQuestion Two Not Found"));
        }
        SecurityQuestion securityQuestionTwo = optionalSecurityQuestionTwo.get();

        Optional<SecurityQuestionsAnswer> optional = securityQuestionsAnswerRepository.findByIntelRxIdAndUser(
                intelRxId, user
        );

        SecurityQuestionsAnswer questionsAnswer = null;
        if (optional.isPresent()) {
            questionsAnswer = optional.get();
        } else {
            questionsAnswer = new SecurityQuestionsAnswer();
        }
        questionsAnswer.setIntelRxId(intelRxId);
        questionsAnswer.setUser(user);
        questionsAnswer.setSecurityQuestionOne(securityQuestionOne);
        questionsAnswer.setAnswerOne(request.getAnswerOne());
        questionsAnswer.setSecurityQuestionTwo(securityQuestionTwo);
        questionsAnswer.setAnswerTwo(request.getAnswerTwo());
        questionsAnswer.setCreatedDateAt(LocalDateTime.now());
        securityQuestionsAnswerRepository.save(questionsAnswer);
        return ResponseEntity.ok(StandardResponse.success("Security Question Set"));
    }

    public ResponseEntity<?> changePassword(UserRequest request) {
        var user = userDetailsService.getAuthenticatedUser();

        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("Un-Authorized"));
        } else if (this.utility.isNullOrEmpty(request.getOldPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("oldPassword is required"));
        } else if (this.utility.isNullOrEmpty(request.getNewPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("newPassword is required"));
        } else if (this.utility.isNullOrEmpty(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("confirmPassword is required"));
        } else if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("password does not match"));
        }

        var intelRxId = userDetailsService.getIntelRxId();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Old Password not match"));
        }
        user.setPassword(this.passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().body(StandardResponse.success("Password has been successfully reset"));
    }

}
