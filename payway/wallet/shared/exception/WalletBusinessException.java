package com.bytz.modules.cms.payway.wallet.shared.exception;


/**
 * 钱包业务异常
 * Wallet Business Exception
 * 
 * 钱包模块的业务异常基类
 */
public class WalletBusinessException extends RuntimeException {
    
    private String errorCode;
    
    // 保持向后兼容
    public WalletBusinessException(String message) {
        super(message);
    }
    
    public WalletBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public WalletBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    public WalletBusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // 新增：基于枚举的构造器
    public WalletBusinessException(WalletErrorCode code) {
        super(code.getMessage());
        this.errorCode = code.getCode();
    }

    public WalletBusinessException(WalletErrorCode code, String message) {
        super(message);
        this.errorCode = code.getCode();
    }

    public WalletBusinessException(WalletErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code.getCode();
    }

    public String getErrorCode() {
        return errorCode;
    }
}