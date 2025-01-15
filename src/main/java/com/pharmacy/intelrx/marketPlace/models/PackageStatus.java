package com.pharmacy.intelrx.marketPlace.models;

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
@Table(name = "package_status")
public class PackageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intelRxId;
    private String deliveryStatus;

    @ManyToOne
    private MarketPlaceCartItem cartItem;

    private LocalDateTime createdAt;
}
