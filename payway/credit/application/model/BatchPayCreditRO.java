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
 * 批量信用支付请求对象
 * Batch Credit Pay Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPayCreditRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 经销商ID（用于校验钱包归属）
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