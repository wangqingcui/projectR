package com.bytz.modules.cms.payway.wallet.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 钱包状态枚举
 * Wallet Status Enumeration
 * 
 * 标识钱包当前的可用状态
 * 需求来源：票据钱包需求 - 钱包创建和管理
 */
public enum WalletStatus {
    
    /**
     * 停用 - 钱包不可用于支付
     */
    DISABLED("DISABLED", "停用", "Disabled"),
    
    /**
     * 启用 - 钱包可正常使用
     */
    ENABLED("ENABLED", "启用", "Enabled");
    
    @EnumValue
    private final String code;
    private final String description;
    private final String englishName;
    
    WalletStatus(String code, String description, String englishName) {
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
