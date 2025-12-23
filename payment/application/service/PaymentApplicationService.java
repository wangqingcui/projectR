package com.bytz.modules.cms.payment.application.service;

import com.bytz.modules.cms.payment.application.assembler.PaymentAssembler;
import com.bytz.modules.cms.payment.application.model.*;
import com.bytz.modules.cms.payment.domain.PaymentDomainService;
import com.bytz.modules.cms.payment.domain.RefundDomainService;
import com.bytz.modules.cms.payment.domain.command.*;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.model.RefundAggregate;
import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 支付应用服务
 * Payment Application Service
 *
 * <p>处理所有写操作（CQRS中的Command端）</p>
 * <p>职责：
 * - 统一处理所有写操作
 * - 协调领域服务
 * - RO/VO转换
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentDomainService paymentDomainService;
    private final PaymentAssembler paymentAssembler;

    private final RefundDomainService refundDomainService;


    /**
     * 创建支付单
     *
     * @param ro 创建支付单请求对象
     * @return 支付单VO
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(PaymentCreateRO ro) {
        log.info("创建支付单，订单号: {}", ro.getOrderId());

        // RO -> Command
        CreatePaymentCommand command = paymentAssembler.toCreateCommand(ro);

        // 调用领域服务
        PaymentAggregate payment = paymentDomainService.createPayment(command);

        // Aggregate -> VO
        return paymentAssembler.toVO(payment);
    }

    /**
     * 关闭支付单
     *
     * @param ro 关闭支付单请求对象
     * @return 支付单VO
     */
    @Transactional(rollbackFor = Exception.class)
    @Deprecated// 应用层不处理这件事情
    public PaymentVO closePayment(ClosePaymentRO ro) {
        log.info("关闭支付单，支付单ID: {}", ro.getPaymentId());

        // RO -> Command
        ClosePaymentCommand command = ClosePaymentCommand.builder()
                .closeReason(ro.getCloseReason())
                .build();

        // 调用领域服务
        PaymentAggregate payment = paymentDomainService.closePayment(ro.getPaymentId(), command);

        // Aggregate -> VO
        return paymentAssembler.toVO(payment);
    }

    /**
     * 执行支付（创建支付流水）
     *
     * @param ro 执行支付请求对象
     * @return 支付流水VO
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransactionVO executePayment(ExecutePaymentRO ro) {
        log.info("执行支付，支付单ID: {}", ro.getPaymentId());

        // RO -> Command
        CreateTransactionCommand command = paymentAssembler.toCreateTransactionCommand(ro);

        // 调用领域服务
        PaymentTransactionEntity transaction = paymentDomainService.executePayment(command);

        // Entity -> VO
        return paymentAssembler.toVO(transaction);
    }

    /**
     * 处理支付完成回调
     * 回调只提供channelTransactionId，可能对应多个支付单
     *
     * @param ro 支付完成回调请求对象
     * @return 支付流水VO列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<PaymentTransactionVO> handlePaymentComplete(CompletePaymentRO ro) {
        log.info("处理支付完成回调，渠道交易记录ID: {}", ro.getChannelTransactionId());

        // RO -> Command
        CompletePaymentCommand command = paymentAssembler.toCompleteCommand(ro);

        // 调用领域服务（返回多个流水）
        List<PaymentTransactionEntity> transactions = paymentDomainService.handlePaymentComplete(command);

        // Entity -> VO
        return paymentAssembler.toTransactionVOList(transactions);
    }

    /**
     * 创建退款支付单
     * 退款必须基于特定的支付流水进行
     *
     * @param ro 创建退款请求对象
     * @return 退款支付单VO
     */
    @Transactional(rollbackFor = Exception.class)
    @Deprecated// 应用层不处理这件事情
    public PaymentVO createRefund(CreateRefundRO ro) {
        log.info("创建退款支付单，原支付单ID: {}",
                ro.getOriginalPaymentId());

        // RO -> Command
        CreateRefundCommand command = CreateRefundCommand.builder()
                .originalPaymentId(ro.getOriginalPaymentId())
                .orderId(ro.getOrderId())
                .resellerId(ro.getResellerId())
                .refundAmount(ro.getRefundAmount())
                .refundReason(ro.getRefundReason())
                .channelType(ro.getChannelType())
                .relatedBusinessId(ro.getRelatedBusinessId())
                .build();

        // 调用领域服务
        RefundAggregate refundPayment = refundDomainService.createRefund(command);

        // Aggregate -> VO
        return paymentAssembler.toVO(refundPayment);
    }

    /**
     * 执行退款（创建退款流水）
     * 在退款支付单创建后，由渠道调用创建退款流水
     *
     * @param ro 执行退款请求对象
     * @return 退款流水VO
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransactionVO executeRefund(ExecuteRefundRO ro) {
        log.info("执行退款，退款支付单ID: {}, 原流水ID: {}", ro.getRefundPaymentId(), ro.getOriginalTransactionId());

        // RO -> Command
        ExecuteRefundCommand command = ExecuteRefundCommand.builder()
                .refundPaymentId(ro.getRefundPaymentId())
                .originalTransactionId(ro.getOriginalTransactionId())
                .refundAmount(ro.getRefundAmount())
                .paymentChannel(ro.getPaymentChannel())
                .businessRemark(ro.getBusinessRemark())
                .expirationTime(ro.getExpirationTime())
                .build();

        // 调用领域服务
        RefundTransactionEntity refundTransaction = refundDomainService.executeRefund(command);

        // Entity -> VO
        return paymentAssembler.toRefundTransactionVO(refundTransaction);
    }

    /**
     * 处理退款回调（异步退款渠道）
     *
     * @param ro 退款回调请求对象
     * @return 更新后的退款流水VO列表
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransactionVO handleRefundCallback(RefundCallbackRO ro) {
        log.info("处理退款回调，支付渠道: {}, 渠道交易记录ID: {}",
                ro.getPaymentChannel(), ro.getChannelTransactionId());

        // RO -> Command
        RefundCallbackCommand command = RefundCallbackCommand.builder()
                .paymentChannel(ro.getPaymentChannel())
                .channelTransactionId(ro.getChannelTransactionId())
                .channelTransactionNumber(ro.getChannelTransactionNumber())
                .success(ro.getSuccess())
                .completedTime(ro.getCompletedTime())
                .errorCode(ro.getErrorCode())
                .errorMessage(ro.getErrorMessage())
                .build();

        // 调用领域服务
        RefundTransactionEntity transaction = refundDomainService.handleRefundCallback(command);

        // Entity -> VO
        return paymentAssembler.toRefundTransactionVO(transaction);
    }
}