package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponDetailRequest;
import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponRequest;
import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponResponse;
import com.pharmacy.intelrx.generalCoupon.models.CouponType;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCoupon;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCouponDetail;
import com.pharmacy.intelrx.generalCoupon.repositories.GeneralCouponDetailRepository;
import com.pharmacy.intelrx.generalCoupon.repositories.GeneralCouponRepository;
import com.pharmacy.intelrx.pharmacy.dto.ReceiptResponse;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.utility.SalesMapping;
import com.pharmacy.intelrx.utility.AdminDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminGeneralCouponService")
public class GeneralCouponService {
    private final Utility utility;
    private final GeneralCouponRepository generalCouponRepository;
    private final GeneralCouponDetailRepository couponDetailRepository;
    private final AdminDetailsService adminDetailsService;
    private final PharmacyRepository pharmacyRepository;
    private final PatientService patientService;
    private final SalesMapping salesMapping;

    @Transactional
    public ResponseEntity<?> createGeneralCoupon(GeneralCouponRequest request) {
        Admin admin = adminDetailsService.getAuthenticatedUser();
        if (admin == null) {
            return ResponseEntity.badRequest().body("admin not authenticated");
        }
        ResponseEntity<?> responseEntity = validateGeneralCoupon(request);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        double amount = request.getCouponValue() / request.getCountPerPerson();
        if (request.getAmountPerPerson() != utility.roundAmount(amount)) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("amountPerPerson is wrong. It should be " +
                            utility.roundAmount(amount)));
        }

        GeneralCoupon generalCoupon = GeneralCoupon.builder()
                .couponTitle(request.getCouponTitle())
                .couponEventType(request.getCouponEventType())
                .couponDescription(request.getCouponDescription())
                .couponStatus(request.getCouponStatus())//active,expired,disabled
                .couponType(CouponType.GENERAL)
                .couponValue(request.getCouponValue())
                .sharingCapacity(request.getSharingCapacity())//automatic or manual
                .couponVicinity(request.getCouponVicinity())//State or Pharmacy
                .countPerPerson(request.getCountPerPerson())
                .amountPerPerson(utility.roundAmount(amount))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdByAdmin(admin)
                .createdAt(LocalDateTime.now())
                .build();

        var genCoupon = generalCouponRepository.save(generalCoupon);
        if (request.getSharingCapacity().equals("automatic") &&
                request.getCouponVicinity().equals("State")) {
            addCouponDetailsByState(genCoupon, request);
        }
        if (request.getSharingCapacity().equals("manually")) {
            if (request.getCouponVicinity().equals("Pharmacy")) {
                addCouponDetailsByPharmacy(genCoupon, request);
            }
            if (request.getCouponVicinity().equals("State")) {
                addCouponDetailsByState(genCoupon, request);
            }
        }


        return ResponseEntity.ok(StandardResponse.success(
                "You have successfully created a coupon of " + request.getCouponValue() + " Naira. " +
                        "This coupon would be expiring on " + request.getEndDate() + "."));

    }

    public void addCouponDetailsByPharmacy(GeneralCoupon genCoupon, GeneralCouponRequest request) {
        int countPerPerson = request.getCountPerPerson();  // Number of coupon details to be saved
        List<Long> pharmacyIds = List.of(request.getPharmacyVicinityId());
        List<GeneralCouponDetail> couponDetails = new ArrayList<>();
        Random random = new Random();

        // Step 1: Add one GeneralCouponDetail entry per pharmacy
        for (Long pharmacyId : pharmacyIds) {
            // Handle the case where pharmacyId is 0, meaning all pharmacies should be considered
            if (pharmacyId == 0) {
                List<Pharmacy> pharmacies = pharmacyRepository.findAll();
                if (!pharmacies.isEmpty()) {
                    for (Pharmacy pharmacy : pharmacies) {
                        // Create GeneralCouponDetail for each pharmacy
                        GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                                .couponCode(genCoupon.getCouponCode())
                                .couponDetailStatus(genCoupon.getCouponStatus())
                                .generalCoupon(genCoupon)
                                .createdByAdmin(genCoupon.getCreatedByAdmin())
                                .pharmacyVicinity(pharmacy)
                                .build();

                        // Add to the batch list
                        couponDetails.add(generalCouponDetail);

                        // Stop when the required count is reached
                        if (couponDetails.size() >= countPerPerson) {
                            break;
                        }
                    }
                } else {
                    // Create GeneralCouponDetail for each pharmacy
                    GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                            .couponCode(genCoupon.getCouponCode())
                            .couponDetailStatus(genCoupon.getCouponStatus())
                            .generalCoupon(genCoupon)
                            .createdByAdmin(genCoupon.getCreatedByAdmin())
                            .build();

                    // Add to the batch list
                    couponDetails.add(generalCouponDetail);

                    // Stop when the required count is reached
                    if (couponDetails.size() >= countPerPerson) {
                        break;
                    }
                }

            } else {
                var pharmacy = pharmacyRepository.findById(pharmacyId).orElse(null);

                // If pharmacy is not found, throw an exception or handle the error
                if (pharmacy == null) {
                    throw new IllegalArgumentException("pharmacyVicinityId " + pharmacyId + " not found");
                }

                // Create GeneralCouponDetail for each pharmacy
                GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                        .couponCode(genCoupon.getCouponCode())
                        .couponDetailStatus(genCoupon.getCouponStatus())
                        .generalCoupon(genCoupon)
                        .createdByAdmin(genCoupon.getCreatedByAdmin())
                        .pharmacyVicinity(pharmacy)
                        .build();

                // Add to the batch list
                couponDetails.add(generalCouponDetail);

                // Stop when the required count is reached
                if (couponDetails.size() >= countPerPerson) {
                    break;
                }
            }
        }

        // Step 2: If the required number of entries isn't met, randomly pick additional pharmacies
        while (couponDetails.size() < countPerPerson) {
            Long randomPharmacyId = pharmacyIds.get(random.nextInt(pharmacyIds.size()));
            if (randomPharmacyId == 0) {
                List<Pharmacy> pharmacies = pharmacyRepository.findAll();
                if (!pharmacies.isEmpty()) {
                    for (Pharmacy pharmacy : pharmacies) {
                        GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                                .couponCode(genCoupon.getCouponCode())
                                .couponDetailStatus(genCoupon.getCouponStatus())
                                .generalCoupon(genCoupon)
                                .pharmacyVicinity(pharmacy)
                                .createdByAdmin(genCoupon.getCreatedByAdmin())
                                .build();

                        // Add the detail to the list
                        couponDetails.add(generalCouponDetail);
                    }
                }
            } else {

                var pharmacy = pharmacyRepository.findById(randomPharmacyId).orElse(null);

                // If pharmacy is not found (though it shouldn't be), throw an exception or handle the error
                if (pharmacy == null) {
                    throw new IllegalArgumentException("pharmacyVicinityId " + randomPharmacyId + " not found");
                }

                GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                        .couponCode(genCoupon.getCouponCode())
                        .couponDetailStatus(genCoupon.getCouponStatus())
                        .generalCoupon(genCoupon)
                        .createdByAdmin(genCoupon.getCreatedByAdmin())
                        .pharmacyVicinity(pharmacy)
                        .build();

                // Add the detail to the list
                couponDetails.add(generalCouponDetail);
            }
        }

        // Batch save all GeneralCouponDetail entries
        couponDetailRepository.saveAll(couponDetails);
    }

    public void addCouponDetailsByState(GeneralCoupon genCoupon, GeneralCouponRequest request) {
        int countPerPerson = request.getCountPerPerson();  // Number of coupon details to be saved
        List<String> states = List.of(request.getStateVicinity());
        List<GeneralCouponDetail> couponDetails = new ArrayList<>();
        Random random = new Random();

        // Step 1: Add one GeneralCouponDetail entry per state
        for (String state : states) {
            GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                    .couponCode(genCoupon.getCouponCode())
                    .couponDetailStatus(genCoupon.getCouponStatus())
                    .generalCoupon(genCoupon)
                    .createdByAdmin(genCoupon.getCreatedByAdmin())
                    .stateVicinity(state)
                    .build();

            couponDetails.add(generalCouponDetail);
            if (couponDetails.size() >= countPerPerson) {
                break;  // Stop when the required count is reached
            }
        }

        // Step 2: If the required number of entries isn't met, randomly pick additional states
        while (couponDetails.size() < countPerPerson) {
            String randomState = states.get(random.nextInt(states.size()));  // Pick a random state
            GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                    .couponCode(genCoupon.getCouponCode())
                    .couponDetailStatus(genCoupon.getCouponStatus())
                    .generalCoupon(genCoupon)
                    .createdByAdmin(genCoupon.getCreatedByAdmin())
                    .stateVicinity(randomState)
                    .build();

            couponDetails.add(generalCouponDetail);
        }

        // Step 3: Batch save all GeneralCouponDetail entries
        couponDetailRepository.saveAll(couponDetails);
    }

    public ResponseEntity<?> allCoupons(String couponStatus, String searchTerm, String sortBy, Pageable pageable) {

        Page<GeneralCoupon> generalCouponPage = generalCouponRepository.findByFilter
                (null, couponStatus, searchTerm, sortBy, pageable);

        List<GeneralCouponResponse> filteredCoupons = generalCouponPage.get()
                .map(this::mapToGeneraCoupon)
                .filter(generalCouponResponse ->
                        generalCouponResponse.getCouponType() != CouponType.IN_HOUSE)
                .toList();

        Page<GeneralCouponResponse> responsePage = new PageImpl<>(filteredCoupons, generalCouponPage.getPageable(), generalCouponPage.getTotalElements());

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public GeneralCouponResponse mapToGeneraCoupon(GeneralCoupon geneCoupon) {
        List<GeneralCouponDetail> generalCouponDetails = couponDetailRepository.findByGeneralCoupon(geneCoupon);
        String getStateVicinity = generalCouponDetails.stream()
                .map(GeneralCouponDetail::getStateVicinity)
                .filter(stateVicinity -> stateVicinity != null) // Optional: filter out null values
                .collect(Collectors.joining(", "));

        String pharmacyVicinity = generalCouponDetails.stream()
                .map(GeneralCouponDetail::getPharmacyVicinity) // Get the Pharmacy object
                .filter(pharmacy -> pharmacy != null && pharmacy.getPharmacyName() != null) // Filter out null pharmacies and names
                .map(Pharmacy::getPharmacyName) // Extract the pharmacy name
                .collect(Collectors.joining(", ")); // Join names with a comma separator


        int countGenCouponDet = couponDetailRepository.countByGeneralCouponAndOrderNotNull(geneCoupon);

        GeneralCouponResponse genCouponRes = new GeneralCouponResponse();
        genCouponRes.setId(geneCoupon.getId());
        genCouponRes.setCouponTitle(geneCoupon.getCouponTitle());
        genCouponRes.setCouponEventType(geneCoupon.getCouponEventType());
        genCouponRes.setCouponDescription(geneCoupon.getCouponDescription());
        genCouponRes.setCouponCode(geneCoupon.getCouponCode());
        genCouponRes.setCountPerPerson(geneCoupon.getCountPerPerson());
        genCouponRes.setAmountPerPerson(geneCoupon.getAmountPerPerson());
        genCouponRes.setCouponUsed(countGenCouponDet);
        genCouponRes.setCouponStatus(geneCoupon.getCouponStatus());
        genCouponRes.setStartDate(geneCoupon.getStartDate());
        genCouponRes.setEndDate(geneCoupon.getEndDate());
        genCouponRes.setCouponValue(geneCoupon.getCouponValue());
        genCouponRes.setStateVicinity(getStateVicinity);
        genCouponRes.setPharmacyVicinity(pharmacyVicinity);
        //genCouponRes

        return genCouponRes;
    }

    public ResponseEntity<?> getCouponDetails(String couponCode, Pageable pageable) {

        Page<GeneralCouponDetail> generalCouponPage = couponDetailRepository.findByCouponCode
                (couponCode, pageable);

        Page<GeneralCouponDetailRequest> responsePage = generalCouponPage.map(this::mapToGeneraCouponDetails);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public GeneralCouponDetailRequest mapToGeneraCouponDetails(GeneralCouponDetail geneCoupon) {
        String location = "";
        String pharmacyName = "";
        GeneralCouponDetailRequest generalCouponRequest = new GeneralCouponDetailRequest();
        generalCouponRequest.setId(geneCoupon.getId());
        generalCouponRequest.setCouponCode(geneCoupon.getCouponCode());
        if (geneCoupon.getOrderReference() != null) {
            Order order = geneCoupon.getOrderReference();
            if (order.getPatient() != null) {
                generalCouponRequest.setPatient(patientService.mapToPatient(order.getPatient()));
            }
            String intelRx = order.getIntelRxId();
            var pharmacy = pharmacyRepository.findByIntelRxId(intelRx).orElse(null);
            pharmacyName = pharmacy == null ? null : pharmacy.getPharmacyName();
            String state = pharmacy.getContactInfo() == null ? null : pharmacy.getContactInfo().getState();
            String country = pharmacy.getContactInfo() == null ? null : pharmacy.getContactInfo().getCountry();
            location = state + " " + country;

        }
        generalCouponRequest.setLocation(location);
        generalCouponRequest.setPharmacy(pharmacyName);
        generalCouponRequest.setDateUsed(geneCoupon.getDateUsed());
        generalCouponRequest.setTimeUsed(geneCoupon.getTimeUsed());

        return generalCouponRequest;
    }

    public ResponseEntity<?> viewOrderReceipt(String couponCode, Long orderId) {
        if (utility.isNullOrEmpty(couponCode)) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponCode is required")
            );
        } else if (orderId == null) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("orderId is required")
            );
        }
        var couponOrder = couponDetailRepository
                .findByCouponCodeAndOrderReferenceId(couponCode, orderId).orElse(null);
        if (couponOrder == null) {
            return ResponseEntity.ok(StandardResponse.success("No order receipt found"));
        }
        Order order = couponOrder.getOrderReference();
        ReceiptResponse responsePage = salesMapping.mapToReceiptResponse(order);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> validateGeneralCoupon(GeneralCouponRequest request) {
        var admin = adminDetailsService.getAuthenticatedUser();
        if (utility.isNullOrEmpty(request.getCouponTitle())) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponTitle is required")
            );
        } else if (utility.isNullOrEmpty(request.getCouponEventType())) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponEventType is required")
            );
        } else if (!request.getCouponEventType().equals("General")
                && !request.getCouponEventType().equals("Sponsored")
                && !request.getCouponEventType().equals("Event")) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponStatus should only be set to General,Sponsored,Event")
            );
        } else if (utility.isNullOrEmpty(request.getCouponStatus())) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponStatus is required")
            );
        } else if (!request.getCouponStatus().equals("active")
                && !request.getCouponStatus().equals("expired")
                && !request.getCouponStatus().equals("disabled")) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponStatus should only be set to active,expired,disabled")
            );
        } else if (utility.isNullOrEmpty(request.getCouponDescription())) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponDescription is required")
            );
        } else if (request.getStartDate() == null) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("startDate is required")
            );
        } else if (request.getEndDate() == null) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("endDate is required")
            );
        } else if (request.getCouponValue() <= 0) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponValue must be greater than 0")
            );
        } else if (utility.isNullOrEmpty(request.getSharingCapacity())) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("sharingCapacity is required")
            );
        } else if (request.getAmountPerPerson() <= 0) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("amountPerPerson must be greater than 0")
            );
        } else if (request.getCountPerPerson() <= 0) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("countPerPerson must be greater than 0")
            );
        } else {
            if (admin != null) {
                if (request.getSharingCapacity().equals("automatic") && !request.getCouponVicinity().equals("State")) {
                    return ResponseEntity.badRequest().body(
                            StandardResponse.error("stateVicinity is required when sharingCapacity is automatic")
                    );
                } else if (request.getSharingCapacity().equals("manually")) {
                    if (utility.isNullOrEmpty(request.getCouponVicinity())) {
                        return ResponseEntity.badRequest().body(
                                StandardResponse.error("couponVicinity is required")
                        );
                    } else if (request.getStateVicinity() == null &&
                            request.getPharmacyVicinityId() == null) {
                        return ResponseEntity.badRequest().body(
                                StandardResponse.error("stateVicinity or pharmacyVicinityId is required for manually sharingCapacity")
                        );
                    } else if (request.getCouponVicinity().equalsIgnoreCase("State")
                            && request.getStateVicinity() == null) {
                        return ResponseEntity.badRequest().body(
                                StandardResponse.error("stateVicinity is required when couponVicinity is State")
                        );
                    } else if (request.getCouponVicinity().equalsIgnoreCase("Pharmacy")
                            && request.getPharmacyVicinityId() == null) {
                        return ResponseEntity.badRequest().body(
                                StandardResponse.error("pharmacyVicinityId is required when couponVicinity is Pharmacy")
                        );
                    }
                }
            } else {
                if (request.getPharmacyBranchVicinityId() == null) {
                    return ResponseEntity.badRequest().body(
                            StandardResponse.error("pharmacyBranchVicinityId is required when couponVicinity is Pharmacy")
                    );
                }
            }
        }

        // Proceed with valid request handling
        return ResponseEntity.ok("validated successfully");

    }

    // Optionally, schedule this method to run at a specific time interval (e.g., daily)
    @Scheduled(cron = "0 0 0 * * ?") // This runs the task daily at midnight
    public void checkAndExpireCoupons() {
        // Fetch all active coupons
        List<GeneralCoupon> activeCoupons = generalCouponRepository.findByCouponStatus("active");

        // Check if any coupon has passed its end date
        for (GeneralCoupon coupon : activeCoupons) {
            if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDate.now())) {
                coupon.setCouponStatus("expired");
                generalCouponRepository.save(coupon); // Save updated coupon status
            }
        }
    }
}
