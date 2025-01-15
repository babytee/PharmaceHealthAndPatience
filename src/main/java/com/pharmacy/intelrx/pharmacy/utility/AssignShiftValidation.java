package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.AssignShiftRequest;
import com.pharmacy.intelrx.pharmacy.models.employee.AssignShift;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.AssignShiftRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AssignShiftValidation {
    private final Utility utility;
    private final EmployeeRepository employeeRepository;
    private final UserDetailsService userDetailsService;
    private final AssignShiftRepository assignShiftRepository;

    public ResponseEntity<?> validateAssignShiftRequest(AssignShiftRequest request) {
        var intelRxId = userDetailsService.getIntelRxId();
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("request body not found"));
        } else if (request.getAssignedMemberId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("assignedMemberId is required"));
        } else if (utility.isNullOrEmpty(request.getStartTime())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("startTime( is required"));
        } else if (utility.isNullOrEmpty(request.getEndTime())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("endTime is required"));
        } else if (utility.isNullOrEmpty(request.getAssignedColor())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("assignedColor is required"));
        } else {

            for (Long memberId : request.getAssignedMemberId()) {
                Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(memberId, intelRxId);


                if (!optionalEmployee.isPresent()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("assigned Employee not found"));
                }

                if (assignShiftRepository.existsByStartDateAndIntelRxIdAndAssignedMemberId(LocalDate.parse(request.getStartDate()), intelRxId, memberId)) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("you can't add the Employee with this employeeId:" + memberId + ", because it has been added for today"));
                }
            }
        }

        return ResponseEntity.ok(StandardResponse.success("Validated successfully"));
    }

    public ResponseEntity<?> validateUpdateAssignShiftRequest(AssignShiftRequest request, Long assignedShiftId) {

        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("request body not found"));
        } else if (assignedShiftId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("assignedShiftId is required"));
        } else if (request.getAssignedMemberId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("assignedMemberId is required"));
        } else if (utility.isNullOrEmpty(request.getStartTime())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("startTime( is required"));
        } else if (utility.isNullOrEmpty(request.getEndTime())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("endTime is required"));
        } else if (utility.isNullOrEmpty(request.getAssignedColor())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("assignedColor is required"));
        } else {

            for (Long memberId : request.getAssignedMemberId()) {
                Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(memberId, intelRxId);
                if (!optionalEmployee.isPresent()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("assigned Employee not found"));
                }
            }

            Optional<AssignShift> optionalAssignShift = assignShiftRepository.findByIdAndIntelRxIdAndPharmacyBranch(
                    assignedShiftId, intelRxId, branch == null ? null : branch
            );
            if (!optionalAssignShift.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("assigned Shift not found for this employee"));
            }

        }

        return ResponseEntity.ok(StandardResponse.success("Validated successfully"));
    }
}
