package com.bytz.modules.cms.payway.telegraphicTransfer.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * cms-backend
 *
 * @author bytz
 * @version 1.0
 * @date 2025/9/25 11:38
 */
@Data
public class TelegraphicTransferModelView {

    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "电汇编号")
    private String telegraphicTransferNumber;

    @ApiModelProperty(value = "银行流水号")
    private String serialNumber;

    @ApiModelProperty(value = "打款日期")
    private LocalDateTime paymentDate;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "总金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "剩余金额")
    private BigDecimal remainingAmount;
}
