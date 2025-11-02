package com.technicalchallenge.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SettlementInstructionsUpdateDTO {

    private Long id;
    private Long entityId;

    @Size(min = 10, max = 500, message = "Settlement instructions must be between 10 and 500 character long")
    private String fieldValue;

    private int version;
    private LocalDateTime createdDate;
}
