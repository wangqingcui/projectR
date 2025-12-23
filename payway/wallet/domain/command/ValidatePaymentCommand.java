package com.bytz.modules.cms.payway.wallet.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 校验支付命令
 * Validate Payment Command
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatePaymentCommand {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;
}
