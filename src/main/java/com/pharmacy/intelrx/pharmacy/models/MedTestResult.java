package com.pharmacy.intelrx.pharmacy.models;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.MedTestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "med_test_result")
public class MedTestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    @ManyToOne
    private MedTestType testType;

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private User user;//added by

    private String testResult;

    private String testNotes;

    private LocalDateTime localDateTime;


}
