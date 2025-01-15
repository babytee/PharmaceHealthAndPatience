package com.pharmacy.intelrx.marketPlace.models;

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
@Table(name = "order_bill_info")
public class OrderBillInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intelRxId;
    private String firstName;
    private String lastName;
    private String pharmacyName;
    private String country;
    private String state;
    private String city;
    private String address;
    private String zipCode;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;//the current user submitting order

    private LocalDateTime createdAt;
}
