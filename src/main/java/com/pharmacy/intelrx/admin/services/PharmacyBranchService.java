package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.admin.dto.PharmacyBranchStatResponse;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchRequest;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchResponse;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchesResponse;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.EmployeeValidation;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service("AdminPharmacyBranchService")
public class PharmacyBranchService {
    private final PharmacyBranchRepository branchRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapping pharmacyMapping;
    private final BranchEmployeeRepository branchEmployeeRepository;
    private final InventoryRepository inventoryRepository;
    private final TransferInventoryRepository transferInventoryRepository;

    public ResponseEntity<?> getAllBranch(String intelRxId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
        Pharmacy pharmacy = optionalPharmacy.get();
        PharmacyBranchesResponse employeeResponseList = pharmacyMapping.mapToPharmacyHeadQuarters(pharmacy);

        return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
    }

    public ResponseEntity<?> getBranch(Long branchId,String intelRxId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        Optional<PharmacyBranch> optionalPharmacy = branchRepository.findByIdAndIntelRxId(branchId,intelRxId);
        PharmacyBranch PharmacyBranch = optionalPharmacy.get();
        PharmacyBranchResponse employeeResponseList = pharmacyMapping.mapToBranchResponse(PharmacyBranch);

        return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
    }

    public ResponseEntity<?>branchOverviewStats(Long branchId,String intelRxId){
        Optional<PharmacyBranch> optionalPharmacy = branchRepository.findByIdAndIntelRxId(branchId,intelRxId);
        PharmacyBranch pharmacyBranch = optionalPharmacy.get();

        var employeeList = branchEmployeeRepository.findAllByPharmacyBranchId(pharmacyBranch.getId());

        List<Inventory> medicationInventoryList = inventoryRepository.findAllByTransferBranch(
                intelRxId,
                "MEDICATION",
                true,
                pharmacyBranch.getId()
        );

        List<Inventory> groceryInventoryList = inventoryRepository.findAllByTransferBranch(
                intelRxId,
                "GROCERY",
                true,
                pharmacyBranch.getId()
        );

        String firstName = pharmacyBranch.getEmployee().getUser().getFirstName();
        String lastName = pharmacyBranch.getEmployee().getUser().getLastName();
        String fullName = lastName + " " + firstName;

        PharmacyBranchStatResponse branchStatResponse = new PharmacyBranchStatResponse();
        branchStatResponse.setManager(fullName);
        branchStatResponse.setEmployees(employeeList.size());
        branchStatResponse.setMedication(medicationInventoryList.size());
        branchStatResponse.setGrocery(groceryInventoryList.size());

        return ResponseEntity.ok(StandardResponse.success(branchStatResponse));
    }




}
