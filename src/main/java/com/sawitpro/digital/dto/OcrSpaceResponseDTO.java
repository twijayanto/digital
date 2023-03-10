package com.sawitpro.digital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrSpaceResponseDTO {

    @JsonProperty("ParsedResults")
    private List<ParsedResultsDTO> parsedResults;

    @JsonProperty("IsErroredOnProcessing")
    private boolean isErroredOnProcessing;
}
