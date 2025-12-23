package com.bytz.modules.cms.payway.wallet.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易实体
 * Wallet Transaction Entity
 * 
 * 记录钱包的每次充值、支付、退款操作
 * 需求来源：票据钱包需求 - 票据钱包消费明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_wallet_transaction")
public class WalletTransactionEntity {
    
    /**
     * 交易流水号ID（主键，全局唯一，32位字符）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 交易流水号编码（业务流水号）
     */
    @TableField("code")
    private String code;
    
    /**
     * 关联钱包ID
     */
    @TableField("wallet_id")
    private String walletId;
    
    /**
     * 交易类型枚举：RECHARGE(充值)/PAYMENT(支付)/REFUND(退款)
     */
    @TableField("transaction_type")
    private WalletTransactionType transactionType;
    
    /**
     * 交易状态枚举：SUCCESS(成功)/FAILED(失败)
     */
    @TableField("transaction_status")
    private WalletTransactionStatus transactionStatus;
    
    /**
     * 交易金额（6位小数，充值和退款为正数，支付为负数）
     */
    @TableField("amount")
    private BigDecimal amount;
    
    /**
     * 交易前余额（6位小数）
     */
    @TableField("balance_before")
    private BigDecimal balanceBefore;
    
    /**
     * 交易后余额（6位小数）
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;
    
    /**
     * 备注信息（可选）
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 删除状态（0-正常，1-删除）
     */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
    
    /**
     * 创建人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * 创建人姓名
     */
    @TableField(value = "create_by_name", fill = FieldFill.INSERT)
    private String createByName;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 交易完成时间（成功或失败时记录）
     */
    @TableField("completed_time")
    private LocalDateTime completedTime;
}