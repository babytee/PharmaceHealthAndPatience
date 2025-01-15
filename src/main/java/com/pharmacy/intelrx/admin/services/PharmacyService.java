package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.admin.dto.PharmacyStatResponse;
import com.pharmacy.intelrx.auxilliary.dto.CustomResponse;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchesResponse;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyRequest;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.repositories.ContactInfoRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.services.AuthService;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminPharmacyService")
public class PharmacyService {
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapping pharmacyMapping;

    public ResponseEntity<?> pharmacyDetails(String intelRxId){
        if(intelRxId == null){
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }
        Optional<Pharmacy> optional = pharmacyRepository.findByIntelRxId(intelRxId);

        if(!optional.isPresent()){
            return ResponseEntity.badRequest().body(StandardResponse.error("No pharmacy with this intelRxId "));
        }

        Pharmacy pharmacy = optional.get();
        PharmacyBranchesResponse response = pharmacyMapping.mapToPharmacyHeadQuarters(pharmacy);

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    public ResponseEntity<?> pharmacies(String filter, Pageable pageable) {
        Page<Pharmacy> pharmacyList = pharmacyRepository.findByFilter(filter, pageable);
        Page<PharmacyBranchesResponse> response = pharmacyList.map(pharmacyMapping::mapToHeadQuarterDetails);
        return ResponseEntity.ok(StandardResponse.success(response));
    }

    public ResponseEntity<?> pharmacyStats() {
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        List<Pharmacy> regBySelf = pharmacyRepository.findAllByRegBy("Self Registered");
        List<Pharmacy> regBySalesRep = pharmacyRepository.findAllByRegBy("Rep");
        List<Pharmacy> subscribed = pharmacyRepository.findDistinctSubscriptionStatusBySubscriptionStatusIsNotNullAndSubscriptionStatusNot("Freemium");


        PharmacyStatResponse statResponse = new PharmacyStatResponse();
        statResponse.setTotalPharmacy(pharmacies.size());
        statResponse.setPharmacySubscribed(subscribed.size());
        statResponse.setTopLocation(getTopState());
        statResponse.setLessThanLastMonth(getDifferenceInPharmacyRegistrations());
        statResponse.setSelfRegisterPercent(regBySelf.size());
        statResponse.setSalesRepRegisterPercent(regBySalesRep.size());
        statResponse.setTotalStateCovered(pharmacies.size());

        return ResponseEntity.ok(StandardResponse.success(statResponse));
    }

    public String getTopState() {
        List<String> topStates = pharmacyRepository.findTopStatesOrderedByCount();
        if (!topStates.isEmpty()) {
            return topStates.toString(); // Return the first state in the list
        }
        return null; // Return null if no state is found
    }

    public int getPharmaciesRegisteredThisMonth() {
        return pharmacyRepository.countPharmaciesRegisteredThisMonth();
    }

    public int getPharmaciesRegisteredLastMonth() {
        return pharmacyRepository.countPharmaciesRegisteredLastMonth();
    }

    public int getDifferenceInPharmacyRegistrations() {
        return getPharmaciesRegisteredThisMonth() - getPharmaciesRegisteredLastMonth();
    }

}
