package com.bytz.modules.cms.payway.telegraphicTransfer.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
 * 莱宝CMS—电汇-电汇详细表
 * </p>
 *
 * @author Bytz
 * @since 2025-09-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("cms_telegraphic_transfer_pay_detail")
@ApiModel(value = "TelegraphicTransferPayDetail对象", description = "莱宝CMS—电汇-电汇详细表")
public class TelegraphicTransferPayDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "电汇表Id")
    private String telegraphicTransferId;

    @ApiModelProperty(value = "电汇交易记录编号")
    private String transactionCode;

    @ApiModelProperty(value = "支付单Id")
    private String paymentId;

    @ApiModelProperty(value = "操作类型（扣款，退款）")
    private String operateType;

    @ApiModelProperty(value = "操作金额")
    private BigDecimal operateAmount;

    @ApiModelProperty(value = "其他费用的退款id")
    private String refundId;

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
