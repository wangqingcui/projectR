package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 关联业务类型枚举
 * Related Business Type Enumeration
 * 
 * <p>标识支付单关联的业务场景。</p>
 * <p>来源：需求文档第五章数据模型 - relatedBusinessType字段</p>
 * <p>术语参考：Glossary.md - "关联业务类型(Related Business Type)"</p>
 */
public enum RelatedBusinessType {
    
    /**
     * 信用记录 - 信用还款场景
     */
    CREDIT_RECORD("CREDIT_RECORD", "信用记录"),
    
    /**
     * 提货单 - 提货费用场景
     */
    DELIVERY_ORDER("DELIVERY_ORDER", "提货单"),
    
    /**
     * 订单 - 普通支付场景
     */
    ORDER("ORDER", "订单");
    
    @EnumValue
    private final String code;
    private final String description;
    
    RelatedBusinessType(String code, String description) {
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
