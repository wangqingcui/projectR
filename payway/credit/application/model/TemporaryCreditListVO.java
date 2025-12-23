package com.bytz.modules.cms.payway.credit.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.common.entity.annotation.MPJFieldMapping;
import com.bytz.modules.cms.payway.credit.domain.enums.TemporaryCreditStatus;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.TemporaryCreditPO;
import com.bytz.modules.cms.reseller.entity.Reseller;
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
public class TemporaryCreditListVO {

    @MPJFieldMapping(entityClass = Reseller.class)
    @ApiModelProperty(value = "经销商名称")
    private String resellerName;

    /**
     * 临时信用ID
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "临时信用ID")
    private String id;

    /**
     * 经销商ID
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "经销商ID")
    private String resellerId;

    /**
     * 临时信用总金额
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "临时信用总金额")
    private BigDecimal totalAmount;

    /**
     * 已使用金额
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "已使用金额")
    private BigDecimal usedAmount;

    /**
     * 剩余可用金额
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "剩余可用金额")
    private BigDecimal remainingAmount;

    /**
     * 状态
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "状态")
    @Dict(dicCode = "temporary_credit_status")
    private TemporaryCreditStatus status;

    /**
     * PowerApps审批ID
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "PowerApps审批ID")
    private String approvalId;

    /**
     * 审批通过时间
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "审批通过时间")
    private LocalDateTime approvalTime;

    /**
     * 过期日期
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "过期日期")
    private LocalDate expiryDate;

    /**
     * 备注
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @MPJFieldMapping(entityClass = TemporaryCreditPO.class)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}