package com.pharmacy.intelrx.pharmacy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.intelrx.auxilliary.dto.*;
import com.pharmacy.intelrx.auxilliary.models.*;
import com.pharmacy.intelrx.auxilliary.repositories.TokenRepository;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.UserStatusService;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.config.JwtService;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.dto.employee.JobInformationRequest;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.PharmacistCertification;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.SecurityQuestionsAnswer;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.SecurityQuestion;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import com.pharmacy.intelrx.pharmacy.repositories.ContactInfoRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacistCertificationRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SecurityQuestionsAnswerRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.SecurityQuestionRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyWalletConfig;
import com.pharmacy.intelrx.utility.EmailEncryptionUtil;
import com.pharmacy.intelrx.utility.EmailService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service("PharmacyAuthService")
public class AuthService {
    private final Utility utility;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PharmacistCertificationRepository certificationRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PharmacyRepository pharmacyRepository;
    private final EmployeeRepository employeeRepository;
    private final UserStatusService userStatusService;
    private final EmailEncryptionUtil emailEncryptionUtil;
    private final EmailService emailService;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final SecurityQuestionsAnswerRepository securityQuestionsAnswerRepository;
    private final PharmacyWalletConfig pharmacyWalletConfig;

    @Value("${spring.intelrx.url}")
    private String baseUrl;

    @Transactional
    public ResponseEntity<?> Register(PharmacyAuthRequest request) throws Exception {

        //Pharmacist Registering as the owner or on behalf of owner
        UserRequest userRequest = request.getUserRequest();
        ResponseEntity<?> userRequestResEntity = userRequest(userRequest);

        if (userRequestResEntity.getStatusCode() != HttpStatus.OK) {
            return userRequest(userRequest);
        }

        //pharmacist registration Certification Request
//        PharmacistCertificationReqRes certificationReqRes = request.getPharmacistCertificationReqRes();
//        ResponseEntity<?> certificationReqResResEntity = PharmacistCertificationReqRes(certificationReqRes);
//
//        if (certificationReqResResEntity.getStatusCode() != HttpStatus.OK) {
//            return PharmacistCertificationReqRes(certificationReqRes);
//        }

        //Contact Request
//        ContactInfoReqRes contactInfoReqRes = request.getContactInfoReqRes();
//        ResponseEntity<?> contactInfoReqResResEntity = ContactInfoReqRes(contactInfoReqRes);
//
//        if (contactInfoReqResResEntity.getStatusCode() != HttpStatus.OK) {
//            return ContactInfoReqRes(contactInfoReqRes);
//        }

        //Check the password if match
        boolean check_password = userRequest.getPassword().equals(userRequest.getConfirmPassword());

        if (!check_password) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Password not match"));
        }

        //store pharmacist owner basic information for registration
        User user = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .phoneNumber(userRequest.getPhoneNumber())
                .email(userRequest.getEmail())
                .password(this.passwordEncoder.encode(userRequest.getPassword()))
                .userType(UserType.OWNER)
                .role(Role.USER)
                .status(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        var getUser = userRepository.save(user);

        //store the Pharmacist Certification information
        PharmacistCertification certification = PharmacistCertification.builder()
//                .certificateNumber(certificationReqRes.getCertificateNumber())
//                .certificateStatus(certificationReqRes.getCertificateStatus())
                .user(user)
                .build();
        certificationRepository.save(certification);

//        String address = contactInfoReqRes.getStreetAddress();
//        String defaultValue = null; // Replace with your desired default value

        // Check if the address is empty (null or empty string) and replace it
//        String processedAddress = (address == null || address.isEmpty()) ? defaultValue : address;

        //store the pharmacist contact address details
        ContactInfo contactInfo = ContactInfo.builder()
//                .country(contactInfoReqRes.getCountry())
//                .state(contactInfoReqRes.getState())
//                .city(contactInfoReqRes.getCity())
//                .lga(contactInfoReqRes.getLga())
//                .streetAddress(processedAddress)
                .user(user)
                .build();
        contactInfoRepository.save(contactInfo);

        //generate intelrx uniqueId and store
        String intelRxId = "intelRx_" + utility.generateEmployeeIntelRxId();
        Pharmacy pharmacy = Pharmacy.builder()
                .intelRxId(intelRxId)
                .user(user)
                .build();
        pharmacyRepository.save(pharmacy);

        //creating new pharmacy wallet
        pharmacyWalletConfig.createWallet(intelRxId);

        //an email method that will send email verification message to the pharmacist
        // Encrypt the email

        String encryptedEmail = emailEncryptionUtil.encrypt(getUser);

        String subject = "Pharmacy Verification";
        String body = "This is your verification link in case the button is not working: " + baseUrl + "verify_email/" + encryptedEmail;
        String link = baseUrl + "verify_email/" + encryptedEmail;

        //this.utility.sendEmail(userRequest.getEmail(), subject, body);

        emailService.regVerificationEmail(user, subject, link, body);

        String obfuscateEmail = utility.obfuscateEmail(userRequest.getEmail());
        //success message on a successful account registration
        return ResponseEntity.ok().body(StandardResponse.success("Click the link on the confirmation email we have sent to " + obfuscateEmail + " to verify your account."));

    }

    public ResponseEntity<?> verifyEmail(String emailEncrypted) {
        try {
            //decrypt to activate
            String decryptedEmail = emailEncryptionUtil.decrypt(emailEncrypted);
            if (utility.isNullOrEmpty(decryptedEmail)) {
                return ResponseEntity.badRequest().body(StandardResponse.error("email is required"));
            }

            if (decryptedEmail.equals("activated") || decryptedEmail == "activated") {
                return ResponseEntity.ok().body(StandardResponse.success("This account has already been activated. Please <a href='https://inventory.intelrx.io/'>log in here</a> "));
            }

            Optional<User> userOptional = userRepository.findByEmail(decryptedEmail);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("email not found for this account " + decryptedEmail));
            }


            emailService.verificationWelcomeEmail(userOptional.get());


            return ResponseEntity.ok().body(StandardResponse.success("We have verified your email " + decryptedEmail + ". your account is now active. Please <a href='https://inventory.intelrx.io/'>log in here</a> "));
        } catch (Exception e) {
            // Handle exceptions or log them
            return ResponseEntity.status(500).body(StandardResponse.error("Internal Server Error " + e.getMessage()));
        }
    }

    public ResponseEntity<?> updateTwoFactorAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }

        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("email not found for this account"));
        }
        User user = userOptional.get();
        String msg = "";
        if (user.isTwoFactorAuth()) {
            user.setTwoFactorAuth(false);
            msg = "Two Factor Authentication Disabled";
        } else {
            user.setTwoFactorAuth(true);
            msg = "Two Factor Authentication Enabled";
        }

        userRepository.save(user);

        return ResponseEntity.ok().body(new CustomResponse("success", msg));

    }

    public ResponseEntity<?> userRequest(UserRequest request) {
        Optional<User> optionalUserEmail = userRepository.findByEmail(request.getEmail());
        Optional<User> optionalUserPhoneNumber = userRepository.findByEmail(request.getEmail());

        if (optionalUserEmail.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy with this email account already exist is required"));
        } else if (optionalUserPhoneNumber.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy with this phone number account already exist is required"));
        } else if (utility.isNullOrEmpty(request.getFirstName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("firstName is required"));
        } else if (utility.isNullOrEmpty(request.getLastName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("lastName is required"));
        } else if (utility.isNullOrEmpty(request.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("phoneNumber is required"));
        } else if (utility.isNullOrEmpty(request.getEmail())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("email is required"));
        } else if (utility.isNullOrEmpty(request.getPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("password is required"));
        } else if (utility.isNullOrEmpty(request.getPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("confirmPassword is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("User request is valid"));
        }
    }

    public ResponseEntity<?> ContactInfoReqRes(ContactInfoReqRes request) {
        if (utility.isNullOrEmpty(request.getCountry())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("country is required"));
        } else if (utility.isNullOrEmpty(request.getState())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("state is required"));
        } else if (utility.isNullOrEmpty(request.getCity())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("city is required"));
        } else if (utility.isNullOrEmpty(request.getLga())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("lga is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("User request is valid"));
        }
    }

    public ResponseEntity<?> PharmacistCertificationReqRes(PharmacistCertificationReqRes request) {
        Optional<PharmacistCertification> optional = certificationRepository.findByCertificateNumber(request.getCertificateNumber());
        if (optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("certificateNumber already exist and it belong to another pharmacy"));
        } else if (utility.isNullOrEmpty(request.getCertificateNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("certificateNumber is required"));
        } else if (utility.isNullOrEmpty(request.getCertificateNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("certificateNumber() is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("User request is valid"));
        }
    }

    public ResponseEntity<?> login(UserRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Email is required"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("User Not Found"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Password not match"));
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

            CustomSingleResponse<BlogResponse> response = new CustomSingleResponse<>();
            AuthenticationResponse authResponse = new AuthenticationResponse();
            UserRequest registerRequest = new UserRequest();

            Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());
            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByUserId(user.getId());

            Employee employee = optionalEmployee.orElse(null);
            Pharmacy pharmacy = optionalPharmacy.orElse(null);
            String intelRxId = "";

            User checkUser = null;
            String pharmacistCategory = null;
            if (employee != null) {
                intelRxId = employee.getEmployeeIntelRxId();
                checkUser = employee.getUser();
                Pharmacy pharm = pharmacyRepository.findByIntelRxId(intelRxId).orElse(null);
                if (pharm != null) {
                    pharmacistCategory = pharm.getPharmacistCategory();
                }

            } else if (pharmacy != null) {
                intelRxId = pharmacy.getIntelRxId();
                checkUser = pharmacy.getUser();
                pharmacistCategory = pharmacy.getPharmacistCategory();
            }

            String userStatus = checkUser.getUserStatus();
            if ("DELETED".equals(userStatus)) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Your account has been deleted"));
            }
            if ("TERMINATED".equals(userStatus)) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Your account has been terminated"));
            }

            registerRequest.setId(user.getId());
            registerRequest.setFirstName(user.getFirstName());
            registerRequest.setLastName(user.getLastName());
            registerRequest.setEmail(user.getEmail());
            registerRequest.setIntelRxId(intelRxId);
            registerRequest.setPharmacistCategory(pharmacistCategory);
            registerRequest.setUserType(user.getUserType());

            // Check if user has 2FA set
            if (user.isTwoFactorAuth()) {
                String otp = utility.generateOTP(email);
                String subject = "OTP Authentication";
                String body = "This is your OTP CODE for the 2FA Authentication: " + otp;
                emailService.otpAuthEmail(email, subject, body);

                response.setStatus("success");
                response.setMessage("An OTP has been sent to your email. Your OTP is: " + body);
                response.setData(authResponse);
                return ResponseEntity.ok(response);
            }

            JobInformationRequest jobInformationRequest = new JobInformationRequest();
            if (user.getUserType() == UserType.EMPLOYEE && employee != null) {
                JobInformation jobInformation = employee.getJobInformation();
                if (jobInformation != null) {
                    JobTitle jobTitle = jobInformation.getJobTitle();
                    if (jobTitle != null) {
                        jobInformationRequest.setEmployeeId(employee.getId());
                        jobInformationRequest.setId(jobTitle.getId());
                        jobInformationRequest.setJobTitle(jobTitle.getName());
                    }
                }
            }

            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);

            authResponse.setAccessToken(jwtToken);
            authResponse.setRefreshToken(refreshToken);
            authResponse.setUserDetails(registerRequest);
            authResponse.setJobInfo(jobInformationRequest);

            response.setStatus("success");
            response.setMessage("Login Successfully");
            response.setData(authResponse);

            userStatusService.userLoggedIn(email);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Authentication failed: " + e.getMessage()));
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) throws Exception {
        if (utility.isNullOrEmpty(request.getEmail())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("email is required"));
        } else if (request.getQuestionId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("questionId is required"));
        } else if (utility.isNullOrEmpty(request.getAnswer())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("answer is required"));
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Email Address Not Found"));
        }

        User getUser = userOpt.get();

        Optional<SecurityQuestion> optionalSecurityQuestionOne = securityQuestionRepository.findById(request.getQuestionId());
        if (!optionalSecurityQuestionOne.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("securityQuestion Not Found"));
        }

        SecurityQuestion securityQuestionOne = optionalSecurityQuestionOne.get();

        Optional<SecurityQuestionsAnswer> optionalSecurityQuestions = securityQuestionsAnswerRepository.
                findBySecurityQuestionOneAndUser(
                        securityQuestionOne, getUser
                );

        if (!optionalSecurityQuestions.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("This is not your question or you have not set any question"));
        }

        Optional<SecurityQuestionsAnswer> optional = securityQuestionsAnswerRepository.
                findBySecurityQuestionOneAndAnswerOneAndUser(
                        securityQuestionOne, request.getAnswer(), getUser
                );

        if (!optional.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("Your answer is not corrected"));
        }

        SecurityQuestionsAnswer questionsAnswer = optional.get();

        //an email method that will send email verification message to the pharmacist
        // Encrypt the email
        String encryptedEmail = emailEncryptionUtil.encrypt(getUser);

        String subject = "Forgot Password Email Verification";
        String body = "This is your reset link in case the button is not working: " + baseUrl + "forgot-password/"
                + encryptedEmail;

        String link = baseUrl + "forgot-password/" + encryptedEmail;

        emailService.resetPasswordEmail(getUser, subject, link, body);

        String obfuscateEmail = utility.obfuscateEmail(request.getEmail());
        //success message on a successful account registration
        return ResponseEntity.ok().body(StandardResponse.success("We have sent a reset link to " + obfuscateEmail));


    }

    public ResponseEntity<?> verifyOTP(OtpRequest request) {
        if (this.utility.isNullOrEmpty(request.getEmail())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Email is required"));
        } else if (this.utility.isNullOrEmpty(request.getOtp())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("OTP is required"));
        } else {
            boolean check_email = userRepository.existsByEmail(request.getEmail());
            if (!check_email) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Email address does not exist or incorrect"));
            } else {
                String otp = request.getOtp();
                boolean isValidOTP = this.utility.validateOTP(request.getEmail(), otp);
                if (isValidOTP) {
                    Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
                    String otps = String.valueOf('0');
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        user.setOtp(otps);
                        userRepository.save(user);
                        utility.markEmailAsVerified(request.getEmail());
                        String jwtToken = this.jwtService.generateToken(user);
                        String refreshToken = this.jwtService.generateRefreshToken(user);
                        this.revokeAllUserTokens(user);
                        this.saveUserToken(user, jwtToken);

                        CustomSingleResponse<BlogResponse> response = new CustomSingleResponse<>();

                        AuthenticationResponse authResponse = new AuthenticationResponse();

                        Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());
                        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByUserId(user.getId());

                        Employee employee = null;
                        Pharmacy pharmacy = null;
                        String intelRxId = "";
                        if (optionalEmployee.isPresent()) {
                            employee = optionalEmployee.get();
                            intelRxId = employee.getEmployeeIntelRxId();
                        }

                        if (optionalPharmacy.isPresent()) {
                            pharmacy = optionalPharmacy.get();
                            intelRxId = pharmacy.getIntelRxId();
                        }

                        UserRequest registerRequest = new UserRequest();
                        registerRequest.setId(user.getId());
                        registerRequest.setFirstName(user.getFirstName());
                        registerRequest.setLastName(user.getLastName());
                        registerRequest.setEmail(user.getEmail());
                        registerRequest.setIntelRxId(intelRxId);
                        registerRequest.setUserType(user.getUserType());

                        JobInformationRequest jobInformationRequest = new JobInformationRequest();
                        //check the user type if THE OWNER OR EMPLOYEE
                        if (user.getUserType().equals("EMPLOYEE")) {
                            jobInformationRequest.setId(employee.getJobInformation().getJobTitle().getId());
                            jobInformationRequest.setJobTitle(employee.getJobInformation().getJobTitle().getName());
                        }

                        authResponse.setUserDetails(registerRequest);
                        authResponse.setJobInfo(jobInformationRequest);
                        authResponse.setAccessToken(jwtToken);
                        authResponse.setRefreshToken(refreshToken);

                        response.setStatus("success");
                        response.setMessage("Email Is Verified");
                        response.setData(authResponse);

                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.badRequest().body(StandardResponse.error("User Not Found"));
                    }
                } else {
                    return ResponseEntity.badRequest().body(StandardResponse.error("OTP Not Found or Expired " + isValidOTP));
                }
            }
        }
    }

    public ResponseEntity<?> resendOTP(OtpRequest request) throws MessagingException, UnsupportedEncodingException {
        if (this.utility.isNullOrEmpty(request.getEmail())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Email is required"));
        } else {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                String otp = this.utility.generateOTP(request.getEmail());
                String subject = "IntelRx OTP Authentication";
                String body = "This is your new OTP CODE:" + otp;
                //this.utility.sendEmail(request.getEmail(), subject, body);
                emailService.otpAuthEmail(request.getEmail(), subject, body);
                return ResponseEntity.ok().body(StandardResponse.success("New OTP has been sent to your email"));
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("User Not Found"));
            }
        }
    }

    public ResponseEntity<?> newPassword(UserRequest request) {
        if (this.utility.isNullOrEmpty(request.getEmail())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("email is required"));
        } else if (this.utility.isNullOrEmpty(request.getPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("password is required"));
        } else if (this.utility.isNullOrEmpty(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("confirmPassword is required"));
        } else if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("password does not match"));
        } else {
            //decrypt email to activate
            String decryptedEmail = emailEncryptionUtil.decrypt(request.getEmail());

            if (decryptedEmail.equals("activated") || decryptedEmail == "activated") {
                return ResponseEntity.ok().body(StandardResponse.success("Reset Link Expired"));
            }

            //var user =
            Optional<User> userOpt = userRepository.findByEmail(decryptedEmail);

            if (userOpt.isPresent()) {

                User user = userOpt.get();

                if (!user.getEmail().equals(decryptedEmail)) {
                    return ResponseEntity.ok().body(StandardResponse.error
                            ("This email account is not found"));
                }

                user.setStatus(true);
                user.setPassword(this.passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);

                return ResponseEntity.ok().body(StandardResponse.success
                        ("Password has been successfully created"));
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error
                        ("User not found"));
            }
        }

    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false).expired(false).build();
        this.tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = this.tokenRepository.findAllValidTokensByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach((token) -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            this.tokenRepository.saveAll(validUserTokens);
        }
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String userEmail = this.jwtService.extractUsername(refreshToken);
            if (userEmail != null) {
                User user = this.userRepository.findByEmail(userEmail).orElseThrow();
                if (this.jwtService.isTokenValid(refreshToken, user)) {
                    this.revokeAllUserTokens(user);
                    String accessToken = this.jwtService.generateToken(user);
                    this.saveUserToken(user, accessToken);
                    AuthenticationResponse authResponse = AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
                    (new ObjectMapper()).writeValue(response.getOutputStream(), authResponse);
                }
            }
        }
    }

    public void checkTokenExpiration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String userEmail = this.jwtService.extractUsername(refreshToken);
            if (userEmail != null) {
                User user = this.userRepository.findByEmail(userEmail).orElseThrow();
                if (this.jwtService.isTokenValid(refreshToken, user)) {
                    this.revokeAllUserTokens(user);
                    String accessToken = this.jwtService.generateToken(user);
                    this.saveUserToken(user, accessToken);
                    AuthenticationResponse authResponse = AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
                    (new ObjectMapper()).writeValue(response.getOutputStream(), authResponse);
                }
            }
        }
    }


}
