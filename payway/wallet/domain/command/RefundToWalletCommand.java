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
 * 退款到钱包命令
 * Refund To Wallet Command
 * 
 * 用于封装退款到钱包所需的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundToWalletCommand {
    
    /**
     * 钱包ID（必填）
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 退款金额（必填）
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal amount;
    
    /**
     * 原支付交易流水号（必填）
     */
    @NotBlank(message = "原支付交易流水号不能为空")
    private String originalTransactionId;
    
    /**
     * 退款原因（可选）
     */
    private String reason;

}