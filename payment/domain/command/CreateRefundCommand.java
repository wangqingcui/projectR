package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建退款命令对象
 * Create Refund Command
 * 
 * <p>用于方便外部系统调用创建退款的独立命令对象</p>
 * <p>用例来源：UC-PM-009 退款处理</p>
 * <p>使用场景：订单系统发起退款、业务系统申请退款</p>
 * <p>退款必须基于特定的支付流水进行，需要验证原流水的可退款金额</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundCommand {
    
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
     * 退款金额（必填，必须大于0）
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;
    
    /**
     * 退款原因（可选）
     */
    private String refundReason;
    
    /**
     * 退款渠道（可选，不填则使用原支付渠道）
     */
    private PaymentChannel channelType;

    /**
     * 退款关联业务类型（可选，如：退款申请单）
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 关联业务ID（可选，如：退款申请单ID）
     */
    private String relatedBusinessId;

}