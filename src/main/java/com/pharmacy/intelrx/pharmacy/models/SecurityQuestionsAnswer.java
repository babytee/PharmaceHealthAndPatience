package com.pharmacy.intelrx.pharmacy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.SecurityQuestion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "security_questions_answer")
public class SecurityQuestionsAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intel_rx_Id")
    private String intelRxId;

    @ManyToOne
    private SecurityQuestion securityQuestionOne;

    private String answerOne;

    @ManyToOne
    private SecurityQuestion securityQuestionTwo;

    private String answerTwo;

    @JsonIgnore
    @ManyToOne
    private User user; // The user who set the question

    private LocalDateTime createdDateAt;
}
