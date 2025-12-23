package com.bytz.modules.cms.payway.wallet.domain.valueobject;

import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易值对象
 * Wallet Transaction Value Object
 * 
 * Domain层的交易记录值对象，不依赖基础设施层
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionValueObject {
    
    /**
     * 交易流水号ID
     */
    private String id;
    
    /**
     * 交易流水号编码（业务流水号）
     */
    private String code;
    
    /**
     * 关联钱包ID
     */
    private String walletId;
    
    /**
     * 交易类型枚举：RECHARGE(充值)/PAYMENT(支付)/REFUND(退款)
     */
    private WalletTransactionType transactionType;
    
    /**
     * 交易状态枚举：SUCCESS(成功)/FAILED(失败)
     */
    private WalletTransactionStatus transactionStatus;
    
    /**
     * 交易金额（6位小数，充值和退款为正数，支付为负数）
     */
    private BigDecimal amount;
    
    /**
     * 交易前余额（6位小数）
     */
    private BigDecimal balanceBefore;
    
    /**
     * 交易后余额（6位小数）
     */
    private BigDecimal balanceAfter;
    
    /**
     * 备注信息（可选）
     */
    private String remark;
    
    /**
     * 删除状态（0-正常，1-删除）
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 交易完成时间（成功或失败时记录）
     */
    private LocalDateTime completedTime;
}
