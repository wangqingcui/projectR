package com.bytz.modules.cms.payment.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退款状态VO
 * Refund Status Value Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundStatusVO {
    
    /**
     * 原支付单ID
     */
    private String originalPaymentId;
    
    /**
     * 原支付单号
     */
    private String originalPaymentCode;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 已退款金额
     */
    private BigDecimal refundedAmount;
    
    /**
     * 可退款金额
     */
    private BigDecimal refundableAmount;
    
    /**
     * 是否允许退款
     */
    private Boolean canRefund;
}
