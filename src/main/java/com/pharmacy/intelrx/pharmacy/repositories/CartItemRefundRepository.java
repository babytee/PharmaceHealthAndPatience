package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.CartItemRefund;
import com.pharmacy.intelrx.pharmacy.models.OrderRefund;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRefundRepository extends JpaRepository<CartItemRefund, Long> {
    List<CartItemRefund>findByCartItemId(Long cartItemId);

    Optional<CartItemRefund> findByCartItemIdAndPharmacyBranch(Long cartItemId, PharmacyBranch pharmacyBranch);

    List<CartItemRefund>findAllByIntelRxId(String intelRxId);

    List<CartItemRefund>findAllByIntelRxIdAndRefundedBy(String intelRxId, User refundedBy);
}
