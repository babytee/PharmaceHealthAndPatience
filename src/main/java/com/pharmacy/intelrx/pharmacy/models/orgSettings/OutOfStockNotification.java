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
@Table(name = "out_of_stock_notification")
public class OutOfStockNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "intel_rx_Id")
    private String intelRxId;
    @Column(name = "check_notification")
    private boolean checkNotification;
    private int medication;
    private int grocery;
    private LocalDateTime createdAt;
}
