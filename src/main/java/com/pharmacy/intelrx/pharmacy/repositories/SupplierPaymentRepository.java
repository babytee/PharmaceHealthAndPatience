package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.PaymentStatus;
import com.pharmacy.intelrx.pharmacy.models.Supplier;
import com.pharmacy.intelrx.pharmacy.models.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment,Long> {
    List<SupplierPayment>findAllBySupplier(Supplier supplier);

    Optional<SupplierPayment>findByIdAndIntelRxId(Long id,String intelRx);

    List<SupplierPayment>findAllByIntelRxIdAndPaymentStatus(String intelRx, PaymentStatus paymentStatus);

    List<SupplierPayment>findAllByIntelRxId(String intelRx);

    Optional<SupplierPayment> findByInvoiceRefNumber(String invoiceRefNumber);

    @Query("SELECT s FROM SupplierPayment s WHERE " +
            "(:searchTerm IS NULL OR " +
            "LOWER(s.invoiceRefNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.intelRxId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(s.totalAmountPaid AS STRING)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(s.balanceDue AS STRING)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(s.dueDay AS STRING)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(s.dueMonth AS STRING)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(s.dueYear AS STRING)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:intelRxId IS NULL OR LOWER(s.intelRxId) = LOWER(:intelRxId))")
    List<SupplierPayment> searchSuppliersByAnyFieldAndIntelRxId(
            @Param("searchTerm") String searchTerm,
            @Param("intelRxId") String intelRxId
    );
}
