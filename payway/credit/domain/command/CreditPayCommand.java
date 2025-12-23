package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 单笔信用支付命令对象
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreditPayCommand {


    /**
     * 支付单ID（必填）
     */
    @NotBlank(message = "支付单ID不能为空")
    private String paymentId;

    /**
     * 支付金额（必填，>0）
     */
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private BigDecimal amount;

    /**
     * 备注说明（可选）
     */
    private String remark;
}