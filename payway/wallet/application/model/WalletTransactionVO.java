package com.bytz.modules.cms.payway.wallet.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易响应对象
 * Wallet Transaction Value Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "WalletTransactionVO", description = "钱包交易记录")
public class WalletTransactionVO {
    
    /**
     * 交易流水号ID
     */
    @ApiModelProperty(value = "交易流水号ID", example = "TX-202501010001")
    private String id;
    
    /**
     * 交易流水号编码（业务流水号）
     */
    @ApiModelProperty(value = "交易编码（业务流水号）", example = "WALLET-PAY-0001")
    private String code;
    
    /**
     * 交易类型枚举
     */
    @ApiModelProperty(value = "交易类型", allowableValues = "RECHARGE,PAYMENT,REFUND")
    @Dict(dicCode = "wallet_transaction_type")
    private WalletTransactionType transactionType;
    
    /**
     * 交易状态枚举
     */
    @ApiModelProperty(value = "交易状态", allowableValues = "SUCCESS,FAILED")
    @Dict(dicCode = "wallet_transaction_status")
    private WalletTransactionStatus transactionStatus;
    
    /**
     * 交易金额
     */
    @ApiModelProperty(value = "交易金额", example = "200.00")
    private BigDecimal amount;
    
    /**
     * 交易前余额
     */
    @ApiModelProperty(value = "交易前余额", example = "1200.00")
    private BigDecimal balanceBefore;
    
    /**
     * 交易后余额
     */
    @ApiModelProperty(value = "交易后余额", example = "1000.00")
    private BigDecimal balanceAfter;
    
    /**
     * 备注信息（充值时为PowerApps传入的票据相关信息）
     */
    @ApiModelProperty(value = "备注信息（充值相关票据说明）", example = "PowerApps Bill: 2025-001")
    private String remark;
    
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2025-01-01T09:00:00")
    private LocalDateTime createdTime;
    
    /**
     * 完成时间
     */
    @ApiModelProperty(value = "完成时间", example = "2025-01-01T09:00:05")
    private LocalDateTime completedTime;
}