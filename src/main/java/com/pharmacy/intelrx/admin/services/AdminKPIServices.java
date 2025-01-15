package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.admin.dto.kpi.*;
import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.auxilliary.dto.RegisterRequest;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.CustomerEvaluation.CustomerRetentionRate;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.Merchandising.CalculateInventoryTurnover;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial.*;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.*;
import com.pharmacy.intelrx.auxilliary.repositories.KPITypeRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.models.KPIRecommendation;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.KPIType;
import com.pharmacy.intelrx.pharmacy.repositories.KPIRecommendationRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service("AdminKPIServices")
public class AdminKPIServices {

    private final YearlySalesDataEvaluation yearlySalesDataEvaluation;
    private final AverageTransactionValueCalculation averageTransactionValueCalculation;
    private final CalculateCostOfGoodsSold calculateCostOfGoodsSold;
    private final CalculateGMROI calculateGMROI;
    private final CalculateShrinkage calculateShrinkage;
    private final CalculateSellThrough calculateSellThrough;
    private final CalculateInventoryTurnover calculateInventoryTurnover;
    private final CustomerRetentionRate customerRetentionRate;
    private final CalculateOPEX calculateOPEX;
    private final CalculateQuickRatio calculateQuickRatio;
    private final KPIRecommendationRepository kpiRecommendationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final KPITypeRepository kpiTypeRepository;
    private final CalculateCurrentRatio calculateCurrentRatio;
    private final CalculateAccountsPayableTurnover accountsPayableTurnover;
    private final CalculateDIO calculateDIO;
    private final CalculateDPO calculateDPO;
    private final CalculateCashConversionCycle cashConversionCycle;
    private final CalculateNetProfitMargin calculateNetProfitMargin;
    private final Utility utility;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> salesDataEvaluation(String intelRxId, String duration) {

        if (utility.isNullOrEmpty(intelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }

        //Year Over Year Sales
        double yoy = yearlySalesDataEvaluation.salesDataEvaluation(intelRxId);
        //String mom = monthlySalesDataEvaluation.MomSalesDataEvaluation(orders);

        // get AverageTransactionValueCalculation
        double atv = averageTransactionValueCalculation.averageTransactionValue(intelRxId, duration);

        // get CalculateCostOfGoodsSold
        double cogs = calculateCostOfGoodsSold.calculateCOGS(intelRxId, duration);

        //Gross Margin Return on Investment (GMROI)
        double gmroi = calculateGMROI.calculateGMROI(intelRxId, duration);

        //calculate Shrinkage
        double shrinkage = calculateShrinkage.calculateShrinkage(intelRxId, duration);

        //calculate sellThrough
        double sellThrough = calculateSellThrough.calculateSellThroughRate(intelRxId, duration);


        SalesDataEvaluationResponse response = new SalesDataEvaluationResponse();
        response.setYoySalesAmount(utility.roundAmount(yoy));
        response.setAverageTransactionAmount(utility.roundAmount(atv));
        response.setCostOfGoodsSoldAmount(utility.roundAmount(cogs));
        response.setGMROI(utility.roundAmount(gmroi));
        response.setShrinkageAmount(utility.roundAmount(shrinkage));
        response.setSellThroughPercentage(utility.roundAmount(sellThrough));

        return ResponseEntity.ok(StandardResponse.success(response));

    }

    public ResponseEntity<?> merchandising(String intelRxId, String duration) {

        if (utility.isNullOrEmpty(intelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }

        // get InventoryTurnover
        double inventoryTurnover = calculateInventoryTurnover.calculateInventoryTurnover(intelRxId, duration);


        MerchandisingResponse response = new MerchandisingResponse();
        response.setInventoryTurnoverAmount(utility.roundAmount(inventoryTurnover));

        return ResponseEntity.ok(StandardResponse.success(response));

    }

    public ResponseEntity<?> customerEvaluation(String intelRxId, String duration) {

        if (utility.isNullOrEmpty(intelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }

        // get InventoryTurnover
        double customerRetentionRates = customerRetentionRate.calculateRetentionRate(intelRxId, duration);


        CustomerEvaluationResponse response = new CustomerEvaluationResponse();
        response.setCustomerRetentionRatePercentage(customerRetentionRates);

        return ResponseEntity.ok(StandardResponse.success(response));

    }

    public ResponseEntity<?> retailFinancial(String intelRxId, String duration) {

        if (utility.isNullOrEmpty(intelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }

        // get Calculate OPEX
        double calculateOPEXs = calculateOPEX.calculateOperatingExpenses(intelRxId, duration);

        // get Calculate QuickRatio
        double calculateQuickRatios = calculateQuickRatio.calculateQuickRatio(intelRxId, duration);

        // get Calculate Current Ratios
        double calculateCurrentRatios = calculateCurrentRatio.calculateCurrentRatio(intelRxId, duration);

        // get AccountsPayableTurnover
        double accountsPayableTurnovers = accountsPayableTurnover.calculateAccountsPayableTurnover(intelRxId, duration);

        // get calculateDIOs
        int calculateDIOs = (int) calculateDIO.getCalculateDIO(intelRxId, duration);

        // get calculateDPOs
        int calculateDPOs = (int) calculateDPO.getCalculateDIO(intelRxId, duration);

        // get cashConversionCycles
        int cashConversionCycles = (int) cashConversionCycle.getCalculateCashConversionCycle(intelRxId, duration);

        // get calculateNetProfitMargins
        double calculateNetProfitMargins = calculateNetProfitMargin.getNetProfitMargin(intelRxId, duration);


        RetailFinancialResponse response = new RetailFinancialResponse();
        response.setOPEXAmount(calculateOPEXs);
        response.setQuickRationPercentage(utility.roundAmount(calculateQuickRatios));
        response.setCurrentRationPercentage(utility.roundAmount(calculateCurrentRatios));
        response.setAccountsPayableTurnoverAmount(utility.roundAmount(accountsPayableTurnovers));
        response.setDaysOfInventoryOutstanding(calculateDIOs + " Days");
        response.setDaysPayableOutstanding(calculateDPOs + " Days");
        response.setCashConversionCycle(cashConversionCycles + " Days");
        response.setNetProfitMarginPercentage(utility.roundAmount(calculateNetProfitMargins));

        return ResponseEntity.ok(StandardResponse.success(response));

    }

    public ResponseEntity<?> addRecommendation(KPIRecommendationRequest request) throws IOException {
        if (utility.isNullOrEmpty(request.getIntelRxId())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        } else if (utility.isNullOrEmpty(request.getRecommendationMsg())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("recommendationMsg is required"));
        } else if (request.getKpiTypeId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId is required"));
        }

        Optional<KPIType> optionalKPIType = kpiTypeRepository.findById(request.getKpiTypeId());
        if (!optionalKPIType.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId does not exist"));
        }
        KPIType kpiType = optionalKPIType.get();

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(request.getIntelRxId());
        if (!optionalPharmacy.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId does not exist"));
        }

        String docs = null;
        if (!utility.isNullOrEmpty(request.getDocs())) {
            docs = s3Service.uploadFileDoc(request.getDocs(), "docs");
        }

        Optional<KPIRecommendation> optionalKPIRecommendation = kpiRecommendationRepository.findByIntelRxIdAndKpiTypeId(request.getIntelRxId(),kpiType.getId());
        KPIRecommendation kpiRecommendation = null;
        if (optionalKPIRecommendation.isPresent()) {
            kpiRecommendation = optionalKPIRecommendation.get();
        }else {
             kpiRecommendation = new KPIRecommendation();
        }

        kpiRecommendation.setKpiType(kpiType);
        kpiRecommendation.setRecommendationMsg(request.getRecommendationMsg());
        kpiRecommendation.setDocs(docs);
        kpiRecommendation.setIntelRxId(request.getIntelRxId());
        kpiRecommendation.setCreatedAt(LocalDateTime.now());

        kpiRecommendationRepository.save(kpiRecommendation);
        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));

    }

    public ResponseEntity<?> updateRecommendation(KPIRecommendationRequest request, Long id) throws IOException {
        if (utility.isNullOrEmpty(request.getIntelRxId())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        } else if (utility.isNullOrEmpty(request.getRecommendationMsg())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("recommendationMsg is required"));
        } else if (request.getKpiTypeId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId is required"));
        } else if (id == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("id is required"));
        }

        Optional<KPIType> optionalKPIType = kpiTypeRepository.findById(request.getKpiTypeId());
        if (!optionalKPIType.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId does not exist"));
        }
        KPIType kpiType = optionalKPIType.get();

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(request.getIntelRxId());
        if (!optionalPharmacy.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId does not exist"));
        }

        Optional<KPIRecommendation> optionalKPIRecommendation = kpiRecommendationRepository.findByIdAndIntelRxId(id, request.getIntelRxId());
        if (!optionalKPIRecommendation.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("id does not exist"));
        }
        KPIRecommendation kpiRecommendation = optionalKPIRecommendation.get();

        String docs = null;
        if (!utility.isNullOrEmpty(request.getDocs())) {
            docs = s3Service.uploadFileDoc(request.getDocs(), "docs");
        } else {
            docs = kpiRecommendation.getDocs();
        }

        kpiRecommendation.setKpiType(kpiType);
        kpiRecommendation.setRecommendationMsg(request.getRecommendationMsg());
        kpiRecommendation.setDocs(docs);
        kpiRecommendation.setIntelRxId(request.getIntelRxId());
        kpiRecommendation.setCreatedAt(LocalDateTime.now());

        kpiRecommendationRepository.save(kpiRecommendation);

        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
    }

//    public ResponseEntity<?> getRecommendation(String intelRxId, Long id) {
//        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
//        if (!optionalPharmacy.isPresent()) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId does not exist"));
//        }
//
//        Optional<KPIRecommendation> optionalKPIRecommendation = kpiRecommendationRepository.findByIdAndIntelRxId(id, intelRxId);
//        if (!optionalKPIRecommendation.isPresent()) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("id does not exist"));
//        }
//        KPIRecommendation kpiRecommendation = optionalKPIRecommendation.get();
//
//        KPIRecommendationRequest kpiRecommendationRequest = mapToKPIRecommendation(kpiRecommendation);
//
//        return ResponseEntity.ok(StandardResponse.success(kpiRecommendationRequest));
//    }

//    public ResponseEntity<?> getAllRecommendation(String intelRxId) {
//        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
//        if (!optionalPharmacy.isPresent()) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId does not exist"));
//        }
//
//        List<KPIRecommendation> kpiRecommendations = kpiRecommendationRepository.findAllByIntelRxId(intelRxId);
//        if (kpiRecommendations.isEmpty()) {
//            return ResponseEntity.ok().body(StandardResponse.success(kpiRecommendations));
//        }
//
//
//        List<KPIRecommendationRequest> kpiRecommendationRequest = kpiRecommendations.stream().map(
//                kpiRecommendation -> mapToKPIRecommendation(kpiRecommendation)
//        ).collect(Collectors.toList());
//
//        return ResponseEntity.ok(StandardResponse.success(kpiRecommendationRequest));
//    }

    public ResponseEntity<?> getRecommendation(String intelRxId,Long kpiTypeId) {
        if (utility.isNullOrEmpty(intelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        }

        if(kpiTypeId == null){
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId is required"));
        }

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
        if (!optionalPharmacy.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId does not exist"));
        }

        Optional<KPIType> optionalKPIType = kpiTypeRepository.findById(kpiTypeId);
        if (!optionalKPIType.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId does not exist"));
        }

        KPIType kpiType = optionalKPIType.get();

        Optional<KPIRecommendation> optional = kpiRecommendationRepository.findByIntelRxIdAndKpiTypeId(intelRxId,kpiType.getId());
        if (!optional.isPresent()) {
            return ResponseEntity.ok().body(StandardResponse.success("Empty"));
        }
        KPIRecommendation kpiRecommendation = optional.get();
        KPIRecommendationRequest kpiRecommendationRequest = mapToKPIRecommendation(kpiRecommendation);

        return ResponseEntity.ok(StandardResponse.success(kpiRecommendationRequest));
    }

    public KPIRecommendationRequest mapToKPIRecommendation(KPIRecommendation kpiRecommendation) {
        String docs = "";
        if (kpiRecommendation.getDocs() != null) {
            S3Service.FetchedImage fetchedPurchaseInvoice = s3Service.fetchImage(kpiRecommendation.getDocs()); // Replace "your_image_name.jpg" with the actual image name
            if (fetchedPurchaseInvoice != null) {
                docs = fetchedPurchaseInvoice.getImageUrl();
            }
        }

        return KPIRecommendationRequest.builder()
                .id(kpiRecommendation.getId())
                .intelRxId(kpiRecommendation.getIntelRxId())
                .recommendationMsg(kpiRecommendation.getRecommendationMsg())
                .docs(docs)
                .kpiType(kpiRecommendation.getKpiType())
                .createdAt(kpiRecommendation.getCreatedAt())
                .build();
    }

    public ResponseEntity<?> deleteRecommendation(KPIRecommendationDeleteRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }
        Admin user = (Admin) authentication.getPrincipal();
        boolean check_password = this.passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!check_password) {
            return ResponseEntity.ok().body(new RegisterRequest(String.valueOf("failed"), "Password not match"));
        }

        if (utility.isNullOrEmpty(request.getIntelRxId())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        } else if (utility.isNullOrEmpty(request.getPassword())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("password is required"));
        } else if (request.getKpiTypeId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId is required"));
        }


        Optional<KPIRecommendation> optional = kpiRecommendationRepository.findByIntelRxIdAndKpiTypeId(request.getIntelRxId(), request.getKpiTypeId());
        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
        }
        KPIRecommendation kpiRecommendation = optional.get();
        kpiRecommendationRepository.delete(kpiRecommendation);
        return ResponseEntity.ok(StandardResponse.success("Deleted Successfully"));
    }
}
