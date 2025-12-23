package com.bytz.modules.cms.payment.domain.validator;

import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;

import java.math.BigDecimal;

/**
 * 默认交易金额验证器
 * Default Transaction Amount Validator
 * 
 * <p>默认验证策略：
 *   - 支付流水：金额必须为正数
 *   - 退款流水：金额必须为正数
 * </p>
 * <p>验证失败时直接抛出PaymentException异常</p>
 */
public class DefaultTransactionAmountValidator implements TransactionAmountValidator {

    @Override
    public void validate(BigDecimal amount, TransactionType transactionType) {
        // 金额不能为null或0
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_AMOUNT, "交易金额不能为空或0");
        }

        // 所有金额必须为正数（无论支付还是退款）
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_AMOUNT, "交易金额必须为正数");
        }
    }
}
