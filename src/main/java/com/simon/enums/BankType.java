package com.simon.enums;

import org.springframework.util.StringUtils;


public enum BankType {
    ZGJSYH("1", "中国建设银行"),
    ZGYH("2", "中国银行"),
    ZSYH("3", "招商银行"),
    ZGGSYH("4", "中国工商银行"),
    ZGNYYH("5", "中国农业银行");
    private String code;

    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private BankType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BankType get(String code) {
        if (StringUtils.hasLength(code)) {
            for (BankType element : BankType.values()) {
                if (element.getCode().equals(code)) {
                    return element;
                }
            }
        }
        return null;
    }
}
