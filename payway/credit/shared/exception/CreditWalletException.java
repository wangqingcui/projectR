package com.bytz.modules.cms.payway.credit.shared.exception;

/**
 * 信用钱包异常
 * Credit Wallet Exception
 */
public class CreditWalletException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    public CreditWalletException(String message) {
        super(message);
    }
    
    public CreditWalletException(String code, String message) {
        super(code, message);
    }
    
    public CreditWalletException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CreditWalletException(CreditWalletErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
    
    public CreditWalletException(CreditWalletErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage);
    }

    public CreditWalletException(CreditWalletErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
    }
}