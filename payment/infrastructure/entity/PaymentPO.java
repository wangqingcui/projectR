package com.bytz.modules.cms.payment.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * 支付单持久化实体
 * Payment Persistence Object
 *
 * <p>对应数据库表 cms_payment</p>
 * <p>注意：这是数据库实体，仅用于数据持久化，不包含业务逻辑</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_payment")
public class PaymentPO {

    /**
     * 主键ID，使用雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 支付单号，业务编码
     */
    @TableField(value = "code", updateStrategy = FieldStrategy.NEVER)
    private String code;

    /**
     * 关联订单号
     */
    @TableField(value = "order_id", updateStrategy = FieldStrategy.NEVER)
    private String orderId;

    /**
     * 经销商ID
     */
    @TableField(value = "reseller_id", updateStrategy = FieldStrategy.NEVER)
    private String resellerId;

    /**
     * 支付金额
     */
    @TableField(value = "payment_amount", updateStrategy = FieldStrategy.NEVER)
    private BigDecimal paymentAmount;

    /**
     * 已支付金额
     */
    @TableField("paid_amount")
    private BigDecimal paidAmount;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 是否存在退款
     */
    @TableField("has_refund")
    private Boolean hasRefund;

    /**
     * 支付类型
     */
    @TableField(value = "payment_type", updateStrategy = FieldStrategy.NEVER)
    private PaymentType paymentType;

    /**
     * 支付状态
     */
    @TableField("payment_status")
    private PaymentStatus paymentStatus;

    /**
     * 业务描述
     */
    @TableField("business_desc")
    private String businessDesc;

    /**
     * 支付单原因（失败原因、取消原因等）
     */
    @TableField("reason")
    private String reason;

    /**
     * 支付截止时间
     */
    @TableField("payment_deadline")
    private LocalDateTime paymentDeadline;

    /**
     * 关联业务ID
     */
    @TableField(value = "related_business_id", updateStrategy = FieldStrategy.NEVER)
    private String relatedBusinessId;

    /**
     * 关联业务类型
     */
    @TableField(value = "related_business_type", updateStrategy = FieldStrategy.NEVER)
    private RelatedBusinessType relatedBusinessType;

    /**
     * 业务到期日
     */
    @TableField("business_expire_date")
    private LocalDate businessExpireDate;

    /**
     * 原支付单ID（退款支付单必填）
     */
    @TableField(value = "original_payment_id",updateStrategy =  FieldStrategy.NEVER)
    private String originalPaymentId;

    /**
     * 版本号（用于乐观锁）
     */
    @TableField("version")
    @Version
    private LocalDateTime version;

    /**
     * 删除标识
     */
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建人姓名
     */
    @TableField(value = "create_by_name", fill = FieldFill.INSERT)
    private String createByName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新人姓名
     */
    @TableField(value = "update_by_name", fill = FieldFill.INSERT_UPDATE)
    private String updateByName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}