package com.bytz.modules.cms.payment.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.common.entity.annotation.MPJFieldMapping;
import com.bytz.modules.cms.order.entity.Order;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import io.swagger.annotations.ApiModelProperty;
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
public class PaymentListVo {


    @ApiModelProperty(value = "是否可支付")
    private Boolean canPay;

    @ApiModelProperty(value = "是否可查看提货明细")
    private Boolean canDetailPick;


    /**
     * ==============================================================================
     */
    /**
     * 合同号
     */
    @MPJFieldMapping(entityClass = Order.class)
    @ApiModelProperty(value = "合同号")
    private String contractNumber;

    @MPJFieldMapping(entityClass = Order.class)
    @ApiModelProperty(value = "需方客户编码")
    private String demanderNumber;

    @MPJFieldMapping(entityClass = Order.class)
    @ApiModelProperty(value = "需方单位名称")
    private String demanderName;

    @MPJFieldMapping(entityClass = Order.class)
    @ApiModelProperty(value = "需方单位地址")
    private String demanderAddress;

    /**
     * ==============================================================================
     */

    /**
     * 支付单ID
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付单ID")
    private String id;

    /**
     * 支付单号
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付单号")
    private String code;

    /**
     * 关联订单号
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "关联订单号")
    private String orderId;

    /**
     * 经销商ID
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "经销商ID")
    private String resellerId;

    /**
     * 支付金额
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付金额")
    private BigDecimal paymentAmount;

    /**
     * 已支付金额
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "已支付金额")
    private BigDecimal paidAmount;

    /**
     * 待支付金额
     */
    @ApiModelProperty(value = "待支付金额")
    private BigDecimal pendingAmount;

    /**
     * 币种
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 是否存在退款
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "是否存在退款")
    private Boolean hasRefund;

    /**
     * 支付类型
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付类型")
    @Dict(dicCode = "payment_type")
    private PaymentType paymentType;

    /**
     * 支付状态
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付状态")
    @Dict(dicCode = "payment_status")
    private PaymentStatus paymentStatus;

    /**
     * 业务描述
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "业务描述")
    private String businessDesc;

    /**
     * 支付单原因（失败原因、取消原因等）
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付单原因（失败原因、取消原因等）")
    private String reason;

    /**
     * 支付截止时间
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付截止时间")
    private LocalDateTime paymentDeadline;

    /**
     * 关联业务ID
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "关联业务ID")
    private String relatedBusinessId;

    /**
     * 关联业务类型
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "关联业务类型")
    @Dict(dicCode = "payment_related_business_type")
    private RelatedBusinessType relatedBusinessType;

    /**
     * 业务到期日
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "业务到期日")
    private LocalDate businessExpireDate;

    /**
     * 原支付单ID（退款支付单使用）
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "原支付单ID（退款支付单使用）")
    private String originalPaymentId;

    /**
     * 创建时间
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}