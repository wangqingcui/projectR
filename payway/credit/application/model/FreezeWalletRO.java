package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 冻结钱包请求对象
 * Freeze Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreezeWalletRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;

    
    /**
     * 冻结原因
     */
//    @NotBlank(message = "冻结原因不能为空")
    private String reason;
}