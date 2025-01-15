package com.pharmacy.intelrx.pharmacy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.LocalDateTime;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transfer_inventory")
public class TransferInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intelRxId;
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "inventory_id",referencedColumnName = "id")
    @JsonIgnore
    private Inventory inventory;

    @ManyToOne
    @JoinColumn(name = "transfer_from_id",referencedColumnName = "id")
    @JsonIgnore
    private PharmacyBranch transferFrom;

    @ManyToOne
    @JoinColumn(name = "transfer_to_id",referencedColumnName = "id")
    @JsonIgnore
    private PharmacyBranch transferTo;

    @ManyToOne
    @JoinColumn(name = "transfer_officer_id", referencedColumnName = "id")
    @JsonIgnore
    private User transferOfficer;//officer who initiated the transfer

    @ManyToOne
    @JoinColumn(name = "received_by_id", referencedColumnName = "id")
    @JsonIgnore
    private Employee receivedBy;//Employee to oversee pick up and receipt of inventory by new branch

    private LocalDate dateSent;

    private LocalDate receivedTime;

    private String status;//Pending, Cancelled,Received

}
