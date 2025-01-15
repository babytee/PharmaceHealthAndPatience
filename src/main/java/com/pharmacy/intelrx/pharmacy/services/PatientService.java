package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.SmsByTermii.TermiiSMSService;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.PatientValidation;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PatientService")
public class PatientService {
    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_EXPIRATION_MINUTES = 20;
    private final PatientRepository patientRepository;
    private final UserDetailsService userDetailsService;
    private final PatientValidation patientValidation;
    private final OrderRepository orderRepository;
    private final FilterInventory filterInventory;
    private final PharmacyMapping pharmacyMapping;
    private final CartItemRepository cartItemRepository;
    private final TermiiSMSService termiiSMSService;
    private final Utility utility;
    private final MedTestResultRepository medTestResultRepository;
    private final MedTestTypeRepository medTestTypeRepository;

    public ResponseEntity<?> addPatient(PatientRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!authentication.isAuthenticated()) {
                return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
            }
            ResponseEntity responseEntity = patientValidation.validatePatientRequest(request);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }

            var patient = addPatientToEntity(request, null);
            return ResponseEntity.ok(StandardResponse.success("Patient Added Successfully", patient));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Response("failed", e.getMessage()));
        }


    }

    public ResponseEntity<?> updatePatient(PatientRequest request, Long patientId) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            String intelRxId = userDetailsService.getIntelRxId();
            if (user == null) {
                return ResponseEntity.internalServerError().body(new Response("failed", "You are unauthorized"));
            }
            ResponseEntity responseEntity = patientValidation.validatePatientRequest(request);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }
            if (patientId == null) {
                return ResponseEntity.badRequest().body(new Response("failed", "patientId is required"));
            }

            Optional<Patient> patientOptional = patientRepository.findByIdAndIntelRxId(patientId, intelRxId);
            if (!patientOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new Response("failed", "Patient not found"));
            }

            var viewPatient = addPatientToEntity(request, patientId);
            return ResponseEntity.ok(StandardResponse.success("Patient Updated Successfully", viewPatient));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Response("failed", e.getMessage()));
        }


    }

    public Patient addPatientToEntity(PatientRequest request, Long patientId) {
        var user = userDetailsService.getAuthenticatedUser();
        var branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();
        Patient patient = null;

        if (patientId != null) {
            Optional<Patient> patientOptional = patientRepository.findByIdAndIntelRxId(patientId, intelRxId);
            patient = patientOptional.get();
            patient.setAddress(request.getAddress());
            patient.setDob(request.getDob());
            patient.setEmail(request.getEmail());
        } else {
            patient = new Patient();
        }

        patient.setName(request.getName());
        patient.setGender(request.getGender());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setIntelRxId(intelRxId);
        patient.setPharmacyBranch(branch);
        patient.setUser(user);
        patient.setCreatedAt(LocalDateTime.now());

        var savedPat = patientRepository.save(patient);

        return savedPat;
    }

    public Patient addUnknownPatientToEntity(String firstCartItemName) {
        var user = userDetailsService.getAuthenticatedUser();
        var branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();

        Patient patient = Patient.builder()
                .name("Unknown")
                .gender("Unknown")
                .phoneNumber("Unknown")
                .intelRxId(intelRxId)
                .pharmacyBranch(branch)
                .user(user)
                .build();

        var savedPat = patientRepository.save(patient);
        savedPat.setName("Unknown-" + firstCartItemName);
        patientRepository.save(savedPat);

        return savedPat;
    }

    public ResponseEntity<?> searchPatients(String keyword) {
        List<Patient> patientList = patientRepository.searchPatients(keyword);
        if (patientList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(patientList));
        }
        List<PatientRequest> patientRequestList = patientList.stream()
                .filter(patient -> patient.getGender() != null && !patient.getGender().equals("Unknown"))  // Filter out patients with unknown or null gender
                .map(this::mapToPatient)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(patientRequestList));

    }

    public PatientRequest mapToPatient(Patient patient) {
        PatientRequest patientRequest = new PatientRequest();
        if (patient.getGender() != null
                && !patient.getGender().equals("Unknown")) {
            patientRequest.setId(patient.getId());
            patientRequest.setName(patient.getName());
            patientRequest.setPhoneNumber(patient.getPhoneNumber());
            patientRequest.setGender(patient.getGender());
        }
        return patientRequest;
    }

    public PatientRequest mapToOrderPatient(Patient patient) {
        PatientRequest patientRequest = new PatientRequest();
        if (patient.getGender() != null) {
            patientRequest.setId(patient.getId());
            patientRequest.setName(patient.getName());
            patientRequest.setPhoneNumber(patient.getPhoneNumber());
            patientRequest.setGender(patient.getGender());
        }
        return patientRequest;
    }

    public ResponseEntity<?> getAllPatients(Long branchId, String patientSearch, Pageable pageable) {
        var user = userDetailsService.getAuthenticatedUser();
        var userBranch = userDetailsService.getBranch();
        Long branch = branchId != null ? branchId : (userBranch != null ? userBranch.getId() : null);

        var intelRxId = userDetailsService.getIntelRxId();

        // Get the unfiltered page of patients
        Page<Patient> orderPage = patientRepository.findByPatientsFilter(intelRxId, branch, patientSearch, pageable);

        // Filter and map patients to PatientHistoryResponse
        List<PatientHistoryResponse> filteredPatients = orderPage.getContent().stream()
                .filter(patient -> patient.getGender() != null && !patient.getGender().equals("Unknown"))  // Filter invalid entries
                .map(this::mapToPatientHistory)  // Map valid patients to PatientHistoryResponse
                .collect(Collectors.toList());

        // Re-create a page object with the filtered results
        Page<PatientHistoryResponse> responsePage = new PageImpl<>(filteredPatients, pageable, orderPage.getTotalElements());

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> getPatients() {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();

        List<Patient> patientList = patientRepository.findAllByIntelRxId(intelRxId);

        // Initialize the response list
        List<PatientHistoryResponse> responsePage = patientList.stream()
                .filter(patient -> patient.getGender() != null && !patient.getGender().equals("Unknown"))  // Filter out patients with unknown or null gender
                .map(this::mapToPatientHistory)
                .collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    private PatientHistoryResponse mapToPatientHistory(Patient patient) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        PatientHistoryResponse response = new PatientHistoryResponse();

        if (patient.getGender() != null
                && !patient.getGender().equals("Unknown")) {
            PatientRequest patientRequest = new PatientRequest();
            patientRequest.setId(patient.getId());
            patientRequest.setName(patient.getName());
            patientRequest.setPhoneNumber(patient.getPhoneNumber());
            patientRequest.setGender(patient.getGender());
            patientRequest.setIntelRxId(patient.getIntelRxId());

            List<Order> orderList = orderRepository.findAllByIntelRxIdAndPatient(intelRxId, patient);

            double lifeTimePurchase = orderRepository.getTotalAmountByPatientAndIntelRxId(patient.getId(), intelRxId);

            response.setPatientDetails(patientRequest);
            response.setPharmacyVisits(orderList.size());
            response.setLifeTimePurchase(lifeTimePurchase);
            response.setDateAdded(patient.getCreatedAt());
        }

        return response;
    }

    public ResponseEntity<?> patientMedPurchaseHistories(Long patientId) {
        String intelRxId = userDetailsService.getIntelRxId();

        if (patientId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patient not found"));
        }

        List<Order> orderList = orderRepository.findAllByIntelRxIdAndPatient(intelRxId, patient);
        //Order order = orderList.get(0);
        List<OrderResponse> orderResponses = orderList.stream().map(order -> mapToOrderResponse(order)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(orderResponses));
    }

    public OrderResponse mapToOrderResponse(Order order) {

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setOrderRef(order.getOrderRef());
        orderResponse.setBalance(order.getBalance());
        orderResponse.setCashPayment(order.getCashPayment());
        orderResponse.setPosPayment(order.getPosPayment());
        orderResponse.setTransferPayment(order.getTransferPayment());
        orderResponse.setIntelRxId(order.getIntelRxId());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setSalesStatus(order.getSalesStatus());
        orderResponse.setStatus(order.isStatus());


        List<String> paymentMethod = new ArrayList<>();
        if (order.getCashPayment() > 0) {
            paymentMethod.add("Cash");
        }
        if (order.getPosPayment() > 0) {
            paymentMethod.add("POS");
        }
        if (order.getTransferPayment() > 0) {
            paymentMethod.add("Transfer");
        }

        orderResponse.setPaymentMethod(paymentMethod);
        orderResponse.setOrderedDate(order.getOrderedDate());

        return orderResponse;
    }

    public ResponseEntity<?> medicationDetails(Long orderId) {
        var user = userDetailsService.getAuthenticatedUser();

        if (orderId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId is required"));
        }

        //var intelRxId = userDetailsService.getIntelRxId();
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (!optionalOrder.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("order not found"));
        }

        Order order = optionalOrder.get();
        List<CartItem> cartItemList = cartItemRepository.findByIntelRxIdAndAndOrderAndStatus
                (order.getIntelRxId(), order, true);
        List<CartItemResponse> cartItemResponseList = cartItemList.stream().map(this::mapToCartItem).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(cartItemResponseList));
    }

    public CartItemResponse mapToCartItem(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setId(cartItem.getId());
        cartItemResponse.setOrderId(cartItem.getOrder().getId());
        cartItemResponse.setInventoryItem(filterInventory.mapToAddedInventoryResponse(cartItem.getInventory()));
        cartItemResponse.setAmount(cartItem.getAmount());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setIntelRxId(cartItem.getIntelRxId());

        if (cartItem.getMedPrescription() != null) {
            MedPrescription medPrescriptions = cartItem.getMedPrescription();
            MedPrescriptionResponse responseList = mapToMedPrescription(medPrescriptions);
            cartItemResponse.setMedPrescription(responseList);
        }

        cartItemResponse.setPatient(mapToPatient(cartItem.getPatient()));

        if (cartItem.getPharmacyBranch() != null) {
            cartItemResponse.setBranch(pharmacyMapping.mapToBranchResponse(cartItem.getPharmacyBranch()));
        }
        return cartItemResponse;
    }

    public MedPrescriptionResponse mapToMedPrescription(MedPrescription medPrescription) {
        MedPrescriptionResponse medPrescriptionResponse = new MedPrescriptionResponse();
        medPrescriptionResponse.setId(medPrescription.getId());
        medPrescriptionResponse.setDosage(medPrescription.getDosage());
        medPrescriptionResponse.setIntelRxId(medPrescription.getIntelRxId());
        medPrescriptionResponse.setRefillDuration(medPrescription.getRefillDuration());
        medPrescriptionResponse.setRefillAlert(medPrescription.isRefill());
        medPrescriptionResponse.setSmsAlert(medPrescription.isSmsAlert());
        return medPrescriptionResponse;
    }

    public ResponseEntity<?> patientRefillNotification(Long patientId) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (patientId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        Optional<Patient> optionalPatient = patientRepository.findById(patientId);
        if (!optionalPatient.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patient not found"));
        }
        Patient patient = optionalPatient.get();
        List<CartItem> cartItemList = cartItemRepository.findByIntelRxIdAndPatientAndStatus(intelRxId, patient, true);
        List<CartItemResponse> cartItemResponseList = cartItemList.stream().map(this::mapToCartItem).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(cartItemResponseList));
    }

    public CartItemResponse mapToMedPrescription(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        if (cartItem.getMedPrescription() != null) {
            cartItemResponse.setId(cartItem.getId());
            cartItemResponse.setOrderId(cartItem.getOrder().getId());
            cartItemResponse.setAmount(cartItem.getAmount());
            cartItemResponse.setQuantity(cartItem.getQuantity());
            cartItemResponse.setIntelRxId(cartItem.getIntelRxId());

            MedPrescription medPrescription = cartItem.getMedPrescription();

            MedPrescriptionResponse medPrescriptionResponse = new MedPrescriptionResponse();
            medPrescriptionResponse.setId(medPrescription.getId());
            medPrescriptionResponse.setDosage(medPrescription.getDosage());
            medPrescriptionResponse.setRefillDuration(medPrescription.getRefillDuration());
            medPrescriptionResponse.setRefillAlert(medPrescription.isRefill());
            medPrescriptionResponse.setSmsAlert(medPrescription.isSmsAlert());

            cartItemResponse.setMedPrescription(medPrescriptionResponse);
        }

        return cartItemResponse;
    }

    public ResponseEntity<?> patientExtMedPurchaseHistories(Long patientId, String otp) {
        String intelRxId = userDetailsService.getIntelRxId();

        if (patientId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patient not found"));
        }
        ResponseEntity responseEntity = verifyOtp(patient, otp);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        List<Order> orderList = orderRepository.findByPatientIdAndIntelRxIdNot(patientId, intelRxId);

        List<OrderResponse> orderResponses = orderList.stream().map(order -> mapToOrderResponse(order)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(orderResponses));
    }

    // Generates a 4-digit OTP and sets otp and otpCreatedAt fields on the Patient entity
    @Transactional
    public ResponseEntity<?> generateOtp(Long patientId) {
        var pharmacy = userDetailsService.getPharmacyInfo();

        if (patientId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patient not found"));
        }
//        if (!utility.isNigerianPhoneNumber(patient.getPhoneNumber())) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("Patient phone number is not valid"));
//        }

        int otp = 1000 + random.nextInt(9000);  // Generates a 4-digit number between 1000-9999
        patient.setOtp(String.valueOf(otp));
        patient.setOtpCreatedAt(LocalDateTime.now());
        var pat = patientRepository.save(patient);
        String msg = "Your OTP " + otp + " initiated from " + pharmacy.getPharmacyName() +
                ". Kindly ignore if you did not permit this";
        termiiSMSService.sendSms(pat.getPhoneNumber(), msg);
        return ResponseEntity.ok(StandardResponse.success("OTP Sent Successfully " + otp));
    }

    // Verifies the OTP, checks expiration, and updates fields if the OTP is valid
    public ResponseEntity<String> verifyOtp(Patient patient, String enteredOtp) {
        if (patient.getOtp() == null || patient.getOtpCreatedAt() == null) {
            return null;  // No OTP was generated
        }

        // Check if the OTP is within the valid timeframe
        Duration duration = Duration.between(patient.getOtpCreatedAt(), LocalDateTime.now());
        if (duration.toMinutes() > OTP_EXPIRATION_MINUTES) {
            // OTP is expired
            patient.setOtp(null);
            patient.setOtpCreatedAt(null);
            patientRepository.save(patient);
            return ResponseEntity.badRequest().body("OTP is expired");
        }

        // Verify if the entered OTP matches the generated OTP
        if (patient.getOtp().equals(enteredOtp)) {
            // OTP is correct, clear OTP fields
            patient.setOtp(null);
            patient.setOtpCreatedAt(null);
            patientRepository.save(patient);
            return ResponseEntity.ok("OTP is verified");
        } else {
            return ResponseEntity.badRequest().body("OTP does not match");  // OTP does not match
        }
    }

    @Transactional
    public ResponseEntity<?> addTestResult(List<TestResultRequest> requests) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRx = userDetailsService.getIntelRxId();

        for (TestResultRequest request : requests) {
            var medTestType = medTestTypeRepository.findById(request.getTestTypeId())
                    .orElse(null);

            if (medTestType == null) {
                return ResponseEntity.badRequest().body(
                        StandardResponse.error("testTypeId not found")
                );
            }

            var patient = patientRepository.findById(request.getPatientId())
                    .orElse(null);

            if (patient == null) {
                return ResponseEntity.badRequest().body(
                        StandardResponse.error("patientId not found")
                );
            }

            MedTestResult medTestResult = new MedTestResult();
            medTestResult.setIntelRxId(intelRx);
            medTestResult.setTestResult(request.getTestResult());
            medTestResult.setUser(user);
            medTestResult.setTestType(medTestType);
            medTestResult.setPatient(patient);
            medTestResult.setTestNotes(request.getTestNotes());
            medTestResult.setLocalDateTime(LocalDateTime.now());
            medTestResultRepository.save(medTestResult);
        }
        return ResponseEntity.ok(StandardResponse.success("Test added successfully"));
    }

    public ResponseEntity<?> getAllTestResult(Long patientId) {
        var intelRx = userDetailsService.getIntelRxId();

        var patient = patientRepository.findById(patientId)
                .orElse(null);

        if (patient == null) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("patientId not found")
            );
        }

        List<MedTestResult> medTestResults = medTestResultRepository.
                findAllByIntelRxIdAndPatient(intelRx, patient);

        if (medTestResults.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(medTestResults));
        }

        List<TestResultResponse> resultResponses = medTestResults.stream().map(
                this::mapTestResult
        ).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(resultResponses));
    }

    public ResponseEntity<?> getTestResult(Long medTestResultId) {
        var intelRx = userDetailsService.getIntelRxId();

        MedTestResult medTestResult = medTestResultRepository.
                findByIdAndIntelRxId(medTestResultId, intelRx).orElse(null);

        if (medTestResult == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("medTestResult not found"));
        }

        TestResultResponse resultResponses = mapTestResult(medTestResult);
        return ResponseEntity.ok(StandardResponse.success(resultResponses));
    }

    public TestResultResponse mapTestResult(MedTestResult medTestResult) {
        TestResultResponse resultResponse = new TestResultResponse();
        resultResponse.setId(medTestResult.getId());
        resultResponse.setPatient(mapToPatient(medTestResult.getPatient()));
        resultResponse.setIntelRxId(medTestResult.getIntelRxId());
        resultResponse.setTestResult(medTestResult.getTestResult());
        resultResponse.setTestNotes(medTestResult.getTestNotes());
        resultResponse.setTestType(medTestResult.getTestType());
        resultResponse.setCreatedAt(medTestResult.getLocalDateTime());

        return resultResponse;
    }


}
