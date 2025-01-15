package com.pharmacy.intelrx.auxilliary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlogCategoryResponse {
    private Long id;
    private String name;
    private String status;
    private String description;


    public BlogCategoryResponse(String status, String description) {
        this.status = status;
        this.description = description;
    }
}
