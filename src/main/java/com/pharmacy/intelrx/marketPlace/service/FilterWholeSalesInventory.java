package com.pharmacy.intelrx.marketPlace.service;

import com.pharmacy.intelrx.brand.models.Size;
import com.pharmacy.intelrx.marketPlace.*;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchesResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.*;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FilterWholeSalesInventory {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapping pharmacyMapping;

    public InventoryResponse mapToAddedInventoryResponse(WholeSalesInventoryProjection inventory) {
        InventoryResponse inventoryResponse = new InventoryResponse();

        inventoryResponse.setId(inventory.getId());
        inventoryResponse.setInventoryType(inventory.getInventoryType());

        // Construct MedName based on the inventory type
        String medName = inventory.getItemName();
        if ("MEDICATION".equals(inventory.getInventoryType())) {
            medName = inventory.getBrand().getName() + " - " + inventory.getItemName();
        }

        inventoryResponse.setItemName(medName);
        inventoryResponse.setGenericName(inventory.getItemName());
        inventoryResponse.setIntelRxId(inventory.getIntelRxId());

        // Map related entities like brand, brandForm, brandClass
        inventoryResponse.setBrand(mapToBrand(inventory.getBrand()));
        inventoryResponse.setBrandForm(mapToBrandForm(inventory.getBrandForm()));
        inventoryResponse.setBrandClass(mapToBrandClass(inventory.getBrandClass()));
        inventoryResponse.setMedicationSize(mapToMedicationSize(inventory.getSize()));

        inventoryResponse.setWholeSalePrice(inventory.getWholeSalePrice());
        inventoryResponse.setWholeSaleQuantity(inventory.getWholeSaleQuantity());
        inventoryResponse.setExpDay(inventory.getExpDay());
        inventoryResponse.setExpMonth(inventory.getExpMonth());
        inventoryResponse.setExpYear(inventory.getExpYear());

        // Fetch supplier details
        var pharmacy = pharmacyRepository.findByIntelRxId(inventory.getIntelRxId()).orElse(null);
        if (pharmacy != null) {
            PharmacyBranchesResponse response = pharmacyMapping.mapToPharmacyHeadQuarters(pharmacy);
            inventoryResponse.setSupplier(response);
        }

        return inventoryResponse;
    }


    public BrandReqRes mapToBrand(BrandProjection brand) {
        BrandReqRes brandReqRes = new BrandReqRes();
        if (brand != null) {
            brandReqRes.setId(brand.getId());
            brandReqRes.setName(brand.getName());
        }
        return brandReqRes;
    }

    public BrandClassReqRes mapToBrandClass(BrandClassProjection brandClass) {
        BrandClassReqRes brandClassReqRes = new BrandClassReqRes();
        if (brandClass != null) {
            brandClassReqRes.setId(brandClass.getId());
            brandClassReqRes.setName(brandClass.getName());
        }

        return brandClassReqRes;
    }

    public BrandFormReqRes mapToBrandForm(BrandFormProjection brandForm) {

        BrandFormReqRes brandFormReqRes = new BrandFormReqRes();
        if (brandForm != null) {
            brandFormReqRes.setId(brandForm.getId());
            brandFormReqRes.setName(brandForm.getName());
        }

        return brandFormReqRes;
    }

    public SizeReqRes mapToMedicationSize(SizeProjection size) {
        SizeReqRes sizeReqRes = new SizeReqRes();
        sizeReqRes.setId(size.getId());
        sizeReqRes.setName(size.getName());

        return sizeReqRes;
    }



}
