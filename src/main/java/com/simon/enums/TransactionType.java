package com.simon.enums;

import org.springframework.util.StringUtils;

/**
 * 交易类型
 */
public enum TransactionType {

    XTHZHZZ("1", "向他行账户转账"),
    XBHZHZZ("2", "向本行账户转账"),
    GZDF("3", "工资代发");
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

    private TransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TransactionType get(String code) {
        if (StringUtils.hasLength(code)) {
            for (TransactionType element : TransactionType.values()) {
                if (element.getCode().equals(code)) {
                    return element;
                }
            }
        }
        return null;
    }
}
