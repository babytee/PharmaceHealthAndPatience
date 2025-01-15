package com.pharmacy.intelrx.admin.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.intelrx.config.JwtService;
import com.pharmacy.intelrx.auxilliary.dto.AuthenticationResponse;
import com.pharmacy.intelrx.auxilliary.dto.CustomSingleResponse;
import com.pharmacy.intelrx.auxilliary.dto.RegisterRequest;
import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.auxilliary.models.Role;
import com.pharmacy.intelrx.auxilliary.models.Token;
import com.pharmacy.intelrx.auxilliary.models.TokenType;
import com.pharmacy.intelrx.admin.repositories.AdminRepository;
import com.pharmacy.intelrx.auxilliary.repositories.TokenRepository;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service("AdminAuthenticationService")
public class AuthenticationService {
    private final Utility utility;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdminRepository repository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<?> Register(RegisterRequest request) {
        String email = request.getEmail();
        if (utility.isNullOrEmpty(request.getFirstname())) {
            return ResponseEntity.ok().body(new RegisterRequest("failed", "firstname is required"));
        } else if (utility.isNullOrEmpty(request.getLastname())) {
            return ResponseEntity.ok().body(new RegisterRequest("failed", "lastname is required"));
        }else if (utility.isNullOrEmpty(request.getEmail())) {
            return ResponseEntity.ok().body(new RegisterRequest("failed", "email is required"));
        } else if (utility.isNullOrEmpty(request.getPhoneNumber())) {
            return ResponseEntity.ok().body(new RegisterRequest("failed", "phoneNumber is required"));
        } else if (utility.isNullOrEmpty(request.getPassword())) {
            return ResponseEntity.ok().body(new RegisterRequest("failed", "password is required"));
        } else if (utility.isNullOrEmpty(request.getConfirm_password())) {
            return ResponseEntity.ok().body(new RegisterRequest("failed", "confirm_password is required"));
        } else if (this.repository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new RegisterRequest("failed", "Email already exist"));
        } else {

            Optional<Admin> userOpt = this.repository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                return ResponseEntity.ok().body(new RegisterRequest("failed", "Admin Already Exist"));
            }


            boolean check_password = request.getPassword().equals(request.getConfirm_password());

            if (!check_password) {
                return ResponseEntity.ok().body(new RegisterRequest(String.valueOf("failed"), "Password not match"));
            }

            Admin admin = Admin.builder()
                    .firstname(request.getFirstname())
                    .lastname(request.getLastname())
                    .email(request.getEmail())
                    .password(this.passwordEncoder.encode(request.getPassword()))
                    .role(Role.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now()).build();
            repository.save(admin);

            CustomSingleResponse<RegisterRequest> response = new CustomSingleResponse<>();
            response.setStatus("success");
            response.setMessage("Registration is successfully ");
            return ResponseEntity.ok(response);

        }
    }

    public ResponseEntity<?> login(RegisterRequest request) {
        if ((request.getEmail().isEmpty() || request.getEmail() == null) && request.getEmail().equalsIgnoreCase("String")) {
            return ResponseEntity.ok().body(new RegisterRequest("failed", "Email is required"));
        } else {
            Optional<Admin> userOpt = this.repository.findByEmail(request.getEmail());
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok().body(new RegisterRequest("failed", "User Not Found"));
            } else {
                Admin user = userOpt.get();
                boolean check_password = this.passwordEncoder.matches(request.getPassword(), user.getPassword());

                if (!check_password) {
                    return ResponseEntity.ok().body(new RegisterRequest(String.valueOf("failed"), "Password not match"));
                } else {
                    try {
                        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
                        String jwtToken = this.jwtService.generateToken(user);
                        String refreshToken = this.jwtService.generateRefreshToken(user);

                        this.revokeAllAdminTokens(user);
                        this.saveAdminToken(user, jwtToken);

                        AuthenticationResponse response = new AuthenticationResponse();

                        RegisterRequest registerRequest = new RegisterRequest();
                        registerRequest.setFirstname(user.getFirstname());
                        registerRequest.setLastname(user.getLastname());
                        registerRequest.setEmail(user.getEmail());

                        response.setStatus("success");
                        response.setDescription("Login is successfully");
                        response.setData(registerRequest);
                        response.setAccessToken(jwtToken);
                        response.setRefreshToken(refreshToken);

                        return ResponseEntity.ok(response);
                    } catch (AuthenticationException e) {
                        // Log the authentication error
                        //logger.error("Authentication failed: " + e.getMessage(), e);
                        // Handle the error and return an appropriate response
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RegisterRequest("failed", "Authentication failed" + e.getMessage()));
                    }

                }

            }
        }
    }

    private void revokeAllAdminTokens(Admin admin) {
        List<Token> validAdminTokens = this.tokenRepository.findAllValidTokensByAdmin(admin.getId());
        if (!validAdminTokens.isEmpty()) {
            validAdminTokens.forEach((token) -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            this.tokenRepository.saveAll(validAdminTokens);
        }
    }

    private void saveAdminToken(Admin admin, String jwtToken) {
        Token token = Token.builder().admin(admin).token(jwtToken).tokenType(TokenType.BEARER).revoked(false).expired(false).build();
        this.tokenRepository.save(token);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String userEmail = this.jwtService.extractUsername(refreshToken);
            if (userEmail != null) {
                Admin user = (Admin) this.repository.findByEmail(userEmail).orElseThrow();
                if (this.jwtService.isTokenValid(refreshToken, user)) {
                    this.revokeAllAdminTokens(user);
                    String accessToken = this.jwtService.generateToken(user);
                    this.saveAdminToken(user, accessToken);
                    AuthenticationResponse authResponse = AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
                    (new ObjectMapper()).writeValue(response.getOutputStream(), authResponse);
                }
            }

        }
    }

}
