package com.pharmacy.intelrx.pharmacy.models.orgSettings;

import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bank_info")
public class BankInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "intel_rx_Id")
    private String intelRxId;
    private String bankName;
    private String accountName;
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "pharmacy_id",referencedColumnName = "id")
    private Pharmacy pharmacy;

    private LocalDateTime createdAt;
}
