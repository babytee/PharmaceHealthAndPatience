package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.JobInformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PharmacyMapping {

    private final PharmacyBranchRepository branchRepository;
    private final BranchEmployeeRepository branchEmployeeRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final EmployeeRepository employeeRepository;
    private final PatientRepository patientRepository;
    private final JobInformationRepository jobInformationRepository;
    private final FilterEmployee filterEmployee;
    private final S3Service s3Service;
    private final InventoryRepository inventoryRepository;

    public PharmacyBranchesResponse mapToPharmacyHeadQuarters(Pharmacy pharmacy) {

        PharmacyBranchesResponse response = new PharmacyBranchesResponse();

        PharmacyRequest pharmacyRequest = new PharmacyRequest();
        pharmacyRequest.setId(pharmacy.getId());
        pharmacyRequest.setPharmacyName(pharmacy.getPharmacyName());
        pharmacyRequest.setPharmacyOwner(pharmacy.getPharmacyOwner());
        pharmacyRequest.setPhoneNumber(pharmacy.getPharmacyNumber());
        pharmacyRequest.setPremiseNumber(pharmacy.getPremiseNumber());
        pharmacyRequest.setIntelRxId(pharmacy.getIntelRxId());
        pharmacyRequest.setPharmacistCategory(pharmacy.getPharmacistCategory());

        Optional<ContactInfo> contactInfo = contactInfoRepository.findByPharmacyId(pharmacy.getId());

        if (contactInfo.isPresent()) {
            ContactInfoReqRes contactInfoReqRes = mapToContactInfo(contactInfo.get());
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
        }

        List<PharmacyBranch> pharmacyBranchList = branchRepository.findAllByIntelRxId(pharmacy.getIntelRxId());

        List<PharmacyBranchResponse> responseList = pharmacyBranchList.stream().map(
                branch -> (mapToBranchResponse(branch))
        ).collect(Collectors.toList());

        response.setHeadQuarters(pharmacyRequest);
        response.setBranches(responseList);

        return response;
    }

    public List<ManagerResponse> mapToPharmacyManagers(List<Employee> employees) {
        Map<String, ManagerResponse> branchNameToManagerResponseMap = new HashMap<>();

        for (Employee employee : employees) {
            Optional<BranchEmployee> branchEmployeeOptional = branchEmployeeRepository.findByEmployee(employee);

            if (branchEmployeeOptional.isPresent()) {
                // Employee is a manager on a branch
                BranchEmployee branchEmployee = branchEmployeeOptional.get();
                String branchName = branchEmployee.getPharmacyBranch().getName();

                ManagerResponse managerResponse = branchNameToManagerResponseMap.computeIfAbsent(branchName, key -> {
                    ManagerResponse newManagerResponse = createManagerResponse(employee, branchEmployee);
                    return newManagerResponse;
                });

                // Update other properties if needed

                // Accumulate counts for each designation
                managerResponse.setEmployeesManaged(managerResponse.getEmployeesManaged() + 1);

                // Put the updated managerResponse back into the map
                branchNameToManagerResponseMap.put(branchName, managerResponse);
            } else {
                // Employee is not attached to any branch, check if it is the headquarters
                Optional<PharmacyBranch> headquartersOptional = branchRepository.findByEmployeeId(employee.getId());

                if (headquartersOptional.isPresent()) {
                    // Employee is a manager at the headquarters
                    PharmacyBranch headquarters = headquartersOptional.get();
                    String branchName = headquarters.getName();

                    ManagerResponse managerResponse = branchNameToManagerResponseMap.computeIfAbsent(branchName, key -> {
                        ManagerResponse newManagerResponse = createManagerResponse(employee, null);
                        return newManagerResponse;
                    });

                    // Update other properties if needed

                    // Accumulate counts for each designation
                    managerResponse.setEmployeesManaged(managerResponse.getEmployeesManaged() + 1);

                    // Put the updated managerResponse back into the map
                    branchNameToManagerResponseMap.put(branchName, managerResponse);
                }
            }
        }

        return new ArrayList<>(branchNameToManagerResponseMap.values());
    }

    private ManagerResponse createManagerResponse(Employee employee, BranchEmployee branchEmployee) {
        ManagerResponse newManagerResponse = new ManagerResponse();
        String fullName = employee.getUser().getLastName() + " " + employee.getUser().getFirstName();
        newManagerResponse.setFullName(fullName);
        newManagerResponse.setEmail(employee.getUser().getEmail());

        if (branchEmployee != null) {
            newManagerResponse.setDesignation(branchEmployee.getPharmacyBranch().getName());
            newManagerResponse.setRegDate(branchEmployee.getPharmacyBranch().getCreatedAt());
        } else {
            newManagerResponse.setDesignation("HeadQuarters");
            // Set other properties for HeadQuarters if needed
        }

        // Initialize employeeManaged to 1 for the current employee
        newManagerResponse.setEmployeesManaged(1);

        return newManagerResponse;
    }

    public PharmacyRequest mapToPharmacy(Pharmacy pharmacy) {
        String logo = String.valueOf(s3Service.fetchImage(pharmacy.getPharmacyLogo()));
        PharmacyRequest pharmacyRequest = new PharmacyRequest();
        pharmacyRequest.setId(pharmacy.getId());
        pharmacyRequest.setPharmacyName(pharmacy.getPharmacyName());
        pharmacyRequest.setPharmacyOwner(pharmacy.getPharmacyOwner());
        pharmacyRequest.setPhoneNumber(pharmacy.getPharmacyNumber());
        pharmacyRequest.setOtherNumber(pharmacy.getOtherNumber());
        pharmacyRequest.setLogo(logo);
        pharmacyRequest.setPremiseNumber(pharmacy.getPremiseNumber());
        pharmacyRequest.setPharmacistCategory(pharmacy.getPharmacistCategory());
        pharmacyRequest.setIntelRxId(pharmacy.getIntelRxId());
        pharmacyRequest.setLinkedIn(pharmacy.getLinkedIn());
        pharmacyRequest.setTwitter(pharmacy.getTwitter());
        pharmacyRequest.setWebsite(pharmacy.getWebsite());
        pharmacyRequest.setRegBy(pharmacy.getRegBy() == null ? "Self Registered" : pharmacy.getRegBy());
        pharmacyRequest.setSubscriptionStatus(pharmacy.getSubscriptionStatus() == null ? "Freemium" : pharmacy.getSubscriptionStatus());
        pharmacyRequest.setCreatedAt(pharmacy.getCreatedAt());

        Optional<ContactInfo> contactInfo = contactInfoRepository.findByPharmacyId(pharmacy.getId());

        if (contactInfo.isPresent()) {
            ContactInfoReqRes contactInfoReqRes = mapToContactInfo(contactInfo.get());
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
        }else{
            ContactInfoReqRes contactInfoReqRes = new ContactInfoReqRes();
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
        }


        return pharmacyRequest;
    }

    public PharmacyBranchesResponse mapToHeadQuarterDetails(Pharmacy pharmacy) {
        PharmacyBranchesResponse response = new PharmacyBranchesResponse();

        String logo = null;

        if (pharmacy.getPharmacyLogo() != null) {
            S3Service.FetchedImage fetchedImage = s3Service.fetchImage(pharmacy.getPharmacyLogo()); // Replace "your_image_name.jpg" with the actual image name
            logo = fetchedImage.getImageUrl();
        }


        PharmacyRequest pharmacyRequest = new PharmacyRequest();
        pharmacyRequest.setId(pharmacy.getId());
        pharmacyRequest.setPharmacyName(pharmacy.getPharmacyName());
        pharmacyRequest.setPharmacyOwner(pharmacy.getPharmacyOwner());
        pharmacyRequest.setPhoneNumber(pharmacy.getPharmacyNumber());
        pharmacyRequest.setOtherNumber(pharmacy.getOtherNumber());
        pharmacyRequest.setLogo(logo);
        pharmacyRequest.setPremiseNumber(pharmacy.getPremiseNumber());
        pharmacyRequest.setPharmacistCategory(pharmacy.getPharmacistCategory());
        pharmacyRequest.setIntelRxId(pharmacy.getIntelRxId());
        pharmacyRequest.setLinkedIn(pharmacy.getLinkedIn());
        pharmacyRequest.setTwitter(pharmacy.getTwitter());
        pharmacyRequest.setWebsite(pharmacy.getWebsite());
        pharmacyRequest.setRegBy(pharmacy.getRegBy() == null ? "Self Registered" : pharmacy.getRegBy());
        pharmacyRequest.setSubscriptionStatus(pharmacy.getSubscriptionStatus() == null ? "Freemium" : pharmacy.getSubscriptionStatus());
        pharmacyRequest.setCreatedAt(pharmacy.getCreatedAt());

        Optional<ContactInfo> contactInfo = contactInfoRepository.findByPharmacyId(pharmacy.getId());

        if (contactInfo.isPresent()) {
            ContactInfoReqRes contactInfoReqRes = mapToContactInfo(contactInfo.get());
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
        }

        List<PharmacyBranch> pharmacyBranchList = branchRepository.findAllByIntelRxId(pharmacy.getIntelRxId());

        List<Employee> employeeList = employeeRepository.findAllByEmployeeIntelRxId(pharmacy.getIntelRxId());

        List<Patient> patientList = patientRepository.findAllByIntelRxId(pharmacy.getIntelRxId());

        List<Inventory> medInventoryList = inventoryRepository.findAllByIntelRxIdAndInventoryTypeAndStatus(pharmacy.getIntelRxId(), "MEDICATION", true);

        List<Inventory> groceryInventoryList = inventoryRepository.findAllByIntelRxIdAndInventoryTypeAndStatus(pharmacy.getIntelRxId(), "GROCERY", true);

        response.setHeadQuarters(pharmacyRequest);
        response.setTotalBranches(pharmacyBranchList.size());
        response.setTotalEmployees(employeeList.size());
        response.setTotalPatients(patientList.size());
        response.setTotalMedications(medInventoryList.size());
        response.setTotalGrocery(groceryInventoryList.size());

        return response;
    }

    public PharmacyBranchesResponse mapToPharmacyRoles(Pharmacy pharmacy, JobTitle jobTitle) {
        PharmacyBranchesResponse response = new PharmacyBranchesResponse();
        List<Employee> employeeList = employeeRepository.findByIntelRxIdAndJobTitleId(pharmacy.getIntelRxId(), jobTitle.getId());


        List<RolesPermissionResponse> responseList = employeeList.stream()
                .map(employee -> {
                    RolesPermissionResponse branchPermissionResponse = new RolesPermissionResponse();
                    if (employee.getBranchEmployee() != null) {
                        BranchEmployee branchEmployee = employee.getBranchEmployee().get(0);
                        var jobInformationList = jobInformationRepository.findAllByJobTitleAndEmployee(branchEmployee.getEmployee().getJobInformation().getJobTitle(), branchEmployee.getEmployee());
                        branchPermissionResponse.setTotalRoleCount(jobInformationList.size());
                        branchPermissionResponse.setDesignation(branchEmployee.getPharmacyBranch().getName());
                    }
                    return branchPermissionResponse;
                })
                .filter(res -> res.getTotalRoleCount() > 0) // Filter to include only places with employees
                .collect(Collectors.groupingBy(RolesPermissionResponse::getDesignation,
                        Collectors.collectingAndThen(
                                Collectors.reducing((a, b) -> {
                                    RolesPermissionResponse result = new RolesPermissionResponse();
                                    result.setTotalRoleCount(a.getTotalRoleCount() + b.getTotalRoleCount());
                                    result.setDesignation(a.getDesignation());
                                    return result;
                                }),
                                java.util.Optional::get)))
                .values()
                .stream()
                .collect(Collectors.toList());


        //HeadQuarters
        RolesPermissionResponse pharmacyPermissionResponse = new RolesPermissionResponse();
        pharmacyPermissionResponse.setDesignation("HeadQuarters");

        employeeList.stream()
                .filter(employee -> employee.getBranchEmployee() == null)
                .forEach(employee -> {
                    var jobInformationList = jobInformationRepository.findAllByJobTitleAndEmployee(employee.getJobInformation().getJobTitle(), employee);
                    int totalRoleCount = jobInformationList.size();
                    pharmacyPermissionResponse.setTotalRoleCount(pharmacyPermissionResponse.getTotalRoleCount() != null ?
                            pharmacyPermissionResponse.getTotalRoleCount() + totalRoleCount : totalRoleCount);
                    pharmacyPermissionResponse.setDesignation("HeadQuarters"); // Set the designation for each iteration
                });

        response.setHeadQuarters(pharmacyPermissionResponse);
        response.setBranches(responseList);

        return response;
    }

    private ContactInfoReqRes mapToContactInfo(ContactInfo contactInfo) {
        ContactInfoReqRes request = new ContactInfoReqRes();

        //ContactInfo contactInfo = optional.get();
        request.setId(contactInfo.getId());
        request.setCountry(contactInfo.getCountry());
        request.setState(contactInfo.getState() != null ? contactInfo.getState() : null);
        request.setCity(contactInfo.getCity() != null ? contactInfo.getCity() : null);
        request.setLga(contactInfo.getLga() != null ? contactInfo.getLga() : null);
        request.setStreetAddress(contactInfo.getStreetAddress() != null ? contactInfo.getStreetAddress() : null);
        request.setZipCode(contactInfo.getZipCode() != null ? contactInfo.getZipCode() : null);


        return request;

    }

    public PharmacyBranchResponse mapToBranchResponse(PharmacyBranch pharmacyBranch) {
        PharmacyBranchResponse branchResponse = new PharmacyBranchResponse();
        branchResponse.setId(pharmacyBranch.getId());
        branchResponse.setName(pharmacyBranch.getName());
        branchResponse.setBgColor(pharmacyBranch.getBgColor());
        branchResponse.setPharmacyIntelRxId(pharmacyBranch.getPharmacy().getIntelRxId());

        PharmacyRequest pharmacyRequest = new PharmacyRequest();
        pharmacyRequest.setId(pharmacyBranch.getPharmacy().getId());
        pharmacyRequest.setIntelRxId(pharmacyBranch.getPharmacy().getIntelRxId());
        pharmacyRequest.setPharmacyName(pharmacyBranch.getPharmacy().getPharmacyName());

        Optional<ContactInfo> contactInfo = contactInfoRepository.findByPharmacyBranchId(pharmacyBranch.getId());

        if (contactInfo.isPresent()) {
            ContactInfoReqRes contactInfoReqRes = mapToContactInfo(contactInfo.get());
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
        }

        var employeeInfo = branchEmployeeRepository.findByPharmacyBranchAndEmployee(pharmacyBranch, pharmacyBranch.getEmployee());

        BranchEmployeeDTO branchEmployeeDTO = new BranchEmployeeDTO();
        BranchEmployee branchEmployee = employeeInfo.get();
        branchEmployeeDTO.setId(branchEmployee.getId());
        branchEmployeeDTO.setEmployeeId(branchEmployee.getEmployee().getId());
        branchEmployeeDTO.setTitle(branchEmployee.getEmployee().getJobInformation().getJobTitle().getName());

        String firstName = branchEmployee.getEmployee().getUser().getFirstName();
        String lastName = branchEmployee.getEmployee().getUser().getLastName();
        String fullName = lastName + " " + firstName;

        branchEmployeeDTO.setName(fullName);

        branchResponse.setManagerInfo(branchEmployeeDTO);
        branchResponse.setPharmacyInfo(pharmacyRequest);

        return branchResponse;
    }

    public PharmacyBranchResponse mapToTransferBranchResponse(PharmacyBranch pharmacyBranch) {
        PharmacyBranchResponse branchResponse = new PharmacyBranchResponse();
        branchResponse.setId(pharmacyBranch.getId());
        branchResponse.setName(pharmacyBranch.getName());
        branchResponse.setBgColor(pharmacyBranch.getBgColor());
        branchResponse.setPharmacyIntelRxId(pharmacyBranch.getPharmacy().getIntelRxId());

        PharmacyRequest pharmacyRequest = new PharmacyRequest();
        pharmacyRequest.setId(pharmacyBranch.getPharmacy().getId());
        pharmacyRequest.setIntelRxId(pharmacyBranch.getPharmacy().getIntelRxId());
        pharmacyRequest.setPharmacyName(pharmacyBranch.getPharmacy().getPharmacyName());

        Optional<ContactInfo> contactInfo = contactInfoRepository.findByPharmacyBranchId(pharmacyBranch.getId());

        if (contactInfo.isPresent()) {
            ContactInfoReqRes contactInfoReqRes = mapToContactInfo(contactInfo.get());
            pharmacyRequest.setContactInfoReqRes(contactInfoReqRes);
        }

        return branchResponse;
    }
}
