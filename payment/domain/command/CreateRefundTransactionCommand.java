package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
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
 * 执行退款记录命令对象
 * Execute Refund Command
 * 
 * <p>用于创建退款流水，供其他渠道执行退款操作</p>
 * <p>用例来源：UC-PM-009 退款处理</p>
 * <p>使用场景：
 *   - 退款支付单创建后，渠道执行退款操作
 *   - 创建退款流水记录
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundTransactionCommand {
    
    /**
     * 退款支付单ID（必填）
     * 退款流水关联的退款支付单
     */
    @NotBlank(message = "退款支付单ID不能为空")
    private String refundPaymentId;
    
    /**
     * 原支付流水ID（必填）
     * 退款必须基于特定的支付流水进行
     */
    @NotBlank(message = "原支付流水ID不能为空")
    private String originalTransactionId;

    /**
     * 退款渠道（选填），默认为原渠道，只有银联可以选择退到其他渠道
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;
    
    /**
     * 退款金额（必填，必须大于0）
     * 所有金额都是正数
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;
    

    
    /**
     * 渠道交易记录ID（必填）
     * 渠道侧唯一标识，用于定位流水
     */
    @NotBlank(message = "渠道交易记录ID不能为空")
    private String channelTransactionId;

    /**
     * 流水状态（可选，支持同步渠道场景）
     * 如果渠道同步返回结果，可直接设置为SUCCESS或FAILED
     * 如果为null，默认为PROCESSING（异步场景）
     */
    private TransactionStatus transactionStatus;
    
    /**
     * 渠道交易号（可选）
     * 可在支付完成确认时补录
     */
    private String channelTransactionNumber;
    
    /**
     * 过期时间（可选）
     */
    private LocalDateTime expirationTime;


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