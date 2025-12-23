package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 停用钱包请求对象
 * Disable Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisableWalletRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 停用原因
     */
//    @NotBlank(message = "停用原因不能为空")
    private String reason;
}