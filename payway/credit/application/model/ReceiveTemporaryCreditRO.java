package com.bytz.modules.cms.payway.credit.application.model;

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
 * 接收临时授信请求对象
 * Receive Temporary Credit Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveTemporaryCreditRO {
    
    /**
     * 经销商ID
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
    
    /**
     * 临时授信总金额
     */
    @NotNull(message = "临时授信总金额不能为空")
    @Positive(message = "临时授信总金额必须大于0")
    private BigDecimal totalAmount;
    
    /**
     * PowerApps审批ID
     */
    @NotBlank(message = "PowerApps审批ID不能为空")
    private String approvalId;
    
    /**
     * 审批通过时间
     */
    @NotNull(message = "审批通过时间不能为空")
    private LocalDateTime approvalTime;
    
    /**
     * 过期日期
     */
    @NotNull(message = "过期日期不能为空")
    private LocalDate expiryDate;
    
    /**
     * 备注
     */
    private String remark;
}
