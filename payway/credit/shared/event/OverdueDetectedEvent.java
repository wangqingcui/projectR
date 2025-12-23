package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 逾期检测事件
 * Overdue Detected Event
 * 
 * <p>在定时任务检测到逾期账单时发布</p>
 * <p>用例来源：UC-CW-030</p>
 * <p>订阅者：
 *   - OverdueAutoFreezeListener（自动冻结）
 *   - OverdueNotificationListener（发送通知）
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueDetectedEvent {
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 逾期账单ID列表
     */
    private List<String> overdueBillIds;
    
    /**
     * 逾期总金额
     */
    private BigDecimal overdueAmount;
    
    /**
     * 逾期账单数量
     */
    private Integer overdueBillCount;
    
    /**
     * 最大逾期天数
     */
    private Integer maxOverdueDays;
    
    /**
     * 检测时间
     */
    private LocalDateTime detectedAt;
}
