package com.technicalchallenge.mapper;

import com.technicalchallenge.dto.AdditionalInfoDTO;
import com.technicalchallenge.dto.SettlementInstructionsUpdateDTO;
import com.technicalchallenge.model.EntityType;
import com.technicalchallenge.model.FieldType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettlementInstructionsMapper {

    @Autowired
    private ModelMapper mapper;

    public SettlementInstructionsUpdateDTO toRequest(AdditionalInfoDTO additionalInfoDTO) {
        return mapper.map(additionalInfoDTO, SettlementInstructionsUpdateDTO.class);
    }

    public AdditionalInfoDTO toDto(SettlementInstructionsUpdateDTO request) {
        AdditionalInfoDTO mappedRequest = mapper.map(request, AdditionalInfoDTO.class);
        mappedRequest.setEntityType(EntityType.TRADE);
        mappedRequest.setFieldName("SETTLEMENT_INSTRUCTIONS");
        mappedRequest.setFieldType(FieldType.STRING);
        return mappedRequest;
    }
}
