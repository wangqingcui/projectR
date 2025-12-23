package com.bytz.modules.cms.payment.application.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.bytz.common.aspect.annotation.Dict;
import com.bytz.modules.cms.common.excel.ExcelDictConvert;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * cms-backend
 *
 * @author bytz
 * @version 1.0
 * @date 2025/12/17 14:35
 */
@Data
public class PaymentTransactionListExcel {

    /**
     * 经销商名字
     */
    @ExcelProperty(value = "客户名")
    @ApiModelProperty(value = "客户名")
    private String resellerName;

    /**
     * 合同号
     */
    @ExcelProperty(value = "合同号")
    @ApiModelProperty(value = "合同号")
    private String contractNumber;

    /**
     * 客户编码
     */
    @ExcelProperty("合同客户编码")
    @ApiModelProperty(value = "客户编码")
    private String demanderNumber;

    /**
     * 客户名称
     */
    @ExcelProperty("合同客户名称")
    @ApiModelProperty(value = "客户名称")
    private String demanderName;

    @ExcelProperty("销售外部编号")
    @ApiModelProperty(value = "销售外部编号")
    private String salesExternalId;

    @ExcelProperty("销售")
    @ApiModelProperty(value = "销售")
    private String salesName;

    /**
     * 支付类型
     */
    @ExcelProperty(value = "支付类型", converter = ExcelDictConvert.class)
    @ApiModelProperty(value = "支付类型")
    @Dict(dicCode = "payment_type")
    private String paymentType;

    /**
     * 流水号
     */
    @ExcelProperty(value = "流水号")
    @ApiModelProperty(value = "流水号")
    private String code;


    /**
     * 流水状态
     */
    @ExcelProperty(value = "流水状态", converter = ExcelDictConvert.class)
    @ApiModelProperty(value = "流水状态")
    @Dict(dicCode = "payment_transaction_status")
    private String transactionStatus;

    /**
     * 交易金额
     */
    @ExcelProperty(value = "交易金额")
    @ApiModelProperty(value = "交易金额")
    private BigDecimal transactionAmount;

    /**
     * 支付渠道
     */
    @ExcelProperty(value = "支付渠道", converter = ExcelDictConvert.class)
    @ApiModelProperty(value = "支付渠道")
    @Dict(dicCode = "payment_channel")
    private String paymentChannel;

    /**
     * 渠道交易号
     */
    @ExcelProperty(value = "渠道交易号")
    @ApiModelProperty(value = "渠道交易号")
    private String channelTransactionNumber;

    /**
     * 流水类型
     */
    @ExcelProperty(value = "流水类型", converter = ExcelDictConvert.class)
    @ApiModelProperty(value = "流水类型")
    @Dict(dicCode = "payment_transaction_type")
    private String transactionType;


    /**
     * 创建时间
     */
    @ExcelProperty(value = "汇款日期")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    @ExcelProperty(value = "完成时间")
    @ApiModelProperty(value = "完成时间")
    private LocalDateTime completedTime;


    /**
     * 业务备注
     */
    @ExcelProperty(value = "业务备注")
    @ApiModelProperty(value = "业务备注")
    private String businessRemark;
}
