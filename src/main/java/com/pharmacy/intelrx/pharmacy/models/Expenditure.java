package com.pharmacy.intelrx.pharmacy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
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
@Table(name = "expenditure")
public class Expenditure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intelRxId;
    private String expenseName;
    private double amountSpent;

    @Column(name = "exp_day")
    private int expDay;

    @Column(name = "exp_month")
    private int expMonth;

    @Column(name = "exp_year")
    private int expYear;

    @ManyToOne
    @JoinColumn(name = "added_by_id", referencedColumnName = "id")
    @JsonIgnore
    private User addedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by_id", referencedColumnName = "id")
    @JsonIgnore
    private User approvedBy;

    @ManyToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    @JsonIgnore
    private PharmacyBranch branch;

    private String expenditureType = "Miscellaneous";//Salaries, Miscellaneous

    @Column(name = "approved", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean approved;

    private LocalDateTime createdAt;

}
