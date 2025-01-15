package com.pharmacy.intelrx.auxilliary.LandingPages;

import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.repositories.BrandRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.TopSellingItemProjection;
import com.pharmacy.intelrx.pharmacy.dto.TopSellingItemDTO;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRepository;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("TopSellingMedicationService")
public class TopSellingMedicationService {
    private static final Map<String, Long> TIME_PERIODS = new HashMap<>();

    static {
        TIME_PERIODS.put("2 Minutes", 2L);
        TIME_PERIODS.put("5 Minutes", 5L);
        TIME_PERIODS.put("10 Minutes", 10L);
        TIME_PERIODS.put("5 Days", 5L * 24 * 60L);
        TIME_PERIODS.put("6 Days", 6L * 24 * 60L);
        TIME_PERIODS.put("1 Week", 7L * 24 * 60L);
        TIME_PERIODS.put("2 Weeks", 14L * 24 * 60L);
        TIME_PERIODS.put("3 Weeks", 21L * 24 * 60L);
        TIME_PERIODS.put("4 Weeks", 28L * 24 * 60L);
        TIME_PERIODS.put("5 Weeks", 35L * 24 * 60L);
        TIME_PERIODS.put("1 Month", 30L * 24 * 60L);
        TIME_PERIODS.put("2 Months", 60L * 24 * 60L);
        TIME_PERIODS.put("3 Months", 90L * 24 * 60L);
        TIME_PERIODS.put("4 Months", 120L * 24 * 60L);
        TIME_PERIODS.put("5 Months", 150L * 24 * 60L);
        TIME_PERIODS.put("6 Months", 180L * 24 * 60L);
        TIME_PERIODS.put("7 Months", 210L * 24 * 60L);
        TIME_PERIODS.put("8 Months", 240L * 24 * 60L);
        TIME_PERIODS.put("9 Months", 270L * 24 * 60L);
        TIME_PERIODS.put("10 Months", 300L * 24 * 60L);
        TIME_PERIODS.put("11 Months", 330L * 24 * 60L);
        TIME_PERIODS.put("12 Months", 360L * 24 * 60L);
    }

    private final CartItemRepository cartItemRepository;
    private final BrandRepository brandRepository;
    private final InventoryRepository inventoryRepository;

    public ResponseEntity<?> topSellingProducts(
            String timePeriod, Long brandId, Long medicationId
    ) {
        Optional<Brand> optionalBrand = Optional.empty();
        if (brandId != null) {
            optionalBrand = brandRepository.findById(brandId);
            if (optionalBrand.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Brand not found"));
            }
        }

        Optional<Inventory> optionalInventory = Optional.empty();
        if (medicationId != null) {
            optionalInventory = inventoryRepository.findById(medicationId);
            if (optionalInventory.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Medication not found"));
            }
        }

        List<TopSellingItemDTO> sellingItemDTOS = getTopSellingInventoryItems(
                timePeriod,
                optionalBrand.map(Brand::getName).orElse(null),
                optionalInventory.map(Inventory::getItemName).orElse(null)
        );

        return ResponseEntity.ok(StandardResponse.success(sellingItemDTOS));
    }

    public List<TopSellingItemDTO> getTopSellingInventoryItems(
            String timePeriod, String brand, String medication) {
        Long minutes = TIME_PERIODS.get(timePeriod);
        if (minutes == null) {
            throw new IllegalArgumentException("Invalid time period");
        }

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMinutes(minutes);

        List<TopSellingItemProjection> results = cartItemRepository.findTopSellingMedications(
                startDate, endDate, "Nigeria", brand, medication);

        if (results.isEmpty()) {
            return List.of(new TopSellingItemDTO("No data", "0.00", "+0.0%"));
        }

        return results.stream()
                .map(result -> {
                    String itemName = result.getItemName();
                    double totalSalesAmount = result.getTotalSalesAmount();
                    double previousSalesAmount = result.getPreviousSalesAmount();

                    String formattedSalesAmount = String.valueOf(totalSalesAmount);
                    String percentageChange = calculatePercentageChange(totalSalesAmount, previousSalesAmount);

                    return new TopSellingItemDTO(itemName, formattedSalesAmount, percentageChange);
                })
                .collect(Collectors.toList());
    }


    private String calculatePercentageChange(double totalSalesAmount, double previousSalesAmount) {
        if (previousSalesAmount == 0) {
            return "+0.0%";
        }
        double change = ((totalSalesAmount - previousSalesAmount) / previousSalesAmount) * 100;
        return String.format("%+.1f%%", change);
    }
}
