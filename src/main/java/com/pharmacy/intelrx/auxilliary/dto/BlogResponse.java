package com.pharmacy.intelrx.auxilliary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class BlogResponse {

    private Long id;
    private String title;
    private String banner;
    private String banner_url;
    private String content;
    private String estimatedReadThroughTime;
    private Boolean pinToTop;
    private Long blog_category_id;
    private Object category;
    private Object postedBy;
    private Long admin_id;
    private String status;
    private String description;
    private String createdAt;

    public BlogResponse(String status, String description) {
        this.status = status;
        this.description = description;
    }

    public BlogResponse(long id, String status, String description) {
        this.id = id;
        this.status = status;
        this.description = description;
    }

}
