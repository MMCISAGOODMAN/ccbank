package com.simon.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginDto {

    @ApiModelProperty("手机号")
    private String telephone;

    @ApiModelProperty("登录密码")
    private String password;

}
