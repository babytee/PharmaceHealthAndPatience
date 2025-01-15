package com.pharmacy.intelrx.brand.controllers;

import com.pharmacy.intelrx.auxilliary.dto.OtpRequest;
import com.pharmacy.intelrx.auxilliary.services.LogoutService;
import com.pharmacy.intelrx.brand.dto.BrandAuthRequest;
import com.pharmacy.intelrx.brand.services.AuthService;
import com.pharmacy.intelrx.pharmacy.dto.ForgotPasswordRequest;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@RestController("BrandAuthController")
@RequestMapping({"/api/v1/brand/auth"})
public class AuthController {
    private final AuthService authService;
    private final LogoutService logoutService;

    @PostMapping({"register"})
    public ResponseEntity<?> register(@RequestBody BrandAuthRequest request) throws Exception {
        return authService.Register(request);
    }

    @GetMapping({"verify_email/{encryptedEmail}"})
    public ResponseEntity<?> verifyEmail(@PathVariable String encryptedEmail) {
        return authService.verifyEmail(encryptedEmail);
    }

    @GetMapping({"two_factor_auth"})
    public ResponseEntity<?> updateTwoFactorAuth() {
        return authService.updateTwoFactorAuth();
    }


    @PostMapping({"login"})
    public ResponseEntity<?> login(@RequestBody UserRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutService.logout(request, response, authentication);
    }

    @PostMapping({"forgot_password"})
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) throws Exception {
        return authService.forgotPassword(request);
    }

    @PostMapping({"/verify_otp"})
    public ResponseEntity<?> verifyOTP(@RequestBody OtpRequest request) {
        return authService.verifyOTP(request);
    }

    @PostMapping({"/resend_otp"})
    public ResponseEntity<?> resendOTP(@RequestBody OtpRequest request) throws MessagingException, UnsupportedEncodingException {
        return authService.resendOTP(request);
    }

    @PostMapping({"/new_password"})
    public ResponseEntity<?> newPassword(@RequestBody UserRequest request) {
        return authService.newPassword(request);
    }


}
