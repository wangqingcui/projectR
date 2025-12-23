package com.bytz.modules.cms.payway.wallet.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 钱包交易状态枚举
 * Wallet Transaction Status Enumeration
 * 
 * 定义钱包交易的处理状态：成功或失败
 * 注：钱包交易不存在"处理中"状态，交易立即成功或失败
 */
public enum WalletTransactionStatus {
    
    /**
     * 成功 - 交易处理成功
     */
    SUCCESS("SUCCESS", "成功", "Success"),
    
    /**
     * 失败 - 交易处理失败
     */
    FAILED("FAILED", "失败", "Failed");
    
    @EnumValue
    private final String code;
    private final String description;
    private final String englishName;
    
    WalletTransactionStatus(String code, String description, String englishName) {
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
