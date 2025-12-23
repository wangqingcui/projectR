package com.bytz.modules.cms.payway.credit.application.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 基础支付项参数
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BasePaymentItemRo {

    public interface AdminGroup {
    }

    /**
     * 支付单ID
     */
    @NotBlank(message = "支付单ID不能为空")
    private String paymentId;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private BigDecimal amount;

}