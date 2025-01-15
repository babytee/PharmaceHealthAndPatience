package com.pharmacy.intelrx.pharmacy.models.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "extra_information")
public class ExtraInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @Column(name = "preferred_nickname")
    private String preferredNickname;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_number")
    private String emergencyContactNumber;

    @Column(name = "relationship_with_emergency")
    private String relationshipWithEmergency;

    @Column(name = "referee_name")
    private String refereeName;

    @Column(name = "referee_number")
    private String refereeNumber;

    @Column(name = "relationship_with_referee")
    private String relationshipWithReferee;

    @Column(name = "disability_status")
    private String disabilityStatus;

    private String[] language;


}
