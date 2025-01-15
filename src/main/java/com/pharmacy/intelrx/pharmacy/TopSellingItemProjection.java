package com.pharmacy.intelrx.pharmacy;

public interface TopSellingItemProjection {
    String getItemName();
    double getTotalSalesAmount();
    double getPreviousSalesAmount();
}
