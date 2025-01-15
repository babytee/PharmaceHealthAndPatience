package com.pharmacy.intelrx.pharmacy.models.employee;

import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "assign_shift")
public class AssignShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intel_rx_id")
    private String intelRxId;

    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id", referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;// null means HeadQuarter

    @ManyToOne
    @JoinColumn(name = "assigned_member_id", referencedColumnName = "id")
    private Employee assignedMember;

    @ManyToOne
    @JoinColumn(name = "job_title_id", referencedColumnName = "id")
    private JobTitle jobTitle;

    private LocalDate startDate;

    private String startTime;

    private String endTime;

    private String shiftTime;//1 hour, 2 hours, 3 and up tp 8 hours

    private String AssignedColor;

    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

}
