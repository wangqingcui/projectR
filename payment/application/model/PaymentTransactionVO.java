package com.bytz.modules.cms.payment.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水响应对象
 * Payment Transaction Response Object
 * 
 * <p>用于返回支付流水的详细信息</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionVO {
    
    /**
     * 流水ID
     */
    private String id;
    
    /**
     * 流水号
     */
    private String code;
    
    /**
     * 支付单ID
     */
    private String paymentId;
    
    /**
     * 流水状态
     */
    @Dict(dicCode = "payment_transaction_status")
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道
     */
    @Dict(dicCode = "payment_channel")
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易记录ID
     */
    private String channelTransactionId;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 流水类型
     */
    @Dict(dicCode = "payment_transaction_type")
    private TransactionType transactionType;
    
    /**
     * 原流水ID（退款流水使用）
     */
    private String originalTransactionId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expirationTime;
    
    /**
     * 业务备注
     */
    private String businessRemark;
}