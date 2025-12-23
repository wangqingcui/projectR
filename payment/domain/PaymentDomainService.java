package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.domain.command.*;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundRequest;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundResponse;
import com.bytz.modules.cms.payment.domain.service.IPaymentChannelRefundService;
import com.bytz.modules.cms.payment.domain.service.PaymentChannelRefundServiceRegistry;
import com.bytz.modules.cms.payment.shared.event.PaymentClosedEvent;
import com.bytz.modules.cms.payment.shared.event.PaymentCompletedEvent;
import com.bytz.modules.cms.payment.shared.event.PaymentCreatedEvent;
import com.bytz.modules.cms.payment.shared.event.PaymentExecutedEvent;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付领域服务
 * Payment Domain Service
 * 
 * <p>职责：
 *   - 创建支付单（含正向支付和退款支付单）
 *   - 验证支付请求
 *   - 执行支付（创建流水并下发渠道）
 *   - 处理支付完成回调
 *   - 关闭支付单
 *   - 创建退款支付单并调用渠道执行退款
 *   - 处理退款回调
 *   - 查询退款状态
 * </p>
 * <p>用例来源：UC-PM-001~009</p>
 * <p>注意：流水的持久化通过聚合根进行，不存在独立的流水仓储</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    private final IPaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentCodeGenerator paymentCodeGenerator;
    private final PaymentChannelRefundServiceRegistry channelRefundServiceRegistry;

    /**
     * 创建支付单（对应功能点T01）
     * 用例来源：UC-PM-001 支付单接收与创建
     *
     * @param command 创建支付单命令
     * @return 创建的支付单聚合根
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate createPayment(@Valid CreatePaymentCommand command) {
        log.info("创建支付单，订单号: {}, 支付类型: {}", command.getOrderId(), command.getPaymentType());

        // 创建支付单聚合根
        PaymentAggregate payment = PaymentAggregate.create(command);

        // 生成支付单号
        payment.setCode(paymentCodeGenerator.generatePaymentCode());

        // 持久化
        payment = paymentRepository.save(payment);

        log.info("支付单创建成功，支付单号: {}", payment.getCode());

        // 发布支付单创建事件
        publishPaymentCreatedEvent(payment);

        return payment;
    }

    /**
     * 验证支付请求（对应功能点T02）
     * 用例来源：UC-PM-002 支付单验证
     *
     * @param paymentId 支付单ID
     * @param command 验证支付命令
     * @return 验证结果，true表示验证通过
     */
    public boolean validatePayment(String paymentId, @Valid ValidatePaymentCommand command) {
        log.info("验证支付请求，支付单ID: {}", paymentId);

        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return payment.validatePayment(command);
    }

    /**
     * 执行支付（对应功能点T05）
     * 用例来源：UC-PM-003 支付执行（单支付单）
     * 创建支付流水并更新支付单状态为PAYING
     *
     * @param command 创建流水命令
     * @return 创建的支付流水
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransactionEntity executePayment(@Valid CreateTransactionCommand command) {
        log.info("执行支付，支付单ID: {}, 金额: {}", command.getPaymentId(), command.getTransactionAmount());

        // 查询支付单
        PaymentAggregate payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 通过聚合根统一创建流水（包含验证逻辑和状态更新）
        PaymentTransactionEntity transaction = payment.createTransaction(command);
        transaction.setCode(paymentCodeGenerator.generateTransactionCode());

        // 持久化（使用update因为支付单已存在）
        paymentRepository.update(payment);

        log.info("支付流水创建成功，流水号: {}", transaction.getCode());

        // 支付单完成直接抛出事件
        if (payment.getPaymentStatus() == PaymentStatus.PAID){
            publishPaymentCompletedEvent(payment);
        }
        return transaction;
    }

    /**
     * 处理支付完成回调（对应功能点T06/T07）
     * 用例来源：UC-PM-006 支付完成确认, UC-PM-007 支付状态更新
     * 根据paymentChannel+channelTransactionId唯一标识交易，查找所有关联的支付单和流水
     * 流水状态更新通过聚合根统一管理
     *
     * @param command 完成支付命令（包含paymentChannel、channelTransactionId、success等）
     * @return 更新后的支付流水列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<PaymentTransactionEntity> handlePaymentComplete(@Valid CompletePaymentCommand command) {
        log.info("处理支付完成回调，支付渠道: {}, 渠道交易记录ID: {}, 是否成功: {}", 
                command.getPaymentChannel(), command.getChannelTransactionId(), command.getSuccess());

        // 根据paymentChannel+channelTransactionId查找所有相关的支付单（加载流水）
        List<PaymentAggregate> payments = paymentRepository.findByChannelAndTransactionId(
                command.getPaymentChannel(), command.getChannelTransactionId(), true);
        
        if (payments.isEmpty()) {
            throw new PaymentException(PaymentErrorCode.TRANSACTION_NOT_FOUND,
                    String.format("未找到支付渠道[%s]和交易记录ID[%s]对应的支付单", 
                            command.getPaymentChannel(), command.getChannelTransactionId()));
        }
        
        List<PaymentTransactionEntity> transactions = new ArrayList<>();
        
        // 处理每个支付单（所有回调业务逻辑由聚合根统一处理）
        for (PaymentAggregate payment : payments) {
            // 聚合根统一处理回调业务逻辑
            PaymentTransactionEntity transaction = payment.handleCallback(command);
            // 发布支付已执行事件
            publishPaymentExecutedEvent(payment,transaction);
            if (transaction != null) {
                transactions.add(transaction);
            } else {
                log.warn("支付单 {} 无可处理的流水，跳过", payment.getCode());
            }
        }

        // 批量保存支付单（通过聚合根保存流水）
        paymentRepository.updateAll(payments);
        
        // 发布支付已执行事件，如果支付完全完成则发布完成事件
        for (PaymentAggregate payment : payments) {
            // 如果支付单完全完成（状态为PAID），发布完成事件
            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                publishPaymentCompletedEvent(payment);
            }
        }

        log.info("支付完成回调处理完成，处理支付单数量: {}", payments.size());
        return transactions;
    }

    /**
     * 关闭支付单（对应功能点T04）
     * 用例来源：UC-PM-004 支付单关闭
     *
     * @param paymentId 支付单ID
     * @param command 关闭支付命令
     * @return 关闭后的支付单
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate closePayment(String paymentId, @Valid ClosePaymentCommand command) {
        log.info("关闭支付单，支付单ID: {}, 原因: {}", paymentId, command.getCloseReason());

        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 执行关闭
        payment.close(command);

        // 持久化（使用update因为支付单已存在）
        paymentRepository.update(payment);

        // 发布支付单关闭事件
        publishPaymentClosedEvent(payment, command.getCloseReason());

        log.info("支付单关闭成功，最终状态: {}", payment.getPaymentStatus());
        return payment;
    }


    // ==================== 事件发布方法 ====================

    private void publishPaymentCreatedEvent(PaymentAggregate payment) {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getCode())
                .orderId(payment.getOrderId())
                .resellerId(payment.getResellerId())
                .paymentAmount(payment.getPaymentAmount())
                .paymentType(payment.getPaymentType())
                .createTime(payment.getCreateTime())
                .build();

        eventPublisher.publishEvent(event);
        log.debug("已发布支付单创建事件，支付单号: {}", payment.getCode());
    }

    private void publishPaymentExecutedEvent(PaymentAggregate payment,PaymentTransactionEntity transaction) {
        PaymentExecutedEvent event = PaymentExecutedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .resellerId(payment.getResellerId())
                .paymentType(payment.getPaymentType()) // PaymentType.REFUND
                .paymentStatus(payment.getPaymentStatus())
                .paidAmount(payment.getPaidAmount())
                .pendingAmount(payment.getPendingAmount())
                // 流水明细信息
                .transactionId(transaction.getId())
                .transactionAmount(transaction.getTransactionAmount())
                .transactionStatus(transaction.getTransactionStatus())
                .paymentChannel(transaction.getPaymentChannel())
                .channelTransactionNumber(transaction.getChannelTransactionNumber())
                .executedTime(transaction.getCompletedTime())
                .build();

        eventPublisher.publishEvent(event);
        log.debug("已发布支付已执行事件，支付单ID: {}", payment.getId());
    }

    private void publishPaymentCompletedEvent(PaymentAggregate payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getCode())
                .orderId(payment.getOrderId())
                .resellerId(payment.getResellerId())
                .paymentAmount(payment.getPaymentAmount())
                .paidAmount(payment.getPaidAmount())
                .paymentType(payment.getPaymentType())
                .relatedBusinessId(payment.getRelatedBusinessId())
                .completedTime(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);
        log.debug("已发布支付单完全完成事件，支付单ID: {}, 支付单号: {}", payment.getId(), payment.getCode());
    }

    private void publishPaymentClosedEvent(PaymentAggregate payment, String closeReason) {
        PaymentClosedEvent event = PaymentClosedEvent.builder()
                .paymentId(payment.getId())
                .paymentStatus(payment.getPaymentStatus())
                .closeReason(closeReason)
                .orderId(payment.getOrderId())
                .paymentAmount(payment.getPaymentAmount())
                .paymentType(payment.getPaymentType())
                .paidAmount(payment.getPaidAmount())
                .closeTime(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);
        log.debug("已发布支付单关闭事件，支付单ID: {}", payment.getId());
    }
}