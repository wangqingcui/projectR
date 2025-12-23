package com.bytz.modules.cms.payway.credit.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bytz.modules.cms.payway.credit.domain.enums.TemporaryCreditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 临时授信持久化实体
 * Temporary Credit Persistence Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_temporary_credit")
public class TemporaryCreditPO {

    /**
     * 临时授信ID，数据库主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 经销商ID
     */
    @TableField("reseller_id")
    private String resellerId;

    /**
     * 关联信用钱包ID
     */
    @TableField("credit_wallet_id")
    private String creditWalletId;

    /**
     * PowerApps审批ID
     */
    @TableField(value = "approval_id", updateStrategy = FieldStrategy.NEVER)
    private String approvalId;

    /**
     * 审批通过时间
     */
    @TableField(value = "approval_time", updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime approvalTime;

    /**
     * 临时授信总金额
     */
    @TableField(value = "total_amount", updateStrategy = FieldStrategy.NEVER)
    private BigDecimal totalAmount;

    /**
     * 已使用金额
     */
    @TableField(value = "used_amount")
    private BigDecimal usedAmount;

    /**
     * 剩余可用金额
     */
    @TableField("remaining_amount")
    private BigDecimal remainingAmount;

    /**
     * 累计已还款金额
     */
    @TableField("repaid_amount")
    private BigDecimal repaidAmount;

    /**
     * 过期日期
     */
    @TableField("expiry_date")
    private LocalDate expiryDate;

    /**
     * 状态
     */
    @TableField("status")
    private TemporaryCreditStatus status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private LocalDateTime version;

    /**
     * 删除标志
     */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    /**
     * 创建人ID
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
     * 更新人ID
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