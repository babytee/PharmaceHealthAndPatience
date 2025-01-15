package com.pharmacy.intelrx.pharmacy.models.orgSettings;

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
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "intel_rx_Id")
    private String intelRxId;
    private boolean discountSwitch=false;
    private double medicationPercentage;
    private double groceryPercentage;
    private LocalDateTime createdAt;
}
