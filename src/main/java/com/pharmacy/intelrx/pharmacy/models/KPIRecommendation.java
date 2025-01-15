package com.pharmacy.intelrx.pharmacy.models;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.KPIType;
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
@Table(name = "kpi_recommendations")
public class KPIRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    @ManyToOne
    private KPIType kpiType;

    @Column(name = "recommendation_msg")
    private String recommendationMsg;

    private String docs;

    private LocalDateTime createdAt;
}
