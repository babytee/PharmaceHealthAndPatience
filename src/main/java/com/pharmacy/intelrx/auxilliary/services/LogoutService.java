//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.pharmacy.intelrx.auxilliary.services;

import com.pharmacy.intelrx.auxilliary.models.Token;
import com.pharmacy.intelrx.auxilliary.repositories.TokenRepository;
import com.pharmacy.intelrx.pharmacy.services.AuthService;
import com.pharmacy.intelrx.utility.UserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LogoutService implements LogoutHandler {
    private final TokenRepository tokenRepository;
    private final UserStatusService userStatusService;

    public LogoutService(final TokenRepository tokenRepository, UserStatusService userStatusService) {
        this.tokenRepository = tokenRepository;
        this.userStatusService = userStatusService;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        ResponseEntity<String> logoutResponse = this.performLogout(request);
        response.setStatus(logoutResponse.getStatusCodeValue());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        SecurityContextHolder.clearContext();

        try {
            response.getWriter().write((String) logoutResponse.getBody());
        } catch (IOException var6) {
            var6.printStackTrace();
        }

    }

    public ResponseEntity<String> performLogout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
//            Token storedToken = tokenRepository.findByToken(jwtToken).orElse(null);

            Token storedToken = tokenRepository.findByTokenAndExpiredOrRevoked(
                    jwtToken, false, false).orElse(null);

            if (storedToken != null) {
                tokenRepository.delete(storedToken);
            }

            var email = request.getUserPrincipal().getName();
            //set user status on logged out to make it offline
            userStatusService.userLoggedOut(email);

            String successMessage = "Logout successful";
            return ResponseEntity.ok().body(successMessage);
        } else {
            String errorMessage = "Invalid token. Please provide a valid Bearer token.";
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }
}
