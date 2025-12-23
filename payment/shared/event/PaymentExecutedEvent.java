package com.bytz.modules.cms.payment.shared.event;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付已执行事件（对应功能点T08）
 * Payment Executed Event
 *
 * <p>触发时机：支付回调处理完成并更新支付单状态后</p>
 * <p>用例来源：UC-PM-008</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecutedEvent {

    /**
     * 支付单ID
     */
    private String paymentId;


    /**
     * 支付单编码
     */
    private String paymentCode;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 经销商ID
     */
    private String resellerId;

    /**
     * 支付类型
     */
    private PaymentType paymentType;

    /**
     * 支付状态
     */
    private PaymentStatus paymentStatus;

    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;

    /**
     * 待支付金额
     */
    private BigDecimal pendingAmount;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;

    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;

    /**
     * 交易状态
     */
    private TransactionStatus transactionStatus;

    /**
     * 渠道交易记录ID
     */
    private String channelTransactionId;

    /**
     * 渠道流水号
     */
    private String channelTransactionNumber;

    /**
     * 执行时间Å
     */
    private LocalDateTime executedTime;

}