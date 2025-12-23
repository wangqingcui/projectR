package com.bytz.modules.cms.payment.shared.event;

import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单完全完成事件
 * Payment Completed Event
 * 
 * <p>触发时机：支付单状态变为PAID（完全支付完成）时</p>
 * <p>用例来源：UC-PM-007 支付状态更新</p>
 * <p>说明：当支付单的paidAmount达到paymentAmount时，状态变为PAID，发布此事件</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    
    /**
     * 支付单ID
     */
    private String paymentId;
    
    /**
     * 支付单号
     */
    private String paymentCode;
    
    /**
     * 关联订单号
     */
    private String orderId;
    
    /**
     * 经销商ID
     */
    private String resellerId;

    /**
     * 关联业务ID（如信用记录ID、提货单ID等）
     */
    private String relatedBusinessId;


    /**
     * 支付金额（目标金额）
     */
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额（等于paymentAmount）
     */
    private BigDecimal paidAmount;
    
    /**
     * 支付类型
     */
    private PaymentType paymentType;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
}
