package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 支付类型枚举
 * Payment Type Enumeration
 *
 * <p>区分支付单的业务类型。</p>
 * <p>来源：需求文档第五章数据模型 - 支付单(Payment)表 paymentType字段</p>
 * <p>术语参考：Glossary.md - "支付类型(Payment Type)"</p>
 */
public enum PaymentType {

    /**
     * 预付款 - 订单确认后的首期付款
     */
    ADVANCE_PAYMENT("ADVANCE_PAYMENT", "预付款"),

    /**
     * 尾款 - 发货或完工后的最终付款
     */
    FINAL_PAYMENT("FINAL_PAYMENT", "尾款"),

    /**
     * 其他费用 - 其他类型的费用支付
     */
    OTHER_PAYMENT("OTHER_PAYMENT", "其他费用"),

    /**
     * 信用还款 - 企业对信用额度的还款操作
     */
    CREDIT_REPAYMENT("CREDIT_REPAYMENT", "信用还款"),

    /**
     * 退款 - 退款支付单类型
     */
    REFUND("REFUND", "退款");

    @EnumValue
    private final String code;
    private final String description;

    PaymentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRefund() {
        return this.equals(REFUND);
    }

    // 正向支付
    public boolean isPayment() {
        return !this.isRefund();
    }

    public boolean isAdvancePayment() {
        return this.equals(ADVANCE_PAYMENT);
    }
    // 信用还款
    public boolean isCreditRepayment() {
        return this.equals(CREDIT_REPAYMENT);
    }
}
