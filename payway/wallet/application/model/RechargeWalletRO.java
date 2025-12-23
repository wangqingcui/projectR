package com.bytz.modules.cms.payway.wallet.application.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 充值钱包请求对象
 * Recharge Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "RechargeWalletRO", description = "充值钱包请求参数")
public class RechargeWalletRO {
    
    /**
     * 钱包ID（必填）
     */
    @NotBlank(message = "钱包ID不能为空")
    @ApiModelProperty(value = "钱包ID", required = true, example = "WALLET-0001")
    private String walletId;
    
    /**
     * 充值金额（必填，可为负值用于冲销）
     */
    @NotNull(message = "充值金额不能为空")
    @ApiModelProperty(value = "充值金额（可为负值用于冲销）", required = true, example = "100.00")
    private BigDecimal amount;
    
    /**
     * 备注信息（可选，PowerApps传入的票据相关信息）
     */
    @ApiModelProperty(value = "备注信息（票据相关）", example = "PowerApps Bill: 2025-001")
    private String remark;
}