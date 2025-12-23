package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 交易状态枚举
 * Transaction Status Enumeration
 * 
 * <p>标识单笔流水的处理状态。</p>
 * <p>来源：需求文档 T05/T06 创建支付流水、支付完成确认</p>
 * <p>状态转换：PROCESSING → SUCCESS/FAILED</p>
 */
public enum TransactionStatus {
    
    /**
     * 处理中 - 交易请求已发起，等待处理结果
     */
    PROCESSING("PROCESSING", "处理中"),
    
    /**
     * 成功 - 交易处理成功
     */
    SUCCESS("SUCCESS", "成功"),
    
    /**
     * 失败 - 交易处理失败
     */
    FAILED("FAILED", "失败");
    
    @EnumValue
    private final String code;
    private final String description;
    
    TransactionStatus(String code, String description) {
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
