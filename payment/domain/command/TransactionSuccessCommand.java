package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流水成功命令对象
 * Transaction Success Command
 * 
 * <p>用例来源：UC-PM-006 支付完成确认</p>
 * <p>使用场景：支付渠道回调成功时更新流水状态</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSuccessCommand {
    
    /**
     * 完成时间（可选）
     */
    private LocalDateTime completedTime;
    
    /**
     * 渠道交易号（可选补录）
     */
    private String channelTransactionNumber;
}
