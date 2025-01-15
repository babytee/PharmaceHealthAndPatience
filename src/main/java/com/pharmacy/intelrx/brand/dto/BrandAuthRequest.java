package com.pharmacy.intelrx.brand.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandAuthRequest {
    @NotNull(message = "Brand name is required.")
    @NotBlank(message = "Brand name is required.")
    private String brandName;

    @NotNull(message = "Company email is required.")
    @NotBlank(message = "Company email is required.")
    @Email(message = "Company email must be a valid email address.")
    private String companyEmail;

    @NotNull(message = "Password is required.")
    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    @NotNull(message = "Confirm password is required.")
    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
