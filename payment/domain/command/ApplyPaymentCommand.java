package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 应用支付成功回调命令对象
 * Apply Payment Command
 * 
 * <p>用例来源：UC-PM-007 支付状态更新</p>
 * <p>使用场景：支付渠道回调成功后更新支付流水状态</p>
 * <p>注意：回调只更新流水状态，金额从流水本身获取</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyPaymentCommand {
    
    /**
     * 支付渠道
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel channel;
    
    /**
     * 渠道交易号（可选）
     */
    private String channelTransactionNumber;
    
    /**
     * 完成时间（可选）
     */
    private LocalDateTime completedTime;
}
