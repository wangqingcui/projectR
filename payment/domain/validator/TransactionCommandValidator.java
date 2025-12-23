package com.bytz.modules.cms.payment.domain.validator;

import com.bytz.modules.cms.payment.domain.command.CreateTransactionCommand;

/**
 * 交易命令验证器接口
 * Transaction Command Validator Interface
 * 
 * <p>验证创建交易命令的完整性和有效性</p>
 * <p>验证失败时直接抛出异常</p>
 */
public interface TransactionCommandValidator {

    /**
     * 验证创建交易命令
     * 验证失败时抛出PaymentException异常
     *
     * @param command 创建交易命令
     * @throws com.bytz.modules.cms.payment.shared.exception.PaymentException 验证失败时抛出
     */
    void validate(CreateTransactionCommand command);
}
