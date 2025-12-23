package com.bytz.modules.cms.payway.wallet.shared.exception;

/**
 * 钱包模块错误码
 * 数字型错误码，预留可扩展空间。
 * 规范示例：15xxxx 段分配给钱包模块。
 */
public enum WalletErrorCode {
    PARAM_INVALID("150002", "参数校验失败"),
    WALLET_NOT_FOUND("150003", "钱包不存在"),
    WALLET_STATUS_ERROR("150004", "钱包状态不允许该操作"),
    INSUFFICIENT_BALANCE("150005", "钱包余额不足"),
    NEGATIVE_BALANCE_FORBIDDEN("150006", "钱包余额为负数，禁止支付"),
    AMOUNT_REQUIRED("150007", "金额不能为空"),
    AMOUNT_MUST_BE_POSITIVE("150008", "金额必须大于0"),
    AMOUNT_CANNOT_BE_ZERO("150009", "金额不能为0"),
    REFUND_AMOUNT_EXCEEDS_ORIGINAL("150010", "退款金额不能超过原支付金额"),
    TRANSACTION_NOT_FOUND("150011", "交易记录不存在"),
    DUPLICATE_WALLET_FOR_RESELLER("150012", "经销商已存在钱包，不能重复创建");

    private final String code;
    private final String message;

    WalletErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}