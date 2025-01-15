package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.FilterSalesRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.EmployeeFilterRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.SalaryPaymentRequest;
import com.pharmacy.intelrx.pharmacy.services.SalaryPaymentService;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("PharmacySalaryPaymentController")

public class SalaryPaymentController {
    private final SalaryPaymentService salaryPaymentService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/pay_salary")
    public ResponseEntity<?> paySalary(@RequestBody SalaryPaymentRequest request) throws IOException {
        return salaryPaymentService.paySalary(request);
    }

    @GetMapping("/salary_payment_history")
    public ResponseEntity<?> paymentHistory(
            @RequestParam(name = "employeeId") Long employeeId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        // Adjust the page number if it is 1
        int adjustedPage = (page <= 1) ? 0 : page - 1;

        // Pass the pagination parameters to the service method
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);

        return salaryPaymentService.paymentHistory(employeeId, pageable);
    }

    @GetMapping("/payment_history_stat/{employeeId}")
    public ResponseEntity<?> paymentHistoryStat(@PathVariable Long employeeId) {
        return salaryPaymentService.paymentHistoryStat(employeeId);
    }

    @GetMapping("/salary_stat")
    public ResponseEntity<?> salaryStat() {
        return salaryPaymentService.salaryStat();
    }


    @GetMapping("/filter_employee_salary")
    public ResponseEntity<?> filterEmployee(
            //@RequestParam(name = "intelRxId", required = true) String intelRxId,
            @RequestParam(name = "employeeType", required = false) String employeeType,
            @RequestParam(name = "salaryStatus", required = false) String salaryStatus,
            @RequestParam(name = "salaryTypeId", required = false) Long salaryTypeId,
            @RequestParam(name = "jobTitleId", required = false) Long jobTitleId,
            @RequestParam(name = "birthMonth", required = false) Integer birthMonth,
            @RequestParam(name = "workerStatus", required = false) String workerStatus,
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        // Now you can use the extracted parameters in your method logic
        EmployeeFilterRequest request = new EmployeeFilterRequest();
        request.setIntelRxId(intelRxId);
        request.setEmployeeType(employeeType);
        request.setSalaryStatus(salaryStatus);
        request.setSalaryTypeId(salaryTypeId);
        request.setJobTitleId(jobTitleId);
        request.setBirthMonth(birthMonth);
        request.setWorkerStatus(workerStatus);
        request.setBranchId(branch == null ? branchId : branch.getId());


        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);

        return salaryPaymentService.filterEmployeeSalary(request, pageable);
    }
}
