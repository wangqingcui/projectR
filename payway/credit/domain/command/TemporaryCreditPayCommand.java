package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * 单笔临时授信支付命令对象
 * Pay With Temporary Credit Command
 *
 * <p>用例来源：UC-CW-011</p>
 * <p>需求来源：T13, T15</p>
 * <p>说明：walletId用于获取termDays计算账单到期日期，钱包冻结时仍可使用临时授信支付</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryCreditPayCommand extends CreditPayCommand {

    /**
     * 账期参数，来自信用钱包，不能为空
     */
    @NotNull(message = "账期不能为空")
    private Integer termDays;
   
}