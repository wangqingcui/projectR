package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 账期更新事件
 * Term Days Updated Event
 * 
 * <p>在账期天数被更新时发布</p>
 * <p>用例来源：UC-CW-005</p>
 * <p>订阅者：通知系统、报表系统、审计系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermDaysUpdatedEvent {
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 原账期天数
     */
    private Integer oldTermDays;
    
    /**
     * 新账期天数
     */
    private Integer newTermDays;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 更新原因
     */
    private String reason;
    
    /**
     * 操作人
     */
    private String operator;
}
