package com.pharmacy.intelrx.auxilliary.models;

import com.pharmacy.intelrx.admin.models.Admin;
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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blogs")
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String banner;
    private String banner_url;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    private String estimatedReadThroughTime;

    @Column(name = "pin_to_top", nullable = false, columnDefinition = "boolean default false")
    private boolean pinToTop=false;

    private String status;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "blog_category_id", referencedColumnName = "id")
    private BlogCategory blogCategory;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "admin_id", referencedColumnName = "id")
    private Admin admin;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
