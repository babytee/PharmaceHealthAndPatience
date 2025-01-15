package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiscellaneousExpenditureDTO {
    private LocalDateTime date;
    private String expenseName;
    private String amount;

    public MiscellaneousExpenditureDTO(String s, String expenseName, String amount) {
    }
}
