package com.simon.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TransactionDto {


    //查询转账的开始时间
    private Date transactionDate;

    //收款账户名 账号 手机号 留言
    private String keyWords;
}
