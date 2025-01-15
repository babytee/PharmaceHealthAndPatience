package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.EmployeeService;
import com.pharmacy.intelrx.pharmacy.dto.employee.EmployeeFilterRequest;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/pharmacy"})
@RestController("AdminPharmacyEmployeeController")
public class EmployeeController {
    private final EmployeeService employeeService;
   // private final UserDetailsService userDetailsService;


    @GetMapping({"review/{employeeId}/{intelRxId}"})
    public ResponseEntity<?> reviewEmployeeDetails(
            @PathVariable Long employeeId,
            @PathVariable String intelRxId) {
        return employeeService.reviewEmployeeDetails(employeeId, intelRxId);
    }

    @GetMapping({"overview/{intelRxId}"})
    public ResponseEntity<?> employeeOverview(@PathVariable String intelRxId) {
        return employeeService.employeeOverview(intelRxId);
    }

    @GetMapping("/filter_employee")
    public ResponseEntity<?> filterEmployee(
            @RequestParam(name = "intelRxId", required = true) String intelRxId,
            @RequestParam(name = "employeeType", required = false) String employeeType,
            @RequestParam(name = "jobTitleId", required = false) Long jobTitleId,
            @RequestParam(name = "birthMonth", required = false) Integer birthMonth,
            @RequestParam(name = "workerStatus", required = false) String workerStatus,
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        // Now you can use the extracted parameters in your method logic
        EmployeeFilterRequest request = new EmployeeFilterRequest();
        request.setIntelRxId(intelRxId);
        request.setEmployeeType(employeeType);
        request.setJobTitleId(jobTitleId);
        request.setBirthMonth(birthMonth);
        request.setWorkerStatus(workerStatus);
        request.setBranchId(branchId);


        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);

        return employeeService.filterEmployee(request,pageable);
    }

    @GetMapping("/filter_branch_employee")
    public ResponseEntity<?> filterBranchEmployee(
            @RequestParam(name = "intelRxId", required = true) String intelRxId,
            @RequestParam(name = "employeeType", required = false) String employeeType,
            @RequestParam(name = "jobTitleId", required = false) Long jobTitleId,
            @RequestParam(name = "birthMonth", required = false) Integer birthMonth,
            @RequestParam(name = "workerStatus", required = false) String workerStatus,
            @RequestParam(name = "branchId", required = true) Long branchId,
            @RequestParam(name = "searchText", required = false)String searchText,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        // Now you can use the extracted parameters in your method logic
        EmployeeFilterRequest request = new EmployeeFilterRequest();
        request.setIntelRxId(intelRxId);
        request.setEmployeeType(employeeType);
        request.setJobTitleId(jobTitleId);
        request.setBirthMonth(birthMonth);
        request.setWorkerStatus(workerStatus);
        request.setBranchId(branchId);
        request.setSearchText(searchText);

        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);

        return employeeService.filterBranchEmployee(request,pageable);
    }

}

