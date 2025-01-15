package com.pharmacy.intelrx.pharmacy.models;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.RefillDuration;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "med_prescriptions")
public class MedPrescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    private String dosage;

    @ManyToOne
    @JoinColumn(name = "refill_duration_id", referencedColumnName = "id")
    private RefillDuration refillDuration;

    private boolean refill = false;

    private boolean smsAlert = false;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;//the current user selling

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "cart_item_id",referencedColumnName = "id")
    private CartItem cartItem;

    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id",referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;

    private LocalDateTime refillReminderDate; // The date when a new refill was sent

    private LocalDate prescriptionDate; // The date when the prescription was created
}
