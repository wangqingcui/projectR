package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 临时授信接收事件
 * Temporary Credit Received Event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryCreditReceivedEvent {
    
    /**
     * 临时授信ID
     */
    private String temporaryCreditId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 临时授信总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * PowerApps审批ID
     */
    private String approvalId;
    
    /**
     * 过期日期
     */
    private LocalDate expiryDate;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredAt;
}
