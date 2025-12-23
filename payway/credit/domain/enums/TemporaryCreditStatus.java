package com.bytz.modules.cms.payway.credit.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 临时授信状态枚举
 * Temporary Credit Status Enumeration
 * 
 * <p>需求来源：需求文档4.2节M02 - 临时授信额度管理</p>
 * <p>说明：PowerApps审批通过后，临时授信进入APPROVED状态，可用于多次支付，直到用完或过期</p>
 */
public enum TemporaryCreditStatus {
    
    /**
     * 审批通过（PowerApps传入后的初始状态，remainingAmount > 0）
     */
    APPROVED("APPROVED", "审批通过"),
    
    /**
     * 使用中（有未还款的交易占用临时授信，0 < remainingAmount < totalAmount）
     */
    IN_USE("IN_USE", "使用中"),
    
    /**
     * 已用完（remainingAmount = 0，所有额度已被使用）
     */
    EXHAUSTED("EXHAUSTED", "已用完"),
    
    /**
     * 已过期（超过expiryDate，不能再用于新支付）
     */
    EXPIRED("EXPIRED", "已过期");
    
    @EnumValue
    private final String code;
    private final String description;
    
    TemporaryCreditStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
