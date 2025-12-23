package com.bytz.modules.cms.payway.wallet.domain.model;

import com.bytz.modules.cms.payway.wallet.domain.enums.WalletStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionType;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;
import com.bytz.modules.cms.payway.wallet.shared.exception.WalletBusinessException;
import com.bytz.modules.cms.payway.wallet.shared.exception.WalletErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包聚合根
 * Wallet Aggregate
 * <p>
 * 管理经销商的票据钱包余额、状态与交易记录
 * 需求来源：票据钱包需求 - 票据钱包创建和管理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletAggregate {

    /**
     * 钱包ID（唯一标识，32位字符，全局唯一）
     */
    private String id;

    public void setId(String id) {
        this.id = id;
        if (this.currentTransaction != null) this.currentTransaction.setWalletId(this.id);
    }

    /**
     * 经销商ID（所属经销商标识，32位字符）
     */
    private String resellerId;

    /**
     * 当前余额（6位小数，可以为负数）
     */
    private BigDecimal balance;

    /**
     * 钱包状态枚举：DISABLED(停用)/ENABLED(启用)
     */
    private WalletStatus status;

    /**
     * 版本号（用于乐观锁）
     */
    private LocalDateTime version;

    /**
     * 删除状态（0-正常，1-删除）
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
    private LocalDateTime createdTime;

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
    private LocalDateTime updatedTime;

    /**
     * 当前交易（运行期只会有一个交易，成功或失败）
     */
    private WalletTransactionValueObject currentTransaction;

    /**
     * 创建钱包
     * <p>
     * 业务规则：每个经销商只能有一个钱包，钱包创建时默认状态为DISABLED，币种固定为CNY
     */
    public static WalletAggregate create(String resellerId) {
        // 实现创建钱包的业务逻辑
        return WalletAggregate.builder()
                .resellerId(resellerId)
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.DISABLED)
                .build();
    }

    /**
     * 启用钱包
     * <p>
     * 状态转换：DISABLED → ENABLED
     */
    public void enable() {
        // 实现启用钱包的业务逻辑
        this.status = WalletStatus.ENABLED;
    }

    /**
     * 停用钱包
     * <p>
     * 状态转换：ENABLED → DISABLED
     */
    public void disable() {
        // 实现停用钱包的业务逻辑
        this.status = WalletStatus.DISABLED;
    }

    /**
     * 充值操作
     * <p>
     * 金额计算：balance += amount
     * 支持负值充值用于冲销，负值充值可以导致余额为负数
     */
    public WalletTransactionValueObject recharge(BigDecimal amount, String remark) {
        // 实现充值操作的业务逻辑
        // 验证充值金额的合法性
        validateRecharge(amount);

        // 记录交易前余额
        BigDecimal balanceBefore = this.balance;

        // 更新余额
        this.balance = this.balance.add(amount);

        // 创建交易记录
        WalletTransactionValueObject transaction = createTransaction(
                WalletTransactionType.RECHARGE, WalletTransactionStatus.SUCCESS, amount, balanceBefore, this.balance, remark);

        // 设置当前交易
        this.currentTransaction = transaction;

        return transaction;
    }

    /**
     * 支付操作
     * <p>
     * 金额计算：balance -= amount
     * 业务规则：余额必须大于等于支付金额，余额不足直接抛出异常
     */
    public WalletTransactionValueObject payment(BigDecimal amount, String remark) {
        // 实现支付操作的业务逻辑
        // 验证支付金额的合法性
        validatePayment(amount);

        // 记录交易前余额
        BigDecimal balanceBefore = this.balance;

        // 扣减余额
        this.balance = this.balance.subtract(amount);

        // 创建交易记录（金额为负数）
        WalletTransactionValueObject transaction = createTransaction(
                WalletTransactionType.PAYMENT, WalletTransactionStatus.SUCCESS, amount.negate(), balanceBefore, this.balance, remark);

        // 设置当前交易
        this.currentTransaction = transaction;

        return transaction;
    }

    /**
     * 退款操作
     * <p>
     * 金额计算：balance += amount
     */
    public WalletTransactionValueObject refund(BigDecimal amount, String reason) {
        // 实现退款操作的业务逻辑
        // 记录交易前余额
        BigDecimal balanceBefore = this.balance;

        // 增加余额
        this.balance = this.balance.add(amount);

        // 创建交易记录
        WalletTransactionValueObject transaction = createTransaction(
                WalletTransactionType.REFUND, WalletTransactionStatus.SUCCESS, amount, balanceBefore, this.balance, reason);

        // 设置当前交易
        this.currentTransaction = transaction;

        return transaction;
    }

    /**
     * 创建交易记录（私有方法）
     */
    private WalletTransactionValueObject createTransaction(
            WalletTransactionType transactionType, WalletTransactionStatus transactionStatus, BigDecimal amount,
            BigDecimal balanceBefore, BigDecimal balanceAfter, String remark) {

        String transactionCode = generateTransactionCode();

        return WalletTransactionValueObject.builder()
                .code(transactionCode)
                .walletId(this.id)
                .transactionType(transactionType)
                .transactionStatus(transactionStatus)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .remark(remark)
                .completedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 生成交易流水号编码
     */
    private String generateTransactionCode() {
        // 实现生成交易流水号编码的业务逻辑
        return "WT" + System.currentTimeMillis();
    }

    /**
     * 校验充值操作的合法性
     * <p>
     * 业务规则：钱包必须存在，充值金额可为正值或负值（冲销）
     */
    public void validateRecharge(BigDecimal amount) {
        // 实现校验充值操作合法性的业务逻辑
        if (amount == null) {
            throw new WalletBusinessException(WalletErrorCode.AMOUNT_REQUIRED, "充值金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new WalletBusinessException(WalletErrorCode.AMOUNT_CANNOT_BE_ZERO, "充值金额不能为0");
        }
    }

    /**
     * 校验支付操作的合法性
     * <p>
     * 业务规则：钱包必须启用，余额必须大于等于支付金额（余额为负数时禁止支付），支付金额必须大于0
     */
    public void validatePayment(BigDecimal amount) {
        // 实现校验支付操作合法性的业务逻辑
        if (this.status != WalletStatus.ENABLED) {
            // 统一为业务异常
            throw new WalletBusinessException(WalletErrorCode.WALLET_STATUS_ERROR);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletBusinessException(WalletErrorCode.AMOUNT_MUST_BE_POSITIVE, "支付金额必须大于0");
        }
        if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new WalletBusinessException(WalletErrorCode.NEGATIVE_BALANCE_FORBIDDEN, "钱包余额为负数，无法支付");
        }
        if (this.balance.compareTo(amount) < 0) {
            // 统一为业务异常
            throw new WalletBusinessException(WalletErrorCode.INSUFFICIENT_BALANCE);
        }
    }

    /**
     * 校验退款操作的合法性
     * <p>
     * 业务规则：钱包必须存在，退款金额必须大于0，不能超过原支付金额
     */
    public void validateRefund(BigDecimal amount, BigDecimal originalAmount) {
        // 实现校验退款操作合法性的业务逻辑
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletBusinessException(WalletErrorCode.AMOUNT_MUST_BE_POSITIVE, "退款金额必须大于0");
        }
        if (originalAmount != null && amount.compareTo(originalAmount) > 0) {
            throw new WalletBusinessException(WalletErrorCode.REFUND_AMOUNT_EXCEEDS_ORIGINAL, "退款金额不能超过原支付金额");
        }
    }
}