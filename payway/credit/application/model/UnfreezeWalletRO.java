package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 解冻钱包请求对象
 * Unfreeze Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnfreezeWalletRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 解冻原因
     */
//    @NotBlank(message = "解冻原因不能为空")
    private String reason;
}