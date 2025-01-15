package com.pharmacy.intelrx.auxilliary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlogRequest {
    private Long id;
    private String title;
    private MultipartFile banner;
    private String banner_url;
    private String content;
    private String status;
    private String estimatedReadThroughTime;
    private Boolean pinToTop;
    private Long blog_category_id;
    private Long admin_id;
}
