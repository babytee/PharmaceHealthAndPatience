package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.orgSettings.BankInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankInfoRepository extends JpaRepository<BankInfo,Long> {
    List<BankInfo> findByIntelRxId(String intelRxId);
    Optional<BankInfo>findByIdAndIntelRxId(Long id,String intelRxId);
}
