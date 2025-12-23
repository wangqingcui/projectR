package com.bytz.modules.cms.payment.shared.event;

import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款已执行事件
 * Refund Executed Event
 * 
 * <p>触发时机：退款完成并更新支付单状态后</p>
 * <p>用例来源：UC-PM-009</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundExecutedEvent {
    
    /**
     * 退款支付单ID
     */
    private String refundPaymentId;
    
    /**
     * 原支付单ID
     */
    private String originalPaymentId;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款支付单状态
     */
    private PaymentStatus refundStatus;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundTime;
}