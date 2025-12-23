package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用支付事件
 * Credit Paid Event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPaidEvent {
    
    /**
     * 交易ID
     */
    private String transactionId;
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 支付后可用额度
     */
    private BigDecimal availableLimit;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredAt;
}
