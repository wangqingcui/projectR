package com.bytz.modules.cms.payway.telegraphicTransfer.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 莱宝CMS—电汇-电汇主表
 * </p>
 *
 * @author Bytz
 * @since 2025-09-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("cms_telegraphic_transfer")
@ApiModel(value = "TelegraphicTransfer对象", description = "莱宝CMS—电汇-电汇主表")
public class TelegraphicTransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "电汇编号")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String telegraphicTransferNumber;

    @ApiModelProperty(value = "银行流水号")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String serialNumber;

    @ApiModelProperty(value = "客户编号")
    private String customerNumber;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "总金额")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private BigDecimal amount;

    @ApiModelProperty(value = "剩余金额")
    private BigDecimal remainingAmount;

    @ApiModelProperty(value = "已用金额")
    private BigDecimal usedAmount;

    @ApiModelProperty(value = "打款日期")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime paymentDate;

    @ApiModelProperty(value = "经销商Id")
    private String resellerId;

    @ApiModelProperty(value = "经销商名称")
    private String resellerName;

    @ApiModelProperty(value = "使用状态（未使用，已使用，用尽）")
    private String usageStatus;

    @ApiModelProperty(value = "启用状态（正常，作废）")
    private String enabledStatus;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @ApiModelProperty(value = "创建人姓名")
    @TableField(fill = FieldFill.INSERT)
    private String createByName;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @ApiModelProperty(value = "更新人姓名")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateByName;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
