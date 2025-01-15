package com.pharmacy.intelrx.marketPlace;

public interface WholeSalesInventoryProjection {
    Long getId();
    String getItemName();
    String getIntelRxId();
    String getInventoryType();

    // New fields
    int getWholeSaleQuantity();
    double getWholeSalePrice();

    int getExpDay();
    int getExpMonth();
    int getExpYear();

    // Brand, BrandForm, BrandClass projections
    BrandProjection getBrand();
    BrandFormProjection getBrandForm();
    BrandClassProjection getBrandClass();
    SizeProjection getSize();
}
