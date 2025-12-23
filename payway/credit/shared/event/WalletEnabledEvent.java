package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 钱包启用事件
 * Wallet Enabled Event
 * 
 * <p>在钱包被启用时发布</p>
 * <p>用例来源：UC-CW-002</p>
 * <p>订阅者：通知系统、报表系统、审计系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEnabledEvent {
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 启用时间
     */
    private LocalDateTime enabledAt;
    
    /**
     * 启用原因
     */
    private String reason;
    
    /**
     * 操作人
     */
    private String operator;
}
