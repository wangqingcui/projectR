package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 解冻钱包命令对象
 * Unfreeze Wallet Command
 * 
 * <p>用例来源：UC-CW-003</p>
 * <p>需求来源：T03</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnfreezeWalletCommand {
    
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
     * 解冻原因（必填）
     */
    @NotBlank(message = "解冻原因不能为空")
    private String reason;
}
