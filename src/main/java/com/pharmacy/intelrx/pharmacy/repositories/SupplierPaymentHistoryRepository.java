package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.SupplierPayment;
import com.pharmacy.intelrx.pharmacy.models.SupplierPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierPaymentHistoryRepository extends JpaRepository<SupplierPaymentHistory, Long> {

    List<SupplierPaymentHistory> findByIntelRxIdAndSupplierPayment(
            String intelRxId, SupplierPayment supplierPayment
    );

}
