package com.bytz.modules.cms.payment.application.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 支付完成回调请求对象
 * Complete Payment Request Object
 * 
 * <p>回调通过paymentChannel+channelTransactionId唯一标识交易</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletePaymentRO {
    
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
     * 是否成功（必填）
     */
    @NotNull(message = "成功标识不能为空")
    private Boolean success;
    
    /**
     * 渠道响应信息（可选，失败时记录错误信息）
     */
    private String channelResponse;
    
    /**
     * 渠道交易号（可选）
     */
    private String channelTransactionNumber;
    
    /**
     * 完成时间（可选）
     */
    private LocalDateTime completedTime;
}