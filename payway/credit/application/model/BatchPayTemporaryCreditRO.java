package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 使用临时授信支付请求对象
 * Pay With Temporary Credit Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPayTemporaryCreditRO {

    /**
     * 临时授信ID
     */
    @NotBlank(message = "临时授信ID不能为空")
    private String temporaryCreditId;

    /**
     * 经销商ID（用于校验临时授信归属）
     */
    @NotBlank(groups = BasePaymentItemRo.AdminGroup.class)
    private String resellerId;

    /**
     * 批量支付项列表
     */
    @NotEmpty(message = "支付项列表不能为空")
    @Valid
    @Size(max = 5, message = "一次最多只能五项")
    private List<BasePaymentItemRo> payments;

}