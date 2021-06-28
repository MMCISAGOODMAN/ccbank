package com.simon.enums;

import org.springframework.util.StringUtils;

public enum TransactionChannel {
    SJYH("1", "手机银行"),
    ZNGT("2", "智能柜台");
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

    private TransactionChannel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TransactionChannel get(String code) {
        if (StringUtils.hasLength(code)) {
            for (TransactionChannel element : TransactionChannel.values()) {
                if (element.getCode().equals(code)) {
                    return element;
                }
            }
        }
        return null;
    }
}
