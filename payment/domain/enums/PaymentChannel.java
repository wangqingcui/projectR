package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 支付渠道类型枚举
 * Payment Channel Type Enumeration
 * 
 * <p>标识支付使用的渠道类型。</p>
 * <p>来源：需求文档 M05 支付渠道集成（T15-T18）</p>
 * <p>术语参考：Glossary.md - "支付渠道类型(Payment Channel Type)"</p>
 */
public enum PaymentChannel {
    
    /**
     * 线上支付 - 银联、企业网银等第三方支付平台
     */
    ONLINE_PAYMENT("ONLINE_PAYMENT", "线上支付"),
    
    /**
     * 钱包支付 - 企业内部资金账户
     */
    WALLET_PAYMENT("WALLET_PAYMENT", "钱包支付"),
    
    /**
     * 电汇支付 - 银行转账方式
     */
    WIRE_TRANSFER("WIRE_TRANSFER", "电汇支付"),
    
    /**
     * 信用账户 - 企业信用额度
     */
    CREDIT_ACCOUNT("CREDIT_ACCOUNT", "信用账户");
    
    @EnumValue
    private final String code;
    private final String description;
    
    PaymentChannel(String code, String description) {
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
