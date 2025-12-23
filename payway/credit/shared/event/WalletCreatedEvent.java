package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包创建事件
 * Wallet Created Event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreatedEvent {
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 初始授信额度
     */
    private BigDecimal totalLimit;
    
    /**
     * 账期天数
     */
    private Integer termDays;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
