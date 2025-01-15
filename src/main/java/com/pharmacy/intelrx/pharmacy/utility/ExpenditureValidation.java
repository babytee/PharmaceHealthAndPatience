package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ExpenditureValidation {
    private final Utility utility;
    private final EmployeeRepository employeeRepository;
    private final UserDetailsService userDetailsService;
    private final ExpenditureRepository expenditureRepository;

    public ResponseEntity<?>validateExpenditureRequest(ExpenditureRequest request){

        if(request==null){
            return ResponseEntity.badRequest().body(StandardResponse.error("request body not found"));
        }else if(utility.isNullOrEmpty(request.getExpenseName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expenseName is required"));
        }else if(request.getAmountSpent()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("amountSpent is required"));
        }else if(request.getExpYear()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("expYear is required"));
        }else if(request.getExpMonth()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("expMonth is required"));
        }else if(request.getExpDay()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("expDay is required"));
        }

        return ResponseEntity.ok(StandardResponse.success("Validated successfully"));
    }

    public ResponseEntity<?>validateUpdateExpenditureRequest(ExpenditureRequest request,Long expenseId){

        if(request==null){
            return ResponseEntity.badRequest().body(StandardResponse.error("request body not found"));
        }else if(utility.isNullOrEmpty(request.getExpenseName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expenseName is required"));
        }else if(request.getAmountSpent()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("amountSpent is required"));
        }else if(request.getExpYear()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("expYear is required"));
        }else if(request.getExpMonth()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("expMonth is required"));
        }else if(request.getExpDay()<1){
            return ResponseEntity.badRequest().body(StandardResponse.error("expDay is required"));
        }else{
            var intelRxId = userDetailsService.getIntelRxId();
            var expense = expenditureRepository.findByIdAndIntelRxId(expenseId,intelRxId);
            if(!expense.isPresent()){
                return ResponseEntity.badRequest().body(StandardResponse.error("expenditure not found"));
            }
        }

        return ResponseEntity.ok(StandardResponse.success("Validated successfully"));
    }
}
