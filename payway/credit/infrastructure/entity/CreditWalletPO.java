package com.bytz.modules.cms.payway.credit.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用钱包持久化实体
 * Credit Wallet Persistence Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_credit_wallet")
public class CreditWalletPO {
    
    /**
     * 钱包ID，数据库主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 经销商ID
     */
    @TableField("reseller_id")
    private String resellerId;
    
    /**
     * 总授信额度
     */
    @TableField("total_limit")
    private BigDecimal totalLimit;
    
    /**
     * 可用额度
     */
    @TableField("available_limit")
    private BigDecimal availableLimit;
    
    /**
     * 已使用额度
     */
    @TableField("used_limit")
    private BigDecimal usedLimit;
    
    /**
     * 账期天数
     */
    @TableField("term_days")
    private Integer termDays;
    
    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 是否可用于预付
     */
    @TableField("prepayment_enabled")
    private Boolean prepaymentEnabled;

    /**
     * 是否冻结
     */
    @TableField("frozen")
    private Boolean frozen;
    
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
