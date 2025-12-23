package com.bytz.modules.cms.payment.shared.event;

import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单关闭事件
 * Payment Closed Event
 * 
 * <p>触发时机：支付单关闭（取消或中止）后</p>
 * <p>用例来源：UC-PM-004</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentClosedEvent {

    /**
     * 支付单ID
     */
    private String paymentId;


    /**
     * 支付单编码
     */
    private String paymentCode;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 经销商ID
     */
    private String resellerId;

    /**
     * 支付类型
     */
    private PaymentType paymentType;
    
    /**
     * 最终状态（CANCELED或TERMINATED）
     */
    private PaymentStatus paymentStatus;
    
    /**
     * 关闭原因
     */
    private String closeReason;

    /**
     * 支付金额（目标金额）
     */
    private BigDecimal paymentAmount;

    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;
}