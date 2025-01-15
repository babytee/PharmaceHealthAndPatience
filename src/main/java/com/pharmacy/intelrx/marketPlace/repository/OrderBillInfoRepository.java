package com.pharmacy.intelrx.marketPlace.repository;

import com.pharmacy.intelrx.marketPlace.models.OrderBillInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBillInfoRepository extends JpaRepository<OrderBillInfo,Long> {
}
