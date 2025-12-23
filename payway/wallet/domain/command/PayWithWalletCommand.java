package com.bytz.modules.cms.payway.wallet.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 钱包支付命令
 * Pay With Wallet Command
 * 
 * 用于封装钱包支付所需的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayWithWalletCommand {
    
    /**
     * 钱包ID（必填）
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 支付金额（必填）
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;
    
    /**
     * 备注信息（可选）
     */
    private String remark;
}
