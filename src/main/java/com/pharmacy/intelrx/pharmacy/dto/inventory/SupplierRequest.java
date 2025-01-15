package com.pharmacy.intelrx.pharmacy.dto.inventory;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierRequest {
    private Long id;
    private Pharmacy supplierPharmacy;
    private String intelRxId;
    private String name;
    private String phoneNumber;
    private User user;
}
