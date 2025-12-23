package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.EnumSet;

/**
 * 支付状态枚举
 * Payment Status Enumeration
 * 
 * <p>标识支付单当前的支付进度状态。</p>
 * <p>来源：需求文档 T03 支付单状态管理</p>
 * <p>状态分组：
 *   - 初始状态：UNPAID
 *   - 中间状态：PAYING, PARTIAL_PAID, FAILED
 *   - 最终状态：PAID, CANCELED, TERMINATED
 * </p>
 */
public enum PaymentStatus {
    
    /**
     * 初始状态：无任何资金划转记录（未发起支付/未发起退款）
     */
    UNPAID("UNPAID", "未划转"),

    /**
     * 中间状态：资金正在划转中（支付扣款中/退款打款中，未完成最终确认）
     */
    PAYING("PAYING", "划转中"),

    /**
     * 中间状态：部分资金已完成划转（分次支付部分到账/分次退款部分退回）
     */
    PARTIAL_PAID("PARTIAL_PAID", "部分划转完成"),

    /**
     * 最终状态：全部资金已完成划转（全额支付到账/全额退款退回）
     */
    PAID("PAID", "全部划转完成"),

    /**
     * 中间状态：单次资金划转失败（支付扣款失败/退款打款失败），可重试
     */
    FAILED("FAILED", "划转失败"),

    /**
     * 最终状态：无成功划转记录，主动取消（支付单/退款单取消）
     */
    CANCELED("CANCELED", "已取消"),

    /**
     * 最终状态：有部分成功划转记录，被动终止（支付单/退款单中止）
     */
    TERMINATED("TERMINATED", "已终止");
    
    @EnumValue
    private final String code;
    private final String description;
    
    /**
     * 最终状态集合，进入此状态后不可变更
     */
    private static final EnumSet<PaymentStatus> FINAL_STATUSES =
            EnumSet.of(PAID, CANCELED, TERMINATED);
    
    /**
     * 允许支付的状态集合
     */
    public static final EnumSet<PaymentStatus> PAYABLE_STATUSES =
            EnumSet.of(UNPAID, PARTIAL_PAID, FAILED);
    
    /**
     * 允许关闭的状态集合（非PAYING、非PAID、非最终状态）
     */
    private static final EnumSet<PaymentStatus> CLOSABLE_STATUSES = 
            EnumSet.of(UNPAID, PARTIAL_PAID, FAILED);
    
    PaymentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为最终状态
     */
    public boolean isFinal() {
        return FINAL_STATUSES.contains(this);
    }
    
    /**
     * 判断是否允许支付
     */
    public boolean isPayable() {
        return PAYABLE_STATUSES.contains(this);
    }
    
    /**
     * 判断是否允许关闭
     */
    public boolean isClosable() {
        return CLOSABLE_STATUSES.contains(this);
    }
}