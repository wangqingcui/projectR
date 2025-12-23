package com.bytz.modules.cms.payway.credit.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.bytz.modules.cms.payway.credit.domain.enums.ManageChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理日志持久化实体
 * Manager Log Persistence Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "cms_manager_log", autoResultMap = true)
public class ManagerLogPO {
    
    /**
     * 日志ID，数据库主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 钱包ID
     */
    @TableField("credit_wallet_id")
    private String creditWalletId;
    
    /**
     * 经销商ID
     */
    @TableField("reseller_id")
    private String resellerId;
    
    /**
     * 操作类型
     */
    @TableField("operation_type")
    private ManageChangeType operationType;
    
    /**
     * 变更后状态（JSON格式）
     */
    @TableField(value = "after_state", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> afterState;
    
    /**
     * 变更原因
     */
    @TableField("reason")
    private String reason;
    
    /**
     * 操作人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * 操作人姓名
     */
    @TableField(value = "create_by_name", fill = FieldFill.INSERT)
    private String createByName;
    
    /**
     * 操作时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}