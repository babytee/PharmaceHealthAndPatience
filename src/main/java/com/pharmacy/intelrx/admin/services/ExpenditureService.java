package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureResponse;
import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.ExpenditureValidation;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@RequiredArgsConstructor
@Service("AdminExpenditureService")
public class ExpenditureService {
    private final ExpenditureRepository expenditureRepository;
    private final UserDetailsService userDetailsService;

    public ResponseEntity<?> allExpenses(String expenseName,String intelRxId, Long branchId, Pageable pageable) {

        Page<Expenditure> allExpenses = expenditureRepository.findByFilters(
                expenseName,
                branchId,
                intelRxId,
                pageable);

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
        var totalCost = expenditureRepository.getTotalAmountByIntelRxIdAndApprovedAndBranch
                (allExpenses.getContent().get(0).getIntelRxId(),true, branchId);

        Page<ExpenditureRequest> expenditureRequests = allExpenses.map(exp ->
        {
            var addedBy = userDetailsService.mapToUserInfo(exp.getAddedBy());
            ExpenditureRequest expenditureRequest = new ExpenditureRequest();
            expenditureRequest.setId(exp.getId());
            expenditureRequest.setIntelRxId(exp.getIntelRxId());
            expenditureRequest.setExpenseName(exp.getExpenseName());
            expenditureRequest.setAmountSpent(exp.getAmountSpent());
            expenditureRequest.setExpYear(exp.getExpYear());
            expenditureRequest.setExpMonth(exp.getExpMonth());
            expenditureRequest.setExpDay(exp.getExpDay());
            expenditureRequest.setAddedBy(addedBy);
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
