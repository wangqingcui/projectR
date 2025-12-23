package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * 使用临时授信支付请求对象
 * Pay With Temporary Credit Request Object
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PayTemporaryCreditRO extends BasePaymentItemRo {

    /**
     * 临时授信ID
     */
    @NotBlank(message = "临时授信ID不能为空")
    private String temporaryCreditId;

    /**
     * 经销商ID（用于校验临时授信归属）
     */
    @NotBlank(groups = AdminGroup.class)
    private String resellerId;
}