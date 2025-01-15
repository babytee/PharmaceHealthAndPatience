package com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CalculateOPEX {
    private final GeneralCalculation generalCalculation;
    private final ExpenditureRepository expenditureRepository;
    public double calculateOperatingExpenses(String intelRxId, String duration) {

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double totalExpenditures = expenditureRepository.getTotalExpenditures(startDate, endDate, intelRxId);

        if (totalExpenditures == null) {
            totalExpenditures = 0.0;
        }

        return totalExpenditures;
    }

}
