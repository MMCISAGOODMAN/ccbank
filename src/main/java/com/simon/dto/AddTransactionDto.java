package com.simon.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AddTransactionDto {

    //转入方的银行账户ID
    private String importUserBankId;
    //转出金额
    private BigDecimal exportMoney;
    //备注
    private String remark;

    //交易类型  向他行账户转账 向本行账户转账  工资代发
    //private String transactionType;

    //交易渠道 手机银行 智能柜台
    private String transactionChannel;

    //转账日期
    private Date transactionDate;

}
