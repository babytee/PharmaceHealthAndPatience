package com.pharmacy.intelrx.utility;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.admin.repositories.AdminRepository;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.pharmacy.repositories.BranchEmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service("AdminDetailsService")
public class AdminDetailsService {
private final AdminRepository adminRepository;
    public Admin getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return adminRepository.findByEmail(authentication.getName()).orElse(null);
    }

    public UserRequest getAuthProfile() {
        Admin admin = getAuthenticatedUser();
        return mapToUserResponse(admin);
    }

    public UserRequest mapToUserResponse(Admin admin) {
        UserRequest userResponse = new UserRequest();
        userResponse.setId(admin.getId());
        userResponse.setEmail(admin.getEmail());
        userResponse.setFirstName(admin.getFirstname());
        userResponse.setLastName(admin.getLastname());
        return userResponse;

    }

}
