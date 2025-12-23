package com.bytz.modules.cms.payway.credit.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 还款发起事件
 * Repayment Initiated Event
 * 
 * <p>在发起还款时发布</p>
 * <p>用例来源：UC-CW-020</p>
 * <p>订阅者：通知系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentInitiatedEvent {
    
    /**
     * 账单ID
     */
    private String billId;
    
    /**
     * 账单编号
     */
    private String code;
    
    /**
     * 钱包ID
     */
    private String walletId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 还款金额
     */
    private BigDecimal repaymentAmount;
    
    /**
     * 还款支付单ID
     */
    private String repaymentPaymentId;
    
    /**
     * 发起时间
     */
    private LocalDateTime initiatedAt;
}
