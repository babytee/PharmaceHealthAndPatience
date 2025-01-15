package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.MonthlyExpenditureDataEvaluation;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.MonthlySalesDataEvaluation;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.TopSellingItemProjection;
import com.pharmacy.intelrx.pharmacy.dto.MiscellaneousExpenditureDTO;
import com.pharmacy.intelrx.pharmacy.dto.MonthlyExpenditureRevenueAnalyticResponse;
import com.pharmacy.intelrx.pharmacy.dto.MonthlySalesRevenueAnalyticResponse;
import com.pharmacy.intelrx.pharmacy.dto.TopSellingItemDTO;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRepository;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyBranchRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyAnalyticService")
public class PharmacyAnalyticService {
    private final OrderRepository orderRepository;
    private final MonthlySalesDataEvaluation monthlySalesDataEvaluation;
    private final UserDetailsService userDetailsService;
    private final GeneralCalculation generalCalculation;
    private final PharmacyBranchRepository pharmacyBranchRepository;
    private final CartItemRepository cartItemRepository;
    private final Utility utility;
    private final ExpenditureRepository expenditureRepository;
    private final MonthlyExpenditureDataEvaluation monthlyExpenditureDataEvaluation;

    public ResponseEntity<?> monthlySalesRevenue(int year, int month, String duration, Long branchId) {

        MonthlySalesRevenueAnalyticResponse monthlySalesRevenueAnalyticResponse = new MonthlySalesRevenueAnalyticResponse();
        var salesRevenue = getMonthlySalesRevenue(year, month, duration, branchId);

        double salesBreakDownPercentMedication = 0.00;
        Map salesBreakDownMedication = null;
        try {
            salesBreakDownMedication = getBreakSalesRevenueBreakDown(year, month, duration, branchId, "MEDICATION");
            salesBreakDownPercentMedication = Double.valueOf((String) salesBreakDownMedication.get("CurrentMonth"));
        } catch (ClassCastException e) {
            // Handle the exception (e.g., log an error message, set a default value, etc.)
            e.printStackTrace(); // Example: printing stack trace
        }

        double salesBreakDownPercentGrocery = 0.00;
        Map salesBreakDownGrocery = null;
        try {
            salesBreakDownGrocery = getBreakSalesRevenueBreakDown(year, month, duration, branchId, "GROCERY");
            salesBreakDownPercentGrocery = Double.valueOf((String) salesBreakDownGrocery.get("CurrentMonth"));
        } catch (ClassCastException e) {
            // Handle the exception (e.g., log an error message, set a default value, etc.)
            e.printStackTrace(); // Example: printing stack trace
        }

        Map<String, Object> getSalesBrakeDown = new HashMap<>();
        getSalesBrakeDown.put("MedicationSales", salesBreakDownMedication);
        getSalesBrakeDown.put("GrocerySales", salesBreakDownGrocery);

        Map getPercent = getPercentageSold(salesBreakDownPercentMedication, salesBreakDownPercentGrocery);

        monthlySalesRevenueAnalyticResponse.setMonthlyRevenue(salesRevenue);
        monthlySalesRevenueAnalyticResponse.setGetMonthlySalesPercentage(getPercent);
        monthlySalesRevenueAnalyticResponse.setGetMonthlySalesBreakDown(getSalesBrakeDown);

        return ResponseEntity.ok(StandardResponse.success(monthlySalesRevenueAnalyticResponse));
    }

    private Map getMonthlySalesRevenue(int year, int month, String duration, Long branchId) {
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<PharmacyBranch> branchOptional = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        PharmacyBranch branch = null;
        if (branchOptional.isPresent()) {
            branch = branchOptional.get();
        } else {
            branch = userDetailsService.getBranch();
        }

        List<Order> orders = null;

        if (month < 1 || year < 1) {

            // Determine start and end dates based on the provided duration
            LocalDateTime startDate = duration == null ? LocalDateTime.now().minusDays(30) : generalCalculation.calculateStartDateBasedOnDuration(duration);
            LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

            orders = orderRepository.findAllByIntelRxIdAndOrderedDateBetweenAndPharmacyBranch(
                    intelRxId, startDate, endDate, branch);

        } else if (month > 0 || year > 0) {

            orders = orderRepository.findAllByIntelRxIdAndOrderedDateYearAndOrderedDateMonthAndPharmacyBranch(
                    intelRxId,
                    year,
                    month,
                    branch);

        } else {
            orders = orderRepository.findAllByIntelRxIdAndPharmacyBranch(intelRxId, branch);
        }
        // Filter orders for the current and previous months
        int currentMonth = 0;
        if (month < 1 || year < 1) {
            // Get the current year and month
            YearMonth currentYearMonth = YearMonth.now();
            // Get the current month as an integer
            currentMonth = currentYearMonth.getMonthValue();
        } else {
            currentMonth = month;
        }

        List<Order> currentMonthOrders = monthlySalesDataEvaluation.filterOrdersByMonth(orders, YearMonth.now().withMonth(currentMonth));
        List<Order> previousMonthOrders = monthlySalesDataEvaluation.filterOrdersByMonth(orders, YearMonth.now().withMonth(currentMonth).minusMonths(1));

        // Calculate total sales for the current and previous months
        double currentMonthSales = monthlySalesDataEvaluation.calculateTotalSales(currentMonthOrders);
        double previousMonthSales = monthlySalesDataEvaluation.calculateTotalSales(previousMonthOrders);

        // Calculate MoM sales difference
        double momSalesDifference = utility.roundAmount(currentMonthSales - previousMonthSales);

// Determine if current month sales are higher, lower, or equal to previous month
        String comparisonResult;
        if (momSalesDifference > 0) {
            comparisonResult = "higher than";
        } else if (momSalesDifference < 0) {
            comparisonResult = "lower than";
        } else {
            comparisonResult = "equal to";
        }

// Format momSalesDifference to ensure it always has two decimal places
        String formattedDifference = String.format("%.2f", momSalesDifference);
        String msg = "Current month sales are " + comparisonResult + " previous month by " + formattedDifference;

// Format currentMonthSales to ensure it always has two decimal places
        String formattedCurrentMonthSales = String.format("%.2f", currentMonthSales);

        Map<String, String> stringList = new HashMap<>();
        stringList.put("CurrentMonth", formattedCurrentMonthSales);
        stringList.put("PastMonthDiff", msg);

        return stringList;


    }

    private Map getBreakSalesRevenueBreakDown(int year, int month, String duration, Long branchId, String inventoryType) {
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<PharmacyBranch> branchOptional = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        PharmacyBranch branch = null;
        if (branchOptional.isPresent()) {
            branch = branchOptional.get();
        } else {
            branch = userDetailsService.getBranch();
        }

        List<CartItem> cartItems = null;

        if (month < 1 || year < 1) {

            // Determine start and end dates based on the provided duration
            LocalDateTime startDate = duration == null ? LocalDateTime.now().minusDays(30) : generalCalculation.calculateStartDateBasedOnDuration(duration);
            LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

            cartItems = cartItemRepository.findAllByIntelRxIdAndCreatedAtBetweenAndPharmacyBranchAndStatusAndInventoryType(
                    intelRxId, startDate, endDate, branch, true, inventoryType);

        } else {
            cartItems = cartItemRepository.
                    findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndPharmacyBranchAndStatusAndInventoryType
                            (
                                    intelRxId,
                                    year,
                                    month,
                                    branch,
                                    true,
                                    inventoryType
                            );


        }
        // Filter orders for the current and previous months
        int currentMonth = 0;
        if (month < 1 || year < 1) {
            // Get the current year and month
            YearMonth currentYearMonth = YearMonth.now();
            // Get the current month as an integer
            currentMonth = currentYearMonth.getMonthValue();
        } else {
            currentMonth = month;
        }
        List<CartItem> currentMonthOrders = monthlySalesDataEvaluation.filterCartsByMonth(cartItems, YearMonth.now().withMonth(currentMonth));

        List<CartItem> previousMonthOrders = monthlySalesDataEvaluation.filterCartsByMonth(cartItems, YearMonth.now().withMonth(currentMonth).minusMonths(1));

        // Calculate total sales for the current and previous months
        double currentMonthSales = monthlySalesDataEvaluation.calculateTotalCartSales(currentMonthOrders);
        double previousMonthSales = monthlySalesDataEvaluation.calculateTotalCartSales(previousMonthOrders);

        // Calculate MoM sales difference
        double momSalesDifference = utility.roundAmount(currentMonthSales - previousMonthSales);

        // Determine if current month sales are higher, lower, or equal to previous month
        String comparisonResult;
        if (momSalesDifference > 0) {
            comparisonResult = "higher than";
        } else if (momSalesDifference < 0) {
            comparisonResult = "lower than";
        } else {
            comparisonResult = "equal to";
        }
        // Format momSalesDifference to ensure it always has two decimal places
        String formattedDifference = String.format("%.2f", momSalesDifference);

        String msg = "Current month sales are " + comparisonResult + " previous month by " + formattedDifference;
// Format currentMonthSales to ensure it always has two decimal places
        String formattedCurrentMonthSales = String.format("%.2f", currentMonthSales);

        Map<String, String> stringList = new HashMap<>();
        stringList.put("CurrentMonth", formattedCurrentMonthSales);
        stringList.put("PastMonthDiff", msg);

        return stringList;

    }

    private Map getPercentageSold(double medicationSold, double groceriesSold) {
        // Total number of items sold
        double totalSold = groceriesSold + medicationSold;

        // Initialize percentage variables
        double percentageGroceriesSold = 0.0;
        double percentageMedicationSold = 0.0;

        // Check if totalSold is not zero to avoid division by zero
        if (totalSold != 0.0) {
            // Calculate the percentage of groceries items sold
            percentageGroceriesSold =utility.roundAmount(groceriesSold / totalSold * 100);

            // Calculate the percentage of medication items sold
            percentageMedicationSold =utility.roundAmount(medicationSold / totalSold * 100);
        }

        // If totalSold is zero, set the percentages to zero
        // Otherwise, calculate the percentages as usual


        Map<String, String> getPercentage = new HashMap<>();
        getPercentage.put("MedicationSales", String.format("%.2f",percentageMedicationSold));
        getPercentage.put("GrocerySales", String.format("%.2f",percentageGroceriesSold));

        return getPercentage;

    }

    //yearlyInsight revenue insight
    private Map yearlyInsight(int year, int month, Long branchId) {
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<PharmacyBranch> branchOptional = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        PharmacyBranch branch = branchOptional.orElse(userDetailsService.getBranch());

        // Use the provided month if it is greater than 0, otherwise use the current month
        int targetMonth = (month > 0) ? month : YearMonth.now().getMonthValue();

        List<CartItem> medicationOrders = cartItemRepository.findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndPharmacyBranchAndStatusAndInventoryType(
                intelRxId,
                year,
                targetMonth,
                branch,
                true,
                "MEDICATION");

        List<CartItem> groceryOrders = cartItemRepository.findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndPharmacyBranchAndStatusAndInventoryType(
                intelRxId,
                year,
                targetMonth,
                branch,
                true,
                "GROCERY");

//        List<CartItem> medicationOrders = monthlySalesDataEvaluation.filterCartsByMonth(cartItems, YearMonth.now().withMonth(currentMonth), "MEDICATION");
//        List<CartItem> groceryOrders = monthlySalesDataEvaluation.filterCartsByMonth(cartItems, YearMonth.now().withMonth(currentMonth), "GROCERY");

        // Calculate total sales for the current and previous months
        double medicationSales = monthlySalesDataEvaluation.calculateTotalCartSales(medicationOrders);
        double grocerySales = monthlySalesDataEvaluation.calculateTotalCartSales(groceryOrders);

        Map<String, Double> sales = new HashMap<>();
        sales.put("Medication", utility.roundAmount(medicationSales));
        sales.put("Grocery", utility.roundAmount(grocerySales));

        return sales;
    }

    public ResponseEntity<?> yearlyRevenueInsight(int year, Long branchId) {
        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        insight.put("Year", year < 1 ? YearMonth.now().getYear() : year);
        insight.put("Jan", yearlyInsight(year, 1, branchId));
        insight.put("Feb", yearlyInsight(year, 2, branchId));
        insight.put("Mar", yearlyInsight(year, 3, branchId));
        insight.put("Apr", yearlyInsight(year, 4, branchId));
        insight.put("May", yearlyInsight(year, 5, branchId));
        insight.put("Jun", yearlyInsight(year, 6, branchId));
        insight.put("Jul", yearlyInsight(year, 7, branchId));
        insight.put("Aug", yearlyInsight(year, 8, branchId));
        insight.put("Sep", yearlyInsight(year, 9, branchId));
        insight.put("Oct", yearlyInsight(year, 10, branchId));
        insight.put("Nov", yearlyInsight(year, 11, branchId));
        insight.put("Dec", yearlyInsight(year, 12, branchId));

        return ResponseEntity.ok(StandardResponse.success(insight));
    }

    public ResponseEntity<?> topSellingProducts() {
        var intelRx = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch() != null ? userDetailsService.getBranch().getId() : null;
        List<TopSellingItemDTO> sellingItemDTOS = getTopSellingInventoryItems(branch, intelRx);

        return ResponseEntity.ok(StandardResponse.success(sellingItemDTOS));

    }

    public List<TopSellingItemDTO> getTopSellingInventoryItems(Long pharmacyBranchId, String intelRxId) {
        List<TopSellingItemProjection> results =
                cartItemRepository.findTopSellingInventoryItemsByBranchAndIntelRxId
                        (pharmacyBranchId, intelRxId);

//        if (results.isEmpty()) {
//            return List.of(new TopSellingItemDTO("No data", "0.00", "+0.0%"));
//        }

        return results.stream()
                .map(result -> {
                    String itemName = result.getItemName();
                    double totalSalesAmount = result.getTotalSalesAmount();
                    double previousSalesAmount = result.getPreviousSalesAmount();

                    // Format the sales amount as currency
//                    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
                    String formattedSalesAmount = String.format("%.2f",totalSalesAmount); //currencyFormatter.format(totalSalesAmount);

                    // Calculate the percentage change
                    String percentageChange = calculatePercentageChange(totalSalesAmount, previousSalesAmount);

                    return new TopSellingItemDTO(itemName, formattedSalesAmount, percentageChange);
                })
                .collect(Collectors.toList());
    }

    public String calculatePercentageChange(double totalSalesAmount, double previousSalesAmount) {
        if (previousSalesAmount == 0) {
            return "+0.0%";
        }
        double change = ((totalSalesAmount - previousSalesAmount) / previousSalesAmount) * 100;
        return String.format("%+.1f%%", change);
    }

    //weekly sales revenue insight
    private Map<String, Double> DateRangeInsight(LocalDate startDate, LocalDate endDate, Long branchId) {
        String intelRxId = userDetailsService.getIntelRxId();
        PharmacyBranch branch = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId)
                .orElseGet(userDetailsService::getBranch);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // to include the whole end day

        List<CartItem> medicationOrders = cartItemRepository.findAllByIntelRxIdAndCreatedAtBetweenAndPharmacyBranchAndStatusAndInventoryType(
                intelRxId, startDateTime, endDateTime, branch, true, "MEDICATION");

        List<CartItem> groceryOrders = cartItemRepository.findAllByIntelRxIdAndCreatedAtBetweenAndPharmacyBranchAndStatusAndInventoryType(
                intelRxId, startDateTime, endDateTime, branch, true, "GROCERY");

        double medicationSales = monthlySalesDataEvaluation.calculateTotalCartSales(medicationOrders);
        double grocerySales = monthlySalesDataEvaluation.calculateTotalCartSales(groceryOrders);

        Map<String, Double> sales = new HashMap<>();
        sales.put("Medication", utility.roundAmount(medicationSales));
        sales.put("Grocery", utility.roundAmount(grocerySales));

        return sales;
    }

    public ResponseEntity<?> rangeRevenueInsight(String start_date, String end_date, Long branchId) {
        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        insight.put("DateRange", start_date + " - " + end_date);

        LocalDate startDate = utility.convertStringToLocalDate(start_date);
        LocalDate endDate = utility.convertStringToLocalDate(end_date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        List<Map<String, Object>> dailyInsights = new ArrayList<>();

        LocalDate currentStartDate = startDate;
        while (currentStartDate.isBefore(endDate) || currentStartDate.isEqual(endDate)) {
            LocalDate currentEndDate = currentStartDate.plusDays(1).minusDays(1); // end of the day
            if (currentEndDate.isAfter(endDate)) {
                currentEndDate = endDate; // do not go past the end date
            }

            Map<String, Double> dailySales = DateRangeInsight(currentStartDate, currentEndDate, branchId);

            Map<String, Object> dailyInsight = new LinkedHashMap<>();
            dailyInsight.put("DateRange", currentStartDate.format(formatter));
            dailyInsight.putAll(dailySales);

            dailyInsights.add(dailyInsight);

            currentStartDate = currentStartDate.plusDays(1); // move to the next day
        }

        insight.put("WeeklyInsights", dailyInsights);

        return ResponseEntity.ok(StandardResponse.success(insight));
    }
    //weekly sales revenue insight end


    //Expenditure
    public ResponseEntity<?> monthlyExpenditureRevenue(int year, int month, String duration, Long branchId) {

        MonthlyExpenditureRevenueAnalyticResponse monthlyExpenditureRevenueAnalyticResponse = new MonthlyExpenditureRevenueAnalyticResponse();
        var salesRevenue = getMonthlyExpenditureRevenue(year, month, duration, branchId);

        double expBreakDownPercentSalaries = 0.0;
        Map expBreakDownSalaries = null;
        try {
            expBreakDownSalaries = getBreakExpenditureRevenueBreakDown(year, month, duration, branchId, "Salaries");
            expBreakDownPercentSalaries = Double.valueOf((String) expBreakDownSalaries.get("CurrentMonth"));
        } catch (ClassCastException e) {
            // Handle the exception (e.g., log an error message, set a default value, etc.)
            e.printStackTrace(); // Example: printing stack trace
        }

        double expBreakDownPercentMiscellaneous = 0.0;
        Map expBreakDownMiscellaneous = null;
        try {
            expBreakDownMiscellaneous = getBreakExpenditureRevenueBreakDown(year, month, duration, branchId, "Miscellaneous");
            expBreakDownPercentMiscellaneous = Double.valueOf((String) expBreakDownMiscellaneous.get("CurrentMonth"));
        } catch (ClassCastException e) {
            // Handle the exception (e.g., log an error message, set a default value, etc.)
            e.printStackTrace(); // Example: printing stack trace
        }

        Map<String, Object> getExpenditureBrakeDown = new HashMap<>();
        getExpenditureBrakeDown.put("Salaries", expBreakDownSalaries);
        getExpenditureBrakeDown.put("Miscellaneous", expBreakDownMiscellaneous);

        Map getPercent = getExpenditurePercentage(expBreakDownPercentSalaries, expBreakDownPercentMiscellaneous);

        monthlyExpenditureRevenueAnalyticResponse.setMonthlyRevenue(salesRevenue);
        monthlyExpenditureRevenueAnalyticResponse.setGetMonthlyExpenditurePercentage(getPercent);
        monthlyExpenditureRevenueAnalyticResponse.setGetMonthlyExpenditureBreakDown(getExpenditureBrakeDown);

        return ResponseEntity.ok(StandardResponse.success(monthlyExpenditureRevenueAnalyticResponse));
    }

    private Map getMonthlyExpenditureRevenue(int year, int month, String duration, Long branchId) {
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<PharmacyBranch> branchOptional = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        PharmacyBranch branch = null;
        if (branchOptional.isPresent()) {
            branch = branchOptional.get();
        } else {
            branch = userDetailsService.getBranch();
        }

        List<Expenditure> expenditures = null;

        if (month < 1 || year < 1) {

            // Determine start and end dates based on the provided duration
            LocalDateTime startDate = duration == null ? LocalDateTime.now().minusDays(30) : generalCalculation.calculateStartDateBasedOnDuration(duration);
            LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

            expenditures = expenditureRepository.findAllByIntelRxIdAndCreatedAtBetweenAndExpenditureTypeAndBranch(
                    intelRxId, startDate, endDate, null, branch);

        } else if (month > 0 || year > 0) {

            expenditures = expenditureRepository.findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndBranch(
                    intelRxId,
                    year,
                    month,
                    null,
                    branch);

        } else {
            expenditures = expenditureRepository.findAllByIntelRxIdAndBranch(intelRxId, branch);
        }
        // Filter orders for the current and previous months
        int currentMonth = 0;
        if (month < 1 || year < 1) {
            // Get the current year and month
            YearMonth currentYearMonth = YearMonth.now();
            // Get the current month as an integer
            currentMonth = currentYearMonth.getMonthValue();
        } else {
            currentMonth = month;
        }

        List<Expenditure> currentMonthOrders = monthlyExpenditureDataEvaluation.filterExpByMonth(expenditures, YearMonth.now().withMonth(currentMonth));
        List<Expenditure> previousMonthOrders = monthlyExpenditureDataEvaluation.filterExpByMonth(expenditures, YearMonth.now().withMonth(currentMonth).minusMonths(1));

        // Calculate total sales for the current and previous months
        double currentMonthSales = monthlyExpenditureDataEvaluation.calculateTotalSales(currentMonthOrders);
        double previousMonthSales = monthlyExpenditureDataEvaluation.calculateTotalSales(previousMonthOrders);

        // Calculate MoM sales difference
        double momSalesDifference = utility.roundAmount(currentMonthSales - previousMonthSales);

        // Determine if current month sales are higher, lower, or equal to previous month
        String comparisonResult;
        if (momSalesDifference > 0) {
            comparisonResult = "higher than";
        } else if (momSalesDifference < 0) {
            comparisonResult = "lower than";
        } else {
            comparisonResult = "equal to";
        }
        String msg = "Current month expense are " + comparisonResult + " previous month by " + String.format("%.2f",momSalesDifference);

        Map<String, String> stringList = new HashMap<>();
        stringList.put("CurrentMonth", String.format("%.2f",currentMonthSales));
        stringList.put("PastMonthDiff", msg);

        return stringList;

    }

    private Map getBreakExpenditureRevenueBreakDown(int year, int month, String duration, Long branchId, String expType) {
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<PharmacyBranch> branchOptional = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        PharmacyBranch branch = null;
        if (branchOptional.isPresent()) {
            branch = branchOptional.get();
        } else {
            branch = userDetailsService.getBranch();
        }

        List<Expenditure> expenditures = null;

        if (month < 1 || year < 1) {

            // Determine start and end dates based on the provided duration
            LocalDateTime startDate = duration == null ? LocalDateTime.now().minusDays(30) : generalCalculation.calculateStartDateBasedOnDuration(duration);
            LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

            expenditures = expenditureRepository.findAllByIntelRxIdAndCreatedAtBetweenAndExpenditureTypeAndBranch(
                    intelRxId, startDate, endDate, null, branch);

        } else if (month > 0 || year > 0) {

            expenditures = expenditureRepository.findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndBranch(
                    intelRxId,
                    year,
                    month,
                    null,
                    branch);

        }
        // Filter orders for the current and previous months
        int currentMonth = 0;
        if (month < 1 || year < 1) {
            // Get the current year and month
            YearMonth currentYearMonth = YearMonth.now();
            // Get the current month as an integer
            currentMonth = currentYearMonth.getMonthValue();
        } else {
            currentMonth = month;
        }
        List<Expenditure> currentMonthExps = monthlyExpenditureDataEvaluation.filterExpTypeByMonth(expenditures, YearMonth.now().withMonth(currentMonth), expType);

        List<Expenditure> previousMonthExps = monthlyExpenditureDataEvaluation.filterExpTypeByMonth(expenditures, YearMonth.now().withMonth(currentMonth).minusMonths(1), expType);

        // Calculate total expenditures for the current and previous months
        double currentMonthSales = monthlyExpenditureDataEvaluation.calculateTotalExpSales(currentMonthExps);
        double previousMonthSales = monthlyExpenditureDataEvaluation.calculateTotalExpSales(previousMonthExps);

        // Calculate MoM expenditures difference
        double momSalesDifference = utility.roundAmount(currentMonthSales - previousMonthSales);

        // Determine if current month expenditures are higher, lower, or equal to previous month
        String comparisonResult;
        if (momSalesDifference > 0) {
            comparisonResult = "higher than";
        } else if (momSalesDifference < 0) {
            comparisonResult = "lower than";
        } else {
            comparisonResult = "equal to";
        }
        String msg = "Current month expense are " + comparisonResult + " previous month by " + String.format("%.2f",momSalesDifference);

        Map<String, String> stringList = new HashMap<>();
        stringList.put("CurrentMonth", String.format("%.2f",currentMonthSales));
        stringList.put("PastMonthDiff", msg);

        return stringList;

    }

    private Map getExpenditurePercentage(double Salaries, double Miscellaneous) {
        // Total number of items sold
        double totalSold = Salaries + Miscellaneous;

        // Initialize percentage variables
        double percentageSalaries = 0.0;
        double percentageMiscellaneous = 0.0;

        // Check if totalExpenditure is not zero to avoid division by zero
        if (totalSold != 0.0) {
            // Calculate the percentage of Salaries
            percentageSalaries = Salaries / totalSold * 100;

            // Calculate the percentage of medication items sold
            percentageMiscellaneous = Miscellaneous / totalSold * 100;
        }

        // If totalExpenditure is zero, set the percentages to zero
        // Otherwise, calculate the percentages as usual


        Map<String, String> getPercentage = new HashMap<>();
        getPercentage.put("Salaries", String.format("%.2f",utility.roundAmount(percentageSalaries)));
        getPercentage.put("Miscellaneous", String.format("%.2f",utility.roundAmount(percentageMiscellaneous)));

        return getPercentage;

    }


    //revenue insight
    public ResponseEntity<?> yearlyExpenditureInsight(int year, Long branchId) {
        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        insight.put("Year", year < 1 ? YearMonth.now().getYear() : year);
        insight.put("Jan", yearlyExpenditureInsight(year, 1, branchId));
        insight.put("Feb", yearlyExpenditureInsight(year, 2, branchId));
        insight.put("Mar", yearlyExpenditureInsight(year, 3, branchId));
        insight.put("Apr", yearlyExpenditureInsight(year, 4, branchId));
        insight.put("May", yearlyExpenditureInsight(year, 5, branchId));
        insight.put("Jun", yearlyExpenditureInsight(year, 6, branchId));
        insight.put("Jul", yearlyExpenditureInsight(year, 7, branchId));
        insight.put("Aug", yearlyExpenditureInsight(year, 8, branchId));
        insight.put("Sep", yearlyExpenditureInsight(year, 9, branchId));
        insight.put("Oct", yearlyExpenditureInsight(year, 10, branchId));
        insight.put("Nov", yearlyExpenditureInsight(year, 11, branchId));
        insight.put("Dec", yearlyExpenditureInsight(year, 12, branchId));

        return ResponseEntity.ok(StandardResponse.success(insight));
    }

    private Map yearlyExpenditureInsight(int year, int month, Long branchId) {
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<PharmacyBranch> branchOptional = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        PharmacyBranch branch = branchOptional.orElse(userDetailsService.getBranch());

        // Use the provided month if it is greater than 0, otherwise use the current month
        int targetMonth = (month > 0) ? month : YearMonth.now().getMonthValue();

        List<Expenditure> salariesMonthExps = expenditureRepository.findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndBranch(
                intelRxId,
                year,
                targetMonth,
                "Salaries",
                branch);

        List<Expenditure> miscellaneousMonthExps = expenditureRepository.findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndBranch(
                intelRxId,
                year,
                targetMonth,
                "Miscellaneous",
                branch);

        // Calculate total expenditures for the current and previous months
        double salariesMonthSales = monthlyExpenditureDataEvaluation.calculateTotalExpSales(salariesMonthExps);
        double miscellaneousMonth = monthlyExpenditureDataEvaluation.calculateTotalExpSales(miscellaneousMonthExps);


        Map<String, String> sales = new HashMap<>();
        sales.put("Salaries", String.format("%.2f",utility.roundAmount(salariesMonthSales)));
        sales.put("Miscellaneous", String.format("%.2f",utility.roundAmount(miscellaneousMonth)));

        return sales;
    }


    public ResponseEntity<?> rangeExpenditureRevenueInsight(String start_date, String end_date, Long branchId) {
        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        insight.put("DateRange", start_date + " - " + end_date);

        LocalDate startDate = utility.convertStringToLocalDate(start_date);
        LocalDate endDate = utility.convertStringToLocalDate(end_date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        List<Map<String, Object>> dailyInsights = new ArrayList<>();

        LocalDate currentStartDate = startDate;
        while (currentStartDate.isBefore(endDate) || currentStartDate.isEqual(endDate)) {
            LocalDate currentEndDate = currentStartDate.plusDays(1).minusDays(1); // end of the day
            if (currentEndDate.isAfter(endDate)) {
                currentEndDate = endDate; // do not go past the end date
            }

            Map<String, Double> dailySales = DateRangeExpenditureInsight(currentStartDate, currentEndDate, branchId);

            Map<String, Object> dailyInsight = new LinkedHashMap<>();
            dailyInsight.put("DateRange", currentStartDate.format(formatter));
            dailyInsight.putAll(dailySales);

            dailyInsights.add(dailyInsight);

            currentStartDate = currentStartDate.plusDays(1); // move to the next day
            System.out.println(currentStartDate);
        }

        insight.put("WeeklyInsights", dailyInsights);

        return ResponseEntity.ok(StandardResponse.success(insight));
    }

    private Map DateRangeExpenditureInsight(LocalDate startDate, LocalDate endDate, Long branchId) {
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<PharmacyBranch> branchOptional = pharmacyBranchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        PharmacyBranch branch = userDetailsService.getBranch();
        if (branchOptional.isPresent()) {
            branch = branchOptional.get();
        }

        List<Expenditure> salariesMonthExps = expenditureRepository.findAllByIntelRxIdAndCreatedAtBetweenAndExpenditureTypeAndBranch(
                intelRxId, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1).minusNanos(1),  // Adjust endDate to include the entire end day
                "Salaries", branch);


        List<Expenditure> miscellaneousMonthExps = expenditureRepository.findAllByIntelRxIdAndCreatedAtBetweenAndExpenditureTypeAndBranch(
                intelRxId, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1).minusNanos(1),  // Adjust endDate to include the entire end day
                "Miscellaneous", branch);

        // Calculate total expenditures for the current and previous months
        double salariesExp = monthlyExpenditureDataEvaluation.calculateTotalExpSales(salariesMonthExps);
        double miscellaneousExp = monthlyExpenditureDataEvaluation.calculateTotalExpSales(miscellaneousMonthExps);

        Map<String, String> sales = new HashMap<>();
        sales.put("Salaries", String.format("%.2f",utility.roundAmount(salariesExp)));
        sales.put("Miscellaneous", String.format("%.2f",utility.roundAmount(miscellaneousExp)));

        return sales;
    }


    public ResponseEntity<?> miscellaneousExpenditure() {
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch() != null ? userDetailsService.getBranch().getId() : null;
        List<Expenditure> results = expenditureRepository.findAllByBranchIdAndIntelRxIdAndExpenditureTypeOrderByAmountSpentDesc(
                branch,
                intelRxId, "Miscellaneous");

//        if (results.isEmpty()) {
//            // Return a default list with empty values
//            return ResponseEntity.ok(
//                    StandardResponse.success(Collections.singletonList(new MiscellaneousExpenditureDTO((String) null, "", "0.00"))));
//        }


        List<MiscellaneousExpenditureDTO> sellingItemDTOS = results.stream().map(result ->
                getMiscellaneousExpenditure(result)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(sellingItemDTOS));

    }

    private MiscellaneousExpenditureDTO getMiscellaneousExpenditure(Expenditure result) {
        MiscellaneousExpenditureDTO miscellaneousExpenditureDTO = new MiscellaneousExpenditureDTO();
        if(result.isApproved()) {

            LocalDateTime date = result.getCreatedAt();
            String expenseName = result.getExpenseName();
            Double amountSpentCost = result.getAmountSpent();

            miscellaneousExpenditureDTO.setDate(date);
            miscellaneousExpenditureDTO.setExpenseName(expenseName);
            miscellaneousExpenditureDTO.setAmount(String.format("%.2f",utility.roundAmount(amountSpentCost)));
        }
        return miscellaneousExpenditureDTO;

    }


}
