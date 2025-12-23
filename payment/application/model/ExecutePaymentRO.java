package com.bytz.modules.cms.payment.application.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 执行支付请求对象
 * Execute Payment Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutePaymentRO {
    
    /**
     * 支付单ID（必填）
     */
    @NotBlank(message = "支付单ID不能为空")
    private String paymentId;
    
    /**
     * 支付渠道（必填）
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;
    
    /**
     * 交易金额（必填）
     */
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    private BigDecimal transactionAmount;
    /**
     * 过期时间（可选）
     */
    private LocalDateTime expirationTime;
}