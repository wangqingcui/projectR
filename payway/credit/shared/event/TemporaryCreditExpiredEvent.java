package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 临时授信过期事件
 * Temporary Credit Expired Event
 * 
 * <p>在临时授信过期时发布</p>
 * <p>用例来源：UC-CW-007</p>
 * <p>订阅者：通知系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryCreditExpiredEvent {
    
    /**
     * 临时授信ID
     */
    private String temporaryCreditId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 临时授信总额
     */
    private BigDecimal totalAmount;
    
    /**
     * 已使用金额
     */
    private BigDecimal usedAmount;
    
    /**
     * 剩余金额
     */
    private BigDecimal remainingAmount;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;
}
