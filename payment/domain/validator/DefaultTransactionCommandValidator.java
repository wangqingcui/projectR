package com.bytz.modules.cms.payment.domain.validator;

import com.bytz.modules.cms.payment.domain.command.CreateTransactionCommand;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;

/**
 * 默认交易命令验证器
 * Default Transaction Command Validator
 * 
 * <p>验证创建交易命令的完整性，包括：
 *   - 渠道交易记录ID必填
 *   - 退款流水必须关联原支付流水ID
 *   - 金额验证（委托给金额验证器）
 * </p>
 * <p>验证失败时直接抛出PaymentException异常</p>
 */
public class DefaultTransactionCommandValidator implements TransactionCommandValidator {

    private final TransactionAmountValidator amountValidator;

    public DefaultTransactionCommandValidator() {
        this.amountValidator = new DefaultTransactionAmountValidator();
    }

    public DefaultTransactionCommandValidator(TransactionAmountValidator amountValidator) {
        this.amountValidator = amountValidator;
    }

    @Override
    public void validate(CreateTransactionCommand command) {
        // 验证渠道交易记录ID必填
        if (command.getChannelTransactionId() == null || command.getChannelTransactionId().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_CHANNEL_TRANSACTION, "渠道交易记录ID不能为空");
        }

        // 验证退款流水关联约束
        TransactionType type = command.getTransactionType() != null ?
                command.getTransactionType() : TransactionType.PAYMENT;
        if (TransactionType.REFUND.equals(type) &&
                (command.getOriginalTransactionId() == null || command.getOriginalTransactionId().isEmpty())) {
            throw new PaymentException(PaymentErrorCode.INVALID_REFUND_TRANSACTION, "退款流水必须关联原支付流水ID");
        }

        // 委托金额验证
        amountValidator.validate(command.getTransactionAmount(), type);
    }

    /**
     * 获取金额验证器
     */
    public TransactionAmountValidator getAmountValidator() {
        return amountValidator;
    }
}
