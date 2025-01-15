package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.admin.services.PatientService;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.generalCoupon.dto.CouponHistoryRequest;
import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponDetailRequest;
import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponRequest;
import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponResponse;
import com.pharmacy.intelrx.generalCoupon.models.CouponTransactionHistory;
import com.pharmacy.intelrx.generalCoupon.models.CouponType;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCoupon;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCouponDetail;
import com.pharmacy.intelrx.generalCoupon.repositories.CouponTransactionHistoryRepository;
import com.pharmacy.intelrx.generalCoupon.repositories.GeneralCouponDetailRepository;
import com.pharmacy.intelrx.generalCoupon.repositories.GeneralCouponRepository;
import com.pharmacy.intelrx.pharmacy.dto.ReceiptResponse;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyBranchRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyWalletRepository;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyWalletConfig;
import com.pharmacy.intelrx.pharmacy.utility.SalesMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
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
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyGeneralCouponService")
public class GeneralCouponService {
    private final Utility utility;
    private final GeneralCouponRepository generalCouponRepository;
    private final GeneralCouponDetailRepository couponDetailRepository;
    private final UserDetailsService userDetailsService;
    private final PharmacyRepository pharmacyRepository;
    private final PatientService patientService;
    private final SalesMapping salesMapping;
    private final PharmacyBranchRepository branchRepository;
    private final CouponTransactionHistoryRepository transactionHistoryRepository;
    private final PharmacyWalletRepository walletRepository;
    private final PharmacyWalletConfig pharmacyWalletConfig;

    @Transactional
    public ResponseEntity<?> createGeneralCoupon(GeneralCouponRequest request) {
        var pharmacy = userDetailsService.getPharmacyInfo();
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
                .couponEventType("General")
                .couponDescription(request.getCouponDescription())
                .couponStatus(request.getCouponStatus())//active,expired,disabled
                .couponType(CouponType.IN_HOUSE)
                .couponValue(request.getCouponValue())
                .sharingCapacity("manually")//automatic or manual
                .countPerPerson(request.getCountPerPerson())
                .amountPerPerson(utility.roundAmount(amount))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdByPharmacy(pharmacy)
                .createdAt(LocalDateTime.now())
                .build();

        var genCoupon = generalCouponRepository.save(generalCoupon);

        addCouponDetailsByPharmacyBranch(genCoupon, request);

        return ResponseEntity.ok(StandardResponse.success(
                "You have successfully created a coupon of " + request.getCouponValue() + " Naira. " +
                        "This coupon would be expiring on " + request.getEndDate() + "."));

    }

    public void addCouponDetailsByPharmacyBranch(GeneralCoupon genCoupon, GeneralCouponRequest request) {
        int countPerPerson = request.getCountPerPerson();  // Number of coupon details to be saved
        var intelRx = userDetailsService.getIntelRxId();
        List<Long> branchIds = List.of(request.getPharmacyBranchVicinityId());
        List<GeneralCouponDetail> couponDetails = new ArrayList<>();
        Random random = new Random();

        // Step 1: Add coupon details for each pharmacy branch in the provided branch IDs
        for (Long branchId : branchIds) {
            // Handle the case where branchId is 0, meaning all branches should be considered
            if (branchId == 0) {
                List<PharmacyBranch> branches = branchRepository.findAllByIntelRxId(intelRx);
                for (PharmacyBranch branch : branches) {
                    GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                            .couponCode(genCoupon.getCouponCode())
                            .couponDetailStatus(genCoupon.getCouponStatus())
                            .generalCoupon(genCoupon)
                            .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                            .branch(branch)
                            .build();

                    couponDetails.add(generalCouponDetail);
                    if (couponDetails.size() >= countPerPerson) {
                        break;  // Stop when the required count is reached
                    }
                }
            } else {
                // If specific branch is provided, fetch and validate the branch
                var branch = branchRepository.findById(branchId).orElse(null);

                // Handle the case where a branch is not found
                if (branch == null) {
                    throw new IllegalArgumentException("pharmacyBranchVicinityId " + branchId + " not found");
                }

                // Create a coupon detail for the specific branch
                GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                        .couponCode(genCoupon.getCouponCode())
                        .couponDetailStatus(genCoupon.getCouponStatus())
                        .generalCoupon(genCoupon)
                        .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                        .branch(branch)
                        .build();

                couponDetails.add(generalCouponDetail);
            }

            // Stop if the required count of coupon details has been reached
            if (couponDetails.size() >= countPerPerson) {
                break;
            }
        }

        // Step 2: If the count is still below `countPerPerson`, randomly select branches to fill the remainder
        while (couponDetails.size() < countPerPerson) {
            Long randomBranchId = branchIds.get(random.nextInt(branchIds.size()));

            // If branchId is 0, we randomly select from all branches of IntelRx
            if (randomBranchId == 0) {
                List<PharmacyBranch> branches = branchRepository.findAllByIntelRxId(intelRx);
                PharmacyBranch randomBranch = null;
                if (!branches.isEmpty()) {
                    randomBranch = branches.get(random.nextInt(branches.size()));
                }

                GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                        .couponCode(genCoupon.getCouponCode())
                        .couponDetailStatus(genCoupon.getCouponStatus())
                        .generalCoupon(genCoupon)
                        .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                        .branch(randomBranch)
                        .build();

                couponDetails.add(generalCouponDetail);
            } else {
                // Randomly select from specific branches provided
                var branch = branchRepository.findById(randomBranchId).orElse(null);

                if (branch == null) {
                    throw new IllegalArgumentException("pharmacyBranchVicinityId " + randomBranchId + " not found");
                }

                GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                        .couponCode(genCoupon.getCouponCode())
                        .couponDetailStatus(genCoupon.getCouponStatus())
                        .generalCoupon(genCoupon)
                        .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                        .branch(branch)
                        .build();

                couponDetails.add(generalCouponDetail);
            }
        }

        // Step 3: Batch save all GeneralCouponDetail entries
        couponDetailRepository.saveAll(couponDetails);
    }

    @Transactional
    public ResponseEntity<?> updateGeneralCoupon(GeneralCouponRequest request, Long generalCouponId) {
        var pharmacy = userDetailsService.getPharmacyInfo();
        if (generalCouponId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("generalCouponId is required"));
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

        var genCoupon = generalCouponRepository.findById(generalCouponId).orElse(null);

        if (genCoupon == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("coupon with this generalCouponId not found"));
        }

        genCoupon.setCouponTitle(request.getCouponTitle());
        genCoupon.setCouponEventType("General");
        genCoupon.setCouponDescription(request.getCouponDescription());
        genCoupon.setCouponStatus(request.getCouponStatus());//active,expired,disabled
        genCoupon.setCouponType(CouponType.IN_HOUSE);
        genCoupon.setCouponValue(request.getCouponValue());
        genCoupon.setSharingCapacity("manually");//automatic or manual
        genCoupon.setCountPerPerson(request.getCountPerPerson());
        genCoupon.setAmountPerPerson(utility.roundAmount(amount));
        genCoupon.setStartDate(request.getStartDate());
        genCoupon.setEndDate(request.getEndDate());
        genCoupon.setCreatedByPharmacy(pharmacy);
        genCoupon.setCreatedAt(LocalDateTime.now());

        var updatedGenCoupon = generalCouponRepository.save(genCoupon);

        updateCouponDetailsByPharmacyBranch(updatedGenCoupon, request);

        return ResponseEntity.ok(StandardResponse.success(
                "You have successfully updated a coupon of " + request.getCouponValue() + " Naira. " +
                        "This coupon would be expiring on " + request.getEndDate() + "."));

    }

    public void updateCouponDetailsByPharmacyBranch(GeneralCoupon genCoupon, GeneralCouponRequest request) {
        // Step 1: Fetch existing coupon details
        List<GeneralCouponDetail> generalCouponDetails = couponDetailRepository.findByGeneralCoupon(genCoupon);
        int existingCount = generalCouponDetails.size();

        int countPerPerson = request.getCountPerPerson();  // Number of coupon details to be saved
        var intelRx = userDetailsService.getIntelRxId();
        List<Long> branchIds = List.of(request.getPharmacyBranchVicinityId());
        List<GeneralCouponDetail> couponDetailsToAdd = new ArrayList<>();
        Random random = new Random();

        // Step 2: Check if new coupon details need to be added
        if (countPerPerson > existingCount) {
            // Determine the number of coupon details to add
            int remainingCount = countPerPerson - existingCount;

            // Step 3: Add coupon details for each pharmacy branch in the provided branch IDs
            for (Long branchId : branchIds) {
                // Handle the case where branchId is 0, meaning all branches should be considered
                if (branchId == 0) {
                    List<PharmacyBranch> branches = branchRepository.findAllByIntelRxId(intelRx);
                    for (PharmacyBranch branch : branches) {
                        // Skip if coupon detail for this branch already exists
                        boolean exists = generalCouponDetails.stream()
                                .anyMatch(detail -> detail.getBranch().getId().equals(branch.getId()));
                        if (exists) continue;

                        GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                                .couponCode(genCoupon.getCouponCode())
                                .couponDetailStatus(genCoupon.getCouponStatus())
                                .generalCoupon(genCoupon)
                                .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                                .branch(branch)
                                .build();

                        couponDetailsToAdd.add(generalCouponDetail);
                        if (couponDetailsToAdd.size() >= remainingCount) {
                            break;  // Stop when the required count is reached
                        }
                    }
                } else {
                    // If specific branch is provided, fetch and validate the branch
                    var branch = branchRepository.findById(branchId).orElse(null);

                    // Handle the case where a branch is not found
                    if (branch == null) {
                        throw new IllegalArgumentException("pharmacyBranchVicinityId " + branchId + " not found");
                    }

                    // Skip if coupon detail for this branch already exists
                    boolean exists = generalCouponDetails.stream()
                            .anyMatch(detail -> detail.getBranch().getId().equals(branchId));
                    if (exists) continue;

                    // Create a coupon detail for the specific branch
                    GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                            .couponCode(genCoupon.getCouponCode())
                            .couponDetailStatus(genCoupon.getCouponStatus())
                            .generalCoupon(genCoupon)
                            .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                            .branch(branch)
                            .build();

                    couponDetailsToAdd.add(generalCouponDetail);
                }

                // Stop if the required count of coupon details has been reached
                if (couponDetailsToAdd.size() >= remainingCount) {
                    break;
                }
            }

            // Step 4: If the count is still below `countPerPerson`, randomly select branches to fill the remainder
            while (couponDetailsToAdd.size() < remainingCount) {
                Long randomBranchId = branchIds.get(random.nextInt(branchIds.size()));

                // If branchId is 0, we randomly select from all branches of IntelRx
                if (randomBranchId == 0) {
                    List<PharmacyBranch> branches = branchRepository.findAllByIntelRxId(intelRx);
                    PharmacyBranch randomBranch;
                    if (!branches.isEmpty()) {
                        randomBranch = branches.get(random.nextInt(branches.size()));
                    } else {
                        randomBranch = null;
                    }

                    // Skip if coupon detail for this branch already exists
                    boolean exists = generalCouponDetails.stream()
                            .anyMatch(detail -> detail.getBranch().getId().equals(randomBranch.getId()));
                    if (exists) continue;

                    GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                            .couponCode(genCoupon.getCouponCode())
                            .couponDetailStatus(genCoupon.getCouponStatus())
                            .generalCoupon(genCoupon)
                            .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                            .branch(randomBranch)
                            .build();

                    couponDetailsToAdd.add(generalCouponDetail);
                } else {
                    // Randomly select from specific branches provided
                    var branch = branchRepository.findById(randomBranchId).orElse(null);

                    if (branch == null) {
                        throw new IllegalArgumentException("pharmacyBranchVicinityId " + randomBranchId + " not found");
                    }

                    // Skip if coupon detail for this branch already exists
                    boolean exists = generalCouponDetails.stream()
                            .anyMatch(detail -> detail.getBranch().getId().equals(randomBranchId));
                    if (exists) continue;

                    GeneralCouponDetail generalCouponDetail = GeneralCouponDetail.builder()
                            .couponCode(genCoupon.getCouponCode())
                            .couponDetailStatus(genCoupon.getCouponStatus())
                            .generalCoupon(genCoupon)
                            .createdByPharmacy(genCoupon.getCreatedByPharmacy())
                            .branch(branch)
                            .build();

                    couponDetailsToAdd.add(generalCouponDetail);
                }
            }

            // Step 5: Batch save all new GeneralCouponDetail entries
            couponDetailRepository.saveAll(couponDetailsToAdd);
        }
    }

    public ResponseEntity<?> disableCoupon(Long generalCouponId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (generalCouponId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("generalCouponId is required"));
        }
        var genCoupon = generalCouponRepository.findById(generalCouponId).orElse(null);

        if (genCoupon == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("coupon with this generalCouponId not found"));
        }

        String msg = "";
        if (genCoupon.getCouponStatus().equals("disabled")) {
            genCoupon.setCouponStatus("active");//active,expired,disabled
            msg = "Coupon is now active successfully";
        } else {
            genCoupon.setCouponStatus("disabled");//active,expired,disabled
            msg = "Disabled Successfully";
        }

        generalCouponRepository.save(genCoupon);
        return ResponseEntity.ok(StandardResponse.success(msg));

    }

    public ResponseEntity<?> generalCoupons(String couponStatus, String searchTerm, String sortBy, Pageable pageable) {
        var pharmacy = userDetailsService.getPharmacyInfo();

        Page<GeneralCoupon> generalCouponPage = generalCouponRepository.findByFilter
                (null, couponStatus, searchTerm, sortBy, pageable);

        List<GeneralCouponResponse> filteredCoupons = generalCouponPage.get()
                .map(this::mapToGeneraCoupon)
                .filter(generalCouponResponse -> generalCouponResponse.getCouponType() != null
                        && generalCouponResponse.getCouponType() != CouponType.IN_HOUSE)
                .collect(Collectors.toList());

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
        genCouponRes.setCouponType(geneCoupon.getCouponType());
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

    public ResponseEntity<?> inHouseCoupons(String couponStatus, String searchTerm, String sortBy, Pageable pageable) {
        var pharmacy = userDetailsService.getPharmacyInfo();

        Page<GeneralCoupon> generalCouponPage = generalCouponRepository.findByFilter
                (pharmacy, couponStatus, searchTerm, sortBy, pageable);

        List<GeneralCouponResponse> filteredCoupons = generalCouponPage.get()
                .map(this::mapToInHouseCoupon)
                .filter(generalCouponResponse ->
                        generalCouponResponse.getCouponType() == CouponType.IN_HOUSE)
                .toList();

        Page<GeneralCouponResponse> responsePage = new PageImpl<>(filteredCoupons, generalCouponPage.getPageable(), generalCouponPage.getTotalElements());

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public GeneralCouponResponse mapToInHouseCoupon(GeneralCoupon geneCoupon) {
        //var pharmacy = userDetailsService.getPharmacyInfo();
        List<GeneralCouponDetail> generalCouponDetails = couponDetailRepository.findByGeneralCoupon(geneCoupon);
        String getStateVicinity = generalCouponDetails.stream()
                .map(GeneralCouponDetail::getStateVicinity)
                .filter(stateVicinity -> stateVicinity != null) // Optional: filter out null values
                .collect(Collectors.joining(", "));

        String pharmacyVicinity = generalCouponDetails.stream()
                .map(GeneralCouponDetail::getBranch) // Get the Pharmacy object
                .filter(pharmacyBranch -> pharmacyBranch != null
                        && pharmacyBranch.getName() != null) // Filter out null pharmacies and names
                .map(PharmacyBranch::getName) // Extract the pharmacy name
                .collect(Collectors.joining(", ")); // Join names with a comma separator


        int countGenCouponDet = couponDetailRepository.countByGeneralCouponAndOrderNotNull(geneCoupon);

        GeneralCouponResponse genCouponRes = new GeneralCouponResponse();
        genCouponRes.setId(geneCoupon.getId());
        genCouponRes.setCouponTitle(geneCoupon.getCouponTitle());
        genCouponRes.setCouponType(geneCoupon.getCouponType());
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
        var user = userDetailsService.getAuthenticatedUser();
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
        } else {
            String intelRx = userDetailsService.getIntelRxId();
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
        var user = userDetailsService.getAuthenticatedUser();
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
        var user = userDetailsService.getAuthenticatedUser();
        if (utility.isNullOrEmpty(request.getCouponTitle())) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("couponTitle is required")
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
        } else if (request.getAmountPerPerson() <= 0) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("amountPerPerson must be greater than 0")
            );
        } else if (request.getCountPerPerson() <= 0) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("countPerPerson must be greater than 0")
            );
        } else if (request.getPharmacyBranchVicinityId() == null) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("pharmacyBranchVicinityId is required when couponVicinity is Pharmacy")
            );
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

    //@Transactional
    public void addToCouponHistory(CouponHistoryRequest request, Order order) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        var pharmacy = userDetailsService.getPharmacyInfo();
        var branch = userDetailsService.getBranch();

        String trxStatus;
        switch (request.getTrxType()) {
            case "credit":
                trxStatus = "Received";
                break;
            case "debit":
                trxStatus = "Withdraw";
                break;
            default:
                throw new IllegalArgumentException("Invalid transaction type: " + request.getTrxType());
        }

        CouponTransactionHistory transactionHistory = CouponTransactionHistory.builder()
                .intelRxId(intelRxId)
                .trxStatus(trxStatus)
                .amount(request.getAmount())
                .couponCode(request.getCouponCode())
                .trxBy(user)
                .description(request.getDescription())
                .trxCouponType(request.getTrxCouponType())  // BRAND, GENERAL, IN_HOUSE(Pharmacy)
                .trxType(request.getTrxType())
                .order(order)
                .createdAt(LocalDateTime.now())
                .build();

        var coupDet = transactionHistoryRepository.save(transactionHistory);

        GeneralCouponDetail couponDetails = couponDetailRepository.findFirstByCouponCodeAndOrderReferenceIsNull(
                request.getCouponCode()).orElse(null);

        if (couponDetails == null) {
            throw new IllegalArgumentException("Coupon details not found for code: " + request.getCouponCode());
        }


        if ("credit".equals(request.getTrxType())) {

            couponDetails.setCouponDetailStatus("expired");
            couponDetails.setDateUsed(LocalDate.now());
            couponDetails.setTimeUsed(LocalTime.now());
            couponDetails.setOrderReference(coupDet.getOrder());
            couponDetails.setBranch(branch);
            couponDetails.setPharmacyVicinity(pharmacy);

            couponDetailRepository.save(couponDetails);
            pharmacyWalletConfig.creditWallet(intelRxId, request.getAmount());
        } else if ("debit".equals(request.getTrxType())) {
            pharmacyWalletConfig.debitWallet(intelRxId, request.getAmount());
        }
    }

    public ResponseEntity<?> checkCouponCode(String couponCode, String couponType) {
        var user = userDetailsService.getAuthenticatedUser();
        var generalCoupon = generalCouponRepository.
                findByCouponCodeAndCouponType(couponCode, CouponType.valueOf(couponType)).orElse(null);
        if (generalCoupon == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error(
                    "Coupon not available"
            ));
        }
        var pharmacy = userDetailsService.getPharmacyInfo();
        GeneralCouponDetail couponDetails = couponDetailRepository.findFirstByCouponCodeAndOrderReferenceIsNull(
                couponCode).orElse(null);

        GeneralCouponResponse generalCouponResponse = new GeneralCouponResponse();
        if(couponDetails.getGeneralCoupon().getCouponStatus().equals("disabled")
            || couponDetails.getGeneralCoupon().getCouponStatus().equals("expired")) {
            generalCouponResponse.setId(couponDetails.getId());
            generalCouponResponse.setCouponType(couponDetails.getGeneralCoupon().getCouponType());
            generalCouponResponse.setCouponTitle(couponDetails.getGeneralCoupon().getCouponTitle());
            generalCouponResponse.setCouponCode(couponDetails.getCouponCode());
            generalCouponResponse.setAmountPerPerson(couponDetails.getGeneralCoupon().getAmountPerPerson());
        }
        return ResponseEntity.ok(StandardResponse.success(generalCouponResponse));
    }

    public CouponHistoryRequest mapToCouponHistory(CouponTransactionHistory transactionHistory) {
        // Check if the coupon type is IN_HOUSE and return null to filter it out
        if (transactionHistory.getTrxCouponType() != null &&
                transactionHistory.getTrxCouponType().equals(CouponType.IN_HOUSE)) {
            return null; // Exclude IN_HOUSE coupon types
        }

        CouponHistoryRequest couponHistoryRequest = new CouponHistoryRequest();
        couponHistoryRequest.setId(transactionHistory.getId());
        couponHistoryRequest.setOrderId(transactionHistory.getOrder().getId());
        couponHistoryRequest.setCouponCode(transactionHistory.getCouponCode());
        couponHistoryRequest.setIntelRxId(transactionHistory.getIntelRxId());
        couponHistoryRequest.setAmount(transactionHistory.getAmount());
        couponHistoryRequest.setDescription(transactionHistory.getDescription());
        couponHistoryRequest.setTrxRef(transactionHistory.getTrxRef());
        couponHistoryRequest.setTrxCouponType(transactionHistory.getTrxCouponType());
        couponHistoryRequest.setTrxStatus(transactionHistory.getTrxStatus());
        couponHistoryRequest.setCreatedAt(transactionHistory.getCreatedAt());

        return couponHistoryRequest;
    }

    public ResponseEntity<?> transactionHistory(
            LocalDateTime dateFrom, LocalDateTime dateTo,
            String trxCouponType, String trxType, String searchTerm, Pageable pageable
    ) {

        var intelRxId = userDetailsService.getIntelRxId();

        // Handle null values to construct dynamic queries
        Page<CouponTransactionHistory> transactionHistories;

        if (dateFrom == null && dateTo == null && trxCouponType == null && searchTerm == null) {
            transactionHistories = transactionHistoryRepository.findByIntelRxId(intelRxId, pageable);
        } else {
            transactionHistories = transactionHistoryRepository.findByDateRangeTrxCouponTypeAndSearchTerm(
                    intelRxId, dateFrom, dateTo, trxCouponType,trxType, searchTerm, pageable
            );
        }

        List<CouponHistoryRequest> filteredList = transactionHistories
                .map(this::mapToCouponHistory)
                .filter(Objects::nonNull) // Filter out null values
                .filter(couponHistoryRequest ->
                        !couponHistoryRequest.getTrxCouponType().equals(CouponType.IN_HOUSE)) // Exclude IN_HOUSE coupon types
                .toList(); // Convert stream to list

        Page<CouponHistoryRequest> responsePage = new PageImpl<>(filteredList, transactionHistories.getPageable(), transactionHistories.getTotalElements());

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> generalCouponStat() {
        var intelRxId = userDetailsService.getIntelRxId();
        var pharmacy = userDetailsService.getPharmacyInfo();
        Map<String, String> stringMap = new HashMap<>();

        var walletBalance = walletRepository.findByIntelRxId(intelRxId)
                .orElse(null);

        double balance = walletBalance == null ? 0.00 : walletBalance.getBalance();
        var geneCoupon = couponDetailRepository.findFirstByPharmacyVicinity(pharmacy)
                .orElse(null);

        if (geneCoupon == null) {

            stringMap.put("totalValueOfCouponUsed", String.format("%.2f", 0.00));
            stringMap.put("totalValueOfInHouseCouponUsed", String.format("%.2f", 0.00));
            stringMap.put("availableCoupons", String.valueOf(0));
            stringMap.put("usedCoupon", String.valueOf(0));
            stringMap.put("walletBalance", String.format("%.2f", balance));
        } else {

            //sales
            int usedCoupon = couponDetailRepository.countByGeneralCouponAndOrderNotNull(
                    geneCoupon.getGeneralCoupon() == null ? null : geneCoupon.getGeneralCoupon()
            );

            int availableCoupon = couponDetailRepository.countByGeneralCouponAndOrderNull(
                    geneCoupon.getGeneralCoupon() == null ? null : geneCoupon.getGeneralCoupon()
            );

            List<GeneralCouponDetail> generalCouponDetails = couponDetailRepository.findByGeneralCoupon(geneCoupon.getGeneralCoupon());

            double totalValueOfCouponUsed = 0.0;
            for (GeneralCouponDetail generalCouponDetail : generalCouponDetails) {
                if (generalCouponDetail.getOrderReference() != null) {
                    GeneralCoupon generalCoupon = generalCouponDetail.getGeneralCoupon();
                    totalValueOfCouponUsed += generalCoupon.getAmountPerPerson();
                }
            }

            double totalValueOfInHouseCouponUsed = 0.0;
            for (GeneralCouponDetail generalCouponDetail : generalCouponDetails) {
                if (generalCouponDetail.getOrderReference() != null &&
                        generalCouponDetail.getGeneralCoupon().getCouponType().equals(CouponType.IN_HOUSE)) {
                    GeneralCoupon generalCoupon = generalCouponDetail.getGeneralCoupon();
                    totalValueOfInHouseCouponUsed += generalCoupon.getAmountPerPerson();
                }
            }

            stringMap.put("totalValueOfCouponUsed", String.format("%.2f", totalValueOfCouponUsed));
            stringMap.put("totalValueOfInHouseCouponUsed", String.format("%.2f", totalValueOfInHouseCouponUsed));
            stringMap.put("availableCoupons", String.valueOf(availableCoupon));
            stringMap.put("usedCoupon", String.valueOf(usedCoupon));
            stringMap.put("walletBalance", String.format("%.2f", balance));
        }

        return ResponseEntity.ok(StandardResponse.success(stringMap));
    }


}
