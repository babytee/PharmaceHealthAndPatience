package com.pharmacy.intelrx.pharmacy.models;

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
@Table(name = "salary_payments")
public class SalaryPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intelRxId;

    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id", referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;// null means HeadQuarter

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    private String payPeriod;

    private double amount;

    private String status;//Paid,OverDue

    private String paymentReceipt;

    private LocalDateTime createdAt;

}
