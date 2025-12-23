package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款回调命令
 * Refund Callback Command
 * 
 * <p>用于处理异步退款渠道的回调通知</p>
 * <p>使用场景：线上支付渠道（银联、企业网银）异步退款回调</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundCallbackCommand {
    
    /**
     * 支付渠道（必填）
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易记录ID（必填）
     * 渠道侧唯一标识，用于定位退款流水
     */
    @NotBlank(message = "渠道交易记录ID不能为空")
    private String channelTransactionId;
    
    /**
     * 渠道交易号（可选）
     * 渠道侧的流水号
     */
    private String channelTransactionNumber;
    
    /**
     * 是否成功（必填）
     */
    @NotNull(message = "退款结果不能为空")
    private Boolean success;
    
    /**
     * 完成时间（可选）
     */
    private LocalDateTime completedTime;
    
    /**
     * 错误码（失败时有值）
     */
    private String errorCode;
    
    /**
     * 错误信息（失败时有值）
     */
    private String errorMessage;
}