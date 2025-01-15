package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.BranchEmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.ContactInfoRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyBranchRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.utility.EmployeeValidation;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service("PharmacyBranchService")
public class PharmacyBranchService {
    private final PharmacyBranchRepository branchRepository;
    private final Utility utility;
    private final PharmacyRepository pharmacyRepository;
    private final FilterEmployee filterEmployee;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PharmacyMapping pharmacyMapping;
    private final EmployeeValidation employeeValidation;
    private final ContactInfoRepository contactInfoRepository;
    private final UserDetailsService userDetailsService;
    private final BranchEmployeeRepository branchEmployeeRepository;

    public ResponseEntity<?> addNewBranch(PharmacyBranchRequest request) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
            } else if (utility.isNullOrEmpty(request.getName())) {
                return ResponseEntity.ok().body(StandardResponse.error("name is required"));
            } else if (request.getEmployeeId() == null) {
                return ResponseEntity.ok().body(StandardResponse.error("employeeId is required"));
            } else if (utility.isNullOrEmpty(request.getBgColor())) {
                return ResponseEntity.ok().body(StandardResponse.error("bgColor is required"));
            }

            // Contact Request
            ResponseEntity<?> contactInfoReqResResEntity = employeeValidation.ContactInfoReqRes(request.getContactInfoReqRes());
            if (contactInfoReqResResEntity.getStatusCode() != HttpStatus.OK) {
                return contactInfoReqResResEntity;
            }

            String intelRxId = userDetailsService.getIntelRxId();
            var branchOptional = branchRepository.findByIntelRxIdAndName(intelRxId, request.getName());

            if (branchOptional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("name already exist"));
            }

            var optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("this logged in user is not found"));
            }
            User user = optionalUser.get();

            var optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);

            if (optionalPharmacy.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy IntelRxId not exist"));
            }

            var optionalEmployee = employeeRepository.findById(request.getEmployeeId());

            if (!optionalEmployee.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("User used as manager not found"));
            }

            Employee employee = optionalEmployee.get();

            Pharmacy pharmacy = optionalPharmacy.get();

            PharmacyBranch pharmacyBranch = PharmacyBranch.builder()
                    .name(request.getName())
                    .intelRxId(intelRxId)
                    .pharmacy(pharmacy)
                    .user(user)
                    .employee(employee)
                    .bgColor(request.getBgColor())
                    .createdAt(LocalDateTime.now())
                    .build();
            var branch = branchRepository.save(pharmacyBranch);

            // Add to BranchEmployee
            AddManagerToBranch(user, branch, employee);

            // ContactInfoReqRes Request
            ContactInfoReqRes contactInfoReqRes = request.getContactInfoReqRes();
            AddToContactEntity(contactInfoReqRes, branch);

            return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
        }catch (Exception e){
            return ResponseEntity.ok(StandardResponse.error(e.getLocalizedMessage()));
        }
    }

    public ContactInfo AddToContactEntity(ContactInfoReqRes contactInfoReqRes, PharmacyBranch pharmacyBranch) {
        ContactInfo contactInfo = null;
        var checkContact = contactInfoRepository.findByPharmacyBranchId(pharmacyBranch.getId());
        if (checkContact.isPresent()) {
            contactInfo = checkContact.get();
            contactInfo.setPharmacyBranch(pharmacyBranch);
        } else {
            contactInfo = new ContactInfo();
            contactInfo.setPharmacyBranch(pharmacyBranch);
        }
        // Set properties
        contactInfo.setCountry(contactInfoReqRes.getCountry());
        contactInfo.setState(contactInfoReqRes.getState());
        contactInfo.setCity(contactInfoReqRes.getCity());
        contactInfo.setLga(contactInfoReqRes.getLga());
        contactInfo.setStreetAddress(contactInfoReqRes.getStreetAddress());

        // Save ContactInfo entity
        var contact = contactInfoRepository.save(contactInfo);

        return contact;
    }

    public BranchEmployee AddManagerToBranch(User user,PharmacyBranch pharmacyBranch, Employee employee) {

        BranchEmployee branchEmployee = null;
        var checkBranchEmployee = branchEmployeeRepository.findByPharmacyBranchId(pharmacyBranch.getId());
        if (checkBranchEmployee.isPresent()) {
            branchEmployee = checkBranchEmployee.get();
            branchEmployee.setPharmacyBranch(pharmacyBranch);
        } else {
            branchEmployee = new BranchEmployee();
            branchEmployee.setPharmacyBranch(pharmacyBranch);
        }

        // Set properties
        branchEmployee.setTitle(employee.getJobInformation().getJobTitle().getName());
        branchEmployee.setPharmacyBranch(pharmacyBranch);
        branchEmployee.setEmployee(employee);
        branchEmployee.setUser(user);
        branchEmployee.setCreatedAt(LocalDateTime.now());

        // Save ContactInfo entity
        var branch = branchEmployeeRepository.save(branchEmployee);

        return branch;
    }

    public BranchEmployee AddMemberToBranch(User user,PharmacyBranch pharmacyBranch, Employee employee) {

        BranchEmployee branchEmployee = null;
        var checkBranchEmployee = branchEmployeeRepository.findByPharmacyBranchAndEmployee(pharmacyBranch,employee);
        if (checkBranchEmployee.isPresent()) {
            branchEmployee = checkBranchEmployee.get();
            branchEmployee.setPharmacyBranch(pharmacyBranch);
        } else {
            branchEmployee = new BranchEmployee();
            branchEmployee.setPharmacyBranch(pharmacyBranch);
        }

        // Set properties
        branchEmployee.setTitle(employee.getJobInformation().getJobTitle().getName());
        branchEmployee.setPharmacyBranch(pharmacyBranch);
        branchEmployee.setEmployee(employee);
        branchEmployee.setUser(user);
        branchEmployee.setCreatedAt(LocalDateTime.now());

        // Save ContactInfo entity
        var branch = branchEmployeeRepository.save(branchEmployee);

        return branch;
    }


    public ResponseEntity<?> updateBranch(PharmacyBranchRequest request, Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getName())) {
            return ResponseEntity.ok().body(StandardResponse.error("name is required"));
        }
        String intelRxId = userDetailsService.getIntelRxId();
        var branchOptional = branchRepository.findByIntelRxIdAndName(intelRxId, request.getName());

        if (branchOptional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("name already exist"));
        }

        var optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("this logged in user is not found"));
        }
        User user = optionalUser.get();

        var optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);

        if (optionalPharmacy.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy IntelRxId not exist"));
        }

        Pharmacy pharmacy = optionalPharmacy.get();

        var optionalPharmacyBranch = branchRepository.findById(id);

        if (optionalPharmacyBranch.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy IntelRxId not exist"));
        }

        PharmacyBranch pharmacyBranch = optionalPharmacyBranch.get();
        pharmacyBranch.setName(request.getName());
        pharmacyBranch.setUser(user);
        pharmacyBranch.setPharmacy(pharmacy);
        pharmacyBranch.setBgColor(request.getBgColor());

        var branch = branchRepository.save(pharmacyBranch);

        // ContactInfoReqRes Request
        ContactInfoReqRes contactInfoReqRes = request.getContactInfoReqRes();
        AddToContactEntity(contactInfoReqRes, branch);
        return ResponseEntity.ok().body("Updated Successfully");
    }

    public ResponseEntity<?> getAllBranch() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        String intelRxId = userDetailsService.getIntelRxId();

        String email = authentication.getName();

        var optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("this logged in user is not found"));
        }

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
        Pharmacy pharmacy = optionalPharmacy.get();
        PharmacyBranchesResponse employeeResponseList = pharmacyMapping.mapToPharmacyHeadQuarters(pharmacy);

        return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
    }

    public ResponseEntity<?> getBranch(Long branchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        String intelRxId = userDetailsService.getIntelRxId();

        String email = authentication.getName();

        var optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("this logged in user is not found"));
        }

        Optional<PharmacyBranch> optionalPharmacy = branchRepository.findByIdAndIntelRxId(branchId,intelRxId);
        PharmacyBranch PharmacyBranch = optionalPharmacy.get();
        PharmacyBranchResponse employeeResponseList = pharmacyMapping.mapToBranchResponse(PharmacyBranch);

        return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
    }




}
