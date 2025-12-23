package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.domain.command.*;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.model.RefundAggregate;
import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.domain.repository.IRefundRepository;
import com.bytz.modules.cms.payment.domain.repository.IRefundTransactionRepository;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 退款领域服务
 * Refund Domain Service
 *
 * <p>职责：
 * - 创建退款单（基于原支付单和原支付流水）
 * - 验证退款请求
 * - 执行退款（创建退款流水并下发渠道）
 * - 处理退款完成回调
 * - 关闭退款单
 * - 查询退款状态
 * </p>
 * <p>用例来源：UC-RM-001~008</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundDomainService {

    private final IRefundRepository refundRepository;
    private final IPaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentCodeGenerator paymentCodeGenerator;
    private final IRefundTransactionRepository refundTransactionRepository;
    private final PaymentChannelRefundServiceRegistry channelRefundServiceRegistry;

    /**
     * 创建退款单（不执行实际退款操作）
     * 用例来源：UC-RM-001 退款单接收与创建
     * 退款必须基于特定的原支付单和原支付流水
     *
     * <p>流程：
     * 1. 验证原支付单和原流水（6项前置验证）
     * 2. 创建退款单
     * 3. 标记原支付单存在退款
     * </p>
     *
     * <p>注意：此方法仅创建退款记录，实际退款操作由应用层或外部系统调用渠道执行</p>
     *
     * @param command 创建退款命令
     * @return 创建的退款单聚合根
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundAggregate createRefund(@Valid CreateRefundCommand command) {
        log.info("创建退款单，原支付单ID: {}, 退款金额: {}",
                command.getOriginalPaymentId(),
                command.getRefundAmount());

        // ========== 6项前置验证 ==========

        // 1. 验证原支付单存在并加载流水
        PaymentAggregate originalPayment = paymentRepository.findById(command.getOriginalPaymentId(), true)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORIGINAL_PAYMENT_NOT_FOUND,
                        "原支付单不存在"));

        // 查询该原支付单是否已有退款记录
        List<RefundAggregate> existingRefunds = refundRepository.findByOriginalPaymentId(command.getOriginalPaymentId(), false);

        // 执行退款前置验证
        validateRefundCreation(originalPayment, existingRefunds, command);

        // 创建退款单聚合根
        RefundAggregate refund = RefundAggregate.create(command);
        refund.setCode(paymentCodeGenerator.generatePaymentCode());

        // 持久化退款单（先保存退款单）
        refund = refundRepository.save(refund);

        // 标记原支付单存在退款（在退款单保存成功后，通过版本控制更新原支付单，保证事务一致性）
        originalPayment.markHasRefund();
        paymentRepository.update(originalPayment);

        // 发布退款单创建事件（复用Payment事件）
        publishPaymentCreatedEvent(refund);

        log.info("退款单创建成功，退款单号: {}", refund.getCode());

        return refund;
    }


    /**
     * 执行退款（创建退款流水）
     * 用例来源：UC-PM-009 退款处理
     * 在退款支付单创建后，由渠道调用创建退款流水
     *
     * @param command 执行退款命令
     * @return 创建的退款流水
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundTransactionEntity executeRefund(@Valid ExecuteRefundCommand command) {
        log.info("执行退款，退款支付单ID: {}, 退款金额: {}",
                command.getRefundPaymentId(), command.getRefundAmount());

        // 查询退款支付单
        RefundAggregate refundPayment = refundRepository.findById(command.getRefundPaymentId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND, "退款支付单不存在"));

        // 验证是退款支付单
        if (refundPayment.getPaymentType() != PaymentType.REFUND) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE, "该支付单不是退款支付单");
        }

        // 验证退款支付单状态允许执行
        if (!refundPayment.canRefund()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    String.format("退款支付单当前状态 %s 不允许执行退款", refundPayment.getPaymentStatus().getDescription()));
        }

        // 查询原支付单（退款支付单必须关联原支付单）
        String originalPaymentId = refundPayment.getOriginalPaymentId();
        if (originalPaymentId == null || originalPaymentId.isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE, "退款支付单未关联原支付单");
        }
        PaymentAggregate originalPayment = paymentRepository.findById(originalPaymentId, true)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORIGINAL_PAYMENT_NOT_FOUND, "原支付单不存在"));

        PaymentTransactionEntity originalTransaction = originalPayment.findTransactionById(command.getOriginalTransactionId());

        validateOriginalTransaction(originalTransaction, refundRepository.findByOriginalPaymentId(originalPaymentId, true), command.getRefundAmount());

        PaymentChannel refundChannel = command.getPaymentChannel() != null ?
                command.getPaymentChannel() : originalTransaction.getPaymentChannel();

        // 构建渠道退款请求
        ChannelRefundRequest channelRequest = ChannelRefundRequest.builder()
                .refundPaymentId(refundPayment.getId())
                .refundPaymentCode(refundPayment.getCode())
                .originalPaymentId(originalPaymentId)
                .originalTransactionId(command.getOriginalTransactionId())
                .originalChannelTransactionId(originalTransaction.getChannelTransactionId())
                .resellerId(originalPayment.getResellerId())
                .orderId(originalPayment.getOrderId())
                .refundAmount(command.getRefundAmount())
                .paymentChannel(refundChannel)
                .refundReason(refundPayment.getReason())
                .relatedBusinessId(refundPayment.getRelatedBusinessId())
                .relatedBusinessType(refundPayment.getRelatedBusinessType())
                .build();

        // 获取渠道退款服务并执行退款
        IPaymentChannelRefundService channelRefundService = channelRefundServiceRegistry.getRefundService(originalTransaction.getPaymentChannel());
        ChannelRefundResponse channelResponse = channelRefundService.executeRefund(channelRequest);


        CreateRefundTransactionCommand createTransactionCommand = CreateRefundTransactionCommand.builder()
                .refundAmount(command.getRefundAmount())
                .paymentChannel(refundChannel)
                .transactionStatus(channelResponse.getStatus())
                .errorMessage(channelResponse.getErrorMessage())
                .channelTransactionId(channelResponse.getChannelTransactionId())
                .channelTransactionNumber(channelResponse.getChannelTransactionNumber())
                .originalTransactionId(originalTransaction.getId())
                .expirationTime(command.getExpirationTime())
                .completedTime(channelResponse.getCompletedTime())
                .businessRemark(command.getBusinessRemark())
                .build();

        // 从这里开始将创建流水的逻辑委托给私有方法
        RefundTransactionEntity refundTransaction = executeRefundInternal(refundPayment, createTransactionCommand);

        // 持久化
        refundRepository.update(refundPayment);

        log.info("退款流水创建成功，流水号: {}", refundTransaction.getCode());
        return refundTransaction;
    }


    // 新增私有方法：从创建流水命令开始处理退款流水的创建（不负责持久化）
    private RefundTransactionEntity executeRefundInternal(RefundAggregate refundPayment, CreateRefundTransactionCommand createTransactionCommand) {


        // 通过聚合根统一创建流水
        RefundTransactionEntity refundTransaction = refundPayment.createTransaction(createTransactionCommand);
        refundTransaction.setCode(paymentCodeGenerator.generateTransactionCode());

        return refundTransaction;
    }

    /**
     * 处理退款回调（异步退款渠道）
     * 用例来源：UC-RM-006 退款完成确认
     * 异步退款渠道完成退款后的回调通知
     *
     * <p>此方法只负责记录退款完成信息，不进行业务验证</p>
     *
     * @param command 退款回调命令
     * @return 更新后的退款流水列表
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundTransactionEntity handleRefundCallback(@Valid RefundCallbackCommand command) {
        log.info("处理退款回调，支付渠道: {}, 渠道交易记录ID: {}, 是否成功: {}",
                command.getPaymentChannel(), command.getChannelTransactionId(), command.getSuccess());

        // 根据paymentChannel+channelTransactionId查找退款流水
        RefundTransactionEntity refundTransaction = refundTransactionRepository.findByChannelTransactionId(
                        command.getChannelTransactionId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.TRANSACTION_NOT_FOUND,
                        String.format("未找到支付渠道[%s]和交易记录ID[%s]对应的退款流水",
                                command.getPaymentChannel(), command.getChannelTransactionId())));
        if (refundTransaction.getTransactionStatus() != TransactionStatus.PROCESSING) {
            return refundTransaction;
        }

        // 根据退款流水找到退款单（不需要加载原支付单信息）
        RefundAggregate refund = refundRepository.findById(refundTransaction.getPaymentId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
                        "退款单不存在"));

        // 构建完成退款命令
        String channelResponse = command.getSuccess() ? null :
                String.format("错误码: %s, 错误信息: %s", command.getErrorCode(), command.getErrorMessage());
        CompleteRefundCommand completeCommand = CompleteRefundCommand.builder()
                .paymentChannel(command.getPaymentChannel())
                .channelTransactionId(command.getChannelTransactionId())
                .channelTransactionNumber(command.getChannelTransactionNumber())
                .success(command.getSuccess())
                .channelResponse(channelResponse)
                .completedTime(command.getCompletedTime())
                .build();

        // 聚合根统一处理回调业务逻辑
        RefundTransactionEntity transaction = refund.handleCallback(completeCommand);

        // 保存更新
        refundRepository.update(refund);


        if (Boolean.TRUE.equals(command.getSuccess())) {
            // 只要成功，就发布执行事件（包含transaction details）
            publishPaymentExecutedEvent(refund, refundTransaction);
            // 退款成功
            if (refund.getPaymentStatus() == PaymentStatus.PAID) {
                publishPaymentCompletedEvent(refund);
            }
        } else {
            // 退款失败，发布执行事件（包含transaction details，status=FAILED区分失败）
            publishPaymentExecutedEvent(refund, refundTransaction);
        }

        log.info("退款回调处理完成，退款单号: {}", refund.getCode());
        return transaction;
    }

    /**
     * 关闭退款单
     * 用例来源：UC-RM-003 退款单状态管理
     *
     * <p>关闭退款单时需要恢复原支付单的hasRefund标记</p>
     *
     * @param command 关闭退款命令
     * @return 关闭后的退款单
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundAggregate closeRefund(@Valid CloseRefundCommand command) {
        log.info("关闭退款单，退款单ID: {}, 关闭原因: {}", command.getRefundPaymentId(), command.getCloseReason());

        // 加载退款单及原支付单信息（需要恢复原支付单的hasRefund标记）
        RefundAggregate refund = refundRepository.findById(command.getRefundPaymentId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
                        "退款单不存在"));

        // 聚合根执行关闭操作
        refund.close(command);
        // 保存更新
        refund = refundRepository.update(refund);

        String originalPaymentId = refund.getOriginalPaymentId();
        Optional<PaymentAggregate> byId = paymentRepository.findById(originalPaymentId);
        PaymentAggregate originalPayment = byId.orElseThrow(() -> new PaymentException(PaymentErrorCode.ORIGINAL_PAYMENT_NOT_FOUND));
        List<RefundAggregate> refundAggregates = refundRepository.findByOriginalPaymentId(originalPaymentId, false);
        List<RefundAggregate> collect = refundAggregates.stream()
                .filter(aggregate -> !aggregate.getId().equals(command.getRefundPaymentId()))
                .filter(payment -> payment.getPaymentStatus() != PaymentStatus.CANCELED)
                .collect(Collectors.toList());

        if (collect.isEmpty()) {
            originalPayment.markNoRefund();
        }
        paymentRepository.update(originalPayment);
        // 恢复原支付单的hasRefund标记


        // 发布关闭事件（复用Payment事件）
        publishPaymentClosedEvent(refund);

        log.info("退款单关闭成功，退款单号: {}", refund.getCode());
        return refund;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证退款创建请求
     * 包含：状态验证、参数匹配验证、未完成退款单验证、余额验证
     */
    public void validateRefundCreation(PaymentAggregate originalPayment, List<RefundAggregate> existingRefunds, CreateRefundCommand command) {
        // 2. 验证原支付单终态（PAID或TERMINATED）
        if (!originalPayment.canRefund()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    String.format("原支付单状态 %s 不允许退款，只有PAID或TERMINATED状态才允许退款",
                            originalPayment.getPaymentStatus().getDescription()));
        }

        // 3. 验证订单ID匹配
        if (!originalPayment.validateOrderId(command.getOrderId())) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    "订单ID与原支付单不匹配");
        }

        // 4. 验证经销商ID匹配
        if (!originalPayment.getResellerId().equals(command.getResellerId())) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    "经销商ID与原支付单不匹配");
        }

        // 5. 验证不存在未完成的退款单
        if (!existingRefunds.isEmpty()) {
            for (RefundAggregate existing : existingRefunds) {
                if (!existing.getPaymentStatus().isFinal()) {
                    throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                            String.format("存在未完成的退款单（状态：%s），请先处理完成后再创建新退款单",
                                    existing.getPaymentStatus().getDescription()));
                }
            }
        }

        // 7. 验证退款金额不超过可退款余额
        BigDecimal refundableAmount = calculateRefundableAmount(originalPayment, existingRefunds);
        if (command.getRefundAmount().compareTo(refundableAmount) > 0) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_EXCEED_LIMIT,
                    String.format("退款金额 %s 超过可退款余额 %s",
                            command.getRefundAmount(), refundableAmount));
        }
    }

    public BigDecimal calculateCanRefund(String paymentId) {
        PaymentAggregate paymentAggregate = paymentRepository.findById(paymentId, true)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORIGINAL_PAYMENT_NOT_FOUND,
                        "原支付单不存在"));
        List<RefundAggregate> existingRefunds = refundRepository.findByOriginalPaymentId(paymentId, true);
        return calculateRefundableAmount(paymentAggregate, existingRefunds);
    }

    /**
     * 计算可退款余额
     * 可退款余额 = 已支付金额 - 已退款金额
     */
    private BigDecimal calculateRefundableAmount(PaymentAggregate originalPayment, List<RefundAggregate> existingRefunds) {

        BigDecimal refundedAmount = BigDecimal.ZERO;
        for (RefundAggregate refund : existingRefunds) {
            refundedAmount = refundedAmount.add(refund.getPaidAmount());
        }

        return originalPayment.getPaidAmount().subtract(refundedAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 验证原支付流水
     * 退款必须基于特定的原支付流水，且该流水必须是成功状态
     */
    public void validateOriginalTransaction(PaymentTransactionEntity originalTransaction, List<RefundAggregate> existRefunds, BigDecimal amount) {
        BigDecimal exist = calculateTransactionCanRefund(originalTransaction, existRefunds);

        // 验证退款金额不超过原流水金额
        if (amount.compareTo(exist) > 0) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_EXCEED_LIMIT,
                    String.format("退款金额 %s 超过原支付流水可退款金额 %s",
                            amount, exist));
        }
    }

    /**
     * 计算原支付流水可退款金额
     */
    public BigDecimal calculateTransactionCanRefund(PaymentTransactionEntity originalTransaction, List<RefundAggregate> existRefunds) {
        List<RefundTransactionEntity> existTransactions = existRefunds.stream().flatMap(aggregate -> aggregate.findTransactionsByOriginalId(originalTransaction.getId()).stream())
                .collect(Collectors.toList());
        BigDecimal all = existTransactions.stream()
                .filter(transaction -> transaction.getTransactionStatus() == TransactionStatus.SUCCESS)
                .map(entity -> entity.getTransactionAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return originalTransaction.getTransactionAmount().subtract(all);
    }

    // ==================== 事件发布方法（复用Payment事件）====================

    /**
     * 发布支付单创建事件（退款复用Payment事件）
     * 退款是PaymentType.REFUND的特殊支付单
     */
    private void publishPaymentCreatedEvent(RefundAggregate refund) {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(refund.getId())
                .paymentCode(refund.getCode())
                .orderId(refund.getOrderId())
                .resellerId(refund.getResellerId())
                .paymentAmount(refund.getPaymentAmount())
                .paymentType(refund.getPaymentType()) // PaymentType.REFUND
                .createTime(refund.getCreateTime())
                .build();
        eventPublisher.publishEvent(event);
        log.debug("发布支付单创建事件（退款）: {}", event);
    }

    /**
     * 发布支付单完成事件（退款完全完成）
     * 当退款单paidAmount达到paymentAmount时发布
     */
    private void publishPaymentCompletedEvent(RefundAggregate refund) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(refund.getId())
                .paymentCode(refund.getCode())
                .orderId(refund.getOrderId())
                .resellerId(refund.getResellerId())
                .paymentAmount(refund.getPaymentAmount())
                .paidAmount(refund.getPaidAmount())
                .paymentType(refund.getPaymentType()) // PaymentType.REFUND
                .completedTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(event);
        log.debug("发布支付单完成事件（退款）: {}", event);
    }

    /**
     * 发布支付执行事件（退款流水执行结果）
     * 用于记录每次退款流水的执行结果（成功或失败）
     */
    private void publishPaymentExecutedEvent(RefundAggregate refund, RefundTransactionEntity transaction) {
        PaymentExecutedEvent event = PaymentExecutedEvent.builder()
                .paymentId(refund.getId())
                .orderId(refund.getOrderId())
                .resellerId(refund.getResellerId())
                .paymentType(refund.getPaymentType()) // PaymentType.REFUND
                .paymentStatus(refund.getPaymentStatus())
                .paidAmount(refund.getPaidAmount())
                .pendingAmount(refund.getPaymentAmount().subtract(refund.getPaidAmount()))
                // 流水明细信息
                .transactionId(transaction.getId())
                .transactionAmount(transaction.getTransactionAmount())
                .transactionStatus(transaction.getTransactionStatus())
                .paymentChannel(transaction.getPaymentChannel())
                .channelTransactionNumber(transaction.getChannelTransactionNumber())
                .executedTime(transaction.getCompletedTime())
                .build();
        eventPublisher.publishEvent(event);
        log.debug("发布支付执行事件（退款流水）: {}", event);
    }

    /**
     * 发布支付单关闭事件（退款单关闭/取消）
     */
    private void publishPaymentClosedEvent(RefundAggregate refund) {
        PaymentClosedEvent event = PaymentClosedEvent.builder()
                .paymentId(refund.getId())
                .paymentCode(refund.getCode())
                .orderId(refund.getOrderId())
                .resellerId(refund.getResellerId())
                .paymentType(refund.getPaymentType()) // PaymentType.REFUND
                .paymentStatus(refund.getPaymentStatus())
                .paidAmount(refund.getPaidAmount())
                .closeReason(refund.getReason())
                .closeTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(event);
        log.debug("发布支付单关闭事件（退款）: {}", event);
    }
}