package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.SmsByTermii.TermiiSMSService;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.CartItemResponse;
import com.pharmacy.intelrx.pharmacy.dto.MedPrescriptionResponse;
import com.pharmacy.intelrx.pharmacy.dto.PatientRequest;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.MedPrescription;
import com.pharmacy.intelrx.pharmacy.models.Patient;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.RefillDuration;
import com.pharmacy.intelrx.pharmacy.repositories.MedPrescriptionRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PatientRepository;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("MedPrescriptionRefillAlertService")
public class MedPrescriptionRefillAlertService {
    private final MedPrescriptionRepository medPrescriptionRepository;
    private final UserDetailsService userDetailsService;
    private final PatientRepository patientRepository;
    private final FilterInventory filterInventory;
    private final PharmacyMapping pharmacyMapping;
    private final TermiiSMSService termiiSMSService;
    private final Utility utility;

    public ResponseEntity<?> getRefillAlerts(Long patientId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("User not authenticated"));
        }
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        if (patientId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        Optional<Patient> optionalPatient = patientRepository.findById(patientId);
        if (!optionalPatient.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patient not found"));
        }

        Patient patient = optionalPatient.get();
        List<MedPrescription> refillPrescriptions = medPrescriptionRepository
                .findByIntelRxIdAndPharmacyBranchAndPatientAndRefill(intelRxId, branch, patient, true);

        List<MedPrescriptionResponse> medPrescriptionResponse = new ArrayList<MedPrescriptionResponse>();
        if (refillPrescriptions.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(medPrescriptionResponse));
        }
        LocalDateTime currentDate = LocalDateTime.now();

        medPrescriptionResponse = refillPrescriptions.stream()
                .filter(prescription -> {
                    LocalDateTime refillDate = calculateRefillDate(prescription);
                    return prescription.isRefill() && refillDate != null && !refillDate.isAfter(currentDate);
                })
                .map(this::convertToMedPrescriptionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(medPrescriptionResponse));
    }

    private LocalDateTime calculateRefillDate(MedPrescription prescription) {
        String durationName = prescription.getRefillDuration().getName();
        long durationValue = convertDurationToDays(durationName);
        ChronoUnit unit = getChronoUnit(durationName);

        return prescription.getPrescriptionDate().atStartOfDay().plus(durationValue, unit);
    }

    private long convertDurationToDays(String durationName) {
        switch (durationName) {
            case "2 Minutes":
                return 2;
            case "5 Minutes":
                return 5;
            case "10 Minutes":
                return 10;
            case "5 Days":
                return 5;
            case "6 Days":
                return 6;
            case "1 Week":
                return 7;
            case "2 Weeks":
                return 14;
            case "3 Weeks":
                return 21;
            case "4 Weeks":
                return 28;
            case "5 Weeks":
                return 35;
            case "1 Month":
                return 30;
            case "2 Months":
                return 60;
            case "3 Months":
                return 90;
            case "4 Months":
                return 120;
            case "5 Months":
                return 150;
            case "6 Months":
                return 180;
            case "7 Months":
                return 210;
            case "8 Months":
                return 240;
            case "9 Months":
                return 270;
            case "10 Months":
                return 300;
            case "11 Months":
                return 330;
            case "12 Months":
                return 360;
            default:
                return 0;
        }
    }

    private ChronoUnit getChronoUnit(String durationName) {
        switch (durationName) {
            case "2 Minutes":
            case "5 Minutes":
            case "10 Minutes":
                return ChronoUnit.MINUTES;
            default:
                return ChronoUnit.DAYS;
        }
    }

    private MedPrescriptionResponse convertToMedPrescriptionResponse(MedPrescription prescription) {
        if (prescription.isRefill()) {
            LocalDateTime refillDate = calculateRefillDate(prescription);
            LocalDateTime currentDate = LocalDateTime.now();
            long minutesLeft = ChronoUnit.MINUTES.between(currentDate, refillDate);
            String alertMessage = generateAlertMessage(minutesLeft);

            CartItem cartItem = prescription.getCartItem();

            return MedPrescriptionResponse.builder()
                    .id(prescription.getId())
                    .intelRxId(prescription.getIntelRxId())
                    .dosage(prescription.getDosage())
                    .refillDuration(prescription.getRefillDuration().getName())
                    .refillAlert(prescription.isRefill())
                    .smsAlert(prescription.isSmsAlert())
                    .cartItem(mapToCartItem(cartItem))
                    .patient(prescription.getPatient())
                    .branch(prescription.getPharmacyBranch())
                    .refillReminderDate(prescription.getRefillReminderDate())
                    .build();
        }
        return null;
    }

    private String generateAlertMessage(long minutesLeft) {
        if (minutesLeft > 1440) {
            long daysLeft = minutesLeft / 1440;
            return "Refill due in " + daysLeft + " days";
        } else if (minutesLeft > 0) {
            return "Refill due in " + minutesLeft + " minutes";
        } else if (minutesLeft == 0) {
            return "Refill due now";
        } else {
            long overdueMinutes = Math.abs(minutesLeft);
            if (overdueMinutes > 1440) {
                long overdueDays = overdueMinutes / 1440;
                return "Refill overdue by " + overdueDays + " days";
            } else {
                return "Refill overdue by " + overdueMinutes + " minutes";
            }
        }
    }

    private CartItemResponse mapToCartItem(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setId(cartItem.getId());
        cartItemResponse.setOrderId(cartItem.getOrder().getId());
        cartItemResponse.setInventoryItem(filterInventory.mapToAddedInventoryResponse(cartItem.getInventory()));
        cartItemResponse.setAmount(cartItem.getAmount());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setIntelRxId(cartItem.getIntelRxId());

        cartItemResponse.setPatient(mapToPatient(cartItem.getPatient()));

        if (cartItem.getPharmacyBranch() != null) {
            cartItemResponse.setBranch(pharmacyMapping.mapToBranchResponse(cartItem.getPharmacyBranch()));
        }
        return cartItemResponse;
    }

    private PatientRequest mapToPatient(Patient patient) {
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setId(patient.getId());
        patientRequest.setName(patient.getName());
        patientRequest.setPhoneNumber(patient.getPhoneNumber());
        patientRequest.setGender(patient.getGender());
        return patientRequest;
    }

    public ResponseEntity<?> sendSms(Long medId, Long patientId) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        var pharmacyInfo = userDetailsService.getPharmacyInfo();

        if (patientId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        Optional<Patient> optionalPatient = patientRepository.findById(patientId);
        if (!optionalPatient.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patient not found"));
        }

        Patient patient = optionalPatient.get();
        Optional<MedPrescription> optionalMedPrescription = medPrescriptionRepository
                .findByIdAndIntelRxIdAndPharmacyBranchAndPatient(medId, intelRxId, branch, patient);

        if (!optionalMedPrescription.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("MedPrescription Does Not Exist"));
        }

        MedPrescription medPrescription = optionalMedPrescription.get();
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime refillDate = calculateRefillDate(medPrescription);
        if (medPrescription.isRefill() &&
                refillDate != null && !refillDate.isAfter(currentDate)) {
            String refillDuration = medPrescription.getRefillDuration().getName();

            medPrescription.setRefillReminderDate(LocalDateTime.now());
            medPrescriptionRepository.save(medPrescription);

            // Get inventory and medication details
            Inventory inventory = medPrescription.getCartItem().getInventory();
            String brandName = inventory.getBrand().getName();

            // Extract necessary information
            String inventoryName = inventory.getItemName() + "(" + brandName + ")";
            String encIntName = utility.obfuscateString(inventoryName);

            String patientName = patient.getName();

            String msg = "Hi " + patientName + ", this is a reminder from " + pharmacyInfo.getPharmacyName() + ". " +
                    "We're checking in to see how you're doing with your medication, " +
                    encIntName + ". We also wanted to let you know it's time for a refill. " +
                    "Please give us a call or visit us to re-order. Take care and stay well!";

            termiiSMSService.sendSms(patient.getPhoneNumber(), msg);
            return ResponseEntity.ok(StandardResponse.success("Message sent successfully"));
        }
        return ResponseEntity.ok(StandardResponse.success("Not yet time time for refill"));

    }

    @Scheduled(cron = "0 0 0 * * ?") // This cron expression means every day at midnight
    public void autoSendSmsAlert() {
        List<MedPrescription> medPrescriptions = medPrescriptionRepository.findAll();
        LocalDateTime currentDate = LocalDateTime.now();
        if (!medPrescriptions.isEmpty()) {
            for (MedPrescription medPrescription : medPrescriptions) {
                LocalDateTime refillDate = calculateRefillDate(medPrescription);
                if (medPrescription.isRefill() &&
                        refillDate != null && !refillDate.isAfter(currentDate)) {
                    Patient patient = medPrescription.getPatient();
                    String refillDuration = medPrescription.getRefillDuration().getName();
                    String msg = "Patient is due for a refill in " + refillDuration;

                    termiiSMSService.sendSms(patient.getPhoneNumber(), msg);
                }
            }
        }
    }

}
