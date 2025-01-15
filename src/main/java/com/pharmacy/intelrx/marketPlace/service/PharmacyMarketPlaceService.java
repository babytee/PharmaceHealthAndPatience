package com.pharmacy.intelrx.marketPlace.service;

import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.marketPlace.WholeSalesInventoryProjection;
import com.pharmacy.intelrx.marketPlace.dto.*;
import com.pharmacy.intelrx.marketPlace.models.MarketPlaceCartItem;
import com.pharmacy.intelrx.marketPlace.models.MarketPlaceOrder;
import com.pharmacy.intelrx.marketPlace.models.OrderBillInfo;
import com.pharmacy.intelrx.marketPlace.models.PackageStatus;
import com.pharmacy.intelrx.marketPlace.repository.MarketPlaceCartItemRepository;
import com.pharmacy.intelrx.marketPlace.repository.MarketPlaceOrderRepository;
import com.pharmacy.intelrx.marketPlace.repository.OrderBillInfoRepository;
import com.pharmacy.intelrx.marketPlace.repository.PackageStatusRepository;
import com.pharmacy.intelrx.pharmacy.TopSellingItemProjection;
import com.pharmacy.intelrx.pharmacy.dto.TopSellingItemDTO;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierPaymentRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierRequest;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.SalesMargin;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SalesMarginRepository;
import com.pharmacy.intelrx.pharmacy.services.InventoryServices;
import com.pharmacy.intelrx.pharmacy.services.PharmacyAnalyticService;
import com.pharmacy.intelrx.pharmacy.services.SupplierService;
import com.pharmacy.intelrx.pharmacy.utility.*;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyMarketPlaceService")
public class PharmacyMarketPlaceService {
    private final UserDetailsService userDetailsService;
    private final Utility utility;
    private final InventoryRepository inventoryRepository;
    private final FilterWholeSalesInventory filterInventory;
    private final FilterInventory filterCartInventory;
    private final MarketPlaceCartItemRepository cartItemRepository;
    private final PharmacyRepository pharmacyRepository;
    private final AddCartOrderValidation cartOrderValidation;
    private final PharmacyMapping pharmacyMapping;
    private final InventoryServices inventoryServices;
    private final InventoryValidation inventoryValidation;
    private final OrderBillInfoRepository orderBillInfoRepository;
    private final MarketPlaceOrderRepository marketPlaceOrderRepository;
    private final PackageStatusRepository packageStatusRepository;
    private final PharmacyWalletConfig pharmacyWalletConfig;
    private final SupplierService supplierService;
    private final PharmacyAnalyticService pharmacyAnalyticService;
    private final SalesMarginRepository salesMarginRepository;

    public ResponseEntity<?> searchMarketPlace(String searchParam, String brandName, Pageable pageable) {
        userDetailsService.getAuthenticatedUser(); // Ensure the user is authenticated.

        if (utility.isNullOrEmpty(searchParam)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("searchParam is required"));
        }

        // Perform wildcard search
        Page<WholeSalesInventoryProjection> inventoryList = inventoryRepository.searchByItemNameOrBrandName
                (searchParam, brandName, pageable);

        if (inventoryList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success("No inventory found matching the search criteria. ss"));
        }

        // Map the inventory projection to the inventory response
        Page<InventoryResponse> orderResponseList = inventoryList.map(filterInventory::mapToAddedInventoryResponse);

        return ResponseEntity.ok(StandardResponse.success(orderResponseList));
    }

    @Transactional
    public ResponseEntity<?> addItemToCart(List<MarketPlaceCartItemRequest> cartItemRequests) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            if (user == null) {
                return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
            }
            for (MarketPlaceCartItemRequest cartItemRequest : cartItemRequests) {
                ResponseEntity responseEntity = cartOrderValidation.validateMarketPlaceCartItemRequest(cartItemRequest);
                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    return responseEntity;
                }

                Pharmacy supplierPharmacy = cartItemRequest.getSupplierPharmacyId() == null ? null :
                        pharmacyRepository.findById(cartItemRequest.getSupplierPharmacyId()).orElse(null);

                if (supplierPharmacy == null) {
                    return ResponseEntity.badRequest().body(StandardResponse.error
                            ("Supplier Pharmacy with this SupplierPharmacyId " +
                                    cartItemRequest.getSupplierPharmacyId() + " was not found"));
                }

                Inventory inventory = inventoryRepository.findByIdAndIntelRxId(cartItemRequest.getInventoryId(),
                        supplierPharmacy.getIntelRxId()).orElse(null);

                if (inventory == null) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required or not found"));
                }

                int qty = cartItemRequest.getQuantity();
                double salePrice = inventory.getWholeSalePrice();
                double totalQtySalePrice = qty * salePrice;
                double totalAmount = cartItemRequest.getAmount();
                String msg = "The whole sales quantity amount for " + inventory.getItemName() + " is not correct or well calculated. It should be " + totalQtySalePrice + " Naira";

                if (totalQtySalePrice < totalAmount) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }
                if (totalQtySalePrice > totalAmount) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }

            }
            MarketPlaceOrder order = null;


            // Proceed to add items to the cart
            addToCart(cartItemRequests, order);


            return ResponseEntity.ok(StandardResponse.success("cart submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage()));
        }
    }

    @Transactional
    private void addToCart(List<MarketPlaceCartItemRequest> cartItemRequests, MarketPlaceOrder order) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();

        // Map to track existing cart items by inventory ID
        Map<Long, MarketPlaceCartItem> existingCartItemsMap = new HashMap<>();

        // Retrieve existing cart items for the given order and user
        List<MarketPlaceCartItem> existingCartItems = cartItemRepository.findByIntelRxIdAndOrderAndUser(
                intelRxId, order, user);

        for (MarketPlaceCartItem cartItem : existingCartItems) {
            existingCartItemsMap.put(cartItem.getInventory().getId(), cartItem);
        }

        // Process each cart item request
        for (MarketPlaceCartItemRequest request : cartItemRequests) {
            Pharmacy supplierPharmacy = request.getSupplierPharmacyId() == null ? null :
                    pharmacyRepository.findById(request.getSupplierPharmacyId()).orElse(null);

            Inventory inventory = inventoryRepository.findByIdAndIntelRxId(
                    request.getInventoryId(), supplierPharmacy.getIntelRxId()).orElse(null);

            MarketPlaceCartItem cartItem;
            if (existingCartItemsMap.containsKey(inventory.getId())) {
                // Update existing cart item
                cartItem = existingCartItemsMap.get(inventory.getId());
                cartItem.setQuantity(request.getQuantity());
                cartItem.setAmount(request.getAmount());
                cartItem.setVat(request.getVat());
            } else {
                // Create a new cart item
                cartItem = new MarketPlaceCartItem();
                cartItem.setIntelRxId(intelRxId);
                cartItem.setUser(user);
                cartItem.setSupplierPharmacy(supplierPharmacy);
                cartItem.setInventory(inventory);
                cartItem.setQuantity(request.getQuantity());
                cartItem.setAmount(request.getAmount());
                cartItem.setVat(request.getVat());
                cartItem.setCreatedAt(LocalDateTime.now());
                if (order != null) {
                    cartItem.setOrder(order);
                    cartItem.setStatus(true);
                }
            }

            // Save the cart item
            MarketPlaceCartItem savedCart = cartItemRepository.save(cartItem);

        }

        // Remove items from the cart that are not present in the new request
        removeOldCart(cartItemRequests, order);
    }

    private void removeOldCart(List<MarketPlaceCartItemRequest> cartItemRequests, MarketPlaceOrder order) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();

        List<Long> requestInventoryIds = cartItemRequests.stream()
                .map(MarketPlaceCartItemRequest::getInventoryId)
                .collect(Collectors.toList());

        List<MarketPlaceCartItem> existingCartItems = cartItemRepository.findByIntelRxIdAndOrderAndUser(
                intelRxId, order, user);

        for (MarketPlaceCartItem existingCartItem : existingCartItems) {
            if (!requestInventoryIds.contains(existingCartItem.getInventory().getId())) {
                cartItemRepository.delete(existingCartItem);
            }
        }
    }

    public ResponseEntity<?> getCartItem() {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(new Response("failed", "You are unauthorized"));
        }
        String intelRxId = userDetailsService.getIntelRxId();

        List<MarketPlaceCartItem> cartItemList = cartItemRepository.findByIntelRxIdAndUserAndStatus(intelRxId, user, false);

        List<MarketPlaceCartItemResponse> responseList = cartItemList.stream().map((cartItem) -> mapToCartItem(cartItem))
                .collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));

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

            if (cartItem.getSupplierPharmacy() != null) {
                cartItemResponse.setSupplierPharmacy(pharmacyMapping.mapToPharmacy(cartItem.getSupplierPharmacy()));
            } else {
                System.err.println("Supplier Pharmacy is null for CartItem ID: " + cartItem.getId());
            }

            var purchaserPharmacy = pharmacyRepository.findByIntelRxId(cartItem.getIntelRxId()).orElse(null);
            if (purchaserPharmacy != null) {
                cartItemResponse.setPurchaserPharmacy(pharmacyMapping.mapToPharmacy(purchaserPharmacy));
            } else {
                System.err.println("Purchaser Pharmacy not found for IntelRxId: " + cartItem.getIntelRxId());
            }


            cartItemResponse.setOrderDate(cartItem.getCreatedAt());
        } catch (Exception e) {
            // Log the exception and cartItem details
            System.err.println("Error mapping CartItem to CartItemResponse: " + e.getMessage());
            e.printStackTrace();
        }

        return cartItemResponse;
    }

    //Order service
    @Transactional
    public ResponseEntity<?> submitOrder(OrderBillInfoRequest request) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            String intelRxId = userDetailsService.getIntelRxId();
            String ref = utility.generateUniqueInvoiceRefNumber();

            List<MarketPlaceCartItem> cartItems = cartItemRepository.findByIntelRxIdAndUserAndStatus(
                    intelRxId, user, false);


            MarketPlaceOrder updateOrder = null;
            MarketPlaceOrder order = null;

            double totalItemAmountByQty = 0.0;
            double totalPayAble = 0.0;

            if (!cartItems.isEmpty()) {
                for (MarketPlaceCartItem cartItem : cartItems) {
                    totalItemAmountByQty += cartItem.getAmount();
                }

                String msg = "The item total amount for this order should be " + totalItemAmountByQty + " Naira";
                if (totalItemAmountByQty < request.getTotalAmount()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }

                totalPayAble = request.getTotalAmount();
                String msg2 = "The totalPayAble amount for this order is less than the item total amount or the invoice amount.";

                if (totalPayAble < request.getTotalAmount()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg2));
                }

                order = new MarketPlaceOrder();
                order.setOrderRef(ref);
                order.setIntelRxId(intelRxId);

                updateOrder = marketPlaceOrderRepository.save(order);

                for (MarketPlaceCartItem cartItem : cartItems) {
                    cartItem.setStatus(true);
                    cartItem.setOrder(updateOrder);
                    cartItem.setSalesStatus("Placed");
                    cartItem.setCreatedAt(LocalDateTime.now());

                    Inventory inventory = cartItem.getInventory();


                    addPackageHistory("Placed", cartItem);

                }

                OrderBillInfo orderBillInfo = addBillInfo(request, user, intelRxId);

                updateOrder.setOrderedDate(LocalDateTime.now());
                updateOrder.setStatus(true);
                updateOrder.setSalesStatus("Placed");
                updateOrder.setUser(user);
                updateOrder.setTotalAmount(totalItemAmountByQty);
                updateOrder.setOrderBillInfo(orderBillInfo);

                marketPlaceOrderRepository.save(updateOrder);
                cartItemRepository.saveAll(cartItems);

                return ResponseEntity.ok(StandardResponse.success("You have successful placed an order!"));
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("Cart is empty"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage()));
        }

    }

    @Transactional
    private void addOrUpdateInventory(MarketPlaceCartItem cartItem) throws IOException {
        //inventory item details from seller
        Inventory inventory = cartItem.getInventory();

        //getting buyer intelRxId and user info
        var intelRxId = cartItem.getIntelRxId();
        User user = cartItem.getUser();

        InventoryRequest inventoryRequest = new InventoryRequest();
        String barCodeNumber = (inventory.getBarCodeNumber() != null) ? inventory.getBarCodeNumber() : "";

        //checking if the those item exist from the buyer inventory
        // to be updated or add as new if it does not exist
        Inventory pharmacyInventory = inventoryRepository.findByItemNameAndIntelRxId(
                inventory.getItemName(), intelRxId
        ).orElse(null);

        int qty = 0;
        double costPrice = 0.00;
        double salesPrice = 0.00;

        if (pharmacyInventory != null) {
            qty += pharmacyInventory.getQuantity() + cartItem.getQuantity();
            costPrice =  pharmacyInventory.getCostPrice();
            salesPrice = pharmacyInventory.getSalePrice();

            inventoryRequest.setId(pharmacyInventory.getId());
            inventoryRequest.setBrandId(String.valueOf(pharmacyInventory.getBrand().getId()));
            inventoryRequest.setBrandClassId(String.valueOf(pharmacyInventory.getBrandClass().getId()));
            inventoryRequest.setBrandFormId(String.valueOf(pharmacyInventory.getBrandForm().getId()));
            inventoryRequest.setSizeId(pharmacyInventory.getSize().getId());

            inventoryRequest.setExpDay(pharmacyInventory.getExpDay());
            inventoryRequest.setExpMonth(pharmacyInventory.getExpMonth());
            inventoryRequest.setExpYear(pharmacyInventory.getExpYear());
            inventoryRequest.setPoison(pharmacyInventory.isPoison());

            inventoryRequest.setQuantity(qty);
        }
        else {
            //get sales margin to calculate the cost margin
            SalesMargin salesMargin = salesMarginRepository.findByIntelRxId(intelRxId).orElse(null);

            qty = cartItem.getQuantity();
            costPrice = inventory.getWholeSalePrice();
            if (inventory.getInventoryType().equals("MEDICATION")) {
                salesPrice = salesMargin == null ? inventory.getWholeSalePrice() :
                        inventory.getWholeSalePrice() * salesMargin.getMedicationMargin();
            }
            if (inventory.getInventoryType().equals("GROCERY")) {
                salesPrice = salesMargin == null ? inventory.getWholeSalePrice() :
                        inventory.getWholeSalePrice() * salesMargin.getGroceryMargin();
            }


            inventoryRequest.setBrandId(String.valueOf(inventory.getBrand().getId()));
            inventoryRequest.setBrandClassId(String.valueOf(inventory.getBrandClass().getId()));
            inventoryRequest.setBrandFormId(String.valueOf(inventory.getBrandForm().getId()));
            inventoryRequest.setSizeId(inventory.getSize().getId());

            inventoryRequest.setExpDay(inventory.getExpDay());
            inventoryRequest.setExpMonth(inventory.getExpMonth());
            inventoryRequest.setExpYear(inventory.getExpYear());
            inventoryRequest.setPoison(inventory.isPoison());

            inventoryRequest.setQuantity(qty);
        }

        inventoryRequest.setIntelRxId(cartItem.getIntelRxId());
        inventoryRequest.setInventoryType(inventory.getInventoryType());
        inventoryRequest.setBarCodeNumber(barCodeNumber);
        inventoryRequest.setItemName(inventory.getItemName());
        inventoryRequest.setCostPrice(costPrice);
        inventoryRequest.setSalePrice(salesPrice);

        var inv = addNewInventory(inventoryRequest, cartItem);

        //update the supplier Pharmacy inventory
        Inventory suppierPharmacyInventory = inventoryRepository.findByIdAndIntelRxId(
                cartItem.getInventory().getId(), cartItem.getSupplierPharmacy().getIntelRxId()
        ).orElse(null);

        if (suppierPharmacyInventory != null) {
            suppierPharmacyInventory.setWholeSaleQuantity(
                    suppierPharmacyInventory.getWholeSaleQuantity() - cartItem.getQuantity()
            );
            inventoryRepository.save(suppierPharmacyInventory);

        }

        //if the user is a wholesaler, add him or her to the supplier list
        var userInfo = userDetailsService.mapToUserInfo(user);
        SupplierRequest supplierRequest = new SupplierRequest();
        supplierRequest.setName(userInfo.getFirstName());
        supplierRequest.setPhoneNumber(userInfo.getPhoneNumber());
        supplierRequest.setUser(user);
        supplierRequest.setIntelRxId(intelRxId);
        supplierRequest.setSupplierPharmacy(cartItem.getSupplierPharmacy());
        var supplier = supplierService.newSupplier(supplierRequest, null);

        if (inventory.getWholeSaleQuantity() > 0) {

            SupplierPaymentRequest paymentRequest = new SupplierPaymentRequest();
            paymentRequest.setIntelRxId(intelRxId);
            paymentRequest.setSupplierId(supplier.getId());
            paymentRequest.setUser(user);
            paymentRequest.setAmountPaid(cartItem.getAmount());
            paymentRequest.setInvoiceAmount(cartItem.getAmount());
            paymentRequest.setPaymentDay(cartItem.getCreatedAt().getDayOfMonth());
            paymentRequest.setPaymentMonth(cartItem.getCreatedAt().getMonthValue());
            paymentRequest.setPaymentYear(cartItem.getCreatedAt().getYear());
            paymentRequest.setPaymentMethodId(2L);
            paymentRequest.setPaymentStatusId(1L);
            paymentRequest.setBalanceDue(0.0);
            addToSupplier(inv, paymentRequest);

        }


    }

    @Transactional
    public Inventory addNewInventory(InventoryRequest request, MarketPlaceCartItem cartItem) {
        var user = cartItem.getUser();

        Inventory inventory = null;
        ResponseEntity<?> responseEntity = inventoryValidation.validateInventoryRequest(request);

        if (request.getInventoryType().equals("MEDICATION")) {
            inventory = inventoryServices.addMedication(request, user, request.getId(),"");
        } else if (request.getInventoryType().equals("GROCERY")) {
            inventory = inventoryServices.addGrocery(request, user, request.getId(),"");
        }

        if (inventory != null) {
            inventory = inventoryRepository.save(inventory);
        }
        return inventory;
    }

    public void addToSupplier(Inventory inventory, SupplierPaymentRequest request) throws IOException {
        var finalSupplier = supplierService.addOtherSupplierToInventory(request);
        inventory.setStatus(true);
        inventory.setInvoiceRefNumber(finalSupplier.getInvoiceRefNumber());
        inventory.setSupplier(finalSupplier.getSupplier());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public OrderBillInfo addBillInfo(OrderBillInfoRequest request, User user, String intelRxId) {
        var pharmacy = userDetailsService.getPharmacyInfo();
        OrderBillInfo orderBillInfo = OrderBillInfo.builder()
                .intelRxId(intelRxId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .pharmacyName(pharmacy.getPharmacyName())
                .country(request.getCountry())
                .state(request.getState())
                .city(request.getCity())
                .address(request.getAddress())
                .user(user)
                .build();

        return orderBillInfoRepository.save(orderBillInfo);

    }

    public ResponseEntity<?> getOrderHistory(String orderRef, String salesStatus, Pageable pageable) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            String intelRxId = userDetailsService.getIntelRxId();

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
        String intelRxId = userDetailsService.getIntelRxId();

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

    @Transactional
    public ResponseEntity<?> managePackageStatusSample(PackageStatusRequest request) throws IOException {
        var intelRxId = userDetailsService.getIntelRxId();
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


        if (request.getDeliveryStatus().equals("Pickup")
                || request.getDeliveryStatus().equals("Delivery")) {
            packageStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                    cartItem.getSupplierPharmacy().getIntelRxId(), "Ongoing", cartItem.getId()
            ).orElse(null);
        } else if (request.getDeliveryStatus().equals("Completed")) {
            packageStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                    cartItem.getSupplierPharmacy().getIntelRxId(), "Delivered", cartItem.getId()
            ).orElse(null);
        } else {
            packageStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                    cartItem.getSupplierPharmacy().getIntelRxId(), request.getDeliveryStatus(), cartItem.getId()
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
            pharmacyWalletConfig.creditWallet(intelRxId, cartItem.getAmount());
            //add or update the pharmacy inventory
            addOrUpdateInventory(cartItem);
        }

        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));

    }

    public ResponseEntity<?> managePackageStatus(PackageStatusRequest request) throws IOException {
        var intelRxId = userDetailsService.getIntelRxId();

        // Validate request parameters
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

        validateDeliveryStatus(request.getDeliveryStatus());

        // Check for existing package status
        var packageStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                cartItem.getSupplierPharmacy().getIntelRxId(),
                request.getDeliveryStatus(),
                cartItem.getId()
        ).orElse(null);

        if (packageStatus != null) {
            return ResponseEntity.ok(StandardResponse.success("This order item is already " + packageStatus.getDeliveryStatus()));
        }

        // Handle auto-addition of related statuses
        if (request.getDeliveryStatus().equals("Processed")) {
            autoAddStatuses(intelRxId, cartItem, "Confirmed", "Processed");
        } else if (request.getDeliveryStatus().equals("Pickup") || request.getDeliveryStatus().equals("Delivery")) {
            autoAddStatuses(intelRxId, cartItem, "Confirmed", "Processed", "Pickup or Delivery");
        } else if (request.getDeliveryStatus().equals("Completed")) {
            autoAddStatuses(intelRxId, cartItem, "Confirmed", "Processed", "Pickup or Delivery", "Delivered");
        }

        // Update cart item sales status and save
        cartItem.setSalesStatus(request.getDeliveryStatus());
        cartItemRepository.save(cartItem);

        // Additional handling for 'Completed' status
        if (request.getDeliveryStatus().equals("Completed")) {
            cartItem.setSalesStatus("Delivered");
            cartItemRepository.save(cartItem);

            MarketPlaceOrder order = cartItem.getOrder();
            order.setSalesStatus("Delivered");
            marketPlaceOrderRepository.save(order);

            pharmacyWalletConfig.creditWallet(intelRxId, cartItem.getAmount());
            addOrUpdateInventory(cartItem); // Update pharmacy inventory
        } else {
            MarketPlaceOrder order = cartItem.getOrder();
            order.setSalesStatus("Ongoing");
            marketPlaceOrderRepository.save(order);
        }

        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
    }

    private void validateDeliveryStatus(String deliveryStatus) {
        Set<String> allowedStatuses = Set.of("Placed", "Confirmed", "Ongoing", "Delivered", "Processed", "Pickup", "Delivery", "Completed");

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

    private void autoAddStatuses(String intelRxId, MarketPlaceCartItem cartItem, String... statuses) {
        for (String status : statuses) {
            var existingStatus = packageStatusRepository.findByIntelRxIdAndDeliveryStatusAndCartItemId(
                    intelRxId,
                    status,
                    cartItem.getId()
            ).orElse(null);

            if (existingStatus == null) {
                addPackageHistory(status, cartItem);
            }
        }
    }


    public ResponseEntity<?> getPackageHistory(Long cartItemId, String intelRxId) {
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

    public ResponseEntity<?> mySales(String salesStatus, Pageable pageable) {
        var pharmacy = userDetailsService.getPharmacyInfo();
        if (!utility.isNullOrEmpty(salesStatus)) {
            validateDeliveryStatus(salesStatus);
        }
        Page<MarketPlaceCartItem> cartItems = cartItemRepository.findBySalesStatusAndSupplierPharmacy
                (salesStatus, pharmacy, pageable);

        if (cartItems == null) {
            return ResponseEntity.ok(StandardResponse.success(cartItems));
        }

        Page<MarketPlaceCartItemResponse> cartItemResponses = cartItems.map(this::mapToCartItem);

        return ResponseEntity.ok(StandardResponse.success(cartItemResponses));
    }

    public ResponseEntity<?> overViewStat() {
        var intelRxId = userDetailsService.getIntelRxId();
        var pharmacy = userDetailsService.getPharmacyInfo();

        //totalOrderPurchased,totalMedicationPurchased and totalGroceryPurchased from another pharmacy
        double totalOrderPurchased = marketPlaceOrderRepository.findTotalOrderAmountByIntelRxId
                (intelRxId).orElse(0.00);

        double totalMedicationPurchased = marketPlaceOrderRepository.findTotalMedicationAmountByOrderId
                (intelRxId).orElse(0.00);

        double totalGroceryPurchased = marketPlaceOrderRepository.findTotalGroceryAmountByOrderId
                (intelRxId).orElse(0.00);

        //mySalesTotal,medicationTotal and groceryTotal from my pharmacy
        double mySalesTotal = cartItemRepository.findTotalAmountByIntelRxId
                (pharmacy).orElse(0.00);

        double medicationTotal = cartItemRepository.findTotalMedicationAmountByIntelRxId
                (pharmacy).orElse(0.00);

        double groceryTotal = cartItemRepository.findTotalGroceryAmountByIntelRxId
                (pharmacy).orElse(0.00);


        Map<String, Double> salesStats = new HashMap<>();
        salesStats.put("totalOrderPurchased", utility.roundAmount(totalOrderPurchased));
        salesStats.put("totalGroceryPurchased", utility.roundAmount(totalGroceryPurchased));
        salesStats.put("totalMedicationPurchased", utility.roundAmount(totalMedicationPurchased));

        salesStats.put("mySalesTotal", utility.roundAmount(mySalesTotal));
        salesStats.put("grocerySales", utility.roundAmount(groceryTotal));
        salesStats.put("medicationSales", utility.roundAmount(medicationTotal));

        return ResponseEntity.ok(StandardResponse.success(salesStats));

    }

    public ResponseEntity<?> getPendingOrderItem(String salesStatus, String search, Pageable pageable) {
        String intelRxId = userDetailsService.getIntelRxId();

        Page<MarketPlaceCartItem> cartItemList = cartItemRepository.
                filterPendingSearch
                        (intelRxId, salesStatus, search, pageable);

        if (cartItemList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(cartItemList));
        }
        Page<MarketPlaceCartItemResponse> responseList = cartItemList.map((cartItem) -> mapToCartItem(cartItem));

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> topSellingProducts() {
        String intelRx = userDetailsService.getIntelRxId();
        List<TopSellingItemDTO> sellingItemDTOS = getTopSellingInventoryItems();

        return ResponseEntity.ok(StandardResponse.success(sellingItemDTOS));
    }

    public List<TopSellingItemDTO> getTopSellingInventoryItems() {
        List<TopSellingItemProjection> results =
                cartItemRepository.findTopSellingInventoryItemsByMonth
                        (LocalDate.now().getMonthValue());

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
                    String formattedSalesAmount = String.format("%.2f", totalSalesAmount); //currencyFormatter.format(totalSalesAmount);

                    // Calculate the percentage change
                    String percentageChange = pharmacyAnalyticService.calculatePercentageChange(totalSalesAmount, previousSalesAmount);

                    return new TopSellingItemDTO(itemName, formattedSalesAmount, percentageChange);
                })
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> marketReport() {
        var intelRxId = userDetailsService.getIntelRxId();

        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        int year = LocalDateTime.now().getYear();
        insight.put("Year", year < 1 ? YearMonth.now().getYear() : year);
        insight.put("Jan", yearlyInsight(year, 1));
        insight.put("Feb", yearlyInsight(year, 2));
        insight.put("Mar", yearlyInsight(year, 3));
        insight.put("Apr", yearlyInsight(year, 4));
        insight.put("May", yearlyInsight(year, 5));
        insight.put("Jun", yearlyInsight(year, 6));
        insight.put("Jul", yearlyInsight(year, 7));
        insight.put("Aug", yearlyInsight(year, 8));
        insight.put("Sep", yearlyInsight(year, 9));
        insight.put("Oct", yearlyInsight(year, 10));
        insight.put("Nov", yearlyInsight(year, 11));
        insight.put("Dec", yearlyInsight(year, 12));

        return ResponseEntity.ok(StandardResponse.success(insight));
    }

    //yearlyInsight revenue
    private Map yearlyInsight(int year, int month) {
        double retailerRevenue = marketPlaceOrderRepository.findAverageOrderAmountByMonthAndYear
                (month, year).orElse(0.00);

        double wholesalerRevenue = cartItemRepository.findTotalAmountByDate
                (month, year).orElse(0.00);

        Map<String, Double> sales = new HashMap<>();
        sales.put("retailerRevenue", utility.roundAmount(retailerRevenue));
        sales.put("wholesalerRevenue", utility.roundAmount(wholesalerRevenue));

        return sales;
    }


}
