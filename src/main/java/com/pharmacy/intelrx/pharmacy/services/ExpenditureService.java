package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureResponse;
import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.ExpenditureValidation;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("ExpenditureService")
public class ExpenditureService {
    private final ExpenditureRepository expenditureRepository;
    private final UserDetailsService userDetailsService;
    private final ExpenditureValidation expenditureValidation;
    private final EmployeeRepository employeeRepository;

    public ResponseEntity<?> addExpense(ExpenditureRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("user is not authenticated"));
        }
        ResponseEntity responseEntity = expenditureValidation.validateExpenditureRequest((request));
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        modifyExpenseEntity(request, null);

        return ResponseEntity.ok(StandardResponse.success("Expense Added Successfully"));

    }

    public ResponseEntity<?> updateExpense(ExpenditureRequest request, Long expenseId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("user is not authenticated"));
        }
        ResponseEntity responseEntity = expenditureValidation.validateUpdateExpenditureRequest(request, expenseId);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        modifyExpenseEntity(request, expenseId);

        return ResponseEntity.ok(StandardResponse.success("Expense Added Successfully"));

    }

    public ResponseEntity<?> approveExpense(Long expenseId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("user is not authenticated"));
        }
        if (expenseId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expenseId is required"));
        }
        var intelRxId = userDetailsService.getIntelRxId();

        Optional<Expenditure> optionalExpenditure = expenditureRepository.findByIdAndIntelRxId(expenseId, intelRxId);

        if (!optionalExpenditure.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expenditure is not found"));
        }

        Expenditure expenditure = optionalExpenditure.get();
        if (expenditure.isApproved() == true) {
//            String fullName = user.getLastName() + " " + user.getFirstName();
//            return ResponseEntity.ok(StandardResponse.success("This has been approved by " + fullName));

            expenditure.setApproved(false);
            expenditure.setApprovedBy(user);
            expenditureRepository.save(expenditure);
            return ResponseEntity.ok(StandardResponse.success("Disapproved"));

        }
        if (user.getUserType() == UserType.OWNER) {
            expenditure.setApproved(true);
            expenditure.setApprovedBy(user);
            expenditureRepository.save(expenditure);
            return ResponseEntity.ok(StandardResponse.success("Approved"));
        } else {
            Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());
            if (user.getUserType().equals("EMPLOYEE")) {
                Employee employee = optionalEmployee.get();
                long jobTitleId = employee.getJobInformation().getJobTitle().getId();
                if (jobTitleId == 5) {
                    expenditure.setApproved(true);
                    expenditure.setApprovedBy(user);
                    expenditureRepository.save(expenditure);

                    return ResponseEntity.ok(StandardResponse.success("Approved"));
                }
            }
        }
        return ResponseEntity.ok(StandardResponse.error("You must be the pharmacy owner or the manager to do this"));

    }

    public void modifyExpenseEntity(ExpenditureRequest request, Long expenseId) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        var expense = expenditureRepository.findByIdAndIntelRxId(expenseId, intelRxId);

        Expenditure expenditure = null;

        if (expense.isPresent()) {
            expenditure = expense.get();
        } else {
            expenditure = new Expenditure();
        }

        expenditure.setExpenseName(request.getExpenseName());
        expenditure.setExpDay(request.getExpDay());
        expenditure.setExpMonth(request.getExpMonth());
        expenditure.setExpYear(request.getExpYear());
        expenditure.setAddedBy(user);
        expenditure.setBranch(branch == null ? null : branch);
        expenditure.setIntelRxId(intelRxId);
        expenditure.setAmountSpent(request.getAmountSpent());
        expenditure.setExpenditureType(request.getExpenditureType() == null ? "Miscellaneous" : request.getExpenditureType());
        expenditure.setApproved(false);
        expenditure.setCreatedAt(LocalDateTime.now());

        expenditureRepository.save(expenditure);

    }

    public ResponseEntity<?> allExpenses(String expenseName, Long branchId, Pageable pageable) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        Page<Expenditure> allExpenses = null;

        if (branch == null && user.getUserType() == UserType.OWNER) {
            allExpenses = expenditureRepository.findByFilters(
                    expenseName,
                    branchId,
                    intelRxId,
                    pageable);
        }

        if (branch == null && user.getUserType() == UserType.EMPLOYEE) {
            allExpenses = expenditureRepository.findByFilters(
                    expenseName,
                    branchId,
                    intelRxId,
                    pageable);
        }

        if (branch != null && user.getUserType() == UserType.EMPLOYEE) {
            allExpenses = expenditureRepository.findByFilters(
                    expenseName,
                    branch.getId(),
                    intelRxId,
                    pageable);
        }


        if (allExpenses.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(allExpenses)); // Return an empty list
        }

        ExpenditureResponse response = mapToExpenses(allExpenses);

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    private ExpenditureResponse mapToExpenses(Page<Expenditure> allExpenses) {
        if (allExpenses.isEmpty()) {
            return ExpenditureResponse.builder()
                    .totalCost(0.0)
                    .expenseList(Collections.emptyList())
                    .build();
        }

        var branchId = allExpenses.getContent().get(0).getBranch() == null ? null : allExpenses.getContent().get(0).getBranch().getId();
        var totalCost = expenditureRepository.getTotalAmountByIntelRxIdAndApprovedAndBranch(
                allExpenses.getContent().get(0).getIntelRxId(),true, branchId
        );

        Page<ExpenditureRequest> expenditureRequests = allExpenses.map(exp ->
        {
            ExpenditureRequest expenditureRequest = new ExpenditureRequest();

                var addedBy = userDetailsService.mapToUserInfo(exp.getAddedBy());
                var approvedBy =
                        exp.getApprovedBy() == null ? null : userDetailsService.mapToUserInfo(exp.getApprovedBy());

                expenditureRequest.setId(exp.getId());
                expenditureRequest.setIntelRxId(exp.getIntelRxId());
                expenditureRequest.setExpenseName(exp.getExpenseName());
                expenditureRequest.setAmountSpent(exp.getAmountSpent());
                expenditureRequest.setExpYear(exp.getExpYear());
                expenditureRequest.setExpMonth(exp.getExpMonth());
                expenditureRequest.setExpDay(exp.getExpDay());
                expenditureRequest.setAddedBy(addedBy);
                expenditureRequest.setApproved(exp.isApproved());
                expenditureRequest.setApprovedBy(approvedBy);
                expenditureRequest.setAmountSpent(exp.getAmountSpent());
                expenditureRequest.setCreatedAt(exp.getCreatedAt());

            return expenditureRequest;

        });

        return ExpenditureResponse.builder()
                .totalCost(totalCost)
                .expenseList(expenditureRequests)
                .build();

    }


}
