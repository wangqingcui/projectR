package com.bytz.modules.cms.payway.wallet.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 创建钱包命令
 * Create Wallet Command
 * 
 * 用于封装创建钱包所需的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletCommand {
    
    /**
     * 经销商ID（必填）
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
}
