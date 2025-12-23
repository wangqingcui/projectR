package com.bytz.modules.cms.payway.credit.shared.event;

import com.bytz.modules.cms.payway.credit.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单创建事件
 * Bill Created Event
 * 
 * <p>在信用支付成功时发布</p>
 * <p>用例来源：UC-CW-010, UC-CW-011, UC-CW-012, UC-CW-013</p>
 * <p>订阅者：通知系统、报表系统、审计系统</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCreatedEvent {
    
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
     * 交易类型
     */
    private TransactionType transactionType;
    
    /**
     * 账单金额
     */
    private BigDecimal amount;
    
    /**
     * 到期日期
     */
    private LocalDate dueDate;
    
    /**
     * 账单备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
