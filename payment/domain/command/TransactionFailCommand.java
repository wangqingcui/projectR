package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 流水失败命令对象
 * Transaction Fail Command
 * 
 * <p>用例来源：UC-PM-006 支付完成确认</p>
 * <p>使用场景：支付渠道回调失败时更新流水状态</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFailCommand {
    
    /**
     * 失败原因
     */
    @NotBlank(message = "失败原因不能为空")
    private String reason;
    
    /**
     * 完成时间（可选，默认为当前时间）
     */
    private LocalDateTime completedTime;


    /**
     * 渠道交易号（可选补录）
     */
    private String channelTransactionNumber;
}