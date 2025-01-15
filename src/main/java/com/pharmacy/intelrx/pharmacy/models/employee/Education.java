package com.pharmacy.intelrx.pharmacy.models.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "educations")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @OneToMany(mappedBy = "education", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EducationDegree> educationDegrees;

    @OneToMany(mappedBy = "education", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkHistory> workHistory;

//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "education_degree_id", referencedColumnName = "id")
//    private EducationDegree educationDegree;
//
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "work_history_id", referencedColumnName = "id")
//    private WorkHistory workHistory;

    private String[] license;

}
