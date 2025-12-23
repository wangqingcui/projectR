package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 流水类型枚举
 * Transaction Type Enumeration
 * 
 * <p>标识支付流水的业务类型。</p>
 * <p>来源：需求文档第五章数据模型 - 支付流水表</p>
 * <p>说明：区分支付流水与退款流水</p>
 */
public enum TransactionType {
    
    /**
     * 支付流水（正向交易）
     */
    PAYMENT("PAYMENT", "支付流水"),
    
    /**
     * 退款流水（逆向交易）
     */
    REFUND("REFUND", "退款流水");
    
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