package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.admin.dto.kpi.*;
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
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyKPIServices")
public class PharmacyKPIServices {
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
    private final UserDetailsService userDetailsService;
    private final KPITypeRepository kpiTypeRepository;
    private final CalculateCurrentRatio calculateCurrentRatio;
    private final CalculateAccountsPayableTurnover accountsPayableTurnover;
    private final CalculateDIO calculateDIO;
    private final CalculateDPO calculateDPO;
    private final CalculateCashConversionCycle cashConversionCycle;
    private final CalculateNetProfitMargin calculateNetProfitMargin;
    private final S3Service s3Service;
    private final Utility utility;

    public ResponseEntity<?> salesDataEvaluation(String duration) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
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

    public ResponseEntity<?> customerEvaluation(String duration) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("not authenticated"));
        }

        double customerRetentionRates = customerRetentionRate.calculateRetentionRate(intelRxId, duration);

        CustomerEvaluationResponse response = new CustomerEvaluationResponse();
        response.setCustomerRetentionRatePercentage(utility.roundAmount(customerRetentionRates));

        return ResponseEntity.ok(StandardResponse.success(response));

    }

    public ResponseEntity<?> merchandising(String duration) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("not authenticated"));
        }

        // get InventoryTurnover
        double inventoryTurnover = calculateInventoryTurnover.calculateInventoryTurnover(intelRxId, duration);

        MerchandisingResponse response = new MerchandisingResponse();
        response.setInventoryTurnoverAmount(utility.roundAmount(inventoryTurnover));

        return ResponseEntity.ok(StandardResponse.success(response));

    }

    public ResponseEntity<?> retailFinancial(String duration) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("not authenticated"));
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

    public ResponseEntity<?> getRecommendation(Long id) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("not authenticated"));
        }

        // Add logging to verify values
        System.out.println("Fetching Pharmacy with intelRxId: " + intelRxId);
        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
        if (!optionalPharmacy.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId does not exist"));
        }

        System.out.println("Fetching KPIRecommendation with id: " + id + " and intelRxId: " + intelRxId);
        Optional<KPIRecommendation> optionalKPIRecommendation = kpiRecommendationRepository.findByIdAndIntelRxId(id, intelRxId);
        if (!optionalKPIRecommendation.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("id does not exist"));
        }
        KPIRecommendation kpiRecommendation = optionalKPIRecommendation.get();

        KPIRecommendationRequest kpiRecommendationRequest = mapToKPIRecommendation(kpiRecommendation);

        return ResponseEntity.ok(StandardResponse.success(kpiRecommendationRequest));
    }

    public ResponseEntity<?> getAllRecommendation(Long kpiTypeId) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("not authenticated"));
        }

        if (kpiTypeId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId is required"));
        }

        // Add logging to verify values
        System.out.println("Fetching Pharmacy with intelRxId: " + intelRxId);
        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
        if (!optionalPharmacy.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId does not exist"));
        }

        System.out.println("Fetching KPIType with id: " + kpiTypeId);
        Optional<KPIType> optionalKPIType = kpiTypeRepository.findById(kpiTypeId);
        if (!optionalKPIType.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("kpiTypeId does not exist"));
        }

        KPIType kpiType = optionalKPIType.get();

        System.out.println("Fetching KPIRecommendations with intelRxId: " + intelRxId + " and kpiType: " + kpiType);
        List<KPIRecommendation> kpiRecommendations = kpiRecommendationRepository.findAllByIntelRxIdAndKpiType(intelRxId, kpiType);
        if (kpiRecommendations.isEmpty()) {
            return ResponseEntity.ok().body(StandardResponse.success(kpiRecommendations));
        }

        List<KPIRecommendationRequest> kpiRecommendationRequest = kpiRecommendations.stream().map(
                kpiRecommendation -> mapToKPIRecommendation(kpiRecommendation)
        ).collect(Collectors.toList());

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


}
