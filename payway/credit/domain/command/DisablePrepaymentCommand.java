package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 关闭预付功能命令对象
 * Disable Prepayment Command
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisablePrepaymentCommand {
    
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
     * 操作原因（必填）
     */
    @NotBlank(message = "操作原因不能为空")
    private String reason;
}