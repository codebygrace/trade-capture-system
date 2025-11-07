package com.technicalchallenge.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TradeFilterDTO {

    private String counterpartyName;

    private String bookName;

    private String trader;

    private String status;

    private LocalDate tradeDateStart;

    private LocalDate tradeDateEnd;


}
