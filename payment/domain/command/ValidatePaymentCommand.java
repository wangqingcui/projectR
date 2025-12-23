package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 验证支付请求命令对象
 * Validate Payment Command
 * 
 * <p>用例来源：UC-PM-002 支付单验证</p>
 * <p>使用场景：支付渠道验证支付请求的合法性</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatePaymentCommand {
    
    /**
     * 支付金额（必须大于0）
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;
    
    /**
     * 经销商ID
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
}
