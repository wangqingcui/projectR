package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 钱包解冻事件
 * Wallet Unfrozen Event
 * 
 * <p>在钱包被解冻时发布</p>
 * <p>用例来源：UC-CW-003</p>
 * <p>订阅者：通知系统、报表系统、审计系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletUnfrozenEvent {
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 解冻时间
     */
    private LocalDateTime unfrozenAt;
    
    /**
     * 解冻原因
     */
    private String reason;
    
    /**
     * 操作人
     */
    private String operator;
}
