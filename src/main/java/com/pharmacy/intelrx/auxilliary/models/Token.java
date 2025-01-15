package com.pharmacy.intelrx.auxilliary.models;

import com.pharmacy.intelrx.admin.models.Admin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

        @Id
        @GeneratedValue
        private Integer id;

        @Column(unique = true)
        private String token;

        @Enumerated(EnumType.STRING)
        private TokenType tokenType = TokenType.BEARER;

        private boolean expired;

        private boolean revoked;


        @ManyToOne
        @JoinColumn(name = "admin_id")
        private Admin admin;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

    }
