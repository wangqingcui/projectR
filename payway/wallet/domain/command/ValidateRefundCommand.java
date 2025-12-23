package com.bytz.modules.cms.payway.wallet.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 校验退款命令
 * Validate Refund Command
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateRefundCommand {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    private BigDecimal amount;
    
    /**
     * 原交易金额
     */
    @NotNull(message = "原交易金额不能为空")
    private BigDecimal originalAmount;
}
