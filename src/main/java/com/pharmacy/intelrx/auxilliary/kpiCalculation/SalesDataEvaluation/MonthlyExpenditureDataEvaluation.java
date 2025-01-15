package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MonthlyExpenditureDataEvaluation {

    public List<Expenditure> filterExpByMonth(List<Expenditure> expenditures, YearMonth yearMonth) {
        List<Expenditure> filteredExpenditure = new ArrayList<>();
        for (Expenditure expenditure : expenditures) {
            LocalDate expenditureDate = expenditure.getCreatedAt().toLocalDate();
            YearMonth orderYearMonth = YearMonth.of(expenditureDate.getYear(), expenditureDate.getMonth());
            if (orderYearMonth.equals(yearMonth)) {
                filteredExpenditure.add(expenditure);
            }
        }
        return filteredExpenditure;
    }

    public List<Expenditure> filterExpTypeByMonth(List<Expenditure> expenditures, YearMonth yearMonth,String expType) {
        List<Expenditure> filteredExpenditures= new ArrayList<>();
        for (Expenditure expenditure : expenditures) {
            if(expenditure.getExpenditureType().equals(expType)) {
                LocalDate cartDate = expenditure.getCreatedAt().toLocalDate();
                YearMonth cartYearMonth = YearMonth.of(cartDate.getYear(), cartDate.getMonth());
                if (cartYearMonth.equals(yearMonth)) {
                    filteredExpenditures.add(expenditure);
                }
            }
        }
        return filteredExpenditures;
    }


    public double calculateTotalSales(List<Expenditure> expenditures) {
        double totalSales = 0;
        for (Expenditure expenditure : expenditures) {
            totalSales += expenditure.getAmountSpent();
        }
        return totalSales;
    }


    public double calculateTotalExpSales(List<Expenditure> expenditures) {
        double totalSales = 0;
        for (Expenditure expenditure : expenditures) {
            if(expenditure.isApproved()) {
                totalSales += expenditure.getAmountSpent();
            }
        }
        return totalSales;
    }
}

