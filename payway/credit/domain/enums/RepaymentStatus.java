package com.bytz.modules.cms.payway.credit.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.EnumSet;

/**
 * 还款状态枚举
 * Repayment Status Enumeration
 * 
 * <p>标识账单的还款状态。</p>
 * <p>需求来源：需求文档 M04-还款服务 T18</p>
 * <p>说明：
 *   - 支持未还款、还款中、已还款三种状态
 *   - 还款分两阶段：发起还款(UNPAID→REPAYING)和完成还款(REPAYING→REPAID)
 *   - 不支持部分还款
 * </p>
 */
public enum RepaymentStatus {
    
    /**
     * 未还款
     */
    UNPAID("UNPAID", "未还款"),
    
    /**
     * 还款中（已创建还款支付单，等待支付完成）
     */
    REPAYING("REPAYING", "还款中"),
    
    /**
     * 已还款（全额）
     */
    REPAID("REPAID", "已还款");
    
    @EnumValue
    private final String code;
    private final String description;
    
    RepaymentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }

    public static EnumSet<RepaymentStatus> needUpdate= EnumSet.of(REPAYING, REPAID);
}