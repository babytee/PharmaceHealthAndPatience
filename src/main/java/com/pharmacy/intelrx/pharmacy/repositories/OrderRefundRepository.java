package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.OrderRefund;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRefundRepository extends JpaRepository<OrderRefund, Long> {
    List<OrderRefund>findByOrderId(Long orderId);

    Optional<OrderRefund>findByOrderIdAndPharmacyBranch(Long orderId, PharmacyBranch pharmacyBranch);

    List<OrderRefund>findAllByIntelRxId(String intelRxId);

    List<OrderRefund>findAllByIntelRxIdAndRefundedBy(String intelRxId, User refundedBy);
}
