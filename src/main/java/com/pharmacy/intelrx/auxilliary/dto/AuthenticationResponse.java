package com.pharmacy.intelrx.auxilliary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    private String status;
    private String description;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private Object data;
    private Object userDetails;
    private Object jobInfo;
    private Object pharmacyInfo;
    private String firebaseToken;

    public AuthenticationResponse(String status, String description, Object data) {
        this.status = status;
        this.description = description;
        this.data = data;
    }
//
//    public AuthenticationResponse(String status, String description) {
//        this.status = status;
//        this.description = description;
//    }
//
//    public AuthenticationResponse(String status,String description, String accessToken, String refreshToken,Object data) {
//        this.status = status;
//        this.description = description;
//        this.accessToken = accessToken;
//        this.refreshToken = refreshToken;
//        this.data = data;
//    }
}
