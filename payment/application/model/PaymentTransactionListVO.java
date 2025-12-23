package com.bytz.modules.cms.payment.application.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.bytz.common.aspect.annotation.Dict;
import com.bytz.common.entity.annotation.MPJFieldMapping;
import com.bytz.modules.cms.order.entity.Order;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.bytz.modules.cms.reseller.entity.Reseller;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水响应对象
 * Payment Transaction Response Object
 *
 * <p>用于返回支付流水的详细信息</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionListVO {

    /**
     * 经销商名字
     */
    @ExcelProperty(value = "客户名")
    @ApiModelProperty(value = "客户名")
    @MPJFieldMapping(entityClass = Reseller.class)
    private String resellerName;

    /**
     * 合同号
     */
    @ApiModelProperty(value = "合同号")
    @MPJFieldMapping(entityClass = Order.class)
    private String contractNumber;

    /**
     * 客户编码
     */
    @ExcelProperty("合同客户编码")
    @MPJFieldMapping(entityClass = Order.class)
    @ApiModelProperty(value = "客户编码")
    private String demanderNumber;

    /**
     * 客户名称
     */
    @ExcelProperty("合同客户名称")
    @MPJFieldMapping(entityClass = Order.class)
    @ApiModelProperty(value = "客户名称")
    private String demanderName;

    @ExcelProperty("销售外部编号")
    @ApiModelProperty(value = "销售外部编号")
    @MPJFieldMapping(entityClass = Order.class)
    private String salesExternalId;

    @ExcelProperty("销售")
    @ApiModelProperty(value = "销售")
    @MPJFieldMapping(entityClass = Order.class)
    private String salesName;

    /**
     * 流水ID
     */
    @ApiModelProperty(value = "流水ID")
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    private String id;

    /**
     * 订单ID
     */
    @ApiModelProperty(value = "订单ID")
    @MPJFieldMapping(entityClass = PaymentPO.class)
    private String orderId;


    /**
     * 订单类别
     */
    @ApiModelProperty(value = "订单类别")
    @MPJFieldMapping(entityClass = Order.class)
    @Dict(dicCode = "mall_order_type")
    private String orderType;

    /**
     * 经销商id
     */
    @ApiModelProperty(value = "经销商id")
    @MPJFieldMapping(entityClass = PaymentPO.class)
    private String resellerId;

    /**
     * 支付类型
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付类型")
    @Dict(dicCode = "payment_type")
    private PaymentType paymentType;


    /**
     * 流水号
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "流水号")
    private String code;

    /**
     * 支付单ID
     */
    @MPJFieldMapping(entityClass = PaymentPO.class)
    @ApiModelProperty(value = "支付单ID")
    private String paymentId;

    /**
     * 流水状态
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "流水状态")
    @Dict(dicCode = "payment_transaction_status")
    private TransactionStatus transactionStatus;

    /**
     * 交易金额
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "交易金额")
    private BigDecimal transactionAmount;

    /**
     * 支付渠道
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "支付渠道")
    @Dict(dicCode = "payment_channel")
    private PaymentChannel paymentChannel;

    /**
     * 渠道交易记录ID
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "渠道交易记录ID")
    private String channelTransactionId;

    /**
     * 渠道交易号
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "渠道交易号")
    private String channelTransactionNumber;

    /**
     * 流水类型
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "流水类型")
    @Dict(dicCode = "payment_transaction_type")
    private TransactionType transactionType;

    /**
     * 原流水ID（退款流水使用）
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "原流水ID（退款流水使用）")
    private String originalTransactionId;

    /**
     * 创建时间
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "完成时间")
    private LocalDateTime completedTime;

    /**
     * 过期时间
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "过期时间")
    private LocalDateTime expirationTime;

    /**
     * 业务备注
     */
    @MPJFieldMapping(entityClass = PaymentTransactionPO.class)
    @ApiModelProperty(value = "业务备注")
    private String businessRemark;
}