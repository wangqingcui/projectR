package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 更新账期命令对象
 * Update Term Days Command
 * 
 * <p>用例来源：UC-CW-005</p>
 * <p>需求来源：T06</p>
 * <p>说明：账期变更只影响新创建的账单</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTermDaysCommand {
    
    /**
     * 新的账期天数（必填，>0）
     */
    @NotNull(message = "新的账期天数不能为空")
    @Positive(message = "账期天数必须大于0")
    private Integer newTermDays;
    
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
     * 更新原因（必填）
     */
    @NotBlank(message = "更新原因不能为空")
    private String reason;
}
