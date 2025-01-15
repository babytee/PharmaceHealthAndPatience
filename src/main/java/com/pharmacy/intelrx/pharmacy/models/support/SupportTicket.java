package com.pharmacy.intelrx.pharmacy.models.support;

import com.pharmacy.intelrx.admin.models.Admin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intelRxId;
    private String emailAddress;
    private String subject;
    private String respondMsg;

    @ManyToOne
    @JoinColumn(name = "support_type_id", referencedColumnName = "id")
    private SupportType supportType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "attached_file")
    private String attachedFile;

    private String ticketStatus = "Pending";

    @ManyToOne
    private Admin resolvedBy;

    private LocalDateTime createdAt;

    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;

    @PrePersist
    public void prePersist() {
        this.ticketNumber = generateUniqueTicketNumber();
        this.createdAt = LocalDateTime.now();
    }

    private String generateUniqueTicketNumber() {
        return "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
