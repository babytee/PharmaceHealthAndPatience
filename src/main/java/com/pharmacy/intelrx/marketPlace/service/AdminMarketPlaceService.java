package com.pharmacy.intelrx.marketPlace.service;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.marketPlace.WholeSalesInventoryProjection;
import com.pharmacy.intelrx.marketPlace.dto.*;
import com.pharmacy.intelrx.marketPlace.models.MarketPlaceCartItem;
import com.pharmacy.intelrx.marketPlace.models.MarketPlaceOrder;
import com.pharmacy.intelrx.marketPlace.models.OrderBillInfo;
import com.pharmacy.intelrx.marketPlace.models.PackageStatus;
import com.pharmacy.intelrx.marketPlace.repository.MarketPlaceCartItemRepository;
import com.pharmacy.intelrx.marketPlace.repository.MarketPlaceOrderRepository;
import com.pharmacy.intelrx.marketPlace.repository.PackageStatusRepository;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryResponse;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.utility.*;
import com.pharmacy.intelrx.utility.AdminDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminMarketPlaceService")
public class AdminMarketPlaceService {
    private final Utility utility;
    private final InventoryRepository inventoryRepository;
    private final FilterWholeSalesInventory filterInventory;
    private final FilterInventory filterCartInventory;
    private final MarketPlaceCartItemRepository cartItemRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapping pharmacyMapping;
    private final MarketPlaceOrderRepository marketPlaceOrderRepository;
    private final PackageStatusRepository packageStatusRepository;
    private final AdminDetailsService userDetailsService;
    private final PharmacyWalletConfig pharmacyWalletConfig;



    public ResponseEntity<?> searchMarketPlace(String searchParam, String brandName, Pageable pageable) {
        userDetailsService.getAuthenticatedUser(); // Ensure the user is authenticated.

        if (utility.isNullOrEmpty(searchParam)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("searchParam is required"));
        }

        // Perform wildcard search
        Page<WholeSalesInventoryProjection> inventoryList = inventoryRepository.searchByItemNameOrBrandName
                (searchParam, brandName, pageable);

        if (inventoryList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success("No inventory found matching the search criteria."));
        }

        // Map the inventory projection to the inventory response
        Page<InventoryResponse> orderResponseList = inventoryList.map(filterInventory::mapToAddedInventoryResponse);

        return ResponseEntity.ok(StandardResponse.success(orderResponseList));
    }

    public MarketPlaceCartItemResponse mapToCartItem(MarketPlaceCartItem cartItem) {
        MarketPlaceCartItemResponse cartItemResponse = new MarketPlaceCartItemResponse();

        try {
            cartItemResponse.setId(cartItem.getId());
            cartItemResponse.setOrderId(cartItem.getOrder() == null ? null : cartItem.getOrder().getId());

            InventoryResponse inventoryResponse = filterCartInventory.mapToAddedInventoryResponse(
                    cartItem.getInventory() == null ? null : cartItem.getInventory());

            cartItemResponse.setInventoryItem(inventoryResponse);
            cartItemResponse.setAmount(cartItem.getAmount());
            cartItemResponse.setQuantity(cartItem.getQuantity());
            cartItemResponse.setIntelRxId(cartItem.getIntelRxId());
            cartItemResponse.setSalesStatus(cartItem.getSalesStatus());

            cartItemResponse.setSupplierPharmacy(pharmacyMapping.mapToPharmacy(
                    cartItem.getSupplierPharmacy() == null ? null : cartItem.getSupplierPharmacy()));

            var purchaserPharmacy = pharmacyRepository.findByIntelRxId(cartItem.getIntelRxId()).orElse(null);
            cartItemResponse.setPurchaserPharmacy(pharmacyMapping.mapToPharmacy(purchaserPharmacy));


            cartItemResponse.setOrderDate(cartItem.getCreatedAt());
        } catch (Exception e) {
            // Log the exception and cartItem details
            System.err.println("Error mapping CartItem to CartItemResponse: " + e.getMessage());
            e.printStackTrace();
        }

        return cartItemResponse;
    }

    public ResponseEntity<?> getOrderHistory(String intelRxId,String orderRef, String salesStatus, Pageable pageable) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            Page<MarketPlaceOrder> marketPlaceOrders = marketPlaceOrderRepository.findByIntelRxIdAndOrderRefAndSalesStatus(
                    intelRxId, orderRef, salesStatus, pageable
            );

            if (marketPlaceOrders.isEmpty()) {
                return ResponseEntity.ok(StandardResponse.success(marketPlaceOrders));
            }


            Page<MarketPlaceOrderResponse> orderResponseList = marketPlaceOrders.map(
                    this::mapToMarketPlaceOrderResponse);

            return ResponseEntity.ok(StandardResponse.success(orderResponseList));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage() + " nice"));
        }

    }

    public ResponseEntity<?> getSingleOrderHistory(Long orderId) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            MarketPlaceOrder order = marketPlaceOrderRepository.findById(orderId).orElse(null);

            if (order == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("order for this orderId " + orderId + " not found"));
            }

            MarketPlaceOrderResponse orderResponse = mapToMarketPlaceOrderResponse(order);

            return ResponseEntity.ok(StandardResponse.success(orderResponse));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage() + " nice"));
        }

    }

    public MarketPlaceOrderResponse mapToMarketPlaceOrderResponse(MarketPlaceOrder marketPlaceOrder) {

        MarketPlaceOrderResponse marketPlaceOrderResponse = new MarketPlaceOrderResponse();
        marketPlaceOrderResponse.setId(marketPlaceOrder.getId());
        marketPlaceOrderResponse.setOrderRef(marketPlaceOrder.getOrderRef());
        marketPlaceOrderResponse.setTotalQuantity(marketPlaceOrder.getCartItem().size());
        marketPlaceOrderResponse.setSalesStatus(marketPlaceOrder.getSalesStatus());
        marketPlaceOrderResponse.setOrderedDate(marketPlaceOrder.getOrderedDate());
        marketPlaceOrderResponse.setDeliveryDate(marketPlaceOrder.getDeliveryDate());
        marketPlaceOrderResponse.setAmount(marketPlaceOrder.getTotalAmount());
        marketPlaceOrderResponse.setDeliveryMethod(marketPlaceOrder.getDeliveryMethod());

        OrderBillInfo orderBillInfo = marketPlaceOrder.getOrderBillInfo();

        OrderBillInfoRequest billInfoRequest = new OrderBillInfoRequest();
        billInfoRequest.setId(orderBillInfo.getId());
        billInfoRequest.setFirstName(orderBillInfo.getFirstName());
        billInfoRequest.setLastName(orderBillInfo.getLastName());
        billInfoRequest.setCountry(orderBillInfo.getCountry());
        billInfoRequest.setState(orderBillInfo.getState());
        billInfoRequest.setCity(orderBillInfo.getCity());
        billInfoRequest.setAddress(orderBillInfo.getAddress());

        marketPlaceOrderResponse.setBillingInfo(billInfoRequest);

        int medCount = marketPlaceOrderRepository.countMedicationItemsByOrderId(marketPlaceOrder.getIntelRxId())
                .orElse(0);
        int groCount = marketPlaceOrderRepository.countGroceryItemsByOrderId(marketPlaceOrder.getIntelRxId())
                .orElse(0);

        marketPlaceOrderResponse.setMedicationItems(medCount);
        marketPlaceOrderResponse.setMedicationItems(groCount);

        return marketPlaceOrderResponse;
    }

    public ResponseEntity<?> getMarketOrderCartItem(Long orderId, Pageable pageable) {
        var user = userDetailsService.getAuthenticatedUser();
        MarketPlaceOrder order = marketPlaceOrderRepository.findById(orderId).orElse(null);

        if (order == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("order for this orderId " + orderId + " not found"));
        }
        Page<MarketPlaceCartItem> cartItemList = cartItemRepository.findByIntelRxIdAndOrder
                (order.getIntelRxId(), order, pageable);

        if (cartItemList == null) {
            return ResponseEntity.ok(StandardResponse.success(cartItemList));
        }
        Page<MarketPlaceCartItemResponse> responseList = cartItemList.map(this::mapToCartItem);

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> managePackageStatus(PackageStatusRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (request.getItemId() == null) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("itemId is required")
            );
        }
        var cartItem = cartItemRepository.findById(request.getItemId())
                .orElse(null);

        if (cartItem == null) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("item does not exist")
            );
        }

        PackageStatus packageStatus = null;
        validateDeliveryStatus(request.getDeliveryStatus());
        var intelRxId = cartItem.getSupplierPharmacy().getIntelRxId();
        if (request.getDeliveryStatus().equals("Pickup")
                || request.getDeliveryStatus().equals("Delivery")) {
            packageStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                    intelRxId, "Ongoing", cartItem.getId()
            ).orElse(null);
        } else if (request.getDeliveryStatus().equals("Completed")) {
            packageStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                    intelRxId, "Delivered", cartItem.getId()
            ).orElse(null);
        } else {
            packageStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                    intelRxId, request.getDeliveryStatus(), cartItem.getId()
            ).orElse(null);
        }

        if (packageStatus != null) {
            return ResponseEntity.ok(StandardResponse.success("This order item is already " + packageStatus.getDeliveryStatus()));
        }

        if (request.getDeliveryStatus().equals("Pickup")
                || request.getDeliveryStatus().equals("Delivery")) {
            addPackageHistory("Ongoing", cartItem);

            MarketPlaceOrder order = cartItem.getOrder();
            order.setDeliveryMethod(request.getDeliveryStatus());
            marketPlaceOrderRepository.save(order);
        } else {
            addPackageHistory(request.getDeliveryStatus(), cartItem);
        }

        cartItem.setSalesStatus(request.getDeliveryStatus());
        cartItemRepository.save(cartItem);
        if (request.getDeliveryStatus().equals("Completed")) {
            cartItem.setSalesStatus("Delivered");
            cartItemRepository.save(cartItem);

            MarketPlaceOrder order = cartItem.getOrder();
            order.setSalesStatus("Delivered");
            marketPlaceOrderRepository.save(order);

            pharmacyWalletConfig.creditWallet(intelRxId,cartItem.getAmount());
        }

        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));

    }

    private void validateDeliveryStatus(String deliveryStatus) {
        Set<String> allowedStatuses = Set.of("Placed", "Confirmed", "Ongoing", "Delivered", "Processed", "Pickup",
                "Delivery", "Completed");

        if (!allowedStatuses.contains(deliveryStatus)) {
            throw new IllegalArgumentException("Invalid delivery status: " + deliveryStatus +
                    ". Allowed values are: Placed, Confirmed, Ongoing, Delivered, Processed, Pickup, Delivery, Completed.");
        }
    }

    private void addPackageHistory(String deliveryStatus, MarketPlaceCartItem cartItem) {
        PackageStatus packageStatus = new PackageStatus();
        packageStatus.setIntelRxId(cartItem.getSupplierPharmacy().getIntelRxId());
        packageStatus.setDeliveryStatus(deliveryStatus);
        packageStatus.setCartItem(cartItem);
        packageStatus.setCreatedAt(LocalDateTime.now());
        packageStatusRepository.save(packageStatus);
    }

    public ResponseEntity<?> getPackageHistory(Long cartItemId,String intelRxId) {
        var user = userDetailsService.getAuthenticatedUser();
        List<PackageStatus> packageStatus = packageStatusRepository.findByIntelRxIdAndCartItemId
                (intelRxId, cartItemId);
        List<PackageStatusRequest> packageStatuses = packageStatus.stream().map(
                this::mapToPackageHistory).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(packageStatuses));
    }

    private PackageStatusRequest mapToPackageHistory(PackageStatus packageStatus) {
        PackageStatusRequest packageStatusRequest = new PackageStatusRequest();
        packageStatusRequest.setId(packageStatus.getId());
        packageStatusRequest.setDeliveryStatus(packageStatus.getDeliveryStatus());
        packageStatusRequest.setCreatedAt(packageStatus.getCreatedAt());
        return packageStatusRequest;
    }

    public ResponseEntity<?> overViewStat() {
        var user = userDetailsService.getAuthenticatedUser();

        //totalOrderPurchased,totalMedicationPurchased and totalGroceryPurchased from another pharmacy
        double totalOrderPurchased = marketPlaceOrderRepository.findTotalOrderAmountByIntelRxId
                (null).orElse(0.00);

        double totalMedicationPurchased = marketPlaceOrderRepository.findTotalMedicationAmountByOrderId
                (null).orElse(0.00);

        double totalGroceryPurchased = marketPlaceOrderRepository.findTotalGroceryAmountByOrderId
                (null).orElse(0.00);

        //mySalesTotal,medicationTotal and groceryTotal from my pharmacy
        double mySalesTotal = cartItemRepository.findTotalAmountByIntelRxId
                (null).orElse(0.00);

        double medicationTotal = cartItemRepository.findTotalMedicationAmountByIntelRxId
                (null).orElse(0.00);

        double groceryTotal = cartItemRepository.findTotalGroceryAmountByIntelRxId
                (null).orElse(0.00);


        Map<String, Double> salesStats = new HashMap<>();
        salesStats.put("totalOrderPurchased", utility.roundAmount(totalOrderPurchased));
        salesStats.put("totalGroceryPurchased", utility.roundAmount(totalGroceryPurchased));
        salesStats.put("totalMedicationPurchased", utility.roundAmount(totalMedicationPurchased));

        salesStats.put("mySalesTotal", utility.roundAmount(mySalesTotal));
        salesStats.put("grocerySales", utility.roundAmount(groceryTotal));
        salesStats.put("medicationSales", utility.roundAmount(medicationTotal));

        return ResponseEntity.ok(StandardResponse.success(salesStats));

    }

    public ResponseEntity<?> getPendingOrderItem(String intelRxId,String salesStatus, String search, Pageable pageable) {

        var user = userDetailsService.getAuthenticatedUser();
        Page<MarketPlaceCartItem> cartItemList = cartItemRepository.
                filterPendingSearch
                        (intelRxId, salesStatus, search, pageable);

        if (cartItemList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(cartItemList));
        }
        Page<MarketPlaceCartItemResponse> responseList = cartItemList.map((cartItem) -> mapToCartItem(cartItem));

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

}
