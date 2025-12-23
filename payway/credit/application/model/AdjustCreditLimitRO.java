package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 调整信用额度请求对象
 * Adjust Credit Limit Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustCreditLimitRO {

    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;

    /**
     * 新的总授信额度
     */
    @NotNull(message = "新的总授信额度不能为空")
    @DecimalMin(value = "0", message = "总授信额度不能为负数")
    private BigDecimal newTotalLimit;

//    /**
//     * 操作员ID
//     */
//    @NotBlank(message = "操作员ID不能为空")
//    private String operator;
//
//    /**
//     * 操作员姓名
//     */
//    @NotBlank(message = "操作员姓名不能为空")
//    private String operatorName;

    /**
     * 调整原因
     */
//    @NotBlank(message = "调整原因不能为空")
    private String reason;
}
