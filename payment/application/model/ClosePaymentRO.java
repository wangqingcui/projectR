package com.bytz.modules.cms.payment.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 关闭支付单请求对象
 * Close Payment Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosePaymentRO {
    
    /**
     * 支付单ID（必填）
     */
    @NotBlank(message = "支付单ID不能为空")
    private String paymentId;
    
    /**
     * 关闭原因（必填）
     */
    @NotBlank(message = "关闭原因不能为空")
    private String closeReason;
}
