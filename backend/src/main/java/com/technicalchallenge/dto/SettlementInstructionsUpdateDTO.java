package com.technicalchallenge.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SettlementInstructionsUpdateDTO {

    @Size(min = 10, max = 500, message = "Settlement instructions must be between 10 and 500 character long")
    private String fieldValue;
}
