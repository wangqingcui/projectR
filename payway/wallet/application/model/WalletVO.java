package com.bytz.modules.cms.payway.wallet.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包响应对象
 * Wallet Value Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "WalletVO", description = "钱包信息")
public class WalletVO {
    
    /**
     * 钱包ID
     */
    @ApiModelProperty(value = "钱包ID", example = "WALLET-0001")
    private String id;
    
    /**
     * 经销商ID
     */
    @ApiModelProperty(value = "经销商ID", example = "RES-123456")
    private String resellerId;
    
    /**
     * 当前余额（可为负数）
     */
    @ApiModelProperty(value = "当前余额（可为负数）", example = "1000.00")
    private BigDecimal balance;
    
    /**
     * 钱包状态枚举
     */
    @ApiModelProperty(value = "钱包状态", allowableValues = "ENABLED,DISABLED")
    @Dict(dicCode = "wallet_status")
    private WalletStatus status;
    
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2025-01-01T08:00:00")
    private LocalDateTime createdTime;
}