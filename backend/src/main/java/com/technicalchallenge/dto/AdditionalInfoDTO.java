package com.technicalchallenge.dto;

import com.technicalchallenge.model.EntityType;
import com.technicalchallenge.model.FieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalInfoDTO {
    private Long id;
    private EntityType entityType;
    private Long entityId;
    private String fieldName;
    private String fieldValue;
    private FieldType fieldType;
    private Boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Integer version;
}
