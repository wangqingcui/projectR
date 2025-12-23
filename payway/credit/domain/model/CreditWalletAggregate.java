package com.bytz.modules.cms.payway.credit.domain.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payway.credit.domain.command.*;
import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.entity.ManagerLog;
import com.bytz.modules.cms.payway.credit.domain.enums.ManageChangeType;
import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
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
import java.util.*;

/**
 * 信用钱包聚合根
 * Credit Wallet Aggregate
 *
 * <p>封装信用额度管理、支付、还款的核心业务规则。</p>
 * <p>需求来源：需求文档 M01-钱包账户与额度管理</p>
 * <p>用例来源：UC-CW-001~UC-CW-005, UC-CW-010, UC-CW-012, UC-CW-020, UC-CW-021</p>
 * <p>职责：
 * - 管理信用钱包的完整生命周期
 * - 管理授信额度和账期参数
 * - 执行信用支付和还款操作
 * - 维护额度计算规则
 * - 管理信用账单
 * - 保证领域不变量
 * </p>
 * <p>关联关系：
 * - 通过resellerId与TemporaryCredit聚合根松耦合关联
 * - 不直接持有TemporaryCredit实体
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditWalletAggregate {

    // ==================== 基本信息 ====================

    /**
     * 钱包主键ID，UUID，32位字符
     */
    private String id;

    /**
     * 经销商ID，外键关联经销商系统
     */
    private String resellerId;

    // ==================== 额度字段 ====================

    /**
     * 总授信额度（>= 0）
     */
    private BigDecimal totalLimit;

    /**
     * 可用额度（允许为负数）
     * 计算公式：totalLimit - usedLimit
     */
    private BigDecimal availableLimit;

    /**
     * 已用额度（>= 0）
     */
    private BigDecimal usedLimit;

    // ==================== 账期字段 ====================

    /**
     * 账期天数，用于计算账单到期日期（> 0）
     */
    private Integer termDays;

    // ==================== 状态字段 ====================

    /**
     * 启用状态（true-启用，false-停用，默认false）
     */
    private Boolean enabled;

    /**
     * 冻结状态（true-冻结，false-未冻结，默认false）
     */
    private Boolean frozen;

    /**
     * 是否可用于预付 （true-支持，false-不支持，默认false）
     */
    private Boolean prepaymentEnabled;

    // ==================== 币种字段 ====================

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

    // ==================== 集合字段（聚合内对象）====================

    /**
     * 未还款账单列表（repaymentStatus = UNPAID 或 REPAYING）
     * 选择性加载：按业务需求加载部分业务需要的数据
     */
    @Builder.Default
    private List<CreditBillEntity> unpaidBills = new ArrayList<>();

    /**
     * 本次操作新增的账单，用于批量持久化
     */
    @Builder.Default
    private List<CreditBillEntity> newBills = new ArrayList<>();

    /**
     * 本次操作新增的管理日志
     */
    @Builder.Default
    private List<ManagerLog> newManagerLogs = new ArrayList<>();

    // ==================== 行为方法 ====================

    /**
     * 信用支付，创建账单并占用额度
     * 用例来源：UC-CW-010
     * 需求来源：T12, T14, T15
     *
     * @param command 信用支付命令
     * @return 创建的账单实体
     */
    public CreditBillEntity creditPay(CreditPayCommand command) {
        // Requirements:
        // - 验证可用额度充足
        // - usedLimit += 支付金额
        // - availableLimit = totalLimit - usedLimit
        // - 生成CREDIT_PAY类型账单

        // 更新额度
        this.usedLimit = this.usedLimit.add(command.getAmount());
        this.availableLimit = this.totalLimit.subtract(this.usedLimit);

        // 验证金额计算结果的合法性
        validateAmountInvariants();

        // 创建账单（delFlag由框架自动填充）
        CreditBillEntity bill = CreditBillEntity.builder()
                .code(BusinessCodeGenerator.generateBillCode("CB"))
                .creditWalletId(this.id)
                .transactionType(TransactionType.CREDIT_PAY)
                .amount(command.getAmount())
                .paymentId(command.getPaymentId())
                .dueDate(LocalDate.now().plusDays(this.termDays))
                .repaymentStatus(RepaymentStatus.UNPAID)
                .remark(command.getRemark())
                .currency(this.currency)
                .build();

        this.newBills.add(bill);

        // 生成额度变更日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.LIMIT_CHANGE, "信用支付", null, null);

        this.newManagerLogs.add(managerLog);

        return bill;
    }

    private ManagerLog generateManagerLog(Object command, ManageChangeType changeType, String reason, String operator, String operatorName) {
        Map<String, Object> afterState = new HashMap<>();
        afterState.put("resellerId", this.resellerId);
        afterState.put("totalLimit", this.totalLimit);
        afterState.put("availableLimit", this.availableLimit);
        afterState.put("usedLimit", this.usedLimit);
        afterState.put("termDays", this.termDays);
        afterState.put("enabled", this.enabled);
        afterState.put("frozen", this.frozen);
        afterState.put("changeType", changeType);
        afterState.put("prepaymentEnabled", this.prepaymentEnabled);
        afterState.put("command", command);

        ManagerLog log = ManagerLog.builder()
                .creditWalletId(this.id)
                .resellerId(this.resellerId)
                .operationType(changeType)
                .afterState(afterState)
                .reason(reason)
                .createBy(operator)
                .createByName(operatorName)
                .build();
        return log;
    }

    /**
     * 批量信用支付，使用同一钱包批量支付多个支付单
     * 用例来源：UC-CW-012
     * 需求来源：T16
     *
     * @param command 批量信用支付命令
     * @return 创建的账单实体列表
     */
    public List<CreditBillEntity> batchCreditPay(BatchCreditPayCommand command) {
        // Requirements:
        // - 批量支付是原子操作，全部成功或全部失败
        // - 验证总金额不超过可用额度


        List<CreditBillEntity> bills = new ArrayList<>();

        for (CreditPayCommand item : command.getPayments()) {

            CreditBillEntity bill = creditPay(item);
            bills.add(bill);
        }

        return bills;
    }

    /**
     * 发起还款，绑定还款支付单ID到账单，状态UNPAID→REPAYING
     * 用例来源：UC-CW-020
     * 需求来源：T18-1
     *
     * @param command 发起还款命令
     */
    public void initiateRepayment(InitiateRepaymentCommand command) {
        // Requirements:
        // - 验证账单状态为UNPAID且未绑定repaymentPaymentId
        CreditBillEntity bill = findUnPaidBillById(command.getBillId());

        if (!bill.canInitiateRepayment()) {
            throw new CreditWalletException(CreditWalletErrorCode.REPAYMENT_AMOUNT_INVALID);
        }

        bill.initiateRepayment(command.getRepaymentPaymentId());
    }

    /**
     * 完成还款，接收支付成功通知后返还额度，状态REPAYING→REPAID
     * 用例来源：UC-CW-021
     * 需求来源：T18-2
     *
     * @param command 完成还款命令
     */
    public void completeRepayment(CompleteRepaymentCommand command) {
        // Requirements:
        // - 返还额度：usedLimit -= 账单金额，availableLimit += 账单金额
        // - 生成LIMIT_CHANGE类型日志
        CreditBillEntity bill = findUnPaidBillById(command.getBillId());
        if (!bill.canCompleteRepayment()) {
            throw new CreditWalletException(CreditWalletErrorCode.REPAYMENT_AMOUNT_INVALID);
        }

        // 返还额度
        BigDecimal repaymentAmount = bill.getAmount();
        this.usedLimit = this.usedLimit.subtract(repaymentAmount);
        this.availableLimit = this.totalLimit.subtract(this.usedLimit);

        // 验证金额计算结果的合法性
        validateAmountInvariants();

        // 完成还款
        bill.completeRepayment(command.getRepaymentTime());

        // 生成额度变更日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.LIMIT_CHANGE, "还款完成", null, null);

        this.newManagerLogs.add(managerLog);
    }

    private CreditBillEntity findUnPaidBillById(String billId) {
        Optional<CreditBillEntity> first = this.unpaidBills.stream().filter(bill -> bill.getId().equals(billId)).findFirst();
        CreditBillEntity bill = first.orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.TRANSACTION_NOT_FOUND));
        return bill;
    }

    /**
     * 调整授信额度
     * 用例来源：UC-CW-004
     * 需求来源：T04
     *
     * @param command 调整额度命令
     */
    public void adjustLimit(AdjustCreditLimitCommand command) {
        // Requirements:
        // - 允许新额度小于已使用额度（可用额度可为负数）

        BigDecimal oldTotalLimit = this.totalLimit;
        this.totalLimit = command.getNewTotalLimit();
        this.availableLimit = this.totalLimit.subtract(this.usedLimit);

        // 验证金额计算结果的合法性
        validateAmountInvariants();

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.LIMIT_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 冻结钱包
     * 用例来源：UC-CW-003
     * 需求来源：T03
     *
     * @param command 冻结命令
     */
    public void freeze(FreezeWalletCommand command) {
        // Requirements:
        // - 冻结后不允许信用支付，但允许还款和查询

        this.frozen = true;

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.FREEZE_STATUS_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 解冻钱包
     * 用例来源：UC-CW-003
     * 需求来源：T03
     *
     * @param command 解冻命令
     */
    public void unfreeze(UnfreezeWalletCommand command) {

        this.frozen = false;

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.FREEZE_STATUS_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 启用钱包
     * 用例来源：UC-CW-002
     * 需求来源：T02
     *
     * @param command 启用命令
     */
    public void enable(EnableWalletCommand command) {

        this.enabled = true;

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.ENABLE_STATUS_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 停用钱包
     * 用例来源：UC-CW-002
     * 需求来源：T02
     *
     * @param command 停用命令
     */
    public void disable(DisableWalletCommand command) {
        // Requirements:
        // - 停用后不允许信用支付，但允许还款和查询

        this.enabled = false;

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.ENABLE_STATUS_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 更新账期天数（不影响已创建账单）
     * 用例来源：UC-CW-005
     * 需求来源：T06
     *
     * @param command 更新账期命令
     */
    public void updateTermDays(UpdateTermDaysCommand command) {
        // Requirements:
        // - 账期变更只影响新创建的账单

        this.termDays = command.getNewTermDays();

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.TERM_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 开启预付功能
     *
     * @param command 开启预付功能命令
     */
    public void enablePrepayment(EnablePrepaymentCommand command) {
        this.prepaymentEnabled = true;

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.PREPAYMENT_STATUS_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 关闭预付功能
     *
     * @param command 关闭预付功能命令
     */
    public void disablePrepayment(DisablePrepaymentCommand command) {
        this.prepaymentEnabled = false;

        // 生成管理日志
        ManagerLog managerLog = generateManagerLog(command, ManageChangeType.PREPAYMENT_STATUS_CHANGE, command.getReason(), command.getOperator(), command.getOperatorName());

        this.newManagerLogs.add(managerLog);
    }

    /**
     * 判断是否可以进行信用支付
     * 业务规则：钱包启用、未冻结、额度充足
     *
     * @param amount 支付金额
     * @return true if can pay
     */
    public boolean canPay(BigDecimal amount) {
        return Boolean.TRUE.equals(this.enabled)
                && Boolean.FALSE.equals(this.frozen)
                && this.availableLimit != null
                && this.availableLimit.compareTo(BigDecimal.ZERO) > 0
                && this.availableLimit.compareTo(amount) >= 0;
    }

    /**
     * 验证金额计算的领域不变量
     * 确保所有金额字段的值在合理范围内，防止计算错误导致数据异常
     *
     * <p>不变量规则：</p>
     * <ul>
     *   <li>totalLimit >= 0：总授信额度不能为负数</li>
     *   <li>usedLimit >= 0：已用额度不能为负数</li>
     *   <li>availableLimit = totalLimit - usedLimit：可用额度计算公式必须正确</li>
     * </ul>
     *
     * @throws CreditWalletException 当金额计算违反不变量时抛出异常
     */
    private void validateAmountInvariants() {
        // 验证totalLimit不为负数
        if (this.totalLimit == null || this.totalLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                    "总授信额度不能为负数，当前值：" + this.totalLimit);
        }

        // 验证usedLimit不为负数
        if (this.usedLimit == null || this.usedLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                    "已用额度不能为负数，当前值：" + this.usedLimit);
        }

        // 验证可用额度计算公式：availableLimit = totalLimit - usedLimit
        BigDecimal expectedAvailableLimit = this.totalLimit.subtract(this.usedLimit);
        if (this.availableLimit == null || this.availableLimit.compareTo(expectedAvailableLimit) != 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT,
                    String.format("可用额度计算错误，期望值：%s，实际值：%s (totalLimit=%s, usedLimit=%s)",
                            expectedAvailableLimit, this.availableLimit, this.totalLimit, this.usedLimit));
        }
    }

    /**
     * 清空临时集合（持久化后调用）
     */
    public void clearTempCollections() {
        this.newBills = new ArrayList<>();
        this.newManagerLogs = new ArrayList<>();
    }

    /**
     * 判断是否支持指定的支付方式列表
     * 只有当所有支付方式都支持时，才返回true
     *
     * @param paymentTypes 支付方式列表
     * @return true表示所有支付方式均支持，false表示至少有一种支付方式不支持
     */
    public boolean isSupportPayment(List<PaymentType> paymentTypes) {
        if (paymentTypes == null || paymentTypes.isEmpty()) {
            return false;
        }
        return paymentTypes.stream()
                .map(this::isSupportPayment)
                .filter(item -> !item)
                .findFirst()
                .orElse(true);
    }

    /**
     * 判断是否支持指定类型的支付
     * 支持返回true，不支持返回false
     *
     * @param paymentType 支付类型
     * @return true表示支持该支付类型，false表示不支持
     */
    public boolean isSupportPayment(PaymentType paymentType) {
        if (paymentType == null) {
            return false;
        }
        if (paymentType.equals(PaymentType.ADVANCE_PAYMENT)) {
            return this.prepaymentEnabled;
        }
        return true;
    }
}