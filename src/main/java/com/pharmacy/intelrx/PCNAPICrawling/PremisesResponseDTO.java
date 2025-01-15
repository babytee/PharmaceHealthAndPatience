package com.pharmacy.intelrx.PCNAPICrawling;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PremisesResponseDTO {
    @JsonProperty("Data")
    private List<PremisesDataDTO> data;

    //@JsonProperty("Total")
    private int total;

    //@JsonProperty("AggregateResults")
    private Object aggregateResults; // Adjust type based on actual data structure, if any

    //@JsonProperty("Errors")
    private Object errors; // Adjust type based on actual data structure, if any
}
