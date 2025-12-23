package com.bytz.modules.cms.payway.wallet.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 充值钱包命令
 * Recharge Wallet Command
 * 
 * 用于封装充值钱包所需的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeWalletCommand {
    
    /**
     * 钱包ID（必填）
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 充值金额（必填，可为负值用于冲销）
     */
    @NotNull(message = "充值金额不能为空")
    private BigDecimal amount;
    
    /**
     * 备注信息（可选，PowerApps传入的票据相关信息）
     */
    private String remark;
}
