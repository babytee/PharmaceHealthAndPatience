package com.pharmacy.intelrx.PCNAPICrawling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pharmacy.intelrx.auxilliary.models.Role;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyAuthRequest;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyRequest;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.PharmacistCertification;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.repositories.ContactInfoRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacistCertificationRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.services.AuthService;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyWalletConfig;
import com.pharmacy.intelrx.utility.EmailEncryptionUtil;
import com.pharmacy.intelrx.utility.EmailService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service("PharmacyPremisesServices")
public class PharmacyPremisesServices {
    private final RestTemplate restTemplate;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final PharmacistCertificationRepository certificationRepository;
    private final Utility utility;
    private final EmailService emailService;
    private final EmailEncryptionUtil emailEncryptionUtil;
    private final PharmacyWalletConfig pharmacyWalletConfig;

    @Value("${spring.intelrx.url}")
    private String baseUrl;

    public ResponseEntity<?> getPremiseDetails(String premisesFilter) {
        String url = "https://pcncore.azurewebsites.net/PublicSearch/" +
                "Pharmacist_PremisesDetails?premisesFilter=" + premisesFilter;

        // Step 1: Get the raw JSON response as a String
        String rawJsonResponse = restTemplate.getForObject(url, String.class);
        System.out.println("Raw JSON Response: " + rawJsonResponse);

        // Step 2: Create an ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Step 3: Parse the JSON string into a JsonNode
            JsonNode rootNode = objectMapper.readTree(rawJsonResponse);

            // Step 4: Extract fields manually
            JsonNode dataNode = rootNode.get("Data");
            int total = rootNode.get("Total").asInt();
            JsonNode aggregateResults = rootNode.get("AggregateResults");
            JsonNode errors = rootNode.get("Errors");

            // Step 5: Create a DTO for each item in the Data array
            List<PremisesDataDTO> premisesDataList = new ArrayList<>();
            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    PremisesDataDTO premisesData = PremisesDataDTO.builder()
                            .firstName(node.get("FirstName").asText())
                            .middleName(node.get("MiddleName").asText())
                            .lastName(node.get("LastName").asText())
                            .premisesName(node.get("PremisesName").asText())
                            .premisesAddress(node.get("PremisesAddress").asText())
                            .dateApproved(LocalDateTime.parse(node.get("DateApproved").asText()))
                            .yearLicenced(node.get("YearLicenced").asText())
                            .premisesState(node.get("PremisesState").asText())
                            .certificateNo(node.get("CertificateNo").asText())
                            .category(node.get("Category").asText())
                            .isLicencePrinted(node.get("IsLicencePrinted").asBoolean())
                            .datePrinted(node.get("DatePrinted").asText())
                            .pharmacist(node.get("Pharmacist").asText())
                            .premisesId(node.get("PremisesId").asText())
                            .pharmacistId(node.get("PharmacistId").asText())
                            .build();

                    premisesDataList.add(premisesData);
                }
            }


            return ResponseEntity.ok(premisesDataList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Error processing the response"));
        }
    }

    public ResponseEntity<?> getSinglePremiseDetails(String PremisesId) {
        String url = "https://pcncore.azurewebsites.net/PublicSearch/" +
                "Pharmacist_PremisesDetailHistory/" + PremisesId;

        // Step 1: Get the raw JSON response as a String
        String rawJsonResponse = restTemplate.getForObject(url, String.class);
        System.out.println("Raw JSON Response: " + rawJsonResponse);

        // Step 2: Create an ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Step 3: Parse the JSON string into a JsonNode
            JsonNode rootNode = objectMapper.readTree(rawJsonResponse);

            // Step 4: Extract fields manually
            JsonNode dataNode = rootNode.get("Data");

            // Step 5: Create a DTO for each item in the Data array
            List<PremisesDataDTO> premisesDataList = new ArrayList<>();
            if (dataNode.isArray()) {

                //for (JsonNode node : dataNode) {
                JsonNode node = dataNode.get(0);
                PremisesDataDTO premisesData = PremisesDataDTO.builder()
                        .firstName(node.get("FirstName").asText())
                        .middleName(node.get("MiddleName").asText())
                        .lastName(node.get("LastName").asText())
                        .premisesName(node.get("PremisesName").asText())
                        .premisesAddress(node.get("PremisesAddress").asText())
                        .dateApproved(LocalDateTime.parse(node.get("DateApproved").asText()))
                        .yearLicenced(node.get("YearLicenced").asText())
                        .premisesState(node.get("PremisesState").asText())
                        .certificateNo(node.get("CertificateNo").asText())
                        .category(node.get("Category").asText())
                        .isLicencePrinted(node.get("IsLicencePrinted").asBoolean())
                        .datePrinted(node.get("DatePrinted").asText())
                        .pharmacist(node.get("Pharmacist").asText())
                        .premisesId(node.get("PremisesId").asText())
                        .pharmacistId(node.get("PharmacistId").asText())
                        .build();

                premisesDataList.add(premisesData);
                //}
            }


            return ResponseEntity.ok(premisesDataList.get(0));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Error processing the response"));
        }
    }

    public ResponseEntity<?> submitPharmacyPremise(PharmacyPremiseRequest premiseRequest) throws Exception {
        ResponseEntity<?> responseEntity = getSinglePremiseDetails(premiseRequest.getPremisesId());

        // Initialize ObjectMapper and register JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Optionally, to write dates as ISO-8601

        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
            // Serialize the response body to a JSON string
            String jsonString = objectMapper.writeValueAsString(responseEntity.getBody());
            JsonNode node = objectMapper.readTree(jsonString);

            // Build the DTO
            PremisesDataDTO premisesData = PremisesDataDTO.builder()
                    .firstName(node.get("FirstName").asText())
                    .middleName(node.get("MiddleName").asText(null))
                    .lastName(node.get("LastName").asText(null))
                    .premisesName(node.get("PremisesName").asText())
                    .premisesAddress(node.get("PremisesAddress").asText(null))
                    .dateApproved(LocalDateTime.parse(node.get("DateApproved").asText()))  // Ensure date parsing is correct
                    .yearLicenced(node.get("YearLicenced").asText(null))
                    .premisesState(node.get("PremisesState").asText(null))
                    .certificateNo(node.get("CertificateNo").asText(null))
                    .category(node.get("Category").asText(null))
                    .isLicencePrinted(node.get("IsLicencePrinted").asBoolean(false))
                    .datePrinted(node.get("DatePrinted").asText(null))
                    .pharmacist(node.get("Pharmacist").asText(null))
                    .premisesId(node.get("PremisesId").asText())
                    .pharmacistId(node.get("PharmacistId").asText(null))
                    .build();


            UserRequest userRequest = new UserRequest();
            userRequest.setFirstName(premisesData.getFirstName());
            userRequest.setLastName(premisesData.getLastName());
            userRequest.setPhoneNumber(premiseRequest.getPhoneNumber());
            userRequest.setEmail(premiseRequest.getEmail());
            userRequest.setPassword(premiseRequest.getPassword());

            PharmacyAuthRequest pharmacyAuthRequest = new PharmacyAuthRequest();
            pharmacyAuthRequest.setUserRequest(userRequest);

            //boolean checkUser = userDetailsService.verifyIfPharmacyInfoExist(premisesData, userRequest);

            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByPremisesId(premisesData.getPremisesId());

            if (optionalPharmacy.isPresent()) {
                return ResponseEntity.ok(StandardResponse.error("Pharmacy with this PremisesId already exist"));
            }

            Optional<User> optionalUser = userRepository.findByEmail(premiseRequest.getEmail());
            if (optionalUser.isPresent()) {
                return ResponseEntity.ok(StandardResponse.error("Pharmacy with this email address already exist"));
            }

            User user = addPharmacy(pharmacyAuthRequest);

            ContactInfoReqRes contactInfoReqRes = new ContactInfoReqRes();
            contactInfoReqRes.setCountry("Nigeria");
            contactInfoReqRes.setState(premisesData.getPremisesState());
            contactInfoReqRes.setStreetAddress(premisesData.getPremisesAddress());

            PharmacyRequest pharmacyRequest = new PharmacyRequest();
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
            pharmacyRequest.setPremisesId(premisesData.getPremisesId());
            pharmacyRequest.setPharmacyOwner(premisesData.getPharmacist());
            pharmacyRequest.setPharmacyName(premisesData.getPremisesName());
            pharmacyRequest.setPremiseNumber(premisesData.getCertificateNo());
            pharmacyRequest.setPharmacistCategory(premiseRequest.getPharmacistCategory());
            verifyPharmacy(pharmacyRequest, user);

            return ResponseEntity.ok(StandardResponse.success("Verification link sent to your email", premisesData));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
    }

    @Transactional
    public ResponseEntity<?> submitPharmacyPremiseManually(PharmacyPremiseRequest premiseRequest) throws Exception {
        ResponseEntity<?> responseEntity = pharmacyPremiseRequest(premiseRequest);

        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
            UserRequest userRequest = new UserRequest();
            userRequest.setFirstName(premiseRequest.getFirstName());
            userRequest.setLastName(premiseRequest.getLastName());
            userRequest.setPhoneNumber(premiseRequest.getPhoneNumber());
            userRequest.setEmail(premiseRequest.getEmail());
            userRequest.setPassword(premiseRequest.getPassword());

            PharmacyAuthRequest pharmacyAuthRequest = new PharmacyAuthRequest();
            pharmacyAuthRequest.setUserRequest(userRequest);

            User user = addPharmacy(pharmacyAuthRequest);

            ContactInfoReqRes contactInfoReqRes = new ContactInfoReqRes();
            contactInfoReqRes.setCountry("Nigeria");
            contactInfoReqRes.setState(premiseRequest.getPremisesState());
            contactInfoReqRes.setStreetAddress(premiseRequest.getPremisesAddress());

            PharmacyRequest pharmacyRequest = new PharmacyRequest();
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
            pharmacyRequest.setPharmacyOwner(premiseRequest.getPharmacist());
            pharmacyRequest.setPharmacyName(premiseRequest.getPremisesName());
            pharmacyRequest.setPremiseNumber(premiseRequest.getCertificateNo());
            pharmacyRequest.setPharmacistCategory(premiseRequest.getPharmacistCategory());

            verifyPharmacy(pharmacyRequest, user);

            return ResponseEntity.ok(StandardResponse.success("Verification link sent to your email", premiseRequest));
        }
        return responseEntity;
    }

    @Transactional
    public User addPharmacy(PharmacyAuthRequest request) throws Exception {
        //Pharmacist Registering as the owner or on behalf of owner
        UserRequest userRequest = request.getUserRequest();

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
                .user(user)
                .build();
        certificationRepository.save(certification);

        //store the pharmacist contact address details
        ContactInfo contactInfo = ContactInfo.builder()
                .user(user)
                .build();
        contactInfoRepository.save(contactInfo);

        //generate intelrx uniqueId and store
        String intelRxId = "intelRx_" + utility.generateEmployeeIntelRxId();
        Pharmacy pharmacy = Pharmacy.builder()
                .intelRxId(intelRxId)
                .user(user)
                .createdAt(LocalDateTime.now())
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

        emailService.regVerificationEmail(user, subject, link, body);

        String obfuscateEmail = utility.obfuscateEmail(userRequest.getEmail());
        //success message on a successful account registration
        return user;

    }

    @Transactional
    public void verifyPharmacy(PharmacyRequest request, User user) {
        //Pharmacy Contact Request
        ContactInfoReqRes contactInfoReqRes = request.getContactInfoReqRes();

        Optional<Pharmacy> pharmacyIntelRxId = pharmacyRepository.findByUserId(user.getId());

        Pharmacy pharmacy = pharmacyIntelRxId.get();

        pharmacy.setIntelRxId(pharmacy.getIntelRxId());
        pharmacy.setPharmacyName(request.getPharmacyName());
        pharmacy.setPharmacyOwner(request.getPharmacyOwner());
        pharmacy.setPharmacyNumber(request.getPhoneNumber());
        pharmacy.setPremiseNumber(request.getPremiseNumber());
        pharmacy.setUser(user);
        pharmacy.setPremisesId(request.getPremisesId());
        pharmacy.setPharmacistCategory(request.getPharmacistCategory());
        pharmacy.setCreatedAt(LocalDateTime.now());
        pharmacy.setUpdatedAt(LocalDateTime.now());

        var pharm = pharmacyRepository.save(pharmacy);

        //store the pharmacist contact address details
        ContactInfo contactInfo = ContactInfo.builder()
                .country(contactInfoReqRes.getCountry())
                .state(contactInfoReqRes.getState())
                .city(contactInfoReqRes.getCity())
                .lga(contactInfoReqRes.getLga())
                .streetAddress(contactInfoReqRes.getStreetAddress())
                .pharmacy(pharm)
                .build();
        contactInfoRepository.save(contactInfo);

    }

    public ResponseEntity<?> pharmacyPremiseRequest(PharmacyPremiseRequest request) {
        Optional<User> optionalUserEmail = userRepository.findByEmail(request.getEmail());
        Optional<User> optionalUserPhoneNumber = userRepository.findByPhoneNumber(request.getPhoneNumber());

        if (optionalUserEmail.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy with this email account already exist"));
        } else if (optionalUserPhoneNumber.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy with this phone number account already exist"));
        } else if (utility.isNullOrEmpty(request.getPremisesName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("premisesName is required"));
        } else if (utility.isNullOrEmpty(request.getPremisesAddress())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("premisesAddress is required"));
        } else if (utility.isNullOrEmpty(request.getPremisesState())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("premisesState is required"));
        }
//        else if (utility.isNullOrEmpty(request.getCertificateNo())) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("certificateNo is required"));
//        }
        else if (utility.isNullOrEmpty(request.getPharmacist())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("pharmacist is required"));
        } else if (utility.isNullOrEmpty(request.getPharmacistCategory())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("pharmacistCategory is required"));
        } else if (!request.getPharmacistCategory().equals("Wholesaler") &&
                !request.getPharmacistCategory().equals("Retailer")) {
            return ResponseEntity.badRequest().body(StandardResponse.error("pharmacistCategory " +
                    "should be Wholesaler or Retailer"));
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
        } else if (utility.isNullOrEmpty(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("confirmPassword is required"));
        } else if (!request.getPassword().equals(request.getConfirmPassword())
                && request.getPassword() != request.getConfirmPassword()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Password not match"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("User request is valid"));
        }
    }


}
