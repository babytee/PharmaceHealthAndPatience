package com.pharmacy.intelrx.auxilliary.kpiCalculation.CustomerEvaluation;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.CalculateCostOfGoodsSold;
import com.pharmacy.intelrx.pharmacy.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CustomerRetentionRate {
    private final CalculateCostOfGoodsSold calculateCostOfGoodsSold;
    private final GeneralCalculation generalCalculation;
    private final PatientRepository patientRepository;

    public double calculateRetentionRate(String intelRxId, String duration) {
        /**
         Customer Retention Rate (CRR)
         Customer Retention Rate is calculated using the formula:
         CRR=((E−N)/S)×100
         Where:
         E = Number of customers at the end of the period.
         N = Number of new customers acquired during the period.
         S = Number of customers at the start of the period.
         **/
        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date


        long startCount = patientRepository.countByIntelRxIdAndCreatedAt(intelRxId, startDate);
        long endCount = patientRepository.countByIntelRxIdAndCreatedAt(intelRxId,endDate);
        long newPatients = patientRepository.countByIntelRxIdAndCreatedAtBetween(intelRxId, startDate, endDate);

        if (startCount == 0) {
            return 0;
        }

        double retentionRate = ((double) (endCount - newPatients) / startCount) * 100;
        return retentionRate;
    }

}
