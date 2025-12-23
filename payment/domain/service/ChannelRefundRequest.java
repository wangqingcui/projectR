package com.bytz.modules.cms.payment.domain.service;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 渠道退款请求
 * Channel Refund Request
 * 
 * <p>向支付渠道发起退款请求的参数</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRefundRequest {
    
    /**
     * 退款支付单ID
     */
    private String refundPaymentId;
    
    /**
     * 退款支付单号
     */
    private String refundPaymentCode;
    
    /**
     * 原支付单ID
     */
    private String originalPaymentId;
    
    /**
     * 原支付流水ID
     */
    private String originalTransactionId;
    
    /**
     * 原渠道交易记录ID
     * 用于在渠道侧定位原支付交易
     */
    private String originalChannelTransactionId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 退款金额（正数）
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款渠道
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 关联业务类型
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 关联业务ID（如退款申请单ID）
     */
    private String relatedBusinessId;
}