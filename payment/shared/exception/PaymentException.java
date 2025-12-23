package com.bytz.modules.cms.payment.shared.exception;

/**
 * 支付异常
 * Payment Exception
 * 
 * <p>支付相关的业务异常</p>
 */
public class PaymentException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentException(String message) {
        super("PAYMENT_ERROR", message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super("PAYMENT_ERROR", message, cause);
    }
    
    public PaymentException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
    
    public PaymentException(PaymentErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage);
    }
    
    public PaymentException(PaymentErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
    }
}
