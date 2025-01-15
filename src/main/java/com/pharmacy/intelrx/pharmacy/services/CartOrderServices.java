package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.SmsByTermii.TermiiSMSService;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.generalCoupon.dto.CouponHistoryRequest;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCoupon;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCouponDetail;
import com.pharmacy.intelrx.generalCoupon.repositories.GeneralCouponDetailRepository;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.dto.employee.JobInformationRequest;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.RefillDurationRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.AddCartOrderValidation;
import com.pharmacy.intelrx.pharmacy.utility.SalesMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("CartOrderServices")
public class CartOrderServices {
    private static final Logger log = LoggerFactory.getLogger(CartOrderServices.class);
    private final PatientRepository patientRepository;
    private final CartItemRepository cartItemRepository;
    private final RefillDurationRepository refillDurationRepository;
    private final AddCartOrderValidation cartOrderValidation;
    private final UserDetailsService userDetailsService;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final MedPrescriptionRepository medPrescriptionRepository;
    private final PatientService patientService;
    private final Utility utility;
    private final SalesMapping salesMapping;
    private final EmployeeRepository employeeRepository;
    private final ExpenditureService expenditureService;
    private final TermiiSMSService termiiSMSService;
    private final TransferInventoryRepository transferInventoryRepository;
    private final GeneralCouponDetailRepository couponDetailRepository;
    private final GeneralCouponService generalCouponService;

    @Transactional
    public ResponseEntity<?> addItemToCart(List<CartItemRequest> cartItemRequests) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            PharmacyBranch branch = userDetailsService.getBranch();
            String intelRxId = userDetailsService.getIntelRxId();

            if (user == null) {
                return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
            }
            for (CartItemRequest cartItemRequest : cartItemRequests) {
                ResponseEntity responseEntity = cartOrderValidation.validateCartItemRequest(cartItemRequest);
                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    return responseEntity;
                }

                Optional<Inventory> optionalInventory = inventoryRepository.findByIdAndIntelRxId(cartItemRequest.getInventoryId(), intelRxId);
                if (!optionalInventory.isPresent()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
                }

                Inventory inventory = optionalInventory.get();
                int qty = cartItemRequest.getQuantity();
                double salePrice = inventory.getSalePrice();
                double totalQtySalePrice = qty * salePrice;
                double totalAmount = cartItemRequest.getAmount();
                String msg = "The quantity amount for " + inventory.getItemName() + " is not correct or well calculated. It should be " + totalQtySalePrice + " Naira";

                if (totalQtySalePrice < totalAmount) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }
                if (totalQtySalePrice > totalAmount) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }


                if (cartItemRequest.getMedPrescriptionRequest() != null) {
                    MedPrescriptionRequest medPrescriptionRequest = cartItemRequest.getMedPrescriptionRequest();
                    ResponseEntity response = cartOrderValidation.validateMedPrescriptionRequest(medPrescriptionRequest);
                    if (response.getStatusCode() != HttpStatus.OK) {
                        return response;
                    }
                }
            }
            Order order = null;

            // Check if the queuedOrderId is not null
            if (cartItemRequests.get(0).getQueuedOrderId() != null) {
                Long queuedOrderId = cartItemRequests.get(0).getQueuedOrderId();
                Optional<Order> optionalOrder = orderRepository.findByIdAndIntelRxIdAndPharmacyBranch(queuedOrderId, intelRxId, branch);

                // Check if the order exists for the given queuedOrderId
                if (!optionalOrder.isPresent()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("queuedOrderId not found"));
                } else {
                    order = optionalOrder.get();
                }
            }

            // Proceed to add items to the cart
            addToCart(cartItemRequests, order);


            return ResponseEntity.ok(StandardResponse.success("cart submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage()));
        }
    }

    @Transactional
    private void addToCart(List<CartItemRequest> cartItemRequests, Order order) {
        var user = userDetailsService.getAuthenticatedUser();
        PharmacyBranch branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();

        // Map to track existing cart items by inventory ID
        Map<Long, CartItem> existingCartItemsMap = new HashMap<>();

        // Retrieve existing cart items for the given order and user
        List<CartItem> existingCartItems = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndOrderAndUser(
                intelRxId, branch, order, user);
        for (CartItem cartItem : existingCartItems) {
            existingCartItemsMap.put(cartItem.getInventory().getId(), cartItem);
        }

        // Process each cart item request
        for (CartItemRequest request : cartItemRequests) {
            Patient patient = request.getPatientId() == null ? null :
                    patientRepository.findById(request.getPatientId()).orElse(null);

            Inventory inventory = inventoryRepository.findByIdAndIntelRxId(
                    request.getInventoryId(), intelRxId).orElse(null);

            CartItem cartItem;
            if (existingCartItemsMap.containsKey(inventory.getId())) {
                // Update existing cart item
                cartItem = existingCartItemsMap.get(inventory.getId());
                cartItem.setQuantity(request.getQuantity());
                cartItem.setAmount(request.getAmount());
                cartItem.setVat(request.getVat());
            } else {
                // Create a new cart item
                cartItem = new CartItem();
                cartItem.setIntelRxId(intelRxId);
                cartItem.setUser(user);
                cartItem.setPatient(patient);
                cartItem.setInventory(inventory);
                cartItem.setQuantity(request.getQuantity());
                cartItem.setAmount(request.getAmount());
                cartItem.setVat(request.getVat());
                cartItem.setPharmacyBranch(branch);
                cartItem.setCreatedAt(LocalDateTime.now());
                if (order != null) {
                    cartItem.setOrder(order);
                    cartItem.setStatus(true);
                }
            }

            // Save the cart item
            CartItem savedCart = cartItemRepository.save(cartItem);

            // Handle MedPrescriptionRequest if present
            if (request.getMedPrescriptionRequest() != null) {
                MedPrescriptionRequest med = request.getMedPrescriptionRequest();
                MedPrescriptionRequest prescriptionRequest = new MedPrescriptionRequest();

                prescriptionRequest.setCarteItemId(savedCart.getId());
                if (patient != null) {
                    prescriptionRequest.setPatientId(patient.getId());
                } else {
                    patient = patientService.addUnknownPatientToEntity(savedCart.getInventory().getItemName()
                    );
                    prescriptionRequest.setPatientId(patient.getId());
                }
                prescriptionRequest.setDosage(med.getDosage());
                prescriptionRequest.setRefillDurationId(med.getRefillDurationId());
                prescriptionRequest.setRefillAlert(Boolean.TRUE.equals(med.getRefillAlert()));
                prescriptionRequest.setSmsAlert(Boolean.TRUE.equals(med.getSmsAlert()));
                addToMedPrescription(prescriptionRequest, patient, savedCart);
            }
        }

        // Remove items from the cart that are not present in the new request
        removeOldCart(cartItemRequests, order);
    }


    @Transactional
    public ResponseEntity<?> resellItemToCart(List<CartItemRequest> cartItemRequests) {
        try {

            var user = userDetailsService.getAuthenticatedUser();

            String intelRxId = userDetailsService.getIntelRxId();

            if (user == null) {
                return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
            }
            for (CartItemRequest cartItemRequest : cartItemRequests) {
                ResponseEntity responseEntity = cartOrderValidation.validateCartItemRequest(cartItemRequest);
                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    return responseEntity;
                }

                Optional<Inventory> optionalInventory = inventoryRepository.findByIdAndIntelRxId(cartItemRequest.getInventoryId(), intelRxId);
                if (!optionalInventory.isPresent()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
                }

                if (cartItemRequest.getCartItemId() == null) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("cartItemId is required"));
                }

                Inventory inventory = optionalInventory.get();
                int qty = cartItemRequest.getQuantity();
                double salePrice = inventory.getSalePrice();
                double totalQtySalePrice = qty * salePrice;
                double totalAmount = cartItemRequest.getAmount();
                String msg = "The quantity amount for " + inventory.getItemName() + " is not correct or well calculated. It should be " + totalQtySalePrice + " Naira";

                if (totalQtySalePrice < totalAmount) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }
                if (totalQtySalePrice > totalAmount) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }


                if (cartItemRequest.getMedPrescriptionRequest() != null) {
                    MedPrescriptionRequest medPrescriptionRequest = cartItemRequest.getMedPrescriptionRequest();
                    ResponseEntity response = cartOrderValidation.validateMedPrescriptionRequest(medPrescriptionRequest);
                    if (response.getStatusCode() != HttpStatus.OK) {
                        return response;
                    }
                }
            }

            // Proceed to add items to the cart
            resellToCart(cartItemRequests);

            return ResponseEntity.ok(StandardResponse.success("cart submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage()));
        }
    }

    @Transactional
    private void resellToCart(List<CartItemRequest> cartItemRequests) {
        var user = userDetailsService.getAuthenticatedUser();
        PharmacyBranch branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();

        // Process each cart item request
        for (CartItemRequest request : cartItemRequests) {
            Patient patient = request.getPatientId() == null ? null :
                    patientRepository.findById(request.getPatientId()).orElse(null);

            Inventory inventory = inventoryRepository.findByIdAndIntelRxId(
                    request.getInventoryId(), intelRxId).orElse(null);

            // Create a new cart item
            CartItem cartItem = new CartItem();
            cartItem.setIntelRxId(intelRxId);
            cartItem.setUser(user);
            cartItem.setPatient(patient);
            cartItem.setInventory(inventory);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setAmount(request.getAmount());
            cartItem.setVat(request.getVat());
            cartItem.setPharmacyBranch(branch);
            cartItem.setCreatedAt(LocalDateTime.now());

            // Save the cart item
            CartItem savedCart = cartItemRepository.save(cartItem);

            // Handle MedPrescriptionRequest if present
            if (request.getMedPrescriptionRequest() != null) {
                MedPrescriptionRequest med = request.getMedPrescriptionRequest();
                MedPrescriptionRequest prescriptionRequest = new MedPrescriptionRequest();

                prescriptionRequest.setCarteItemId(savedCart.getId());
                if (patient != null) {
                    prescriptionRequest.setPatientId(patient.getId());
                } else {
                    patient = patientService.addUnknownPatientToEntity(savedCart.getInventory()
                            .getItemName());
                    prescriptionRequest.setPatientId(patient.getId());
                }
                prescriptionRequest.setDosage(med.getDosage());
                prescriptionRequest.setRefillDurationId(med.getRefillDurationId());
                prescriptionRequest.setRefillAlert(Boolean.TRUE.equals(med.getRefillAlert()));
                prescriptionRequest.setSmsAlert(Boolean.TRUE.equals(med.getSmsAlert()));
                addToMedPrescription(prescriptionRequest, patient, savedCart);
            }
        }

        // Remove items from the cart that are not present in the new request
        removeExistingMedPrescription(cartItemRequests);
    }

    private void removeOldCart(List<CartItemRequest> cartItemRequests, Order order) {
        var user = userDetailsService.getAuthenticatedUser();
        PharmacyBranch branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();

        List<Long> requestInventoryIds = cartItemRequests.stream()
                .map(CartItemRequest::getInventoryId)
                .collect(Collectors.toList());

        List<CartItem> existingCartItems = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndOrderAndUser(
                intelRxId, branch, order, user);

        for (CartItem existingCartItem : existingCartItems) {
            if (!requestInventoryIds.contains(existingCartItem.getInventory().getId())) {
                cartItemRepository.delete(existingCartItem);
            }
        }
    }

    private void addToMedPrescription(MedPrescriptionRequest request, Patient patient, CartItem cartItem) {
        var user = userDetailsService.getAuthenticatedUser();
        PharmacyBranch branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();

        MedPrescription medPrescription = medPrescriptionRepository.findByCartItem(cartItem)
                .orElseGet(MedPrescription::new);

        medPrescription.setDosage(request.getDosage());
        medPrescription.setCartItem(cartItem);
        medPrescription.setPatient(patient);
        medPrescription.setUser(user);
        medPrescription.setRefill(Boolean.TRUE.equals(request.getRefillAlert()));

        if (request.getRefillDurationId() != null) {
            refillDurationRepository.findById(request.getRefillDurationId())
                    .ifPresent(medPrescription::setRefillDuration);
        }

        medPrescription.setSmsAlert(Boolean.TRUE.equals(request.getSmsAlert()));
        medPrescription.setIntelRxId(intelRxId);
        medPrescription.setPharmacyBranch(branch);
        medPrescription.setPrescriptionDate(LocalDate.now());

        medPrescriptionRepository.save(medPrescription);
    }

    private void removeExistingMedPrescription(List<CartItemRequest> cartItemRequests) {
        String intelRxId = userDetailsService.getIntelRxId();
        for (CartItemRequest request : cartItemRequests) {
            if (request.getMedPrescriptionRequest() != null) {
                Optional<CartItem> optionalCartItem = cartItemRepository.
                        findByIdAndIntelRxId(request.getCartItemId(), intelRxId);

                if (optionalCartItem.isPresent()) {

                    CartItem cartItem = optionalCartItem.get();

                    Optional<MedPrescription> optionalMedPrescription = medPrescriptionRepository.findByCartItem(cartItem);

                    if (optionalMedPrescription.isPresent()) {
                        MedPrescription medPrescription = optionalMedPrescription.get();
                        medPrescription.setRefill(false);
                        medPrescription.setSmsAlert(false);
                        medPrescriptionRepository.save(medPrescription);
                    }
                }
            }
        }
    }

    public ResponseEntity<?> getCartItem() {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(new Response("failed", "You are unauthorized"));
        }
        String intelRxId = userDetailsService.getIntelRxId();

        List<CartItem> cartItemList = cartItemRepository.findByIntelRxIdAndUserAndStatus(intelRxId, user, false);
        List<CartItemResponse> responseList = cartItemList.stream().map((cartItem) -> salesMapping.mapToCartItem(cartItem)).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    //Order service
    @Transactional
    public ResponseEntity<?> submitOrder(OrderRequest request) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            String intelRxId = userDetailsService.getIntelRxId();
            String ref = utility.generateUniqueInvoiceRefNumber();
            PharmacyBranch branch = userDetailsService.getBranch();
            var pharmacyInfo = userDetailsService.getPharmacyInfo();

            Patient patient = null;
            if (request.getPatientId() == null) {
                patient = patientService.addUnknownPatientToEntity("");
            } else {
                Optional<Patient> optionalPatient = patientRepository.findById(request.getPatientId());
                patient = optionalPatient.get();
            }
            Optional<Order> optionalOrder = null;
            Order order = null;
            List<CartItem> cartItems = null;
            Order updateOrder = null;

            //get coupon amount if coupon code exist
            double couponAmount = 0.0;
            GeneralCouponDetail couponDetails = null;
            if (request.getCouponCode() != null) {
                couponDetails = couponDetailRepository.findFirstByCouponCodeAndOrderReferenceIsNull(
                        request.getCouponCode()).orElse(null);

                if (couponDetails == null) {

                    log.info("Searching for coupon with code: {}", request.getCouponCode());

                    return ResponseEntity.badRequest().body(
                            StandardResponse.error("Coupon code does not exist or already linked to an order." + request.getCouponCode())
                    );

                }
                couponAmount = couponDetails.getGeneralCoupon().getAmountPerPerson();
            }

            //if order id is not null
            if (request.getId() != null) {
                optionalOrder = orderRepository.findByIdAndIntelRxIdAndPharmacyBranchAndPatientAndStatus
                        (request.getId(), intelRxId, branch, patient, false);
                order = optionalOrder.get();

                //initialize the order by saving it first before updating
                order.setIntelRxId(intelRxId);
                order.setPatient(patient);

                updateOrder = orderRepository.save(order);
                cartItems = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndOrderAndPatientAndStatus(
                        intelRxId, branch, updateOrder, patient, true);

            } else {

                order = new Order();
                //initialize the order by saving it first before updating
                order.setIntelRxId(intelRxId);
                order.setPatient(patient);

                updateOrder = orderRepository.save(order);
                cartItems = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndOrderAndPatientAndStatus(
                        intelRxId, branch, null,
                        request.getPatientId() == null ? null : patient, false);

            }

            double totalItemAmountByQty = 0.0;
            double totalPayAble = 0.0;

            Inventory inventory = null;
            MedPrescription medPrescription = null;

            StringBuilder messageBuilder = new StringBuilder();
            if (!cartItems.isEmpty()) {
                for (CartItem cartItem : cartItems) {
                    totalItemAmountByQty += cartItem.getAmount();
                }
                String msg = "The item total amount or the invoice amount for this order should be " + totalItemAmountByQty + " Naira";
                if (totalItemAmountByQty < request.getTotalAmount()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }

                totalPayAble = request.getCashPayment() + request.getPosPayment() + request.getTransferPayment() + couponAmount;
                String msg2 = "The totalPayAble amount for this order is less than the item total amount or the invoice amount . Kindly complete your payment through POS, Transfer or Cash";

                if (totalPayAble < request.getTotalAmount()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg2));
                }


                for (CartItem cartItem : cartItems) {
                    cartItem.setStatus(true);
                    cartItem.setOrder(updateOrder);
                    cartItem.setPatient(patient);
                    cartItem.setPharmacyBranch(branch);
                    cartItem.setCreatedAt(LocalDateTime.now());

                    //Check and Clear Old Medication On Renewal If Available
                    //clearOldMedicationOnRenewal(cartItem);

                    if (cartItem.getMedPrescription() != null && cartItem.getMedPrescription().isSmsAlert()) {
                        // Get inventory and medication details
                        inventory = cartItem.getInventory();
                        medPrescription = cartItem.getMedPrescription();

                        // Extract necessary information
                        String inventoryName = inventory != null ? utility.obfuscateString(inventory.getItemName()) : "Unknown Inventory";
                        String medicationDetails = medPrescription != null ? medPrescription.getDosage() : "No Prescription";

                        String msgFrom = "Medication Reminder From " + pharmacyInfo.getPharmacyName() + "\n";
                        // Combine information
                        messageBuilder.append(msgFrom)
                                .append("Medication Name: ").append(inventoryName)
                                .append(", Medication: ").append(medicationDetails)
                                .append("\n");
                    }

                }

            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("Cart is empty"));
            }

            //Send the medication dosage after ordering if they select the dosage sms
            if (utility.isNigerianPhoneNumber(patient.getPhoneNumber()) && medPrescription != null
                    && medPrescription.isSmsAlert()) {
                // Convert StringBuilder to String
                String message = messageBuilder.toString();
                termiiSMSService.sendSms(patient.getPhoneNumber(), message);
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());

            User cashier = null;
            if (optionalEmployee.isPresent()) {
                Employee employee = optionalEmployee.get();
                if (employee.getJobInformation().getJobTitle().getName().equals("Cashier")) {
                    cashier = userDetailsService.getAuthenticatedUser();
                }
            }

            updateOrder.setOrderRef(ref);
            updateOrder.setIntelRxId(intelRxId);
            updateOrder.setOrderedDate(LocalDateTime.now());
            updateOrder.setStatus(true);
            updateOrder.setSalesStatus("Completed");
            updateOrder.setSalesPerson(user);
            updateOrder.setCashier(cashier);
            updateOrder.setPatient(patient);
            updateOrder.setTotalAmount(totalItemAmountByQty);
            updateOrder.setCashPayment(request.getCashPayment());
            updateOrder.setPosPayment(request.getPosPayment());
            updateOrder.setTransferPayment(request.getTransferPayment());
            updateOrder.setCouponAmount(couponAmount);
            updateOrder.setCouponCode(request.getCouponCode());
            updateOrder.setTotalPayable(totalPayAble);
            updateOrder.setBalance(request.getBalance());
            updateOrder.setChangeReturn(request.getChangeReturn());
            updateOrder.setPharmacyBranch(branch);

            Order order1 = orderRepository.save(updateOrder);
            cartItemRepository.saveAll(cartItems);

            if (request.getCouponCode() != null) {
                GeneralCoupon generalCoupon = couponDetails.getGeneralCoupon();
                String description = (generalCoupon.getCouponTitle() != null ? generalCoupon.getCouponTitle() : "Coupon") +
                        " used by " + patient.getName() +
                        " at " + (pharmacyInfo != null ? pharmacyInfo.getPharmacyName() : "Unknown Pharmacy");
                CouponHistoryRequest historyRequest = new CouponHistoryRequest();
                historyRequest.setCouponCode(request.getCouponCode());
                historyRequest.setTrxType("credit");
                historyRequest.setTrxCouponType(String.valueOf(generalCoupon.getCouponType()));
                historyRequest.setAmount(couponAmount);
                historyRequest.setDescription(description);
                //historyRequest.setOrder(order1);  // Ensure order1 is initialized

                // Ensure the order is fully initialized and coupon details are correct
                if (order1 != null && couponAmount > 0) {
                    generalCouponService.addToCouponHistory(historyRequest, order1);
                } else {
                    log.error("Order not initialized or invalid coupon amount");
                    return ResponseEntity.badRequest().body(StandardResponse.error
                            ("Order not initialized or invalid coupon amount"));
                }

                //generalCouponService.addToCouponHistory(historyRequest, order1);

            }


            return ResponseEntity.ok(StandardResponse.success("Transaction Successful"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage()));
        }

    }

    //ResellOrder service
    @Transactional
    public ResponseEntity<?> submitResellOrder(OrderRequest request) {
        try {
            var user = userDetailsService.getAuthenticatedUser();

            if (user == null) {
                return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
            }

            String intelRxId = userDetailsService.getIntelRxId();
            String ref = utility.generateUniqueInvoiceRefNumber();
            PharmacyBranch branch = userDetailsService.getBranch();
            var pharmacyInfo = userDetailsService.getPharmacyInfo();

            Optional<Patient> optionalPatient = patientRepository.findById(request.getPatientId());
            Patient patient = optionalPatient.get();


            Order order = null;
            List<CartItem> cartItems = null;
            Order updateOrder = null;

            //get coupon amount if coupon code exist
            double couponAmount = 0.0;
            GeneralCouponDetail couponDetails = null;
            if (request.getCouponCode() != null) {
                couponDetails = couponDetailRepository.findFirstByCouponCodeAndOrderReferenceIsNull(
                        request.getCouponCode()).orElse(null);

                if (couponDetails == null) {

                    log.info("Searching for coupon with code: {}", request.getCouponCode());

                    return ResponseEntity.badRequest().body(
                            StandardResponse.error("Coupon code does not exist or already linked to an order." + request.getCouponCode())
                    );

                }
                couponAmount = couponDetails.getGeneralCoupon().getAmountPerPerson();
            }

            if (request.getId() != null) {
                order = new Order();
                //initialize the order by saving it first before updating
                order.setIntelRxId(intelRxId);
                order.setPatient(patient);
                updateOrder = orderRepository.save(order);

                cartItems = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndOrderAndPatientAndStatus(
                        intelRxId, branch, null, patient, false);
            }

            double totalItemAmountByQty = 0.0;
            double totalPayAble = 0.0;

            Inventory inventory = null;
            MedPrescription medPrescription = null;

            StringBuilder messageBuilder = new StringBuilder();
            if (!cartItems.isEmpty()) {
                for (CartItem cartItem : cartItems) {
                    totalItemAmountByQty += cartItem.getAmount();
                }
                String msg = "The item total amount or the invoice amount for this order should be " + totalItemAmountByQty + " Naira";
                if (totalItemAmountByQty < request.getTotalAmount()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg));
                }

                totalPayAble = request.getCashPayment() + request.getPosPayment() + request.getTransferPayment() + couponAmount;
                String msg2 = "The totalPayAble amount for this order is less than the item total amount or the invoice amount . Kindly complete your payment through POS, Transfer or Cash";

                if (totalPayAble < request.getTotalAmount()) {
                    return ResponseEntity.badRequest().body(StandardResponse.error(msg2));
                }


                for (CartItem cartItem : cartItems) {
                    cartItem.setStatus(true);
                    cartItem.setOrder(updateOrder);
                    cartItem.setPatient(patient);
                    cartItem.setPharmacyBranch(branch);
                    cartItem.setCreatedAt(LocalDateTime.now());

                    if (cartItem.getMedPrescription() != null && cartItem.getMedPrescription().isSmsAlert()) {
                        // Get inventory and medication details
                        inventory = cartItem.getInventory();
                        medPrescription = cartItem.getMedPrescription();

                        // Extract necessary information
                        String inventoryName = inventory != null ? utility.obfuscateString(inventory.getItemName()) : "Unknown Inventory";
                        String medicationDetails = medPrescription != null ? medPrescription.getDosage() : "No Prescription";

                        String msgFrom = "Medication Reminder From " + pharmacyInfo.getPharmacyName() + "\n";
                        // Combine information
                        messageBuilder.append(msgFrom)
                                .append("Medication Name: ").append(inventoryName)
                                .append(", Medication: ").append(medicationDetails)
                                .append("\n");
                    }

                }

            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("Cart is empty"));
            }

            //Send the medication dosage after ordering if they select the dosage sms
            if (utility.isNigerianPhoneNumber(patient.getPhoneNumber()) && medPrescription != null
                    && medPrescription.isSmsAlert()) {
                // Convert StringBuilder to String
                String message = messageBuilder.toString();
                termiiSMSService.sendSms(patient.getPhoneNumber(), message);
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());

            User cashier = null;
            if (optionalEmployee.isPresent()) {
                Employee employee = optionalEmployee.get();
                if (employee.getJobInformation().getJobTitle().getName().equals("Cashier")) {
                    cashier = userDetailsService.getAuthenticatedUser();
                }
            }

            updateOrder.setOrderRef(ref);
            updateOrder.setIntelRxId(intelRxId);
            updateOrder.setOrderedDate(LocalDateTime.now());
            updateOrder.setStatus(true);
            updateOrder.setSalesStatus("Completed");
            updateOrder.setSalesPerson(user);
            updateOrder.setCashier(cashier);
            updateOrder.setPatient(patient);
            updateOrder.setTotalAmount(totalItemAmountByQty);
            updateOrder.setCashPayment(request.getCashPayment());
            updateOrder.setPosPayment(request.getPosPayment());
            updateOrder.setTransferPayment(request.getTransferPayment());
            updateOrder.setCouponAmount(couponAmount);
            updateOrder.setCouponCode(request.getCouponCode());
            updateOrder.setTotalPayable(totalPayAble);
            updateOrder.setBalance(request.getBalance());
            updateOrder.setChangeReturn(request.getChangeReturn());
            updateOrder.setPharmacyBranch(branch);

            Order order1 = orderRepository.save(updateOrder);
            cartItemRepository.saveAll(cartItems);

            if (request.getCouponCode() != null && couponDetails != null) {
                GeneralCoupon generalCoupon = couponDetails.getGeneralCoupon();
                String description = (generalCoupon.getCouponTitle() != null ? generalCoupon.getCouponTitle() : "Coupon") +
                        " used by " + (patient != null ? patient.getName() : "Unknown Patient") +
                        " at " + (pharmacyInfo != null ? pharmacyInfo.getPharmacyName() : "Unknown Pharmacy");

                CouponHistoryRequest historyRequest = new CouponHistoryRequest();
                historyRequest.setTrxType("credit");
                historyRequest.setTrxCouponType(String.valueOf(generalCoupon.getCouponType()));
                historyRequest.setAmount(couponAmount);
                historyRequest.setDescription(description);
                //historyRequest.setOrder(order1);  // Ensure order1 is initialized
                generalCouponService.addToCouponHistory(historyRequest, order1);
            }


            return ResponseEntity.ok(StandardResponse.success("Transaction Successful"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage()));
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> transferOrder(Long orderId) {
        try {
            var user = userDetailsService.getAuthenticatedUser();

            String intelRxId = userDetailsService.getIntelRxId();
            PharmacyBranch branch = userDetailsService.getBranch();

            if (orderId != null) {

                Optional<Order> optionalOrder = orderRepository.
                        findByIdAndIntelRxIdAndPharmacyBranch(orderId, intelRxId, branch);

                if (optionalOrder.isPresent()) {
                    return ResponseEntity.ok(StandardResponse.success("Added To Queue Successful"));
                }

            }

            List<CartItem> cartItems = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndUserAndStatus(
                    intelRxId, branch, user, false);

            Patient patient = cartItems.get(0).getPatient();

            if (patient == null) {
                patient = patientService.addUnknownPatientToEntity(cartItems.get(0)
                        .getInventory().getItemName());
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());

            User cashier = null;
            if (optionalEmployee.isPresent()) {
                Employee employee = optionalEmployee.get();
                if (employee.getJobInformation().getJobTitle().getName().equals("Cashier")) {
                    cashier = userDetailsService.getAuthenticatedUser();
                }
            }

            Order updateOrder = new Order();
            updateOrder.setIntelRxId(intelRxId);
            updateOrder.setOrderedDate(LocalDateTime.now());
            updateOrder.setStatus(false);
            updateOrder.setSalesStatus("Pending");
            updateOrder.setSalesPerson(user);
            updateOrder.setCashier(cashier);
            updateOrder.setPatient(patient);
            updateOrder.setPharmacyBranch(branch);

            orderRepository.save(updateOrder);

            if (!cartItems.isEmpty()) {
                for (CartItem cartItem : cartItems) {
                    cartItem.setStatus(true);
                    cartItem.setPatient(patient);
                    cartItem.setOrder(updateOrder);
                    cartItem.setIntelRxId(intelRxId);
                    cartItem.setPharmacyBranch(branch);
                    cartItem.setCreatedAt(LocalDateTime.now());
                }

                cartItemRepository.saveAll(cartItems);
                return ResponseEntity.ok(StandardResponse.success("Added To Queue Successful. This is the Queue ID:" + patient.getName()));

            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("Cart is empty"));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getLocalizedMessage()));
        }

    }

    public ResponseEntity<?> removeCartOrder(Long orderId) {
        var user = userDetailsService.getAuthenticatedUser();
        PharmacyBranch branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();
        JobInformationRequest jobReq = userDetailsService.getJobInfo();

        if (intelRxId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
        }

        Long jobInfoId = (jobReq != null) ? jobReq.getJobTitleId() : null;
        Optional<Order> optionalOrder = findOrder(orderId, intelRxId, branch, user, jobInfoId);

        if (optionalOrder.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.error("Order for this orderId does not exist"));
        }

        Order order = optionalOrder.get();
        List<CartItem> existingCartItems = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndOrderAndUser(
                intelRxId, branch, order, user);

        for (CartItem existingCartItem : existingCartItems) {
            updateInventoryReturn(existingCartItem.getInventory().getId(), existingCartItem.getQuantity());
            if (existingCartItem.getMedPrescription() != null) {
                MedPrescription medPrescription = existingCartItem.getMedPrescription();
                medPrescriptionRepository.delete(medPrescription);
            }
            cartItemRepository.delete(existingCartItem);
        }
        orderRepository.delete(order);

        return ResponseEntity.ok(StandardResponse.success("Order Successfully Deleted"));
    }

    private Optional<Order> findOrder(Long orderId, String intelRxId, PharmacyBranch branch, User user, Long jobInfoId) {
        if (user.getUserType() == UserType.OWNER) {
            return orderRepository.findByIdAndIntelRxId(orderId, intelRxId);
        } else if (jobInfoId != null) {
            if (jobInfoId.equals(2L)) {
                // Cashier
                return orderRepository.findByIdAndCashier(orderId, intelRxId, branch == null ? null : branch.getId(), user);
            } else if (jobInfoId.equals(3L)) {
                // Sales Person
                return orderRepository.findByIdAndSalesPerson(orderId, intelRxId, branch == null ? null : branch.getId(), user);
            }
        }
        return orderRepository.findByIdAndIntelRxId(orderId, intelRxId);
    }

    public void updateInventoryReturn(Long inventoryId, int quantity) {
        var user = userDetailsService.getAuthenticatedUser();
        PharmacyBranch branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();

        if (branch == null) {
            Inventory inventory = inventoryRepository.findByIdAndIntelRxId(inventoryId, intelRxId).orElse(null);
            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + quantity);
                inventoryRepository.save(inventory);
            }
        } else {
            TransferInventory transferInventory = transferInventoryRepository.findByIntelRxIdAndId(intelRxId, inventoryId).orElse(null);
            if (transferInventory != null) {
                transferInventory.setQuantity(transferInventory.getQuantity() + quantity);
                transferInventoryRepository.save(transferInventory);
            }
        }
    }

    public ResponseEntity<?> getQueuedItems() {

        var user = userDetailsService.getAuthenticatedUser();

        if (user == null) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }

        String intelRxId = userDetailsService.getIntelRxId();

        PharmacyBranch branch = userDetailsService.getBranch();
        List<Order> orderList = null;
        if (branch == null && user.getUserType() == UserType.OWNER) {
            orderList = orderRepository.findByIntelRxIdAndPharmacyBranchAndStatus(intelRxId, null, false);
        }
        if (branch == null && user.getUserType() == UserType.EMPLOYEE) {
            orderList = orderRepository.findByIntelRxIdAndPharmacyBranchAndStatus(intelRxId, null, false);
        }
        if (branch != null && user.getUserType() == UserType.EMPLOYEE) {
            orderList = orderRepository.findByIntelRxIdAndPharmacyBranchAndStatus(intelRxId, branch, false);
        }


        if (orderList == null || orderList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderList));
        }
        List<OrderResponse> responseList = orderList.stream().map((cartItem) -> salesMapping.mapToOrderResponse(cartItem)).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> getQueuedItemDetails(Long orderId) {
        var user = userDetailsService.getAuthenticatedUser();
        PharmacyBranch branch = userDetailsService.getBranch();
        if (user == null) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }
        String intelRxId = userDetailsService.getIntelRxId();

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (!optionalOrder.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "orderId not found"));
        }
        Order order = optionalOrder.get();

        List<CartItem> cartItemList = cartItemRepository.findByIntelRxIdAndPharmacyBranchAndOrderAndPatientAndStatus(intelRxId, branch, order, order.getPatient(), true);


        List<CartItemResponse> cartItemResponse = cartItemList.stream().map(cartItem -> salesMapping.mapToCartItem(cartItem)).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(cartItemResponse));
    }

    public ResponseEntity<?> searchPatientsFromOrder(String keyword) {
        var user = userDetailsService.getAuthenticatedUser();
        var branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();
        List<Order> orderList = orderRepository.searchOrdersByPatientDetailsAndIntelRxIdAndStatusAndBranch(
                keyword, intelRxId, false, branch);
        if (orderList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderList));
        }
        List<OrderResponse> responseList = orderList.stream().map((cartItem) -> salesMapping.mapToOrderResponse(cartItem)).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public void clearOldMedicationOnRenewal(CartItem cartItem) {
        if (cartItem.getMedPrescription() != null) {
            MedPrescription medPrescription = cartItem.getMedPrescription();
            medPrescription.setRefill(false);
            medPrescription.setSmsAlert(false);
            medPrescriptionRepository.save(medPrescription);
        }
    }


}
