package com.sawitpro.digital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResultsDTO {

    @JsonProperty("ParsedText")
    private String parsedText;

    @JsonProperty("ErrorMessage")
    private String errorMessage;
}
