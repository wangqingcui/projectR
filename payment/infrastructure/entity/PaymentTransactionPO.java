package com.bytz.modules.cms.payment.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水持久化实体
 * Payment Transaction Persistence Object
 * 
 * <p>对应数据库表 cms_payment_transaction</p>
 * <p>注意：这是数据库实体，仅用于数据持久化，不包含业务逻辑</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_payment_transaction")
public class PaymentTransactionPO {
    
    /**
     * 主键ID，使用雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 流水号，业务编码（不可修改）
     */
    @TableField(value = "code", updateStrategy = FieldStrategy.NEVER)
    private String code;
    
    /**
     * 支付单ID，外键关联（不可修改）
     */
    @TableField(value = "payment_id", updateStrategy = FieldStrategy.NEVER)
    private String paymentId;
    
    /**
     * 流水状态
     */
    @TableField("transaction_status")
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额（不可修改）
     */
    @TableField(value = "transaction_amount", updateStrategy = FieldStrategy.NEVER)
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道（不可修改）
     */
    @TableField(value = "payment_channel", updateStrategy = FieldStrategy.NEVER)
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易记录ID（渠道侧唯一标识，用于定位流水，不可修改）
     */
    @TableField(value = "channel_transaction_id", updateStrategy = FieldStrategy.NEVER)
    private String channelTransactionId;
    
    /**
     * 渠道交易号
     */
    @TableField("channel_transaction_number")
    private String channelTransactionNumber;
    
    /**
     * 流水类型（PAYMENT/REFUND，不可修改）
     */
    @TableField(value = "transaction_type", updateStrategy = FieldStrategy.NEVER)
    private TransactionType transactionType;
    
    /**
     * 原支付流水ID（退款流水必填，不可修改）
     */
    @TableField(value = "original_transaction_id", updateStrategy = FieldStrategy.NEVER)
    private String originalTransactionId;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 完成时间
     */
    @TableField("completed_time")
    private LocalDateTime completedTime;
    
    /**
     * 过期时间
     */
    @TableField("expiration_time")
    private LocalDateTime expirationTime;
    
    /**
     * 业务备注
     */
    @TableField("business_remark")
    private String businessRemark;
    
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