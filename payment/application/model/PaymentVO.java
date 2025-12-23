package com.bytz.modules.cms.payment.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付单响应对象
 * Payment Response Object
 * 
 * <p>用于返回支付单的完整信息</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVO {
    
    /**
     * 支付单ID
     */
    private String id;
    
    /**
     * 支付单号
     */
    private String code;
    
    /**
     * 关联订单号
     */
    private String orderId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 待支付金额
     */
    private BigDecimal pendingAmount;
    
    /**
     * 币种
     */
    private String currency;
    
    /**
     * 是否存在退款
     */
    private Boolean hasRefund;
    
    /**
     * 支付类型
     */
    @Dict(dicCode = "payment_type")
    private PaymentType paymentType;
    
    /**
     * 支付状态
     */
    @Dict(dicCode = "payment_status")
    private PaymentStatus paymentStatus;
    
    /**
     * 业务描述
     */
    private String businessDesc;
    
    /**
     * 支付单原因（失败原因、取消原因等）
     */
    private String reason;
    
    /**
     * 支付截止时间
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 关联业务ID
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    @Dict(dicCode = "payment_related_business_type")
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 业务到期日
     */
    private LocalDate businessExpireDate;
    
    /**
     * 原支付单ID（退款支付单使用）
     */
    private String originalPaymentId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}