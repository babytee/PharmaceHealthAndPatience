package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ConcludeEmployeeRequest;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ConcludeEmployeeValidation {
    private final Utility utility;
    private final UserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> validate(ConcludeEmployeeRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Request body is required"));
        }

        if (utility.isNullOrEmpty(request.getReasons())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("reasons is required"));
        }

        if (utility.isNullOrEmpty(request.getConcludeType())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("concludeType is required"));
        }

        String concludeType = request.getConcludeType();
        if (!concludeType.equals("TERMINATED") && !concludeType.equals("DELETED") &&
                !concludeType.equals("SUSPENDED") && !concludeType.equals("END CONTRACT")) {
            return ResponseEntity.badRequest().body(StandardResponse.error("The concludeType must be one of TERMINATED, DELETED, SUSPENDED, or END CONTRACT"));
        }

        if (request.getEmployeeId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
        }

        if (checkEmployee(request.getEmployeeId()) == null) {
            return ResponseEntity.badRequest().body("employeeId does not exist");
        }

        boolean check_password = this.passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!check_password) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Password not match"));
        }
        // }

        return ResponseEntity.ok(StandardResponse.success("validated"));
    }

    public Employee checkEmployee(Long employeeId) {
        var intelRxId = userDetailsService.getIntelRxId();
        var employeeOptional = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, intelRxId);
        Employee employee = null;
        if (employeeOptional.isPresent()) {
            employee = employeeOptional.get();
        }

        return employee;
    }
}
