package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.RefillDurationRepository;
import com.pharmacy.intelrx.pharmacy.services.OrderRefundService;
import com.pharmacy.intelrx.pharmacy.services.PatientService;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SalesMapping {

    private final FilterInventory filterInventory;
    private final PatientService patientService;
    private final PharmacyMapping pharmacyMapping;
    private final UserDetailsService userDetailsService;
    private final OrderRefundService orderRefundService;
    private final PharmacyRepository pharmacyRepository;
    private final CartItemRepository cartItemRepository;
    private final ContactInfoRepository contactInfoRepository;

    public CartItemResponse mapToCartItem(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();

        try {
            cartItemResponse.setId(cartItem.getId());
            cartItemResponse.setOrderId(cartItem.getOrder() == null ? null : cartItem.getOrder().getId());

            cartItemResponse.setInventoryItem(filterInventory.mapToAddedInventoryResponse(cartItem.getInventory()));

            cartItemResponse.setAmount(cartItem.getAmount());
            cartItemResponse.setQuantity(cartItem.getQuantity());
            cartItemResponse.setIntelRxId(cartItem.getIntelRxId());

            if (cartItem.getMedPrescription() != null) {
                MedPrescription medPrescriptions = cartItem.getMedPrescription();
                MedPrescriptionResponse responseList = mapToMedPrescription(medPrescriptions);
                cartItemResponse.setMedPrescription(responseList);
            }

            cartItemResponse.setPatient(patientService.mapToPatient(cartItem.getPatient()));

            if (cartItem.getPharmacyBranch() != null) {
                cartItemResponse.setBranch(pharmacyMapping.mapToBranchResponse(cartItem.getPharmacyBranch()));
            }

            cartItemResponse.setItemRefund(orderRefundService.mapToItemRefund(cartItem.getId()));
        } catch (Exception e) {
            // Log the exception and cartItem details
            System.err.println("Error mapping CartItem to CartItemResponse: " + e.getMessage());
            e.printStackTrace();
        }

        return cartItemResponse;
    }

    public CartItemResponse mapToOrderItem(Order order) {
        List<CartItem> cartItems = cartItemRepository.findByIntelRxIdAndAndOrder(order.getIntelRxId(), order);

        CartItemResponse cartItemResponse = new CartItemResponse();

        for (CartItem cartItem : cartItems) {
            cartItemResponse.setId(cartItem.getId());
            cartItemResponse.setOrderId(cartItem.getOrder().getId());
            cartItemResponse.setInventoryItem(filterInventory.mapToAddedInventoryResponse(cartItem.getInventory()));
            cartItemResponse.setAmount(cartItem.getAmount());
            cartItemResponse.setQuantity(cartItem.getQuantity());
            cartItemResponse.setIntelRxId(cartItem.getIntelRxId());
            if (cartItem.getMedPrescription() != null) {
                MedPrescription medPrescriptions = cartItem.getMedPrescription();
                MedPrescriptionResponse responseList = mapToMedPrescription(medPrescriptions);
                cartItemResponse.setMedPrescription(responseList);
            }
            cartItemResponse.setPatient(patientService.mapToPatient(cartItem.getPatient()));
            if (cartItem.getPharmacyBranch() != null) {
                cartItemResponse.setBranch(pharmacyMapping.mapToBranchResponse(cartItem.getPharmacyBranch()));
            }
        }
        return cartItemResponse;
    }

    public OrderResponse mapToOrderResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setOrderRef(order.getOrderRef());
        orderResponse.setBalance(order.getBalance());
        orderResponse.setCashPayment(order.getCashPayment());
        orderResponse.setPosPayment(order.getPosPayment());
        orderResponse.setTransferPayment(order.getTransferPayment());
        orderResponse.setIntelRxId(order.getIntelRxId());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setChangeReturn(order.getChangeReturn());
        orderResponse.setSalesStatus(order.getSalesStatus());
        orderResponse.setStatus(order.isStatus());

//        List<CartItem> cartItems = order.getCartItem();
//        List<Boolean>countPoison = new ArrayList<>();
//        for (CartItem cartItem:cartItems){
//            countPoison.add(cartItem.getInventory().isPoison());
//        }
//        orderResponse.setPoison(countPoison.size());

        if (order.getSalesPerson() == null) {
            orderResponse.setSalesPerson(null);
        } else {
            orderResponse.setSalesPerson(userDetailsService.mapToUserResponse(order.getSalesPerson()));
        }

        if (order.getCashier() == null) {
            orderResponse.setCashier(null);
        } else {
            orderResponse.setCashier(userDetailsService.mapToUserResponse(order.getCashier()));
        }

        List<String> paymentMethod = new ArrayList<>();
        if (order.getCashPayment() > 0) {
            paymentMethod.add("Cash");
        }
        if (order.getPosPayment() > 0) {
            paymentMethod.add("POS");
        }
        if (order.getTransferPayment() > 0) {
            paymentMethod.add("Transfer");
        }


        orderResponse.setPaymentMethod(paymentMethod);
        if (order.getPatient() != null) {
            orderResponse.setPatient(patientService.mapToOrderPatient(order.getPatient()));
        }
        if (order.getPharmacyBranch() != null) {
            orderResponse.setBranch(pharmacyMapping.mapToBranchResponse(order.getPharmacyBranch()));
        }
        orderResponse.setOrderedDate(order.getOrderedDate());
        orderResponse.setOrderRefund(orderRefundService.mapToOrderRefund(order));

        return orderResponse;
    }

    public ReceiptResponse mapToReceiptResponse(Order order) {

        ReceiptResponse orderResponse = new ReceiptResponse();

        String pharmacyName = "";
        String branchName = "";
//        String branchAddress = "";
//        String address = "";
        Optional<ContactInfo> bContactInfo = null;
        Optional<ContactInfo> contactInfo = null;
        if (order.getPharmacyBranch() != null) {
            PharmacyBranch branch = order.getPharmacyBranch();
            Pharmacy pharmacy = branch.getPharmacy();
            branchName = branch.getName();
            bContactInfo = contactInfoRepository.findByPharmacyBranchId(branch.getId());
            ContactInfo branchContactInfo = bContactInfo.get();
            //ContactInfo branchContactInfo = branch.getContactInfo();
            //branchAddress = branchContactInfo != null ? branchContactInfo : null;
            orderResponse.setBranchAddress(mapToContactInfoReqRes(branchContactInfo));

            contactInfo = contactInfoRepository.findByPharmacyId(pharmacy.getId());
            ContactInfo pContactInfo = contactInfo.get();
            pharmacyName = pharmacy.getPharmacyName();
            //address = contactInfo != null ? pContactInfo.getStreetAddress() : null;
            orderResponse.setAddress(mapToContactInfoReqRes(pContactInfo));
            orderResponse.setPharmacyNumber(pharmacy.getPharmacyNumber());

        }

        if (order.getPharmacyBranch() == null) {

            Optional<Pharmacy> pharmacyOption = pharmacyRepository.findByIntelRxId(order.getIntelRxId());
            Pharmacy pharmacy = pharmacyOption.get();
            //ContactInfo contactInfo = pharmacy.getContactInfo();
            pharmacyName = pharmacy.getPharmacyName();
            contactInfo = contactInfoRepository.findByPharmacyId(pharmacy.getId());
            ContactInfo pContactInfo = contactInfo.get();
            //address = pharmacy.getContactInfo() != null ? pharmacy.getContactInfo() : null;
            orderResponse.setAddress(mapToContactInfoReqRes(pContactInfo));
            orderResponse.setPharmacyNumber(pharmacy.getPharmacyNumber());

        }

        orderResponse.setPharmacyName(pharmacyName);

        orderResponse.setBranchName(branchName);
        //orderResponse.setBranchAddress(branchAddress);
        orderResponse.setReceiptNo(order.getOrderRef());
        orderResponse.setCashTendered(order.getCashPayment());
        orderResponse.setPosAmount(order.getPosPayment());
        orderResponse.setTransferAmount(order.getTransferPayment());
        orderResponse.setCouponAmount(order.getCouponAmount());
        orderResponse.setCouponCode(order.getCouponCode());
        orderResponse.setTotal(order.getTotalAmount());
        orderResponse.setChangeReturn(order.getChangeReturn());

        if (order.getSalesPerson() == null) {
            orderResponse.setSalesPerson(null);
        } else {
            orderResponse.setSalesPerson(userDetailsService.mapToUserResponse(order.getSalesPerson()));
        }

        if (order.getCashier() == null) {
            orderResponse.setCashier(null);
        } else {
            orderResponse.setCashier(userDetailsService.mapToUserResponse(order.getCashier()));
        }

        orderResponse.setSoldTo(patientService.mapToPatient(order.getPatient()));
        orderResponse.setCartOrder(mapToOrderItem(order));

        return orderResponse;
    }

    public MedPrescriptionResponse mapToMedPrescription(MedPrescription medPrescription) {
        MedPrescriptionResponse medPrescriptionResponse = new MedPrescriptionResponse();
        medPrescriptionResponse.setId(medPrescription.getId());
        medPrescriptionResponse.setDosage(medPrescription.getDosage());
        medPrescriptionResponse.setIntelRxId(medPrescription.getIntelRxId());
        medPrescriptionResponse.setRefillDuration(medPrescription.getRefillDuration());
        medPrescriptionResponse.setRefillAlert(medPrescription.isRefill());
        medPrescriptionResponse.setSmsAlert(medPrescription.isSmsAlert());
        medPrescriptionResponse.setPatient(patientService.mapToPatient(medPrescription.getPatient()));
        if (medPrescription.getPharmacyBranch() != null) {
            medPrescriptionResponse.setBranch(pharmacyMapping.mapToBranchResponse(medPrescription.getPharmacyBranch()));
        }
        return medPrescriptionResponse;
    }

    public ContactInfoReqRes mapToContactInfoReqRes(ContactInfo contactInfo) {
        ContactInfoReqRes contactInfoReqRes = new ContactInfoReqRes();
        if (contactInfo != null) {
            contactInfoReqRes.setId(contactInfo.getId());
            contactInfoReqRes.setCountry(contactInfo.getCountry());
            contactInfoReqRes.setState(contactInfo.getState());
            contactInfoReqRes.setCity(contactInfo.getCity());
            contactInfoReqRes.setLga(contactInfo.getLga());
            contactInfoReqRes.setStreetAddress(contactInfo.getStreetAddress());
            contactInfoReqRes.setZipCode(contactInfo.getZipCode());
        }
        return contactInfoReqRes;
    }

}
