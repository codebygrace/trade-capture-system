package com.technicalchallenge.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SETTLEMENT_INSTRUCTIONS")
public class SettlementInstructions extends AdditionalInfo {
}
