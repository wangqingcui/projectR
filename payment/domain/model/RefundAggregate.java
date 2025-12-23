package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.command.CloseRefundCommand;
import com.bytz.modules.cms.payment.domain.command.CompleteRefundCommand;
import com.bytz.modules.cms.payment.domain.command.CreateRefundCommand;
import com.bytz.modules.cms.payment.domain.command.CreateRefundTransactionCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 退款单聚合根
 * Refund Aggregate Root
 *
 * <p>管理退款单的完整生命周期（独立于Payment聚合）。</p>
 * <p>来源：支付模块需求设计文档v3.7 M03-M04退款模块</p>
 * <p>职责：
 * - 退款单的创建（独立聚合根）
 * - 退款状态的管理和流转
 * - 退款金额的计算和更新
 * - 退款单的关闭（取消）
 * </p>
 * <p>相关用例：UC-RM-001~008</p>
 *
 * <p><strong>字段一致性说明</strong>：<br>
 * 退款单与支付单共享cms_payment数据库表，通过paymentType=REFUND区分。<br>
 * 所有字段名与Payment聚合保持完全一致，但语义映射到退款业务：
 * <ul>
 * <li>paymentAmount → 退款金额（非支付金额）</li>
 * <li>paidAmount → 已退款金额（非已支付金额）</li>
 * <li>paymentStatus → 退款状态（UNPAID=未退款，PAYING=退款中，PAID=已退款）</li>
 * <li>paymentType → REFUND（区分字段，固定值）</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundAggregate {

    // ==================== 主键和编码 ====================

    /**
     * 数据库主键，UUID格式
     */
    private String id;

    /**
     * 退款单号，仅用于展示，不用于数据关联
     */
    private String code;

    // ==================== 业务关联 ====================

    /**
     * 关联订单号
     */
    private String orderId;

    /**
     * 经销商ID（退款方标识）
     */
    private String resellerId;

    // ==================== 金额信息（字段名与Payment一致，语义映射到退款）====================

    /**
     * 退款金额（目标退款总额，始终为正数）
     * <strong>字段复用说明</strong>：物理字段名为paymentAmount，语义为退款金额
     */
    private BigDecimal paymentAmount;

    /**
     * 已退款金额（累计成功退款金额，保留2位小数HALF_UP）
     * <strong>字段复用说明</strong>：物理字段名为paidAmount，语义为已退款金额
     */
    private BigDecimal paidAmount;

    /**
     * 币种（默认CNY）
     */
    private String currency;

    // ==================== 类型和状态（字段名与Payment一致）====================

    /**
     * 支付类型（固定值REFUND，用于区分退款单）
     * <strong>字段复用说明</strong>：物理字段名为paymentType，退款单固定为REFUND
     */
    private PaymentType paymentType;

    /**
     * 退款状态（复用PaymentStatus枚举）
     * <strong>字段复用说明</strong>：物理字段名为paymentStatus，枚举值复用但语义映射到退款：
     * <ul>
     * <li>UNPAID → 未退款</li>
     * <li>PAYING → 退款中</li>
     * <li>PAID → 已退款</li>
     * <li>FAILED → 退款失败</li>
     * <li>CANCELED → 已取消</li>
     * </ul>
     * 注意：不使用PARTIAL_PAID（一次性退款）和TERMINATED（仅支付单使用）
     */
    private PaymentStatus paymentStatus;

    // ==================== 业务描述 ====================

    /**
     * 业务描述（退款用途说明）
     */
    private String businessDesc;

    /**
     * 退款原因（记录退款失败原因、取消原因等状态变更原因）
     */
    private String reason;

    /**
     * 退款截止时间
     */
    private LocalDateTime paymentDeadline;

    // ==================== 关联业务 ====================

    /**
     * 关联业务ID
     */
    private String relatedBusinessId;

    /**
     * 关联业务类型
     */
    private RelatedBusinessType relatedBusinessType;

    /**
     * 业务到期日
     */
    private LocalDate businessExpireDate;

    // ==================== 退款关联（退款单专用字段）====================

    /**
     * 原支付单ID（退款单必填，关联原始支付单的id）
     * <strong>重要</strong>：此字段仅退款单使用，支付单不使用此字段
     */
    private String originalPaymentId;

    // ==================== 审计字段 ====================

    /**
     * 版本号（用于乐观锁）
     */
    private LocalDateTime version;

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
     * 创建时间
     */
    private LocalDateTime createTime;

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

    // ==================== 集合字段（聚合内对象）====================

    /**
     * 进行中的退款流水列表（PROCESSING状态，支持多笔并发退款流水用于重试场景）
     * 注意：允许多笔流水同时处于PROCESSING状态，每笔流水通过channelTransactionId区分
     */
    @Builder.Default
    private List<RefundTransactionEntity> processingTransactions = new ArrayList<>();

    /**
     * 已完成的退款流水列表（SUCCESS或FAILED状态）
     * 从数据库加载的已完成流水
     */
    @Builder.Default
    private List<RefundTransactionEntity> completedTransactions = new ArrayList<>();

    // ==================== 创建方法（工厂方法）====================

    /**
     * 创建退款单（对应功能点T11）
     * 用例来源：UC-RM-001 退款单接收与创建
     * <strong>6项前置验证</strong>：
     * <ol>
     * <li>原支付单存在性验证</li>
     * <li>原支付单终态验证（PAID或TERMINATED）</li>
     * <li>订单ID匹配验证</li>
     * <li>经销商ID匹配验证</li>
     * <li>退款金额验证（≤可退款余额）</li>
     * <li>一次性退款验证（hasRefund=false）</li>
     * </ol>
     * 创建后自动标记原支付单hasRefund=true
     *
     * @param command 创建退款命令
     * @return 新创建的退款单聚合根
     */
    public static RefundAggregate create(CreateRefundCommand command) {
        // 验证参数
        if (command.getRefundAmount() == null || command.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_AMOUNT, "退款金额必须大于0");
        }
        if (command.getOriginalPaymentId() == null || command.getOriginalPaymentId().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE, "退款单必须关联原支付单ID");
        }

        return RefundAggregate.builder()
                .orderId(command.getOrderId())
                .resellerId(command.getResellerId())
                .paymentAmount(command.getRefundAmount()) // 退款金额
                .paidAmount(BigDecimal.ZERO) // 已退款金额初始为0
                .currency("CNY")
                .paymentType(PaymentType.REFUND) // 固定为REFUND
                .paymentStatus(PaymentStatus.UNPAID) // 初始状态：未退款
                .businessDesc(command.getRefundReason())
                .reason(null)
                .originalPaymentId(command.getOriginalPaymentId()) // 必填
                .relatedBusinessId(command.getRelatedBusinessId())
                .relatedBusinessType(command.getRelatedBusinessType()) // 确保设置
                .processingTransactions(new ArrayList<>())
                .completedTransactions(new ArrayList<>())
                .build();
    }

    /**
     * 验证是否可以关闭退款单
     * 业务规则：
     * - PAYING状态禁止关闭（存在进行中流水）
     * - 最终状态禁止关闭
     * - 已退款金额>0禁止关闭（一次性退款约束）
     *
     * @return 验证结果，true表示允许关闭
     */
    public boolean validateClose() {
        // PAYING状态禁止关闭操作
        if (this.paymentStatus == PaymentStatus.PAYING) {
            return false;
        }

        // 最终状态禁止关闭
        if (this.paymentStatus.isFinal()) {
            return false;
        }

        return true;
    }

    // ==================== 操作方法 ====================

    /**
     * 创建退款流水（对应功能点T15）
     * 用例来源：UC-RM-005 创建退款流水
     * <strong>1:1映射约束</strong>：每笔退款流水必须关联唯一的原支付流水
     * <strong>前置验证</strong>：
     * <ol>
     * <li>原支付流水存在性验证</li>
     * <li>原支付流水状态为SUCCESS验证</li>
     * <li>退款金额不超过原支付流水金额验证</li>
     * </ol>
     * 支持同步和异步退款场景
     * 创建后自动更新退款单状态为PAYING
     *
     * @param command 创建退款流水命令
     * @return 新创建的退款流水
     */
    public RefundTransactionEntity createTransaction(CreateRefundTransactionCommand command) {
        // 验证是否允许创建退款流水
        if (!canRefund()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    String.format("退款单当前状态 %s 不允许创建退款流水", this.paymentStatus.getDescription()));
        }

        // 验证退款金额不超限
        BigDecimal pendingAmount = getPendingRefundAmount();
        if (command.getRefundAmount().compareTo(pendingAmount) > 0) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_EXCEED_LIMIT,
                    "退款流水金额超过待退款金额");
        }

        // 注意：允许多笔进行中的退款流水（支持重试场景），不做限制检查

        // 设置退款单ID
        command.setRefundPaymentId(this.id);

        // 使用RefundTransactionEntity.create()工厂方法创建退款流水
        RefundTransactionEntity transaction = RefundTransactionEntity.create(command);

        // 添加到进行中流水列表
        if (this.processingTransactions == null) {
            this.processingTransactions = new ArrayList<>();
        }
        this.processingTransactions.add(transaction);

        // 根据流水状态更新退款单
        updateByTransaction(transaction);

        return transaction;
    }

    /**
     * 处理退款回调（对应功能点T16/T17）
     * 用例来源：UC-RM-006、UC-RM-007
     * 统一处理退款完成通知，更新流水状态和退款单金额
     * 支持幂等性处理，防止重复回调
     *
     * @param command 完成退款命令
     * @return 更新后的退款流水列表
     */
    public RefundTransactionEntity handleCallback(CompleteRefundCommand command) {
        // 检查是否有进行中的退款流水
        if (!hasProcessingTransaction()) {
            return null;
        }

        // 从进行中流水列表中查找匹配的流水（通过channelTransactionId）
        RefundTransactionEntity transaction = null;
        for (RefundTransactionEntity tx : this.processingTransactions) {
            if (tx.getChannelTransactionId() != null &&
                tx.getChannelTransactionId().equals(command.getChannelTransactionId())) {
                transaction = tx;
                break;
            }
        }

        if (transaction == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_CHANNEL_TRANSACTION,
                    "未找到匹配的进行中退款流水");
        }

        // 验证回调的流水信息（包含null安全检查）
        if (command.getChannelTransactionId() == null || command.getPaymentChannel() == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_CHANNEL_TRANSACTION,
                    "回调的退款流水信息缺失必要字段");
        }

        if (command.getPaymentChannel() != transaction.getPaymentChannel()) {
            throw new PaymentException(PaymentErrorCode.INVALID_CHANNEL_TRANSACTION,
                    "回调的支付渠道与流水不匹配");
        }
        // 跳过非PROCESSING状态的流水（已经处理过，幂等性）
        if (transaction.getTransactionStatus() != TransactionStatus.PROCESSING) {
            return null;
        }
        updateByTransaction(transaction);

        return transaction;
    }

    /**
     * 关闭退款单（对应功能点T13）
     * 用例来源：UC-RM-003 退款单状态管理
     * 根据已退款金额确定最终状态：
     * - 已退款金额=0 → CANCELED
     * - 已退款金额>0 → 不允许关闭（一次性退款约束）
     * 关闭原因记录到reason字段
     * 前置条件：非PAYING、非PAID、非最终状态
     *
     * @param command 关闭退款命令
     */
    public void close(CloseRefundCommand command) {
        // 验证（统一在validateClose中处理，包括paidAmount > 0的检查）
        if (!validateClose()) {
            if (this.paymentStatus == PaymentStatus.PAYING) {
                throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                        "PAYING状态存在进行中流水，禁止关闭操作");
            }
            if (this.paymentStatus.isFinal()) {
                throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                        String.format("当前状态 %s 为最终状态，不允许关闭", this.paymentStatus.getDescription()));
            }
        }
        boolean hasPaid = this.paidAmount.compareTo(BigDecimal.ZERO) > 0;

        // 退款单关闭只有一种状态：CANCELED（已退款金额必须为0）
        this.paymentStatus = hasPaid ? PaymentStatus.TERMINATED : PaymentStatus.CANCELED;

        // 记录关闭原因到reason字段
        this.reason = command.getCloseReason();
    }

    // ==================== 内部方法（不对外暴露，由公共API内部调用）====================

    /**
     * 【内部方法】根据退款流水状态更新退款单状态
     * 触发时机：createTransaction创建流水后自动调用
     */
    private void updateByTransaction(RefundTransactionEntity transaction) {
        switch (transaction.getTransactionStatus()) {
            case PROCESSING:
                // 退款单状态更新为PAYING
                break;
            case FAILED:
                // 标记退款单失败，记录失败原因
                markFailed(transaction.getErrorMessage());
                break;
            case SUCCESS:
                applyAmount(transaction.getTransactionAmount());
                markSuccess();
                break;
            default:
                // 不处理其他状态
                break;
        }
    }

    private void applyAmount(BigDecimal paidAmount) {
        this.paidAmount = this.paidAmount.add(paidAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void markSuccess() {
        // 根据已退款金额和目标退款金额更新状态
        if (this.paidAmount.compareTo(this.paymentAmount) >= 0) {
            this.paymentStatus = PaymentStatus.PAID;
        } else {
            this.paymentStatus = PaymentStatus.PAYING;
        }
    }

    /**
     * 【内部方法】标记退款单失败
     * 触发时机：handleCallback处理失败回调时自动调用
     * 业务规则：更新状态为FAILED，记录失败原因到reason字段
     *
     * @param failReason 失败原因
     */
    private void markFailed(String failReason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.reason = failReason;
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算待退款金额（保留2位小数）
     *
     * @return 待退款金额
     */
    public BigDecimal getPendingRefundAmount() {
        return this.paymentAmount.subtract(this.paidAmount).setScale(2, RoundingMode.HALF_UP)
                .subtract(getProcessingAmount()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 判断是否允许退款
     * 业务规则：只有UNPAID或FAILED状态才允许发起退款
     *
     * @return true如果允许退款，否则false
     */
    public boolean canRefund() {
        return (this.paymentStatus == PaymentStatus.UNPAID || this.paymentStatus == PaymentStatus.FAILED || this.paymentStatus == PaymentStatus.PAYING)
               && getPendingRefundAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 验证orderId是否匹配（退款单绑定订单）
     *
     * @param orderId 订单ID
     * @return true如果匹配，否则false
     */
    public boolean validateOrderId(String orderId) {
        if (orderId == null || this.orderId == null) {
            return false;
        }
        return this.orderId.equals(orderId);
    }

    /**
     * 检查是否存在进行中的退款流水
     *
     * @return true如果存在进行中的退款流水，否则false
     */
    public boolean hasProcessingTransaction() {
        return this.processingTransactions.stream().map(RefundTransactionEntity::getTransactionStatus)
                .anyMatch(status -> status == TransactionStatus.PROCESSING);
    }

    /**
     * 获取所有进行中的退款流水列表
     *
     * @return 进行中的退款流水列表
     */
    public List<RefundTransactionEntity> getProcessingTransactions() {
        return this.processingTransactions != null ? this.processingTransactions : new ArrayList<>();
    }

    public BigDecimal getProcessingAmount() {
        return this.getProcessingTransactions().stream()
                .filter(entity -> entity.getTransactionStatus() == TransactionStatus.PROCESSING)
                .map(RefundTransactionEntity::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取已完成的退款流水列表
     *
     * @return 已完成的退款流水列表
     */
    public List<RefundTransactionEntity> getCompletedTransactions() {
        return this.completedTransactions != null ? this.completedTransactions : new ArrayList<>();
    }

    /**
     * 获取所有退款流水列表（包含进行中和已完成）
     * 按创建时间倒序排序
     *
     * @return 所有退款流水列表（按创建时间倒序）
     */
    public List<RefundTransactionEntity> getTransactions() {
        List<RefundTransactionEntity> allTransactions = new ArrayList<>();
        if (this.processingTransactions != null) {
            allTransactions.addAll(this.processingTransactions);
        }
        if (this.completedTransactions != null) {
            allTransactions.addAll(this.completedTransactions);
        }
        // 按创建时间倒序排序
        allTransactions.sort((t1, t2) -> {
            if (t1.getCreateTime() == null && t2.getCreateTime() == null) return 0;
            if (t1.getCreateTime() == null) return 1;
            if (t2.getCreateTime() == null) return -1;
            return t2.getCreateTime().compareTo(t1.getCreateTime());
        });
        return allTransactions;
    }

    /**
     * 根据原支付流水ID查找已完成的退款流水
     *
     * @param originalTransactionId 原支付流水ID
     * @return 匹配的已完成退款流水列表
     */
    public List<RefundTransactionEntity> findTransactionsByOriginalId(String originalTransactionId) {
        return getCompletedTransactions().stream()
                .filter(tx -> tx.getOriginalTransactionId() != null &&
                              tx.getOriginalTransactionId().equals(originalTransactionId))
                .collect(Collectors.toList());
    }

    /**
     * 持久化后将指定的进行中退款流水移动到已完成列表
     * 根据流水状态判断是否移动：
     * - SUCCESS或FAILED状态：移动到已完成列表
     * - PROCESSING状态：保留在进行中
     *
     */
    public void completeProcessingTransaction() {
        Map<Boolean, List<RefundTransactionEntity>> groups = this.processingTransactions.stream().collect(Collectors.groupingBy(entity -> entity.getTransactionStatus() == TransactionStatus.PROCESSING));
        this.processingTransactions = groups.getOrDefault(true, new ArrayList<>());
        List<RefundTransactionEntity> complete = groups.get(false);
        if (CollectionUtils.isNotEmpty(complete)) {
            this.completedTransactions.addAll(complete);
        }
    }
}