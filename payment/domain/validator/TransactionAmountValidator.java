package com.bytz.modules.cms.payment.domain.validator;

import com.bytz.modules.cms.payment.domain.enums.TransactionType;

import java.math.BigDecimal;

/**
 * 交易金额验证器接口
 * Transaction Amount Validator Interface
 * 
 * <p>支持不同业务策略下的金额验证</p>
 * <p>验证失败时直接抛出异常</p>
 */
public interface TransactionAmountValidator {

    /**
     * 验证交易金额
     * 验证失败时抛出PaymentException异常
     *
     * @param amount 交易金额
     * @param transactionType 交易类型
     * @throws com.bytz.modules.cms.payment.shared.exception.PaymentException 验证失败时抛出
     */
    void validate(BigDecimal amount, TransactionType transactionType);
}
