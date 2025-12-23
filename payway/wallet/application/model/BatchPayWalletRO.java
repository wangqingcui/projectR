package com.bytz.modules.cms.payway.wallet.application.model;

import com.bytz.modules.cms.payway.credit.application.model.BasePaymentItemRo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * 批量使用钱包支付请求对象
 * Batch Pay Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BatchPayWalletRO", description = "批量使用钱包支付请求参数")
public class BatchPayWalletRO {

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

    /**
     * 批量支付项列表（必填，至少包含一项）
     */
    @NotEmpty(message = "支付项列表不能为空")
    @Valid
    @ApiModelProperty(value = "批量支付项列表", required = true)
    // 限制最大五项
    @Size(max = 5, message = "一次最多只能五项")
    private List<BasePaymentItemRo> payments;
}