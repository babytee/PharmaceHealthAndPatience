package com.pharmacy.intelrx.pharmacy.models;

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
@Table(name = "transfer_employee")
public class TransferEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String employeeIntelRxId;

    //user
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;//transfer by

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id",referencedColumnName = "id")
    private Employee employee;

    //current  branch
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_branch_id", referencedColumnName = "id")
    private PharmacyBranch currentBranch;

    //new  branch
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "new_branch_id", referencedColumnName = "id")
    private PharmacyBranch newBranch;

    private LocalDateTime dateTransferred;
}
