package com.bytz.modules.cms.payway.credit.domain.entity;

import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TransactionType;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 信用账单实体
 * Credit Bill Entity
 * 
 * <p>记录完整的支付和还款生命周期。</p>
 * <p>需求来源：需求文档 M06-交易流水管理 T15, T23</p>
 * <p>生命周期：创建 → UNPAID → REPAYING → REPAID</p>
 * <p>说明：
 *   - 账单创建即表示支付成功，无需paymentStatus字段
 *   - 还款即全额还款，无需账单级别的repaidAmount和remainingAmount字段
 *   - 每笔账单只能还款一次
 *   - code字段作为业务唯一标识，用于外部交互和查询
 *   - id仅作为数据库主键（内部使用）
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBillEntity {
    
    /**
     * 账单数据库主键，UUID，32位字符
     */
    private String id;
    
    /**
     * 账单编号，业务唯一标识，用于外部交互和查询
     * 生成规则：前缀(2-4字母) + 时间戳(17位)
     * - CREDIT_PAY: CB20250124153020001
     * - TEMPORARY_CREDIT_PAY: TC20250124153020001
     */
    private String code;
    
    /**
     * 钱包ID，外键
     * - CREDIT_PAY时：必填，关联信用钱包
     * - TEMPORARY_CREDIT_PAY时：必填，虽然临时额度不和信用钱包额度关联，但使用termDays计算到期日期
     */
    private String creditWalletId;
    
    /**
     * 交易类型：CREDIT_PAY, TEMPORARY_CREDIT_PAY
     */
    private TransactionType transactionType;
    
    /**
     * 临时授信ID，仅当transactionType=TEMPORARY_CREDIT_PAY时有值
     */
    private String temporaryCreditId;
    
    /**
     * 账单金额（>0）
     */
    private BigDecimal amount;
    
    /**
     * 支付单ID，必填，关联支付模块
     */
    private String paymentId;
    
    /**
     * 还款支付单ID，还款发起时填充
     */
    private String repaymentPaymentId;
    
    /**
     * 到期日期，账单创建时计算：createdDate + termDays
     */
    private LocalDate dueDate;
    
    /**
     * 还款状态：UNPAID, REPAYING, REPAID
     */
    private RepaymentStatus repaymentStatus;
    
    /**
     * 还款完成时间，还款成功后填充
     */
    private LocalDateTime repaymentCompletedTime;
    
    /**
     * 账单备注，可选，用于记录业务方传入的附加说明
     */
    private String remark;
    
    /**
     * 币种（默认CNY）
     */
    private String currency;
    
    /**
     * 创建时间（即账单创建时间），框架自动填充
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
    
    /**
     * 乐观锁版本号
     */
    private LocalDateTime version;
    
    /**
     * 逻辑删除标志，0-正常，1-删除
     */
    private Integer delFlag;
    
    /**
     * 判断账单是否可以发起还款
     * 
     * @return true if can initiate repayment
     */
    public boolean canInitiateRepayment() {
        return RepaymentStatus.UNPAID.equals(this.repaymentStatus) 
                && this.repaymentPaymentId == null;
    }
    
    /**
     * 判断账单是否可以完成还款
     * 
     * @return true if can complete repayment
     */
    public boolean canCompleteRepayment() {
        return RepaymentStatus.REPAYING.equals(this.repaymentStatus);
    }
    
    /**
     * 判断账单是否逾期
     * 
     * @param currentDate 当前日期
     * @return true if overdue
     */
    public boolean isOverdue(LocalDate currentDate) {
        return !RepaymentStatus.REPAID.equals(this.repaymentStatus) 
                && currentDate.isAfter(this.dueDate);
    }
    
    /**
     * 发起还款，更新状态为REPAYING
     * 
     * @param repaymentPaymentId 还款支付单ID
     */
    public void initiateRepayment(String repaymentPaymentId) {
        if (!canInitiateRepayment()) {
            throw new CreditWalletException(CreditWalletErrorCode.REPAYMENT_AMOUNT_INVALID);
        }
        this.repaymentStatus = RepaymentStatus.REPAYING;
        this.repaymentPaymentId = repaymentPaymentId;
        // Note: updateTime is handled by framework's FieldFill.INSERT_UPDATE
    }
    
    /**
     * 完成还款，更新状态为REPAID
     */
    public void completeRepayment(LocalDateTime createTime) {
        if (!canCompleteRepayment()) {
            throw new CreditWalletException(CreditWalletErrorCode.REPAYMENT_AMOUNT_INVALID);
        }
        this.repaymentStatus = RepaymentStatus.REPAID;
        this.repaymentCompletedTime = createTime;
        // Note: updateTime is handled by framework's FieldFill.INSERT_UPDATE
    }
}