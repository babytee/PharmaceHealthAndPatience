package com.pharmacy.intelrx.SmsByTermii;

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
public class TermiiSmsRequest {
    private String to;
    private String from;
    private String sms;
    private String type;
    private String channel;
    private String api_key;
}
