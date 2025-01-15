package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.dto.employee.*;
import com.pharmacy.intelrx.pharmacy.services.AssignShiftService;
import com.pharmacy.intelrx.pharmacy.services.EmployeeService;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("pharmacyEmployeeController")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final UserDetailsService userDetailsService;
    private final AssignShiftService assignShiftService;

    @PostMapping({"add_employee"})
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeRequest request) {
        return employeeService.addEmployee(request);
    }

    @PatchMapping({"update_user/{employeeId}"})
    public ResponseEntity<?> updateUser(@RequestBody UserRequest request, @PathVariable Long employeeId) {
        return employeeService.updateUser(request, employeeId);
    }

    @PatchMapping({"update_contact/{employeeId}"})
    public ResponseEntity<?> updateContact(@RequestBody ContactInfoReqRes request, @PathVariable Long employeeId) {
        return employeeService.updateContact(request, employeeId);
    }

    @PatchMapping({"Job_information/{employeeId}"})
    public ResponseEntity<?> updateJobInformation(@RequestBody JobInformationRequest request, @PathVariable Long employeeId) {
        return employeeService.updateJobInformation(request, employeeId);
    }

    @PatchMapping({"update_compensation_detail/{employeeId}"})
    public ResponseEntity<?> updateCompensationDetail(@RequestBody CompensationDetailRequest request, @PathVariable Long employeeId) {
        return employeeService.updateCompensationDetail(request, employeeId);
    }

    @PatchMapping({"update_education/{employeeId}"})
    public ResponseEntity<?> updateEducation(@RequestBody EducationRequest request,@PathVariable Long employeeId) {
        return employeeService.updateEducation(request,employeeId);
    }

    @PatchMapping({"update_legal/{employeeId}"})
    public ResponseEntity<?> updateLegal(@RequestBody LegalRequest request,@PathVariable Long employeeId) {
        return employeeService.updateLegal(request, employeeId);
    }

    @PatchMapping({"update_benefit/employeeId"})
    public ResponseEntity<?> updateBenefit(@RequestBody BenefitRequest request, @PathVariable Long employeeId) {
        return employeeService.updateBenefit(request,employeeId);
    }

    @PatchMapping({"update_extra_information/{employeeId}"})
    public ResponseEntity<?> updateExtraInformation(@RequestBody ExtraInformationRequest request,@PathVariable Long employeeId) {
        return employeeService.updateExtraInformation(request,employeeId);
    }

    @PatchMapping({"update_employee_document/{employeeId}"})
    public ResponseEntity<?> updateEmployeeDocument(@RequestBody List<EmployeeDocumentRequest> request, @PathVariable Long employeeId) {
        return employeeService.updateEmployeeDocument(request,employeeId);
    }

    @GetMapping({"review/{employeeId}"})
    public ResponseEntity<?> reviewEmployeeDetails(@PathVariable Long employeeId) {
        return employeeService.reviewEmployeeDetails(employeeId);
    }

    @GetMapping({"employee_details"})
    public ResponseEntity<?> employeeDetails() {
        return employeeService.employeeDetails();
    }

    @GetMapping({"get_conclude_employee/{employeeId}"})
    public ResponseEntity<?> getConcludeEmployee(@PathVariable Long employeeId) {
        return employeeService.getConcludeEmployee(employeeId);
    }

    @PatchMapping({"approved_employee/{employeeId}"})
    public ResponseEntity<?> approvedEmployee(@PathVariable Long employeeId) {
        return employeeService.approvedEmployee(employeeId);
    }

    @GetMapping({"app_access/{employeeId}/{jobTitleId}"})
    public ResponseEntity<?> appAccess(@PathVariable Long employeeId, @PathVariable Long jobTitleId) {
        return employeeService.appAccess(employeeId, jobTitleId);
    }

    @PostMapping({"transfer_employee"})
    public ResponseEntity<?> transferEmployee(@RequestBody TransferEmployeeRequest request) {
        return employeeService.transferEmployee(request);
        //return ResponseEntity.ok(StandardResponse.error(request.getCurrentBranchId()+" "+request.getNewBranchId()));
    }

    @PostMapping({"conclude_employee"})
    public ResponseEntity<?> concludeEmployee(@RequestBody ConcludeEmployeeRequest request) {
        return employeeService.concludeEmployee(request);
    }

    @DeleteMapping({"delete_employee"})
    public ResponseEntity<?> deleteEmployee(@RequestParam Long employeeId, @RequestParam String password) {
        return employeeService.deleteEmployee(employeeId, password);
    }

    @GetMapping({"overview"})
    public ResponseEntity<?> employeeOverview() {
        return employeeService.employeeOverview();
    }

    @GetMapping({"get_managers"})
    public ResponseEntity<?> getManagers() {
        return employeeService.getManagers();
    }

    @GetMapping({"all_employee"})
    public ResponseEntity<?> allEmployee() {
        return employeeService.allEmployee();
    }

    @GetMapping("/filter_employee")
    public ResponseEntity<?> filterEmployee(
            //@RequestParam(name = "intelRxId", required = true) String intelRxId,
            @RequestParam(name = "employeeType", required = false) String employeeType,
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
        request.setSalaryTypeId(salaryTypeId);
        request.setJobTitleId(jobTitleId);
        request.setBirthMonth(birthMonth);
        request.setWorkerStatus(workerStatus);
        request.setBranchId(branch == null ? branchId : branch.getId());


        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);

        return employeeService.filterEmployee(request, pageable);
    }

    @PostMapping({"assign_shift"})
    public ResponseEntity<?> assignShift(@RequestBody AssignShiftRequest request) {
        return assignShiftService.assignShift(request);
    }

    @PatchMapping({"update_shift/{shiftId}"})
    public ResponseEntity<?> updateShift(@RequestBody AssignShiftRequest request, @PathVariable Long shiftId) {
        return assignShiftService.updateShift(request, shiftId);
    }

    @GetMapping("get_employee_shifts")
    public ResponseEntity<?> getEmployeeShifts(
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "assignedMemberId", required = false) Long assignedMemberId,
            @RequestParam(name = "shiftTime", required = false) String shiftTime,
            @RequestParam(name = "jobTitleId", required = false) Long jobTitleId) {

        // ...
        AssignShiftRequest assignShiftRequest = new AssignShiftRequest();
        assignShiftRequest.setEmployeeId(assignedMemberId);
        assignShiftRequest.setStartDate(startDate);
        assignShiftRequest.setEndDate(endDate);
        assignShiftRequest.setShiftTime(shiftTime);
        assignShiftRequest.setJobTitleId(jobTitleId);
        assignShiftRequest.setBranchId(branchId);

        return assignShiftService.getEmployeeShifts(assignShiftRequest);
    }

}

