package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.command.CreateRefundTransactionCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款流水实体
 * Refund Transaction Entity
 *
 * <p>退款流水实体，记录单笔退款操作的详细信息（RefundAggregate的子实体）。</p>
 * <p>来源：支付模块需求设计文档v3.7 M04退款执行服务</p>
 * <p>职责：
 * - 记录单笔退款操作的详细信息
 * - 管理退款流水的状态（PROCESSING/SUCCESS/FAILED）
 * - 关联原支付流水（1:1映射）
 * </p>
 * <p>相关用例：UC-RM-005~007</p>
 * 
 * <p><strong>字段一致性说明</strong>：<br>
 * 退款流水与支付流水共享cms_payment_transaction数据库表，通过transactionType=REFUND区分。<br>
 * 所有字段名与PaymentTransaction实体保持完全一致，但语义映射到退款业务：
 * <ul>
 * <li>paymentId → 退款单ID（非支付单ID）</li>
 * <li>transactionAmount → 退款金额（非支付金额）</li>
 * <li>transactionStatus → 退款流水状态</li>
 * <li>transactionType → REFUND（区分字段，固定值）</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundTransactionEntity {

    // ==================== 主键和编码 ====================

    /**
     * 数据库主键，UUID格式
     */
    private String id;

    /**
     * 退款流水号
     */
    private String code;

    // ==================== 退款单关联（字段名与PaymentTransaction一致）====================

    /**
     * 退款单ID（关联Refund聚合根）
     * <strong>字段复用说明</strong>：物理字段名为paymentId，语义为退款单ID
     */
    private String paymentId;

    // ==================== 金额和渠道（字段名与PaymentTransaction一致）====================

    /**
     * 退款金额（本次退款流水金额，始终为正数）
     * <strong>字段复用说明</strong>：物理字段名为transactionAmount，语义为退款金额
     */
    private BigDecimal transactionAmount;

    /**
     * 退款渠道（线上支付/钱包支付/电汇支付/信用账户）
     */
    private PaymentChannel paymentChannel;

    // ==================== 流水状态和类型（字段名与PaymentTransaction一致）====================

    /**
     * 退款流水状态（PROCESSING/SUCCESS/FAILED）
     * <strong>字段复用说明</strong>：物理字段名为transactionStatus，枚举值复用
     */
    private TransactionStatus transactionStatus;

    /**
     * 流水类型（固定值REFUND，用于区分退款流水）
     * <strong>字段复用说明</strong>：物理字段名为transactionType，退款流水固定为REFUND
     */
    private TransactionType transactionType;

    // ==================== 渠道交易信息 ====================

    /**
     * 渠道交易记录ID（渠道侧唯一标识）
     */
    private String channelTransactionId;

    /**
     * 渠道流水号（渠道返回的交易流水号）
     */
    private String channelTransactionNumber;

    // ==================== 退款关联（退款流水专用字段）====================

    /**
     * 原支付流水ID（关联原始支付流水的id）
     * <strong>重要</strong>：此字段仅退款流水使用，支付流水不使用此字段
     * <strong>1:1映射约束</strong>：每笔退款流水必须关联唯一的原支付流水
     */
    private String originalTransactionId;

    // ==================== 时间信息 ====================

    /**
     * 创建时间（流水发起时间）
     */
    private LocalDateTime createTime;

    /**
     * 完成时间（渠道回调成功或失败的时间）
     */
    private LocalDateTime completedTime;

    // ==================== 失败信息 ====================

    /**
     * 错误信息（退款失败时的详细错误描述）
     */
    private String errorMessage;

    // ==================== 业务信息 ====================

    /**
     * 业务单号（业务系统的单据号，如退款申请单号）
     */
    private String businessOrderId;

    /**
     * 业务备注（业务系统的备注信息）
     */
    private String businessRemark;

    /**
     * 过期时间（同步退款场景使用）
     */
    private LocalDateTime expirationTime;

    // ==================== 审计字段 ====================

    /**
     * 删除标志（0-正常，1-删除）
     */
    private Integer delFlag;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 创建人姓名
     */
    private String createByName;

    /**
     * 更新人ID
     */
    private String updateBy;

    /**
     * 更新人姓名
     */
    private String updateByName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 行为方法（仅供聚合根内部调用）====================

    /**
     * 创建退款流水（支持同步和异步场景）
     * 用例来源：UC-RM-005 创建退款流水
     *
     * <p>Transaction内部处理状态逻辑：
     * - 如果command中transactionStatus为null，默认PROCESSING（异步场景）
     * - 如果command中transactionStatus为SUCCESS/FAILED，直接使用（同步场景）
     * - 同步场景下会自动设置completedTime
     * </p>
     *
     * @param command 创建流水命令
     * @return 新创建的退款流水
     */
    public static RefundTransactionEntity create(CreateRefundTransactionCommand command) {
        // 验证参数
        if (StringUtils.isNotBlank(command.getRefundPaymentId())) {
            throw new PaymentException(
                    PaymentErrorCode.INVALID_PARAMETER,
                    "退款单ID不能为空");
        }
        if (command.getRefundAmount() == null || command.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(
                    PaymentErrorCode.INVALID_AMOUNT,
                    "退款金额必须大于0");
        }
        if (command.getPaymentChannel() == null) {
            throw new PaymentException(
                    PaymentErrorCode.INVALID_PARAMETER,
                    "支付渠道不能为空");
        }

        // 确定流水状态：如果command中有指定，使用指定的；否则默认PROCESSING
        TransactionStatus status = command.getTransactionStatus() != null ?
                command.getTransactionStatus() : TransactionStatus.PROCESSING;

        RefundTransactionEntity transaction = RefundTransactionEntity.builder()
                .paymentId(command.getRefundPaymentId())
                .transactionStatus(status)
                .transactionAmount(command.getRefundAmount())
                .paymentChannel(command.getPaymentChannel())
                .channelTransactionId(command.getChannelTransactionId())
                .channelTransactionNumber(command.getChannelTransactionNumber())
                .transactionType(TransactionType.REFUND)
                .originalTransactionId(command.getOriginalTransactionId())
                .expirationTime(command.getExpirationTime())
                .businessRemark(command.getBusinessRemark())
                .createTime(LocalDateTime.now())
                .build();

        // 根据状态处理
        if (status == TransactionStatus.SUCCESS) {
            transaction.setCompletedTime(command.getCompletedTime() != null ?
                    command.getCompletedTime() : LocalDateTime.now());
        } else if (status == TransactionStatus.FAILED) {
            transaction.setCompletedTime(command.getCompletedTime() != null ? 
                    command.getCompletedTime() : LocalDateTime.now());
            transaction.setErrorMessage(command.getErrorMessage());
        }

        return transaction;
    }

    /**
     * 标记流水成功（对应功能点T06）
     * 用例来源：UC-RM-006 退款完成确认
     * 状态转换：PROCESSING → SUCCESS
     *
     * <p>注意：此方法仅供聚合根内部调用，外部请使用
     * {@link RefundAggregate#handleCallback}，流水状态变更应通过聚合根统一管理</p>
     *
     * @param completedTime 完成时间
     * @param channelTransactionNumber 渠道流水号（可选）
     * @return 更新后的流水
     */
    public RefundTransactionEntity success(LocalDateTime completedTime, String channelTransactionNumber) {
        if (this.transactionStatus != TransactionStatus.PROCESSING) {
            throw new PaymentException(
                   PaymentErrorCode.INVALID_TRANSACTION_STATE,
                    "只有PROCESSING状态的流水才能标记为成功");
        }

        this.transactionStatus = TransactionStatus.SUCCESS;
        this.completedTime = completedTime != null ? completedTime : LocalDateTime.now();

        if (channelTransactionNumber != null) {
            this.channelTransactionNumber = channelTransactionNumber;
        }

        return this;
    }

    /**
     * 标记流水失败（对应功能点T06）
     * 状态转换：PROCESSING → FAILED
     *
     * <p>注意：此方法仅供聚合根内部调用，外部请使用
     * {@link RefundAggregate#handleCallback}，流水状态变更应通过聚合根统一管理</p>
     *
     * @param completedTime 完成时间
     * @param errorMessage 错误信息
     * @param channelTransactionNumber 渠道流水号（可选）
     * @return 更新后的流水
     */
    public RefundTransactionEntity fail(LocalDateTime completedTime, String errorMessage, String channelTransactionNumber) {
        if (this.transactionStatus != TransactionStatus.PROCESSING) {
            throw new com.bytz.modules.cms.payment.shared.exception.PaymentException(
                    com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode.INVALID_TRANSACTION_STATE,
                    "只有PROCESSING状态的流水才能标记为失败");
        }

        this.transactionStatus = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedTime = completedTime != null ? completedTime : LocalDateTime.now();

        if (channelTransactionNumber != null) {
            this.channelTransactionNumber = channelTransactionNumber;
        }
        return this;
    }

    // ==================== 辅助方法（查询方法，不涉及状态变更）====================

    /**
     * 判断流水是否成功
     */
    public boolean isSuccess() {
        return TransactionStatus.SUCCESS.equals(this.transactionStatus);
    }

    /**
     * 判断流水是否失败
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.transactionStatus);
    }

    /**
     * 判断流水是否处理中
     */
    public boolean isProcessing() {
        return TransactionStatus.PROCESSING.equals(this.transactionStatus);
    }

    /**
     * 判断是否为退款流水
     */
    public boolean isRefundTransaction() {
        return TransactionType.REFUND.equals(this.transactionType);
    }
}