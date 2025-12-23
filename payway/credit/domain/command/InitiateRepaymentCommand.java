package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 发起还款命令对象
 * Initiate Repayment Command
 * 
 * <p>用例来源：UC-CW-020</p>
 * <p>需求来源：T18-1</p>
 * <p>说明：验证账单状态为UNPAID且未绑定repaymentPaymentId</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateRepaymentCommand {
    
    /**
     * 账单ID（必填）
     */
    @NotBlank(message = "账单ID不能为空")
    private String billId;
    
    /**
     * 还款支付单ID（支付系统返回，必填）
     */
    @NotBlank(message = "还款支付单ID不能为空")
    private String repaymentPaymentId;
}
