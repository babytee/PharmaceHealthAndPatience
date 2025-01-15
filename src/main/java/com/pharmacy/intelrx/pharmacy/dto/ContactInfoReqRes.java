package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactInfoReqRes {
    private Long id;
    private String country;
    private String state;
    private String city;
    private String lga;
    private String zipCode;
    private String streetAddress;
    private String intelRxId;
}
