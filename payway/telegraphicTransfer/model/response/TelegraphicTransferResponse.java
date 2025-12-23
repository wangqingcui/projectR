package com.bytz.modules.cms.payway.telegraphicTransfer.model.response;

import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransfer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * cms-backend
 *
 * @author bytz
 * @version 1.0
 * @date 2025/9/22 09:38
 */
@Data
public class TelegraphicTransferResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "电汇编号")
    private String telegraphicTransferNumber;

    @ApiModelProperty(value = "银行流水号")
    private String serialNumber;

    @ApiModelProperty(value = "客户编号")
    private String customerNumber;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "总金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "剩余金额")
    private BigDecimal remainingAmount;

    @ApiModelProperty(value = "已用金额")
    private BigDecimal usedAmount;

    @ApiModelProperty(value = "打款日期")
    private LocalDateTime paymentDate;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "使用明细")
    private List<TelegraphicTransferPayDetailResponse> payDetailList;

    public void setValue(TelegraphicTransfer telegraphicTransfer) {
        this.id = telegraphicTransfer.getId();
        this.telegraphicTransferNumber = telegraphicTransfer.getTelegraphicTransferNumber();
        this.serialNumber = telegraphicTransfer.getSerialNumber();
        this.customerNumber = telegraphicTransfer.getCustomerNumber();
        this.customerName = telegraphicTransfer.getCustomerName();
        this.amount = telegraphicTransfer.getAmount();
        this.remainingAmount = telegraphicTransfer.getRemainingAmount();
        this.usedAmount = telegraphicTransfer.getUsedAmount();
        this.paymentDate = telegraphicTransfer.getPaymentDate();
        this.remark = telegraphicTransfer.getRemark();
    }
}
