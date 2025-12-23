package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.command.*;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.domain.validator.DefaultTransactionAmountValidator;
import com.bytz.modules.cms.payment.domain.validator.DefaultTransactionCommandValidator;
import com.bytz.modules.cms.payment.domain.validator.TransactionAmountValidator;
import com.bytz.modules.cms.payment.domain.validator.TransactionCommandValidator;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付单聚合根
 * Payment Aggregate Root
 *
 * <p>管理支付单的完整生命周期。</p>
 * <p>来源：需求文档第五章数据模型 - 支付单（Payment）表</p>
 * <p>职责：
 * - 支付单的创建（含正向支付和退款支付单）
 * - 支付状态的管理和流转
 * - 支付金额的计算和更新
 * - 支付单的关闭（取消/中止）
 * </p>
 * <p>相关用例：UC-PM-001~008</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAggregate {

    // ==================== 主键和编码 ====================

    /**
     * 数据库主键，UUID格式
     */
    private String id;

    /**
     * 支付单号，仅用于展示，不用于数据关联
     */
    private String code;

    // ==================== 业务关联 ====================

    /**
     * 关联订单号
     */
    private String orderId;

    /**
     * 经销商ID（支付方标识）
     */
    private String resellerId;

    // ==================== 金额信息 ====================

    /**
     * 支付金额（目标支付总额，始终为正数）
     */
    private BigDecimal paymentAmount;

    /**
     * 已支付金额（累计成功支付金额）
     */
    private BigDecimal paidAmount;

    /**
     * 币种（默认CNY）
     */
    private String currency;

    /**
     * 是否存在退款（正向支付单标记是否有退款记录）
     */
    private Boolean hasRefund;

    // ==================== 类型和状态 ====================

    /**
     * 支付类型（预付款/尾款/其他费用/信用还款/退款）
     */
    private PaymentType paymentType;

    /**
     * 支付状态（UNPAID/PAYING/PARTIAL_PAID/PAID/FAILED/CANCELED/TERMINATED）
     */
    private PaymentStatus paymentStatus;

    // ==================== 业务描述 ====================

    /**
     * 业务描述（支付用途说明，来自业务系统）
     */
    private String businessDesc;

    /**
     * 支付单原因（用于记录失败原因、取消原因等支付单自身状态变更原因）
     */
    private String reason;

    /**
     * 支付截止时间
     */
    private LocalDateTime paymentDeadline;

    // ==================== 关联业务 ====================

    /**
     * 关联业务ID（如信用记录ID、提货单ID等）
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

    // ==================== 退款关联 ====================
    // 注意：originalPaymentId字段已移除，该字段仅用于RefundAggregate
    // Payment聚合根不再使用此字段

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
     * 进行中的流水（PROCESSING状态，包含新增和进行中的流水）
     * 业务约束：同一时间只会有一条进行中的流水
     */
    private PaymentTransactionEntity processingTransaction;

    /**
     * 已完成的流水列表（SUCCESS或FAILED状态）
     * 从数据库加载的已完成流水
     */
    @Builder.Default
    private List<PaymentTransactionEntity> completedTransactions = new ArrayList<>();

    // ==================== 验证方法（独立于操作方法，方便外部调用）====================

    /**
     * 验证支付请求（对应功能点T02）
     * 验证规则：状态允许支付、金额不超限、经销商匹配
     *
     * @param command 验证支付命令
     * @return 验证结果，true表示验证通过
     */
    public boolean validatePayment(ValidatePaymentCommand command) {
        // 验证状态允许支付
        if (!canPay()) {
            return false;
        }

        // 验证经销商匹配
        if (!this.resellerId.equals(command.getResellerId())) {
            return false;
        }

        // 验证金额不超过待支付金额
        if (canPayAmount(command.getAmount())) {
            return false;
        }

        return true;
    }

    /**
     * 验证是否可以标记为PAYING状态
     *
     * @return 验证结果，true表示允许
     */
    public boolean validateMarkPaying() {
        return this.paymentStatus.isPayable();
    }

    /**
     * 验证应用支付金额
     * 业务规则：累加后的已支付金额不能超过支付金额
     * 使用默认验证器进行金额校验
     *
     * @return 验证结果，true表示验证通过
     */
    public boolean validateApplyPayment() {
        return validateApplyPayment(new DefaultTransactionAmountValidator());
    }

    /**
     * 验证应用支付金额
     * 业务规则：累加后的已支付金额不能超过支付金额
     * 支持传入自定义验证器以适应不同业务策略
     *
     * @param amountValidator 金额验证器
     * @return 验证结果，true表示验证通过
     */
    public boolean validateApplyPayment(TransactionAmountValidator amountValidator) {
        if (this.processingTransaction == null) {
            return false;
        }
        BigDecimal amount = this.processingTransaction.getTransactionAmount();
        TransactionType type = this.processingTransaction.getTransactionType();

        // 使用验证器进行金额校验（验证失败会抛出异常）
        try {
            amountValidator.validate(amount, type);
        } catch (PaymentException e) {
            return false;
        }

        // 所有金额都是正数，直接累加
        BigDecimal newPaidAmount = this.paidAmount.add(amount).setScale(2, RoundingMode.HALF_UP);
        return newPaidAmount.compareTo(this.paymentAmount) <= 0;
    }

    /**
     * 验证是否可以关闭支付单
     * 业务规则：
     * - PAYING状态禁止关闭（存在进行中流水）
     * - 最终状态禁止关闭
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

    // ==================== 操作方法（不包含验证逻辑）====================

    /**
     * 创建支付单（对应功能点T01）
     * 用例来源：UC-PM-001 支付单接收与创建
     * 仅用于创建正向支付单，退款支付单请通过领域服务createRefund创建
     * 注意：调用前请先调用validateCreateCommand进行验证
     *
     * @param command 创建支付单命令
     * @return 新创建的支付单聚合根
     */
    public static PaymentAggregate create(CreatePaymentCommand command) {

        return PaymentAggregate.builder()
                .orderId(command.getOrderId())
                .resellerId(command.getResellerId())
                .paymentAmount(command.getPaymentAmount())
                .paidAmount(BigDecimal.ZERO)
                .currency("CNY")
                .hasRefund(false)
                .paymentType(command.getPaymentType())
                .paymentStatus(PaymentStatus.UNPAID)
                .businessDesc(command.getBusinessDesc())
                .paymentDeadline(command.getPaymentDeadline())
                .relatedBusinessId(command.getRelatedBusinessId())
                .relatedBusinessType(command.getRelatedBusinessType())
                .businessExpireDate(command.getBusinessExpireDate())
                .processingTransaction(null)
                .completedTransactions(new ArrayList<>())
                .build();
    }

    /**
     * 关闭支付单（对应功能点T04）
     * 用例来源：UC-PM-004 支付单关闭
     * 根据已支付金额确定最终状态：
     * - 已支付金额=0 → CANCELED
     * - 已支付金额>0 → TERMINATED
     * 注意：调用前请先调用validateClose进行验证
     *
     * @param command 关闭支付命令
     */
    public void close(ClosePaymentCommand command) {
        // 验证
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

        // 根据已支付金额确定最终状态
        if (this.paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = PaymentStatus.CANCELED;
        } else {
            this.paymentStatus = PaymentStatus.TERMINATED;
        }

        // 记录关闭原因到reason字段（支付单自身状态变更原因）
        this.reason = command.getCloseReason();
    }

    /**
     * 标记正向支付单存在退款记录
     * 触发时机：退款支付单创建成功后
     * 允许多次调用（支持多次退款场景）
     * 业务规则：只有正向支付单才能标记hasRefund
     */
    public void markHasRefund() {
        // 只验证是否为正向支付单，允许多次调用（支持多次退款）
        if (this.paymentType == PaymentType.REFUND) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    "退款支付单不能标记hasRefund");
        }

        this.hasRefund = true;
    }

    /**
     * 退款关闭时，恢复退款状态
     */
    public void  markNoRefund() {
        if (this.hasRefund) {
            this.hasRefund = false;
        } else {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    "支付单当前未标记有退款记录，不能取消退款标记");
        }
    }

    /**
     * 验证orderId是否匹配（支付单绑定订单）
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

    // ==================== 辅助方法 ====================

    /**
     * 计算待支付金额（保留2位小数）
     *
     * @return 待支付金额
     */
    public BigDecimal getPendingAmount() {
        return this.paymentAmount.subtract(this.paidAmount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 判断是否允许支付
     * 业务规则：只有UNPAID或PARTIAL_PAID或FAILED状态才允许支付
     *
     * @return true如果允许支付，否则false
     */
    public boolean canPay() {
        return this.paymentStatus.isPayable()
               && getPendingAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否允许支付
     * 业务规则：只有UNPAID或PARTIAL_PAID或FAILED状态才允许支付
     *
     * @return true如果允许支付，否则false
     */
    public boolean canPayAmount(BigDecimal amount) {
        return getPendingAmount().compareTo(amount) >= 0;
    }


    /**
     * 判断是否允许关闭
     *
     * @return true如果允许关闭，否则false
     */
    public boolean canClose() {
        return this.paymentStatus.isClosable();
    }

    /**
     * 设置进行中流水
     *
     * @param transaction 支付流水
     */
    public void setProcessingTransaction(PaymentTransactionEntity transaction) {
        this.processingTransaction = transaction;
    }

    /**
     * 设置已完成流水列表
     *
     * @param completedTransactions 已完成流水列表
     */
    public void setCompletedTransactions(List<PaymentTransactionEntity> completedTransactions) {
        this.completedTransactions = completedTransactions != null ? completedTransactions : new ArrayList<>();
    }

    /**
     * 获取所有流水列表（包含进行中和已完成）
     * 按创建时间倒序排序
     *
     * @return 所有流水列表（按创建时间倒序）
     */
    public List<PaymentTransactionEntity> getTransactions() {
        List<PaymentTransactionEntity> allTransactions = new ArrayList<>();
        if (this.processingTransaction != null) {
            allTransactions.add(this.processingTransaction);
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
     * 判断是否允许退款
     * 业务规则：
     * - 非退款支付单
     * - 支付单状态为最终状态
     * - 已支付金额大于0
     *
     * @return true如果允许退款，否则false
     */
    public boolean canRefund() {
        return this.paymentType != PaymentType.REFUND
               && this.paymentStatus.isFinal()
               && this.paidAmount.compareTo(BigDecimal.ZERO) > 0;
    }


    /**
     * 查找指定的支付流水
     * 从所有流水（processing、completed）中查找
     *
     * @param transactionId 流水ID
     * @return 支付流水（可能为null）
     */
    public PaymentTransactionEntity findTransactionById(String transactionId) {
        // 先从processing流水中查找
        if (this.processingTransaction != null &&
            this.processingTransaction.getId() != null &&
            this.processingTransaction.getId().equals(transactionId)) {
            return this.processingTransaction;
        }
        // 再从completed中查找
        if (this.completedTransactions != null) {
            return this.completedTransactions.stream()
                    .filter(t -> t.getId() != null && t.getId().equals(transactionId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }


    /**
     * 检查是否存在进行中的流水
     *
     * @return true如果存在进行中的流水，否则false
     */
    public boolean hasProcessingTransaction() {
        return this.processingTransaction != null;
    }

    /**
     * 获取所有流水列表（包含进行中和已完成）
     * 用例来源：UC-PM-005 创建支付流水
     * 通过聚合根统一创建流水，确保流水操作不脱离聚合根控制
     * 支持同步和异步两种场景
     *
     * @param command 创建流水命令
     * @return 创建的支付流水
     */
    public PaymentTransactionEntity createTransaction(CreateTransactionCommand command) {
        return createTransaction(command, new DefaultTransactionCommandValidator());
    }

    /**
     * 创建支付流水（对应功能点T05）
     * 用例来源：UC-PM-005 创建支付流水
     * 通过聚合根统一创建流水，确保流水操作不脱离聚合根控制
     * 支持传入自定义验证器以适应不同业务策略
     * 支持同步和异步两种场景
     *
     * @param command   创建流水命令
     * @param validator 交易命令验证器
     * @return 创建的支付流水
     */
    public PaymentTransactionEntity createTransaction(CreateTransactionCommand command, TransactionCommandValidator validator) {
        // 验证是否允许创建流水
        if (!canPay()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE,
                    String.format("支付单当前状态 %s 不允许创建流水", this.paymentStatus.getDescription()));
        }
        // 验证金额不超限
        if (!canPayAmount(command.getTransactionAmount())) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_EXCEED_LIMIT,
                    "流水金额超过待支付金额");
        }

        // 检查是否已有进行中的流水
        if (hasProcessingTransaction()) {
            throw new PaymentException(PaymentErrorCode.DUPLICATE_TRANSACTION,
                    "支付单已存在进行中的流水");
        }
        command.setPaymentId(this.id);
        // Transaction内部处理状态逻辑
        PaymentTransactionEntity transaction = PaymentTransactionEntity.create(command, validator);
        this.processingTransaction = transaction;
        updateByTransaction(transaction);

        return transaction;
    }

    /**
     * 根据流水状态更新支付单状态
     * 内部辅助方法
     *
     * @param transaction
     */
    private void updateByTransaction(PaymentTransactionEntity transaction) {
        switch (transaction.getTransactionStatus()) {
            case PROCESSING:
                // 异步场景：设置为支付中，记录进行中流水
                this.paymentStatus = PaymentStatus.PAYING;
                break;
            case SUCCESS:
                // 同步成功场景：直接更新已支付金额和状态
                this.paidAmount = this.paidAmount.add(transaction.getTransactionAmount())
                        .setScale(2, RoundingMode.HALF_UP);
                updatePaymentStatusAfterSuccess();
                break;
            case FAILED:
                // 同步失败场景：标记为失败
                this.paymentStatus = PaymentStatus.FAILED;
                this.reason = transaction.getBusinessRemark();
                break;
            default:
                throw new PaymentException(PaymentErrorCode.INVALID_TRANSACTION_STATE,
                        "无效的流水状态");
        }
    }

    /**
     * 根据已支付金额更新支付单状态
     * 内部辅助方法
     */
    private void updatePaymentStatusAfterSuccess() {
        BigDecimal pendingAmount = this.paymentAmount.subtract(this.paidAmount)
                .setScale(2, RoundingMode.HALF_UP);

        if (pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = PaymentStatus.PAID;
        } else if (pendingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_EXCEED_LIMIT,
                    "已支付金额超过支付金额");
        } else {
            this.paymentStatus = PaymentStatus.PARTIAL_PAID;
        }
    }


    // ==================== 流水状态更新方法（由聚合根统一管理）====================


    /**
     * 将进行中流水移动到已完成列表
     * 根据流水状态判断是否移动：
     * - SUCCESS或FAILED状态：移动到已完成列表
     * - PROCESSING状态：保留在进行中
     */
    public void completeProcessingTransaction() {
        if (this.processingTransaction == null) {
            return;
        }

        // 只有SUCCESS或FAILED状态的流水才移动到已完成列表
        if (this.processingTransaction.isSuccess() || this.processingTransaction.isFailed()) {
            if (this.completedTransactions == null) {
                this.completedTransactions = new ArrayList<>();
            }
            this.completedTransactions.add(this.processingTransaction);
            this.processingTransaction = null;
        }
        // PROCESSING状态的流水保留在进行中，不移动
    }

    // ==================== 回调处理方法（聚合根统一处理）====================

    /**
     * 处理支付完成回调
     * 所有回调业务逻辑封装在聚合根内
     * 包含：验证状态、更新流水状态、更新支付单状态/金额、移动流水到已完成列表
     *
     * @param command 完成支付命令
     * @return 处理后的流水（如果存在），否则返回null
     */
    public PaymentTransactionEntity handleCallback(CompletePaymentCommand command) {
        // 检查是否有进行中的流水
        if (!hasProcessingTransaction()) {
            return null;
        }

        PaymentTransactionEntity transaction = getProcessingTransaction();

        //验证运行时和完成的流水是同一个
        if (!transaction.getChannelTransactionId().equals(command.getChannelTransactionId()) || command.getPaymentChannel() != transaction.getPaymentChannel()) {
            throw new PaymentException(PaymentErrorCode.INVALID_CHANNEL_TRANSACTION,
                    "回调的流水信息与进行中流水不匹配");
        }

        // 跳过非PROCESSING状态的流水（已经处理过）
        if (!transaction.isProcessing()) {
            return null;
        }
        if (Boolean.TRUE.equals(command.getSuccess())) {
            transaction.success(TransactionSuccessCommand.builder()
                    .completedTime(command.getCompletedTime())
                    .channelTransactionNumber(command.getChannelTransactionNumber())
                    .build());
        } else {
            transaction.fail(TransactionFailCommand.builder()
                    .completedTime(command.getCompletedTime())
                    .reason(command.getChannelResponse())
                    .channelTransactionNumber(command.getChannelTransactionNumber())
                    .build());
        }
        updateByTransaction(transaction);
        return transaction;
    }
}