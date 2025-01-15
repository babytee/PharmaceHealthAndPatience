package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.employee.*;
import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.JobTitleRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.JobInformationRepository;
import com.pharmacy.intelrx.pharmacy.services.PharmacyBranchService;
import com.pharmacy.intelrx.pharmacy.utility.*;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service("AdminPharmacyEmployeeService")
public class EmployeeService {
    private final Utility utility;
    private final EmployeeRepository employeeRepository;
    private final FilterEmployee filterEmployee;
    private final JobInformationRepository jobInformationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyBranchRepository branchRepository;

    public ResponseEntity<?> reviewEmployeeDetails(Long employeeId,String intelRxId) {
        try {
            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, intelRxId);
            Employee employee = null;
            if (!optionalEmployee.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employee not found or information not available"));
            }

            employee = optionalEmployee.get();

            if (!employee.getEmployeeIntelRxId().equals(intelRxId)) {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
            }

            EmployeeResponse employeeResponse = filterEmployee.mapToEmployeeResponse(employee);

            return ResponseEntity.ok(StandardResponse.success(employeeResponse));
        } catch (Exception e) {
            // Handle the exception according to your requirements
            e.printStackTrace(); // Log the exception or handle it in a more appropriate way
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("An error occurred: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> employeeOverview(String intelRxId) {

        if (utility.isNullOrEmpty(intelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }

        List<Employee> employeeList = employeeRepository.findAllByEmployeeIntelRxId(intelRxId);
        int cleaner = 0;
        int pharmacists = 0;
        int cashiers = 0;
        int salesPersons = 0;
        int managers = 0;
        int administrator = 0;

        if (employeeList.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("The Pharmacy or employee with that IntelRxId does not exist"));
        }

        for (Employee employee : employeeList) {
            // Employee employee = null;
            List<JobInformation> jobInformationList = jobInformationRepository.findAllByEmployeeId(employee.getId());

            for (JobInformation jobInformation : jobInformationList) {
                //allJobTitleList = jobTitleRepository.findAll();
                if (jobInformation.getJobTitle().getId() == 1) {
                    var jobTitle = jobInformationRepository.findByJobTitle(jobInformation.getJobTitle());
                    pharmacists = jobTitle.size();
                }
                if (jobInformation.getJobTitle().getId() == 2) {
                    var jobTitle = jobInformationRepository.findByJobTitle(jobInformation.getJobTitle());
                    cashiers = jobTitle.size();
                }
                if (jobInformation.getJobTitle().getId() == 3) {
                    var jobTitle = jobInformationRepository.findByJobTitle(jobInformation.getJobTitle());
                    salesPersons = jobTitle.size();
                }
                if (jobInformation.getJobTitle().getId() == 4) {
                    var jobTitle = jobInformationRepository.findByJobTitle(jobInformation.getJobTitle());
                    cleaner = jobTitle.size();
                }
                if (jobInformation.getJobTitle().getId() == 5) {
                    var jobTitle = jobInformationRepository.findByJobTitle(jobInformation.getJobTitle());
                    managers = jobTitle.size();
                }
                if (jobInformation.getJobTitle().getId() == 6) {
                    var jobTitle = jobInformationRepository.findByJobTitle(jobInformation.getJobTitle());
                    administrator = jobTitle.size();
                }



            }
        }

        OverviewResponse overviewResponse = new OverviewResponse();

        overviewResponse.setTotalEmployees(employeeList.size());
        overviewResponse.setPharmacists(pharmacists);
        overviewResponse.setCashiers(cashiers);
        overviewResponse.setSalesPersons(salesPersons);
        overviewResponse.setManagers(managers);
        overviewResponse.setAdministrator(administrator);
        overviewResponse.setCleaner(cleaner);

        return ResponseEntity.ok(StandardResponse.success(overviewResponse));

    }

    public ResponseEntity<?> filterEmployee(EmployeeFilterRequest request, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(request.getIntelRxId());
        if (optionalPharmacy == null || optionalPharmacy.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No Pharmacy with this intelRxId: " + request.getIntelRxId()));
        }
        Page<EmployeeResponse> employeeResponseList = null;

        employeeResponseList = filterEmployee.FilterEmployeeInfo(request,pageable);

        if (employeeResponseList == null) {
            return ResponseEntity.ok(StandardResponse.success("No record found"));
        } else {
            return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
        }
    }

    public ResponseEntity<?> filterBranchEmployee(EmployeeFilterRequest request, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(request.getIntelRxId());
        if (optionalPharmacy == null || optionalPharmacy.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No Pharmacy with this intelRxId: " + request.getIntelRxId()));
        }
        Optional<PharmacyBranch> optionalBranch = branchRepository.findByIdAndIntelRxId(request.getBranchId(), request.getIntelRxId());
        if (optionalBranch == null || optionalBranch.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No Pharmacy Branch with this branchId: " + request.getBranchId()));
        }
//        List<BranchEmployee> branchEmployeeList = branchEmployeeRepository.findAllByPharmacyBranchId(request.getBranchId());
//        if (branchEmployeeList == null || branchEmployeeList.isEmpty()) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("No Pharmacy Branch with this branchId: " + request.getBranchId()));
//        }
        Page<EmployeeResponse> employeeResponseList = null;

        employeeResponseList = filterEmployee.FilterEmployeeInfo(request,pageable);

        if (employeeResponseList == null) {
            return ResponseEntity.ok(StandardResponse.success("No record found"));
        } else {
            return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
        }
    }

}

