package com.bytz.modules.cms.payway.credit.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.common.entity.annotation.MPJFieldMapping;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TransactionType;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditBillPO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 信用账单视图对象
 * Credit Bill View Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBillListVO {

    @ApiModelProperty(value = "是否可创建还款单")
    private Boolean canCreateRepayment;

    /**
     * 支付单code
     */
    @ApiModelProperty(value = "支付单code")
    @MPJFieldMapping(entityClass = PaymentPO.class, propertyName = "code")
    private String paymentCode;

    /**
     * 账单ID
     */
    @ApiModelProperty(value = "账单ID")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String id;

    /**
     * 账单编号
     */
    @ApiModelProperty(value = "账单编号")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String code;

    /**
     * 关联信用钱包ID
     */
    @ApiModelProperty(value = "关联信用钱包ID")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String creditWalletId;

    /**
     * 交易类型
     */
    @ApiModelProperty(value = "交易类型")
    @Dict(dicCode = "credit_bill_transaction_type")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private TransactionType transactionType;

    /**
     * 临时授信ID
     */
    @ApiModelProperty(value = "临时授信ID")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String temporaryCreditId;

    /**
     * 账单金额
     */
    @ApiModelProperty(value = "账单金额")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private BigDecimal amount;

    /**
     * 支付单ID
     */
    @ApiModelProperty(value = "支付单ID")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String paymentId;

    /**
     * 还款支付单ID
     */
    @ApiModelProperty(value = "还款支付单ID")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String repaymentPaymentId;

    /**
     * 到期日期
     */
    @ApiModelProperty(value = "到期日期")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private LocalDate dueDate;

    /**
     * 还款状态
     */
    @ApiModelProperty(value = "还款状态")
    @Dict(dicCode = "credit_bill_repayment_status")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private RepaymentStatus repaymentStatus;

    /**
     * 还款完成时间
     */
    @ApiModelProperty(value = "还款完成时间")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private LocalDateTime repaymentCompletedTime;

    /**
     * 备注说明
     */
    @ApiModelProperty(value = "备注说明")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String remark;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private String currency;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @MPJFieldMapping(entityClass = CreditBillPO.class)
    private LocalDateTime createTime;
}