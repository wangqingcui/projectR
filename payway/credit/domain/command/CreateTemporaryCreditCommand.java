package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 创建临时授信命令对象
 * Create Temporary Credit Command
 * 
 * <p>用例来源：UC-CW-006</p>
 * <p>需求来源：T08</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemporaryCreditCommand {
    
    /**
     * 经销商ID（必填）
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
    
    /**
     * 临时授信总金额（必填，>0）
     */
    @NotNull(message = "临时授信总金额不能为空")
    @Positive(message = "临时授信总金额必须大于0")
    private BigDecimal totalAmount;
    
    /**
     * PowerApps审批ID（用于幂等性，必填）
     */
    @NotBlank(message = "审批ID不能为空")
    private String approvalId;
    
    /**
     * 审批通过时间（必填）
     */
    @NotNull(message = "审批通过时间不能为空")
    private LocalDateTime approvalTime;
    
    /**
     * 过期日期（必填，>当前日期）
     */
    @NotNull(message = "过期日期不能为空")
    private LocalDate expiryDate;
    
    /**
     * 备注（可选）
     */
    private String remark;
}
