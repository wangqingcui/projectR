package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建支付流水命令对象
 * Create Transaction Command
 * 
 * <p>用例来源：UC-PM-005 创建支付流水</p>
 * <p>使用场景：支付渠道验证通过后创建支付流水记录</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionCommand {
    
    /**
     * 支付单ID（必填）
     */
    @NotBlank(message = "支付单ID不能为空")
    private String paymentId;
    
    /**
     * 支付渠道（必填）
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;
    
    /**
     * 交易金额（必填）
     */
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    private BigDecimal transactionAmount;
    
    /**
     * 渠道交易记录ID（必填）
     */
    @NotBlank(message = "渠道交易记录ID不能为空")
    private String channelTransactionId;
    
    /**
     * 渠道交易号（可选）
     */
    private String channelTransactionNumber;
    
    /**
     * 流水类型（PAYMENT/REFUND，可选，默认为PAYMENT）
     */
    private TransactionType transactionType;
    
    /**
     * 原支付流水ID（退款流水必填）
     */
    private String originalTransactionId;
    
    /**
     * 过期时间（可选）
     */
    private LocalDateTime expirationTime;
    
    /**
     * 流水状态（可选，支持同步渠道场景）
     * 如果渠道同步返回结果，可直接设置为SUCCESS或FAILED
     * 如果为null，默认为PROCESSING（异步场景）
     */
    private TransactionStatus transactionStatus;
    
    /**
     * 完成时间（同步场景传入，状态为SUCCESS/FAILED时使用）
     */
    private LocalDateTime completedTime;
    
    /**
     * 错误信息（同步失败场景传入）
     */
    private String errorMessage;



    /**
     * 业务备注
     */
    private String businessRemark;
}