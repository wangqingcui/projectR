package com.bytz.modules.cms.payment.shared.exception;

/**
 * 支付模块错误码枚举
 * Payment Error Code Enumeration
 * 
 * <p>定义支付模块的所有错误码，用于异常处理和错误响应。</p>
 */
public enum PaymentErrorCode {
    
    // ==================== 支付单相关错误 ====================
    
    PAYMENT_NOT_FOUND("PM001", "支付单不存在"),
    PAYMENT_ALREADY_EXISTS("PM002", "支付单已存在"),
    INVALID_PAYMENT_STATE("PM003", "支付单状态无效"),
    PAYMENT_ALREADY_PAID("PM004", "支付单已完成支付"),
    PAYMENT_ALREADY_CLOSED("PM005", "支付单已关闭"),
    
    // ==================== 金额相关错误 ====================
    
    INVALID_AMOUNT("PM010", "无效的金额"),
    AMOUNT_EXCEED_LIMIT("PM011", "金额超过限制"),
    INSUFFICIENT_AMOUNT("PM012", "金额不足"),
    
    // ==================== 流水相关错误 ====================
    
    TRANSACTION_NOT_FOUND("PM020", "支付流水不存在"),
    INVALID_TRANSACTION_STATE("PM021", "支付流水状态无效"),
    DUPLICATE_TRANSACTION("PM022", "重复的支付流水"),
    INVALID_CHANNEL_TRANSACTION("PM023", "无效的渠道交易记录"),
    INVALID_REFUND_TRANSACTION("PM024", "无效的退款流水"),
    
    // ==================== 验证相关错误 ====================
    
    VALIDATION_FAILED("PM030", "验证失败"),
    RESELLER_MISMATCH("PM031", "经销商不匹配"),
    ORDER_MISMATCH("PM032", "订单不匹配"),
    INVALID_PARAMETER("PM033", "无效的参数"),
    
    // ==================== 退款相关错误 ====================
    
    REFUND_NOT_ALLOWED("PM040", "不允许退款"),
    REFUND_AMOUNT_EXCEED("PM041", "退款金额超过可退款金额"),
    ORIGINAL_PAYMENT_NOT_FOUND("PM042", "原支付单不存在"),
    
    // ==================== 渠道相关错误 ====================
    
    CHANNEL_NOT_SUPPORTED("PM050", "支付渠道不支持该操作"),
    CHANNEL_REFUND_FAILED("PM051", "渠道退款失败"),
    
    // ==================== 系统错误 ====================
    
    SYSTEM_ERROR("PM999", "系统错误");
    
    private final String code;
    private final String message;
    
    PaymentErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举值
     *
     * @param code 错误码
     * @return 枚举值，如果未找到则返回SYSTEM_ERROR
     */
    public static PaymentErrorCode fromCode(String code) {
        if (code == null) {
            return SYSTEM_ERROR;
        }
        for (PaymentErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}