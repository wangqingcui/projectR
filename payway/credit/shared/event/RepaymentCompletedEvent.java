package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 还款完成事件
 * Repayment Completed Event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentCompletedEvent {
    
    /**
     * 还款交易ID
     */
    private String repaymentTransactionId;
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 还款金额
     */
    private BigDecimal amount;
    
    /**
     * 还款后可用额度
     */
    private BigDecimal availableLimit;
    
    /**
     * 临时授信ID（如果是临时授信还款）
     */
    private String temporaryCreditId;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredAt;
}
