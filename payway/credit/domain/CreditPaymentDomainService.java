package com.bytz.modules.cms.payway.credit.domain;

import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 信用支付领域服务
 * Credit Payment Domain Service
 * 
 * <p>处理跨聚合的业务逻辑。</p>
 * <p>用例来源：UC-CW-010, UC-CW-011, UC-CW-030</p>
 * <p>需求来源：T11, T20</p>
 * <p>职责：
 *   - 验证信用支付前置条件
 *   - 计算到期日期
 *   - 判断账单是否逾期
 *   - 协调多个聚合根之间的业务流程
 * </p>
 */
@Slf4j
@Service
public class CreditPaymentDomainService {
    
    /**
     * 验证信用支付前置条件
     * 需求来源：T11-1
     * 
     * @param wallet 信用钱包聚合根
     * @param amount 支付金额
     */
    public void validateCreditPayment(CreditWalletAggregate wallet, BigDecimal amount) {
        // TODO: Implement validateCreditPayment business logic
        // Requirements:
        // - 钱包已启用（enabled = true）
        // - 钱包未冻结（frozen = false）
        // - 支付金额 > 0
        // - 支付金额 <= 可用额度
        
        if (!Boolean.TRUE.equals(wallet.getEnabled())) {
            throw new CreditWalletException(CreditWalletErrorCode.WALLET_DISABLED);
        }
        
        if (Boolean.TRUE.equals(wallet.getFrozen())) {
            throw new CreditWalletException(CreditWalletErrorCode.WALLET_FROZEN);
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT);
        }
        
        if (wallet.getAvailableLimit().compareTo(amount) < 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INSUFFICIENT_LIMIT);
        }
    }
    
    /**
     * 验证临时授信支付前置条件
     * 需求来源：T11-2
     * 
     * @param temporaryCredit 临时授信聚合根
     * @param amount 支付金额
     */
    public void validateTemporaryCreditPayment(TemporaryCreditAggregate temporaryCredit, BigDecimal amount) {
        // TODO: Implement validateTemporaryCreditPayment business logic
        // Requirements:
        // - 临时授信状态为APPROVED或IN_USE
        // - 临时授信未过期（currentDate <= expiryDate）
        // - 支付金额 > 0
        // - 支付金额 <= 剩余可用额度
        
        if (temporaryCredit.isExpired()) {
            throw new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_EXPIRED);
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CreditWalletException(CreditWalletErrorCode.INVALID_AMOUNT);
        }
        
        if (!temporaryCredit.hasAvailableAmount(amount)) {
            throw new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_EXHAUSTED);
        }
    }
    
    /**
     * 计算到期日期
     * 
     * @param createdDate 创建日期
     * @param termDays 账期天数
     * @return 到期日期
     */
    public LocalDate calculateDueDate(LocalDate createdDate, Integer termDays) {
        // TODO: Implement calculateDueDate business logic
        // Requirements:
        // - dueDate = createdDate + termDays
        // - 使用日历天数，非工作日
        
        return createdDate.plusDays(termDays);
    }
    
    /**
     * 判断是否逾期
     * 需求来源：T20
     * 
     * @param dueDate 到期日期
     * @param currentDate 当前日期
     * @return true if overdue
     */
    public boolean isOverdue(LocalDate dueDate, LocalDate currentDate) {
        // TODO: Implement isOverdue business logic
        // Requirements:
        // - currentDate > dueDate
        
        return currentDate.isAfter(dueDate);
    }
}