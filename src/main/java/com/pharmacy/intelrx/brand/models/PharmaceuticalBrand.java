package com.pharmacy.intelrx.brand.models;

import com.pharmacy.intelrx.auxilliary.models.User;
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
@Table(name = "pharmaceutical_brands")
public class PharmaceuticalBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intel_rx_Id")
    private String intelRxId;

    private String companyName;
    private String companyEmail;

    @OneToOne
    private User user;

    private LocalDateTime createdAt;
}
