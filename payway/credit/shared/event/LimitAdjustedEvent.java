package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 额度调整事件
 * Limit Adjusted Event
 * 
 * <p>在信用额度被调整时发布</p>
 * <p>用例来源：UC-CW-004</p>
 * <p>订阅者：通知系统、报表系统、审计系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitAdjustedEvent {
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 原总额度
     */
    private BigDecimal oldTotalLimit;
    
    /**
     * 新总额度
     */
    private BigDecimal newTotalLimit;
    
    /**
     * 当前可用额度
     */
    private BigDecimal availableLimit;
    
    /**
     * 调整时间
     */
    private LocalDateTime adjustedAt;
    
    /**
     * 调整原因
     */
    private String reason;
    
    /**
     * 操作人
     */
    private String operator;
}
