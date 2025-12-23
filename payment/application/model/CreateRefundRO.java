package com.bytz.modules.cms.payment.application.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建退款请求对象
 * Create Refund Request Object
 * 
 * <p>退款必须基于特定的支付流水进行，需要验证原流水的可退款金额</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundRO {
    
    /**
     * 原支付单ID（必填）
     */
    @NotBlank(message = "原支付单ID不能为空")
    private String originalPaymentId;
    
    /**
     * 订单ID（必填）
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;
    
    /**
     * 经销商ID（必填）
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
    
    /**
     * 退款金额（必填）
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;
    
    /**
     * 退款原因（可选）
     */
    private String refundReason;
    
    /**
     * 退款渠道（可选）
     */
    private PaymentChannel channelType;
    
    /**
     * 关联业务ID（可选，如：退款申请单ID）
     */
    private String relatedBusinessId;

}