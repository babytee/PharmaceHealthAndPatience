package com.pharmacy.intelrx.newsLetterModule.models;

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
@Table(name = "news_letters")
public class NewsLetter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String newsType;//Email,Sms
    private String sendTo;//Wholesalers,Retailers
    private String scheduleTimeType;// Immediately, Later
    private LocalDateTime scheduleTime;
    private String newsLetterTitle;
    private Long adminId;
    private String newsStatus;// LIVE, DRAFT

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

}

