package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.dto.CustomResponse;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchesResponse;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyRequest;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.repositories.ContactInfoRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service("PharmacyService")
public class PharmacyService {
    private final PharmacyRepository pharmacyRepository;
    private final Utility utility;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PharmacyMapping pharmacyMapping;
    private final ContactInfoRepository contactInfoRepository;
    private final UserDetailsService userDetailsService;

    @Transactional
    public ResponseEntity<?> verifyPharmacy(PharmacyRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getPharmacyName())) {
            return ResponseEntity.badRequest().body(new CustomResponse("failed", "pharmacyName is required"));
        } else if (utility.isNullOrEmpty(request.getPremiseNumber())) {
            return ResponseEntity.badRequest().body(new CustomResponse("failed", "premiseNumber is required"));
        } else if (utility.isNullOrEmpty(request.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(new CustomResponse("failed", "phoneNumber is required"));
        } else if (utility.isNullOrEmpty(request.getPharmacyOwner())) {
            return ResponseEntity.badRequest().body(new CustomResponse("failed", "pharmacyOwner is required"));
        }else {

            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.badRequest().body(new CustomResponse("failed", "The logged in User not found"));
            }

            User user = optionalUser.get();

            //Pharmacy Contact Request
            ContactInfoReqRes contactInfoReqRes = request.getContactInfoReqRes();
            ResponseEntity<?> contactInfoReqResResEntity = authService.ContactInfoReqRes(contactInfoReqRes);

            if (contactInfoReqResResEntity.getStatusCode() != HttpStatus.OK) {
                return authService.ContactInfoReqRes(contactInfoReqRes);
            }

            Optional<Pharmacy> pharmacyOptional = pharmacyRepository.findByPremiseNumber(request.getPremiseNumber());

            if (pharmacyOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new CustomResponse("failed", "Pharmacy with this premises number already exist"));
            }


            String intelRxId = userDetailsService.getIntelRxId();
            Optional<Pharmacy> pharmacyIntelRxId = pharmacyRepository.findByIntelRxId(intelRxId);

            if (!pharmacyIntelRxId.isPresent()) {
                return ResponseEntity.badRequest().body(new CustomResponse("failed", "Pharmacy with this IntelRxId does not exist"));
            }

            Pharmacy pharmacy = pharmacyIntelRxId.get();
            pharmacy.setIntelRxId(intelRxId);
            pharmacy.setPharmacyName(request.getPharmacyName());
            pharmacy.setPharmacyOwner(request.getPharmacyOwner());
            pharmacy.setPharmacyNumber(request.getPhoneNumber());
            pharmacy.setPremiseNumber(request.getPremiseNumber());
            pharmacy.setUser(user);
            pharmacy.setCreatedAt(LocalDateTime.now());
            pharmacy.setUpdatedAt(LocalDateTime.now());

            var pharm = pharmacyRepository.save(pharmacy);

            //store the pharmacist contact address details
            ContactInfo contactInfo = ContactInfo.builder()
                    .country(contactInfoReqRes.getCountry())
                    .state(contactInfoReqRes.getState())
                    .city(contactInfoReqRes.getCity())
                    .lga(contactInfoReqRes.getLga())
                    .streetAddress(contactInfoReqRes.getStreetAddress())
                    .pharmacy(pharm)
                    .build();
            contactInfoRepository.save(contactInfo);

            return ResponseEntity.ok().body(new CustomResponse("success", "Pharmacy details added successfully"));
        }

    }

    public ResponseEntity<?> pharmacyDetails(){
        String intelRxId = userDetailsService.getIntelRxId();

        Optional<Pharmacy> optional = pharmacyRepository.findByIntelRxId(intelRxId);

        if(!optional.isPresent()){
            return ResponseEntity.badRequest().body(StandardResponse.error("No pharmacy with this intelRxId "));
        }

        Pharmacy pharmacy = optional.get();
        PharmacyBranchesResponse response = pharmacyMapping.mapToPharmacyHeadQuarters(pharmacy);

        return ResponseEntity.ok(StandardResponse.success(response));
    }
}
