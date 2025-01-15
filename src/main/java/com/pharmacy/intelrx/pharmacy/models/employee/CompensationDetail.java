package com.pharmacy.intelrx.pharmacy.models.employee;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.PaymentFrequency;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.SalaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compensation_details")
public class CompensationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "salary_type_id", referencedColumnName = "id")
    private SalaryType salaryType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_frequency_id", referencedColumnName = "id")
    private PaymentFrequency paymentFrequency;

    private String salary;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    private String salaryStatus;//Paid,Overdue,Pending

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
