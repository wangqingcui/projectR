package com.bytz.modules.cms.payway.wallet.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 钱包交易类型枚举
 * Wallet Transaction Type Enumeration
 * 
 * 定义钱包交易的类型：充值、支付、退款
 */
public enum WalletTransactionType {
    
    /**
     * 充值 - 向钱包充值资金
     */
    RECHARGE("RECHARGE", "充值", "Recharge"),
    
    /**
     * 支付 - 使用钱包余额支付
     */
    PAYMENT("PAYMENT", "支付", "Payment"),
    
    /**
     * 退款 - 退款到钱包
     */
    REFUND("REFUND", "退款", "Refund");
    
    @EnumValue
    private final String code;
    private final String description;
    private final String englishName;
    
    WalletTransactionType(String code, String description, String englishName) {
        this.code = code;
        this.description = description;
        this.englishName = englishName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEnglishName() {
        return englishName;
    }
}
