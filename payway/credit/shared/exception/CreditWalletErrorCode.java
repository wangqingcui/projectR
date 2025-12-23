package com.bytz.modules.cms.payway.credit.shared.exception;

/**
 * 信用钱包错误码枚举
 * Credit Wallet Error Code Enumeration
 */
public enum CreditWalletErrorCode {

    WALLET_NOT_FOUND("CW001", "信用钱包不存在"),
    WALLET_ERROR_CODE("CW002", "信用钱包错误"),
    WALLET_ALREADY_EXISTS("CW003", "信用钱包已存在"),
    WALLET_DISABLED("CW004", "信用钱包已停用"),
    WALLET_FROZEN("CW005", "信用钱包已冻结"),
    INSUFFICIENT_LIMIT("CW006", "可用额度不足"),
    INVALID_AMOUNT("CW007", "无效的金额"),
    TRANSACTION_NOT_FOUND("CW008", "交易记录不存在"),
    TEMPORARY_CREDIT_NOT_FOUND("CW009", "临时信用不存在"),
    TEMPORARY_CREDIT_ERROR("CW010", "临时信用错误"),
    TEMPORARY_CREDIT_EXPIRED("CW011", "临时信用已过期"),
    TEMPORARY_CREDIT_EXHAUSTED("CW012", "临时信用已用完"),
    REPAYMENT_AMOUNT_INVALID("CW013", "还款金额无效"),
    APPROVAL_ID_DUPLICATE("CW014", "审批ID重复"),
    UNSUPPORTED_PAYMENT_TYPE("CW015", "存在不支持的支付单类型");

    private final String code;
    private final String message;

    CreditWalletErrorCode(String code, String message) {
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