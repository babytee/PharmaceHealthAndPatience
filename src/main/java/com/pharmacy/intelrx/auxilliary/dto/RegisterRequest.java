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
public class RegisterRequest {
    private Long id;
    private String firstname;
    private String lastname;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirm_password;
    private String status;
    private String description;

    public RegisterRequest(String status, String description) {
        this.status = status;
        this.description = description;
    }

    public RegisterRequest(Long id,String email,String firstname, String lastname) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
    }
}
