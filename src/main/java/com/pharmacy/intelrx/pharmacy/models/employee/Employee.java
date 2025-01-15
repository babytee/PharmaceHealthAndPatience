package com.pharmacy.intelrx.pharmacy.models.employee;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
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
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_type")
    private String employeeType;//CONTRACT or FULL-TIME

    @Column(name = "employee_intel_rx_id")
    private String employeeIntelRxId;


    //user
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean status = false;


    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private ContactInfo contactInfo;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private JobInformation jobInformation;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private CompensationDetail compensationDetail;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Education education;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Legal legal;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Benefit benefit;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private ExtraInformation extraInformation;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<BranchEmployee> branchEmployee;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<EmployeeDocument> employeeDocument;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
