package com.bytz.modules.cms.payway.credit.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.modules.cms.payway.credit.domain.enums.TemporaryCreditStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 临时信用视图对象
 * Temporary Credit View Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryCreditVO {

    /**
     * 临时信用ID
     */
    @ApiModelProperty(value = "临时信用ID")
    private String id;

    /**
     * 经销商ID
     */
    @ApiModelProperty(value = "经销商ID")
    private String resellerId;

    /**
     * 临时信用总金额
     */
    @ApiModelProperty(value = "临时信用总金额")
    private BigDecimal totalAmount;

    /**
     * 已使用金额
     */
    @ApiModelProperty(value = "已使用金额")
    private BigDecimal usedAmount;

    /**
     * 剩余可用金额
     */
    @ApiModelProperty(value = "剩余可用金额")
    private BigDecimal remainingAmount;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    @Dict(dicCode = "temporary_credit_status")
    private TemporaryCreditStatus status;

    /**
     * PowerApps审批ID
     */
    @ApiModelProperty(value = "PowerApps审批ID")
    private String approvalId;

    /**
     * 审批通过时间
     */
    @ApiModelProperty(value = "审批通过时间")
    private LocalDateTime approvalTime;

    /**
     * 过期日期
     */
    @ApiModelProperty(value = "过期日期")
    private LocalDate expiryDate;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}