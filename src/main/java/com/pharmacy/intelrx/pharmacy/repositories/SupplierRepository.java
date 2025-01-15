package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.PaymentStatus;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByInvoiceRefNumber(String invoiceRefNumber);

    List<Supplier> findAllByIntelRxId(String intelRxId);

    Optional<Supplier> findByIdAndIntelRxId(Long id,String intelRxId);

    Optional<Supplier> findBySupplierPharmacy(Pharmacy supplierPharmacy);

    // List<Supplier>findAllByPaymentStatus(String paymentStatus);

    @Query("SELECT s FROM Supplier s WHERE " +
            "(:searchTerm IS NULL OR " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.intelRxId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:intelRxId IS NULL OR LOWER(s.intelRxId) = LOWER(:intelRxId))")
    List<Supplier> searchSuppliersByAnyFieldAndIntelRxId(
            @Param("searchTerm") String searchTerm,
            @Param("intelRxId") String intelRxId
    );



}
