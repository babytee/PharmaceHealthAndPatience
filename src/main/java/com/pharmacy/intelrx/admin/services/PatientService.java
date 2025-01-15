package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminPatientService")
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserDetailsService userDetailsService;
    private final OrderRepository orderRepository;
    private final FilterInventory filterInventory;
    private final PharmacyMapping pharmacyMapping;
    private final CartItemRepository cartItemRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyBranchRepository branchRepository;


    public PatientRequest mapToPatient(Patient patient) {
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setId(patient.getId());
        patientRequest.setName(patient.getName());
        patientRequest.setPhoneNumber(patient.getPhoneNumber());
        patientRequest.setGender(patient.getGender());
        return patientRequest;
    }

    public ResponseEntity<?> getPharmacyPatients(String patientSearch, String intelRxId, Pageable pageable) {

        Page<Patient> orderPage = patientRepository.searchPharmacyPatients(intelRxId, patientSearch, pageable);

        Page<PatientHistoryResponse> responsePage = orderPage.map(this::mapToPatientHistory);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> getPharmacyBranchPatients(String patientSearch, String intelRxId, Long branchId, Pageable pageable) {

        Optional<PharmacyBranch> optional = branchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("branchId does not exist"));
        }
        PharmacyBranch branch = optional.get();
        Page<Patient> orderPage = patientRepository.findByPatientsFilter(intelRxId, branchId, patientSearch, pageable);
        Page<PatientHistoryResponse> responsePage = orderPage.map(this::mapToPatientHistory);
        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> getAllPatients(String patientSearch, Pageable pageable) {

        Page<Patient> orderPage = patientRepository.searchAllPatient(patientSearch, pageable);

        Page<PatientHistoryResponse> responsePage = orderPage.map(this::mapToPatientHistory);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> getPatientDetails(Long patientId,String intelRxId) {
        if(patientId == null){
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }
        if(intelRxId == null){
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }
        Optional<Patient> optionalPatient = patientRepository.findByIdAndIntelRxId(patientId,intelRxId);
        if(optionalPatient.isPresent()){
            return ResponseEntity.badRequest().body(StandardResponse.error("Patient not found"));
        }
        Patient patient = optionalPatient.get();
        PatientHistoryResponse responsePage = mapToPatientHistory(patient);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> getPatients() {
        var user = userDetailsService.getAuthenticatedUser();

        var intelRxId = userDetailsService.getIntelRxId();

        List<Patient> patientList = patientRepository.findAll();

        List<PatientHistoryResponse> responsePage = patientList.stream().map(patient -> mapToPatientHistory(patient)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    private PatientHistoryResponse mapToPatientHistory(Patient patient) {

        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setId(patient.getId());
        patientRequest.setName(patient.getName());
        patientRequest.setPhoneNumber(patient.getPhoneNumber());
        patientRequest.setGender(patient.getGender());

        List<Order> orderList = orderRepository.findAllByIntelRxIdAndPatient(patient.getIntelRxId(), patient);

        double lifeTimePurchase = orderRepository.getTotalAmountByPatientAndIntelRxId(patient.getId(), patient.getIntelRxId());

        var optionalPharmacy = pharmacyRepository.findByIntelRxId(patient.getIntelRxId());

        Pharmacy pharmacy = optionalPharmacy.get();
        String location = null;
        if (pharmacy.getContactInfo() != null) {
            String state = pharmacy.getContactInfo().getState();
            String country = pharmacy.getContactInfo().getCountry();
            location = state + "," + country;
        }
        LocalDateTime firstPurchaseDate = null;
        if (!orderList.isEmpty()) {
            Order order = orderList.get(0);
            firstPurchaseDate = order.getOrderedDate();
        }


        PatientHistoryResponse response = new PatientHistoryResponse();
        response.setPatientDetails(patientRequest);
        response.setPharmacyVisits(orderList.size());
        response.setPharmacyVisited(pharmacy.getPharmacyName());
        response.setLocation(location);
        response.setLifeTimePurchase(lifeTimePurchase);
        response.setDateAdded(patient.getCreatedAt());
        response.setFirstPurchaseDate(firstPurchaseDate);
        return response;
    }

    public ResponseEntity<?> patientMedPurchaseHistories(Long patientId) {

        if (patientId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        Optional<Patient> optionalPatient = patientRepository.findById(patientId);
        if (!optionalPatient.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patient not found"));
        }
        Patient patient = optionalPatient.get();
        List<Order> orderList = orderRepository.findAllByPatient(patient);

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
        if (orderId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId is required"));
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (!optionalOrder.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("order not found"));
        }
        Order order = optionalOrder.get();
        List<CartItem> cartItemList = cartItemRepository.findByIntelRxIdAndAndOrderAndStatus(order.getIntelRxId(), order, true);
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
}
