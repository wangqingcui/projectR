package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 完成支付命令对象
 * Complete Payment Command
 * 
 * <p>用例来源：UC-PM-006 支付完成确认</p>
 * <p>使用场景：支付渠道完成支付后通知支付模块</p>
 * <p>回调通过paymentChannel+channelTransactionId唯一标识交易</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletePaymentCommand {
    
    /**
     * 支付渠道（必填，与channelTransactionId一起唯一标识交易）
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易记录ID（必填，通过该ID查找关联的支付单）
     */
    @NotBlank(message = "渠道交易记录ID不能为空")
    private String channelTransactionId;
    
    /**
     * 是否成功
     */
    @NotNull(message = "成功标识不能为空")
    private Boolean success;
    
    /**
     * 渠道响应信息（JSON格式，可选，失败时记录错误信息）
     */
    private String channelResponse;
    
    /**
     * 渠道交易号（可选补录）
     */
    private String channelTransactionNumber;
    
    /**
     * 完成时间（可选）
     */
    private LocalDateTime completedTime;
}