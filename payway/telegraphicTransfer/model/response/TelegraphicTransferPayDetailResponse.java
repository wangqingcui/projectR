package com.bytz.modules.cms.payway.telegraphicTransfer.model.response;

import com.bytz.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * cms-backend
 *
 * @author bytz
 * @version 1.0
 * @date 2025/9/22 09:44
 */
@Data
public class TelegraphicTransferPayDetailResponse {

    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "订单Id")
    private String orderId;

    @ApiModelProperty(value = "合同编号")
    private String contractNumber;

    @ApiModelProperty(value = "订单类型")
    @Dict(dicCode = "mall_order_type")
    private String orderType;

    @Dict(dicCode = "mall_order_status")
    @ApiModelProperty(value = "订单状态（草稿；订单审核中；待盖章；待提交预付款；产品排产中；待提交尾款；已完成；已终止）")
    private String status;

    @ApiModelProperty(value = "操作类型（扣款，退款）")
    @Dict(dicCode = "details_operation_ype")
    private String operateType;

    @ApiModelProperty(value = "操作金额")
    private BigDecimal operateAmount;

    @ApiModelProperty(value = "其他费用的退款id")
    private String refundId;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否可撤销")
    private Boolean canRevoke;
}
