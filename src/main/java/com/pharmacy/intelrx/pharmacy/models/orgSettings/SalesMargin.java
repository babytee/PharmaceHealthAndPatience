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
@Table(name = "sales_margins")
public class SalesMargin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "intel_rx_Id")
    private String intelRxId;
    private double medicationMargin;
    private double groceryMargin;
    private LocalDateTime createdAt;
}
