package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByPremiseNumber(String premiseNumber);

    List<Pharmacy> findAllByIntelRxId(String intelRxId);

    Optional<Pharmacy> findByIntelRxId(String intelRxId);

    Optional<Pharmacy> findByIntelRxIdAndUserId(String intelRxId, Long userId);

    Optional<Pharmacy> findByUserId(Long userId);

    Optional<Pharmacy> findByPremisesId(String premisesId);

    @Query("SELECT p FROM Pharmacy p " +
            "LEFT JOIN p.contactInfo c " +
            "WHERE " +
            "((LOWER(p.pharmacyName) LIKE LOWER(CONCAT('%', :filter, '%'))) OR " +
            "(LOWER(p.intelRxId) LIKE LOWER(CONCAT('%', :filter, '%'))) OR " +
            "(LOWER(p.premiseNumber) LIKE LOWER(CONCAT('%', :filter, '%'))) OR " +
            "(LOWER(p.pharmacyOwner) LIKE LOWER(CONCAT('%', :filter, '%'))) OR " +
            "(LOWER(c.city) LIKE LOWER(CONCAT('%', :filter, '%'))) OR " +
            "(LOWER(c.state) LIKE LOWER(CONCAT('%', :filter, '%'))) OR " +
            "(c.id IS NULL)) OR " +
            "(:filter IS NULL)")
    Page<Pharmacy> findByFilter(
            @Param("filter") String filter,
            Pageable pageable
    );


    List<Pharmacy> findAllByRegBy(String regBy);

    @Query("SELECT SUM(SIZE(p.contactInfo.state)) FROM Pharmacy p")
    Integer getTotalStateCoverages();

    @Query("SELECT p.contactInfo.state " +
            "FROM Pharmacy p " +
            "GROUP BY p.contactInfo.state " +
            "ORDER BY COUNT(p.contactInfo.state) DESC")
    List<String> findTopStatesOrderedByCount();

    List<Pharmacy> findDistinctSubscriptionStatusBySubscriptionStatusIsNotNullAndSubscriptionStatusNot(String subscriptionStatus);


    @Query("SELECT COUNT(p) FROM Pharmacy p WHERE MONTH(p.createdAt) = MONTH(CURRENT_DATE()) AND YEAR(p.createdAt) = YEAR(CURRENT_DATE())")
    int countPharmaciesRegisteredThisMonth();

    @Query("SELECT COUNT(p) FROM Pharmacy p WHERE MONTH(p.createdAt) = MONTH(CURRENT_DATE()) - 1 AND YEAR(p.createdAt) = YEAR(CURRENT_DATE())")
    int countPharmaciesRegisteredLastMonth();


}
