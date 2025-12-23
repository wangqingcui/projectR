package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 临时授信使用事件
 * Temporary Credit Used Event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryCreditUsedEvent {
    
    /**
     * 交易ID
     */
    private String transactionId;
    
    /**
     * 临时授信ID
     */
    private String temporaryCreditId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 使用金额
     */
    private BigDecimal amount;
    
    /**
     * 使用后剩余金额
     */
    private BigDecimal remainingAmount;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredAt;
}
