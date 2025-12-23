package com.bytz.modules.cms.payment.application.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建预付支付单请求参数
 * cms-backend
 *
 * @author bytz
 * @version 1.0
 * @date 2025/12/2
 */
@Data
@ApiModel(value = "创建预付支付单请求参数", description = "创建预付支付单请求参数")
public class CreatePrepayRequest {

    @ApiModelProperty(value = "订单ID", required = true)
    @NotBlank
    private String orderId;

    @ApiModelProperty(value = "是否整单支付", required = true)
    @NotNull
    private Boolean isFullPayment;
}
