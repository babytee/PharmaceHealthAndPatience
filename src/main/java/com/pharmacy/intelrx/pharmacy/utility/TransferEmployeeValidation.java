package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.TransferEmployeeRequest;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyBranchRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TransferEmployeeValidation {
    private final Utility utility;
    private final PharmacyBranchRepository branchRepository;
    private final UserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> validate(TransferEmployeeRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Request body is required"));
        }
        if (request.getCurrentBranchId() == null && request.getNewBranchId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("newBranchId or currentBranchId can not be empty"));
        }


//        else if (request.getCurrentBranchId() == null) {
//            return ResponseEntity.badRequest().body("currentBranchId is required");
//        } else if (request.getNewBranchId() == null) {
//            return ResponseEntity.badRequest().body("newBranchId is required");
//        }else{
        if (request.getCurrentBranchId() != null && checkBranch(request.getCurrentBranchId()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("currentBranchId does not exist"));
        }
        if (request.getNewBranchId() != null && checkBranch(request.getNewBranchId()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("newBranchId does not exist"));
        }

//            if(checkEmployee(request.getEmployeeId())==null){
//                return ResponseEntity.badRequest().body("employeeId does not exist");
//            }

        if (request.getNewBranchId() != null && checkBranch(request.getNewBranchId()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("newBranchId does not exist"));
        }

        if (checkBranchManager(request.getEmployeeId()) != null) {
            var manager = checkBranchManager(request.getEmployeeId());
            return ResponseEntity.badRequest().body(StandardResponse.error("This employee is a manager at "+manager.getName()));
        }

        boolean check_password = this.passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!check_password) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Password not match"));
        }
        // }

        return ResponseEntity.ok(StandardResponse.success("validated"));
    }

    public PharmacyBranch checkBranch(Long branchId) {
        var intelRxId = userDetailsService.getIntelRxId();
        var branchOptional = branchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        return branchOptional.orElse(null);
    }

    public PharmacyBranch checkBranchManager(Long employeeId) {
        var intelRxId = userDetailsService.getIntelRxId();
        var branchOptional = branchRepository.findByIdAndIntelRxId(employeeId,intelRxId);
        PharmacyBranch branch = null;
        if (branchOptional.isPresent()) {
            branch = branchOptional.get();
        }

        return branch;
    }

    public Employee checkEmployee(Long employeeId) {
        var intelRxId = userDetailsService.getIntelRxId();
        return employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, intelRxId)
                .orElse(null);
    }
}
