package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.employee.AssignShiftRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.AssignShiftResponse;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.employee.AssignShift;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.employee.AssignShiftRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.AssignShiftValidation;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyAssignShiftService")
public class AssignShiftService {
    private final UserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final AssignShiftRepository assignShiftRepository;
    private final AssignShiftValidation assignShiftValidation;
    private final Utility utility;
    private final FilterEmployee filterEmployee;

    //@Transactional
    public ResponseEntity<?> assignShift(AssignShiftRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("User is Un-Authenticated"));
        }

        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        ResponseEntity responseEntity = assignShiftValidation.validateAssignShiftRequest(request);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        for (Long memberId : request.getAssignedMemberId()) {
            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(memberId, intelRxId);
            Employee employee = optionalEmployee.get();
            JobTitle jobTitle = employee.getJobInformation().getJobTitle();


            Optional<AssignShift> optional = assignShiftRepository.findByAssignedMemberAndIntelRxIdAndPharmacyBranchAndStartDate(
                    employee, intelRxId, branch == null ? null : branch, LocalDate.parse(request.getStartDate())
            );

            if (optional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("startDate Already Exist "));
            }


            String shiftTime = calculateDuration(request.getStartTime(), request.getEndTime());

            AssignShift assignShift = AssignShift.builder()
                    .assignedMember(employee)
                    .AssignedColor(request.getAssignedColor())
                    .jobTitle(jobTitle)
                    .startDate(LocalDate.parse(request.getStartDate()))
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .shiftTime(shiftTime)
                    .pharmacyBranch(branch == null ? null : branch)
                    .intelRxId(intelRxId)
                    .createdAt(LocalDateTime.now())
                    .build();

            AssignShift assignShift1 = assignShiftRepository.save(assignShift);
            //for the remaining days of the week
            assignShiftContinuallyFromTheLastRecord(assignShift1);
        }


        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));

    }

    private String calculateDuration(String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ha");

        LocalTime start = LocalTime.parse(startTime, formatter);
        LocalTime end = LocalTime.parse(endTime, formatter);

        // Calculate duration between start and end time
        // Get hours and minutes from duration
        long hours = Duration.between(start, end).toHours(); // Total hours
        long minutes = Duration.between(start, end).toMinutesPart(); // Minutes part

        return "Total duration: " + hours + " hours " + minutes + " minutes";
    }

    public ResponseEntity<?> updateShift(AssignShiftRequest request, Long shiftId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("User is Un-Authenticated"));
        }
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        ResponseEntity responseEntity = assignShiftValidation.validateUpdateAssignShiftRequest(request, shiftId);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        for (Long memberId : request.getAssignedMemberId()) {
            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(memberId, intelRxId);
            Employee employee = optionalEmployee.get();
            JobTitle jobTitle = employee.getJobInformation().getJobTitle();

            Optional<AssignShift> optional = assignShiftRepository.findByIdAndAssignedMemberAndIntelRxIdAndPharmacyBranch(
                    shiftId, employee, intelRxId, branch == null ? null : branch
            );
            if (!optional.isPresent()) {
                return ResponseEntity.ok(StandardResponse.error("Employee with this id:" + memberId + " not found"));
            }
            AssignShift assignShift = optional.get();
            assignShift.setAssignedColor(request.getAssignedColor());
            assignShift.setAssignedMember(employee);
            assignShift.setStartDate(LocalDate.parse(request.getStartDate()));
            assignShift.setStartTime(request.getStartTime());
            assignShift.setEndTime(request.getEndTime());
            assignShift.setShiftTime(request.getShiftTime());
            assignShift.setPharmacyBranch(branch == null ? null : branch);
            assignShift.setUpdatedAt(LocalDateTime.now());
            assignShift.setJobTitle(jobTitle);

            assignShiftRepository.save(assignShift);
        }


        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));

    }

    public ResponseEntity<?> getEmployeeShifts(AssignShiftRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        LocalDate startDate = null;
        LocalDate endDate = null;
        if (!utility.isNullOrEmpty(request.getStartDate()) && !utility.isNullOrEmpty(request.getEndDate())) {
            startDate = utility.convertStringToLocalDate(request.getStartDate());
            endDate = utility.convertStringToLocalDate(request.getEndDate());
        }

        List<AssignShift> assignShifts = assignShiftRepository.filterAssignShifts(
                startDate == null ? null : startDate.toString(),
                endDate == null ? null : endDate.toString(),
                request.getJobTitleId(),
                branch == null ? request.getBranchId() : branch.getId(),
                request.getShiftTime(),
                intelRxId,
                request.getEmployeeId()
        );

        if (assignShifts.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(assignShifts));
        }

        List<AssignShiftResponse> assignShiftResponses = assignShifts.stream().map(
                assignShift -> mapToAssignShift(assignShift)
        ).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(assignShiftResponses));

    }

    private AssignShiftResponse mapToAssignShift(AssignShift assignShift) {

        //String shiftDateTime = utility.addHoursAndFormat(String.valueOf(assignShift.getCreatedAt()),assignShift.getShiftTime());
        return AssignShiftResponse.builder()
                .id(assignShift.getId())
                .shiftHours(assignShift.getShiftTime())
                .startTime(assignShift.getStartTime())
                .endTime(assignShift.getEndTime())
                .createdAt(assignShift.getCreatedAt())
                .shiftDate(assignShift.getStartDate())
                .assignedColor(assignShift.getAssignedColor())
                .jobTitle(assignShift.getJobTitle())
                .employeeInfo(filterEmployee.mapToUserInfo(assignShift.getAssignedMember()))
                .build();
    }

    public void assignShiftContinuallyFromTheLastRecord(AssignShift assignShift) {
        LocalDate lastStartDate = assignShift.getStartDate();
        LocalDate endOfWeek = lastStartDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // Create and save shifts for the current week
        while (!lastStartDate.isAfter(endOfWeek)) {
            if (!assignShiftRepository.existsByStartDateAndIntelRxIdAndAssignedMemberId(lastStartDate, assignShift.getIntelRxId(), assignShift.getAssignedMember().getId())) {
                AssignShift addAssignShift = AssignShift.builder()
                        .assignedMember(assignShift.getAssignedMember())
                        .AssignedColor(assignShift.getAssignedColor())
                        .jobTitle(assignShift.getJobTitle())
                        .startDate(lastStartDate)
                        .startTime(assignShift.getStartTime())
                        .endTime(assignShift.getEndTime())
                        .shiftTime(assignShift.getShiftTime())
                        .pharmacyBranch(assignShift.getPharmacyBranch())
                        .intelRxId(assignShift.getIntelRxId())
                        .createdAt(LocalDateTime.now())
                        .build();
                assignShiftRepository.save(addAssignShift);
            }
            lastStartDate = lastStartDate.plusDays(1);
        }

        // Calculate the start date of the next week
        LocalDate nextWeekStartDate = endOfWeek.plusDays(1);

        // Create and save shifts for the next week
        while (nextWeekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            AssignShift addAssignShift = AssignShift.builder()
                    .assignedMember(assignShift.getAssignedMember())
                    .AssignedColor(assignShift.getAssignedColor())
                    .jobTitle(assignShift.getJobTitle())
                    .startDate(nextWeekStartDate)
                    .startTime(assignShift.getStartTime())
                    .endTime(assignShift.getEndTime())
                    .shiftTime(assignShift.getShiftTime())
                    .pharmacyBranch(assignShift.getPharmacyBranch())
                    .intelRxId(assignShift.getIntelRxId())
                    .createdAt(LocalDateTime.now())
                    .build();
            assignShiftRepository.save(addAssignShift);

            nextWeekStartDate = nextWeekStartDate.plusDays(1);
        }
    }

    public void assignShiftContinuallyFromTheLastWeekRecord() {
        // Fetch shifts created or updated since the last scheduled execution
        LocalDate lastScheduledExecutionTime = getLastScheduledExecutionDate();
        List<AssignShift> assignShiftList = assignShiftRepository.findAllByStartDate(lastScheduledExecutionTime);
        if (!assignShiftList.isEmpty()) {
            for (AssignShift assignShift : assignShiftList) {
                assignShiftContinuallyFromTheLastRecord(assignShift);
            }
        }
    }

    // Method to get the last scheduled execution time
    private LocalDate getLastScheduledExecutionDate() {
        // Get the current date and time
        LocalDate currentDate = LocalDate.now();

        // Get the current year and month
        int currentYear = currentDate.getYear();
        Month currentMonth = currentDate.getMonth();

        // Create a LocalDateTime object with the current year and month, set to the first day of the month at midnight
        return LocalDate.of(currentYear, currentMonth, 1);
    }

    // Schedule the method to run every week
    @Scheduled(cron = "0 0 0 * * MON") // Run at midnight every Monday
    public void scheduleAssignShiftContinuallyFromTheLastRecord() {
        assignShiftContinuallyFromTheLastWeekRecord();
    }

}

