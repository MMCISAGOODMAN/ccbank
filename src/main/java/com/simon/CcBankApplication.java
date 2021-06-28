package com.simon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.simon.*.mapper")
public class CcBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcBankApplication.class, args);
    }
}
