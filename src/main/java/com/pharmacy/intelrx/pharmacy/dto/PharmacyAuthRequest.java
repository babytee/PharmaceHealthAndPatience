package com.pharmacy.intelrx.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmacyAuthRequest {
    private UserRequest userRequest;
    private PharmacistCertificationReqRes pharmacistCertificationReqRes;
    private ContactInfoReqRes contactInfoReqRes;
}
