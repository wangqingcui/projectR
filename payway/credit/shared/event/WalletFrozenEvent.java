package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 钱包冻结事件
 * Wallet Frozen Event
 * 
 * <p>在钱包被冻结时发布</p>
 * <p>用例来源：UC-CW-003, UC-CW-031</p>
 * <p>订阅者：通知系统、报表系统、审计系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletFrozenEvent {
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 冻结时间
     */
    private LocalDateTime frozenAt;
    
    /**
     * 冻结原因
     */
    private String reason;
    
    /**
     * 操作人（系统自动冻结为"SYSTEM"）
     */
    private String operator;
    
    /**
     * 逾期交易编号列表（自动冻结时使用）
     */
    private List<String> overdueTransactionCodes;
}
