package com.bytz.modules.cms.payway.wallet.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 创建钱包请求对象
 * Create Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRO {
    
    /**
     * 经销商ID（必填）
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
}
