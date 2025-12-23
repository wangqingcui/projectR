package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 启用钱包请求对象
 * Enable Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnableWalletRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 启用原因
     */
//    @NotBlank(message = "启用原因不能为空")
    private String reason;
}