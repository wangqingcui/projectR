package com.bytz.modules.cms.payway.wallet.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包数据对象
 * Wallet Data Object
 * 
 * 对应数据库表 cms_wallet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_wallet")
public class WalletEntity {
    
    /**
     * 钱包ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 经销商ID
     */
    @TableField("reseller_id")
    private String resellerId;
    
    /**
     * 当前余额
     */
    @TableField("balance")
    private BigDecimal balance;
    
    /**
     * 钱包状态
     */
    @TableField("status")
    private WalletStatus status;
    
    /**
     * 币种（固定为CNY）
     */
    @TableField(value = "currency", fill = FieldFill.INSERT)
    private String currency;
    
    /**
     * 版本号（用于乐观锁）
     */
    @TableField("version")
    @Version
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
    private LocalDateTime createdTime;
    
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
    private LocalDateTime updatedTime;
}
