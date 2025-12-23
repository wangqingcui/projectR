package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 启用钱包命令对象
 * Enable Wallet Command
 * 
 * <p>用例来源：UC-CW-002</p>
 * <p>需求来源：T02</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnableWalletCommand {
    
    /**
     * 操作员ID（必填）
     */
    @NotBlank(message = "操作员ID不能为空")
    private String operator;
    
    /**
     * 操作员姓名（必填）
     */
    @NotBlank(message = "操作员姓名不能为空")
    private String operatorName;
    
    /**
     * 启用原因（必填）
     */
    @NotBlank(message = "启用原因不能为空")
    private String reason;
}
