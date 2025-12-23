package com.bytz.modules.cms.payment.shared.event;

import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单创建事件
 * Payment Created Event
 * 
 * <p>触发时机：支付单创建成功后</p>
 * <p>用例来源：UC-PM-001</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {
    
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
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 支付类型
     */
    private PaymentType paymentType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}