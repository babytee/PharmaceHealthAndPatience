package com.pharmacy.intelrx.pharmacy.models.orgSettings;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.RefillDuration;
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
@Table(name = "drug_expiration_notifications")
public class DrugExpirationNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "intel_rx_Id")
    private String intelRxId;
    @Column(name = "check_notification")
    private boolean checkNotification;

    @ManyToOne
    @JoinColumn(name = "frequency",referencedColumnName = "id")
    private RefillDuration frequency;

    private LocalDateTime createdAt;
}
