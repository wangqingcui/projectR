package com.bytz.modules.cms.payway.credit.domain.model;

import com.bytz.modules.cms.payway.credit.domain.command.BatchTemporaryCreditPayCommand;
import com.bytz.modules.cms.payway.credit.domain.command.CompleteRepaymentCommand;
import com.bytz.modules.cms.payway.credit.domain.command.InitiateRepaymentCommand;
import com.bytz.modules.cms.payway.credit.domain.command.TemporaryCreditPayCommand;
import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TemporaryCreditStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TransactionType;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import com.bytz.modules.cms.shared.util.BusinessCodeGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 临时授信聚合根
 * Temporary Credit Aggregate
 *
 * <p>封装临时信用额度管理、支付、还款的核心业务规则。</p>
 * <p>需求来源：需求文档 M02-临时授信管理</p>
 * <p>用例来源：UC-CW-006, UC-CW-007, UC-CW-011, UC-CW-013</p>
 * <p>职责：
 * - 管理临时信用的完整生命周期
 * - 管理临时信用额度和过期时间
 * - 执行临时信用支付和还款操作
 * - 维护额度计算规则
 * - 管理临时信用账单
 * - 保证领域不变量
 * </p>
 * <p>关联关系：
 * - 通过resellerId与CreditWallet聚合根松耦合关联
 * - 创建时不校验钱包状态：临时授信创建不要求钱包存在或启用
 * - 支付时不校验钱包冻结状态：临时授信就是用来在信用不足或冻结时使用的
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryCreditAggregate {

    // ==================== 基本信息 ====================

    /**
     * 临时授信主键ID，UUID，32位字符
     */
    private String id;

    /**
     * 经销商ID
     */
    private String resellerId;

    /**
     * 关联的信用钱包ID
     */
    private String creditWalletId;

    /**
     * PowerApps审批ID，用于幂等性处理
     */
    private String approvalId;

    /**
     * 审批通过时间
     */
    private LocalDateTime approvalTime;

    // ==================== 额度字段 ====================

    /**
     * 临时授信总额（> 0）
     */
    private BigDecimal totalAmount;

    /**
     * 已用金额，支付时累加，还款时不减少（>= 0，初始值0）
     */
    private BigDecimal usedAmount;

    /**
     * 剩余可用金额（作为审批额度快照，不随还款恢复）= totalAmount - usedAmount
     */
    private BigDecimal remainingAmount;

    /**
     * 累计已还款金额（记录每次还款累加值，用于追踪还款进度）
     */
    private BigDecimal repaidAmount;

    // ==================== 状态字段 ====================

    /**
     * 过期日期
     */
    private LocalDate expiryDate;

    /**
     * 状态：APPROVED, IN_USE, EXHAUSTED, EXPIRED（初始值APPROVED）
     */
    private TemporaryCreditStatus status;

    /**
     * 备注（可空，最大500字符）
     */
    private String remark;

    /**
     * 币种（默认CNY）
     */
    private String currency;

    // ==================== 审计字段 ====================

    /**
     * 创建时间，框架自动填充
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，框架自动填充
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID，框架自动填充
     */
    private String createBy;

    /**
     * 创建人姓名，框架自动填充
     */
    private String createByName;

    /**
     * 更新人ID，框架自动填充
     */
    private String updateBy;

    /**
     * 更新人姓名，框架自动填充
     */
    private String updateByName;

    // ==================== 技术字段 ====================

    /**
     * 乐观锁版本号
     */
    private LocalDateTime version;

    /**
     * 逻辑删除标志，0-正常，1-删除
     */
    private Integer delFlag;

    // ==================== 集合字段 ====================

    /**
     * 未还款账单列表
     * 选择性加载：按业务需求加载部分业务需要的数据
     */
    @Builder.Default
    private List<CreditBillEntity> unpaidBills = new ArrayList<>();

    /**
     * 本次操作新增的账单
     */
    @Builder.Default
    private List<CreditBillEntity> newBills = new ArrayList<>();

    // ==================== 行为方法 ====================

    /**
     * 临时授信支付，创建账单并占用额度
     * 用例来源：UC-CW-011
     * 需求来源：T13, T15
     *
     * @param command 临时授信支付命令
     * @return 创建的账单实体
     */
    public CreditBillEntity temporaryCreditPay(TemporaryCreditPayCommand command) {
        // 更新额度
        this.usedAmount = this.usedAmount.add(command.getAmount());
        this.remainingAmount = this.totalAmount.subtract(this.usedAmount);

        // 验证金额计算结果的合法性
        validateAmountInvariants();

        changeStatus();

        // 创建账单（delFlag由框架自动填充）
        CreditBillEntity bill = CreditBillEntity.builder()
                .code(BusinessCodeGenerator.generateBillCode("TC"))
                .creditWalletId(this.creditWalletId)
                .transactionType(TransactionType.TEMPORARY_CREDIT_PAY)
                .temporaryCreditId(this.id)
                .amount(command.getAmount())
                .paymentId(command.getPaymentId())
                .dueDate(LocalDate.now().plusDays(command.getTermDays()))
                .repaymentStatus(RepaymentStatus.UNPAID)
                .remark(command.getRemark())
                .currency(this.currency)
                .build();

        this.newBills.add(bill);

        return bill;
    }

    private void changeStatus() {
        // 更新状态
        if (this.remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = TemporaryCreditStatus.EXHAUSTED;
        } else {
            this.status = TemporaryCreditStatus.IN_USE;
        }
    }

    /**
     * 批量临时授信支付，使用同一临时授信批量支付多个支付单
     * 用例来源：UC-CW-013
     * 需求来源：T17
     *
     * @param command 批量临时授信支付命令
     * @return 创建的账单实体列表
     */
    public List<CreditBillEntity> batchTemporaryCreditPay(BatchTemporaryCreditPayCommand command) {

        List<CreditBillEntity> bills = new ArrayList<>();

        for (TemporaryCreditPayCommand item : command.getPayments()) {
            CreditBillEntity bill = temporaryCreditPay(item);
            bills.add(bill);
        }

        return bills;
    }

    private void validateCanPay(BigDecimal totalAmount) {
        if (!canPayStatus()) {
            throw new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_ERROR);

        }

        // 判断该临时信用是否已过期
        if (isExpired()) {
            throw new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_EXPIRED);
        }

        if (!hasAvailableAmount(totalAmount)) {
            throw new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_EXHAUSTED);
        }
    }

    /**
     * 发起还款，绑定还款支付单ID到账单，状态UNPAID→REPAYING
     * 用例来源：UC-CW-020
     * 需求来源：T18-1
     *
     * @param command 发起还款命令
     */
    public void initiateRepayment(InitiateRepaymentCommand command) {
        CreditBillEntity bill = findUnPaidBillById(command.getBillId());

        if (!bill.canInitiateRepayment()) {
            throw new CreditWalletException(CreditWalletErrorCode.REPAYMENT_AMOUNT_INVALID);
        }

        bill.initiateRepayment(command.getRepaymentPaymentId());
    }

    /**
     * 完成还款，接收支付成功通知后记录已还款金额
     * 用例来源：UC-CW-021
     * 需求来源：T18-2
     *
     * @param command 完成还款命令
     */
    public void completeRepayment(CompleteRepaymentCommand command) {
        // Requirements:
        // - 仅累计repaidAmount，不恢复remainingAmount或减少usedAmount
        // - 临时授信是一次性额度，还款后不恢复

        CreditBillEntity bill = findUnPaidBillById(command.getBillId());

        if (!bill.canCompleteRepayment()) {
            throw new CreditWalletException(CreditWalletErrorCode.REPAYMENT_AMOUNT_INVALID);
        }

        // 累计已还款金额
        BigDecimal repaymentAmount = bill.getAmount();
        this.repaidAmount = this.repaidAmount.add(repaymentAmount);

        // 验证金额计算结果的合法性
        validateAmountInvariants();

        // 完成还款
        bill.completeRepayment(command.getRepaymentTime());
    }

    private CreditBillEntity findUnPaidBillById(String billId) {
        Optional<CreditBillEntity> first = this.unpaidBills.stream().filter(bill -> bill.getId().equals(billId)).findFirst();
        CreditBillEntity bill = first.orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.TRANSACTION_NOT_FOUND));
        return bill;
    }

    /**
     * 标记过期
     * 用例来源：UC-CW-007
     * 需求来源：T10
     * 说明：由定时任务调用，过期后不能再用于新的支付，已使用的额度仍需还款
     */
    public void expire() {

        this.status = TemporaryCreditStatus.EXPIRED;
    }


    public boolean canPayStatus() {
        if (this.status == null || this.expiryDate == null) {
            return false;
        }
        return TemporaryCreditStatus.APPROVED.equals(this.status) ||
               TemporaryCreditStatus.IN_USE.equals(this.status);

    }


    /**
     * 判断是否已过期
     *
     * @return true if expired
     */
    public boolean isExpired() {
        // 如果 expiryDate 为空，视为无期限
        if (this.expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(this.expiryDate);
    }

    /**
     * 判断是否已用尽（剩余额度为 null 或 <= 0）
     *
     * @return true if exhausted
     */
    public boolean isExhausted() {
        return this.remainingAmount == null || this.remainingAmount.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * 判断剩余额度是否足够
     *
     * @param amount 支付金额
     * @return true if has available amount
     */
    public boolean hasAvailableAmount(BigDecimal amount) {
        return this.remainingAmount != null &&
               this.remainingAmount.compareTo(amount) >= 0;
    }

    /**
     * 验证金额计算的领域不变量
     * 确保所有金额字段的值在合理范围内，防止计算错误导致数据异常
     *
     * <p>不变量规则：</p>
     * <ul>
     *   <li>totalAmount > 0：临时授信总额必须大于0</li>
     *   <li>usedAmount >= 0：已用金额不能为负数</li>
     *   <li>repaidAmount >= 0：已还款金额不能为负数</li>
     *   <li>remainingAmount = totalAmount - usedAmount：剩余可用金额计算公式必须正确</li>
     *   <li>usedAmount <= totalAmount：已用金额不能超过总额（注：允许等于，代表用尽）</li>
     *   <li>repaidAmount <= usedAmount：已还款金额不能超过已用金额</li>
     * </ul>
     *
     * @throws CreditWalletException 当金额计算违反不变量时抛出异常
     */
    private void validateAmountInvariants() {
        // 验证totalAmount必须大于0
        if (this.totalAmount == null || this.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                "临时授信总额必须大于0，当前值：" + this.totalAmount);
        }

        // 验证usedAmount不为负数
        if (this.usedAmount == null || this.usedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                "已用金额不能为负数，当前值：" + this.usedAmount);
        }

        // 验证repaidAmount不为负数
        if (this.repaidAmount == null || this.repaidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                "已还款金额不能为负数，当前值：" + this.repaidAmount);
        }

        // 验证剩余可用金额计算公式：remainingAmount = totalAmount - usedAmount
        BigDecimal expectedRemainingAmount = this.totalAmount.subtract(this.usedAmount);
        if (this.remainingAmount == null || this.remainingAmount.compareTo(expectedRemainingAmount) != 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                String.format("剩余可用金额计算错误，期望值：%s，实际值：%s (totalAmount=%s, usedAmount=%s)",
                    expectedRemainingAmount, this.remainingAmount, this.totalAmount, this.usedAmount));
        }

        // 验证已用金额不超过总额
        if (this.usedAmount.compareTo(this.totalAmount) > 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                String.format("已用金额超过总额，usedAmount=%s, totalAmount=%s",
                    this.usedAmount, this.totalAmount));
        }

        // 验证已还款金额不超过已用金额
        if (this.repaidAmount.compareTo(this.usedAmount) > 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                String.format("已还款金额超过已用金额，repaidAmount=%s, usedAmount=%s",
                    this.repaidAmount, this.usedAmount));
        }
    }

    /**
     * 清空临时集合（持久化后调用）
     */
    public void clearTempCollections() {
        this.newBills = new ArrayList<>();
    }
}