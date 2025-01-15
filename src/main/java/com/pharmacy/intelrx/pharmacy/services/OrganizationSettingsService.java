package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.dto.CustomResponse;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.RefillDuration;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.*;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.RefillDurationRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.JobInformationRepository;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.Auxiliary;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyOrganizationSettingsService")
public class OrganizationSettingsService {

    private final PharmacyRepository pharmacyRepository;
    private final Utility utility;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PharmacyMapping pharmacyMapping;
    private final ContactInfoRepository contactInfoRepository;
    private final UserDetailsService userDetailsService;
    private final S3Service s3Service;
    private final BankInfoRepository bankInfoRepository;
    private final SalesMarginRepository salesMarginRepository;
    private final DiscountRepository discountRepository;
    private final DrugExpirationNotificationRepository drugExpirationNotificationRepository;
    private final OutOfStockNotificationRepository outOfStockNotificationRepository;
    private final RefillDurationRepository durationRepository;
    private final EmployeeRepository employeeRepository;
    private final Auxiliary auxiliary;
    private final JobInformationRepository jobInformationRepository;
    private final BranchEmployeeRepository branchEmployeeRepository;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public ResponseEntity<?> updatePharmacy(PharmacyRequest request) throws IOException {
        var user = userDetailsService.getAuthenticatedUser();
        var branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();


        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getPharmacyName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("pharmacyName is required"));
        } else if (utility.isNullOrEmpty(request.getPremiseNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("premiseNumber is required"));
        } else if (utility.isNullOrEmpty(Arrays.toString(request.getOtherNumber()))) {
            return ResponseEntity.badRequest().body(StandardResponse.error("otherNumber is required"));
        } else {

            Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
            if (!optionalUser.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("The logged in User not found"));
            }

            //Pharmacy Contact Request
            ContactInfoReqRes contactInfoReqRes = request.getContactInfoReqRes();
            ResponseEntity<?> contactInfoReqResResEntity = authService.ContactInfoReqRes(contactInfoReqRes);

            if (contactInfoReqResResEntity.getStatusCode() != HttpStatus.OK) {
                return authService.ContactInfoReqRes(contactInfoReqRes);
            }


            Optional<Pharmacy> pharmacyIntelRxId = pharmacyRepository.findByIntelRxId(intelRxId);

            if (!pharmacyIntelRxId.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy with this IntelRxId does not exist"));
            }


            Pharmacy pharmacy = pharmacyIntelRxId.get();

            String logo = null;
            if (!utility.isNullOrEmpty(request.getLogo())) {
                logo = s3Service.uploadFileDoc(request.getLogo(), "pharmacy");
            }else {
                logo = pharmacy.getPharmacyLogo();
            }

            pharmacy.setIntelRxId(intelRxId);
            pharmacy.setPharmacyName(request.getPharmacyName());
            pharmacy.setPharmacyLogo(logo == null ? null : logo);
            pharmacy.setOtherNumber(request.getOtherNumber());
            pharmacy.setPharmacyNumber(request.getPhoneNumber());
            pharmacy.setPremiseNumber(request.getPremiseNumber());
            pharmacy.setWebsite(request.getWebsite() == null ? null : request.getWebsite());
            pharmacy.setTwitter(request.getTwitter() == null ? null : request.getTwitter());
            pharmacy.setLinkedIn(request.getLinkedIn() == null ? null : request.getLinkedIn());
            pharmacy.setUpdatedAt(LocalDateTime.now());

            var pharm = pharmacyRepository.save(pharmacy);

            Optional<ContactInfo> optionalContactInfo = contactInfoRepository.findByPharmacyId(pharm.getId());
            if (!optionalContactInfo.isPresent()) {

            }
            //store the pharmacist contact address details
            ContactInfo contactInfo = optionalContactInfo.get();

            contactInfo.setCountry(contactInfoReqRes.getCountry());
            contactInfo.setState(contactInfoReqRes.getState());
            contactInfo.setCity(contactInfoReqRes.getCity());
            contactInfo.setLga(contactInfoReqRes.getLga());
            contactInfo.setStreetAddress(contactInfoReqRes.getStreetAddress());
            contactInfo.setPharmacy(pharm);
            contactInfoRepository.save(contactInfo);

            return ResponseEntity.ok().body(new CustomResponse("success", "Pharmacy details updated successfully"));
        }

    }

    public ResponseEntity<?> pharmacyDetails() throws IOException {
        String intelRxId = userDetailsService.getIntelRxId();

        Optional<Pharmacy> optional = pharmacyRepository.findByIntelRxId(intelRxId);

        if (!optional.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("No pharmacy with this intelRxId "));
        }

        Pharmacy pharmacy = optional.get();
        PharmacyBranchesResponse response = pharmacyMapping.mapToHeadQuarterDetails(pharmacy);

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    public ResponseEntity<?> getManagers() {
        var user = userDetailsService.getAuthenticatedUser();
        String employeeIntelRxId = userDetailsService.getIntelRxId();

        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        if (utility.isNullOrEmpty(employeeIntelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeIntelRxId is required"));
        }

        List<Employee> employeeList = employeeRepository.findByIntelRxIdAndJobTitleId(employeeIntelRxId, 5L);

        if (employeeList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.error("No employee record found"));
        }

        List<ManagerResponse> managerResponseList = pharmacyMapping.mapToPharmacyManagers(employeeList);


        return ResponseEntity.ok(StandardResponse.success(managerResponseList));
    }

    public ResponseEntity<?> getRoles() {
        var user = userDetailsService.getAuthenticatedUser();
        String employeeIntelRxId = userDetailsService.getIntelRxId();

        if (user == null) {
            return ResponseEntity.ok(StandardResponse.error("You are unauthorized"));
        }

        var Pharmacist = auxiliary.getJobTitle(1L);//Pharmacist
        var Cashier = auxiliary.getJobTitle(2L);//Cashier
        var SalesPerson = auxiliary.getJobTitle(3L);//Sales Person
        var Cleaner = auxiliary.getJobTitle(4L);//Cleaner
        var Manager = auxiliary.getJobTitle(5L);//Manager
        var Administrator = auxiliary.getJobTitle(6L);//Administrator
        var HR = auxiliary.getJobTitle(7L);//Administrator

//        List<Employee> employeeList = employeeRepository.findByIntelRxIdAndJobTitleId(employeeIntelRxId, Pharmacist.getId());
//        if (employeeList.isEmpty()) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("No employee record found"));
//        }

        RolesPermissionResponse pharmacistPermissionResponse = mapToRoles(Pharmacist, employeeIntelRxId);
        RolesPermissionResponse cashierPermissionResponse = mapToRoles(Cashier, employeeIntelRxId);
        RolesPermissionResponse salesPersonPermissionResponse = mapToRoles(SalesPerson, employeeIntelRxId);
        RolesPermissionResponse cleanerPermissionResponse = mapToRoles(Cleaner, employeeIntelRxId);
        RolesPermissionResponse managerPermissionResponse = mapToRoles(Manager, employeeIntelRxId);
        RolesPermissionResponse administratorPermissionResponse = mapToRoles(Administrator, employeeIntelRxId);
        RolesPermissionResponse hrPermissionResponse = mapToRoles(HR, employeeIntelRxId);


        List<RolesPermissionResponse> addRoles = new ArrayList<>();
        addRoles.add(pharmacistPermissionResponse);
        addRoles.add(cashierPermissionResponse);
        addRoles.add(salesPersonPermissionResponse);
        addRoles.add(cleanerPermissionResponse);
        addRoles.add(managerPermissionResponse);
        addRoles.add(administratorPermissionResponse);
        addRoles.add(hrPermissionResponse);


        return ResponseEntity.ok(StandardResponse.success(addRoles));
    }

    private RolesPermissionResponse mapToRoles(JobTitle jobTitle, String intelRxId) {

        List<Employee> employeeList = employeeRepository.findByIntelRxIdAndJobTitleId(intelRxId, jobTitle.getId());

        RolesPermissionResponse permissionResponse = new RolesPermissionResponse();
        permissionResponse.setRole(jobTitle.getName());

        PharmacyBranchesResponse branchesResponse = null;

        int totalRoleCount = 0;
        String designation = null;

        for (Employee employee : employeeList) {
            var jobInformationList = jobInformationRepository.findByJobTitle(employee.getJobInformation().getJobTitle());

            Optional<BranchEmployee> branchOptional = branchEmployeeRepository.findByEmployee(employee);
            if (branchOptional.isPresent()) {
                BranchEmployee branchEmployee = branchOptional.get();
                //designation = branchEmployee.getPharmacyBranch().getName();
                totalRoleCount = jobInformationList.size();
            } else {
                //designation = "HeadQuarters";
                totalRoleCount = jobInformationList.size();
            }
            JobInformation jobInformation = jobInformationList.get(0);
            var pharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
            branchesResponse = pharmacyMapping.mapToPharmacyRoles(pharmacy.get(), jobInformation.getJobTitle());
        }


        permissionResponse.setTotalRoleCount(totalRoleCount);
        permissionResponse.setDesignation(designation);
       permissionResponse.setDetails(branchesResponse);

        return permissionResponse;
    }


    public ResponseEntity<?> addNewBankInfo(BankInfoRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }
        if (utility.isNullOrEmpty(request.getBankName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("bankName is required"));
        }
        if (utility.isNullOrEmpty(request.getAccountName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("accountName is required"));
        }
        if (utility.isNullOrEmpty(request.getAccountNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("accountNumber is required"));
        }

        BankInfo bankInfo = new BankInfo();
        bankInfo.setIntelRxId(intelRxId);
        bankInfo.setBankName(request.getBankName());
        bankInfo.setAccountName(request.getAccountName());
        bankInfo.setAccountNumber(request.getAccountNumber());
        bankInfo.setCreatedAt(LocalDateTime.now());
        bankInfoRepository.save(bankInfo);


        return ResponseEntity.ok(StandardResponse.success("Bank Information added"));

    }

    public ResponseEntity<?> updateBankInfo(BankInfoRequest request, Long id) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }
        if (id == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("id is required"));
        }
        if (utility.isNullOrEmpty(request.getBankName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("bankName is required"));
        }
        if (utility.isNullOrEmpty(request.getAccountName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("accountName is required"));
        }
        if (utility.isNullOrEmpty(request.getAccountNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("accountNumber is required"));
        }


        Optional<BankInfo> optionalBankInfo = bankInfoRepository.findByIdAndIntelRxId(id, intelRxId);
        if (!optionalBankInfo.isPresent()) {
            return ResponseEntity.ok().body(StandardResponse.error("bank with this id(" + id + ") not found"));
        }

        BankInfo bankInfo = optionalBankInfo.get();
        bankInfo.setBankName(request.getBankName());
        bankInfo.setAccountName(request.getAccountName());
        bankInfo.setAccountNumber(request.getAccountNumber());
        bankInfo.setCreatedAt(LocalDateTime.now());
        bankInfoRepository.save(bankInfo);


        return ResponseEntity.ok(StandardResponse.success("Bank Information Updated"));

    }

    public ResponseEntity<?> getBankInfo() {
        String intelRxId = userDetailsService.getIntelRxId();

        List<BankInfo> bankInfos = bankInfoRepository.findByIntelRxId(intelRxId);

        if (bankInfos.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No BankInfo has been set"));
        }

        //BankInfo bankInfo = bankInfos.get();

        List<BankInfoRequest> response = bankInfos.stream().map(bankInfo -> mapToBankInfo(bankInfo)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    private BankInfoRequest mapToBankInfo(BankInfo bankInfo) {
        BankInfoRequest bankInfoRequest = BankInfoRequest.builder()
                .id(bankInfo.getId())
                .intelRxId(bankInfo.getIntelRxId())
                .bankName(bankInfo.getBankName())
                .accountName(bankInfo.getAccountName())
                .accountNumber(bankInfo.getAccountNumber())
                .build();

        return bankInfoRequest;
    }

    public ResponseEntity<?> manageSalesMargin(SalesMarginRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        Double grocery = request.getGroceryMargin() == 0 ? 0 : request.getGroceryMargin();
        Double medication = request.getMedicationMargin() == 0 ? 0 : request.getMedicationMargin();

        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("You are unauthorized"));
        }


        Optional<SalesMargin> optionalSalesMargin = salesMarginRepository.findByIntelRxId(intelRxId);
        SalesMargin salesMargin = null;
        if (optionalSalesMargin.isPresent()) {
            salesMargin = optionalSalesMargin.get();
            salesMargin.setGroceryMargin(grocery);
            salesMargin.setMedicationMargin(medication);
            salesMarginRepository.save(salesMargin);
        } else {
            salesMargin = new SalesMargin();
            salesMargin.setIntelRxId(intelRxId);
            salesMargin.setGroceryMargin(grocery);
            salesMargin.setMedicationMargin(medication);
            salesMargin.setCreatedAt(LocalDateTime.now());
            salesMarginRepository.save(salesMargin);
        }

        return ResponseEntity.ok(StandardResponse.success("Information Updated"));

    }

    public ResponseEntity<?> getSalesMargin() {
        String intelRxId = userDetailsService.getIntelRxId();

        Optional<SalesMargin> optional = salesMarginRepository.findByIntelRxId(intelRxId);

        if (!optional.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("No SalesMargin has been set"));
        }

        SalesMargin salesMargin = optional.get();

        SalesMarginRequest response = mapToSalesMargin(salesMargin);

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    private SalesMarginRequest mapToSalesMargin(SalesMargin salesMargin) {
        SalesMarginRequest salesMarginRequest = SalesMarginRequest.builder()
                .id(salesMargin.getId())
                .intelRxId(salesMargin.getIntelRxId())
                .groceryMargin(salesMargin.getGroceryMargin())
                .medicationMargin(salesMargin.getMedicationMargin())
                .build();

        return salesMarginRequest;
    }

    public ResponseEntity<?> manageDiscount(DiscountRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        Double grocery = request.getGroceryPercentage() == 0 ? 0 : request.getGroceryPercentage();
        Double medication = request.getMedicationPercentage() == 0 ? 0 : request.getMedicationPercentage();

        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("You are unauthorized"));
        }

        Optional<Discount> optionalDiscount = discountRepository.findByIntelRxId(intelRxId);
        Discount discount = null;
        if (optionalDiscount.isPresent()) {
            discount = optionalDiscount.get();
            discount.setGroceryPercentage(grocery);
            discount.setMedicationPercentage(medication);
            discount.setDiscountSwitch(request.isDiscountSwitch());
            discountRepository.save(discount);
        } else {
            discount = new Discount();
            discount.setGroceryPercentage(grocery);
            discount.setIntelRxId(intelRxId);
            discount.setMedicationPercentage(medication);
            discount.setDiscountSwitch(request.isDiscountSwitch());
            discount.setCreatedAt(LocalDateTime.now());
            discountRepository.save(discount);
        }

        return ResponseEntity.ok(StandardResponse.success("Discount Updated"));

    }

    public ResponseEntity<?> getDiscount() {
        String intelRxId = userDetailsService.getIntelRxId();

        Optional<Discount> optional = discountRepository.findByIntelRxId(intelRxId);

        if (!optional.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("No Discount has been set"));
        }

        Discount discount = optional.get();

        DiscountRequest response = mapToDiscount(discount);

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    private DiscountRequest mapToDiscount(Discount discount) {
        DiscountRequest discountRequest = DiscountRequest.builder()
                .id(discount.getId())
                .intelRxId(discount.getIntelRxId())
                .discountSwitch(discount.isDiscountSwitch())
                .medicationPercentage(discount.getMedicationPercentage())
                .groceryPercentage(discount.getGroceryPercentage())
                .build();

        return discountRequest;
    }


    public ResponseEntity<?> manageOutOfStockExpiration(OutOfStockNotificationRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        Integer grocery = request.getGrocery() == 0 ? 0 : request.getGrocery();
        Integer medication = request.getMedication() == 0 ? 0 : request.getMedication();

        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("You are unauthorized"));
        }

        Optional<OutOfStockNotification> optionalDiscount = outOfStockNotificationRepository.findByIntelRxId(intelRxId);
        OutOfStockNotification outOfStockNotification = null;
        if (optionalDiscount.isPresent()) {
            outOfStockNotification = optionalDiscount.get();
        } else {
            outOfStockNotification = new OutOfStockNotification();
        }

        outOfStockNotification.setGrocery(grocery);
        outOfStockNotification.setIntelRxId(intelRxId);
        outOfStockNotification.setMedication(medication);
        outOfStockNotification.setCheckNotification(request.getCheckNotification());
        outOfStockNotification.setCreatedAt(LocalDateTime.now());
        outOfStockNotificationRepository.save(outOfStockNotification);

        return ResponseEntity.ok(StandardResponse.success("Out Of Stock Notification Updated"));

    }

    public ResponseEntity<?> getOutOfStockExpiration() {
        String intelRxId = userDetailsService.getIntelRxId();

        Optional<OutOfStockNotification> optional = outOfStockNotificationRepository.findByIntelRxId(intelRxId);

        if (!optional.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("No Out Of Stock Expiration has been set"));
        }

        OutOfStockNotification outOfStockNotification = optional.get();

        OutOfStockNotificationRequest response = mapToOutOfStockExpiration(outOfStockNotification);

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    private OutOfStockNotificationRequest mapToOutOfStockExpiration(OutOfStockNotification notification) {
        return OutOfStockNotificationRequest.builder()
                .id(notification.getId())
                .intelRxId(notification.getIntelRxId())
                .checkNotification(notification.isCheckNotification())
                .grocery(notification.getGrocery())
                .medication(notification.getMedication())
                .build();

    }


    public ResponseEntity<?> manageDrugExpiration(DrugExpirationNotificationRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("You are unauthorized"));
        }

        Optional<DrugExpirationNotification> optional = drugExpirationNotificationRepository.findByIntelRxId(intelRxId);
        DrugExpirationNotification drugExpirationNotification = null;

        Optional<RefillDuration> optionalRefillDuration = durationRepository.findById(request.getFrequencyId());
        RefillDuration refillDuration = optionalRefillDuration.get();

        if (optional.isPresent()) {
            drugExpirationNotification = optional.get();
        } else {
            drugExpirationNotification = new DrugExpirationNotification();
        }

        drugExpirationNotification.setCheckNotification(
                request.getCheckNotification());
        drugExpirationNotification.setFrequency(refillDuration);
        drugExpirationNotification.setIntelRxId(intelRxId);
        drugExpirationNotification.setCreatedAt(LocalDateTime.now());
        drugExpirationNotificationRepository.save(drugExpirationNotification);

        return ResponseEntity.ok(StandardResponse.success("Drug Expiration Notification Updated"));

    }

    public ResponseEntity<?> getDrugExpiration() {
        String intelRxId = userDetailsService.getIntelRxId();

        Optional<DrugExpirationNotification> optional = drugExpirationNotificationRepository.findByIntelRxId(intelRxId);

        if (!optional.isPresent()) {
            return ResponseEntity.ok(StandardResponse.error("No Discount has been set"));
        }

        DrugExpirationNotification drugExpirationNotification = optional.get();

        DrugExpirationNotificationRequest response = mapToDrugExpiration(drugExpirationNotification);

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    private DrugExpirationNotificationRequest mapToDrugExpiration(DrugExpirationNotification drugExpirationNotification) {
        return DrugExpirationNotificationRequest.builder()
                .id(drugExpirationNotification.getId())
                .intelRxId(drugExpirationNotification.getIntelRxId())
                .checkNotification(drugExpirationNotification.isCheckNotification())
                .frequency(drugExpirationNotification.getFrequency())
                .build();
    }

}
