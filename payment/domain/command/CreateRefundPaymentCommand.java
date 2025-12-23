package com.bytz.modules.cms.payment.domain.command;

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
 * 创建退款支付单命令对象
 * Create Refund Payment Command
 * 
 * <p>用例来源：UC-PM-009 退款处理</p>
 * <p>使用场景：由聚合根创建退款支付单时使用</p>
 * <p>注意：此命令用于聚合根工厂方法，与CreateRefundCommand（外部调用命令）不同</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundPaymentCommand {
    
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
     * 原支付单ID（必填）
     */
    @NotBlank(message = "原支付单ID不能为空")
    private String originalPaymentId;

    /**
     * 关联业务类型（可选，如：退款申请单）
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 关联业务ID（可选，如：退款申请单ID）
     */
    private String relatedBusinessId;
    
    /**
     * 退款原因（可选）
     */
    private String refundReason;
}