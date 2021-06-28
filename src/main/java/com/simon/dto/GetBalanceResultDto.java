package com.simon.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetBalanceResultDto {

    private String bankNumber;

    private String balance;
}
