package com.pharmacy.intelrx.auxilliary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResponse {
    private String status;
    private String message;
    private List<Object> data;

    public CustomResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public CustomResponse(String status, List<Object> data) {
        this.status = status;
        this.data = data;
    }

}
