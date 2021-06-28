package com.simon.dto;

import com.simon.model.User;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class LoginResult extends User {

    private String token;

    private Date lastLoginTime;

    private String balance;

    private String bankNumber;

}
