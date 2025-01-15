package com.pharmacy.intelrx.pharmacy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String gender;
    private String phoneNumber;
    private String intelRxId;

    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id",referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "patient")
    @JsonIgnore
    private List<Order> orders;

    private String address;

    private String dob;

    private String email;

    private String otp;

    private LocalDateTime otpCreatedAt;

    private LocalDateTime createdAt;

}
