package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.command.CompletePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.CreateTransactionCommand;
import com.bytz.modules.cms.payment.domain.command.TransactionFailCommand;
import com.bytz.modules.cms.payment.domain.command.TransactionSuccessCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.domain.validator.DefaultTransactionCommandValidator;
import com.bytz.modules.cms.payment.domain.validator.TransactionCommandValidator;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水实体
 * Payment Transaction Entity
 *
 * <p>记录每次支付或退款的渠道交易与状态。</p>
 * <p>来源：需求文档第五章数据模型 - 支付流水（PaymentTransactionEntity）表</p>
 * <p>职责：
 * - 记录支付/退款流水
 * - 管理流水状态
 * - 关联渠道交易信息
 * </p>
 * <p>相关用例：UC-PM-005, UC-PM-006, UC-PM-009</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionEntity {

    // ==================== 主键和编码 ====================

    /**
     * 数据库主键，UUID格式
     */
    private String id;

    /**
     * 流水号，仅用于展示
     */
    private String code;

    // ==================== 关联支付单 ====================

    /**
     * 支付单ID（外键，关联支付单的id）
     */
    private String paymentId;

    // ==================== 状态和金额 ====================

    /**
     * 流水状态（PROCESSING/SUCCESS/FAILED）
     */
    private TransactionStatus transactionStatus;

    /**
     * 交易金额（始终为正数）
     * 支付流水：正数
     * 退款流水：正数
     */
    private BigDecimal transactionAmount;

    // ==================== 渠道信息 ====================

    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;

    /**
     * 渠道交易记录ID（渠道侧唯一标识，用于定位流水）
     */
    private String channelTransactionId;

    /**
     * 渠道交易号（可在支付完成确认时补录）
     */
    private String channelTransactionNumber;

    // ==================== 业务信息 ====================


    // ==================== 退款标识 ====================

    /**
     * 流水类型（PAYMENT/REFUND）
     */
    private TransactionType transactionType;

    // ==================== 时间信息 ====================

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间（成功或失败时记录）
     */
    private LocalDateTime completedTime;

    /**
     * 过期时间
     */
    private LocalDateTime expirationTime;

    // ==================== 其他 ====================

    /**
     * 业务备注
     */
    private String businessRemark;

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
     * 创建支付流水（支持同步和异步场景）
     * 用例来源：UC-PM-005 创建支付流水
     *
     * <p>Transaction内部处理状态逻辑：
     * - 如果command中transactionStatus为null，默认PROCESSING（异步场景）
     * - 如果command中transactionStatus为SUCCESS/FAILED，直接使用（同步场景）
     * - 同步场景下会自动设置completedTime
     * </p>
     *
     * @param command 创建流水命令
     * @return 新创建的支付流水
     */
    public static PaymentTransactionEntity create(CreateTransactionCommand command) {
        return create(command, new DefaultTransactionCommandValidator());
    }

    /**
     * 创建支付流水（支持同步和异步场景，自定义验证器）
     * 用例来源：UC-PM-005 创建支付流水
     *
     * <p>Transaction内部处理状态逻辑：
     * - 如果command中transactionStatus为null，默认PROCESSING（异步场景）
     * - 如果command中transactionStatus为SUCCESS/FAILED，直接使用（同步场景）
     * - 同步场景下会自动设置completedTime
     * </p>
     *
     * @param command   创建流水命令
     * @param validator 交易命令验证器
     * @return 新创建的支付流水
     */
    public static PaymentTransactionEntity create(CreateTransactionCommand command, TransactionCommandValidator validator) {
        // 使用验证器进行参数校验（验证失败会直接抛出异常）
        validator.validate(command);

        TransactionType type = command.getTransactionType() != null ?
                command.getTransactionType() : TransactionType.PAYMENT;

        // 确定流水状态：如果command中有指定，使用指定的；否则默认PROCESSING
        TransactionStatus status = command.getTransactionStatus() != null ?
                command.getTransactionStatus() : TransactionStatus.PROCESSING;

        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                .paymentId(command.getPaymentId())
                .transactionStatus(TransactionStatus.PROCESSING)
                .transactionAmount(command.getTransactionAmount())
                .paymentChannel(command.getPaymentChannel())
                .channelTransactionId(command.getChannelTransactionId())
                .channelTransactionNumber(command.getChannelTransactionNumber())
                .transactionType(type)
                .expirationTime(command.getExpirationTime())
                .businessRemark(command.getBusinessRemark())
                .build();
        switch (status) {
            case PROCESSING:
                // 异步场景，无需额外处理
                break;
            case SUCCESS:
                transaction.success(TransactionSuccessCommand.builder()
                        .completedTime(command.getCompletedTime())
                        .build());
                break;
            case FAILED:
                // 同步失败场景，设置完成时间和错误信息
                transaction.fail(TransactionFailCommand.builder()
                        .completedTime(command.getCompletedTime())
                        .reason(command.getErrorMessage())
                        .channelTransactionNumber(command.getChannelTransactionNumber())
                        .build());
                break;
            default:
                throw new PaymentException(PaymentErrorCode.VALIDATION_FAILED,
                        "无效的流水状态：" + status);
        }
        return transaction;
    }


    /**
     * 标记流水成功（对应功能点T06）
     * 用例来源：UC-PM-006 支付完成确认
     * 状态转换：PROCESSING → SUCCESS
     *
     * <p>注意：此方法仅供聚合根内部调用，外部请使用
     * 或 {@link PaymentAggregate#handleCallback(CompletePaymentCommand)}</p>
     *
     * @param command 流水成功命令
     * @return 更新后的流水
     * {@link PaymentAggregate#handleCallback}，流水状态变更应通过聚合根统一管理
     */
    public PaymentTransactionEntity success(TransactionSuccessCommand command) {
        if (this.transactionStatus != TransactionStatus.PROCESSING) {
            throw new PaymentException(PaymentErrorCode.INVALID_TRANSACTION_STATE,
                    "只有PROCESSING状态的流水才能标记为成功");
        }

        this.transactionStatus = TransactionStatus.SUCCESS;
        this.completedTime = command.getCompletedTime() != null ? command.getCompletedTime() : LocalDateTime.now();

        if (command.getChannelTransactionNumber() != null) {
            this.channelTransactionNumber = command.getChannelTransactionNumber();
        }

        return this;
    }

    /**
     * 标记流水失败（对应功能点T06）
     * 状态转换：PROCESSING → FAILED
     *
     * <p>注意：此方法仅供聚合根内部调用，外部请使用
     * 或 {@link PaymentAggregate#handleCallback(CompletePaymentCommand)} </p>
     *
     * @param command 流水失败命令
     * @return 更新后的流水
     * {@link PaymentAggregate#handleCallback}，流水状态变更应通过聚合根统一管理
     */
    public PaymentTransactionEntity fail(TransactionFailCommand command) {
        if (this.transactionStatus != TransactionStatus.PROCESSING) {
            throw new PaymentException(PaymentErrorCode.INVALID_TRANSACTION_STATE,
                    "只有PROCESSING状态的流水才能标记为失败");
        }

        this.transactionStatus = TransactionStatus.FAILED;
        this.businessRemark = command.getReason();
        this.completedTime = command.getCompletedTime() != null ? command.getCompletedTime() : LocalDateTime.now();

        if (command.getChannelTransactionNumber() != null) {
            this.channelTransactionNumber = command.getChannelTransactionNumber();
        }
        return this;
    }

    /**
     * 判断特定流水是否允许退款
     * 业务规则：
     * - 流水必须存在
     * - 流水状态为SUCCESS
     * - 流水为正向支付流水（非退款流水）
     *
     * @return true如果允许退款，否则false
     */
    public boolean canRefundTransaction() {
        return isSuccess() && isPaymentTransaction();
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
     * 判断是否为支付流水
     */
    public boolean isPaymentTransaction() {
        return TransactionType.PAYMENT.equals(this.transactionType);
    }

    /**
     * 判断是否为退款流水
     */
    public boolean isRefundTransaction() {
        return TransactionType.REFUND.equals(this.transactionType);
    }
}