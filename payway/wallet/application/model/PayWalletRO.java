package com.bytz.modules.cms.payway.wallet.application.model;

import com.bytz.modules.cms.payway.credit.application.model.BasePaymentItemRo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * 使用钱包支付请求对象
 * Pay Wallet Request Object
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "PayWalletRO", description = "使用钱包支付请求参数")
public class PayWalletRO extends BasePaymentItemRo {
    
    /**
     * 钱包ID（必填）
     */
    @NotBlank(message = "钱包ID不能为空")
    @ApiModelProperty(value = "钱包ID", required = true, example = "WALLET-0001")
    private String walletId;

    /**
     * 经销商ID（用于校验临时授信归属）
     */
    @NotBlank(groups = BasePaymentItemRo.AdminGroup.class)
    private String resellerId;

}