package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 调整信用额度命令对象
 * Adjust Credit Limit Command
 * 
 * <p>用例来源：UC-CW-004</p>
 * <p>需求来源：T04</p>
 * <p>说明：允许新额度小于已使用额度（可用额度可为负数）</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustCreditLimitCommand {
    
    /**
     * 新的总授信额度（必填，>=0）
     */
    @NotNull(message = "新的总授信额度不能为空")
    @PositiveOrZero(message = "新的总授信额度不能为负数")
    private BigDecimal newTotalLimit;
    
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
     * 调整原因（必填）
     */
    @NotBlank(message = "调整原因不能为空")
    private String reason;
}
