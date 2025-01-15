package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.LandingPages.InventoryProjection;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import com.pharmacy.intelrx.marketPlace.WholeSalesInventoryProjection;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByIdAndIntelRxId(Long inventoryId, String intelRxId);


    void deleteByIdAndIntelRxId(Long inventoryId, String intelRxId);

    @Query("SELECT DISTINCT i.id AS id, i.itemName, i.inventoryType AS itemName FROM Inventory i")
    List<InventoryProjection> findAllUniqueInventoryNames();

    @Query("SELECT DISTINCT i.id AS id, i.itemName, i.inventoryType AS itemName FROM Inventory i WHERE i.inventoryType = :inventoryType")
    List<InventoryProjection> findAllUniqueInventoryNamesByInventoryType(@Param("inventoryType") String inventoryType);

    @Query("SELECT DISTINCT i.id AS id, i.itemName, i.inventoryType AS itemName FROM Inventory i WHERE " +
            "i.id = :id AND i.inventoryType = :inventoryType")
    Optional<InventoryProjection> findUniqueInventoryNamesById
            (@Param("id") Long id, @Param("inventoryType") String inventoryType);

    Optional<Inventory> findByIntelRxIdAndInventoryTypeAndItemNameAndBrandAndBrandFormAndSize(
            String intelRxId,
            String inventoryType,
            String name,
            Brand brand,
            BrandForm brandForm,
            Size size);


    List<Inventory> findAllByIntelRxIdAndInventoryTypeAndStatus(String intelRxId, String inventoryType, boolean status);

    List<Inventory> findAllByIntelRxIdAndStatus(String intelRxId, boolean status);

    List<Inventory> findAllByIntelRxIdAndUserAndStatus(String intelRxId, User user, boolean status);

    Optional<Inventory> findByItemNameAndInventoryType(String name, String inventoryType);

    Optional<Inventory> findByItemNameAndIntelRxId(String name, String inventoryType);

    List<Inventory> findAllByStatusAndUserId(boolean status, Long userId);

    List<Inventory> findAllByInvoiceRefNumber(String invoiceRefNumber);

    Page<Inventory> findAllByInvoiceRefNumberAndIntelRxId(
            String invoiceRefNumber,
            String intelRxId,
            Pageable pageable);

    List<Inventory> findAllByInventoryTypeAndInvoiceRefNumber(String inventoryType, String invoiceRefNumber);

    List<Inventory> findAllByInventoryTypeAndSupplier(String inventoryType, Supplier supplier);


    @Query("SELECT i FROM Inventory i WHERE " +
            "(:brandId is null or i.brand.id = :brandId) and " +
            "(:brandClassId is null or i.brandClass.id = :brandClassId) and " +
            "(:brandFormId is null or i.brandForm.id = :brandFormId) and " +
            "(:sizeId is null or i.size.id = :sizeId) and " +
            "(:intelRxId is null or i.intelRxId = :intelRxId) and " +
            "(:inventoryType is null or i.inventoryType = :inventoryType) and " +
            "(i.deleteStatus is null or i.deleteStatus = false) and " + // Allow null or false
            "(:status is null or i.status = :status) and " +
            "(:poison is null or i.poison = :poison) and " +
            "(:searchText is null or lower(i.barCodeNumber) like lower(concat('%', :searchText, '%')) or lower(i.itemName) like lower(concat('%', :searchText, '%')))")
    Page<Inventory> findByFilter(
            @Param("brandId") Long brandId,
            @Param("brandClassId") Long brandClassId,
            @Param("brandFormId") Long brandFormId,
            @Param("sizeId") Long sizeId,
            @Param("intelRxId") String intelRxId,
            @Param("inventoryType") String inventoryType,
            @Param("status") Boolean status,
            @Param("poison") Boolean poison,
            @Param("searchText") String searchText,
            Pageable pageable
    );

    @Query("SELECT ti.inventory FROM TransferInventory ti " +
            "LEFT JOIN ti.inventory i " +
            "LEFT JOIN ti.transferFrom tf " +
            "LEFT JOIN ti.transferTo tt " +
            "WHERE " +
            "(:brandId is null or i.brand.id = :brandId) and " +
            "(:brandClassId is null or i.brandClass.id = :brandClassId) and " +
            "(:brandFormId is null or i.brandForm.id = :brandFormId) and " +
            "(:sizeId is null or i.size.id = :sizeId) and " +
            "(:transferStatus is null or ti.status = :transferStatus) and " +
            "(:intelRxId is null or (i.intelRxId = :intelRxId or ti.intelRxId = :intelRxId)) and " +
            "(:inventoryType is null or i.inventoryType = :inventoryType) and " +
            "(i.deleteStatus is null or i.deleteStatus = false) and " + // Allow null or false
            "(:status is null or i.status = :status) and " +
            "(:poison is null or i.poison = :poison) and " +
            "(:searchText is null or lower(i.barCodeNumber) like lower(concat('%', :searchText, '%')) or lower(i.itemName) like lower(concat('%', :searchText, '%'))) and " +
            "(:branchId is null or tf.id = :branchId or tt.id = :branchId)")
    Page<Inventory> findByBranchFilter(
            @Param("brandId") Long brandId,
            @Param("brandClassId") Long brandClassId,
            @Param("brandFormId") Long brandFormId,
            @Param("sizeId") Long sizeId,
            @Param("transferStatus") String transferStatus,
            @Param("intelRxId") String intelRxId,
            @Param("inventoryType") String inventoryType,
            @Param("status") Boolean status,
            @Param("poison") Boolean poison,
            @Param("searchText") String searchText,
            @Param("branchId") Long branchId,
            Pageable pageable
    );

    @Query("SELECT ti.inventory FROM TransferInventory ti " +
            "LEFT JOIN ti.inventory i " +
            "LEFT JOIN ti.transferFrom tf " +
            "LEFT JOIN ti.transferTo tt " +
            "WHERE " +
            "(:intelRxId is null or (i.intelRxId = :intelRxId or ti.intelRxId = :intelRxId)) and " +
            "(:inventoryType is null or i.inventoryType = :inventoryType) and " +
            "(i.deleteStatus is null or i.deleteStatus = false) and " + // Allow null or false
            "(:status is null or i.status = :status) and " +
            "(:branchId is null or tf.id = :branchId or tt.id = :branchId)")
    List<Inventory> findAllByTransferBranch(
            @Param("intelRxId") String intelRxId,
            @Param("inventoryType") String inventoryType,
            @Param("status") Boolean status,
            @Param("branchId") Long branchId
    );

    List<Inventory> findAllByIntelRxIdAndCreatedAtBetween(String intelRxId, LocalDateTime startDate, LocalDateTime endDate);


    List<Inventory> findAllByIntelRxIdAndCreatedAtBefore(String intelRxId, LocalDateTime period);


    @Query("SELECT i FROM Inventory i WHERE " +
            "(:brandName is null or i.brand.name = :brandName) and " +
            "(:brandClassName is null or i.brandClass.name = :brandClassName) and " +
            "(:brandFormName is null or i.brandForm.name = :brandFormName) and " +
            "(:inventoryType is null or i.inventoryType = :inventoryType) and " +
            "(i.deleteStatus is null or i.deleteStatus = false) and " + // Allow null or false
            "(:status is null or i.status = :status) and " +
            "(:poison is null or i.poison = :poison) and " +
            "(:searchText is null or lower(i.barCodeNumber) like lower(concat('%', :searchText, '%')) or lower(i.itemName) like lower(concat('%', :searchText, '%')))")
    List<Inventory> findByFilter(
            @Param("brandName") Long brandName,
            @Param("brandClassName") Long brandClassName,
            @Param("brandFormName") Long brandFormName,
            @Param("inventoryType") String inventoryType,
            @Param("status") Boolean status,
            @Param("poison") Boolean poison,
            @Param("searchText") String searchText
    );


    @Query("SELECT SUM(i.costPrice * i.quantity) FROM Inventory i " +
            "WHERE i.createdAt <= :startDate " +
            "AND i.intelRxId = :intelRxId ")
    Double getBeginningInventoryValue(@Param("startDate") LocalDateTime startDate,
                                      @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(i.costPrice * i.quantity) FROM Inventory i " +
            "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
            "AND i.intelRxId = :intelRxId ")
    Double getPurchasesValueDuringPeriod(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(i.costPrice * i.quantity) FROM Inventory i " +
            "WHERE i.createdAt <= :endDate " +
            "AND i.intelRxId = :intelRxId ")
    Double getEndingInventoryValue(@Param("endDate") LocalDateTime endDate,
                                   @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(i.costPrice * i.quantity) FROM Inventory i " +
            "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
            "AND i.intelRxId = :intelRxId ")
    Double getTotalInventoryValue(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  @Param("intelRxId") String intelRxId);

    @Query("SELECT AVG(i.costPrice * i.quantity) FROM Inventory i " +
            "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
            "AND i.intelRxId = :intelRxId ")
    Double getAverageInventoryValue(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.intelRxId = :intelRxId")
    Integer getRecordedInventoryQuantity(@Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.intelRxId = :intelRxId AND i.status = true")
    Integer getActualInventoryQuantity(@Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.createdAt <= :startDate AND i.intelRxId = :intelRxId")
    Integer getBeginningInventoryQuantity(@Param("startDate") LocalDateTime startDate,
                                          @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i " +
            "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
            "AND i.intelRxId = :intelRxId ")
    Integer getPurchasesQuantityDuringPeriod(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             @Param("intelRxId") String intelRxId);


    @Query("SELECT i FROM Inventory i WHERE (" +
            "(LOWER(i.itemName) LIKE LOWER(CONCAT('%', :searchParam, '%')) " +
            "OR LOWER(i.brand.name) LIKE LOWER(CONCAT('%', :searchParam, '%'))) " +
            "AND (:brandName IS NULL OR LOWER(i.brand.name) LIKE LOWER(CONCAT('%', :brandName, '%'))) " +
            ") AND i.wholeSalePrice > 0 " +
            "AND i.wholeSaleQuantity > 0 " +
            "AND (i.expYear > YEAR(CURRENT_DATE) " +
            "OR (i.expYear = YEAR(CURRENT_DATE) AND i.expMonth > MONTH(CURRENT_DATE)) " +
            "OR (i.expYear = YEAR(CURRENT_DATE) AND i.expMonth = MONTH(CURRENT_DATE) AND i.expDay >= DAY(CURRENT_DATE)))")
    Page<WholeSalesInventoryProjection> searchByItemNameOrBrandName(@Param("searchParam") String searchParam,
                                                                    @Param("brandName") String brandName,
                                                                    Pageable pageable);

}
