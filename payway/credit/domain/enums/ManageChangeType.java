package com.bytz.modules.cms.payway.credit.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 管理日志操作类型枚举
 * Management Change Type Enumeration
 * 
 * <p>用于管理日志表。</p>
 * <p>需求来源：需求文档4.1节T07 - 钱包管理日志记录</p>
 */
public enum ManageChangeType {
    
    /**
     * 额度变更
     */
    LIMIT_CHANGE("LIMIT_CHANGE", "额度变更"),
    
    /**
     * 账期变更
     */
    TERM_CHANGE("TERM_CHANGE", "账期变更"),
    
    /**
     * 冻结状态变更
     */
    FREEZE_STATUS_CHANGE("FREEZE_STATUS_CHANGE", "冻结状态变更"),
    
    /**
     * 启用状态变更
     */
    ENABLE_STATUS_CHANGE("ENABLE_STATUS_CHANGE", "启用状态变更"),
    
    /**
     * 预付状态变更
     */
    PREPAYMENT_STATUS_CHANGE("PREPAYMENT_STATUS_CHANGE", "预付状态变更");
    
    @EnumValue
    private final String code;
    private final String description;
    
    ManageChangeType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
