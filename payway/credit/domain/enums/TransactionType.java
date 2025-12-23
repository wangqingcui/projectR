package com.bytz.modules.cms.payway.credit.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 交易类型枚举
 * Transaction Type Enumeration
 * 
 * <p>标识信用交易的业务类型。</p>
 * <p>需求来源：需求文档 M03-信用支付服务 T15</p>
 * <p>说明：还款不是独立记录，而是账单生命周期的一个阶段，因此无REPAYMENT类型</p>
 */
public enum TransactionType {
    
    /**
     * 信用支付（普通信用钱包支付）
     */
    CREDIT_PAY("CREDIT_PAY", "信用支付"),
    
    /**
     * 临时授信支付
     */
    TEMPORARY_CREDIT_PAY("TEMPORARY_CREDIT_PAY", "临时授信支付");
    
    @EnumValue
    private final String code;
    private final String description;
    
    TransactionType(String code, String description) {
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
