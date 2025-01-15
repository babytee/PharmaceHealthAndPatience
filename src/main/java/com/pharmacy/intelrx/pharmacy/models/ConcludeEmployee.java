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
@Table(name = "conclude_employee")
public class ConcludeEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String employeeIntelRxId;

    //user
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;//concluded by

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id",referencedColumnName = "id")
    private Employee employee;

    private String concludeType;//TERMINATED,DELETED,SUSPENDED,END CONTRACT

    private String reasons;

    private LocalDateTime dateTerminated;
}
