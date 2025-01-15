package com.pharmacy.intelrx.pharmacy.models;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.BankInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pharmacies")
public class Pharmacy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intel_rx_Id")
    private String intelRxId;

    //@Column(name = "premises_Id")
    private String premisesId;

    private String pharmacistCategory;

    @Column(name = "pharmacy_name")
    private String pharmacyName;

    @Column(name = "premise_number")
    private String premiseNumber;

    @Column(name = "pharmacy_number")
    private String pharmacyNumber;

    @Column(name = "other_number")
    private String[] otherNumber;

    @Column(name = "pharmacy_owner")
    private String pharmacyOwner;

    @OneToOne(cascade = CascadeType.ALL)
    private ContactInfo contactInfo;

    @Column(name = "pharmacy_logo")
    private String pharmacyLogo;

    private String website;

    private String twitter;

    private String linkedIn;


    private String regBy = "Self Registered";

    private String subscriptionStatus = "Freemium";

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")//attach the owner
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    private List<BankInfo> bankInfoList;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
