package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 发起还款请求对象
 * Initiate Repayment Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateRepaymentRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 账单ID
     */
    @NotBlank(message = "账单ID不能为空")
    private String billId;
}