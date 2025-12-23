package com.bytz.modules.cms.payway.credit.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 信用账单持久化实体
 * Credit Bill Persistence Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_credit_bill")
public class CreditBillPO {
    
    /**
     * 数据库主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 账单编号
     */
    @TableField(value = "code", updateStrategy = FieldStrategy.NEVER)
    private String code;
    
    /**
     * 关联信用钱包ID
     */
    @TableField(value = "credit_wallet_id", updateStrategy = FieldStrategy.NEVER)
    private String creditWalletId;
    
    /**
     * 交易类型
     */
    @TableField(value = "transaction_type", updateStrategy = FieldStrategy.NEVER)
    private TransactionType transactionType;
    
    /**
     * 临时授信ID
     */
    @TableField(value = "temporary_credit_id", updateStrategy = FieldStrategy.NEVER)
    private String temporaryCreditId;
    
    /**
     * 账单金额
     */
    @TableField(value = "amount", updateStrategy = FieldStrategy.NEVER)
    private BigDecimal amount;
    
    /**
     * 支付单ID
     */
    @TableField(value = "payment_id", updateStrategy = FieldStrategy.NEVER)
    private String paymentId;
    
    /**
     * 还款支付单ID
     */
    @TableField("repayment_payment_id")
    private String repaymentPaymentId;
    
    /**
     * 到期日期
     */
    @TableField("due_date")
    private LocalDate dueDate;
    
    /**
     * 还款状态
     */
    @TableField("repayment_status")
    private RepaymentStatus repaymentStatus;
    
    /**
     * 还款完成时间
     */
    @TableField("repayment_completed_time")
    private LocalDateTime repaymentCompletedTime;
    
    /**
     * 备注说明
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