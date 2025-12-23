package com.bytz.modules.cms.payway.telegraphicTransfer.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * cms-backend
 *
 * @author bytz
 * @version 1.0
 * @date 2025/12/22 13:04
 */
@Data
@Builder
public class TelegraphicTransferPayDetailRO {

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

}
